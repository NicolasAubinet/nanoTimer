package com.cube.nanotimer.cube;

import com.cube.nanotimer.smartcube.model.CubeOrientation;
import com.cube.nanotimer.smartcube.model.CubeRotation;

/**
 * Keeps a drawn cube square while the gyro's yaw drifts, for a screen where the cube is held
 * square in the hands, as it is through a drill.
 *
 * <p>A gyro loses yaw while the cube is being turned (about 20° over a set of algorithms on a GAN
 * i4), and nothing in the cube's data can tell which way is front. But the solver holds the cube
 * square to their body almost all the time, so once it has been held still for a moment, whatever
 * separates the drawn cube from the nearest square grip is drift, and is taken back out of the yaw.
 * Gravity already fixes the up face, so only the yaw is corrected, and only while the white or
 * yellow face is on top, which covers every drill grip.
 *
 * <p>Regrips, rotations and slices need nothing special: the gyro follows them (it turns with the
 * core), and a cube resting after one is square again, with the same small drift left to remove.
 * A hold further off square than {@link #MAX_CORRECTION_DEGREES} is taken as deliberate and left
 * alone; drift corrected every time the cube rests never gets that far.
 *
 * <p>For the drawn cube only. The session's {@link GyroReference} is never changed, so the frames a
 * solve is reconstructed in are not affected.
 */
public final class YawSquarer {

  /** How long the cube must be held still before it is squared. */
  private static final long SETTLE_MS = 500;
  /** Movement below this still counts as still; a GAN i4 at rest reads about 1° of noise. */
  private static final double STILL_DEGREES = 4;
  /** Further off square than this is a deliberate hold, not drift. */
  static final double MAX_CORRECTION_DEGREES = 25;
  /** Time constant of the ease onto square, so the correction is not seen as a jump. */
  private static final double EASE_MS = 150;

  /** The reading the current still stretch started at, and when. */
  private CubeOrientation stillFrom;
  private long stillFromMs;
  private long lastMs = -1;
  /** Yaw added to the reference, in degrees about the vertical. */
  private double correctionDegrees;

  /**
   * The pose to draw {@code reading} at, as {@link CubeRotation#continuousFrame} but with the drift
   * taken out. Null where there is no reference or no reading.
   */
  public synchronized CubeOrientation pose(CubeOrientation reference, CubeOrientation reading,
      long nowMs) {
    if (reference == null || reading == null) {
      return null;
    }
    long elapsedMs = lastMs < 0 ? 0 : Math.max(0, nowMs - lastMs);
    lastMs = nowMs;
    if (stillFrom == null || stillFrom.angleToDegrees(reading) > STILL_DEGREES) {
      stillFrom = reading;
      stillFromMs = nowMs;
    } else if (nowMs - stillFromMs >= SETTLE_MS && isUpOrDownOnTop(reading)) {
      double offSquare = CubeRotation.yawOffSquare(
          corrected(reference).deltaTo(CubeRotation.upright(reading)));
      if (Math.abs(offSquare) <= MAX_CORRECTION_DEGREES) {
        correctionDegrees += offSquare * (1 - Math.exp(-elapsedMs / EASE_MS));
      }
    }
    return CubeRotation.continuousFrame(corrected(reference), reading);
  }

  /** Drops the correction, for a reference just taken: it is exact until the cube drifts again. */
  public synchronized void reset() {
    stillFrom = null;
    lastMs = -1;
    correctionDegrees = 0;
  }

  /** With a side face on top, the vertical is off the axis the reference turns about. */
  private static boolean isUpOrDownOnTop(CubeOrientation reading) {
    char up = CubeRotation.upFace(reading);
    return up == 'U' || up == 'D';
  }

  private CubeOrientation corrected(CubeOrientation reference) {
    // The gyro's vertical is its z axis.
    double half = Math.toRadians(correctionDegrees) / 2;
    return reference.multiply(new CubeOrientation(Math.cos(half), 0, 0, Math.sin(half)));
  }
}
