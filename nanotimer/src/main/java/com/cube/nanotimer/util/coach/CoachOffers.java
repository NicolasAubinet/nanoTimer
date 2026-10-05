package com.cube.nanotimer.util.coach;

import java.util.List;

/**
 * Which of Play's offers a purchase of a base plan is made with. Offers are known by the tag set
 * on them in Play Console, never by their id, so the launch offer can be ended there with no
 * release: Play stops listing it and the next tag is taken.
 */
final class CoachOffers {

  /** In the order they are wanted. Play lists an offer only to a buyer who may have it. */
  static final String[] TAGS = {"launch", "trial"};

  /** One way of buying a base plan, as Play lists it. */
  static final class Offer {
    private final String basePlan;
    private final String offerId;
    private final List<String> tags;
    private final String offerToken;

    /** @param offerId null for the base plan bought plainly, with no offer */
    Offer(String basePlan, String offerId, List<String> tags, String offerToken) {
      this.basePlan = basePlan;
      this.offerId = offerId;
      this.tags = tags;
      this.offerToken = offerToken;
    }

    String getOfferId() {
      return offerId;
    }

    /** What the purchase is launched with. */
    String getOfferToken() {
      return offerToken;
    }
  }

  private CoachOffers() {
  }

  /**
   * The offer to buy the base plan with: the first wanted tag Play lists for it, else the plain
   * base plan, else null when Play lists no way of buying it.
   */
  static Offer pick(List<Offer> offers, String basePlan) {
    for (String tag : TAGS) {
      for (Offer offer : offers) {
        // A base plan's own tags are listed on its plain entry too, which is no offer.
        if (offer.basePlan.equals(basePlan) && offer.offerId != null
            && offer.tags.contains(tag)) {
          return offer;
        }
      }
    }
    for (Offer offer : offers) {
      if (offer.basePlan.equals(basePlan) && offer.offerId == null) {
        return offer;
      }
    }
    return null;
  }
}
