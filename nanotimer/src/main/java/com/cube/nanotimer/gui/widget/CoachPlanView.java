package com.cube.nanotimer.gui.widget;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import androidx.fragment.app.FragmentActivity;

import com.cube.nanotimer.R;
import com.cube.nanotimer.coach.CoachPayload;
import com.cube.nanotimer.coach.FocusArea;
import com.cube.nanotimer.coach.StepShares;
import com.cube.nanotimer.coach.StoredCoachPlan;
import com.cube.nanotimer.drill.DrillSpec;
import com.cube.nanotimer.session.MethodStatistics;
import com.cube.nanotimer.gui.CrossDrillActivity;
import com.cube.nanotimer.gui.DrillActivity;
import com.cube.nanotimer.gui.DrillScreenActivity;
import com.cube.nanotimer.gui.DrillSetupActivity;
import com.cube.nanotimer.gui.widget.dialog.CaseAlgorithmsDialog;
import com.cube.nanotimer.smartcube.step.CaseAlgorithms;
import com.cube.nanotimer.smartcube.step.LastLayerDiagram;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.coach.CoachPractice;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.util.helper.Utils;

/**
 * A plan drawn as cards, whoever wrote it.
 *
 * <p>It renders the plan's codes and the payload's figures, and never the plan's own citations. A
 * claim is checked against the payload elsewhere ({@link StoredCoachPlan#uncited}); here the numbers
 * on screen are the user's own, so a coach that invented one has nothing to print with it. What a
 * coach adds that the device cannot is the prose, and that is the one thing taken from the plan.
 *
 * <p>A reason this version has never heard of still draws: its title is generic, its text is
 * whatever was written, and its drill still launches. An app on Play's release cycle will be older
 * than whatever is sending to it, and a card that refused would take the whole plan down.
 */
public class CoachPlanView {

  private final Activity activity;
  private final LinearLayout container;
  private final LayoutInflater inflater;

  public CoachPlanView(Activity activity, LinearLayout container) {
    this.activity = activity;
    this.container = container;
    this.inflater = LayoutInflater.from(activity);
  }

  public void setPlan(StoredCoachPlan stored) {
    container.removeAllViews();
    if (stored == null) {
      return;
    }
    List<String> undealable = stored.getPlan().undealableDrills(stored.getPayload());
    for (FocusArea area : stored.getPlan().getFocus()) {
      container.addView(areaCard(area, stored.getPayload(), undealable));
    }
  }

  /** The standing practice sets, one outlined card per family, into their own container. */
  public void setPractice(LinearLayout into, List<CoachPractice.Set> sets) {
    into.removeAllViews();
    for (CoachPractice.Set set : sets) {
      into.addView(practiceCard(into, set));
    }
  }

