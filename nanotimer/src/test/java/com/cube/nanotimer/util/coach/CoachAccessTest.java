package com.cube.nanotimer.util.coach;

import org.junit.Assert;
import org.junit.Test;

/** When a purchase Play lists as bought ends the coach's "no access". */
public class CoachAccessTest {

  private static final long NOW = 1_000_000_000_000L;
  private static final String TOKEN = "abcdefghijklmnop.AO-J1Oxyz_0123456789";

  @Test
  public void anotherPurchaseEndsTheRefusal() {
    Assert.assertTrue(CoachAccess.lifts("another-purchase-token", TOKEN, NOW, NOW, false));
  }

  /** Play on the device can lag a refund, so the same purchase is not sent straight back. */
  @Test
  public void theRefusedPurchaseStaysRefusedForADay() {
    Assert.assertFalse(CoachAccess.lifts(TOKEN, TOKEN, NOW, NOW, false));
    Assert.assertFalse(CoachAccess.lifts(TOKEN, TOKEN, NOW, NOW + CoachAccess.RECHECK_MS - 1,
        false));
  }

  /** A subscription back from a hold keeps its token, and Play lists it as bought again. */
  @Test
  public void theRefusedPurchaseIsSentAgainAfterADay() {
    Assert.assertTrue(CoachAccess.lifts(TOKEN, TOKEN, NOW, NOW + CoachAccess.RECHECK_MS, false));
  }

  @Test
  public void buyingItAgainEndsTheRefusalAtOnce() {
    Assert.assertTrue(CoachAccess.lifts(TOKEN, TOKEN, NOW, NOW, true));
  }

  /** A refusal dated ahead of the clock would otherwise hold for as long as the clock was off. */
  @Test
  public void aClockSetBackDoesNotKeepTheRefusal() {
    Assert.assertTrue(CoachAccess.lifts(TOKEN, TOKEN, NOW + 1, NOW, false));
  }

  @Test
  public void onlyTextAHeaderCanCarryIsSent() {
    Assert.assertTrue(CoachAccess.sendable(TOKEN));
    Assert.assertFalse(CoachAccess.sendable(null));
    Assert.assertFalse(CoachAccess.sendable(""));
    Assert.assertFalse(CoachAccess.sendable("abc\r\nX-Other: 1"));
    Assert.assertFalse(CoachAccess.sendable("abc def"));
  }
}
