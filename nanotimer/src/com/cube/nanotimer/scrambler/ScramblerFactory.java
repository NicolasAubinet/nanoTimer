package com.cube.nanotimer.scrambler;

import com.cube.nanotimer.scrambler.basic.ClockScrambler;
import com.cube.nanotimer.scrambler.basic.FiveScrambler;
import com.cube.nanotimer.scrambler.basic.FourScrambler;
import com.cube.nanotimer.scrambler.basic.MegaminxScrambler;
import com.cube.nanotimer.scrambler.basic.PyraminxScrambler;
import com.cube.nanotimer.scrambler.basic.SevenScrambler;
import com.cube.nanotimer.scrambler.basic.SixScrambler;
import com.cube.nanotimer.scrambler.basic.SkewbScrambler;
import com.cube.nanotimer.scrambler.basic.Square1Scrambler;
import com.cube.nanotimer.scrambler.basic.ThreeScrambler;
import com.cube.nanotimer.scrambler.basic.TwoScrambler;
import com.cube.nanotimer.vo.CubeType;

public class ScramblerFactory {

  public static Scrambler getScrambler(CubeType type) {
    Scrambler scrambler;
    switch (type) {
      case TWO_BY_TWO:
        scrambler = new TwoScrambler();
        break;
      case THREE_BY_THREE:
//        scrambler = new RSThreeScrambler();
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
