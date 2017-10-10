package com.cube.nanotimer.scrambler.randomstate.square1;

import android.content.Context;
import android.content.res.Resources;
import com.cube.nanotimer.R;
import com.cube.nanotimer.scrambler.randomstate.RSScrambler;
import com.cube.nanotimer.scrambler.randomstate.ScrambleConfig;
import com.cube.nanotimer.util.helper.FileUtils;
import com.cube.nanotimer.util.helper.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.DataFormatException;

public class RSSquare1Scrambler implements RSScrambler {
  private Square1Solver solver = new Square1Solver();

  public RSSquare1Scrambler() {
  }

  @Override
  public String[] getNewScramble(ScrambleConfig config) {
    Square1State randomState = solver.getRandomState(new Random());
    return solver.generate(randomState);
  }

  @Override
  public void prepareGenTables(Context context) {
    if (Square1Solver.isInitialised()) {
      return;
    }

    // load and decompress the tables from file to save on generation that takes up to 3 minutes on some devices
    // (these files were encoded via a unit test class with methods from FileUtils.compressToGzip*)
    try {
      Resources resources = context.getResources();

      InputStream is = resources.openRawResource(R.raw.square1_shapes);
      ArrayList<Square1State> shapes = (ArrayList<Square1State>) FileUtils.loadCompressedGzipSerializable(is);
      Square1Solver.setShapes(shapes);

      is = resources.openRawResource(R.raw.square1_even_shape_distance);
      HashMap<Integer, Integer> evenShapeDistance = (HashMap<Integer, Integer>) FileUtils.loadCompressedGzipSerializable(is);
      Square1Solver.setEvenShapeDistance(evenShapeDistance);

      is = resources.openRawResource(R.raw.square1_odd_shape_distance);
      HashMap<Integer, Integer> oddShapeDistance = (HashMap<Integer, Integer>) FileUtils.loadCompressedGzipSerializable(is);
      Square1Solver.setOddShapeDistance(oddShapeDistance);

      is = resources.openRawResource(R.raw.square1_corners_distance);
      byte[] byteArray = FileUtils.loadCompressedGzip(is, Square1Solver.N_CORNERS_PERMUTATIONS*Square1Solver.N_EDGES_COMBINATIONS);
      byte[][] cornersDistance = Utils.toTwoDimensionalByteArray(byteArray, Square1Solver.N_CORNERS_PERMUTATIONS);
      Square1Solver.setCornersDistance(cornersDistance);

      is = resources.openRawResource(R.raw.square1_edges_distance);
      byteArray = FileUtils.loadCompressedGzip(is, Square1Solver.N_EDGES_PERMUTATIONS*Square1Solver.N_CORNERS_COMBINATIONS);
      byte[][] edgesDistance = Utils.toTwoDimensionalByteArray(byteArray, Square1Solver.N_EDGES_PERMUTATIONS);
      Square1Solver.setEdgesDistance(edgesDistance);
    } catch (ClassNotFoundException | DataFormatException | IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void genTables() {
    Square1Solver.genTables();
  }

  @Override
  public void stop() {
    solver.stop();
  }
}
