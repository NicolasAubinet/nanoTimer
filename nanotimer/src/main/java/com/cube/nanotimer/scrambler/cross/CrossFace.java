package com.cube.nanotimer.scrambler.cross;

import com.cube.nanotimer.scrambler.randomstate.Move;

/**
 * The six faces a cross can be solved on. Each face is defined by its quarter turn (the 4 edges it
 * moves are that face's cross edges) and knows its opposite (for the Dual neutrality mode).
 *
 * <p>D is the conventional CFOP cross (bottom).
 */
public enum CrossFace {
  U(Move.U),
  D(Move.D),
  R(Move.R),
  L(Move.L),
  F(Move.F),
  B(Move.B);

  private final Move quarterTurn;

  CrossFace(Move quarterTurn) {
    this.quarterTurn = quarterTurn;
  }

  /** The quarter turn of this face, used to derive its 4 cross edges. */
  public Move getQuarterTurn() {
    return quarterTurn;
  }

  /** The home indices of this face's 4 cross edges. */
  public byte[] getCrossPieces() {
    return CrossSolver.facePieces(quarterTurn);
  }

  /** The opposite face (U/D, R/L, F/B). */
  public CrossFace opposite() {
    switch (this) {
      case U: return D;
      case D: return U;
      case R: return L;
      case L: return R;
      case F: return B;
      case B: return F;
      default: throw new IllegalStateException();
    }
  }
}
