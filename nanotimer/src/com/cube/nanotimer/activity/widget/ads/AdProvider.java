package com.cube.nanotimer.activity.widget.ads;

import android.app.Activity;
import android.view.View;
import com.pfaey.opypm200398.AdListener.MraidAdListener;
import com.pfaey.opypm200398.AdView;

public class AdProvider {

  public static View getAdView(Activity a) {
    AdView adView = new AdView(a, AdView.BANNER_TYPE_IN_APP_AD, AdView.PLACEMENT_TYPE_INTERSTITIAL, false, false, AdView.ANIMATION_TYPE_LEFT_TO_RIGHT);
    adView.setAdListener(adListener);
    return adView;
  }

  private static MraidAdListener adListener = new MraidAdListener() {
    @Override
    public void onAdLoadingListener() {
    }

    @Override
    public void onAdLoadedListener() {
    }

    @Override
    public void onErrorListener(String s) {
    }

    @Override
    public void onCloseListener() {
    }

    @Override
    public void onAdExpandedListner() {
    }

    @Override
    public void onAdClickListener() {
    }

    @Override
    public void noAdAvailableListener() {
    }
  };

}
