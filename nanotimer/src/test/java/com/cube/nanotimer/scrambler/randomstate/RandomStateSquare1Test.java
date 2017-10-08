package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.scrambler.randomstate.square1.RSSquare1Scrambler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

@RunWith(JUnit4.class)
public class RandomStateSquare1Test {

  @Test
  public void basicTest() {
    new RSSquare1Scrambler().genTables();
    RSSquare1Scrambler randomScrambler = new RSSquare1Scrambler();
    String[] scramble1 = randomScrambler.getNewScramble(null);

    // save to file
//    saveToFile(Square1Solver.shapes, "square1_shapes.dat");
//    saveToFile(Square1Solver.evenShapeDistance, "square1_even_shape_distance.dat");
//    saveToFile(Square1Solver.oddShapeDistance, "square1_odd_shape_distance.dat");
//    saveToFile(Square1Solver.cornersDistance, "square1_corners_distance.dat");
//    saveToFile(Square1Solver.edgesDistance, "square1_edges_distance.dat");

    System.out.println(/*Arrays.toString(scramble2) + "\n" +*/ Arrays.toString(scramble1));
  }

  private void saveToFile(Serializable serializable, String fileName) {
    try {
      FileOutputStream fos = new FileOutputStream(fileName);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(serializable);
      oos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private <T, U> void savePropertiesToFile(Map<T, U> map, String fileName) {
    Properties properties = new Properties();

    for (Map.Entry<T, U> entry : map.entrySet()) {
      properties.put(entry.getKey(), entry.getValue());
    }

    try {
      properties.store(new FileOutputStream(fileName), null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
