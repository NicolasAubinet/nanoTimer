package com.cube.nanotimer.coach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The last plan without its text, the step figures it was written from, and the same figures over
 * the solves done since. Sent only for the same method and once enough solves have followed
 * ({@link CoachPayloadBuilder#SINCE_FLOOR}), so a first plan carries none.
 */
public class PreviousPlan {

  /**
   * The step layer over some solves: the whole solve, each step, the F2L groups and the turning.
   * No cases, which a few dozen solves cannot support.
   */
  public static class Steps {

    private final int solves;
    private final Integer twoLookCount;
    private final StepFigure solve;
    private final List<StepFigure> families;
    private final List<StepFigure> f2lGroups;
    private final List<TurningFigure> turning;

    /** @param solves how many solves the figures were read from */
    public Steps(int solves, Integer twoLookCount, StepFigure solve, List<StepFigure> families,
        List<StepFigure> f2lGroups, List<TurningFigure> turning) {
      this.solves = solves;
      this.twoLookCount = twoLookCount;
      this.solve = solve;
      this.families = figures(families);
      this.f2lGroups = figures(f2lGroups);
      this.turning = turning == null ? Collections.<TurningFigure>emptyList()
          : Collections.unmodifiableList(new ArrayList<TurningFigure>(turning));
    }

    public int getSolves() {
      return solves;
    }

    public List<StepFigure> getFamilies() {
      return families;
    }

    public List<TurningFigure> getTurning() {
      return turning;
    }

    /** The figure at a payload path, for the paths of the step layer; null for any other. */
    Double value(String path) {
      if ("two_look_count".equals(path)) {
        return twoLookCount == null ? null : Double.valueOf(twoLookCount.intValue());
      }
      if ("windows.families".equals(path)) {
        return Double.valueOf(solves);
      }
      String[] segments = path.split("\\.");
      if (segments.length == 2 && "solve".equals(segments[0])) {
        return solve == null ? null : CoachPayload.field(solve, segments[1]);
      }
      if (segments.length != 3) {
        return null;
      }
      if ("turning".equals(segments[0])) {
        for (TurningFigure figure : turning) {
          if (figure.getCode().equals(segments[1])) {
            return figure.field(segments[2]);
          }
        }
        return null;
      }
      List<StepFigure> list = "families".equals(segments[0]) ? families
          : "f2l_groups".equals(segments[0]) ? f2lGroups : Collections.<StepFigure>emptyList();
      for (StepFigure figure : list) {
        if (figure.getCode().equals(segments[1])) {
          return CoachPayload.field(figure, segments[2]);
        }
      }
      return null;
    }

    JSONObject toJson() throws JSONException {
      JSONObject json = new JSONObject();
      json.put("solves", solves);
      if (twoLookCount != null) {
        json.put("two_look_count", twoLookCount.intValue());
      }
      if (solve != null) {
        json.put("solve", solve.toJson());
      }
      json.put("families", array(families));
      json.put("f2l_groups", array(f2lGroups));
      JSONArray turns = new JSONArray();
      for (TurningFigure figure : turning) {
        turns.put(figure.toJson());
      }
      json.put("turning", turns);
      return json;
    }

    static Steps fromJson(JSONObject json) throws JSONException {
      if (json == null) {
        return new Steps(0, null, null, null, null, null);
      }
      List<TurningFigure> turning = new ArrayList<TurningFigure>();
      JSONArray turns = json.optJSONArray("turning");
      for (int i = 0; turns != null && i < turns.length(); i++) {
        turning.add(TurningFigure.fromJson(turns.getJSONObject(i)));
      }
      return new Steps(json.optInt("solves"),
          json.has("two_look_count") ? Integer.valueOf(json.getInt("two_look_count")) : null,
          json.has("solve") ? StepFigure.fromJson(json.getJSONObject("solve")) : null,
          stepFigures(json.optJSONArray("families")), stepFigures(json.optJSONArray("f2l_groups")),
          turning);
    }
  }

  /** One card of the last plan: what it named, the figures it cited, and what was drilled since. */
  public static class Card {

    private final String reasonCode;
    private final List<String> codes;
    private final List<Evidence> evidence;
    private final int run;
    private final String drillType;
    private final List<String> drillCases;
    private final Integer drillReps;

    /**
     * @param run how many plans in a row have carried this card, the last one included
     * @param drillType the drill's type code, null for a card that had none
     * @param drillReps reps done since on the drill's cases, or cross reps for a cross drill; null
     *     with no drill
     */
    public Card(String reasonCode, List<String> codes, List<Evidence> evidence, int run,
        String drillType, List<String> drillCases, Integer drillReps) {
      this.reasonCode = reasonCode;
      this.codes = Collections.unmodifiableList(new ArrayList<String>(codes));
      this.evidence = Collections.unmodifiableList(new ArrayList<Evidence>(evidence));
      this.run = run;
      this.drillType = drillType;
      this.drillCases = drillType == null || drillCases == null
          ? Collections.<String>emptyList()
          : Collections.unmodifiableList(new ArrayList<String>(drillCases));
      this.drillReps = drillType == null ? null : drillReps;
    }

    /** A card of the last plan without its text, its drill cut down to its type and cases. */
    static Card of(FocusArea area, int run, Integer drillReps) {
      String drillType = null;
      List<String> drillCases = new ArrayList<String>();
      if (area.getDrill() != null) {
        try {
          // Read as text, not as a DrillSpec: a spec newer than this app is still a drill given.
          JSONObject drill = new JSONObject(area.getDrill());
          drillType = drill.optString("type", null);
          drillCases = strings(drill.optJSONArray("cases"));
        } catch (JSONException e) {
          drillType = null;
        }
      }
      // What that plan cited of the plan before it is two plans old: only its own figures ride.
      List<Evidence> evidence = new ArrayList<Evidence>();
      for (Evidence cited : area.getEvidence()) {
        if (!cited.getPath().startsWith("previous.")) {
          evidence.add(cited);
        }
      }
      return new Card(area.getReasonCode(), area.getCodes(), evidence, run, drillType, drillCases,
          drillReps);
    }

    public String getReasonCode() {
      return reasonCode;
    }

    public List<String> getCodes() {
      return codes;
    }

    /** The figures as they stood when that plan was written: the before of any before and after. */
    public List<Evidence> getEvidence() {
      return evidence;
    }

    public int getRun() {
      return run;
    }

    public String getDrillType() {
      return drillType;
    }

    public List<String> getDrillCases() {
      return drillCases;
    }

    public Integer getDrillReps() {
      return drillReps;
    }

    /** The same advice: one reason about the same steps or cases. */
    boolean matches(String reasonCode, List<String> codes) {
      return this.reasonCode.equals(reasonCode) && this.codes.size() == codes.size()
          && this.codes.containsAll(codes);
    }

    JSONObject toJson() throws JSONException {
      JSONObject json = new JSONObject();
      json.put("reason", reasonCode);
      json.put("codes", new JSONArray(codes));
      json.put("run", run);
      JSONArray cited = new JSONArray();
      for (Evidence one : evidence) {
        cited.put(one.toJson());
      }
      json.put("evidence", cited);
      if (drillType != null) {
        JSONObject drill = new JSONObject();
        drill.put("type", drillType);
        drill.put("cases", new JSONArray(drillCases));
        json.put("drill", drill);
        if (drillReps != null) {
          json.put("drill_reps", drillReps.intValue());
        }
      }
      return json;
    }

    static Card fromJson(JSONObject json) throws JSONException {
      List<Evidence> evidence = new ArrayList<Evidence>();
      JSONArray cited = json.optJSONArray("evidence");
      for (int i = 0; cited != null && i < cited.length(); i++) {
        evidence.add(Evidence.fromJson(cited.getJSONObject(i)));
      }
      JSONObject drill = json.optJSONObject("drill");
      return new Card(json.getString("reason"), strings(json.optJSONArray("codes")), evidence,
          json.optInt("run", 1), drill == null ? null : drill.getString("type"),
          drill == null ? null : strings(drill.optJSONArray("cases")),
          json.has("drill_reps") ? Integer.valueOf(json.getInt("drill_reps")) : null);
    }
  }

  private final int solvesSince;
  private final Steps before;
  private final Steps since;
  private final List<Card> cards;

  /**
   * @param solvesSince how many solves followed the last plan
   * @param before the step layer the last plan was written from
   * @param since the same over the solves done since, the newest of them where there are many
   */
  public PreviousPlan(int solvesSince, Steps before, Steps since, List<Card> cards) {
    this.solvesSince = solvesSince;
    this.before = before;
    this.since = since;
    this.cards = cards == null ? Collections.<Card>emptyList()
        : Collections.unmodifiableList(new ArrayList<Card>(cards));
  }

  public int getSolvesSince() {
    return solvesSince;
  }

  public Steps getBefore() {
    return before;
  }

  public Steps getSince() {
    return since;
  }

  /** The last plan's cards, in its order. */
  public List<Card> getCards() {
    return cards;
  }

  /** The last plan's card giving this advice, or null. */
  public Card card(String reasonCode, List<String> codes) {
    for (Card card : cards) {
      if (card.matches(reasonCode, codes)) {
        return card;
      }
    }
    return null;
  }

  /**
   * The figure at a path below {@code previous.}: {@code solves_since};
   * {@code was.<path>}, the figure at a payload path as the last plan cited it or as its step
   * layer held it; {@code since.<path>}, the same over the solves since, step layer only; and
   * {@code cards.1.run} or {@code cards.1.drill_reps}, counted from one.
   */
  Double value(String path) {
    if ("solves_since".equals(path)) {
      return Double.valueOf(solvesSince);
    }
    if (path.startsWith("was.")) {
      return was(path.substring("was.".length()));
    }
    if (path.startsWith("since.")) {
      return since.value(path.substring("since.".length()));
    }
    String[] segments = path.split("\\.");
    return segments.length == 3 && "cards".equals(segments[0])
        ? cardField(segments[1], segments[2]) : null;
  }

  private Double was(String path) {
    for (Card card : cards) {
      for (Evidence cited : card.getEvidence()) {
        if (cited.getPath().equals(path)) {
          return Double.valueOf(cited.getValue());
        }
      }
    }
    return before.value(path);
  }

  private Double cardField(String number, String field) {
    int index;
    try {
      index = Integer.parseInt(number) - 1;
    } catch (NumberFormatException e) {
      return null;
    }
    if (index < 0 || index >= cards.size()) {
      return null;
    }
    Card card = cards.get(index);
    if ("run".equals(field)) {
      return Double.valueOf(card.getRun());
    }
    return "drill_reps".equals(field) && card.getDrillReps() != null
        ? Double.valueOf(card.getDrillReps().intValue()) : null;
  }

  JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("solves_since", solvesSince);
    json.put("before", before.toJson());
    json.put("since", since.toJson());
    JSONArray written = new JSONArray();
    for (Card card : cards) {
      written.put(card.toJson());
    }
    json.put("cards", written);
    return json;
  }

  static PreviousPlan fromJson(JSONObject json) throws JSONException {
    List<Card> cards = new ArrayList<Card>();
    JSONArray written = json.optJSONArray("cards");
    for (int i = 0; written != null && i < written.length(); i++) {
      cards.add(Card.fromJson(written.getJSONObject(i)));
    }
    return new PreviousPlan(json.getInt("solves_since"),
        Steps.fromJson(json.optJSONObject("before")), Steps.fromJson(json.optJSONObject("since")),
        cards);
  }

  private static List<StepFigure> figures(List<StepFigure> figures) {
    return figures == null ? Collections.<StepFigure>emptyList()
        : Collections.unmodifiableList(new ArrayList<StepFigure>(figures));
  }

  private static JSONArray array(List<StepFigure> figures) throws JSONException {
    JSONArray array = new JSONArray();
    for (StepFigure figure : figures) {
      array.put(figure.toJson());
    }
    return array;
  }

  private static List<StepFigure> stepFigures(JSONArray array) throws JSONException {
    List<StepFigure> figures = new ArrayList<StepFigure>();
    for (int i = 0; array != null && i < array.length(); i++) {
      figures.add(StepFigure.fromJson(array.getJSONObject(i)));
    }
    return figures;
  }

  private static List<String> strings(JSONArray array) throws JSONException {
    List<String> strings = new ArrayList<String>();
    for (int i = 0; array != null && i < array.length(); i++) {
      strings.add(array.getString(i));
    }
    return strings;
  }
}
