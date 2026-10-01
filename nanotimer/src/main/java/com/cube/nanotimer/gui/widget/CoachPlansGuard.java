package com.cube.nanotimer.gui.widget;

import android.app.Activity;

import com.cube.nanotimer.R;
import com.cube.nanotimer.util.coach.CoachPlans;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

/**
 * What a confirm says before the solve type that carries the weekly plans is deleted, or changed
 * so that no plan can be written for it. Never silently: the plans either move or pause, and the
 * solver is told which before agreeing.
 */
public final class CoachPlansGuard {

  private CoachPlansGuard() {
  }

  /** Whether a solve type other than this one can carry the plans. */
  public static boolean anotherCanCarry(List<SolveType> solveTypes, SolveType except) {
    for (SolveType solveType : solveTypes) {
      if (solveType.getId() != except.getId() && CoachPlans.eligible(solveType)) {
        return true;
      }
    }
    return false;
  }

  /** The paragraph a delete confirm adds. */
  public static int deleteWarning(boolean another) {
    return another ? R.string.coach_guard_delete_move : R.string.coach_guard_delete_pause;
  }

  public static int deleteConfirm(boolean another) {
    return another ? R.string.coach_guard_delete_move_confirm
        : R.string.coach_guard_delete_pause_confirm;
  }

  /** What the confirm of a change says, the solve type being kept but no longer eligible. */
  public static int changeWarning(boolean another) {
    return another ? R.string.coach_guard_change_move : R.string.coach_guard_change_pause;
  }

  public static int changeConfirm(boolean another) {
    return another ? R.string.coach_guard_change_move_confirm
        : R.string.coach_guard_change_pause_confirm;
  }

  /** Once the change is made: offers the solve types that can take the plans, if any can. */
  public static void movePlans(Activity activity, boolean another) {
    movePlans(activity, another, new Runnable() {
      @Override
      public void run() {
      }
    });
  }

  /** @param onMoved run on the main thread once the plans have a solve type again */
  public static void movePlans(final Activity activity, boolean another, final Runnable onMoved) {
    if (!another) {
      return;
    }
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!activity.isFinishing()) {
          CoachPlansForDialog.show(activity, onMoved);
        }
      }
    });
  }
}
