package com.cube.nanotimer.util.coach;

import org.junit.Assert;
import org.junit.Test;

/** Every answer the service can give to an access code, read off the status and body. */
public class CoachCodeReplyTest {

  @Test
  public void aCodeTakenForATimeSaysUntilWhen() {
    CoachCodeReply reply = CoachCodeReply.parse(200,
        "{\"status\":\"granted\",\"expires_at\":\"2026-11-05T10:00:00Z\"}");

    Assert.assertEquals(CoachCodeReply.Kind.GRANTED, reply.getKind());
    Assert.assertEquals(1_793_872_800_000L, reply.getUntil());
  }

  /** The service writes a fraction of a second when there is one. */
  @Test
  public void theEndIsReadToTheSecond() {
    CoachCodeReply reply = CoachCodeReply.parse(200,
        "{\"status\":\"granted\",\"expires_at\":\"2026-11-05T10:00:00.123456Z\"}");

    Assert.assertEquals(1_793_872_800_000L, reply.getUntil());
  }

  @Test
  public void aCodeTakenForGoodHasNoEnd() {
    CoachCodeReply reply = CoachCodeReply.parse(200, "{\"status\":\"granted\"}");

    Assert.assertEquals(CoachCodeReply.Kind.GRANTED, reply.getKind());
    Assert.assertEquals(CoachAccess.NO_END, reply.getUntil());
  }

  @Test
  public void aRefusalNamesWhy() {
    CoachCodeReply refused = CoachCodeReply.parse(403,
        "{\"error\":{\"code\":\"code_refused\",\"message\":\"That code lets nobody in\"}}");
    CoachCodeReply limited = CoachCodeReply.parse(429,
        "{\"error\":{\"code\":\"rate_limited\",\"message\":\"Too many\",\"retry_after_s\":3600}}");

    Assert.assertEquals(CoachCodeReply.Kind.REFUSED, refused.getKind());
    Assert.assertEquals(CoachRefusal.CODE_REFUSED, refused.getRefusal());
    Assert.assertEquals(CoachRefusal.RATE_LIMITED, limited.getRefusal());
  }

  /** A service older than the codes answers 404 with no body this app knows. */
  @Test
  public void anAnswerThatIsNotOneGrantsNothing() {
    for (String body : new String[] {"Not found", "{}", "{\"status\":\"ready\"}",
        "{\"status\":\"granted\",\"expires_at\":\"tomorrow\"}"}) {
      Assert.assertEquals(body, CoachCodeReply.Kind.UNREADABLE,
          CoachCodeReply.parse(200, body).getKind());
    }
    Assert.assertEquals(CoachCodeReply.Kind.UNREADABLE,
        CoachCodeReply.parse(404, "{\"status\":\"granted\"}").getKind());
  }
}
