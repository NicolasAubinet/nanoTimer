package com.cube.nanotimer.util.view;

import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * The learning drill's algorithm leaving the screen once a case has gone in with it enough times.
 * A slow fade rather than a cut, so its going reads as a step forward and not as something lost.
 * The view keeps its room, so the cube above it does not move.
 */
public final class LearningAlgorithmFade {

  private static final long FADE_MS = 900;

  private LearningAlgorithmFade() {
  }

  public static void show(View algorithm) {
    algorithm.animate().cancel();
    algorithm.setAlpha(1f);
    algorithm.setVisibility(View.VISIBLE);
  }

  /** Fades a shown algorithm out; one not on screen just keeps its room. */
  public static void hide(final View algorithm) {
    algorithm.animate().cancel();
    if (algorithm.getVisibility() != View.VISIBLE) {
      algorithm.setVisibility(View.INVISIBLE);
      return;
    }
    algorithm.animate().alpha(0f).setDuration(FADE_MS)
        .setInterpolator(new AccelerateInterpolator())
        .withEndAction(new Runnable() {
          @Override
          public void run() {
            algorithm.setVisibility(View.INVISIBLE);
            algorithm.setAlpha(1f);
          }
        });
  }
}
