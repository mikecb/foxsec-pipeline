package com.mozilla.secops.gatekeeper;

import com.amazonaws.services.guardduty.model.Finding;
import com.mozilla.secops.IOOptions;
import com.mozilla.secops.alert.Alert;
import com.mozilla.secops.alert.AlertSuppressor;
import com.mozilla.secops.parser.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.*;
import org.joda.time.DateTime;

/** Implements various transforms on AWS GuardDuty {@link Finding} Events */
public class GuardDutyTransforms implements Serializable {
  private static final long serialVersionUID = 1L;

  /** Runtime options for GuardDuty Transforms */
  public interface Options extends PipelineOptions, IOOptions {
    @Description(
        "Ignore GuardDuty Findings for any finding types that match regex (multiple allowed)")
    String[] getIgnoreGDFindingTypeRegex();

    void setIgnoreGDFindingTypeRegex(String[] value);

    @Description(
        "Escalate GuardDuty Findings for any finding types that match regex (multiple allowed)")
    String[] getEscalateGDFindingTypeRegex();

    void setEscalateGDFindingTypeRegex(String[] value);

    @Default.Long(60 * 15) // 15 minutes
    @Description("Suppress alert generation for repeated GuardDuty Findings within this value")
    Long getAlertSuppressionSeconds();

    void setAlertSuppressionSeconds(Long value);
  }

  /** Extract GuardDuty Findings */
  public static class ExtractFindings extends PTransform<PCollection<Event>, PCollection<Event>> {
    private static final long serialVersionUID = 1L;

    private List<Pattern> exclude;

    /**
     * static initializer for filter
     *
     * @param opts {@link Options} pipeline options
     */
    public ExtractFindings(Options opts) {
      String[] ignoreRegexes = opts.getIgnoreGDFindingTypeRegex();
      exclude = new ArrayList<Pattern>();
      if (ignoreRegexes != null) {
        for (String s : ignoreRegexes) {
          exclude.add(Pattern.compile(s));
        }
      }
    }

    @Override
    public PCollection<Event> expand(PCollection<Event> input) {
      return input.apply(
          ParDo.of(
              new DoFn<Event, Event>() {
                private static final long serialVersionUID = 1L;

                @ProcessElement
                public void processElement(ProcessContext c) {
                  Event e = c.element();
                  if (!e.getPayloadType().equals(Payload.PayloadType.GUARDDUTY)) {
                    return;
                  }
                  GuardDuty gde = e.getPayload();
                  if (gde == null) {
                    return;
                  }
                  Finding f = gde.getFinding();
                  if (f == null || f.getType() == null) {
                    return;
                  }
                  for (Pattern p : exclude) {
                    if (p.matcher(f.getType()).matches()) {
                      return;
                    }
                  }
                  c.output(e);
                }
              }));
    }
  }

  /** Generate Alerts for relevant Findings */
  public static class GenerateAlerts extends PTransform<PCollection<Event>, PCollection<Alert>> {
    private static final long serialVersionUID = 1L;

    private static final String alertCategory = "gatekeeper:aws";

    private List<Pattern> escalate;
    private String critNotifyEmail;

    /**
     * static initializer for alert generation / escalation
     *
     * @param opts {@link Options} pipeline options
     */
    public GenerateAlerts(Options opts) {
      critNotifyEmail = opts.getCriticalNotificationEmail();
      String[] escalateRegexes = opts.getEscalateGDFindingTypeRegex();

      escalate = new ArrayList<Pattern>();
      if (escalateRegexes != null) {
        for (String s : escalateRegexes) {
          escalate.add(Pattern.compile(s));
        }
      } else {
        escalate.add(Pattern.compile(".+"));
      }
    }

    private void addEscalationMetadata(Alert a) {
      if (critNotifyEmail != null) {
        a.addMetadata("notify_email_direct", critNotifyEmail);
      }
    }

    @Override
    public PCollection<Alert> expand(PCollection<Event> input) {
      return input.apply(
          ParDo.of(
              new DoFn<Event, Alert>() {
                private static final long serialVersionUID = 1L;

                @ProcessElement
                public void processElement(ProcessContext c) {
                  Event e = c.element();
                  if (!e.getPayloadType().equals(Payload.PayloadType.GUARDDUTY)) {
                    return;
                  }
                  GuardDuty gd = e.getPayload();
                  if (gd == null) {
                    return;
                  }
                  Finding f = gd.getFinding();
                  if (f == null) {
                    return;
                  }
                  Alert a = new Alert();
                  a.setSummary(
                      String.format(
                          "suspicious activity detected in aws account %s: %s",
                          f.getAccountId(), f.getTitle()));
                  a.setTimestamp(DateTime.parse(f.getUpdatedAt()));
                  a.setCategory(alertCategory);
                  a.setSeverity(Alert.AlertSeverity.CRITICAL);
                  a.addMetadata("aws_account", f.getAccountId());
                  a.addMetadata("aws_region", f.getRegion());
                  a.addMetadata("description", f.getDescription());
                  a.addMetadata("finding_aws_severity", Double.toString(f.getSeverity()));
                  a.addMetadata("finding_type", f.getType());
                  a.addMetadata("finding_id", f.getId());
                  for (Pattern p : escalate) {
                    if (p.matcher(f.getType()).matches()) {
                      addEscalationMetadata(a);
                      break;
                    }
                  }
                  c.output(a);
                }
              }));
    }
  }

  /**
   * Suppress Alerts for repeated GuardDuty Findings.
   *
   * <p>A "repeated finding" in GuardDuty means the same (potential) bad actor is performing the
   * same action against the same resource in your AWS environment. Findings are uniquely identified
   * by their "id".
   *
   * <p>GuardDuty has a built-in setting to avoid emitting a new CloudWatch event for repeated
   * findings within a certain window of time. Valid values for that window are 15 minutes, 1 hour,
   * or 6 hours (default).
   * https://docs.aws.amazon.com/guardduty/latest/ug/guardduty_findings_cloudwatch.html#guardduty_findings_cloudwatch_notification_frequency
   *
   * <p>This transform adds a second layer of protection against generation of alerts for repeated
   * findings
   */
  public static class SuppressAlerts extends PTransform<PCollection<Alert>, PCollection<Alert>> {
    private static final long serialVersionUID = 1L;
    private static final String suppressionStateMetadataKey = "finding_id";

    private static Long alertSuppressionWindow;

    /**
     * static initializer for alert suppression
     *
     * @param opts {@link Options} pipeline options
     */
    public SuppressAlerts(Options opts) {
      alertSuppressionWindow = opts.getAlertSuppressionSeconds();
    }

    @Override
    public PCollection<Alert> expand(PCollection<Alert> input) {
      return input
          .apply(
              ParDo.of(
                  new DoFn<Alert, KV<String, Alert>>() {
                    private static final long serialVersionUID = 1L;

                    @ProcessElement
                    public void processElement(ProcessContext c) {
                      Alert a = c.element();
                      if (a == null || a.getMetadataValue(suppressionStateMetadataKey) == null) {
                        return;
                      }
                      c.output(KV.of(a.getMetadataValue(suppressionStateMetadataKey), a));
                    }
                  }))
          .apply(ParDo.of(new AlertSuppressor(alertSuppressionWindow)));
    }
  }
}
