package com.cube.nanotimer.scrambler.randomstate;

import android.content.Context;

public interface RSScrambler {

  String[] getNewScramble(ScrambleConfig config);
  void prepareGenTables(Context context);
  void genTables();
  void stop();

}
