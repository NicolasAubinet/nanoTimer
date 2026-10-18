package com.cube.nanotimer.gui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.cube.nanotimer.R;

/**
 * One face of the cube as nine stickers on the case stage: the picture a drill with no case set
 * shows on the setup card, a cross for the cross drill and an open slot for F2L.
 */
public class DrillFacePicture extends View {

  private static final float PAD = 0.17f;
  private static final float GAP = 0.045f;
  private static final float STAGE_RADIUS = 0.18f;

  private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF sticker = new RectF();
  private final GradientDrawable stage;
  private int[] stickers = new int[9];

  public DrillFacePicture(Context context) {
    this(context, null);
  }

  public DrillFacePicture(Context context, AttributeSet attrs) {
    super(context, attrs);
    fill.setStyle(Paint.Style.FILL);
    stage = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.case_stage).mutate();
  }

  /** The nine sticker colours, row by row from the top left. */
  public void setStickers(int[] colours) {
    stickers = colours.clone();
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    float size = Math.min(getWidth(), getHeight());
    stage.setCornerRadius(size * STAGE_RADIUS);
    stage.setBounds(0, 0, (int) size, (int) size);
    stage.draw(canvas);

    float origin = size * PAD;
    float gap = size * GAP;
    float cell = (size - 2 * origin - 2 * gap) / 3;
    float radius = cell * 0.16f;
    for (int i = 0; i < 9; i++) {
      float left = origin + (i % 3) * (cell + gap);
      float top = origin + (i / 3) * (cell + gap);
      sticker.set(left, top, left + cell, top + cell);
      fill.setColor(stickers[i]);
      canvas.drawRoundRect(sticker, radius, radius, fill);
    }
  }
}
