package com.cube.nanotimer.util.coach;

import java.util.Locale;

/**
 * The service's refusal codes, which are its contract with the app; its English messages are not.
 *
 * <p>The app ships on Play's cycle and the service does not, so a code added there after this app
 * was built must still read, as {@link #UNKNOWN}.
 */
public enum CoachRefusal {
  UNAUTHORIZED,
  PAYLOAD_INVALID,
  PAYLOAD_TOO_LARGE,
  PAYLOAD_TOO_NEW,
  INSUFFICIENT_DATA,
  RATE_LIMITED,
  WEEKLY_CAP_REACHED,
  SPEND_CAP_REACHED,
  PLAN_REJECTED,
  UPSTREAM_UNAVAILABLE,
  STORE_UNAVAILABLE,
  UNKNOWN;

  static CoachRefusal of(String code) {
    for (CoachRefusal refusal : values()) {
      if (refusal != UNKNOWN && refusal.name().toLowerCase(Locale.ROOT).equals(code)) {
        return refusal;
      }
    }
    return UNKNOWN;
  }
}
