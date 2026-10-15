package com.cube.nanotimer.cube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.cube.nanotimer.vo.BlindMethod;
import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.SolveStep;
import java.util.List;
import org.junit.Test;

/**
 * A 3-style DNF off a GAN i4, timed on Oct 14 2026 - 19:21:59. Its second edge algorithm ends on
 * an M2 turned as four faces, of which the gyro vouched for the middle two: read that way the M2 is
 * a quarter slice between two faces, and every algorithm after it was spelled a quarter turn out.
 */
public class SplitM2CaptureTest {

  private static final String SCRAMBLE = "D L2 F2 U' F2 D2 F2 R2 F2 R2 U2 R B' L B2 L2 R D' F' U2";

  private static final String MOVES =
      "[y] y@31781 B'@31781 y@31881 x@31881 R'@31881 B'@32150 U@32311 D'@32322 z'@32478 R@32478 "
      + "y'@32550 z@32550 R@32550 D@32689 U'@32689 y@32690 y'@32818 z@32818 B'@32818 y@33100 "
      + "x@33100 R@33100 z@33469 B@33470 F@34026 B'@34046 z'@34047 R@34231 R@34359 F@34508 "
      + "B'@34522 z'@34523 D@34694 D@34808 y@34937 B'@34937 F@35271 B'@35312 z'@35313 x'@35339 "
      + "F@35339 U'@35726 D@35749 y@35750 y'@35997 x'@35997 R'@35997 U@36115 D'@36137 y'@36138 "
      + "F@36288 U@36416 F'@36557 D@36701 U'@36723 y@36724 R@36917 y'@37325 D'@37326 F'@39446 "
      + "B@39494 z@39495 F'@39547 L'@39734 F@39886 B'@39893 z'@39894 U'@40096 U'@40207 B@40347 "
      + "F'@40383 z@40384 x'@40618 L'@40618 F@40834 B'@40890 z'@40891 z@40916 F@40916 y'@41585 "
      + "z'@41585 R@41585 F@41824 U'@41971 D@41997 y@41998 R'@42199 R'@42260 D'@42444 U@42444 "
      + "F@42646 y'@42922 R'@42922 B@43184 U@43374 B'@43552 U@43668 D@43674 B@43804 D@43965 "
      + "B'@44087 U@44223 U@44329 B@44386 D'@44545 B'@44677 U@44797 D'@44837 B@44967 U'@45358 "
      + "B'@45801 y@46557 B@46557 y'@46686 U@46686 B'@46788 D@46837 y@46964 B@46964 U'@47028 "
      + "y'@47144 B@47144 D'@47225 B'@47323 U'@47488 B@47576 D@47655 B'@47775 U@47878 B'@48088 "
      + "D'@49727 U'@50844 B@50951 D'@51043 B'@51578 U@51654 U@51740 B@51779 D@52010 B'@52179 "
      + "U'@52672 x'@55938 L'@55939 B@56256 D'@56409 z'@56524 F'@56524 D@56677 B@56794 B@56882 "
      + "D'@56967 F@57071 D@57287 B@58508 x@58771 L@58772";

  private static final String STEPS =
      "0:memo:31781:0 1:edges:3832:7309 1.0:UF-RF-RB:100:1589 1.1:UF-DB-DF:556:1313 "
      + "1.2:UF-BL-UL:387:1600 1.3:UF-RU-LF:2120:1470 1.4:UF-LD-LU:669:1337 "
      + "2:corners:5402:10448 2.0:UFR-FUL-BUL:262:2617 2.1:UFR-RDF-DBR:756:3170 "
      + "2.2:UFR-UBL-LDB:1117:1828 2.3:UFR-FDL-RUB:3267:2833";

  @Test
  public void theM2IsReadAsTwoSlices() {
    assertEquals("M' U2 M' U2 M2", part(1, 1));
  }

  @Test
  public void theCornersAfterItAreSpelledInTheGrip() {
    assertEquals("R U R' D R U' R D' R' U' R D R' U R' D'", part(2, 1));
    assertEquals("U' R D' R' U2 R D R' U'", part(2, 2));
  }

  /** It broke into a corner already home; the target after it was right, so the wanted keeps it. */
  @Test
  public void aBreakInIntoASolvedCornerSaysTheOneItOwed() {
    StoredSolveReplay.Result read = StoredSolveReplay.reinterpret(SCRAMBLE, MOVES,
        CubeMethod.BLIND, BlindMethod.THREE_STYLE);
    assertNotNull(read);
    SolveStep breakIn = read.getSteps().get(2).getSubSteps().get(2);
    assertEquals("UFR-UBL-LDB", breakIn.getName());
    assertEquals("UFR-UBR-LDB", breakIn.getWantedName());
  }

  private static String part(int step, int part) {
    List<SolveStep> steps = SolveStepsFormat.parse(STEPS);
    return SolveSolution.from(MOVES, steps, CubeMethod.BLIND).getSteps().get(step)
        .getPartMoves(part);
  }
}
