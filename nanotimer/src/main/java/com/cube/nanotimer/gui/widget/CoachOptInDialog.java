package com.cube.nanotimer.gui.widget;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.R;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.coach.CoachPlanWorker;
import com.cube.nanotimer.util.coach.CoachPlans;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

/**
 * The one question before any weekly plan: which solve type the plans are for, and whether its
 * figures may be sent for them every week without a tap. Agreeing to one is agreeing to the other.
 */
public final class CoachOptInDialog {

  private final Activity activity;
  private final Runnable onTurnedOn;
  private List<SolveType> eligible;
  private SolveType picked;
  private TextView typeView;
  private TextView bodyView;

  private CoachOptInDialog(Activity activity, Runnable onTurnedOn) {
    this.activity = activity;
    this.onTurnedOn = onTurnedOn;
  }

  /**
   * @param offered the solve type the plans are offered for first, when plans can be written for it
   * @param onTurnedOn run on the main thread once the solver has agreed
   */
  public static void show(final Activity activity, final SolveType offered, Runnable onTurnedOn) {
    final CoachOptInDialog dialog = new CoachOptInDialog(activity, onTurnedOn);
    App.INSTANCE.getService().getSolveTypes(CubeType.THREE_BY_THREE,
        new DataCallback<List<SolveType>>() {
          @Override
          public void onData(List<SolveType> solveTypes) {
            dialog.eligible = CoachPlans.eligible(solveTypes);
            if (dialog.eligible.isEmpty()) {
              return;
            }
            dialog.picked = dialog.eligible.get(0);
            for (SolveType solveType : dialog.eligible) {
              if (solveType.getId() == offered.getId()) {
                dialog.picked = solveType;
              }
            }
            activity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                if (!activity.isFinishing()) {
                  dialog.show();
                }
              }
            });
          }
        });
  }

  private void show() {
    View content = LayoutInflater.from(activity).inflate(R.layout.coach_opt_in_dialog, null);
    typeView = content.findViewById(R.id.tvCoachOptInType);
    bodyView = content.findViewById(R.id.tvCoachOptInBody);
    showPicked();
    // With one solve type to write them for there is nothing to choose.
    if (eligible.size() > 1) {
      content.findViewById(R.id.llCoachOptInType).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          pick();
        }
      });
    }
    new AlertDialog.Builder(activity, R.style.NanoTimerDialogTheme)
        .setView(content)
        .setNegativeButton(R.string.coach_opt_in_not_now, null)
        .setPositiveButton(R.string.coach_opt_in_turn_on, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            turnOn();
          }
        })
        .show();
  }

  private void showPicked() {
    String name = name(picked);
    typeView.setText(eligible.size() > 1 ? name + " ▾" : name);
    bodyView.setText(activity.getString(R.string.coach_opt_in_body, name));
  }

  private void pick() {
    String[] names = new String[eligible.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = name(eligible.get(i));
    }
    new AlertDialog.Builder(activity, R.style.NanoTimerDialogTheme)
        .setTitle(R.string.coach_plans_for)
        .setSingleChoiceItems(names, eligible.indexOf(picked),
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                picked = eligible.get(which);
                showPicked();
              }
            })
        .show();
  }

  private void turnOn() {
    CoachPlans.moveTo(picked);
    Options.INSTANCE.setCoachPlansOn(true);
    CoachPlanWorker.onAppStart(activity);
    askToNotify();
    onTurnedOn.run();
  }

  /** The plan and the reminder arrive as notifications, which Android 13 and later ask about. */
  private void askToNotify() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        && ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(activity,
          new String[] {Manifest.permission.POST_NOTIFICATIONS}, 0);
    }
  }

  private String name(SolveType solveType) {
    return Utils.toSolveTypeLocalizedName(activity, solveType.getName());
  }
}
