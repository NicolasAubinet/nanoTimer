package com.cube.nanotimer.gui;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.R;
import com.cube.nanotimer.coach.CoachPayloadBuilder;
import com.cube.nanotimer.cube.AlgorithmFollower;
import com.cube.nanotimer.cube.CaseFlags;
import com.cube.nanotimer.cube.CubePatternFormat;
import com.cube.nanotimer.cube.CubeStickering;
import com.cube.nanotimer.cube.SmartCubeManager;
import com.cube.nanotimer.cube.SolveTypeMethod;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.session.MethodStatistics;
import com.cube.nanotimer.smartcube.drill.CaseTally;
import com.cube.nanotimer.smartcube.drill.DrillRep;
import com.cube.nanotimer.smartcube.drill.DrillSession;
import com.cube.nanotimer.smartcube.drill.LayerRotation;
import com.cube.nanotimer.smartcube.drill.PairWeights;
import com.cube.nanotimer.smartcube.step.CaseAlgorithms;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.drill.DrillSpec;
import com.cube.nanotimer.gui.widget.DrillCasePips;
import com.cube.nanotimer.gui.widget.DrillCaseTable;
import com.cube.nanotimer.gui.widget.DrillLiveCells;
import com.cube.nanotimer.gui.widget.DrillStatCells;
import com.cube.nanotimer.gui.widget.dialog.CaseAlgorithmsDialog;
import com.cube.nanotimer.smartcube.model.CubeMove;
import com.cube.nanotimer.smartcube.model.CubeOrientation;
import com.cube.nanotimer.smartcube.model.CubeRotation;
import com.cube.nanotimer.smartcube.model.CubeState;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.gui.widget.GroupLearnedPanel;
import com.cube.nanotimer.session.CaseKnowledge;
import com.cube.nanotimer.step.LastLayerAlgorithms;
import com.cube.nanotimer.step.OllShapes;
import com.cube.nanotimer.vo.CaseHistory;
import com.cube.nanotimer.util.view.DrillRepFlourish;
import com.cube.nanotimer.util.view.LearningAlgorithmFade;
import com.cube.nanotimer.util.view.PracticeOfferPulse;
import com.cube.nanotimer.util.view.ScrambleFollowAnimator;
import com.cube.nanotimer.util.view.DrillControlLabels;
import com.cube.nanotimer.util.view.DrillSplitBarView;
import com.cube.nanotimer.util.view.StepPalette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runs a case drill: the case stands on the screen's cube, the smart cube is only the input, and the
 * user's own cube ends up scrambled, which is expected and needs nothing done about it.
 *
 * <p><b>There is no control that starts or stops a rep, and none may be added.</b> The cube reports
 * both ends of one: a rep begins when the case is put up and ends the moment
 * {@code CubieCube.isSolved} comes back true. Case after case with nothing to press is most of what
 * makes this feel unlike a scramble list. Neither button is an exception: one spends the rep in
 * front of the user, the other deals it again.
 *
 * <p><b>The case on screen is never named.</b> Naming it would hand over the answer, and a
 * recognition drill would be timing reading instead. A case is named once its rep is over, beside
 * that rep's time, and goes again with the case it belongs to: left standing into the next rep it
 * reads as naming the case being worked, which is the thing this screen is careful about. It
 * belongs beside the time and nowhere else, since set under the cube at headline size it read as a
 * caption for the cube, and so again as the name of the case being looked at.
 */
public class DrillActivity extends DrillScreenActivity implements DrillCaseTable.Listener {

  /** The drill to run, as its JSON text. */
  public static final String EXTRA_SPEC = "drillSpec";

  /**
   * The face to deal the last layer onto, as its letter. Not on the spec: which colour a solver
   * finishes on is theirs, and a drill prescribed from outside has no way of knowing it.
   */
  public static final String EXTRA_LAYER_FACE = "drillLayerFace";

  /** The solve type whose recent solves lean an F2L pair drill's draw. Optional. */
  public static final String EXTRA_SOLVE_TYPE = "drillSolveType";

  /**
   * How long the solved cube stays up before the next case replaces it.
   *
   * <p>The length of the green beat, so the wash lands on the case it belongs to rather than over
   * its successor. The rep's line is not on this clock: it outlives its case and only stands back
   * when the next one is dealt, because one beat is not long enough to read. The timing does not
   * care either way, since recognition runs from when the next case is <em>shown</em>, so the hold
   * is charged to nobody.
   */
  private static final long REP_HOLD_MS = DrillRepFlourish.PLAY_BEAT_MS;

  /** The family a case belongs to, which is the colour its name is written in everywhere. */
  private static final String FAMILY_OLL = "oll_";

  private DrillSession session;
  /** What this drill is called, for the summary once the bar that named it has gone. */
  private String label;
  /** The face the user finishes on, which is the one the drawn cube has to stand on. */
  private String layerFace;
  /** The one family this drill deals, whose colour the meter and the mean cell are written in. */
  private int familyHue;

