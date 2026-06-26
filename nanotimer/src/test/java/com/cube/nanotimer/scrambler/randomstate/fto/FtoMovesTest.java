package com.cube.nanotimer.scrambler.randomstate.fto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FtoMovesTest {

  private static boolean isPerm(int[] arr, int n) {
    if (arr.length != n) {
      return false;
    }
    boolean[] seen = new boolean[n];
    for (int v : arr) {
      if (v < 0 || v >= n || seen[v]) {
        return false;
      }
      seen[v] = true;
    }
    return true;
  }

  @Test
  public void moveCubeEntriesAreValidStates() {
    for (int i = 0; i < 24; i++) {
      FtoCubie m = FtoMoves.moveCube[i];
      assertTrue("cp perm @" + i, isPerm(m.cp, 6));
      assertTrue("ep perm @" + i, isPerm(m.ep, 12));
      assertTrue("uf perm @" + i, isPerm(m.uf, 12));
      assertTrue("rl perm @" + i, isPerm(m.rl, 12));
      for (int c : m.co) {
        assertTrue("co binary @" + i, c == 0 || c == 1);
      }
    }
  }

  @Test
  public void faceGeneratorsHaveOrderThree() {
    FtoCubie solved = new FtoCubie();
    for (int k = 0; k < 8; k++) {
      FtoCubie g = FtoMoves.moveCube[k * 2];
      assertTrue("generator != identity @" + k, !g.isEqual(solved));
      FtoCubie cubed = FtoCubie.mult(g, g, g, null);
      assertTrue("g^3 == identity @" + k, cubed.isEqual(solved));
      // The odd index is the square == inverse, so g * g^2 == identity.
      FtoCubie gTimesSquare = FtoCubie.mult(g, FtoMoves.moveCube[k * 2 + 1], null);
      assertTrue("g * g^2 == identity @" + k, gTimesSquare.isEqual(solved));
    }
  }

  @Test
  public void symmetryTablesAreConsistent() {
    for (int i = 0; i < 12; i++) {
      for (int j = 0; j < 12; j++) {
        int k = FtoMoves.symMult[i][j];
        assertTrue("symMult in range", k >= 0 && k < 12);
        assertEquals("symMulI inverts symMult", i, FtoMoves.symMulI[k][j]);
      }
    }
    for (int s = 0; s < 12; s++) {
      for (int j = 0; j < 8; j++) {
        int axis = FtoMoves.symMulM[s][j];
        assertTrue("conjugated axis is a face (0..7)", axis >= 0 && axis < 8);
      }
    }
  }
}
