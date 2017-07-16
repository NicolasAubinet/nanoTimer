package com.cube.nanotimer.gui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.cube.nanotimer.R;


public class DrawerLayoutActivity extends AppCompatActivity {
  protected DrawerLayout drawerLayout;
  protected ActionBarDrawerToggle drawerToggle;
  protected NavigationView navigationView;

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    drawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    drawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Pass the event to ActionBarDrawerToggle, if it returns
    // true, then it has handled the app icon touch event
    if (drawerToggle.onOptionsItemSelected(item)) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  protected void initViews() {
    drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
    drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
    drawerLayout.addDrawerListener(drawerToggle);

//    drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        drawerLayout.openDrawer(GravityCompat.START);
//      }
//    });
//    drawerToggle.syncState();

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);

    navigationView = (NavigationView) findViewById(R.id.navigationView);
    navigationView.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
      @Override
      public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawer(navigationView);
        return onOptionsItemSelected(item);
      }
    });
  }

}
