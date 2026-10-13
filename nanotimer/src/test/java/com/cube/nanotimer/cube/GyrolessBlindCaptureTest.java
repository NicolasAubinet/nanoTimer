package com.cube.nanotimer.cube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.SolveStep;
import java.util.List;
import org.junit.Test;

/**
 * A 3-style solve reported from a cube with no gyro: twelve slices, each read as the two faces the
 * cube reported, since no spin ever came to vouch for one.
 */
public class GyrolessBlindCaptureTest {

  private static final String MOVES =
      "[] U@8357 R@8448 D@8546 R'@8654 B'@8750 F@8767 D@8874 L'@8946 D'@9191 B@9277 F'@9292 "
      + "U'@9423 R@9690 D'@9788 F'@9880 D@9994 U'@10009 R@10100 R@10161 D'@10204 U@10215 F'@10294 "
      + "D@10343 R'@10405 R'@10864 D@10970 U'@10971 B@11071 U'@11109 B'@11189 U'@11469 D@11470 "
      + "L@11596 U@11679 L'@11738 D'@11788 U@11789 U@11845 D'@11846 R@11899 U@11971 R'@12025 "
      + "F'@12126 R@12165 F'@12228 B@12239 U'@12333 F@12422 U@12486 B'@12542 F@12545 U'@12725 "
      + "U@13053 D'@13054 F@13171 U'@13226 F'@13283 D@13366 U'@13387 R@13479 U@13544 R'@13598 "
      + "L@13951 U'@14071 D@14072 F'@14165 U@14212 F@14286 D'@14372 U@14380 L'@14458 U'@14502 "
      + "R'@14804 D'@14854 R@14931 U'@14980 R'@15047 D@15095 R@15173 U@15254 R'@15787 U@15855 "
      + "R@15956 R@16013 D'@16065 R'@16125 U'@16195 R@16240 D@16293 R'@16390 U@16441 R'@16499 "
      + "U'@16590 R@16640 U'@16804 R'@16906 R'@16963 D@17007 R@17057 U'@17091 R'@17170 D'@17210 "
      + "R@17281 U@17338 R@17415 U@17540";

  private static final String STEPS =
      "0:memo:8357:0 1:edges:1570:4575 1.0:UF-BD-LD:91:975 1.1:UF-BR-RU:267:715 "
      + "1.2:UF-RD-BL:459:1035 1.3:UF-DF-BU:72:754 1.4:UF-LF-FR:328:545 1.5:UF-RF-UL:353:551 "
      + "2:corners:999:2039 2.0:UFR-UFL-RDF:302:450 2.1:UFR-LDB-RDB:533:853 "
      + "2.2:UFR-BUL-DFL:164:736";

  @Test
  public void anUnsensedPairIsReadAsTheSliceItWas() {
    assertEquals("U R D R' S' R D' R' S U'", part(1, 0));
  }

  @Test
  public void everyEdgeAlgorithmIsSpelledWithItsSlices() {
    assertEquals("R D' F' E' F2 E F' D R'", part(1, 1));
    assertEquals("R' E' R U' R' E' R U R' E2 R", part(1, 2));
    assertEquals("U R' F' R S R' F R S' U'", part(1, 3));
    assertEquals("E R U' R' E' R U R'", part(1, 4));
    assertEquals("L E' L' U L E L' U'", part(1, 5));
  }

  @Test
  public void theCornersAfterThemKeepTheirGrip() {
    assertEquals("R' D' R U' R' D R U", part(2, 0));
  }

  @Test
  public void withNoStepsToHoldThemToNothingFolds() {
    for (SolveMovesFormat.Move move : SolveSolution.timedBlindSolution(MOVES, null, null)) {
      assertTrue(move.getNotation(), Slices.isFace(move.getNotation()));
    }
  }

  /** One pair the algorithm never takes back: it keeps its faces, and the frame is left alone. */
  @Test
  public void aTwoHanderLeavesTheAlgorithmsAfterItStanding() {
    String moves = MOVES.replace("R@14931 ", "R@14931 U@14950 D'@14953 ");
    assertEquals("R' D' R U D' U' R' D R U", part(moves, STEPS, 2, 0));
    assertEquals("R' U R2 D' R' U' R D R' U R' U' R", part(moves, STEPS, 2, 1));
  }

  /** Among slices that do cancel, the odd pair out is the two-hander and the rest still fold. */
  @Test
  public void aTwoHanderBesideRealSlicesIsTheOneLeftAsFaces() {
    String moves = MOVES.replace("R@8448 ", "R@8448 F@8500 B'@8503 ");
    assertEquals("U R F B' D R' S' R D' R' S U'", part(moves, STEPS, 1, 0));
    assertEquals("R D' F' E' F2 E F' D R'", part(moves, STEPS, 1, 1));
  }

  /**
   * An M2 solve off a gyro cube, read without its gyro. One M2 arrives as {@code R'}, {@code L R'},
   * {@code L}: timed alone it folds to a single {@code M'} and turns every letter after it.
   */
  @Test
  public void aSlowM2IsNotReadAsAQuarterSlice() {
    String moves = withoutGyro(SlowSliceCaptureTest.MOVES);
    assertEquals("L' U' L U M2 U' L' U L", part(moves, SlowSliceCaptureTest.STEPS, 1, 1));
    assertEquals("F2 R U' R' U' R U R' F' R U R' U' R' F R F2",
        part(moves, SlowSliceCaptureTest.STEPS, 2, 0));
  }

  /** The same odd pair out in an M2 target, where it is the M2 that stands and turns the core. */
  @Test
  public void aTwoHanderInAnM2TargetLeavesTheM2Folded() {
    String moves = withoutGyro(SlowSliceCaptureTest.MOVES)
        .replace("U'@8646 ", "U'@8646 F'@8878 B@8881 ");
    assertEquals("L' U' L U M2 U' L' U L", part(moves, SlowSliceCaptureTest.STEPS, 1, 1));
  }

  /** A slice a wide gave back: no gyro saw the wide, so its algorithm keeps its faces. */
  @Test
  public void aSliceAnUnseenWideUndoesKeepsItsFaces() {
    String moves = withoutGyro(BlindPeekCaptureTest.MOVES_249);
    assertEquals("F' B L' U L U2 F U B' U R U R' U'",
        part(moves, BlindPeekCaptureTest.STEPS_249, 3, 0));
    assertEquals("R' D' R D R' D' R U' R' D R D' R' D R U",
        part(moves, BlindPeekCaptureTest.STEPS_249, 4, 0));
  }

  private static String withoutGyro(String moves) {
    StringBuilder stripped = new StringBuilder();
    for (String token : moves.trim().split("\\s+")) {
      if (token.startsWith("[")
          || !SolveMovesFormat.isRotation(token.substring(0, token.indexOf('@')))) {
        stripped.append(token).append(' ');
      }
    }
    return stripped.toString().trim();
  }

  private static String part(int step, int part) {
    return part(MOVES, STEPS, step, part);
  }

  private static String part(String moves, String stepsText, int step, int part) {
    List<SolveStep> steps = SolveStepsFormat.parse(stepsText);
    return SolveSolution.from(moves, steps, CubeMethod.BLIND).getSteps().get(step)
        .getPartMoves(part);
  }
}
