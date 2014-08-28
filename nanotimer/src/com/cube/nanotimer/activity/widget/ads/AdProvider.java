package com.cube.nanotimer.activity.widget.ads;

import android.app.Activity;
import android.view.View;
import com.pfaey.opypm200398.AdView;

public class AdProvider {

  public static View getAdView(Activity a) {
    AdView adView = new AdView(a, AdView.BANNER_TYPE_IN_APP_AD, AdView.PLACEMENT_TYPE_INTERSTITIAL, false, false, AdView.ANIMATION_TYPE_LEFT_TO_RIGHT);
//    AdView adView = new AdView(a, AdView.BANNER_TYPE_IN_APP_AD, AdView.PLACEMENT_TYPE_INTERSTITIAL, true, false, AdView.ANIMATION_TYPE_LEFT_TO_RIGHT); // test mode
    return adView;
  }

}
