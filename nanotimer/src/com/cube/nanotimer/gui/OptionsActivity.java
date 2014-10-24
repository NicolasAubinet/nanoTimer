package com.cube.nanotimer.gui;

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
import com.cube.nanotimer.scrambler.AlreadyGeneratingException;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.util.helper.DialogUtils;

public class OptionsActivity extends PreferenceActivity {

  private OnSharedPreferenceChangeListener prefChangedListener;

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
    } else if (key.equals(Options.PREGEN_SCRAMBLES)) {
      String strScramblesCount = pref.getString(Options.PREGEN_SCRAMBLES, "100");
      try {
        int nScrambles = Integer.parseInt(strScramblesCount);
        ScramblerService.INSTANCE.preGenerate(nScrambles);
        // TODO : display progression dialog with "Generating scramble 1 of 200" etc.
        // TODO :   also add a "Stop" button, and call binder.stopGeneration() if clicked (with confirmation msg)
        // TODO :   need a progression listener
      } catch (AlreadyGeneratingException e) {
        DialogUtils.showInfoMessage(this, R.string.scrambles_already_generating);
      } catch (NumberFormatException e) {
        DialogUtils.showInfoMessage(this, R.string.scrambles_already_generating);
      }
    }
  }

}
