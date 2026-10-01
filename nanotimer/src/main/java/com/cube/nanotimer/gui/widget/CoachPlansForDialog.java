package com.cube.nanotimer.gui.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.coach.CoachPayloadBuilder;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.YesNoListener;
import com.cube.nanotimer.util.coach.CoachPlanWorker;
import com.cube.nanotimer.util.coach.CoachPlans;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.util.view.SolveTypeIcons;
import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.List;

/**
 * Which solve type the weekly plans are written for, picked from the ones that can carry them.
 * Moving them is rare and asks first: the week since the last plan is kept, but the new solve type
 * needs enough smart cube solves of its own before a plan is written for it.
 */
public final class CoachPlansForDialog {

  private final Activity activity;
  private final Runnable onMoved;
  private List<SolveType> eligible;
  private SolveType flagged;
  private final List<Integer> counts = new ArrayList<Integer>();

  private CoachPlansForDialog(Activity activity, Runnable onMoved) {
    this.activity = activity;
    this.onMoved = onMoved;
  }

  /** @param onMoved run on the main thread once the plans have been moved */
  public static void show(Activity activity, Runnable onMoved) {
    new CoachPlansForDialog(activity, onMoved).load();
  }

  private void load() {
    App.INSTANCE.getService().getSolveTypes(CubeType.THREE_BY_THREE,
        new DataCallback<List<SolveType>>() {
          @Override
          public void onData(List<SolveType> solveTypes) {
            eligible = CoachPlans.eligible(solveTypes);
            flagged = CoachPlans.flagged(solveTypes);
            count();
          }
        });
  }

  /** Counts one solve type after the other, the service answering each on a thread of its own. */
  private void count() {
    if (counts.size() == eligible.size()) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (!activity.isFinishing()) {
            showPicker();
          }
        }
      });
      return;
    }
    App.INSTANCE.getService().getCoachSolveCount(eligible.get(counts.size()), CubeMethod.CFOP,
        new DataCallback<Integer>() {
          @Override
          public void onData(Integer count) {
            counts.add(count);
            count();
          }
        });
  }

  private void showPicker() {
    if (eligible.isEmpty()) {
      // Nothing to pick from: say why, as the paused Plan tab does.
      DialogUtils.showOkDialog(activity, activity.getString(R.string.coach_plans_for),
          activity.getString(R.string.coach_plans_paused_body) + " "
              + activity.getString(R.string.coach_plans_paused_none));
      return;
    }
    LayoutInflater inflater = LayoutInflater.from(activity);
    View content = inflater.inflate(R.layout.coach_plans_for_dialog, null);
    LinearLayout rows = content.findViewById(R.id.llCoachPlansForTypes);
    final AlertDialog dialog = new AlertDialog.Builder(activity, R.style.NanoTimerDialogTheme)
        .setView(content)
        .setNegativeButton(R.string.close, null)
        .create();
    for (int i = 0; i < eligible.size(); i++) {
      final SolveType choice = eligible.get(i);
      final int missing = CoachPayloadBuilder.PLAN_FLOOR - counts.get(i).intValue();
      View row = inflater.inflate(R.layout.coach_plans_for_item, rows, false);
      ImageView icon = row.findViewById(R.id.imgCoachPlansForType);
      icon.setImageResource(SolveTypeIcons.forSolveType(choice));
      icon.setColorFilter(ContextCompat.getColor(activity, R.color.lightblue));
      ((TextView) row.findViewById(R.id.tvCoachPlansForName)).setText(name(choice));
      ((TextView) row.findViewById(R.id.tvCoachPlansForCount))
          .setText(String.valueOf(counts.get(i)));
      TextView note = row.findViewById(R.id.tvCoachPlansForNote);
      note.setVisibility(missing > 0 ? View.VISIBLE : View.GONE);
      if (missing > 0) {
        note.setText(activity.getResources().getQuantityString(R.plurals.coach_plans_for_first,
            missing, missing));
      }
      final boolean current = flagged != null && choice.getId() == flagged.getId();
      row.setSelected(current);
      row.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          dialog.dismiss();
          if (!current) {
            confirm(choice, missing);
          }
        }
      });
      rows.addView(row);
    }
    dialog.show();
  }

  private void confirm(final SolveType choice, int missing) {
    if (flagged == null) {
      move(choice); // the plans were written for nothing, so nothing is given up
      return;
    }
    String message = activity.getString(R.string.coach_plans_move_body, name(choice), name(flagged));
    if (missing > 0) {
      message += " " + activity.getResources().getQuantityString(R.plurals.coach_plans_move_floor,
          missing, name(choice), missing);
    }
    DialogUtils.showConfirmCancelDialog(activity, R.string.coach_plans_move_title, message,
        R.string.coach_plans_move_confirm, R.string.cancel, new YesNoListener() {
          @Override
          public void onYes() {
            move(choice);
          }
        });
  }

  private void move(SolveType choice) {
    CoachPlans.moveTo(choice);
    CoachPlanWorker.check(activity);
    onMoved.run();
  }

  private String name(SolveType solveType) {
    return Utils.toSolveTypeLocalizedName(activity, solveType.getName());
  }
}
