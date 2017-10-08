package com.cube.nanotimer.scrambler.randomstate;

public interface RSScrambler {

  String[] getNewScramble(ScrambleConfig config);
  void genTables();
  void stop();

}
