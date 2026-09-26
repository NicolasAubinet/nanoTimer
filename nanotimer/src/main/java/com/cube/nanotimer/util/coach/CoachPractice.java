package com.cube.nanotimer.util.coach;

import android.os.Handler;
import android.os.Looper;

import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.coach.CoachPayload;
import com.cube.nanotimer.coach.PairExecution;
import com.cube.nanotimer.coach.StepTurning;
import com.cube.nanotimer.drill.DrillSpec;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The practice sets under a plan: per family, the cases costing the solver the most, chosen by the
 * coach service with no model behind it. Cached as the service sent them, so the section draws at
 * once and a failed refresh keeps the last answer.
 */
public final class CoachPractice {

  /** One family's cases, costliest first, and the drill that runs them. */
  public static final class Set {
    private final String family;
    private final List<String> cases;
    private final DrillSpec drill;

    Set(String family, List<String> cases, DrillSpec drill) {
      this.family = family;
      this.cases = Collections.unmodifiableList(cases);
      this.drill = drill;
    }

    public String getFamily() {
      return family;
    }

    public List<String> getCases() {
      return cases;
    }

    public DrillSpec getDrill() {
      return drill;
    }
  }

  public interface Listener {
    /** On the main thread, only when the service answered. */
    void onPractice(List<Set> sets);
  }

  private CoachPractice() {
  }

  /** The sets last kept for this solve type, empty when there are none. */
  public static List<Set> kept(SolveType solveType) {
    return parse(Options.INSTANCE.getCoachPractice(solveType.getId()));
  }

  /**
   * Asks the service afresh. Only the case figures matter here, so the payload is built without the
   * replayed pairs and turnings a plan waits on.
   */
  public static void refresh(final CoachClient client, final SolveType solveType,
      CubeMethod method, final Listener listener) {
    final Handler main = new Handler(Looper.getMainLooper());
    App.INSTANCE.getService().getCoachPayload(solveType, method,
        Collections.<PairExecution>emptyList(), Collections.<StepTurning>emptyList(),
        new DataCallback<CoachPayload>() {
          @Override
          public void onData(final CoachPayload payload) {
            new Thread(new Runnable() {
              @Override
              public void run() {
                String reply = client.requestPractice(CoachClient.requestBody(payload, null));
                final List<Set> sets = parse(reply);
                if (reply == null || (sets.isEmpty() && !answersEmpty(reply))) {
                  return;
                }
                Options.INSTANCE.setCoachPractice(solveType.getId(), reply);
                main.post(new Runnable() {
                  @Override
                  public void run() {
                    listener.onPractice(sets);
                  }
                });
              }
            }).start();
          }
        });
  }

  /** A set whose drill this app cannot run is left out rather than drawn with a dead button. */
  static List<Set> parse(String reply) {
    List<Set> sets = new ArrayList<Set>();
    if (reply == null) {
      return sets;
    }
    try {
      JSONArray array = new JSONObject(reply).getJSONArray("sets");
      for (int i = 0; i < array.length(); i++) {
        JSONObject set = array.getJSONObject(i);
        DrillSpec drill;
        try {
          drill = DrillSpec.fromJson(set.getJSONObject("drill").toString());
        } catch (IllegalArgumentException e) {
          continue;
        }
        sets.add(new Set(set.getString("family"), drill.getCases(), drill));
      }
    } catch (JSONException e) {
      return new ArrayList<Set>();
    }
    return sets;
  }

  /** Nothing to practise is an answer worth keeping; text that is no answer at all is not. */
  private static boolean answersEmpty(String reply) {
    try {
      return new JSONObject(reply).getJSONArray("sets").length() == 0;
    } catch (JSONException e) {
      return false;
    }
  }
}
