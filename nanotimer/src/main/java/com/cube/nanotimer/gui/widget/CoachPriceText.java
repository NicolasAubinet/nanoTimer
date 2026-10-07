package com.cube.nanotimer.gui.widget;

import android.content.res.Resources;

import com.cube.nanotimer.R;
import com.cube.nanotimer.util.coach.CoachOffers;

import java.util.List;

/**
 * What Play asks for a base plan, in the words the Plan tab and the purchase sheet show. Every
 * price is Play's own text for the buyer's country, and every length is read off the offer, so an
 * offer changed in Play Console changes what is said here with no release.
 */
final class CoachPriceText {

  private final Resources resources;

  CoachPriceText(Resources resources) {
    this.resources = resources;
  }

  /** What is paid first: "2 weeks free, then €43.99 for the first year", or the price alone. */
  String first(CoachOffers.Offer offer) {
    String free = length(offer.getFreePeriod());
    String paid = paidFirst(offer);
    return free == null ? paid : resources.getString(R.string.coach_price_free_then, free, paid);
  }

  /** "€54.99 a year after that", or null for an offer with no lower price before it. */
  String after(CoachOffers.Offer offer) {
    return offer.getIntro() == null ? null
        : resources.getString(R.string.coach_price_after, regular(offer));
  }

  /** "20% off", or null for an offer that takes nothing off. */
  String tag(CoachOffers.Offer offer) {
    int off = offer.getPercentOff();
    return off == 0 ? null : resources.getString(R.string.coach_price_off, off);
  }

  /** The button that buys the plan: its free weeks when it has some, else its price. */
  String button(CoachOffers.Offer offer) {
    String free = length(offer.getFreePeriod());
    return free == null
        ? resources.getString(R.string.coach_purchase_subscribe_for, paidFirst(offer))
        : resources.getString(R.string.coach_purchase_start_free, free);
  }

  /** The button on the Plan tab that opens the sheet. */
  String offerButton(List<CoachOffers.Offer> plans) {
    for (CoachOffers.Offer plan : plans) {
      String free = length(plan.getFreePeriod());
      if (free != null) {
        return resources.getString(R.string.coach_offer_try_free, free);
      }
    }
    return resources.getString(R.string.coach_offer_try);
  }

  /**
   * The line under that button: what both base plans cost once any offer is over, the shorter one
   * first. Null unless Play listed both.
   *
   * @param plans the yearly one first
   */
  String offerNote(List<CoachOffers.Offer> plans) {
    if (plans.size() != 2) {
      return null;
    }
    String yearly = regular(plans.get(0));
    String monthly = regular(plans.get(1));
    boolean free = false;
    int off = 0;
    for (CoachOffers.Offer plan : plans) {
      free |= length(plan.getFreePeriod()) != null;
      off = Math.max(off, plan.getPercentOff());
    }
    if (!free) {
      return resources.getString(R.string.coach_offer_prices, monthly, yearly);
    }
    return off == 0 ? resources.getString(R.string.coach_offer_then, monthly, yearly)
        : resources.getString(R.string.coach_offer_then_less, monthly, yearly, off);
  }

  /** "€54.99 a year": what the base plan costs once any offer is over. */
  private String regular(CoachOffers.Offer offer) {
    CoachOffers.Phase phase = offer.getRegular();
    CoachOffers.Period period = phase.getPeriod();
    if (period == null) {
      return phase.getPrice();
    }
    if (period.getCount() == 1 && period.getUnit() == 'Y') {
      return resources.getString(R.string.coach_price_yearly, phase.getPrice());
    }
    if (period.getCount() == 1 && period.getUnit() == 'M') {
      return resources.getString(R.string.coach_price_monthly, phase.getPrice());
    }
    return resources.getString(R.string.coach_price_every, phase.getPrice(), length(period));
  }

  /** The first price that is not free: the offer's lower one, else the base plan's own. */
  private String paidFirst(CoachOffers.Offer offer) {
    CoachOffers.Phase intro = offer.getIntro();
    if (intro == null) {
      return regular(offer);
    }
    CoachOffers.Period period = intro.getPeriod();
    if (period == null) {
      return intro.getPrice();
    }
    if (period.getCount() == 1 && period.getUnit() == 'Y') {
      return resources.getString(R.string.coach_price_first_year, intro.getPrice());
    }
    if (period.getCount() == 1 && period.getUnit() == 'M') {
      return resources.getString(R.string.coach_price_first_month, intro.getPrice());
    }
    return resources.getString(R.string.coach_price_first, intro.getPrice(), length(period));
  }

  /** "2 weeks", or null for no period. */
  private String length(CoachOffers.Period period) {
    if (period == null) {
      return null;
    }
    int plural;
    switch (period.getUnit()) {
      case 'D':
        plural = R.plurals.coach_period_days;
        break;
      case 'W':
        plural = R.plurals.coach_period_weeks;
        break;
      case 'M':
        plural = R.plurals.coach_period_months;
        break;
      default:
        plural = R.plurals.coach_period_years;
        break;
    }
    return resources.getQuantityString(plural, period.getCount(), period.getCount());
  }
}
