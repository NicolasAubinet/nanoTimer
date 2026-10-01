package com.cube.nanotimer.util.coach;

import com.cube.nanotimer.Options;
import com.cube.nanotimer.cube.SolveTypeMethod;
import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ScrambleType;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

/**
 * The one solve type the weekly plans are written for. A background job has no current solve type
 * to read, so the solver's choice is kept.
 */
public final class CoachPlans {

  private CoachPlans() {
  }

  /** Whether plans can be written for the solve type: whole 3x3 solves, sighted, read as CFOP. */
  public static boolean eligible(SolveType solveType) {
    return eligible(solveType, SolveTypeMethod.of(solveType));
  }

  /** @param method the method the solve type's solves are read as */
  static boolean eligible(SolveType solveType, CubeMethod method) {
    ScrambleType scrambleType = solveType.getScrambleType();
    return solveType.getCubeTypeId() == CubeType.THREE_BY_THREE.getId()
        && !solveType.isBlind()
        && method == CubeMethod.CFOP
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
