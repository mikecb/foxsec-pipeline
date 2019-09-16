package com.mozilla.secops.customs;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mozilla.secops.alert.Alert;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/** Alert format used for notifications to FxA */
public class CustomsAlert implements Serializable {
  private static final long serialVersionUID = 1L;

  /** Severity of a given alert */
  public enum AlertSeverity {
    /** Informational */
    @JsonProperty("info")
    INFORMATIONAL,
    /** Warning */
    @JsonProperty("warn")
    WARNING,
    /** Critical */
    @JsonProperty("critical")
    CRITICAL
  }

  /** Indicator types */
  public enum IndicatorType {
    /** Source IP address */
    @JsonProperty("sourceaddress")
    SOURCEADDRESS,
    /** Account ID/email */
    @JsonProperty("email")
    EMAIL,
    /** Account UID */
    @JsonProperty("uid")
    UID
  }

  /** Alert actions */
  public enum AlertAction {
    /** Consider a report only */
    @JsonProperty("report")
    REPORT,
    /** Indicator should be suspected */
    @JsonProperty("suspect")
    SUSPECT,
    /** Indicator should be blocked temporarily */
    @JsonProperty("block")
    BLOCK,
    /** Indicator should be disabled permanently */
    @JsonProperty("disable")
    DISABLE
  }

