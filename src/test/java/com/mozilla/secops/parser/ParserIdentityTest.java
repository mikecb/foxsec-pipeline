package com.mozilla.secops.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mozilla.secops.identity.IdentityManager;
import org.junit.Test;

public class ParserIdentityTest {
  public ParserIdentityTest() {}

  @Test
  public void testOpenSSHStackdriverWithIdentity() throws Exception {
    String buf =
        "{\"insertId\":\"f8p4mz1a3ldcos1xz\",\"labels\":{\"compute.googleapis.com/resource_"
            + "name\":\"emit-bastion\"},\"logName\":\"projects/sandbox-00/logs/syslog\",\"receiveTimestamp\""
            + ":\"2018-09-20T18:43:38.318580313Z\",\"resource\":{\"labels\":{\"instance_id\":\"9999999999999"
            + "999999\",\"project_id\":\"sandbox-00\",\"zone\":\"us-east1-b\"},\"type\":\"gce_instance\"},\""
            + "textPayload\":\"Sep 18 22:15:38 emit-bastion sshd[2644]: Accepted publickey for riker from 12"
            + "7.0.0.1 port 58530 ssh2: RSA SHA256:dd/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"timestamp"
            + "\":\"2018-09-18T22:15:38Z\"}";
    IdentityManager mgr = IdentityManager.load("/testdata/identitymanager.json");
    Parser p = new Parser();
    p.setIdentityManager(mgr);
    assertNotNull(p);
    Event e = p.parse(buf);
    assertNotNull(e);
    assertEquals(Payload.PayloadType.OPENSSH, e.getPayloadType());
    OpenSSH o = e.getPayload();
    assertNotNull(o);
    assertEquals("riker", o.getUser());
    assertEquals("publickey", o.getAuthMethod());
    assertEquals("127.0.0.1", o.getSourceAddress());
    assertNull(o.getSourceAddressCity());
    assertNull(o.getSourceAddressCountry());
    Normalized n = e.getNormalized();
    assertNotNull(n);
    assertTrue(n.isOfType(Normalized.Type.AUTH));
    assertEquals("riker", n.getSubjectUser());
    assertEquals("wriker@mozilla.com", n.getSubjectUserIdentity());
    assertEquals("127.0.0.1", n.getSourceAddress());
  }

  @Test
  public void testCloudtrailStackdriverConsoleAuthWithIdentity() throws Exception {
    String buf =
        "{\"insertId\": \"1culjbag27h3ns1\",\"jsonPayload\": {\"awsRegion\":\"us-west-2\",\"eventID\": "
            + "\"00000000-0000-0000-0000-000000000000\",\"eventName\":\"ConsoleLogin\", "
            + "\"eventSource\":\"signin.amazonaws.com\",\"eventTime\":\"2018-06-26T06:00:13Z\", "
            + "\"eventType\":\"AwsConsoleSignIn\",\"eventVersion\":\"1.05\", "
            + "\"recipientAccountId\":\"123456789\",\"responseElements\": "
            + "{\"ConsoleLogin\":\"Success\"},\"sourceIPAddress\":\"127.0.0.1\",\"userAgent\": "
            + "\"Mozilla/5.0(Macintosh;IntelMacOSX10.13;rv:62.0)Gecko/20100101Firefox/62.0\", "
            + "\"userIdentity\":{\"accountId\":\"123456789\",\"arn\": "
            + "\"arn:aws:iam::123456789:user/riker\",\"principalId\":\"XXXXXXXXXXXXXXXXXXXXX\", "
            + "\"type\":\"IAMUser\",\"userName\":\"riker\",\"sessionContext\": {\"attributes\": "
            + "{\"mfaAuthenticated\": \"true\",\"creationDate\": \"2018-07-02T18:14:11Z\"}}}}, \"resource\": "
            + "{\"type\": \"project\", \"labels\": {\"project_id\": \"sandbox\"}}, \"timestamp\": "
            + "\"2018-10-11T18:41:09.542038318Z\", \"logName\": \"projects/sandbox/logs/ctstreamer\", "
            + "\"receiveTimestamp\": \"2018-10-11T18:41:12.665161934Z\"}";

    IdentityManager mgr = IdentityManager.load("/testdata/identitymanager.json");
    Parser p = new Parser();
    p.setIdentityManager(mgr);
    assertNotNull(p);
    Event e = p.parse(buf);
    assertNotNull(e);
    assertEquals(Payload.PayloadType.CLOUDTRAIL, e.getPayloadType());
    Cloudtrail ct = e.getPayload();
    assertNotNull(ct);
    assertEquals("riker", ct.getUser());
    assertEquals("127.0.0.1", ct.getSourceAddress());
    assertNull(ct.getSourceAddressCity());
    assertNull(ct.getSourceAddressCountry());
    Normalized n = e.getNormalized();
    assertNotNull(n);
    assertTrue(n.isOfType(Normalized.Type.AUTH));
    assertEquals("riker", n.getSubjectUser());
    assertEquals("127.0.0.1", n.getSourceAddress());
    assertEquals("wriker@mozilla.com", n.getSubjectUserIdentity());
    assertEquals("riker-vacationing-on-risa", n.getObject());
    assertNull(n.getSourceAddressCity());
    assertNull(n.getSourceAddressCountry());
  }

