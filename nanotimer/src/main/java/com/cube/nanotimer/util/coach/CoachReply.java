package com.cube.nanotimer.util.coach;

import com.cube.nanotimer.coach.CoachPlan;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * What the coach service answered to one plan request: a plan, a refusal naming why, or nothing
 * the app could use.
 *
 * <p>Read from the status and body alone, so every shape the service can send is testable without
 * a network.
 */
public final class CoachReply {

  public enum Kind {
    /** A plan this app can read. */
    PLAN,
    /** The service declined, and said why in {@link #getRefusal}. */
    REFUSED,
    /** No answer arrived: no connection, a timeout, a dropped socket. */
    UNREACHABLE,
    /** An answer arrived that this app cannot read, most likely one written for a newer app. */
    UNREADABLE
  }

  private final Kind kind;
  private final CoachPlan plan;
  private final CoachRefusal refusal;
  private final int retryAfterSeconds;

  private CoachReply(Kind kind, CoachPlan plan, CoachRefusal refusal, int retryAfterSeconds) {
    this.kind = kind;
    this.plan = plan;
    this.refusal = refusal;
    this.retryAfterSeconds = retryAfterSeconds;
  }

  static CoachReply unreachable() {
    return new CoachReply(Kind.UNREACHABLE, null, null, 0);
  }

  static CoachReply unreadable() {
    return new CoachReply(Kind.UNREADABLE, null, null, 0);
  }

  static CoachReply parse(int httpStatus, String body) {
    try {
      JSONObject json = new JSONObject(body);
      JSONObject error = json.optJSONObject("error");
      if (error != null) {
        return new CoachReply(Kind.REFUSED, null, CoachRefusal.of(error.optString("code")),
            error.optInt("retry_after_s", 0));
      }
      // "pending" is reserved for a service that answers later, which this app cannot wait for.
      if (httpStatus != 200 || !"ready".equals(json.optString("status"))) {
        return unreadable();
      }
      return new CoachReply(Kind.PLAN, CoachPlan.parse(json.getJSONObject("plan").toString()),
          null, 0);
    } catch (JSONException | IllegalArgumentException e) {
      return unreadable();
    }
  }

  public Kind getKind() {
    return kind;
  }

  /** The plan, only for {@link Kind#PLAN}. */
  public CoachPlan getPlan() {
    return plan;
  }

  /** Why the service declined, only for {@link Kind#REFUSED}. */
  public CoachRefusal getRefusal() {
    return refusal;
  }

  /** How long the service asked to be left alone, 0 when it did not say. */
  public int getRetryAfterSeconds() {
    return retryAfterSeconds;
  }
}