  /** The whole rep line, since the beat scales it and the hold is posted to it. */
  private View lastRepRow;
  private View repWash;
  /** A coach drill's cases, its verdict on a rep, and where that rep's time went. */
  private DrillCasePips casePips;
  private TextView repVerdict;
  private DrillSplitBarView repSplit;
  /** The rep the line is about, which the case bars ring; null before one or after practice. */
  private DrillRep lastRep;
  /** The offer to practise a case just turned off its list, and the case it is for. */
  private TextView practiceOffer;
  private String offeredCase;
  private TextView practiceAlgorithm;
  /** Greys the practised algorithm's moves as they are turned; null where there is none to follow. */
  private AlgorithmFollower algorithmFollower;
  private ScrambleFollowAnimator algorithmAnimator;
  /** The scramble's own face: an algorithm being followed reads like a scramble being turned. */
  private Typeface algorithmFace;
  private Button skip;
  /** For a coach learning drill: the cases the solves know, those cleared here, a group to fete. */
  private final Set<String> knownCases = new HashSet<String>();
  private final Set<String> clearedHere = new HashSet<String>();
  private String groupToCelebrate;
  /** Back leaves practice for the drill rather than ending the drill. */
  private final OnBackPressedCallback practiceBack = new OnBackPressedCallback(false) {
    @Override
    public void handleOnBackPressed() {
      leavePractice();
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drill_screen);
    bindDrillScreen();

    lastRepRow = findViewById(R.id.llDrillLastRep);
    repWash = findViewById(R.id.drillRepWash);
    casePips = findViewById(R.id.vDrillCasePips);
    repVerdict = findViewById(R.id.tvDrillLastRepVerdict);
    repSplit = findViewById(R.id.vDrillRepSplit);
    casePips.setBars(true);
    practiceOffer = findViewById(R.id.tvDrillPracticeOffer);
    practiceAlgorithm = findViewById(R.id.tvDrillPracticeAlg);
    algorithmAnimator = new ScrambleFollowAnimator(practiceAlgorithm);
    Drawable bulb = ContextCompat.getDrawable(this, R.drawable.ic_case_idea).mutate();
    bulb.setTint(ContextCompat.getColor(this, R.color.on_accent));
    int bulbSize = getResources().getDimensionPixelSize(R.dimen.drill_practice_offer_icon);
    bulb.setBounds(0, 0, bulbSize, bulbSize);
    practiceOffer.setCompoundDrawables(bulb, null, null, null);
    practiceOffer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        enterPractice();
      }
    });
    getOnBackPressedDispatcher().addCallback(this, practiceBack);
    getSupportFragmentManager().setFragmentResultListener(CaseAlgorithmsDialog.FLAGS_CHANGED,
        this, new FragmentResultListener() {
          @Override
          public void onFragmentResult(String key, Bundle result) {
            showPracticeAlgorithm(); // a pick in the case popup is the algorithm practised
            if (session != null && session.getSpec().isLearning()) {
              teachAll(); // and the one a learning drill teaches
            }
          }
        });
    getSupportFragmentManager().registerFragmentLifecycleCallbacks(
        new FragmentManager.FragmentLifecycleCallbacks() {
          @Override
          public void onFragmentViewDestroyed(FragmentManager fm, Fragment f) {
            if (f instanceof CaseAlgorithmsDialog) {
              restartPractice();
            }
          }
        }, false);

    skip = findViewById(R.id.btDrillSkip);
    skip.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (session != null && session.getPracticeCase() != null) {
          leavePractice();
          return;
        }
        DrillRep rep = session == null ? null : session.abandon();
        if (rep != null) {
          onRepFinished(rep);
        }
      }
    });
    labelButtons(false);
    findViewById(R.id.btDrillAlgorithm).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showAlgorithms();
      }
    });
    findViewById(R.id.btDrillReset).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        resetRep();
      }
    });
    findViewById(R.id.btDrillDone).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

    String json = getIntent().getStringExtra(EXTRA_SPEC);
    DrillSpec spec;
    try {
      // Refused rather than replaced: running a different drill from the one that was sent would
      // report reps against a prescription nobody made.
      spec = DrillSpec.fromJson(json);
    } catch (RuntimeException e) {
      showUnavailable(getString(R.string.drill_spec_unreadable));
      return;
    }
    familyHue = StepPalette.cfop(this)
        .colorFor(spec.getCases().isEmpty() ? null : spec.getCases().get(0));
    label = spec.getLabel() == null ? getString(R.string.drill_title) : spec.getLabel();
    setTitle(label);
    layerFace = getIntent().getStringExtra(EXTRA_LAYER_FACE);
    SolveType solveType = (SolveType) getIntent().getSerializableExtra(EXTRA_SOLVE_TYPE);
    if (spec.getType() == DrillSpec.Type.F2L_PAIR && solveType != null) {
      loadPairWeights(spec, solveType);
    } else {
      begin(spec, spec.getType() == DrillSpec.Type.F2L_PAIR
          ? PairWeights.of(spec.getCases(), null) : null);
    }
  }

  /** The pairs' recent figures lean the draw, so the session waits for them. */
  private void loadPairWeights(final DrillSpec spec, SolveType solveType) {
    App.INSTANCE.getService().getMethodStatistics(solveType, SolveTypeMethod.of(solveType),
        CoachPayloadBuilder.FAMILY_WINDOW, new DataCallback<MethodStatistics>() {
          @Override
          public void onData(final MethodStatistics statistics) {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                if (!isFinishing()) {
                  begin(spec, PairWeights.of(spec.getCases(), statistics));
                }
              }
            });
          }
        });
  }

  private void begin(DrillSpec spec, Map<String, Long> weights) {
    session = new DrillSession(spec, Utils.getRandom(), weights, layerFace);
    initRecording(spec);
    hidePracticeOffer();
    if (spec.isLearning()) {
      teachAll();
      if (spec.isFromCoach()) {
        loadKnownCases();
      }
    }

    if (!session.isRunnable()) {
      showUnavailable(getString(R.string.drill_no_known_cases));
      return;
    }
    if (spec.retiresCases()) {
      useCaseProgress();
    }
    startWhenCubeConnected();
  }

  /**
   * The first case is queued before the page is up rather than waited for: the view holds it and
   * draws it as its first frame, so the drill never opens on a solved cube it then replaces.
   */
  @Override
  protected void startDrill() {
    nextRep();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    lastRepRow.removeCallbacks(showNextCase);
    DrillRepFlourish.cancel(repWash, lastRepRow);
  }

  @Override
  protected boolean isDrillRunning() {
    return session != null;
  }

  /** The case is on screen, which is where the first rep's recognition runs from. */
  @Override
  protected void onCaseVisible() {
    caseShown();
  }

  @Override
  protected boolean standsOnMat() {
    return true;
  }

  /** The three controls, each with its icon; practice turns Skip into the way back. */
  private void labelButtons(boolean practising) {
    DrillControlLabels.apply(
        new Button[] {findViewById(R.id.btDrillAlgorithm), findViewById(R.id.btDrillReset), skip},
        new int[] {R.drawable.ic_drill_alg, R.drawable.ic_drill_reset,
            practising ? R.drawable.ic_drill_back : R.drawable.ic_drill_skip},
        new int[] {R.string.drill_case_algorithm, R.string.drill_reset,
            practising ? R.string.drill_practice_back : R.string.drill_skip});
  }

  /**
   * The layer stands on top, the way the user is holding their own cube.
   *
   * <p>A case is dealt in the colours' own frame, where white is up whatever colour its owner
   * finishes on, so a last layer on yellow was drawn on the underside of a cube whose top face was
   * a solved white. On a cube with a gyroscope the grip answers this and the page ignores what is
   * asked here; on one without, nothing says how it is held and this is the whole of the answer.
   */
  @Override
  protected CubeOrientation restingRotation() {
    CubeRotation rotation = CubeRotation.byNotation(LayerRotation.toTop(layerFace));
    return rotation == null ? null : rotation.quaternion();
  }

  /**
   * The state the cube says it is really in. A turn it made without reporting is recovered here,
   * and the case is drawn again from where that leaves it: a QiYi drops a state change during fast
   * slices, and the drill that missed one drew a scrambled cube for the rest of the rep and never
   * saw the case solved however well it was.
   */
  @Override
  public void onState(CubeState state) {
    if (!cubeReady || finished || session == null) {
      return;
    }
    DrillSession.Correction correction = session.onState(state);
    if (!correction.isRepaired()) {
      return;
    }
    cube.setState(CubePatternFormat.format(session.getFacelets()));
    if (correction.getRep() != null) {
      onRepFinished(correction.getRep());
    }
  }

  /**
   * A turn made before the case is on screen is dropped, not queued. Both cubes here miss it
   * equally, so they stay in step, and the physical cube's own state never mattered; timing a rep
   * against a case the user could not yet see would.
   */
  @Override
  public void onMove(CubeMove move) {
    if (!cubeReady || finished || session == null || session.getCurrentCase() == null) {
      return;
    }
    hidePracticeOffer(); // turning the next case is the answer to the offer
    cube.addMove(move.getNotation());
    if (algorithmFollower != null && isAlgorithmUp()) {
      algorithmFollower.onMove(move);
      showAlgorithmProgress();
    }
    DrillRep rep = session.onMove(move);
    if (rep != null) {
      onRepFinished(rep);
    }
  }

  /**
   * Holds the solved cube for a beat before the next case goes up.
   *
   * <p>Not decoration. A case that was replaced the instant it was solved read as though the user
   * had done something wrong, because the only thing they saw was the cube changing under them. The
   * beat costs them nothing: recognition runs from when the next case is <em>shown</em>, so the
   * hold is charged to nobody.
   */
  private void onRepFinished(DrillRep rep) {
    recorder.record(rep);
    noteLearned(rep);
    if (session.getSpec().retiresCases() && !rep.isPractice()) {
      refreshCaseProgress(); // during the beat, so the dot fills beside the rep that earned it
    }
    showLastRep(rep);
    offerPractice(rep);
    // No wash for a rep that was skipped: nothing was solved to dwell on. The hold stays, since
    // what the user could not place is the one thing worth reading off a skipped rep, and the line
    // goes with the case.
    if (isOverTarget(rep)) {
      DrillRepFlourish.playQuietly(repWash, lastRepRow); // green would contradict the red verdict
    } else if (!rep.isAbandoned()) {
      DrillRepFlourish.play(repWash, lastRepRow);
    }
    holdThenNext();
  }

  /** A counted coach-drill rep that missed its case's target. */
  private boolean isOverTarget(DrillRep rep) {
    DrillSpec spec = session.getSpec();
    return spec.retiresCases() && !spec.isLearning() && !rep.isPractice() && !rep.isAbandoned()
        && CaseTally.isClean(rep) && !session.getTally().isWin(rep);
  }

  private void holdThenNext() {
    lastRepRow.removeCallbacks(showNextCase);
    lastRepRow.postDelayed(showNextCase, REP_HOLD_MS);
  }

  private final Runnable showNextCase = new Runnable() {
    @Override
    public void run() {
      if (groupToCelebrate != null) {
        celebrate(groupToCelebrate);
        return;
      }
      nextRep();
    }
  };

  /** Every case a learning drill teaches, against the algorithm the screen shows for it. */
  private void teachAll() {
    for (String code : session.getCases()) {
      session.setTaughtAlgorithm(code, learningAlgorithm(code));
    }
  }

  /** The cases the solves say are known, read once for a shape group's completion. */
  private void loadKnownCases() {
    App.INSTANCE.getService().getCaseHistory(0, new DataCallback<CaseHistory>() {
      @Override
      public void onData(final CaseHistory history) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            for (CaseKnowledge known : history.getCases()) {
              if (known.getStatus() == CaseKnowledge.Status.KNOWN) {
                knownCases.add(known.getCode());
              }
            }
          }
        });
      }
    });
  }

  /**
   * A case a learning drill has just cleared counts as learned from now on; in a coach drill, one
   * that completes its shape group is celebrated once the rep's beat is over.
   */
  private void noteLearned(DrillRep rep) {
    String code = rep.getCaseCode();
    if (!session.getSpec().isLearning() || rep.isPractice() || !session.isRetired(code)
        || !clearedHere.add(code)) {
      return;
    }
    Options.INSTANCE.addLearnedCase(code);
    String group = OllShapes.groupOf(code);
    if (!session.getSpec().isFromCoach() || group == null
        || Options.INSTANCE.isGroupCelebrated(group)) {
      return;
    }
    if (learnedCases().containsAll(OllShapes.casesOf(group))) {
      groupToCelebrate = group;
    }
  }

  private Set<String> learnedCases() {
    Set<String> learned = new HashSet<String>(knownCases);
    learned.addAll(Options.INSTANCE.getLearnedCases());
    return learned;
  }

  private void celebrate(String group) {
    groupToCelebrate = null;
    Options.INSTANCE.setGroupCelebrated(group);
    int learned = 0;
    Set<String> cases = learnedCases();
    for (String[] row : LastLayerAlgorithms.ORIENTATIONS) {
      if (cases.contains("oll_" + row[0])) {
        learned++;
      }
    }
    GroupLearnedPanel.show(this, group, OllShapes.casesOf(group), learned,
        LastLayerAlgorithms.ORIENTATIONS.length, new Runnable() {
          @Override
          public void run() {
            if (!finished && !isFinishing()) {
              nextRep();
            }
          }
        });
  }

  /**
   * Deals the same case again, for one botched by a slip rather than by not knowing it. Spends no
   * rep, and there is no hold: the user is waiting on the case they asked to have back.
   */
  private void resetRep() {
    if (session.getCurrentCase() == null) {
      return; // between reps, with the finished cube still up
    }
    session.resetRep();
    cube.setState(CubePatternFormat.format(session.getFacelets()));
    restartAlgorithmProgress();
    if (cubeReady) {
      caseShown();
    }
  }

  /**
   * The algorithms for the case in front of the user, which is the one thing this screen otherwise
   * refuses to say. Someone who has drawn a case they cannot place is not learning anything by
   * staring at it, and the alternative was leaving the drill to go and look it up.
   *
   * <p>The rep is not stopped or forgiven. It goes on being timed and is marked as looked up, so a
   * time reached this way is not later read as knowing the case. Between reps there is no case up
   * and nothing to show.
   */
  private void showAlgorithms() {
    String currentCase = session == null ? null : session.getPracticeCase();
    if (currentCase == null) {
      currentCase = session == null ? null : session.getCurrentCase();
      if (currentCase == null) {
        return;
      }
      session.markRevealed();
    }
    DialogUtils.showFragment(this, CaseAlgorithmsDialog.newInstance(currentCase));
  }

  /**
   * Offers to practise the case, for a coach drill's rep turned off the case's list: the bulb solve
   * details lights, so never for an algorithm the user muted, picked or typed in.
   */
  private void offerPractice(DrillRep rep) {
    hidePracticeOffer();
    String turned = rep.getTurned();
    if (!session.getSpec().isFromCoach() || rep.isPractice() || rep.isAbandoned()
        || !CaseAlgorithms.isWorthPointingOut(rep.getCaseCode(), turned)
        || CaseFlags.isTheirs(rep.getCaseCode(), turned)) {
      return;
    }
    // A learning drill's own algorithm is what it asked for, even when off the usual list.
    if (session.getSpec().isLearning() && CaseAlgorithms.sameTurning(rep.getCaseCode(), turned,
        learningAlgorithm(rep.getCaseCode()))) {
      return;
    }
    offeredCase = rep.getCaseCode();
    practiceOffer.setVisibility(View.VISIBLE);
    PracticeOfferPulse.play(practiceOffer);
  }

  /**
   * The offered case, over and over against the algorithm the user picks, until they go back. The
   * popup opens on its own while no algorithm is picked for the case.
   */
  private void enterPractice() {
    if (session == null || offeredCase == null) {
      return;
    }
    hidePracticeOffer();
    lastRepRow.removeCallbacks(showNextCase); // tapped during the beat
    session.startPractice(offeredCase);
    setPracticing(true);
    drawCase();
    if (Options.INSTANCE.getCaseAlgorithm(offeredCase) == null) {
      showAlgorithms();
    }
  }

  private void leavePractice() {
    lastRepRow.removeCallbacks(showNextCase);
    session.endPractice();
    setPracticing(false);
    nextRep();
  }

  /** The practice case dealt afresh, since turns made behind the popup were not an attempt at it. */
  private void restartPractice() {
    if (finished || isFinishing() || session == null || session.getPracticeCase() == null) {
      return; // also reached when the screen is torn down with the popup open
    }
    lastRepRow.removeCallbacks(showNextCase);
    session.nextRep();
    drawCase();
  }

  /** A coach drill keeps the offer's room, so the cube does not jump when it shows. */
  private void hidePracticeOffer() {
    boolean coach = session != null && session.getSpec().isFromCoach();
    practiceOffer.setVisibility(coach ? View.INVISIBLE : View.GONE);
  }

  /** Practice counts no case, so the drill's progress steps aside for a line saying so. */
  private void setPracticing(boolean on) {
    boolean retiring = session.getSpec().retiresCases();
    casePips.setVisibility(!on && retiring ? View.VISIBLE : View.GONE);
    TextView progress = findViewById(R.id.tvDrillProgress);
    progress.setVisibility(View.VISIBLE); // the card's heading in every mode
    findViewById(R.id.pbDrillProgress).setVisibility(!on && !retiring ? View.VISIBLE : View.GONE);
    if (on) {
      progress.setText(R.string.drill_practice_mode);
    }
    labelButtons(on);
    // A learning drill's fade may still be running, and would hide the practised algorithm.
    LearningAlgorithmFade.show(practiceAlgorithm);
    practiceAlgorithm.setVisibility(on ? View.VISIBLE : View.GONE);
    practiceBack.setEnabled(on);
    showPracticeAlgorithm();
  }

  /** The algorithm picked for the case being practised, or how to pick one. */
  private void showPracticeAlgorithm() {
    String code = session == null ? null : session.getPracticeCase();
    if (code == null) {
      return;
    }
    followAlgorithm(Options.INSTANCE.getCaseAlgorithm(code));
  }

  /** Writes an algorithm under the cube and greys it as turned, or says how to pick one. */
  private void followAlgorithm(String algorithm) {
    algorithmAnimator.reset();
    algorithmFollower = null;
    if (algorithm != null) {
      try {
        algorithmFollower = new AlgorithmFollower(algorithm);
      } catch (IllegalArgumentException e) {
        algorithmFollower = null; // shown as written, just not greyed
      }
    }
    practiceAlgorithm.setText(algorithm != null ? algorithm
        : getString(R.string.drill_practice_pick));
    practiceAlgorithm.setTextSize(TypedValue.COMPLEX_UNIT_SP, algorithm != null ? 18 : 14);
    if (algorithmFace == null) {
      algorithmFace = Typeface.createFromAsset(getAssets(), "fonts/JetBrainsMono-Medium.ttf");
    }
    practiceAlgorithm.setTypeface(algorithm != null ? algorithmFace : Typeface.DEFAULT);
    practiceAlgorithm.setTextColor(ContextCompat.getColor(this,
        algorithm != null ? R.color.white : R.color.secondary_text));
    showAlgorithmProgress();
  }

  /** A learning drill shows the case's algorithm for its first reps, then lets it fade. */
  private void showLearningAlgorithm() {
    if (!session.getSpec().isLearning() || session.getPracticeCase() != null) {
      return;
    }
    if (!session.isGuided()) {
      algorithmFollower = null;
      LearningAlgorithmFade.hide(practiceAlgorithm);
      return;
    }
    followAlgorithm(learningAlgorithm(session.getCurrentCase()));
    LearningAlgorithmFade.show(practiceAlgorithm);
  }

  /** The user's chosen algorithm, else the one the drill names if it solves the case, else the usual. */
  private String learningAlgorithm(String code) {
    String chosen = Options.INSTANCE.getCaseAlgorithm(code);
    if (chosen != null) {
      return chosen;
    }
    String named = session.getSpec().getCaseAlgorithm(code);
    if (named != null && CaseAlgorithms.solves(code, named)) {
      return named;
    }
    return LastLayerAlgorithms.forCaseCode(code);
  }

  /** Whether an algorithm is on screen for the case up: practice, or a guided learning rep. */
  private boolean isAlgorithmUp() {
    return session.getPracticeCase() != null || session.isGuided();
  }

  /** The practised algorithm with its turned moves greyed, as the timer greys a scramble's. */
  private void showAlgorithmProgress() {
    if (algorithmFollower != null) {
      algorithmAnimator.show(new SpannableString(TextUtils.join(" ",
          algorithmFollower.getTokens())), algorithmFollower.getDoneCount());
    }
  }

  /** A case dealt again starts the algorithm over. */
  private void restartAlgorithmProgress() {
    if (algorithmFollower != null && isAlgorithmUp()) {
      algorithmFollower.reset();
      algorithmAnimator.reset();
      showAlgorithmProgress();
    }
  }

  private void nextRep() {
    if (!session.nextRep()) {
      showSummary();
      return;
    }
    drawCase();
  }

  /** Puts the case just dealt on the screen's cube. */
  private void drawCase() {
    if (session.getShownEdges() != null) {
      cube.setStickering(
          CubeStickering.pieces(session.getShownEdges(), session.getShownCorners()));
    }
    cube.setState(CubePatternFormat.format(session.getFacelets()));
    restartAlgorithmProgress();
    showLearningAlgorithm();
    // ⚠️ The case now on screen is deliberately not named anywhere. Only the cube says what it
    // is. The line naming the rep that is over stays, since one beat is not long enough to read
    // it, but it stands back so that it cannot be taken for the name of this one.
    recedeLastRep();
    if (session.getPracticeCase() == null) {
      if (session.getSpec().retiresCases()) {
        refreshCaseProgress();
      } else {
        setProgress(session.getReps().size() + 1, session.getSpec().getReps());
      }
    }
    // Only once the case is really up: before the page has drawn, the first one is not yet in
    // front of anybody, and onCubeDrawn is what says it is.
    if (cubeReady) {
      caseShown();
    }
  }

  /**
   * The case is in front of the user, which is where the rep's looking runs from and where the
   * turns that count start.
   *
   * <p>The cube is asked where it stands at the same moment. From here on, a turn it makes without
   * reporting has to be recoverable, and it is only recoverable against a state: waiting for the
   * next one to arrive by itself would leave the first turn of the rep the one turn whose loss
   * nothing later could put right.
   */
  private void caseShown() {
    session.markCaseShown(System.currentTimeMillis());
    session.onState(SmartCubeManager.INSTANCE.getCurrentState());
  }

  /**
   * The rep that has just ended, named and timed together. Naming it is only safe here: the case is
   * over, so it can say what was done, or for a skipped one what it was the user could not place.
   */
  private void showLastRep(DrillRep rep) {
    String name = Utils.toSmartCubeCaseHeadline(this, rep.getCaseCode());
    int colour = familyColour(rep.getCaseCode());
    boolean retiring = session.getSpec().retiresCases();
    repVerdict.setVisibility(View.GONE);
    lastRep = rep.isPractice() ? null : rep;
    DrillLiveCells.show(this, session.getReps());
    if (retiring) {
      refreshCaseProgress(); // rings the case this line is about
    }
    if (rep.isAbandoned()) {
      setLastRep(name, colour, getString(R.string.drill_rep_skipped), true, null);
      repSplit.setVisibility(View.INVISIBLE); // keeps its room, so the cube does not move
      return;
    }
    setLastRep(name, colour, FormatterService.INSTANCE.formatSolveTime(rep.getTotalMs()), false,
        splitWords(rep));
    repSplit.setSplit(rep.getRecognitionMs(), rep.getExecutionMs(),
        ContextCompat.getColor(this, colour));
    repSplit.setVisibility(View.VISIBLE);
    if (rep.isPractice()) {
      showPracticeVerdict(rep);
    } else if (retiring) {
      showVerdict(rep);
    }
  }

  /** The split bar's two halves in words: each name quiet, its figure in white. */
  private CharSequence splitWords(DrillRep rep) {
    SpannableStringBuilder words = new SpannableStringBuilder();
    appendHalf(words, R.string.drill_summary_column_recognition, rep.getRecognitionMs());
    words.append("   ");
    appendHalf(words, R.string.drill_summary_column_execution, rep.getExecutionMs());
    return words;
  }

  private void appendHalf(SpannableStringBuilder words, int nameRes, long ms) {
    words.append(getString(nameRes)).append(' ');
    int start = words.length();
    words.append(FormatterService.INSTANCE.formatSolveTime(ms));
    words.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.white)), start,
        words.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
  }

  /** Flags a practice rep that was not the algorithm picked for it; a rep that was says nothing. */
  private void showPracticeVerdict(DrillRep rep) {
    String chosen = Options.INSTANCE.getCaseAlgorithm(rep.getCaseCode());
    if (chosen == null || CaseAlgorithms.sameTurning(rep.getCaseCode(), chosen, rep.getTurned())) {
      return;
    }
    setVerdict(getString(R.string.drill_practice_other), R.color.danger_text);
  }

  private static int familyColour(String code) {
    if (CaseAlgorithms.isPair(code)) {
      return R.color.step_f2l;
    }
    return code != null && code.startsWith(FAMILY_OLL) ? R.color.step_oll : R.color.step_pll;
  }

  /** A coach drill counts cases, not reps: a bar per case takes the rep count's meter's place. */
  private void useCaseProgress() {
    findViewById(R.id.pbDrillProgress).setVisibility(View.GONE);
    casePips.setVisibility(View.VISIBLE);
    refreshCaseProgress();
  }

  private void refreshCaseProgress() {
    List<String> cases = shownCases(true);
    CaseTally tally = session.getTally();
    boolean won = lastRep != null && CaseTally.isClean(lastRep) && tally.isWin(lastRep);
    casePips.show(cases, tally, session.getSpec().getWinsToRetire(),
        lastRep == null ? null : lastRep.getCaseCode(), won);
    if (session.getPracticeCase() == null) {
      int cleared = 0;
      for (String code : cases) {
        cleared += tally.isCleared(code) ? 1 : 0;
      }
      ((TextView) findViewById(R.id.tvDrillProgress))
          .setText(getString(R.string.drill_cases_cleared, cleared, cases.size()));
    }
  }

  /**
   * The cases to count progress over. A drill with a working set may hold far more cases than it
   * reaches, so only those dealt so far are shown, plus while it runs those in the set.
   */
  private List<String> shownCases(boolean running) {
    if (session.getSpec().getWorkingSet() == 0) {
      return session.getCases();
    }
    List<String> active = running ? session.getActiveCases() : Collections.<String>emptyList();
    List<String> shown = new ArrayList<String>();
    for (String code : session.getCases()) {
      if (active.contains(code) || wasDealt(code)) {
        shown.add(code);
      }
    }
    return shown;
  }

  private boolean wasDealt(String code) {
    for (DrillRep rep : session.getReps()) {
      if (code.equals(rep.getCaseCode())) {
        return true;
      }
    }
    return false;
  }

  /** How the rep did against its case's target, said only once the rep is over. */
  private void showVerdict(DrillRep rep) {
    long target = session.getSpec().getTargetMs(rep.getCaseCode());
    String text;
    int colourRes;
    if (session.getSpec().isLearning() && rep.isGuided() && !rep.isAbandoned()) {
      text = getString(R.string.drill_rep_guided);
      colourRes = R.color.secondary_text;
    } else if (rep.isOffTaught()) {
      text = getString(R.string.drill_rep_other_alg);
      colourRes = R.color.danger_text;
    } else if (session.getSpec().isLearning() && CaseTally.isClean(rep)) {
      text = getString(R.string.drill_rep_unaided);
      colourRes = R.color.green_soft;
    } else if (!CaseTally.isClean(rep)) {
      text = getString(R.string.drill_rep_not_counted);
      colourRes = R.color.secondary_text;
    } else if (session.getTally().isWin(rep)) {
      text = getString(R.string.drill_rep_under, FormatterService.INSTANCE.formatSolveTime(target));
      colourRes = R.color.green_soft;
    } else {
      text = getString(R.string.drill_rep_over,
          FormatterService.INSTANCE.formatSolveTime(rep.getTotalMs() - target));
      colourRes = R.color.danger_text;
    }
    setVerdict(text, colourRes);
  }

  private void setVerdict(String text, int colourRes) {
    int colour = ContextCompat.getColor(this, colourRes);
    repVerdict.setText(text); // dark ink on a chip filled with the verdict's colour
    repVerdict.setBackgroundTintList(ColorStateList.valueOf(colour));
    repVerdict.setVisibility(View.VISIBLE);
  }

  @Override
  protected void showSummary() {
    if (finished) {
      return; // a cube unplugged on the summary screen must not re-run this over its own figures
    }
    finished = true;
    practiceBack.setEnabled(false);
    practiceOffer.setVisibility(View.GONE);
    showSummaryFor(label);

    // Before the figures give up: a drill where every case was skipped still has something to
    // report, which is which cases they were. It is also what prunes a rep, hence counted().
    DrillCaseTable table =
        new DrillCaseTable(this, session.getReps(), session.getSpec().getType(), this);
    showFigures(table.counted());
  }

  /**
   * A rep thrown out on the summary, or put back. It leaves the stored drill as well as the screen:
   * a rep the user rejected must not go on being averaged into the case history behind their back.
   */
  @Override
  public void onRepsPruned(int position, boolean deleted, List<DrillRep> counted) {
    recorder.setCaseRepDeleted(position, deleted);
    showFigures(counted);
  }

  /** The cells over the table, from the reps that count. Redrawn whenever that stops being all. */
  private void showFigures(List<DrillRep> reps) {
    DrillSpec spec = session.getSpec();
    if (spec.retiresCases()) {
      showTargets(reps);
    } else {
      setSummaryCell(0, getString(R.string.drill_summary_cell_reps), String.valueOf(reps.size()),
          getString(R.string.drill_summary_cell_of, spec.getReps()));
    }

    // Skipped reps are counted apart rather than folded into a mean, the same rule the case stats
    // follow: a case you gave up on is not a slow time.
    long recognitionTotal = 0;
    long executionTotal = 0;
    long best = Long.MAX_VALUE;
    String bestCase = null;
    int timed = 0;
    for (DrillRep rep : reps) {
      if (rep.isAbandoned()) {
        continue;
      }
      recognitionTotal += rep.getRecognitionMs();
      executionTotal += rep.getExecutionMs();
      if (rep.getTotalMs() < best) {
        best = rep.getTotalMs();
        bestCase = rep.getCaseCode();
      }
      timed++;
    }
    if (timed == 0) {
      showSummaryEmpty(R.string.drill_summary_nothing_timed);
      return;
    }
    showSummaryFigures(); // a drill pruned to nothing and then put back has these to get back
    // Both halves, side by side and the same size, whichever one the drill was scored on. Reading
    // a case is half of what a drill trains, and the half the target happens to name says nothing
    // about whether the other one is where the time went.
    ((TextView) findViewById(R.id.tvDrillCellKeyTwo)).setText(R.string.drill_summary_cell_mean);
    DrillStatCells.nameHalves(this, familyHue);
    ((TextView) findViewById(R.id.tvDrillMeanRecognition))
        .setText(FormatterService.INSTANCE.formatSolveTime(recognitionTotal / timed));
    ((TextView) findViewById(R.id.tvDrillMeanExecution))
        .setText(FormatterService.INSTANCE.formatSolveTime(executionTotal / timed));
    // The best rep is the best of both halves together, and says which case it was: a time with no
    // case attached was the one figure on this screen that could not be acted on.
    if (spec.retiresCases()) {
      // A coach drill's length is a result of its own, so it takes the cell the best rep had.
      setSummaryCell(2, getString(R.string.drill_summary_cell_reps), String.valueOf(reps.size()),
          getString(R.string.drill_summary_cell_at_most, spec.getReps()));
      // The cell is laid out for a best time, in the best time's green; a count is not one.
      ((TextView) findViewById(R.id.tvDrillCellValueThree))
          .setTextColor(ContextCompat.getColor(this, R.color.white));
    } else {
      setSummaryCell(2, getString(R.string.drill_summary_cell_best),
          FormatterService.INSTANCE.formatSolveTime(best),
          Utils.toSmartCubeCaseHeadline(this, bestCase));
    }
  }

  /**
   * A coach drill's summary: how many cases cleared, what stopped it, and each case against its
   * target. Read off the reps that count, so throwing a rep out below can take a clearing with it.
   */
  private void showTargets(List<DrillRep> reps) {
    DrillSpec spec = session.getSpec();
    List<String> cases = shownCases(false);
    CaseTally tally = CaseTally.of(spec, reps);
    List<String> over = new ArrayList<String>();
    LinearLayout rows = findViewById(R.id.llDrillTargetRows);
    rows.removeAllViews();
    LayoutInflater inflater = LayoutInflater.from(this);
    for (String code : cases) {
      String name = Utils.toSmartCubeCaseHeadline(this, code);
      if (!tally.isCleared(code)) {
        over.add(name);
      }
      rows.addView(targetRow(inflater, rows, code, name, reps, tally));
    }
    findViewById(R.id.llDrillTargets).setVisibility(View.VISIBLE);

    setSummaryCell(0, getString(R.string.drill_summary_cell_cleared),
        String.valueOf(cases.size() - over.size()),
        getString(R.string.drill_summary_cell_of_cases, cases.size()));

    StringBuilder note = new StringBuilder();
    if (session.getReps().size() >= spec.getReps()) {
      note.append(getString(R.string.drill_summary_ceiling, spec.getReps()));
    }
    // Pairs all carry the same name, so naming those still over says nothing the rows do not.
    if (!over.isEmpty() && spec.getType() != DrillSpec.Type.F2L_PAIR) {
      note.append(note.length() == 0 ? "" : " ").append(getString(spec.isLearning()
          ? R.string.drill_summary_still_learning : R.string.drill_summary_over_target,
          joined(over)));
    }
    TextView tvNote = findViewById(R.id.tvDrillSummaryNote);
    tvNote.setText(note);
    tvNote.setVisibility(note.length() == 0 ? View.GONE : View.VISIBLE);
  }

  /** One case: how often it was dealt, its wins, its target, and its mean against that target. */
  private View targetRow(LayoutInflater inflater, LinearLayout parent, final String code,
      String name, List<DrillRep> reps, CaseTally tally) {
    int dealt = 0;
    int timed = 0;
    long total = 0;
    for (DrillRep rep : reps) {
      if (code.equals(rep.getCaseCode())) {
        dealt++;
        if (!rep.isAbandoned()) {
          timed++;
          total += rep.getTotalMs();
        }
      }
    }
    long target = session.getSpec().getTargetMs(code);
    View row = inflater.inflate(R.layout.drill_target_row, parent, false);
    row.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogUtils.showFragment(DrillActivity.this, CaseAlgorithmsDialog.newInstance(code));
      }
    });
    TextView tvName = row.findViewById(R.id.tvDrillTargetName);
    tvName.setText(name);
    tvName.setTextColor(ContextCompat.getColor(this, familyColour(code)));
    ((TextView) row.findViewById(R.id.tvDrillTargetReps)).setText(String.valueOf(dealt));
    ((DrillCasePips) row.findViewById(R.id.vDrillTargetWins))
        .show(Collections.singletonList(code), tally, session.getSpec().getWinsToRetire());
    // A learning drill has no time to beat, only reps without looking.
    boolean learning = session.getSpec().isLearning();
    ((TextView) row.findViewById(R.id.tvDrillTargetTarget)).setText(learning
        ? getString(R.string.drill_case_no_time) : FormatterService.INSTANCE.formatSolveTime(target));
    TextView tvMean = row.findViewById(R.id.tvDrillTargetMean);
    if (learning && timed > 0) {
      tvMean.setText(FormatterService.INSTANCE.formatSolveTime(total / timed));
      tvMean.setTextColor(ContextCompat.getColor(this, R.color.white));
    } else if (timed == 0) {
      tvMean.setText(R.string.drill_case_no_time);
      tvMean.setTextColor(ContextCompat.getColor(this, R.color.secondary_text));
    } else {
      long mean = total / timed;
      tvMean.setText(FormatterService.INSTANCE.formatSolveTime(mean));
      tvMean.setTextColor(ContextCompat.getColor(this,
          mean <= target ? R.color.green_soft : R.color.danger_text));
    }
    return row;
  }

  private static String joined(List<String> names) {
    StringBuilder sb = new StringBuilder();
    for (String name : names) {
      sb.append(sb.length() == 0 ? "" : ", ").append(name);
    }
    return sb.toString();
  }
}
