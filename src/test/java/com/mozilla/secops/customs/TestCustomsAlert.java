package com.mozilla.secops.customs;

import static org.junit.Assert.assertEquals;

import com.mozilla.secops.alert.Alert;
import java.util.ArrayList;
import org.junit.Test;

public class TestCustomsAlert {
  @Test
  public void testAlertConversion() throws Exception {
    String buf =
        "{\"severity\":\"info\",\"id\":\"85e899ac-28fa-46d6-84c1-36c2061eed49\",\"summary"
            + "\":\"test suspicious account creation, 216.160.83.56 3\",\"category\":\"customs"
            + "\",\"timestamp\":\"1970-01-01T00:00:00.000Z\",\"metadata\":[{\"key\":\"notify_m"
            + "erge\",\"value\":\"account_creation_abuse\"},{\"key\":\"customs_category\",\"va"
            + "lue\":\"account_creation_abuse\"},{\"key\":\"sourceaddress\",\"value\":\"216.16"
            + "0.83.56\"},{\"key\":\"count\",\"value\":\"3\"},{\"key\":\"email\",\"value\":\"u"
            + "ser@mail.com, user.1@mail.com, user.1.@mail.com\"}]}";
    ArrayList<CustomsAlert> c = CustomsAlert.fromAlert(Alert.fromJSON(buf));
    assertEquals(4, c.size());

    buf =
        "{\"severity\":\"info\",\"id\":\"6f520812-7081-4e7e-9b6c-c25bf69f6744\",\"summa"
            + "ry\":\"test suspicious distributed account creation, 216.160.83.54 6\",\"categ"
            + "ory\":\"customs\",\"timestamp\":\"2019-09-16T18:13:39.390Z\",\"metadata\":[{\""
            + "key\":\"notify_merge\",\"value\":\"account_creation_abuse_distributed\"},{\"ke"
            + "y\":\"customs_category\",\"value\":\"account_creation_abuse_distributed\"},{\""
            + "key\":\"count\",\"value\":\"6\"},{\"key\":\"sourceaddress\",\"value\":\"216.16"
            + "0.83.54\"},{\"key\":\"email\",\"value\":\"user6@mail.com\"},{\"key\":\"email_s"
            + "imilar\",\"value\":\"user3@mail.com, user1@mail.com, user2@mail.com, user4@mai"
            + "l.com, user5@mail.com\"}]}";
    c = CustomsAlert.fromAlert(Alert.fromJSON(buf));
    // We should have two here, one for the primary address indicator and one for the source
    // address.
    //
    // Since this heuristic will create an alert for each of the similar addresses too, we don't
    // expect those to be included in the returned alert list as well (those will be converted
    // as they come in, in the same way).
    assertEquals(2, c.size());
  }
}
