package com.cube.nanotimer.coach;

/**
 * When the next plan may be asked for. A plan is due a week after the last one and then waits for
 * enough new solves, so an idle week neither loses a plan nor repeats one.
 *
 * <p>The times are the device's own and never leave it. Only solves of the plans' solve type are
 * counted: drill reps are not.
 */
public final class PlanSchedule {

  /** How long after a plan the next one is due. */
  public static final long PERIOD_MS = 7L * 24 * 60 * 60 * 1000;

  /** How long after a solve a plan is not asked for, so it reads the whole session. */
  public static final long QUIET_MS = 15L * 60 * 1000;

  /** How long after a plan the solver is told how many solves the next one still needs. */
  public static final long NUDGE_AFTER_MS = 4L * 24 * 60 * 60 * 1000;

  public enum State {
    /** Less than a week since the last plan. */
    NOT_DUE,
    /** Due, and short of solves by {@link #getSolvesNeeded}. */
    NEEDS_SOLVES,
    /** Due with enough solves, but the last solve is too recent. */
    QUIET,
    READY
  }

  private final long lastPlanAt;
  private final long lastSolveAt;
  private final long now;
  private final int solvesNeeded;

  private PlanSchedule(long lastPlanAt, long lastSolveAt, long now, int solvesNeeded) {
    this.lastPlanAt = lastPlanAt;
    this.lastSolveAt = lastSolveAt;
    this.now = now;
    this.solvesNeeded = solvesNeeded;
  }

  /**
   * @param lastPlanAt when the last plan was written for any solve type, 0 when there is none
   * @param solves how many solves the plans' solve type has under its method
   * @param solvesSince how many of those were done after the last plan
   * @param lastSolveAt when the newest of them was done, 0 when there is none
   */
  public static PlanSchedule of(long lastPlanAt, int solves, int solvesSince, long lastSolveAt,
      long now) {
    // A time ahead of the clock means the clock was set back. The week is counted from now, which
    // only holds if the caller also stores now as the last plan's time.
    long plan = Math.min(lastPlanAt, now);
    int needed = CoachPayloadBuilder.PLAN_FLOOR - solves;
    if (plan != 0) {
      needed = Math.max(needed, CoachPayloadBuilder.SINCE_FLOOR - solvesSince);
    }
    // A solve dated ahead of the clock is no session: it would hold the plan back until then.
    return new PlanSchedule(plan, lastSolveAt > now ? 0 : lastSolveAt, now, Math.max(needed, 0));
  }

  public State getState() {
    if (!isFirst() && now < getDueAt()) {
      return State.NOT_DUE;
    }
    if (solvesNeeded > 0) {
      return State.NEEDS_SOLVES;
    }
    return inSession() ? State.QUIET : State.READY;
  }

  /** True when no plan was ever written, so neither the week nor the new solves are waited for. */
  public boolean isFirst() {
    return lastPlanAt == 0;
  }

  /** How many more solves the next plan needs, 0 when it has them. Counted before it is due too. */
  public int getSolvesNeeded() {
    return solvesNeeded;
  }

  /** When the next plan is due, 0 for a first plan. */
  public long getDueAt() {
    return isFirst() ? 0 : lastPlanAt + PERIOD_MS;
  }

  /** When the last solve stops holding a plan back, 0 when there is no solve. */
  public long getQuietUntil() {
    return lastSolveAt == 0 ? 0 : lastSolveAt + QUIET_MS;
  }

  /**
   * Whether to ask now. A first plan asked for from the screen does not wait for the session to
   * end: the solver is looking at it.
   */
  public boolean mayAsk(boolean onScreen) {
    State state = getState();
    return state == State.READY || (state == State.QUIET && isFirst() && onScreen);
  }

  /**
   * Whether to tell the solver how many solves are missing: once per plan, some days after it,
   * and never during a session.
   *
   * @param lastNudgeAt when the solver was last told, 0 when never
   */
  public boolean nudgeDue(long lastNudgeAt) {
    return !isFirst() && solvesNeeded > 0 && now >= lastPlanAt + NUDGE_AFTER_MS
        && lastNudgeAt < lastPlanAt && !inSession();
  }

  private boolean inSession() {
    return lastSolveAt != 0 && now < getQuietUntil();
  }
}
