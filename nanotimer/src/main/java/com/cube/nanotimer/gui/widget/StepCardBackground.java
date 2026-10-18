package com.cube.nanotimer.gui.widget;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.cube.nanotimer.R;

/**
 * The drill screens' card: the app's raised card, lit from the top in the step colour of what is
 * being drilled. Shared so the card a drill is picked on and the one it ends on are the same object.
 */
public final class StepCardBackground {

  private static final int WASH_ALPHA = 0x3D;

  private StepCardBackground() {
  }

  /** Paints the card in a step colour, keeping the padding its layout gave it. */
  public static void paint(View card, int colour) {
    Resources res = card.getResources();
    GradientDrawable wash = new GradientDrawable();
    wash.setGradientType(GradientDrawable.RADIAL_GRADIENT);
    wash.setColors(new int[] {withAlpha(colour, WASH_ALPHA), withAlpha(colour, 0)});
    wash.setGradientCenter(0.5f, 0f);
    wash.setGradientRadius(res.getDisplayMetrics().widthPixels * 0.7f);
    wash.setCornerRadius(res.getDimension(R.dimen.card_corner));
    LayerDrawable background = new LayerDrawable(new Drawable[] {
        ContextCompat.getDrawable(card.getContext(), R.drawable.analysis_card), wash});
    float density = res.getDisplayMetrics().density;
    background.setLayerInset(1, 0, Math.round(density), 0, Math.round(3 * density));
    int left = card.getPaddingLeft();
    int top = card.getPaddingTop();
    int right = card.getPaddingRight();
    int bottom = card.getPaddingBottom();
    card.setBackground(background);
    card.setPadding(left, top, right, bottom);
  }

  public static int withAlpha(int colour, int alpha) {
    return (colour & 0x00FFFFFF) | (alpha << 24);
  }
}
