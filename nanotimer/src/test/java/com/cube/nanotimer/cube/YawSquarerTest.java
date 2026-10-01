package com.cube.nanotimer.cube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.cube.nanotimer.smartcube.model.CubeOrientation;
import com.cube.nanotimer.smartcube.model.CubeRotation;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * The drawn cube is squared once it rests, without touching the tilt. The page draws a pose as the
 * cube's rotation into the scene, so the gyro's world, and its yaw drift, multiply on the left.
 */
public class YawSquarerTest {

  private static final CubeOrientation REST = new CubeOrientation(1, 0, 0, 0);
  private static final long FRAME_MS = 16;

  @Test
  public void aDriftedGripIsSquaredWithWhiteOrYellowUp() {
    for (CubeRotation grip : upOrDownOnTop()) {
      for (double drift : new double[] {-22, -7, 12, 20}) {
        CubeOrientation pose = settle(new YawSquarer(), reading(grip, drift, 0));
        assertEquals(grip + " drift " + drift, 0, pose.angleToDegrees(grip.quaternion()), 0.5);
      }
    }
  }

  @Test
  public void theTiltIsKept() {
    for (CubeRotation grip : upOrDownOnTop()) {
      CubeOrientation pose = settle(new YawSquarer(), reading(grip, 18, 15));
      CubeOrientation untouched = CubeRotation.continuousFrame(REST, reading(grip, 0, 15));
      assertEquals(grip.toString(), 0, pose.angleToDegrees(untouched), 0.5);
    }
  }

  @Test
  public void aSideFaceOnTopIsLeftAlone() {
    CubeRotation grip = CubeRotation.byNotation("x");
    CubeOrientation pose = settle(new YawSquarer(), reading(grip, 15, 0));
    assertEquals(15, pose.angleToDegrees(grip.quaternion()), 0.5);
  }

  @Test
  public void aHoldFarOffSquareIsLeftAlone() {
    CubeRotation grip = CubeRotation.byNotation("x2");
    CubeOrientation pose = settle(new YawSquarer(), reading(grip, 35, 0));
    assertEquals(35, pose.angleToDegrees(grip.quaternion()), 0.5);
  }

  @Test
  public void nothingIsCorrectedBeforeTheCubeHasRested() {
    CubeRotation grip = CubeRotation.byNotation("x2");
    YawSquarer squarer = new YawSquarer();
    CubeOrientation pose = null;
    for (long t = 0; t < 400; t += FRAME_MS) {
      pose = squarer.pose(REST, reading(grip, 20, 0), t);
    }
    assertEquals(20, pose.angleToDegrees(grip.quaternion()), 0.5);
  }

  /** A regrip, a flip or a slice after squaring reads square at once: same drift, same fix. */
  @Test
  public void theCorrectionHoldsThroughRotationsAndSlices() {
    YawSquarer squarer = new YawSquarer();
    long t = settleUntil(squarer, reading(CubeRotation.byNotation("x2"), 15, 0));
    for (CubeRotation grip : CubeRotation.all()) {
      t += FRAME_MS;
      CubeOrientation pose = squarer.pose(REST, reading(grip, 15, 0), t);
      assertEquals(grip.toString(), 0, pose.angleToDegrees(grip.quaternion()), 0.5);
    }
  }

  @Test
  public void resetDropsTheCorrection() {
    CubeRotation grip = CubeRotation.byNotation("x2");
    YawSquarer squarer = new YawSquarer();
    long t = settleUntil(squarer, reading(grip, 15, 0));
    squarer.reset();
    CubeOrientation pose = squarer.pose(REST, reading(grip, 15, 0), t + FRAME_MS);
    assertEquals(15, pose.angleToDegrees(grip.quaternion()), 0.5);
  }

  @Test
  public void noReferenceDrawsNothing() {
    assertNull(new YawSquarer().pose(null, REST, 0));
  }

  private static List<CubeRotation> upOrDownOnTop() {
    List<CubeRotation> grips = new ArrayList<>(CubeRotation.withFaceUp('U'));
    grips.addAll(CubeRotation.withFaceUp('D'));
    return grips;
  }

  private static CubeOrientation settle(YawSquarer squarer, CubeOrientation reading) {
    return squarer.pose(REST, reading, settleUntil(squarer, reading) + FRAME_MS);
  }

  private static long settleUntil(YawSquarer squarer, CubeOrientation reading) {
    long t = 0;
    for (; t < 2000; t += FRAME_MS) {
      squarer.pose(REST, reading, t);
    }
    return t;
  }

  /** Held at {@code grip}, tipped by {@code tilt}, with the gyro's yaw off by {@code drift} degrees. */
  private static CubeOrientation reading(CubeRotation grip, double drift, double tilt) {
    CubeOrientation q = grip.quaternion();
    CubeOrientation held = new CubeOrientation(q.getW(), q.getX(), -q.getZ(), q.getY());
    return about(0, 0, 1, drift).multiply(held).multiply(about(1, 0, 0, tilt));
  }

  private static CubeOrientation about(double x, double y, double z, double degrees) {
    double half = Math.toRadians(degrees) / 2;
    double s = Math.sin(half);
    return new CubeOrientation(Math.cos(half), x * s, y * s, z * s);
  }
}
