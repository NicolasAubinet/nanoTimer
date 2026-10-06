package com.cube.nanotimer.util.coach;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * What the coach service answered to an access code. Read from the status and body alone, like
 * {@link CoachReply}.
 */
public final class CoachCodeReply {

  public enum Kind {
    /** The code let this install in, until {@link #getUntil}. */
    GRANTED,
    /** The service declined, and said why in {@link #getRefusal}. */
    REFUSED,
    UNREACHABLE,
    UNREADABLE
  }

  private final Kind kind;
  private final CoachRefusal refusal;
  private final long until;

  private CoachCodeReply(Kind kind, CoachRefusal refusal, long until) {
    this.kind = kind;
    this.refusal = refusal;
    this.until = until;
  }

  static CoachCodeReply unreachable() {
    return new CoachCodeReply(Kind.UNREACHABLE, null, 0);
  }

  static CoachCodeReply unreadable() {
    return new CoachCodeReply(Kind.UNREADABLE, null, 0);
  }

  static CoachCodeReply parse(int httpStatus, String body) {
    try {
      JSONObject json = new JSONObject(body);
      JSONObject error = json.optJSONObject("error");
      if (error != null) {
        return new CoachCodeReply(Kind.REFUSED, CoachRefusal.of(error.optString("code")), 0);
      }
      if (httpStatus != 200 || !"granted".equals(json.optString("status"))) {
        return unreadable();
      }
      String expiresAt = json.optString("expires_at", null);
      return new CoachCodeReply(Kind.GRANTED, null,
          expiresAt == null ? CoachAccess.NO_END : millis(expiresAt));
    } catch (JSONException | ParseException e) {
      return unreadable();
    }
  }

  /** An instant as the service writes it, read to the second: 2026-11-05T10:00:00.5Z. */
  private static long millis(String instant) throws ParseException {
    if (instant.length() < 20 || !instant.endsWith("Z")) {
      throw new ParseException(instant, 0);
    }
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    format.setTimeZone(TimeZone.getTimeZone("UTC"));
    format.setLenient(false);
    return format.parse(instant.substring(0, 19)).getTime();
  }

  public Kind getKind() {
    return kind;
  }

  /** Why the service declined, only for {@link Kind#REFUSED}. */
  public CoachRefusal getRefusal() {
    return refusal;
  }

  /** When the access ends, or {@link CoachAccess#NO_END}. Only for {@link Kind#GRANTED}. */
  public long getUntil() {
    return until;
  }
}