  private static HashMap<String, String> heuristicDescriptions =
      new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
          put(
              "account_creation_abuse",
              "Large number of accounts created in one session from a single IP address");
          put(
              "account_creation_abuse_distributed",
              "Large number of very similar accounts created in fixed time frame from different addresses");
        }
      };

  private DateTime timestamp;
  private UUID alertId;
  private IndicatorType indicatorType;
  private String indicator;
  private AlertSeverity severity;
  private Integer confidence;
  private String heuristic;
  private String heuristicDescription;
  private String reason;
  private AlertAction suggestedAction;
  private HashMap<String, Object> details;

  /**
   * Convert an {@link Alert} into one or more instances of {@link CustomsAlert}
   *
   * <p>This method will convert an alert that has been generated by the customs pipeline into one
   * or more CustomsAlert objects which can then be submitted directly to the FxA service.
   *
   * <p>This method contains special handling for the various types of alert messages that can be
   * generated by the Customs pipeline
   *
   * @param a Alert
   * @return ArrayList of CustomsAlert, or null if conversion was not possible
   */
  public static ArrayList<CustomsAlert> fromAlert(Alert a) {
    if ((a.getCategory() == null) || (!a.getCategory().equals("customs"))) {
      throw new RuntimeException(String.format("unexpected category type %s", a.getCategory()));
    }
    switch (a.getMetadataValue("customs_category")) {
      case "account_creation_abuse":
        return convertAccountCreationAbuse(a);
      case "account_creation_abuse_distributed":
        return convertAccountCreationAbuseDistributed(a);
    }
    return null;
  }

  private static CustomsAlert baseAlert(Alert a) {
    CustomsAlert ret = new CustomsAlert();
    ret.setTimestamp(a.getTimestamp());
    ret.details.put("origin_alert_id", a.getAlertId());

    // Default to a mid-range confidence value
    ret.setConfidence(50);

    // Set the heuristic and description fields based on the category metadata.
    ret.setHeuristic(a.getMetadataValue("customs_category"));
    String desc = heuristicDescriptions.get(a.getMetadataValue("customs_category"));
    if (desc == null) {
      ret.setHeuristicDescription("unknown");
    } else {
      ret.setHeuristicDescription(desc);
    }
    return ret;
  }

  /**
   * Convert an account creation abuse alert
   *
   * <p>The IP address will be noted as suspected, in addition to the accounts that have been
   * created.
   *
   * @param a Alert
   * @return ArrayList of CustomsAlert
   */
  public static ArrayList<CustomsAlert> convertAccountCreationAbuse(Alert a) {
    ArrayList<CustomsAlert> ret = new ArrayList<>();

    String reason =
        String.format(
            "%s created %s accounts in a single session",
            a.getMetadataValue("sourceaddress"), a.getMetadataValue("count"));

    // Create alert for address
    CustomsAlert buf = baseAlert(a);
    buf.setSeverity(AlertSeverity.WARNING);
    buf.setIndicatorType(IndicatorType.SOURCEADDRESS);
    buf.setIndicator(a.getMetadataValue("sourceaddress"));
    buf.setSuggestedAction(AlertAction.SUSPECT);
    buf.setReason(reason);
    ret.add(buf);

    // Create alert for each account identifier
    String[] parts = a.getMetadataValue("email").split(", ?");
    for (String i : parts) {
      buf = baseAlert(a);
      buf.setSeverity(AlertSeverity.WARNING);
      buf.setIndicatorType(IndicatorType.EMAIL);
      buf.setIndicator(i);
      buf.setSuggestedAction(AlertAction.SUSPECT);
      buf.setReason(reason);
      ret.add(buf);
    }

    return ret;
  }

  /**
   * Convert an account creation abuse distributed alert
   *
   * <p>The IP address will be noted as suspected, in addition to the account in the alert.
   *
   * @param a Alert
   * @return ArrayList of CustomsAlert
   */
  public static ArrayList<CustomsAlert> convertAccountCreationAbuseDistributed(Alert a) {
    ArrayList<CustomsAlert> ret = new ArrayList<>();

    String reason =
        String.format(
            "%d very similar accounts to %s created in fixed time frame",
            Integer.parseInt(a.getMetadataValue("count")) - 1, a.getMetadataValue("email"));

    // Create alert for address
    CustomsAlert buf = baseAlert(a);
    buf.setSeverity(AlertSeverity.WARNING);
    buf.setIndicatorType(IndicatorType.SOURCEADDRESS);
    buf.setIndicator(a.getMetadataValue("sourceaddress"));
    buf.setSuggestedAction(AlertAction.SUSPECT);
    buf.setReason(reason);
    ret.add(buf);

    // Create alert for main account identifier included in alert. We don't create alerts from the
    // similar email field as those will be handled with the other input alerts that are created.
    buf = baseAlert(a);
    buf.setSeverity(AlertSeverity.WARNING);
    buf.setIndicatorType(IndicatorType.EMAIL);
    buf.setIndicator(a.getMetadataValue("email"));
    buf.setSuggestedAction(AlertAction.SUSPECT);
    buf.setReason(reason);
    ret.add(buf);

    return ret;
  }

  /** Construct new {@link CustomsAlert} */
  public CustomsAlert() {
    alertId = UUID.randomUUID();
    timestamp = new DateTime(DateTimeZone.UTC);
    details = new HashMap<String, Object>();
    severity = AlertSeverity.INFORMATIONAL;
    suggestedAction = AlertAction.REPORT;
  }

  /**
   * Set timestamp
   *
   * @param timestamp DateTime
   */
  @JsonProperty("timestamp")
  public void setTimestamp(DateTime timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Get timestamp
   *
   * @return DateTime
   */
  public DateTime getTimestamp() {
    return timestamp;
  }

  /**
   * Set UUID
   *
   * @param alertId UUID
   */
  @JsonProperty("id")
  public void setId(UUID alertId) {
    this.alertId = alertId;
  }

  /**
   * Get UUID
   *
   * @return UUID
   */
  public UUID getId() {
    return alertId;
  }

  /**
   * Set indicator type
   *
   * @param indicatorType IndicatorType
   */
  @JsonProperty("indicator_type")
  public void setIndicatorType(IndicatorType indicatorType) {
    this.indicatorType = indicatorType;
  }

  /**
   * Get indicator type
   *
   * @return IndicatorType
   */
  public IndicatorType getIndicatorType() {
    return indicatorType;
  }

  /**
   * Set indicator
   *
   * @param indicator String
   */
  @JsonProperty("indicator")
  public void setIndicator(String indicator) {
    this.indicator = indicator;
  }

  /**
   * Get indicator
   *
   * @return String
   */
  public String getIndicator() {
    return indicator;
  }

  /**
   * Set severity
   *
   * @param severity AlertSeverity
   */
  @JsonProperty("severity")
  public void setSeverity(AlertSeverity severity) {
    this.severity = severity;
  }

  /**
   * Get severity
   *
   * @return AlertSeverity
   */
  public AlertSeverity getSeverity() {
    return severity;
  }

  /**
   * Set confidence
   *
   * @param confidence Integer
   */
  @JsonProperty("confidence")
  public void setConfidence(Integer confidence) {
    this.confidence = confidence;
  }

  /**
   * Get confidence
   *
   * @return Integer
   */
  public Integer getConfidence() {
    return confidence;
  }

  /**
   * Set heuristic
   *
   * @param heuristic String
   */
  @JsonProperty("heuristic")
  public void setHeuristic(String heuristic) {
    this.heuristic = heuristic;
  }

  /**
   * Get heuristic
   *
   * @return String
   */
  public String getHeuristic() {
    return heuristic;
  }

  /**
   * Set heuristic description
   *
   * @param heuristicDescription String
   */
  @JsonProperty("heuristic_description")
  public void setHeuristicDescription(String heuristicDescription) {
    this.heuristicDescription = heuristicDescription;
  }

  /**
   * Get heuristic description
   *
   * @return String
   */
  public String getHeuristicDescription() {
    return heuristicDescription;
  }

  /**
   * Set reason
   *
   * @param reason String
   */
  @JsonProperty("reason")
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * Get reason
   *
   * @return String
   */
  public String getReason() {
    return reason;
  }

  /**
   * Set suggested action
   *
   * @param suggestedAction AlertAction
   */
  @JsonProperty("suggested_action")
  public void setSuggestedAction(AlertAction suggestedAction) {
    this.suggestedAction = suggestedAction;
  }

  /**
   * Get suggested action
   *
   * @return AlertAction
   */
  public AlertAction getSuggestedAction() {
    return suggestedAction;
  }

  /**
   * Set details map
   *
   * @param details HashMap
   */
  @JsonProperty("details")
  public void setDetails(HashMap<String, Object> details) {
    this.details = details;
  }

  /**
   * Get details map
   *
   * @return HashMap
   */
  public HashMap<String, Object> getDetails() {
    return details;
  }

  /**
   * Return JSON string representation.
   *
   * @return String or null if serialization fails.
   */
  public String toJSON() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JodaModule());
    mapper.configure(
        com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.setSerializationInclusion(Include.NON_NULL);
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException exc) {
      return null;
    }
  }
}
