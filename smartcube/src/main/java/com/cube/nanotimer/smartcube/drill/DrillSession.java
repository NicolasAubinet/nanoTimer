package com.cube.nanotimer.smartcube.drill;

import com.cube.nanotimer.drill.DrillSpec;

import com.cube.nanotimer.smartcube.cube.CubeTwin;
import com.cube.nanotimer.smartcube.cube.CubieCube;
import com.cube.nanotimer.smartcube.model.CubeMove;
import com.cube.nanotimer.smartcube.model.CubeState;
import com.cube.nanotimer.smartcube.model.Face;
import com.cube.nanotimer.smartcube.step.F2LPairScrambles;
import com.cube.nanotimer.smartcube.step.FaceTurns;
import com.cube.nanotimer.smartcube.step.LastLayerScrambles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A drill being run: the virtual cube holds the case, the smart cube is only the input, and the
 * user's own cube ends up scrambled, which is expected and needs nothing done about it.
 *
 * <p><b>Why this needs nothing from the gyro.</b> A cube reports its turns against its own centres,
 * so feeding them straight to a virtual cube keeps that cube equal to the physical one whatever the
 * user does with it. Turning the whole cube round reports nothing and changes nothing, and the next
 * turn arrives as whichever face it physically was. A slice is the one move that carries the centres
 * with it, and it is still no exception: {@code M} arriving as {@code R} and {@code L'} is not a
 * misreading but the truth relative to the centres that moved. Solved relative to your own centres
 * is what solved means, so {@link CubieCube#isSolved} is the whole of the check and the frame never
 * comes into it. What the frame is needed for is naming <em>where</em> in a solve something happened,
 * and a drill never asks.
 *
 * <p>What that does leave is the user's grip, and it is a matter of what is <em>shown</em>, never of
 * what is checked here: a case drawn on a cube standing square while the user holds theirs some
 * other way has them turning the face they meant on a face they cannot see. The screen answers it by
 * following the grip off the gyro; nothing in this class needs to know either way.
 */
public final class DrillSession {

  private static final long NOT_SHOWN = -1;

  /** Clean reps a learning drill turns with the algorithm on screen before it fades. */
  public static final int GUIDED_REPS = 2;

  /** Where the faces sit in a facelet string, which is the order {@link CubieCube} writes one in. */
  private static final String FACELET_ORDER = "URFDLB";

  /** The cross and the four slots with the cross on D, each slot as its corner and its edge. */
  private static final String[] CROSS_EDGES = {"DF", "DR", "DB", "DL"};
  private static final String[][] SLOTS = {{"DFR", "FR"}, {"DLF", "FL"}, {"DBL", "BL"},
      {"DRB", "BR"}};

  private final DrillSpec spec;
  private final Random random;
  private final Map<String, Long> weights;
  /** The face the last layer is dealt onto, which is the colour the user solves it on. */
  private final String layerFace;
  private final List<String> cases = new ArrayList<String>();
  private final List<String> unknownCases = new ArrayList<String>();
  private final List<DrillRep> reps = new ArrayList<DrillRep>();
  private final List<String> pass = new ArrayList<String>();

  /** An F2L pair drill, whose case is a pair and whose rep ends when that pair is in. */
  private final boolean pairs;
  /** The current pair's corner and edge by the faces of their slot, as dealt. */
  private String pairCorner;
  private String pairEdge;
  private int[] shownEdges;
  private int[] shownCorners;

  private CubieCube cube = new CubieCube();
  /** The user's own cube, followed only so that a turn it never reported can be recovered. */
  private final CubeTwin twin = new CubeTwin();
  private String currentCase;
  private String currentScramble;
  private final List<CubeMove> moves = new ArrayList<CubeMove>();
  private long firstMoveMs;
  /** The first turn that was not an AUF, which is where the algorithm really started. */
  private long algStartMs;
  private long lastMoveMs;
  /** Host wall-clock, converted to the cube's clock when it is read. See {@link #markCaseShown}. */
  private long caseShownAtMs = NOT_SHOWN;
  /** Host clock minus cube clock, from the last move that carried both. */
  private long clockSkewMs;
  private int resetCount;
  private boolean revealed;
  /** Whether the case up is shown with its algorithm, decided when it is dealt. */
  private boolean guided;
  /** The case being practised, dealt over and over outside the drill's reps. */
  private String practiceCase;

  public DrillSession(DrillSpec spec, Random random) {
    this(spec, random, null, "U");
  }

  public DrillSession(DrillSpec spec, Random random, Map<String, Long> weights) {
    this(spec, random, weights, "U");
  }

  /**
   * @param weights what each case is costing, for a spec drawing by weight. A case with no weight
   *     still comes up, since nothing known about it is not a reason to never practise it.
   * @param layerFace the face to deal the last layer onto. Not the sender's business and not on the
   *     spec: which colour a solver finishes on is theirs, and a drill prescribed from outside has
   *     no way of knowing it.
   */
  public DrillSession(DrillSpec spec, Random random, Map<String, Long> weights, String layerFace) {
    if (spec.getType() == DrillSpec.Type.CROSS) {
      throw new IllegalArgumentException("A cross drill is run by CrossDrillSession");
    }
    this.spec = spec;
    this.random = random;
    this.weights = weights;
    this.layerFace = layerFace == null ? "U" : layerFace;
    this.pairs = spec.getType() == DrillSpec.Type.F2L_PAIR;
    List<String> known = pairs ? F2LPairScrambles.cases() : LastLayerScrambles.cases();
    for (String code : spec.getCases()) {
      (known.contains(code) ? cases : unknownCases).add(code);
    }
    if (spec.getWorkingSet() > 0 && weights != null) {
      // Heaviest first, so the working set opens on what is costing most; ties keep the spec's order.
      Collections.sort(cases, new Comparator<String>() {
        @Override
        public int compare(String a, String b) {
          return Long.compare(weightOf(b), weightOf(a));
        }
      });
    }
  }

  public DrillSpec getSpec() {
    return spec;
  }

  /** The cases asked for that this app has no scramble for, dropped rather than run wrong. */
  public List<String> getUnknownCases() {
    return Collections.unmodifiableList(unknownCases);
  }

  /** False when every case asked for was dropped, which leaves nothing to run. */
  public boolean isRunnable() {
    return !cases.isEmpty();
  }

  /**
   * Puts the next case on the virtual cube, or returns false when the drill is over. The rep does
   * not start here: it starts when the case reaches the user's eyes, which only the caller knows
   * and says with {@link #markCaseShown}.
   */
  public boolean nextRep() {
    if (practiceCase != null) {
      deal(practiceCase);
      return true;
    }
    if (isFinished()) {
      currentCase = null;
      return false;
    }
    deal(pick());
    return true;
  }

  /**
   * Practises one case: it is dealt again each time it is solved, until {@link #endPractice}. A
   * case already up and not yet turned is put back rather than spent, since leaving it was not an
   * answer about it.
   */
  public void startPractice(String code) {
    if (currentCase != null && moves.isEmpty() && practiceCase == null
        && spec.getSelection() != DrillSpec.Selection.WEIGHTED) {
      pass.add(currentCase); // the end of the pass is what is dealt next
    }
    practiceCase = code;
    deal(code);
  }

  /** Back to the drill, whose next case the caller deals with {@link #nextRep}. */
  public void endPractice() {
    practiceCase = null;
    currentCase = null;
    caseShownAtMs = NOT_SHOWN;
  }

  /** The case being practised, or null while the drill is running. */
  public String getPracticeCase() {
    return practiceCase;
  }

  private void deal(String code) {
    currentCase = code;
    if (pairs) {
      // A random slot, so the pair is not always read in the same place. Practice keeps front right,
      // where the algorithm on screen reads as written.
      int spins = practiceCase != null ? 0 : random.nextInt(4);
      currentScramble = LayerRotation.toFace(
          LayerRotation.spun(F2LPairScrambles.forCase(currentCase, random), spins), layerFace);
      pairCorner = LayerRotation.piece("DFR", spins, layerFace);
      pairEdge = LayerRotation.piece("FR", spins, layerFace);
    } else {
      currentScramble = LayerRotation.toFace(
          LastLayerScrambles.forCase(currentCase, random), layerFace);
    }
    cube = new CubieCube();
    FaceTurns.apply(cube, currentScramble);
    if (pairs) {
      readShownPieces();
    }
    moves.clear();
    firstMoveMs = 0;
    algStartMs = 0;
    lastMoveMs = 0;
    caseShownAtMs = NOT_SHOWN;
    resetCount = 0;
    revealed = false;
    guided = spec.isLearning() && practiceCase == null
        && getTally().getGuided(code) < GUIDED_REPS;
  }

  /** Whether the case up is a learning drill's rep turned with its algorithm on screen. */
  public boolean isGuided() {
    return guided && currentCase != null;
  }

  /**
   * The case is now in front of the user, as of a host wall-clock timestamp. This is where
   * recognition runs from, and until it is called the rep has not begun and turns are not counted
   * against it.
   *
   * <p><b>The two clocks are not the same one.</b> A move is stamped on the cube's own clock, which
   * is fitted to host time when the cube connects and only re-fitted once it has drifted seconds;
   * subtracting one from the other took whatever the fit was out by out of the user's recognition
   * and gave it to their execution, which is how a rep came back as 0.00 to recognise and four
   * seconds to turn. The moves carry both stamps often enough to keep the difference, so this one
   * is converted rather than compared.
   *
   * <p>Set apart from {@link #nextRep} because the two are not the same moment. A case is chosen
   * before it can be drawn, and a screen may hold the one just finished for a beat so that solving
   * it reads as an ending rather than as a jump; measuring from the previous rep's last move, which
   * is what this used to do, charged every one of those beats to the user's recognition.
   */
  public void markCaseShown(long hostMs) {
    caseShownAtMs = hostMs;
    // Turns count from here, so what the cube did before this is not something to recover: the
    // caller says where it stands now, and anything it misses from now on is measured against that.
    twin.reset();
  }

  /**
   * The user looked up the algorithm for the case in front of them. Nothing about the rep changes:
   * it is still theirs to finish and still timed, since stopping the clock would only teach them to
   * look things up. What it costs is the claim that the time says they know the case, and the rep
   * carries that so nothing reading the drill back has to guess.
   */
  public void markRevealed() {
    if (currentCase != null) {
      revealed = true;
    }
  }

  /**
   * Puts the case back as it was and starts the rep over, for one botched by a slip rather than by
   * not knowing it. Costs no rep: the case comes round again here and now, where skipping it would
   * spend it and move on.
   *
   * <p>The same scramble, not another of the same case: a redo is of the position that went wrong,
   * and a fresh alignment would be a different one to look at. The rep it eventually finishes
   * carries how many times it was restarted, and is no measurement of the case: recognition is
   * timed again off a position the user has already read and begun on, which is a couple of tenths
   * whatever they know. The figures leave such a rep out, and {@link #showAgain} deals the case once
   * more so the drill is not left with nothing for it.
   *
   * <p>Like a case just dealt, the rep is not running until {@link #markCaseShown} says the user can
   * see it again.
   */
  public void resetRep() {
    if (currentCase == null) {
      return;
    }
    cube = new CubieCube();
    FaceTurns.apply(cube, currentScramble);
    moves.clear();
    firstMoveMs = 0;
    algStartMs = 0;
    lastMoveMs = 0;
    caseShownAtMs = NOT_SHOWN;
    resetCount++;
  }

  /**
   * Feeds one turn to the virtual cube. Turns made before the case was shown are dropped rather
   * than queued: the cube on screen missed them too, so the two stay in step, and a rep cannot be
   * timed against a case the user could not yet see.
   *
   * @return the finished rep if that turn solved the case, null while it is still going
   */
  public DrillRep onMove(CubeMove move) {
    // Before the drop: the twin follows the cube itself, so a turn this rep has no use for is
    // still not one the cube is later found to have "missed".
    twin.onMove(move);
    if (currentCase == null || caseShownAtMs == NOT_SHOWN) {
      return null;
    }
    if (move.getHostTimestampMs() != null) {
      clockSkewMs = move.getHostTimestampMs() - move.getCubeTimestampMs();
    }
    long at = move.getCubeTimestampMs();
    if (at < caseShownOnCubeClock()) {
      // Turned before this case was up and delivered late: the cube still has to have it, or the
      // two fall out of step, but it is the previous case's turn and cannot start this rep, and it
      // is not one of the turns the rep is made of.
      cube.applyMove(move.getFace(), move.isPrime());
      return isCaseDone() ? complete(false) : null;
    }
    if (moves.isEmpty()) {
      firstMoveMs = at;
    }
    if (algStartMs == 0 && move.getFace() != Face.valueOf(layerFace)) {
      algStartMs = at;
    }
    moves.add(move);
    lastMoveMs = at;
    cube.applyMove(move.getFace(), move.isPrime());
    return isCaseDone() ? complete(false) : null;
  }

  /**
   * The state the cube says it is really in, which is the truth wherever the two disagree.
   *
   * <p>Reported turns are the fast path and not a complete one: a QiYi drops a state change during
   * fast slices, and one turn lost that way leaves the drawn cube wrong for the rest of the rep,
   * with the case never reading as done however well it was solved. The turns nobody saw are handed
   * to the case here instead, which needs no notation for them and no idea of which they were.
   *
   * <p>Only while a case is in front of the user: a turn made before it was shown is dropped by
   * {@link #onMove}, and one recovered here has to be dropped the same way or the two cubes part
   * company over it.
   *
   * <p>What the rep cannot have is the turns themselves. A recovered one has no notation and no
   * timestamp, so it counts towards nothing and the rep it ends is timed to the last turn that was
   * really reported. A rep a turn short is a smaller price than one that could not end at all.
   *
   * @return what that cost, which is nothing at all unless something really was missed
   */
  public Correction onState(CubeState state) {
    CubieCube missed = twin.missing(state);
    if (missed == null || currentCase == null || caseShownAtMs == NOT_SHOWN) {
      return Correction.NONE;
    }
    cube = cube.multiply(missed);
    return new Correction(true, isCaseDone() ? complete(false) : null);
  }

  /**
   * What a reported state put right. Both halves can land at once: the drawn cube is behind the
   * one in the user's hands, and the turns it was behind by are the ones that finished the case.
   */
  public static final class Correction {

    private static final Correction NONE = new Correction(false, null);

    private final boolean repaired;
    private final DrillRep rep;

    private Correction(boolean repaired, DrillRep rep) {
      this.repaired = repaired;
      this.rep = rep;
    }

    /** Whether the case has moved under the drawing, and so has to be drawn again. */
    public boolean isRepaired() {
      return repaired;
    }

    /** The rep the missed turns finished, or null if the case is still going. */
    public DrillRep getRep() {
      return rep;
    }
  }

  /**
   * <b>An OLL is done when the layer is one colour, not when the cube is solved.</b> Orienting is
   * all an OLL claims to do, and every good algorithm for a case leaves the layer permuted its own
   * way; a scramble here is one particular algorithm inverted, so ending on a solved cube would ask
   * the user to reproduce that algorithm rather than the one they know, and any other correct
   * algorithm would orient the face and then never finish. A PLL restores the permutation, so for
   * one of those a solved cube is exactly the question.
   */
  private boolean isCaseDone() {
    if (pairs) {
      return F2LPairScrambles.isPairIn(cube.toFaceCube(),
          LayerRotation.piece("D", 0, layerFace).charAt(0), pairCorner, pairEdge);
    }
    if (currentCase != null && currentCase.startsWith("oll_")) {
      return isLayerOriented();
    }
    return cube.isSolved();
  }

  /**
   * The pieces a pair's rep is drawn with: the cross, the slots already in, and the pair, wherever
   * its pieces are. The rest is grey, so the last layer says nothing and the slot to fill reads as
   * the one hole; a stuck case shows the slot its piece is trapped in grey around that piece.
   */
  private void readShownPieces() {
    int[] cp = new int[8];
    int[] co = new int[8];
    int[] ep = new int[12];
    int[] eo = new int[12];
    cube.toPermutation(cp, co, ep, eo);
    List<Integer> edges = new ArrayList<Integer>();
    List<Integer> corners = new ArrayList<Integer>();
    for (String cross : CROSS_EDGES) {
      edges.add(F2LPairScrambles.edgeIndex(LayerRotation.piece(cross, 0, layerFace)));
    }
    for (String[] slot : SLOTS) {
      int corner = F2LPairScrambles.cornerIndex(LayerRotation.piece(slot[0], 0, layerFace));
      int edge = F2LPairScrambles.edgeIndex(LayerRotation.piece(slot[1], 0, layerFace));
      if (cp[corner] == corner && co[corner] == 0 && ep[edge] == edge && eo[edge] == 0) {
        corners.add(corner);
        edges.add(edge);
      }
    }
    corners.add(F2LPairScrambles.cornerIndex(pairCorner));
    edges.add(F2LPairScrambles.edgeIndex(pairEdge));
    shownEdges = toArray(edges);
    shownCorners = toArray(corners);
  }

  private static int[] toArray(List<Integer> values) {
    int[] array = new int[values.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = values.get(i);
    }
    return array;
  }

  /** The edges a pair's rep colours, by the slot each belongs in. Null for any other drill. */
  public int[] getShownEdges() {
    return shownEdges == null ? null : shownEdges.clone();
  }

  /** The corners a pair's rep colours, likewise. */
  public int[] getShownCorners() {
    return shownCorners == null ? null : shownCorners.clone();
  }

  /** Whether the nine stickers of the layer's face all show the same colour. */
  private boolean isLayerOriented() {
    String facelets = cube.toFaceCube();
    int start = FACELET_ORDER.indexOf(layerFace) * 9;
    for (int i = 1; i < 9; i++) {
      if (facelets.charAt(start + i) != facelets.charAt(start)) {
        return false;
      }
    }
    return true;
  }

  /** Gives up on the case in front of the user, spending the rep. Null when there is none. */
  public DrillRep abandon() {
    return currentCase == null ? null : complete(true);
  }

  /**
   * A turn of the layer's own face before the algorithm only squared the case up to be read, so it
   * is looking rather than solving; the one that closes an algorithm falls inside the execution by
   * construction. A rep of nothing but those has no algorithm to have started, and is split at its
   * first turn as before.
   */
  private DrillRep complete(boolean abandoned) {
    long algStart = algStartMs > 0 ? algStartMs : firstMoveMs;
    // On the cube's clock, not the host's: the turns are stamped on that one, and the offsets the
    // rep is stored with are measured from this.
    long shownAt = caseShownOnCubeClock();
    long recognition = moves.isEmpty() ? 0 : Math.max(0, algStart - shownAt);
    long execution = moves.isEmpty() ? 0 : Math.max(0, lastMoveMs - algStart);
    boolean practice = practiceCase != null;
    DrillRep rep = new DrillRep(currentCase, currentScramble,
        new ArrayList<CubeMove>(moves), shownAt, recognition, execution, moves.size(),
        resetCount, revealed, abandoned, practice, guided && !practice);
    // A practice rep is none of the drill's: it neither spends one nor counts towards retiring.
    if (!practice) {
      reps.add(rep);
    }
    // A retiring drill deals nothing again: a case stays in until it has earned its way out.
    if (!practice && !spec.retiresCases() && !abandoned && (resetCount > 0 || revealed)) {
      // Not for one that was skipped: giving up on a case is an answer about it, and the rep spends
      // it whether or not it had been restarted or looked up first.
      showAgain(currentCase);
    }
    currentCase = null;
    caseShownAtMs = NOT_SHOWN;
    return rep;
  }

  /** What the reps so far have counted towards each case leaving the drill. */
  public CaseTally getTally() {
    return CaseTally.of(spec, reps);
  }

  /** Whether a case has beaten its target often enough to leave the drill. */
  public boolean isRetired(String code) {
    return getTally().isCleared(code);
  }

  /** The cases this app can deal, in the order the spec named them. */
  public List<String> getCases() {
    return Collections.unmodifiableList(cases);
  }

  /**
   * The cases still being dealt, in the order the spec named them, or by weight for a drill with a
   * working set, which deals from only the first of them that have not retired.
   */
  public List<String> getActiveCases() {
    CaseTally tally = getTally();
    int limit = spec.getWorkingSet() > 0 ? spec.getWorkingSet() : Integer.MAX_VALUE;
    List<String> active = new ArrayList<String>();
    for (String code : cases) {
      if (active.size() < limit && !tally.isCleared(code)) {
        active.add(code);
      }
    }
    return active;
  }

  /** When the case went up, said in the clock the moves are stamped on. */
  private long caseShownOnCubeClock() {
    return caseShownAtMs - clockSkewMs;
  }

  /**
   * Deals a case again later in this pass, for one whose rep measured nothing: restarted, or solved
   * with the algorithm on screen. Spending a case on a rep the figures will not read leaves the
   * drill with nothing to say about it, and on a short drill it never comes round again.
   *
   * <p>Never as the next case, which is what {@link #resetRep} already offers and what nobody needs
   * twice. Nothing is done where the pass is spent, since the next one deals every case anyway, and
   * nothing for a drill drawing by weight, which can deal any case at any time.
   *
   * <p><b>And nothing where the drill has no rep to spare.</b> The extra showing is a rep like any
   * other and comes out of the same count, so on a drill of one rep per case it was paid for by the
   * case at the end of the pass, which the user had picked and then never saw. A case still owed
   * its first turn outranks a second turn for one that has already had it.
   */
  private void showAgain(String code) {
    if (!pass.isEmpty() && spec.getReps() - reps.size() > pass.size()) {
      pass.add(random.nextInt(pass.size()), code);
    }
  }

  /** Every case in turn, the order redrawn each pass so that the next one cannot be guessed. */
  private String pick() {
    List<String> active = getActiveCases();
    if (spec.getSelection() == DrillSpec.Selection.WEIGHTED) {
      return weighted(active);
    }
    pass.retainAll(active); // a working set can have moved on since the pass was drawn
    if (pass.isEmpty()) {
      pass.addAll(active);
      Collections.shuffle(pass, random);
    }
    return pass.remove(pass.size() - 1);
  }

  private String weighted(List<String> active) {
    long total = 0;
    for (String code : active) {
      total += weightOf(code);
    }
    long draw = (long) (random.nextDouble() * total);
    for (String code : active) {
      draw -= weightOf(code);
      if (draw < 0) {
        return code;
      }
    }
    return active.get(active.size() - 1);
  }

  private long weightOf(String code) {
    Long weight = weights == null ? null : weights.get(code);
    return weight == null || weight <= 0 ? 1 : weight;
  }

  /** The case in front of the user, or null between reps. */
  public String getCurrentCase() {
    return currentCase;
  }

  /** What the virtual cube was set up with. */
  public String getCurrentScramble() {
    return currentScramble;
  }

  /** The virtual cube as it stands, for drawing. */
  public String getFacelets() {
    return cube.toFaceCube();
  }

  public List<DrillRep> getReps() {
    return Collections.unmodifiableList(reps);
  }

  public boolean isFinished() {
    return !isRunnable() || reps.size() >= spec.getReps() || getActiveCases().isEmpty();
  }
}