  @Test
  public void testDuopullStackdriverAdminLoginWithIdentity() throws Exception {
    String buf =
        "{\"insertId\": \"gh39djdfyz4lya\",\"jsonPayload\": {\"Fields\": {\"event_object\": null,"
            + "\"event_username\": \"riker\",\"event_description_factor\": \"push\",\"msg\": \"duopull event\","
            + "\"event_timestamp\": 1556216112,\"event_description_device\": \"555-555-5555\","
            + "\"event_action\": \"admin_login\",\"event_description_primary_auth_method\": \"Password\","
            + "\"path\": \"/admin/v1/logs/administrator\",\"event_description_ip_address\": \"127.0.0.1\""
            + "},\"Pid\": 1,\"Hostname\": \"localhost\","
            + "\"EnvVersion\": \"2.0\",\"Type\": \"app.log\",\"Timestamp\": 1556216660526927000,"
            + "\"Severity\": 6,\"Logger\": \"duopull\",\"Time\": \"2019-04-25T18:24:20Z\"},"
            + "\"resource\": {\"type\": \"project\",\"labels\": {\"project_id\": \"pipeline\"}},"
            + "\"timestamp\": \"2019-04-25T18:24:20.527024712Z\","
            + "\"logName\": \"projects/pipeline/logs/duopull\","
            + "\"receiveTimestamp\": \"2019-04-25T18:24:20.555273442Z\"}";

    IdentityManager mgr = IdentityManager.load("/testdata/identitymanager.json");
    Parser p = new Parser();
    p.setIdentityManager(mgr);
    assertNotNull(p);
    Event e = p.parse(buf);
    assertNotNull(e);
    assertEquals(Payload.PayloadType.DUOPULL, e.getPayloadType());
    assertEquals("2019-04-25T18:15:12.000Z", e.getTimestamp().toString());
    Duopull d = e.getPayload();
    assertNotNull(d);
    com.mozilla.secops.parser.models.duopull.Duopull data = d.getDuopullData();
    assertNotNull(data);
    assertEquals("riker", data.getEventUsername());
    assertEquals("127.0.0.1", data.getEventDescriptionIpAddress());
    Normalized n = e.getNormalized();
    assertNotNull(n);
    assertTrue(n.isOfType(Normalized.Type.AUTH));
    assertEquals("riker", n.getSubjectUser());
    assertEquals("127.0.0.1", n.getSourceAddress());
    assertEquals("wriker@mozilla.com", n.getSubjectUserIdentity());
    assertEquals("duo_admin_login", n.getObject());
    assertNull(n.getSourceAddressCity());
    assertNull(n.getSourceAddressCountry());
  }
}
