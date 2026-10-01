package com.cube.nanotimer.gui.widget;

import android.app.Activity;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.R;
import com.cube.nanotimer.coach.PlanSchedule;
import com.cube.nanotimer.cube.SolveTypeMethod;
import com.cube.nanotimer.gui.SolveTypesActivity;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.coach.CoachPlanWorker;
import com.cube.nanotimer.util.coach.CoachPlans;
import com.cube.nanotimer.util.coach.CoachRead;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveType;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * The Plan tab's status: when the next weekly plan comes and which solve type the plans are for.
 * On that solve type it is one row under the plan's headline. On another one it says where the
 * plans go, and with no solve type to carry them it says so. Moving them is done in the settings.
 */
public class CoachPlanStatus {

  private final Activity activity;
  private final ViewGroup tab;
  private final View status;
  private final SolveType solveType;
  /** Whether this build has a coach to ask: without one there are no weekly plans to speak of. */
  private final boolean coach;
  private boolean hasPlan;
  /** Once per screen: a check that ends without a plan must not start the next one. */
  private boolean checked;

  /**
   * @param tab the Plan tab's own layout, which the status moves about in
   * @param solveType the solve type the screen is showing
   */
  public CoachPlanStatus(Activity activity, ViewGroup tab, SolveType solveType, boolean coach) {
    this.activity = activity;
    this.tab = tab;
    this.status = tab.findViewById(R.id.llCoachPlanStatus);
    this.solveType = solveType;
    this.coach = coach;
  }

  /** Whether the tab is showing a plan, which the row sits under. Drawn at the next refresh. */
  public void setHasPlan(boolean hasPlan) {
    this.hasPlan = hasPlan;
  }

  /** Reads the schedule again and redraws. Main thread only. */
  public void refresh() {
    boolean cfop = solveType.getCubeTypeId() == CubeType.THREE_BY_THREE.getId()
        && SolveTypeMethod.of(solveType) == CubeMethod.CFOP;
    if (!coach || !cfop || !Options.INSTANCE.isCoachPlansOn()) {
      status.setVisibility(View.GONE);
      return;
    }
    App.INSTANCE.getService().getSolveTypes(CubeType.THREE_BY_THREE,
        new DataCallback<List<SolveType>>() {
          @Override
          public void onData(List<SolveType> solveTypes) {
            final boolean none = CoachPlans.eligible(solveTypes).isEmpty();
            final SolveType flagged = CoachPlans.flagged(solveTypes);
            if (flagged == null || flagged.getId() != solveType.getId()) {
              show(new Runnable() {
                @Override
                public void run() {
                  showElsewhere(flagged, none);
                }
              });
              return;
            }
            long now = System.currentTimeMillis();
            App.INSTANCE.getService().getPlanSchedule(flagged, CubeMethod.CFOP,
                CoachPlans.lastPlanAt(now), now, new DataCallback<PlanSchedule>() {
                  @Override
                  public void onData(final PlanSchedule schedule) {
                    show(new Runnable() {
                      @Override
                      public void run() {
                        showNext(schedule);
                      }
                    });
                  }
                });
          }
        });
  }

