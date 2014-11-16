package com.cube.nanotimer.gui;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.ReleaseNotes;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent.State;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenListener;
import com.cube.nanotimer.util.YesNoListener;
import com.cube.nanotimer.util.helper.DialogUtils;

public class OptionsActivity extends PreferenceActivity {

  private OnSharedPreferenceChangeListener prefChangedListener;
  private AlertDialog dialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle(R.string.settings);
    addPreferencesFromResource(R.xml.preferences);

    prefChangedListener = new OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        preferenceChanged(sharedPreferences, key);
      }
    };
    PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(prefChangedListener);
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
    return true;
  }

  private void preferenceChanged(SharedPreferences pref, String key) {
    if (key.equals(Options.RANDOMSTATE_SCRAMBLES)) {
      Boolean defaultValue = getResources().getBoolean(R.bool.randomstate_scrambles);
      boolean randomState = pref.getBoolean(Options.RANDOMSTATE_SCRAMBLES, defaultValue);
      ScramblerService.INSTANCE.activateRandomStateScrambles(randomState);
    } else if (key.equals(Options.SCRAMBLES_QUALITY)) {
      // Calls to runOnUiThread and isFinishing were added to fix a crash when opening the dialog
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (!isFinishing()) {
            DialogUtils.showYesNoConfirmation(OptionsActivity.this, R.string.want_to_clear_scramble_cache, new YesNoListener() {
              @Override
              public void onYes() {
                ScramblerService.INSTANCE.stopGeneration();
                ScramblerService.INSTANCE.addRandomStateGenListener(new RandomStateGenListener() {
                  @Override
                  public void onStateUpdate(RandomStateGenEvent event) {
                    if (event.getState() == State.IDLE) {
                      ScramblerService.INSTANCE.removeRandomStateGenListener(this);
                      ScramblerService.INSTANCE.deleteCaches();
                      ScramblerService.INSTANCE.checkScrambleCaches();
                    }
                  }
                });
              }
            });
          }
        }
      });
    }
  }

}
