package com.cube.nanotimer.activity.widget.ads;

import android.app.Activity;
import android.view.View;
import com.startapp.android.publish.SDKAdPreferences;
import com.startapp.android.publish.SDKAdPreferences.Gender;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;
import com.startapp.android.publish.banner.Banner;

public class AdProvider {

//	private static AvocarrotInterstitial myAd;
	private static StartAppAd startAppAd;

  public static void init(Activity a) {
    // Initialize Avocarrot
//    Avocarrot.initWithKey(a.getApplicationContext(), "529fd76c53416ba1939dc035ac9123a2a937b60a");
//    Avocarrot.setSandbox(true);
    // Pre-load Avocarrot Ad
//    myAd = new AvocarrotInterstitial();
//    myAd.loadAdForPlacement("default");
  	
  	// Startapp
  	StartAppSDK.init(a, "108845167", "208524420",
  			new SDKAdPreferences()
  				.setAge(20)
  				.setGender(Gender.MALE));
  	startAppAd = new StartAppAd(a);
  }

  public static void showInterstitial(Activity a) {
  	// Avocarrot
//    myAd.showAd(a, "default");
  	// Startapp
  	startAppAd.showAd();
  	startAppAd.loadAd();
  }

  public static View getBannerAdView(Activity a) {
    // Startapp
    return new Banner(a);

    // Airpush
//    AdView adView = new AdView(a, AdView.BANNER_TYPE_IN_APP_AD, AdView.PLACEMENT_TYPE_INTERSTITIAL, false, false, AdView.ANIMATION_TYPE_LEFT_TO_RIGHT);
//    AdView adView = new AdView(a, AdView.BANNER_TYPE_IN_APP_AD, AdView.PLACEMENT_TYPE_INTERSTITIAL, true, false, AdView.ANIMATION_TYPE_LEFT_TO_RIGHT); // test mode
//    return adView;

    // Avocarrot
    /*AvocarrotCustom myAd = new AvocarrotCustom();
    myAd.loadAdForPlacement(a, "default");

    final LinearLayout layout = new LinearLayout(a);
    layout.setOrientation(LinearLayout.HORIZONTAL);
    layout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50));
    final ImageView imgView = new ImageView(a);
    final TextView textView = new TextView(a);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
    layout.addView(imgView);
    layout.addView(textView);

    // Setup a CustomAd Listener (required)
    myAd.setAdListener(new AvocarrotCustomListener() {

      @Override
      public void adDidLoad(final CustomAdItem ad) {
        Log.d("Avocarrot", "adDidLoad");

        // Fill in details in your view
        if (ad.getHeadline() != null)
          textView.setText(ad.getHeadline());

        if (ad.getImage() != null)
          imgView.setImageBitmap(ad.getImage());

        // Bind view
        ad.bindView(layout);

        // Set click listener
//          myButton.setOnClickListener(new View.OnClickListener() {
//              @Override
//              public void onClick(View v) {
//                  ad.handleClick();
//              }
//          });
      }

      @Override
      public void adDidNotLoad(String reason) {
        Log.d("Avocarrot", "adDidNotLoad: " + reason);
      }

      @Override
      public void adDidFailToLoad(Exception e) {
        Log.e("Avocarrot", "adDidFailToLoad", e);
      }

      @Override
      public void onAdImpression(String message) {
        Log.d("Avocarrot", "onAdImpression: " + message);
      }

      @Override
      public void onAdClick(String message) {
        Log.d("Avocarrot", "onAdClick: " + message);
      }

      @Override
      public void userWillLeaveApp() {
        Log.d("Avocarrot", "userWillLeaveApp");
      }
    });

    return layout;*/
  }
  
  public static void resume() {
  	startAppAd.onResume();
  }
  
  public static void pause() {
  	startAppAd.onPause();
  }

}