  private void show(final Runnable draw) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!activity.isFinishing()) {
          draw.run();
        }
      }
    });
  }

  /** The row on the plans' own solve type. */
  private void showNext(PlanSchedule schedule) {
    place(true);
    boolean writing = CoachRead.of(solveType).getState() == CoachRead.State.READING;
    long heldUntil = CoachPlans.heldUntil(System.currentTimeMillis());
    boolean waiting = writing;
    CharSequence line;
    if (writing) {
      line = activity.getString(R.string.coach_writing);
    } else {
      switch (schedule.getState()) {
        case NOT_DUE:
          String days = quantity(R.plurals.coach_days, schedule.getDaysUntilDue());
          line = schedule.getSolvesNeeded() == 0 ? lit(R.string.coach_next_days, days)
              : lit(R.string.coach_next_days_solves, days,
                  quantity(R.plurals.coach_more_solves, schedule.getSolvesNeeded()));
          break;
        case NEEDS_SOLVES:
          line = lit(schedule.isFirst() ? R.string.coach_first_solves : R.string.coach_next_solves,
              quantity(R.plurals.coach_more_solves, schedule.getSolvesNeeded()));
          break;
        case QUIET:
          line = activity.getString(R.string.coach_next_session);
          waiting = true;
          break;
        default:
          if (heldUntil != 0) {
            line = activity.getString(R.string.coach_next_retry, DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(heldUntil)));
          } else {
            line = activity.getString(R.string.coach_next_waiting);
            waiting = true;
          }
          break;
      }
    }
    TextView text = status.findViewById(R.id.tvCoachPlanStatus);
    text.setText(line);
    text.setTextColor(ContextCompat.getColor(activity, R.color.secondary_text));
    text.setTextSize(14);
    text.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    status.findViewById(R.id.pbCoachPlanStatus).setVisibility(waiting ? View.VISIBLE : View.GONE);
    status.findViewById(R.id.tvCoachPlanStatusBody).setVisibility(View.GONE);
    status.findViewById(R.id.buCoachPlanStatus).setVisibility(View.GONE);

    // A first plan does not wait for the session to end while the solver is looking at the tab.
    if (!checked && !writing && heldUntil == 0 && schedule.mayAsk(true)) {
      checked = true;
      CoachPlanWorker.checkFromScreen(activity);
    }
  }

  /**
   * On a solve type the plans are not written for: where they go, or that they go nowhere.
   *
   * @param none whether no solve type at all can carry the plans
   */
  private void showElsewhere(SolveType flagged, boolean none) {
    place(false);
    TextView text = status.findViewById(R.id.tvCoachPlanStatus);
    text.setTextColor(ContextCompat.getColor(activity, R.color.white));
    text.setTextSize(16);
    TextView body = status.findViewById(R.id.tvCoachPlanStatusBody);
    TextView button = status.findViewById(R.id.buCoachPlanStatus);
    status.findViewById(R.id.pbCoachPlanStatus).setVisibility(View.GONE);

    if (flagged != null) {
      text.setText(activity.getString(R.string.coach_plans_written_for,
          Utils.toSolveTypeLocalizedName(activity, flagged.getName())));
      text.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
      body.setVisibility(View.GONE);
      button.setVisibility(View.GONE);
      return;
    }
    boolean here = CoachPlans.eligible(solveType);
    text.setText(R.string.coach_plans_paused);
    text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_plans_paused, 0, 0, 0);
    text.setCompoundDrawablePadding(
        Math.round(6 * activity.getResources().getDisplayMetrics().density));
    String why = activity.getString(R.string.coach_plans_paused_body);
    body.setText(none ? why + " " + activity.getString(R.string.coach_plans_paused_none) : why);
    body.setVisibility(View.VISIBLE);
    button.setText(here
        ? activity.getString(R.string.coach_plans_resume_here,
            Utils.toSolveTypeLocalizedName(activity, solveType.getName()))
        : activity.getString(R.string.solve_types));
    button.setVisibility(View.VISIBLE);
    button.setBackgroundResource(here ? R.drawable.dialog_action_button
        : R.drawable.dialog_action_button_ghost);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (CoachPlans.eligible(solveType)) {
          moveTo(solveType);
        } else {
          activity.startActivity(new Intent(activity, SolveTypesActivity.class)
              .putExtra("cubeType", CubeType.THREE_BY_THREE));
        }
      }
    });
  }

  /**
   * Puts the status where it reads: under the plan's headline for the row, above everything else
   * for a solve type the plans are not written for.
   */
  private void place(boolean row) {
    View head = tab.findViewById(R.id.llAnalysisPlanWritten);
    tab.removeView(status);
    tab.addView(status, row ? tab.indexOfChild(head) + 1 : 0);
    status.setVisibility(View.VISIBLE);
    int gap = activity.getResources().getDimensionPixelSize(R.dimen.space_m);
    // The rule sets the row off from a headline, so it is drawn only under one.
    status.findViewById(R.id.vCoachPlanStatusRule)
        .setVisibility(row && hasPlan ? View.VISIBLE : View.GONE);
    ((LinearLayout.LayoutParams) status.getLayoutParams()).bottomMargin = row && hasPlan ? 0 : gap;
  }

  /** Only from a pause, where the plans are written for nothing and none can be lost. */
  private void moveTo(SolveType to) {
    CoachPlans.moveTo(to);
    checked = false;
    refresh();
  }

  private String quantity(int plural, int count) {
    return activity.getResources().getQuantityString(plural, count, count);
  }

  /** A line with the figures in it lit, which are what the eye is after. */
  private CharSequence lit(int line, String... figures) {
    SpannableString text = new SpannableString(activity.getString(line, (Object[]) figures));
    int white = ContextCompat.getColor(activity, R.color.white);
    for (String figure : figures) {
      int at = text.toString().indexOf(figure);
      if (at >= 0) {
        text.setSpan(new ForegroundColorSpan(white), at, at + figure.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }
    return text;
  }
}
