package com.cube.nanotimer.util.coach;

import com.cube.nanotimer.coach.CoachPayload;
import com.cube.nanotimer.coach.CoachPlan;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/** Every shape the coach service answers with, read without a network. */
public class CoachReplyTest {

  private static final String PLAN = "{\"plan_version\":1,\"source\":\"coach\","
      + "\"headline\":\"Recognition is where the time goes\",\"focus\":[]}";

  @Test
  public void aReadyEnvelopeIsAPlan() {
    CoachReply reply = CoachReply.parse(200, "{\"status\":\"ready\",\"plan_id\":\"p1\","
        + "\"cached\":false,\"source\":\"coach\",\"model\":\"m\",\"dropped_areas\":0,\"plan\":" + PLAN + "}");

    Assert.assertEquals(CoachReply.Kind.PLAN, reply.getKind());
    Assert.assertEquals(CoachPlan.Source.COACH, reply.getPlan().getSource());
    Assert.assertEquals("Recognition is where the time goes", reply.getPlan().getHeadline());
  }

  @Test
  public void aRefusalNamesItsCodeAndWait() {
    CoachReply reply = CoachReply.parse(429, "{\"error\":{\"code\":\"weekly_cap_reached\","
        + "\"message\":\"no\",\"retry_after_s\":3600}}");

    Assert.assertEquals(CoachReply.Kind.REFUSED, reply.getKind());
    Assert.assertEquals(CoachRefusal.WEEKLY_CAP_REACHED, reply.getRefusal());
    Assert.assertEquals(3600, reply.getRetryAfterSeconds());
  }

  /** The service ships on its own cycle, so a code added after this app was built still reads. */
  @Test
  public void aRefusalCodeThisAppNeverHeardOfIsUnknown() {
    CoachReply reply = CoachReply.parse(503, "{\"error\":{\"code\":\"on_fire\",\"message\":\"x\"}}");

    Assert.assertEquals(CoachReply.Kind.REFUSED, reply.getKind());
    Assert.assertEquals(CoachRefusal.UNKNOWN, reply.getRefusal());
    Assert.assertEquals(0, reply.getRetryAfterSeconds());
  }

  @Test
  public void aPlanNewerThanThisAppIsUnreadable() {
    String newer = PLAN.replace("\"plan_version\":1", "\"plan_version\":" + (CoachPlan.VERSION + 1));

    Assert.assertEquals(CoachReply.Kind.UNREADABLE,
        CoachReply.parse(200, "{\"status\":\"ready\",\"plan\":" + newer + "}").getKind());
  }

  /** Reserved for a later service that answers by polling, which this app cannot follow. */
  @Test
  public void aPendingEnvelopeIsUnreadable() {
    Assert.assertEquals(CoachReply.Kind.UNREADABLE,
        CoachReply.parse(200, "{\"status\":\"pending\",\"plan_id\":\"p1\"}").getKind());
  }

  /** A proxy's HTML error page is what arrives when something in front of the service fails. */
  @Test
  public void aBodyThatIsNotJsonIsUnreadable() {
    Assert.assertEquals(CoachReply.Kind.UNREADABLE,
        CoachReply.parse(502, "<html>Bad Gateway</html>").getKind());
  }

  @Test
  public void anEndedSubscriptionIsToldApartFromPlayNotAnswering() {
    CoachReply ended = CoachReply.parse(403,
        "{\"error\":{\"code\":\"no_access\",\"message\":\"no\"}}");
    Assert.assertEquals(CoachReply.Kind.REFUSED, ended.getKind());
    Assert.assertEquals(CoachRefusal.NO_ACCESS, ended.getRefusal());

    CoachReply unasked = CoachReply.parse(503, "{\"error\":{\"code\":\"purchase_unavailable\","
        + "\"message\":\"no\",\"retry_after_s\":60}}");
    Assert.assertEquals(CoachRefusal.PURCHASE_UNAVAILABLE, unasked.getRefusal());
    Assert.assertEquals(60, unasked.getRetryAfterSeconds());
  }

  @Test
  public void theStreamRidesBesideThePayloadAndNotInIt() throws Exception {
    CoachPayload payload = CoachPayload.parse(
        "{\"schema_version\":" + CoachPayload.VERSION + ",\"puzzle\":\"3x3\",\"method\":\"cfop\"}");

    String text = CoachClient.requestBody(payload, "stream-1");
    // A plan that answers the request is kept beside the payload it carried.
    Assert.assertEquals(payload.toJson(), CoachClient.payloadOf(text).toJson());
    JSONObject body = new JSONObject(text);

    Assert.assertEquals("stream-1", body.getString("stream"));
    Assert.assertFalse(body.getJSONObject("payload").has("stream"));
    Assert.assertEquals("cfop", body.getJSONObject("payload").getString("method"));
  }
}
