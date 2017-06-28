package com.cube.nanotimer.gui.widget.ads;

import android.app.Activity;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.Options.AdsStyle;
import com.startapp.android.publish.SDKAdPreferences;
import com.startapp.android.publish.SDKAdPreferences.Gender;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

import java.util.Random;

public class AdProvider {

	private static StartAppAd startAppAd;
//  private static Appnext appnext;
  private static boolean interstitialShown; // true if the last call to showInterstitial did display an ad

  public static void init(Activity a) {
    if (!isAdsEnabled()) {
      return;
    }
    // Startapp
    // banner
  	StartAppSDK.init(a, "108845167", "208524420",
  			new SDKAdPreferences()
  				.setAge(20)
  				.setGender(Gender.MALE), false);
  	// interstitial
  	startAppAd = new StartAppAd(a);
  	startAppAd.loadAd();
  	
  	// Appnext
  	// interstitial
//  	appnext = new Appnext(a);
//    appnext.setAppID("dd009c3e-f718-4fbe-8897-b8f560e6eace");
//    appnext.cacheAd();
//    appnext.setPopupClosedCallback(new PopupClosedInterface() {
//      @Override
//      public void popupClosed() {
//        appnext.cacheAd();
//      }
//    });
  }

  public static void showInterstitial() {
    if (!isAdsEnabled()) {
      return;
    }
    if (interstitialShown) {
      // don't show interstitial ads two times in a row
      interstitialShown = false;
      return;
    }
    int frequency; // from 0 to 10, 0 being never, 10 being every time
    AdsStyle style = Options.INSTANCE.getAdsStyle();
    if (style == AdsStyle.INTERSTITIAL) {
      frequency = 5;
    } else if (style == AdsStyle.MIXED) {
      frequency = 3;
    } else {
      interstitialShown = false;
      return;
    }

    Random r = new Random();
    if (r.nextInt(10) >= frequency) {
      interstitialShown = false;
      return;
    }
    int adChoice = r.nextInt(2);
    if (adChoice == 0) {
      // startapp
    	startAppAd.showAd();
    	startAppAd.loadAd();
    } else if (adChoice == 1) {
    	// appnext
//      appnext.showBubble();
    }
    interstitialShown = true;
    return;
  }

  public static boolean hideInterstitial() {
//    if (appnext != null && appnext.isBubbleVisible()) {
//      appnext.hideBubble();
//      return true;
//    }
    // no need to handle startapp here as it is like a separate activity. backspace closes it already
    return false;
  }

  public static boolean isInterstialAppnextDisplayed() {
//    return appnext != null && appnext.isBubbleVisible();
    return false;
  }

  public static void resume() {
    if (!isAdsEnabled()) {
      return;
    }
    if (startAppAd != null) {
      startAppAd.onResume();
    }
  }
  
  public static void pause() {
    if (!isAdsEnabled()) {
      return;
    }
    if (startAppAd != null) {
      startAppAd.onPause();
    }
  }

  public static boolean wasInterstitialShown() {
    return interstitialShown;
  }

  private static boolean isAdsEnabled() {
    return Options.INSTANCE.isAdsEnabled();
  }

}
