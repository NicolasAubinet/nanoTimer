package com.cube.nanotimer.smartcube.drill;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class AlgorithmTimesTest {

  private static final String A = "F R' F' R U R U' R'";
  private static final String B = "F R U' R' U' R U R' F'";
  private static final String C = "y F' r U r' U' r' F r";

  @Test
  public void keepsTheLastFiveAndAveragesThem() {
    AlgorithmTimes times = AlgorithmTimes.parse("oll_37", null);
    for (long ms = 1000; ms <= 1600; ms += 100) {
      times.add(A, ms);
    }
    assertEquals(Arrays.asList(1200L, 1300L, 1400L, 1500L, 1600L), times.get(A));
    assertEquals(1400, times.mean(A));
  }

  @Test
  public void averagesWhatItHasBeforeFive() {
    AlgorithmTimes times = AlgorithmTimes.parse("oll_37", null);
    assertEquals(-1, times.mean(A));
    times.add(A, 1000);
    times.add(A, 1501);
    assertEquals(1251, times.mean(A));
  }

  @Test
  public void twoSpellingsOfOneTurningShareTheirTimes() {
    AlgorithmTimes times = AlgorithmTimes.parse("oll_37", null);
    times.add(C, 1000);
    times.add("F' r U r' U' r' F r", 2000);
    assertEquals(Arrays.asList(1000L, 2000L), times.get(C));
    assertEquals(Collections.emptyList(), times.get(A));
  }

  @Test
  public void survivesBeingStoredAndReadBack() {
    AlgorithmTimes times = AlgorithmTimes.parse("oll_37", null);
    times.add(A, 1234);
    times.add(A, 1300);
    times.add(C, 999);
    AlgorithmTimes read = AlgorithmTimes.parse("oll_37", times.toString());
    assertEquals(Arrays.asList(1234L, 1300L), read.get(A));
    assertEquals(Collections.singletonList(999L), read.get(C));
  }

  @Test
  public void namesTheFastestOnlyAmongFullAveragesAndOnlyWithTwo() {
    AlgorithmTimes times = AlgorithmTimes.parse("oll_37", null);
    for (int i = 0; i < 5; i++) {
      times.add(A, 1500);
    }
    times.add(C, 900); // faster, but only twice
    times.add(C, 900);
    assertNull("one full average has nothing to beat", times.fastest(Arrays.asList(A, B, C)));
    for (int i = 0; i < 5; i++) {
      times.add(B, 1300);
    }
    assertEquals(B, times.fastest(Arrays.asList(A, B)));
    times.add(C, 900);
    times.add(C, 900);
    times.add(C, 900);
    assertEquals(C, times.fastest(Arrays.asList(A, B, C)));
  }
}
