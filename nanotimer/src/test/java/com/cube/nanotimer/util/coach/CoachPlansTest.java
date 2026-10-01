package com.cube.nanotimer.util.coach;

import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ScrambleType;
import com.cube.nanotimer.vo.ScrambleTypes;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.vo.SolveTypeStep;

import org.junit.Assert;
import org.junit.Test;

/** Which solve types the weekly plans can be written for. */
public class CoachPlansTest {

  private static final int THREE = CubeType.THREE_BY_THREE.getId();

  @Test
  public void aWholeSightedCfop3x3IsEligible() {
    Assert.assertTrue(CoachPlans.eligible(type(false, null, THREE), CubeMethod.CFOP));
    Assert.assertTrue(
        CoachPlans.eligible(type(false, ScrambleTypes.DEFAULT, THREE), CubeMethod.CFOP));
  }

  @Test
  public void anotherMethodIsNot() {
    Assert.assertFalse(CoachPlans.eligible(type(false, null, THREE), CubeMethod.ROUX));
  }

  @Test
  public void aBlindTypeIsNot() {
    Assert.assertFalse(CoachPlans.eligible(type(true, null, THREE), CubeMethod.CFOP));
    Assert.assertFalse(CoachPlans.eligible(type(true, null, THREE))); // read as blind
  }

  @Test
  public void aSpecialScrambleIsNot() {
    ScrambleType pll = new ScrambleType("pll") {
    };
    Assert.assertFalse(CoachPlans.eligible(type(false, pll, THREE), CubeMethod.CFOP));
  }

  /** Its solves are split by its own steps, and no method is read into them. */
  @Test
  public void aTypeWithStepsOfItsOwnIsNot() {
    SolveType stepped = type(false, null, THREE);
    stepped.setSteps(new SolveTypeStep[] {new SolveTypeStep()});

    Assert.assertFalse(CoachPlans.eligible(stepped, CubeMethod.CFOP));
  }

  @Test
  public void anotherPuzzleIsNot() {
    Assert.assertFalse(CoachPlans.eligible(type(false, null, THREE + 1), CubeMethod.CFOP));
  }

  /** A method named on the type decides, whatever the preferred one is. */
  @Test
  public void theTypesOwnMethodIsTheOneRead() {
    SolveType roux = type(false, null, THREE);
    roux.setMethod(CubeMethod.ROUX);
    SolveType cfop = type(false, null, THREE);
    cfop.setMethod(CubeMethod.CFOP);

    Assert.assertFalse(CoachPlans.eligible(roux));
    Assert.assertTrue(CoachPlans.eligible(cfop));
  }

  /** What a change of the preferred method would do to the solve type that carries the plans. */
  @Test
  public void aTypeThatFollowsThePreferredMethodIsEligibleOnlyUnderCfop() {
    SolveType follows = type(false, null, THREE);
    SolveType cfop = type(false, null, THREE);
    cfop.setMethod(CubeMethod.CFOP);

    Assert.assertTrue(CoachPlans.eligibleUnder(follows, CubeMethod.CFOP));
    Assert.assertFalse(CoachPlans.eligibleUnder(follows, CubeMethod.ROUX));
    Assert.assertTrue(CoachPlans.eligibleUnder(cfop, CubeMethod.ROUX));
  }

  private static SolveType type(boolean blind, ScrambleType scrambleType, int cubeTypeId) {
    return new SolveType(1, "type", blind, scrambleType, cubeTypeId);
  }
}
