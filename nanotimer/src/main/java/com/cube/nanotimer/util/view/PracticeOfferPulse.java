package com.cube.nanotimer.util.view;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * One swell of the drill's offer to practise a case as it appears, so it is seen during the beat
 * the user spends looking at the cube. Once only: a button that keeps moving is noise.
 */
public final class PracticeOfferPulse {

  private static final float PEAK_SCALE = 1.15f;
  private static final long UP_MS = 160;
  private static final long DOWN_MS = 260;
  private static final float OVERSHOOT = 3f;

  private PracticeOfferPulse() {
  }

  public static void play(final View offer) {
    offer.animate().cancel();
    offer.setScaleX(1f);
    offer.setScaleY(1f);
    offer.animate().scaleX(PEAK_SCALE).scaleY(PEAK_SCALE).setDuration(UP_MS)
        .setInterpolator(new DecelerateInterpolator()) // the down phase's overshoot otherwise sticks
        .withEndAction(new Runnable() {
          @Override
          public void run() {
            offer.animate().scaleX(1f).scaleY(1f).setDuration(DOWN_MS)
                .setInterpolator(new OvershootInterpolator(OVERSHOOT));
          }
        });
  }
}
