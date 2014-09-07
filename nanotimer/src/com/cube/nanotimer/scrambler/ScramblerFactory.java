package com.cube.nanotimer.scrambler;

import com.cube.nanotimer.scrambler.impl.ClockScrambler;
import com.cube.nanotimer.scrambler.impl.FiveScrambler;
import com.cube.nanotimer.scrambler.impl.FourScrambler;
import com.cube.nanotimer.scrambler.impl.MegaminxScrambler;
import com.cube.nanotimer.scrambler.impl.PyraminxScrambler;
import com.cube.nanotimer.scrambler.impl.SevenScrambler;
import com.cube.nanotimer.scrambler.impl.SixScrambler;
import com.cube.nanotimer.scrambler.impl.SkewbScrambler;
import com.cube.nanotimer.scrambler.impl.Square1Scrambler;
import com.cube.nanotimer.scrambler.impl.ThreeScrambler;
import com.cube.nanotimer.scrambler.impl.TwoScrambler;
import com.cube.nanotimer.vo.CubeType;

public class ScramblerFactory {

  public static Scrambler getScrambler(CubeType type) {
    Scrambler scrambler;
    switch (type.getType()) {
      case TWO_BY_TWO:
        scrambler = new TwoScrambler();
        break;
      case THREE_BY_THREE:
        scrambler = new ThreeScrambler();
        break;
      case FOUR_BY_FOUR:
        scrambler = new FourScrambler();
        break;
      case FIVE_BY_FIVE:
        scrambler = new FiveScrambler();
        break;
      case SIX_BY_SIX:
        scrambler = new SixScrambler();
        break;
      case SEVEN_BY_SEVEN:
        scrambler = new SevenScrambler();
        break;
      case MEGAMINX:
        scrambler = new MegaminxScrambler();
        break;
      case PYRAMINX:
        scrambler = new PyraminxScrambler();
        break;
      case SKEWB:
        scrambler = new SkewbScrambler();
        break;
      case SQUARE1:
        scrambler = new Square1Scrambler();
        break;
      case CLOCK:
        scrambler = new ClockScrambler();
        break;
      default:
        throw new RuntimeException("Could not find timer type " + type.getName());
    }
    return scrambler;
  }

}
