package com.cube.nanotimer.scrambler.randomstate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndexConvertor {

  public static void main(String[] args) {
    System.out.println("Corners orientation");
    byte[] state = new byte[] { 0, 0, 0, 0, 0, 0, 0 };
    System.out.println("orient: " + packCornerOrientation(state) + " |\t" + Arrays.toString(unpackCornerOrientation(packCornerOrientation(state))));
    state = new byte[] { 0, 0, 0, 0, 0, 1, 0 };
    System.out.println("orient: " + packCornerOrientation(state) + " |\t" + Arrays.toString(unpackCornerOrientation(packCornerOrientation(state))));
    state = new byte[] { 1, 1, 1, 1, 1, 1, 1 };
    System.out.println("orient: " + packCornerOrientation(state) + " |\t" + Arrays.toString(unpackCornerOrientation(packCornerOrientation(state))));
    state = new byte[] { 2, 2, 2, 2, 2, 2, 2 };
    System.out.println("orient: " + packCornerOrientation(state) + " |\t" + Arrays.toString(unpackCornerOrientation(packCornerOrientation(state))));

    System.out.println("Edges orientation");
    state = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    System.out.println("orient: " + packEdgeOrientation(state) + " |\t" + Arrays.toString(unpackEdgeOrientation(packEdgeOrientation(state))));
    state = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
    System.out.println("orient: " + packEdgeOrientation(state) + " |\t" + Arrays.toString(unpackEdgeOrientation(packEdgeOrientation(state))));
    state = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    System.out.println("orient: " + packEdgeOrientation(state) + " |\t" + Arrays.toString(unpackEdgeOrientation(packEdgeOrientation(state))));
    state = new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
    System.out.println("orient: " + packEdgeOrientation(state) + " |\t" + Arrays.toString(unpackEdgeOrientation(packEdgeOrientation(state))));

    System.out.println("Corner permutation");
    state = new byte[] { 0, 1, 2, 3, 4, 5, 6 };
    System.out.println("perm: " + Arrays.toString(state) + "\t" + packPermutation(state) + " |\t" + Arrays.toString(unpackCornerPermutation(packPermutation(state))));
    state = new byte[] { 0, 1, 2, 3, 4, 5, 7 };
    System.out.println("perm: " + Arrays.toString(state) + "\t" + packPermutation(state) + " |\t" + Arrays.toString(unpackCornerPermutation(packPermutation(state))));
    state = new byte[] { 0, 1, 2, 3, 4, 6, 5 };
    System.out.println("perm: " + Arrays.toString(state) + "\t" + packPermutation(state) + " |\t" + Arrays.toString(unpackCornerPermutation(packPermutation(state))));
    state = new byte[] { 0, 1, 2, 3, 5, 6, 4 };
    System.out.println("perm: " + Arrays.toString(state) + "\t" + packPermutation(state) + " |\t" + Arrays.toString(unpackCornerPermutation(packPermutation(state))));
    state = new byte[] { 7, 6, 5, 4, 3, 1, 2 };
    System.out.println("perm: " + Arrays.toString(state) + "\t" + packPermutation(state) + " |\t" + Arrays.toString(unpackCornerPermutation(packPermutation(state))));
    state = new byte[] { 7, 6, 5, 4, 3, 2, 0 };
    System.out.println("perm: " + Arrays.toString(state) + "\t" + packPermutation(state) + " |\t" + Arrays.toString(unpackCornerPermutation(packPermutation(state))));
    state = new byte[] { 7, 6, 5, 4, 3, 2, 1 };
    System.out.println("perm: " + Arrays.toString(state) + "\t" + packPermutation(state) + " |\t" + Arrays.toString(unpackCornerPermutation(packPermutation(state))));
    state = new byte[] { 5, 3, 0, 1, 6, 4, 7 };
    System.out.println("perm: " + Arrays.toString(state) + "\t" + packPermutation(state) + " |\t" + Arrays.toString(unpackCornerPermutation(packPermutation(state))));
    state = new byte[] { 4, 3, 0, 1, 7, 5, 6 };
    System.out.println("perm: " + Arrays.toString(state) + "\t" + packPermutation(state) + " |\t" + Arrays.toString(unpackCornerPermutation(packPermutation(state))));
    state = new byte[] { 3, 0, 2, 6, 1, 7, 5 };
    System.out.println("perm: " + Arrays.toString(state) + "\t" + packPermutation(state) + " |\t" + Arrays.toString(unpackCornerPermutation(packPermutation(state))));
  }

  public static int packCornerPermutation(byte[] perm) {
    return packPermutation(perm);
  }

  public static int packCornerOrientation(byte[] perm) {
    return packOrientation(perm, (byte) 3);
  }

  public static int packEdgePermutation(byte[] perm) {
    return packPermutation(perm);
  }

  public static int packEdgeOrientation(byte[] perm) {
    return packOrientation(perm, (byte) 2);
  }


  public static byte[] unpackCornerPermutation(int permInd) {
    return unpackPermutation(permInd, (byte) 7);
  }

  public static byte[] unpackCornerOrientation(int permInd) {
    return unpackOrientation(permInd, (byte) 3, (byte) 7);
  }

  public static byte[] unpackEdgePermutation(int permInd) {
    return unpackPermutation(permInd, (byte) 11);
  }

  public static byte[] unpackEdgeOrientation(int permInd) {
    return unpackOrientation(permInd, (byte) 2, (byte) 11);
  }


  public static int packPermutation(byte[] perm) {
    int nDifferentValues = perm.length + 1; // +1 because the perm array is missing the last element
    int permInd = 0;
    for (int i = 0; i < perm.length; i++) {
      int curInd = perm[i];
      for (int j = 0; j < i; j++) {
        if (perm[j] < perm[i]) {
          curInd--;
        }
      }
      permInd = permInd * (nDifferentValues - i) + curInd;
    }
    return permInd;
  }

  public static int packOrientation(byte[] orient, byte nDifferentValues) {
    int orientInd = 0;
    for (byte p : orient) {
      orientInd = orientInd * nDifferentValues + p;
    }
    return orientInd;
  }

  public static byte[] unpackPermutation(int permInd, byte length) {
    int nDifferentValues = length + 1; // +1 because the perm array is missing the last element
    byte[] perm = new byte[length];
    // TODO : could maybe merge the loops for improved performance
    for (int i = perm.length - 1; i >= 0; i--) {
      perm[i] = (byte) (permInd % (nDifferentValues - i));
      permInd /= (nDifferentValues - i);
    }
//    System.out.println("      " + Arrays.toString(perm));
    List<Byte> available = new ArrayList<Byte>();
    for (byte i = 0; i < nDifferentValues; i++) {
      available.add(i);
    }
    for (int i = 0; i < perm.length; i++) {
      perm[i] = available.remove(perm[i]);
    }
    return perm;
  }

  public static byte[] unpackOrientation(int orientInd, byte nDifferentValues, byte length) {
    byte[] orient = new byte[length];
    for (int i = orient.length - 1; i >= 0; i--) {
      orient[i] = (byte) (orientInd % nDifferentValues);
      orientInd /= nDifferentValues;
    }
    return orient;
  }

}
