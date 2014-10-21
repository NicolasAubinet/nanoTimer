package com.cube.nanotimer.gui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.ReleaseNotes;
import com.cube.nanotimer.scrambler.AlreadyGeneratingException;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.scrambler.ScramblerService.ScrambleServiceBinder;
import com.cube.nanotimer.util.helper.DialogUtils;

public class OptionsActivity extends PreferenceActivity {

  private ScrambleServiceBinder binder;
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

    Intent i = new Intent(this, ScramblerService.class);
    bindService(i, new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName componentName, IBinder binder) {
        OptionsActivity.this.binder = (ScrambleServiceBinder) binder;
      }

      @Override
      public void onServiceDisconnected(ComponentName componentName) {
        OptionsActivity.this.binder = null;
      }
    }, Context.BIND_AUTO_CREATE);
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
      if (binder != null) {
        binder.activateRandomStateScrambles(randomState);
      }
    } else if (key.equals(Options.PREGEN_SCRAMBLES)) {
      int nScrambles = pref.getInt(Options.PREGEN_SCRAMBLES, 100);
      if (binder != null) {
        try {
          binder.preGenerate(nScrambles);
          // TODO : display progression dialog with "Generating scramble 1 of 200" etc.
          // TODO :   also add a "Stop" button, and call binder.stopGeneration() if clicked (with confirmation msg)
          // TODO :   need a progression listener
        } catch (AlreadyGeneratingException e) {
          DialogUtils.showInfoMessage(this, R.string.scrambles_already_generating);
        }
      }
    }
  }

}
