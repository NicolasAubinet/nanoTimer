package com.cube.nanotimer.smartcube.drill;

import static org.junit.Assert.assertEquals;

import com.cube.nanotimer.session.MethodStatistics;
import com.cube.nanotimer.smartcube.step.F2LPairScrambles;
import com.cube.nanotimer.vo.StepStats;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class PairWeightsTest {

  private static final List<String> CASES = Arrays.asList("pair_a1", "pair_a25", "pair_a12");

  @Test
  public void withNoSolvesACaseIsDealtByHowOftenItComesUp() {
    Map<String, Long> weights = PairWeights.of(CASES, null);
    for (String code : CASES) {
      assertEquals(code, F2LPairScrambles.placements(code), weights.get(code).longValue());
    }
  }

  /** Over the basic mean leans the draw; under it, or never met, is frequency alone. */
  @Test
  public void aCaseRecentlyOverTheBasicMeanIsDealtMoreOften() {
    MethodStatistics statistics = new MethodStatistics(Collections.<StepStats>emptyList(),
        Arrays.asList(stats("pair_3_fr", 10, 1000), stats("pair_7_bl", 10, 1400),
            stats("pair_a1_fr", 1, 2500), stats("pair_a25_fl", 2, 900)), 30);
    assertEquals(Long.valueOf(1200), PairWeights.basicMean(statistics.getCases("pair")));

    Map<String, Long> weights = PairWeights.of(CASES, statistics);
    assertEquals(PairWeights.LEAN * F2LPairScrambles.placements("pair_a1"),
        weights.get("pair_a1").longValue());
    assertEquals(F2LPairScrambles.placements("pair_a25"), weights.get("pair_a25").longValue());
    assertEquals(F2LPairScrambles.placements("pair_a12"), weights.get("pair_a12").longValue());
  }

  private static StepStats stats(String code, int count, long meanMs) {
    return new StepStats(code, count, meanMs * count, 0, meanMs, meanMs, 0);
  }
}
