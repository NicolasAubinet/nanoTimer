package com.cube.nanotimer.util.coach;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Which of Play's offers a base plan is bought with. */
public class CoachOffersTest {

  private static CoachOffers.Offer offer(String basePlan, String offerId, String... tags) {
    return new CoachOffers.Offer(basePlan, offerId, Arrays.asList(tags),
        basePlan + "/" + offerId);
  }

  /** As Play lists them to somebody who never had the subscription. */
  private static List<CoachOffers.Offer> newBuyer() {
    List<CoachOffers.Offer> offers = new ArrayList<CoachOffers.Offer>();
    offers.add(offer("monthly", "trial", "trial"));
    offers.add(offer("monthly", "launch", "launch"));
    offers.add(offer("monthly", null));
    offers.add(offer("yearly", "trial", "trial"));
    offers.add(offer("yearly", "launch", "launch"));
    offers.add(offer("yearly", null));
    return offers;
  }

  @Test
  public void theLaunchOfferIsTakenWhilePlayListsIt() {
    Assert.assertEquals("yearly/launch",
        CoachOffers.pick(newBuyer(), "yearly").getOfferToken());
    Assert.assertEquals("monthly/launch",
        CoachOffers.pick(newBuyer(), "monthly").getOfferToken());
  }

  /** The launch offer switched off in Play Console, with no release. */
  @Test
  public void theTrialIsTakenOnceTheLaunchOfferIsGone() {
    List<CoachOffers.Offer> offers = new ArrayList<CoachOffers.Offer>();
    offers.add(offer("monthly", null));
    offers.add(offer("monthly", "trial", "trial"));
    Assert.assertEquals("monthly/trial", CoachOffers.pick(offers, "monthly").getOfferToken());
  }

  /** Somebody who had the subscription before is listed neither offer. */
  @Test
  public void thePlainBasePlanIsTakenWhenNoOfferIsListed() {
    List<CoachOffers.Offer> offers = Arrays.asList(offer("monthly", null), offer("yearly", null));
    CoachOffers.Offer picked = CoachOffers.pick(offers, "yearly");
    Assert.assertEquals("yearly/null", picked.getOfferToken());
    Assert.assertNull(picked.getOfferId());
  }

  /** An offer inherits its base plan's tags, and so does the plain entry. */
  @Test
  public void aTagOnTheBasePlanItselfIsNotAnOffer() {
    List<CoachOffers.Offer> offers = Arrays.asList(offer("monthly", null, "launch"),
        offer("monthly", "trial", "launch", "trial"));
    Assert.assertEquals("monthly/trial", CoachOffers.pick(offers, "monthly").getOfferToken());
  }

  @Test
  public void anOfferOnTheOtherBasePlanIsNotTaken() {
    List<CoachOffers.Offer> offers = Arrays.asList(offer("yearly", "launch", "launch"),
        offer("monthly", null));
    Assert.assertNull(CoachOffers.pick(offers, "monthly").getOfferId());
  }

  @Test
  public void aBasePlanPlayDoesNotListCannotBeBought() {
    Assert.assertNull(CoachOffers.pick(newBuyer(), "weekly"));
    Assert.assertNull(CoachOffers.pick(Collections.<CoachOffers.Offer>emptyList(), "monthly"));
  }
}
