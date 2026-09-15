package com.cube.nanotimer.util.coach;

import android.os.Handler;
import android.os.Looper;

import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.coach.CoachPayload;
import com.cube.nanotimer.coach.CoachPayloadBuilder;
import com.cube.nanotimer.coach.StoredCoachPlan;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.SolveType;

import java.util.HashMap;
import java.util.Map;

/**
 * One solve type's request for a plan, which outlives the screen that asked: a model call takes
 * longer than a rotation, and the answer is kept whether or not anyone is still looking.
 *
 * <p><b>A request that got no answer is kept and sent again as it was, never rebuilt.</b> The phone
 * can give up after the service has already stored a plan, and only the same payload text is
 * answered from that store rather than bought a second time. A refusal says no plan was stored, so
 * it lets the request go and the next ask reads the history afresh.
 */
public final class CoachRead {

  public enum State {
    IDLE,
    READING,
    /** The last ask ended without a plan, and {@link #getFailure} says how. */
    FAILED
  }

  public interface Listener {
    /** Called on the main thread whenever the state moves, a plan having been saved first. */
    void onReadChanged(CoachRead read);
  }

  private static final Map<Integer, CoachRead> READS = new HashMap<Integer, CoachRead>();

  private final SolveType solveType;
  private final Handler main = new Handler(Looper.getMainLooper());
  private State state = State.IDLE;
  private CoachReply failure;
  /** When the service said to ask again, as a time rather than a wait: it is shown later. */
  private long askAgainAt;
  /** How many solves the method has been read from, or -1 when a resent request never counted. */
  private int solveCount = -1;
  private Listener listener;

  private CoachRead(SolveType solveType) {
    this.solveType = solveType;
  }

  /** Main thread only, like everything else here that is not a callback. */
  public static CoachRead of(SolveType solveType) {
    CoachRead read = READS.get(solveType.getId());
    if (read == null) {
      read = new CoachRead(solveType);
      READS.put(solveType.getId(), read);
    }
    return read;
  }

  public State getState() {
    return state;
  }

  /** How the last ask failed, only in {@link State#FAILED}. */
  public CoachReply getFailure() {
    return failure;
  }

  /** When the service said it could be asked again, 0 when it did not say. */
  public long getAskAgainAt() {
    return askAgainAt;
  }

  public int getSolveCount() {
    return solveCount;
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  /** Lets go of a screen on its way out, unless a newer one has taken its place. */
  public void removeListener(Listener listener) {
    if (this.listener == listener) {
      this.listener = null;
    }
  }

  /** Asks for a plan, sending again what went unanswered if anything did. */
  public void start(final CoachClient client, final CubeMethod method) {
    if (state == State.READING) {
      return;
    }
    moveTo(State.READING, null);
    String pending = Options.INSTANCE.getCoachPendingRequest(solveType.getId());
    if (pending != null) {
      send(client, pending);
      return;
    }
    App.INSTANCE.getService().getCoachSolveCount(solveType, method, new DataCallback<Integer>() {
      @Override
      public void onData(Integer count) {
        solveCount = count.intValue();
        // Below the floor every figure is withheld, so the answer is known without asking.
        if (solveCount < CoachPayloadBuilder.PLAN_FLOOR) {
          post(State.FAILED, CoachReply.refused(CoachRefusal.INSUFFICIENT_DATA));
        } else {
          ask(client, method);
        }
      }
    });
  }

  private void ask(final CoachClient client, CubeMethod method) {
    App.INSTANCE.getService().getCoachPayload(solveType, method, new DataCallback<CoachPayload>() {
      @Override
      public void onData(final CoachPayload payload) {
        App.INSTANCE.getService().getCoachStream(solveType, new DataCallback<String>() {
          @Override
          public void onData(String stream) {
            String body = CoachClient.requestBody(payload, stream);
            // Kept before it leaves, so a request the process dies waiting on is still the one resent.
            Options.INSTANCE.setCoachPendingRequest(solveType.getId(), body);
            send(client, body);
          }
        });
      }
    });
  }

  private void send(final CoachClient client, final String body) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        final CoachReply reply = client.requestPlan(body);
        switch (reply.getKind()) {
          case PLAN:
            StoredCoachPlan stored = new StoredCoachPlan(reply.getPlan(),
                CoachClient.payloadOf(body), System.currentTimeMillis());
            App.INSTANCE.getService().saveCoachPlan(solveType, stored, new DataCallback<Void>() {
              @Override
              public void onData(Void unused) {
                Options.INSTANCE.setCoachPendingRequest(solveType.getId(), null);
                post(State.IDLE, null);
              }
            });
            break;
          case REFUSED:
            Options.INSTANCE.setCoachPendingRequest(solveType.getId(), null);
            post(State.FAILED, reply);
            break;
          default:
            // No answer, or one this app cannot read: either way a plan may be sitting in the store.
            post(State.FAILED, reply);
            break;
        }
      }
    }).start();
  }

  private void post(final State state, final CoachReply failure) {
    main.post(new Runnable() {
      @Override
      public void run() {
        moveTo(state, failure);
      }
    });
  }

  private void moveTo(State state, CoachReply failure) {
    this.state = state;
    this.failure = failure;
    askAgainAt = failure == null || failure.getRetryAfterSeconds() == 0 ? 0
        : System.currentTimeMillis() + failure.getRetryAfterSeconds() * 1000L;
    if (listener != null) {
      listener.onReadChanged(this);
    }
  }
}
