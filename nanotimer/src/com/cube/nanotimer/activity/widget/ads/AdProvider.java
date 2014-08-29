package com.cube.nanotimer.activity.widget.ads;

import android.app.Activity;
import android.view.View;
import com.startapp.android.publish.SDKAdPreferences;
import com.startapp.android.publish.SDKAdPreferences.Gender;
import com.startapp.android.publish.StartAppSDK;
import com.startapp.android.publish.banner.Banner;

public class AdProvider {

//	private static StartAppAd startAppAd;

  public static void init(Activity a) {
  	StartAppSDK.init(a, "108845167", "208524420",
  			new SDKAdPreferences()
  				.setAge(20)
  				.setGender(Gender.MALE));
//  	startAppAd = new StartAppAd(a);
  }

  public static View getBannerAdView(Activity a) {
    return new Banner(a);
  }

//  public static void showInterstitial(Activity a) {
  // TODO : if using this, must also call resume() and pause() (see usage here: https://github.com/StartApp-SDK/Documentation/wiki/Android-InApp-Documentation)
//  	startAppAd.showAd();
//  	startAppAd.loadAd();
//  }

//  public static void resume() {
//  	startAppAd.onResume();
//  }
  
//  public static void pause() {
//  	startAppAd.onPause();
//  }

}
