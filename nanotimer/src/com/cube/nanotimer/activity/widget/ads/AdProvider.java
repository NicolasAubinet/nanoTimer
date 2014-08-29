package com.cube.nanotimer.activity.widget.ads;

import android.app.Activity;
import android.view.View;
import com.appnext.appnextsdk.Appnext;
import com.startapp.android.publish.SDKAdPreferences;
import com.startapp.android.publish.SDKAdPreferences.Gender;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;
import com.startapp.android.publish.banner.Banner;

import java.util.Random;

public class AdProvider {

  private static Activity activity;
	private static StartAppAd startAppAd;
  private static Appnext appnext;
  private static Banner banner;

  public static void init(Activity a) {
    activity = a;
    // Startapp
    // banner
  	StartAppSDK.init(a, "108845167", "208524420",
  			new SDKAdPreferences()
  				.setAge(20)
  				.setGender(Gender.MALE));
  	banner = new Banner(a);
  	// interstitial
  	startAppAd = new StartAppAd(a);
  	startAppAd.loadAd();
  	
  	// Appnext
  	// interstitial
  	appnext = new Appnext(activity);
    appnext.setAppID("dd009c3e-f718-4fbe-8897-b8f560e6eace");
    appnext.cacheAd();
  }

  public static View getBannerAdView() {
    return banner;
  }

  public static void showInterstitial() {
    int adChoice = new Random().nextInt(2);
    if (adChoice == 0) {
      // startapp
    	startAppAd.showAd();
    	startAppAd.loadAd();
    } else if (adChoice == 1) {
    	// appnext
      appnext.showBubble();
      appnext.cacheAd();
    }
  }
  
  public static boolean hideInterstitial() {
    if (appnext != null && appnext.isBubbleVisible()) {
      appnext.hideBubble();
      return true;
    }
    return false;
  }

  public static void resume() {
  	startAppAd.onResume();
  }
  
  public static void pause() {
  	startAppAd.onPause();
  }

}
