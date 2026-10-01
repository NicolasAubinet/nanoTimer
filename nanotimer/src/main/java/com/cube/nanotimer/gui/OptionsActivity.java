package com.cube.nanotimer.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.view.Menu;
import android.view.MenuItem;
import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.R;
import com.cube.nanotimer.cube.SmartCubeGate;
import com.cube.nanotimer.gui.widget.CoachPlansForDialog;
import com.cube.nanotimer.gui.widget.ReleaseNotes;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.coach.CoachClient;
import com.cube.nanotimer.util.coach.CoachPlans;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

public class OptionsActivity extends NanoTimerActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.empty_screen);
    setTitle(R.string.settings);

    getSupportFragmentManager().beginTransaction().replace(R.id.containerLayout, new OptionsFragment()).commit();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.itReleaseNotes:
        ReleaseNotes.showReleaseNotesDialog(this);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  public static class OptionsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);

      Preference smartCube = findPreference("smart_cube_category");
      if (smartCube != null && !SmartCubeGate.ENABLED) {
        getPreferenceScreen().removePreference(smartCube);
      }

      showCoach();

      // Null when the reflection-based PreferenceFragment could not build its manager, which it
      // swallows rather than crashing on.
      Preference editSolveTypes = findPreference("edit_solve_types");
      if (editSolveTypes != null) {
        editSolveTypes.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(Preference preference) {
            startActivity(new Intent(getActivity(), SolveTypesActivity.class));
            return true;
          }
        });
      }
    }

    /** The coach's settings, for a solver who has weekly plans: which solve type they are for. */
    private void showCoach() {
      Preference coach = findPreference("coach_category");
      final Preference plansFor = findPreference("coach_plans_for");
      if (coach == null || plansFor == null) {
        return;
      }
      if (!SmartCubeGate.ENABLED || CoachClient.configured(getActivity()) == null
          || !Options.INSTANCE.isCoachPlansOn()) {
        getPreferenceScreen().removePreference(coach);
        return;
      }
      showPlansFor(plansFor);
      plansFor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          CoachPlansForDialog.show(getActivity(), new Runnable() {
            @Override
            public void run() {
              showPlansFor(plansFor);
            }
          });
          return true;
        }
      });
    }

    private void showPlansFor(final Preference plansFor) {
      App.INSTANCE.getService().getSolveTypes(CubeType.THREE_BY_THREE,
          new DataCallback<List<SolveType>>() {
            @Override
            public void onData(List<SolveType> solveTypes) {
              final SolveType flagged = CoachPlans.flagged(solveTypes);
              final Activity activity = getActivity();
              if (activity == null) {
                return;
              }
              activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  plansFor.setSummary(flagged == null
                      ? activity.getString(R.string.coach_plans_setting_paused)
                      : activity.getString(R.string.coach_plans_setting_summary,
                          Utils.toSolveTypeLocalizedName(activity, flagged.getName())));
                }
              });
            }
          });
    }
  }

}
