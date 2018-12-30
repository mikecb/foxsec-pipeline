package com.mozilla.secops.authprofile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mozilla.secops.TestUtil;
import com.mozilla.secops.alert.Alert;
import com.mozilla.secops.parser.Event;
import com.mozilla.secops.parser.Normalized;
import java.util.Collection;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class TestAuthProfile {
  @Rule public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  private void testEnv() {
    environmentVariables.set("DATASTORE_EMULATOR_HOST", "localhost:8081");
    environmentVariables.set("DATASTORE_EMULATOR_HOST_PATH", "localhost:8081/datastore");
    environmentVariables.set("DATASTORE_HOST", "http://localhost:8081");
    environmentVariables.set("DATASTORE_PROJECT_ID", "foxsec-pipeline");
  }

  public TestAuthProfile() {}

  private AuthProfile.AuthProfileOptions getTestOptions() {
    AuthProfile.AuthProfileOptions ret =
        PipelineOptionsFactory.as(AuthProfile.AuthProfileOptions.class);
    ret.setDatastoreNamespace("testauthprofileanalyze");
    ret.setDatastoreKind("authprofile");
    ret.setIdentityManagerPath("/testdata/identitymanager.json");
    return ret;
  }

  @Rule public final transient TestPipeline p = TestPipeline.create();

  @Test
  public void noopPipelineTest() throws Exception {
    p.run().waitUntilFinish();
  }

  @Test
  public void parseAndWindowTest() throws Exception {
    PCollection<String> input = TestUtil.getTestInput("/testdata/authprof_buffer1.txt", p);

    PCollection<KV<String, Iterable<Event>>> res = input.apply(new AuthProfile.ParseAndWindow());
    PAssert.thatMap(res)
        .satisfies(
            results -> {
              Iterable<Event> edata = results.get("riker");
              assertNotNull(edata);
              assertTrue(edata instanceof Collection);

              Event[] e = ((Collection<Event>) edata).toArray(new Event[0]);
              assertEquals(e.length, 5);

              Normalized n = e[0].getNormalized();
              assertNotNull(n);
              assertTrue(n.isOfType(Normalized.Type.AUTH));
              assertEquals("216.160.83.56", n.getSourceAddress());
              assertEquals("riker", n.getSubjectUser());

              return null;
            });

    p.run().waitUntilFinish();
  }

  @Test
  public void analyzeTest() throws Exception {
    testEnv();
    AuthProfile.AuthProfileOptions options = getTestOptions();
    PCollection<String> input = TestUtil.getTestInput("/testdata/authprof_buffer1.txt", p);

    PCollection<Alert> res =
        input
            .apply(new AuthProfile.ParseAndWindow())
            .apply(ParDo.of(new AuthProfile.Analyze(options)));

    PAssert.that(res)
        .satisfies(
            results -> {
              long newCnt = 0;
              long infoCnt = 0;
              for (Alert a : results) {
                assertEquals("authprofile", a.getCategory());
                String actualSummary = a.getSummary();
                if (actualSummary.equals("riker authenticated to emit-bastion from Milton/US")) {
                  infoCnt++;
                  assertEquals(Alert.AlertSeverity.INFORMATIONAL, a.getSeverity());
                  assertEquals("wriker@mozilla.com", a.getMetadataValue("identity_key"));
                  assertNull(a.getMetadataValue("notify_email_direct"));
                } else if (actualSummary.equals(
                    "riker authenticated to emit-bastion from new source" + ", Milton/US")) {
                  newCnt++;
                  assertEquals(Alert.AlertSeverity.WARNING, a.getSeverity());
                  assertEquals(
                      "holodeck-riker@mozilla.com", a.getMetadataValue("notify_email_direct"));
                  assertEquals("wriker@mozilla.com", a.getMetadataValue("identity_key"));
                }
              }
              assertEquals(1L, newCnt);
              assertEquals(4L, infoCnt);
              return null;
            });

    p.run().waitUntilFinish();
  }
}