  private View practiceCard(LinearLayout parent, final CoachPractice.Set set) {
    View card = inflater.inflate(R.layout.coach_practice_set, parent, false);
    ((TextView) card.findViewById(R.id.tvCoachPracticeTitle)).setText(activity.getString(
        R.string.coach_practice_set,
        Utils.toSmartCubeStepLocalizedName(activity, set.getFamily(), 0)));
    addCaseCharts((LinearLayout) card.findViewById(R.id.llCoachPracticeCases), set.getCases());
    Button button = card.findViewById(R.id.buCoachPracticeDrill);
    button.setText(set.getCases().size() == 1 ? R.string.coach_drill_case
        : R.string.coach_drill_cases);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        launch(set.getDrill());
      }
    });
    return card;
  }

  /** One small chart per case, each over its short name, sharing the row equally. */
  private void addCaseCharts(LinearLayout row, List<String> codes) {
    for (String code : codes) {
      View cell = inflater.inflate(R.layout.coach_practice_case, row, false);
      LastLayerDiagram diagram = LastLayerDiagram.forCase(code);
      LastLayerCaseView chart = cell.findViewById(R.id.vCoachPracticeChart);
      chart.setDiagram(diagram);
      chart.setVisibility(diagram == null ? View.INVISIBLE : View.VISIBLE);
      ((TextView) cell.findViewById(R.id.tvCoachPracticeName)).setText(shortName(code));
      row.addView(cell);
    }
  }

  /** A case as a cuber writes it under its chart: Rb, 21. */
  private static String shortName(String code) {
    String name = code.substring(code.indexOf('_') + 1);
    return name.isEmpty() ? name : name.substring(0, 1).toUpperCase(Locale.US) + name.substring(1);
  }

  private View areaCard(FocusArea area, CoachPayload payload, List<String> undealable) {
    View card = inflater.inflate(R.layout.coach_focus_area, container, false);
    ((TextView) card.findViewById(R.id.tvCoachAreaTitle)).setText(title(area));

    String text = area.getText() != null ? area.getText() : observation(area, payload);
    TextView tvText = card.findViewById(R.id.tvCoachAreaText);
    tvText.setText(text);
    tvText.setVisibility(text == null ? View.GONE : View.VISIBLE);

    LinearLayout cases = card.findViewById(R.id.llCoachAreaCases);
    for (String code : area.getCodes()) {
      if (payload.value("cases." + code + ".mean_ms") != null) {
        cases.addView(caseRow(cases, area, payload, code));
      } else if (payload.value("f2l_habits." + code + ".extra_moves") != null) {
        cases.addView(pairRow(cases, payload, code));
      }
    }

    DrillSpec drill = bindDrill(card, area, undealable);
    // A card about a step names no case, so the cases its drill deals are shown instead.
    if (drill != null && drill.getType() != DrillSpec.Type.CROSS && cases.getChildCount() == 0) {
      LinearLayout row = new LinearLayout(activity);
      row.setOrientation(LinearLayout.HORIZONTAL);
      row.setPadding(0, activity.getResources().getDimensionPixelSize(R.dimen.space_s), 0, 0);
      addCaseCharts(row, drill.getCases());
      cases.addView(row);
    }
    return card;
  }

  /**
   * A plan holds one area per family per kind of problem, since a drill cannot deal OLLs and PLLs
   * together, so the two that a solver has both of arrive as two cards saying the same words. The
   * family is what tells them apart, and it is put in the title rather than left to the charts.
   */
  private String title(FocusArea area) {
    if (area.getReason() == null) {
      return activity.getString(R.string.coach_reason_unknown);
    }
    switch (area.getReason()) {
      case SLOW_CASE:
        return familyTitle(area, R.string.coach_reason_slow_case);
      case SLOW_UNDER_PRESSURE:
        return familyTitle(area, R.string.coach_reason_slow_under_pressure);
      case RECOGNITION_HEAVY:
        return activity.getString(R.string.coach_reason_recognition_heavy);
      case TWO_LOOK_OLL:
        return activity.getString(R.string.coach_reason_two_look_oll);
      case SLOW_STEP:
        return familyTitle(area, R.string.coach_reason_slow_step);
      case STUCK_PIECES:
        return familyTitle(area, R.string.coach_reason_stuck_pieces);
      case SLOW_FIRST_PAIR:
        return familyTitle(area, R.string.coach_reason_slow_first_pair);
      case LONG_PAIR:
        return activity.getString(R.string.coach_reason_long_pair);
      case STALLING:
        return familyTitle(area, R.string.coach_reason_stalling);
      case UNEVEN_STEP:
        return familyTitle(area, R.string.coach_reason_uneven_step);
      default:
        // Not the consistency note: it ranks across the whole history and names no family of its own.
        return activity.getString(R.string.coach_reason_inconsistent_case);
    }
  }

  private String familyTitle(FocusArea area, int reason) {
    if (area.getCodes().isEmpty()) {
      return activity.getString(reason);
    }
    String family = MethodStatistics.familyOf(area.getCodes().get(0));
    return activity.getString(R.string.coach_area_family,
        Utils.toSmartCubeStepLocalizedName(activity, family, 0), activity.getString(reason));
  }

  /**
   * The sentence an area with no cases under it is made of, worked out from the payload. A case area
   * says what it means with its rows and gets none.
   */
  private String observation(FocusArea area, CoachPayload payload) {
    if (area.getReason() == FocusArea.Reason.RECOGNITION_HEAVY && !area.getCodes().isEmpty()) {
      String code = area.getCodes().get(0);
      Double recognition = payload.value("families." + code + ".recognition_ms");
      Double mean = payload.value("families." + code + ".mean_ms");
      if (recognition == null || mean == null) {
        return null;
      }
      return activity.getString(R.string.coach_recognition_body,
          Utils.toSmartCubeStepLocalizedName(activity, code, 0), time(recognition), time(mean));
    }
    if (area.getReason() == FocusArea.Reason.SLOW_STEP && !area.getCodes().isEmpty()) {
      String code = area.getCodes().get(0);
      StepShares shares = StepShares.of(payload);
      Double took = shares.actual(code);
      Double should = shares.expected(code);
      if (took == null || should == null) {
        return null;
      }
      return activity.getString(R.string.coach_step_share_body,
          Utils.toSmartCubeStepLocalizedName(activity, code, 0), percent(took), percent(should));
    }
    if (area.getReason() == FocusArea.Reason.TWO_LOOK_OLL && !area.getCodes().isEmpty()) {
      return twoLook(payload, area.getCodes().get(0));
    }
    if (area.getReason() == FocusArea.Reason.STUCK_PIECES) {
      Double share = payload.value("f2l_groups.f2l_stuck.share");
      Double stuck = payload.value("f2l_groups.f2l_stuck.mean_ms");
      Double basic = payload.value("f2l_groups.f2l_basic.mean_ms");
      return share == null || stuck == null || basic == null ? null
          : activity.getString(R.string.coach_stuck_body, percent(share), time(stuck), time(basic));
    }
    if (area.getReason() == FocusArea.Reason.SLOW_FIRST_PAIR) {
      Double first = payload.value("f2l_groups.f2l_first.recognition_ms");
      Double later = payload.value("f2l_groups.f2l_later.recognition_ms");
      return first == null || later == null ? null
          : activity.getString(R.string.coach_first_pair_body, time(first), time(later));
    }
    if (area.getReason() == FocusArea.Reason.STALLING && !area.getCodes().isEmpty()) {
      String code = area.getCodes().get(0);
      Double stalls = payload.value("turning." + code + ".stalls");
      Double stallMs = payload.value("turning." + code + ".stall_ms");
      Double mean = payload.value("families." + code + ".mean_ms");
      return stalls == null || stallMs == null || mean == null ? null
          : activity.getString(R.string.coach_stalling_body,
              Utils.toSmartCubeStepLocalizedName(activity, code, 0), moves(stalls), time(stallMs),
              time(mean));
    }
    if (area.getReason() == FocusArea.Reason.UNEVEN_STEP && !area.getCodes().isEmpty()) {
      String code = area.getCodes().get(0);
      Double mean = payload.value("families." + code + ".mean_ms");
      Double spread = payload.value("families." + code + ".std_dev_ms");
      Double best = payload.value("families." + code + ".best_ms");
      return mean == null || spread == null || best == null ? null
          : activity.getString(R.string.coach_uneven_body,
              Utils.toSmartCubeStepLocalizedName(activity, code, 0), time(mean), time(spread),
              time(best));
    }
    if (area.getReason() == FocusArea.Reason.LONG_PAIR) {
      Double solves = payload.value("f2l_moves.count");
      Double turned = payload.value("f2l_moves.turned_moves");
      Double usual = payload.value("f2l_moves.usual_moves");
      return solves == null || turned == null || usual == null ? null
          : activity.getString(R.string.coach_long_pair_body, solves.intValue(), moves(turned),
              moves(usual));
    }
    return null;
  }

  /**
   * What the last layer costs, how often it took two looks, and how many cases are behind that. The
   * three read as one paragraph and any of them may be missing, so they are joined rather than
   * formatted together.
   */
  private String twoLook(CoachPayload payload, String family) {
    StringBuilder said = new StringBuilder();
    StepShares shares = StepShares.of(payload);
    if (overBaseline(shares, family)) {
      said.append(activity.getString(R.string.coach_two_look_share,
          percent(shares.actual(family)), percent(shares.expected(family))));
    }
    Double looks = payload.value("two_look_count");
    Double solves = payload.value("windows.families");
    if (looks != null && solves != null) {
      append(said, activity.getString(R.string.coach_two_look_body, looks.intValue(),
          solves.intValue()));
    }
    Double toLearn = payload.value("to_learn_cases.count");
    if (toLearn != null && toLearn.intValue() > 0) {
      append(said, activity.getString(R.string.coach_two_look_to_learn, toLearn.intValue()));
    }
    return said.length() == 0 ? null : said.toString();
  }

  private static void append(StringBuilder said, String sentence) {
    if (said.length() > 0) {
      said.append(' ');
    }
    said.append(sentence);
  }

  private View caseRow(LinearLayout parent, FocusArea area, CoachPayload payload, String code) {
    View row = inflater.inflate(R.layout.coach_case_row, parent, false);

    LastLayerDiagram diagram = LastLayerDiagram.forCase(code);
    LastLayerCaseView chart = row.findViewById(R.id.vCoachCaseChart);
    chart.setDiagram(diagram);
    chart.setVisibility(diagram == null ? View.GONE : View.VISIBLE);

    ((TextView) row.findViewById(R.id.tvCoachCaseName))
        .setText(Utils.toSmartCubeCaseHeadline(activity, code));
    fill(row, R.id.tvCoachCaseFigures, figures(area, payload, code));
    fill(row, R.id.tvCoachCaseCost, cost(area, payload, code));
    return row;
  }

  /**
   * An F2L case, named by the algorithm its extra moves are counted over and never by its number,
   * which is the site's and not something a solver knows it by. The button shows the case itself.
   */
  private View pairRow(LinearLayout parent, CoachPayload payload, final String code) {
    View row = inflater.inflate(R.layout.coach_pair_row, parent, false);
    fill(row, R.id.tvCoachCaseName, CaseAlgorithms.shortestListed(code));
    Double extra = payload.value("f2l_habits." + code + ".extra_moves");
    fill(row, R.id.tvCoachCaseFigures,
        activity.getString(R.string.coach_pair_extra, moves(extra)));
    Double count = payload.value("f2l_habits." + code + ".count");
    Double offList = payload.value("f2l_habits." + code + ".off_list_rate");
    fill(row, R.id.tvCoachCaseCost, count == null || offList == null ? null
        : activity.getString(R.string.coach_pair_off_list, count.intValue(), rounded(offList)));
    row.findViewById(R.id.buCoachPairCase).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogUtils.showFragment((FragmentActivity) activity,
            CaseAlgorithmsDialog.newInstance(code));
      }
    });
    return row;
  }

  /** What the case costs, said the way its reason wants it said. */
  private String figures(FocusArea area, CoachPayload payload, String code) {
    if (area.getReason() == FocusArea.Reason.SLOW_UNDER_PRESSURE) {
      Double drilled = payload.value("drill_cases." + code + ".mean_ms");
      Double gap = payload.value("comparisons." + code + ".gap_ms");
      if (drilled != null && gap != null) {
        return activity.getString(R.string.coach_case_gap, time(drilled), time(gap));
      }
    }
    Double mean = payload.value("cases." + code + ".mean_ms");
    Double family = payload.value("cases." + code + ".family_mean_ms");
    return mean == null || family == null ? null
        : activity.getString(R.string.coach_case_mean, time(mean), time(family));
  }

  /** The second line: how often, and what that has added up to. */
  private String cost(FocusArea area, CoachPayload payload, String code) {
    if (area.getReason() == FocusArea.Reason.INCONSISTENT_CASE) {
      Double rate = payload.value("cases." + code + ".rejection_rate");
      return rate == null ? null : activity.getString(R.string.coach_case_rejected,
          (int) Math.round(rate.doubleValue() * 100));
    }
    Double count = payload.value("cases." + code + ".count");
    Double lost = payload.value("cases." + code + ".time_lost_ms");
    return count == null || lost == null ? null
        : activity.getString(R.string.coach_case_cost, count.intValue(), time(lost));
  }

  private static void fill(View row, int id, String text) {
    TextView view = row.findViewById(id);
    view.setText(text);
    view.setVisibility(text == null ? View.GONE : View.VISIBLE);
  }

  /**
   * A drill written by a version newer than this app is one area that cannot be practised, not a
   * plan that cannot be read, so the spec is parsed here rather than when the plan was.
   *
   * @return the drill whose button is shown, or null where there is no button
   */
  private DrillSpec bindDrill(View card, FocusArea area, List<String> undealable) {
    Button button = card.findViewById(R.id.buCoachAreaDrill);
    if (area.getDrill() == null) {
      return null;
    }
    final DrillSpec spec;
    try {
      spec = DrillSpec.fromJson(area.getDrill());
    } catch (IllegalArgumentException e) {
      return null;
    }
    // A button over a case nothing can deal opens a drill with nothing in it.
    for (String code : spec.getCases()) {
      if (undealable.contains(code)) {
        return null;
      }
    }
    button.setVisibility(View.VISIBLE);
    // A cross drill is named by no case at all, so counting its case list would label it "these cases".
    button.setText(spec.getType() == DrillSpec.Type.CROSS ? R.string.drill_cross_title
        : spec.getCases().size() == 1 ? R.string.coach_drill_case : R.string.coach_drill_cases);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        launch(spec);
      }
    });
    return spec;
  }

  /**
   * Straight into the runner rather than through the setup screen: the plan already chose the cases
   * and the count, and the only thing left to ask is which way up the cube is held, which is the
   * user's own standing answer. Recorded, since a prescribed drill's reps are what the next plan
   * reads its solve-against-drill gap from.
   */
  private void launch(DrillSpec spec) {
    Intent intent;
    if (spec.getType() == DrillSpec.Type.CROSS) {
      // The coach cannot know the solver's cross face; the setup screen's standing answer is it.
      String face = DrillSetupActivity.layerFace().opposite().name();
      intent = new Intent(activity, CrossDrillActivity.class);
      intent.putExtra(CrossDrillActivity.EXTRA_SPEC, DrillSpec.cross(spec.getId(), face,
          spec.getReps(), spec.getPlanningMs(), spec.getLabel()).toJson());
    } else {
      intent = new Intent(activity, DrillActivity.class);
      intent.putExtra(DrillActivity.EXTRA_SPEC, spec.toJson());
      intent.putExtra(DrillActivity.EXTRA_LAYER_FACE, DrillSetupActivity.layerFace().name());
    }
    intent.putExtra(DrillScreenActivity.EXTRA_RECORDING, true);
    activity.startActivity(intent);
  }

  /** A mean of moves, to the tenth the payload carries. */
  private static String moves(Double moves) {
    return String.format(Locale.getDefault(), "%.1f", moves.doubleValue());
  }

  private String time(Double ms) {
    return FormatterService.INSTANCE.formatSolveTime(Long.valueOf(Math.round(ms.doubleValue())));
  }

  /**
   * Whether the last layer is worth pricing, which is not the same question as whether it is taken
   * twice: a solver can two-look half his solves and still land under the one-look baseline. Saying
   * what it costs where it costs nothing reads as an accusation the figure beside it disproves.
   *
   * <p>The two shares must differ once rounded, since the sentence prints them rounded and "16%
   * where 16% is usual" says nothing at all.
   */
  private static boolean overBaseline(StepShares shares, String family) {
    Double took = shares.actual(family);
    Double should = shares.expected(family);
    return took != null && should != null && rounded(took) > rounded(should);
  }

  private static int rounded(Double share) {
    return (int) Math.round(share.doubleValue() * 100);
  }

  private String percent(Double share) {
    return activity.getString(R.string.coach_percent, Integer.valueOf(rounded(share)));
  }
}
