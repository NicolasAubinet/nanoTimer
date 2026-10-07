package com.cube.nanotimer.util.coach;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The Play purchase this install shows the coach, and whether the coach has said it has ended.
 * Also the access an entered code gave it, for the few installs let in that way.
 *
 * <p>Play on the device only ever gives a purchase to send. Ending one is the service's to say:
 * Play listing nothing may be a store that could not be asked, so the purchase last seen is still
 * sent and the service answers for it.
 *
 * <p>Kept in a file of its own, outside what a backup carries: a restore must not bring back
 * another day's purchase, and the token is the buyer's credential.
 */
public final class CoachAccess {

  /**
   * How long a refused purchase that Play still lists as bought is left before it is sent again.
   * Play on the device can lag a refund, and a subscription that was on hold comes back under the
   * same token.
   */
  static final long RECHECK_MS = 24L * 60 * 60 * 1000;

  private static final String FILE = "coach_access";
  private static final String TOKEN_KEY = "purchase_token";
  private static final String REFUSED_TOKEN_KEY = "refused_token";
  private static final String REFUSED_AT_KEY = "refused_at";
  private static final String CODE_UNTIL_KEY = "code_until";

  /** An access with no end. */
  public static final long NO_END = Long.MAX_VALUE;

  private static CoachAccess instance;

  private final SharedPreferences preferences;

  private CoachAccess(SharedPreferences preferences) {
    this.preferences = preferences;
  }

  public static synchronized CoachAccess of(Context context) {
    if (instance == null) {
      instance = new CoachAccess(
          context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE));
    }
    return instance;
  }

  /** The purchase to send with every request, or null when Play has never shown this install one. */
  public synchronized String getPurchaseToken() {
    return preferences.getString(TOKEN_KEY, null);
  }

  /** Whether the coach has said the purchase this install sends no longer buys anything. */
  public synchronized boolean hasEnded() {
    String token = getPurchaseToken();
    return token != null && token.equals(preferences.getString(REFUSED_TOKEN_KEY, null));
  }

  /**
   * Whether this install holds something the coach lets it in for, as far as the app can tell: a
   * purchase the coach has not ended, or an entered code that still runs.
   */
  public synchronized boolean letsIn(long now) {
    return letsIn(getPurchaseToken() != null, hasEnded(), getCodeUntil(), now);
  }

  static boolean letsIn(boolean purchase, boolean ended, long codeUntil, long now) {
    return (purchase && !ended) || codeUntil > now;
  }

  /**
   * When the access an entered code gave this install ends: 0 when it holds none,
   * {@link #NO_END} for one with no end. The coach decides at every request; this is only what the
   * app shows.
   */
  public synchronized long getCodeUntil() {
    return preferences.getLong(CODE_UNTIL_KEY, 0);
  }

  /** The coach let this install in for an access code. */
  synchronized void codeAccepted(long until) {
    preferences.edit().putLong(CODE_UNTIL_KEY, until).commit();
  }

  /** The coach answered that nothing lets this install in, so whatever a code gave is over. */
  synchronized void codeEnded() {
    preferences.edit().remove(CODE_UNTIL_KEY).commit();
  }

  /**
   * Play lists the purchase as bought.
   *
   * @param justBought whether the solver has just been through the purchase, rather than Play
   *     listing what it remembers
   * @return whether this gives back an access the coach had ended
   */
  synchronized boolean bought(String token, boolean justBought, long now) {
    String refused = preferences.getString(REFUSED_TOKEN_KEY, null);
    boolean ended = hasEnded();
    SharedPreferences.Editor editor = preferences.edit().putString(TOKEN_KEY, token);
    boolean lifted = refused != null
        && lifts(token, refused, preferences.getLong(REFUSED_AT_KEY, 0), now, justBought);
    if (lifted) {
      editor.remove(REFUSED_TOKEN_KEY).remove(REFUSED_AT_KEY);
    }
    editor.commit();
    return ended && lifted;
  }

  /** The coach answered a request carrying this purchase with "no access". */
  synchronized void refused(String sentToken, long now) {
    // A purchase made while the request was out is not the one that was refused.
    if (sentToken != null && sentToken.equals(getPurchaseToken())) {
      preferences.edit().putString(REFUSED_TOKEN_KEY, sentToken).putLong(REFUSED_AT_KEY, now)
          .commit();
    }
  }

  /**
   * Whether a purchase Play lists as bought ends the coach's refusal. Another purchase always does.
   * The refused one does when the solver has just bought it again, or once a day, which is how a
   * subscription back from a hold is found without asking at every start of the app.
   */
  static boolean lifts(String bought, String refused, long refusedAt, long now,
      boolean justBought) {
    if (!bought.equals(refused)) {
      return true;
    }
    return justBought || refusedAt > now || now - refusedAt >= RECHECK_MS;
  }

  /**
   * Whether text can travel as a header at all. What a purchase token looks like is the service's
   * to judge, so this only keeps out what the connection itself would throw on.
   */
  static boolean sendable(String token) {
    return token != null && token.matches("[\\x21-\\x7E]+");
  }
}
