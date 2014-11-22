package com.cube.nanotimerpro;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UnlockerActivity extends Activity {

  private static final String NANOTIMERFREE_PACKAGE = "com.cube.nanotimer";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.unlocker_screen);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (isAppInstalled(NANOTIMERFREE_PACKAGE)) {
      ((TextView) findViewById(R.id.tvMainTitle)).setText(R.string.nanotimer_unlocked);
      findViewById(R.id.tvAction).setVisibility(View.VISIBLE);
      ((TextView) findViewById(R.id.tvAction)).setText(R.string.open_nanotimer);

      Button buAction = (Button) findViewById(R.id.buAction);
      buAction.setText(R.string.open);
      buAction.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          Intent launchIntent = getPackageManager().getLaunchIntentForPackage(NANOTIMERFREE_PACKAGE);
          if (launchIntent != null) {
            startActivity(launchIntent);
          } else {
            Toast.makeText(UnlockerActivity.this, R.string.could_not_launch_nanotimer, Toast.LENGTH_LONG).show();
          }
        }
      });
    } else {
      ((TextView) findViewById(R.id.tvMainTitle)).setText(R.string.nanotimer_should_be_installed);
      findViewById(R.id.tvAction).setVisibility(View.GONE);

      Button buAction = (Button) findViewById(R.id.buAction);
      buAction.setText(R.string.go_to_play_store);
      buAction.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + NANOTIMERFREE_PACKAGE));
          if (getPackageManager().queryIntentActivities(storeIntent, 0).size() > 0) {
            try {
              startActivity(storeIntent);
            } catch (ActivityNotFoundException e) {
              Toast.makeText(UnlockerActivity.this, R.string.could_not_launch_market, Toast.LENGTH_LONG).show();
            }
          } else {
            Toast.makeText(UnlockerActivity.this, R.string.could_not_find_market, Toast.LENGTH_LONG).show();
          }
        }
      });
    }
  }

  private boolean isAppInstalled(String packageName) {
    boolean installed;
    try {
      getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
      installed = true;
    } catch (PackageManager.NameNotFoundException e) {
      installed = false;
    }
    return installed;

    // OR (other way to check)
//    List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//    boolean installed = list.size() > 0;

    // OR (other way to check)
//    boolean installed = (i.resolveActivityInfo(getPackageManager(), 0) != null);
  }

}
