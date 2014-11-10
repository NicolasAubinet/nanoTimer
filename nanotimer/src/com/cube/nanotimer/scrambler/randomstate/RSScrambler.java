package com.cube.nanotimer.scrambler.randomstate;

public interface RSScrambler {

  String[] getNewScramble(ScrambleConfig config);
  void freeMemory();
  void genTables();
  void stop();

}
