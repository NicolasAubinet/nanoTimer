package com.cube.nanotimer.util.coach;

import com.cube.nanotimer.App;
import com.cube.nanotimer.DynamicTranslations;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.cube.SolveTypeMethod;
import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ScrambleType;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.List;

/**
 * The one solve type the weekly plans are written for. A background job has no current solve type
 * to read, so the solver's choice is kept.
 */
public final class CoachPlans {

  private CoachPlans() {
  }

  /**
   * Whether plans can be written for the solve type: whole 3x3 solves, sighted, two-handed, read
   * as CFOP. A type with steps of its own is read through those, so it never holds a CFOP solve.
   */
  public static boolean eligible(SolveType solveType) {
    return eligible(solveType, SolveTypeMethod.of(solveType));
  }

  /** Whether plans could be written for the solve type if the preferred method were this one. */
  public static boolean eligibleUnder(SolveType solveType, CubeMethod preferred) {
    return eligible(solveType, SolveTypeMethod.of(solveType, preferred));
  }

  /** Whether the weekly plans are written for this solve type. */
  public static boolean carriedBy(SolveType solveType) {
    return Options.INSTANCE.isCoachPlansOn()
        && Options.INSTANCE.getCoachPlansSolveType() == solveType.getId();
  }

  /** @param method the method the solve type's solves are read as */
  static boolean eligible(SolveType solveType, CubeMethod method) {
    DynamicTranslations names = App.INSTANCE.getDynamicTranslations();
    return eligible(solveType, method, names != null && names.isOneHanded(solveType.getName()));
  }

  /** @param oneHanded the coach's bars are two-handed ones, so a one-handed type gets no plan */
  static boolean eligible(SolveType solveType, CubeMethod method, boolean oneHanded) {
    ScrambleType scrambleType = solveType.getScrambleType();
    return solveType.getCubeTypeId() == CubeType.THREE_BY_THREE.getId()
        && !solveType.isBlind()
        && !oneHanded
        && method == CubeMethod.CFOP
        && !solveType.hasSteps()
        && (scrambleType == null || scrambleType.isDefault());
  }

  /**
   * When the last plan was written, 0 when none was. A time ahead of the clock means the clock was
   * set back, so it is moved to now and kept: the week then runs from here, not from a date that
   * may be months away.
   */
  public static long lastPlanAt(long now) {
    long last = Options.INSTANCE.getCoachLastPlanAt();
    if (last > now) {
      Options.INSTANCE.setCoachLastPlanAt(now);
      return now;
    }
    return last;
  }

  /** Until when a refusal keeps the weekly job from asking the coach, 0 when it may ask. */
  public static long heldUntil(long now) {
    long askAgainAt = Options.INSTANCE.getCoachAskAgainAt();
    return CoachPlanJob.held(askAgainAt, now) ? askAgainAt : 0;
  }

  /** Writes the plans for another solve type from now on. The week since the last plan is kept. */
  public static void moveTo(SolveType solveType) {
    Options.INSTANCE.setCoachPlansSolveType(solveType.getId());
  }

  /** The solve types plans can be written for, in the order given. */
  public static List<SolveType> eligible(List<SolveType> solveTypes) {
    List<SolveType> eligible = new ArrayList<SolveType>();
    for (SolveType solveType : solveTypes) {
      if (eligible(solveType)) {
        eligible.add(solveType);
      }
    }
    return eligible;
  }

  /**
   * The solve type the plans are for, or null when the solver has not agreed to them, has picked
   * none, or the one picked is gone or no longer eligible.
   *
   * @param solveTypes the 3x3 solve types as they are now
   */
  public static SolveType flagged(List<SolveType> solveTypes) {
    if (!Options.INSTANCE.isCoachPlansOn()) {
      return null;
    }
    int id = Options.INSTANCE.getCoachPlansSolveType();
    for (SolveType solveType : solveTypes) {
      if (solveType.getId() == id) {
        return eligible(solveType) ? solveType : null;
      }
    }
    return null;
  }
}
