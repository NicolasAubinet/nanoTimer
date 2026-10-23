package com.cube.nanotimer.cube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.SolveStep;
import org.junit.Test;

/**
 * A 3-style solve on a GAN i4 (reported 2026-10-23) whose first algorithm opens on an {@code S}
 * turned as {@code L R'} 23 ms apart. The core had rocked only ~48° by the slice's reading, so the
 * pair was not read as one, and the same reading 23 ms shorter made the {@code L} a wide: stored as
 * {@code r R'}, which is the slice, it was shown as {@code f F'}.
 */
public class WideIntoSliceCaptureTest {

  static final String SCRAMBLE =
      "B2 L2 B2 F2 D F2 R2 F2 R2 F' D R F' L D2 R D B' F' D2";

  static final String MOVES =
      "[y] y@17140 U'@17140 x@17369 L@17370 R'@17393 U'@17915 L@18144 y@18385 U@18385 L'@18831 "
      + "R@18831 x'@18832 z@19004 B'@19004 z'@19208 L'@19208 B@19401 z@19688 U@19688 U@20778 "
      + "D'@20825 y'@20826 y@20856 U@20856 y'@21033 L@21033 U'@21127 D@21208 y@21209 z'@21334 "
      + "F'@21334 z@21436 U'@21436 F@21521 D'@21648 U@21675 y'@21676 L'@21852 U'@22612 D@22641 "
      + "y@22642 L'@22964 R@22964 x'@22965 U@23228 B'@23340 F@23437 z'@23438 y'@23526 z@23526 "
      + "R'@23526 y@23635 x@23635 B@23635 R@23752 B@23902 F'@23909 z@23910 U'@24062 B'@24193 "
      + "R'@24882 L@24941 x@24942 F@25432 z'@25584 R'@25584 F'@25713 R'@25802 L@25802 x@25803 "
      + "D@25973 x@26036 R'@26037 R'@26148 D'@26285 y'@26454 x'@26454 x'@26454 L'@26454 R@26454 "
      + "F@26638 R'@26722 z'@26877 F'@26877 z@27597 U@27597 F'@27863 D'@27945 U@27956 y'@27957 "
      + "L@28119 U'@28222 U'@28431 L'@28502 U'@28692 D@28715 y@28716 y'@28831 F@28831 y@29089 "
      + "U@29089 B@29574 D@29782 U'@29794 y@29795 L'@29937 y@30010 U'@30011 L@30147 U@30236 "
      + "D'@30236 y'@30237 B'@30337 y'@30400 B'@30400 U@30579 D'@30591 y'@30592 U@30736 D'@30745 "
      + "y'@30746 F@30848 y'@30988 U@30989 F'@31081 U@31310 D'@31323 y'@31324 U@31454 D'@31477 "
      + "y'@31478 B@31948 y@33040 U@33040 B'@33161 D'@33335 B@33518 U'@33606 B'@33710 D@33853 "
      + "B@34011 U@34243 U@34355 B'@34443 D@34556 B@34716 U@34844 U@34963 B'@35087 D'@35332 "
      + "B@35464 B'@36319 B'@36456 D@36530 D@36657 B@36733 U@36853 B'@36972 D@37036 B@37147 "
      + "U'@37279 B'@37379 D@37489 B@37715 B@37937";

  static final String STEPS =
      "0:memo:17140:0 1:edges:3339:11469 1.0:UF-RD-DB:230:2318 1.1:UF-UL-FL:1090:1863 "
      + "1.2:UF-RF-RU:323:1977 1.3:UF-DF-DL:491:1445 1.4:UF-UB-RB:720:1492 "
      + "1.5:flip%3AUF-BL:485:2374 2:corners:2179:3810 2.0:UFR-RDF-UBR:1092:971 "
      + "2.1:UFR-LDF-UBL:232:1221 2.2:UFR-RDB-DBL:855:1618";

  @Test
  public void theOpeningSliceReadsAsOne() {
    assertEquals("y U' S R' B R S' R' B' R U", solution().getSteps().get(1).getPartMoves(0));
  }

  /** The slice turns the frame exactly as the wide did, so everything after it still solves. */
  @Test
  public void theSolveStillSolves() {
    StringBuilder all = new StringBuilder();
    for (SolveSolution.Step step : solution().getSteps()) {
      all.append(' ').append(step.getMoves().replace(SolveSolution.GROUP_SEPARATOR, " "));
    }
    assertTrue(all.toString(), DisplayedSolutionReplay.solves(SCRAMBLE, all.toString()));
  }

  private static SolveSolution solution() {
    return SolveSolution.from(MOVES, SolveStepsFormat.parse(STEPS), CubeMethod.BLIND);
  }
}
