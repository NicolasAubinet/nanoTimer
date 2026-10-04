package com.cube.nanotimer.util.coach;

import android.content.Context;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.R;
import com.cube.nanotimer.coach.CoachPayload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Asks the coach service for a plan. The one place the app talks to the network.
 *
 * <p>The address and token come from {@code coach.url} and {@code coach.token} in
 * {@code local.properties}, so a build made without them has no coach to ask.
 */
public final class CoachClient {

  private static final int CONNECT_TIMEOUT_MS = 15_000;
  // A model call takes tens of seconds, and the service answers only once it is written.
  private static final int READ_TIMEOUT_MS = 120_000;

  // No model behind it, so it answers at once or not at all.
  private static final int PRACTICE_READ_TIMEOUT_MS = 15_000;

  private final String planUrl;
  private final String practiceUrl;
  private final String token;
  private final String coachId;

  CoachClient(String baseUrl, String token, String coachId) {
    String base = baseUrl.replaceAll("/+$", "");
    this.planUrl = base + "/v1/plan";
    this.practiceUrl = base + "/v1/practice";
    this.token = token;
    this.coachId = coachId;
  }

  /** The client this build was configured with, or null when it was built without a coach. */
  public static CoachClient configured(Context context) {
    String url = context.getString(R.string.coach_url);
    String token = context.getString(R.string.coach_token);
    return url.isEmpty() || token.isEmpty() ? null
        : new CoachClient(url, token, Options.INSTANCE.getCoachId());
  }

  /**
   * Blocks until the service answers or gives up, so never call it on the main thread.
   *
   * @param requestBody what {@link #requestBody} wrote, sent as it was kept: the service knows a
   *     repeated question by the payload's exact text
   */
  public CoachReply requestPlan(String requestBody) {
    try {
      Answer answer = post(planUrl, requestBody, READ_TIMEOUT_MS);
      return answer.body == null ? CoachReply.unreadable()
          : CoachReply.parse(answer.status, answer.body);
    } catch (IOException e) {
      return CoachReply.unreachable();
    }
  }

  /**
   * The standing practice sets for a payload, as the service sent them, or null for any failure:
   * the section keeps what it last had. Blocks, like {@link #requestPlan}.
   */
  public String requestPractice(String requestBody) {
    try {
      Answer answer = post(practiceUrl, requestBody, PRACTICE_READ_TIMEOUT_MS);
      return answer.status == 200 ? answer.body : null;
    } catch (IOException e) {
      return null;
    }
  }

  private static final class Answer {
    final int status;
    final String body;

    Answer(int status, String body) {
      this.status = status;
      this.body = body;
    }
  }

  private Answer post(String url, String requestBody, int readTimeoutMs) throws IOException {
    HttpURLConnection connection = null;
    try {
      byte[] body = requestBody.getBytes(StandardCharsets.UTF_8);
      connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
      connection.setReadTimeout(readTimeoutMs);
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setFixedLengthStreamingMode(body.length);
      connection.setRequestProperty("Authorization", "Bearer " + token);
      connection.setRequestProperty("Coach-Id", coachId);
      connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
      try (OutputStream out = connection.getOutputStream()) {
        out.write(body);
      }
      int status = connection.getResponseCode();
      InputStream in = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
      return new Answer(status, in == null ? null : read(in));
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  /** The payload a request body carries, which is what a plan that answers it is kept beside. */
  static CoachPayload payloadOf(String requestBody) {
    try {
      return CoachPayload.parse(new JSONObject(requestBody).getJSONObject("payload").toString());
    } catch (JSONException e) {
      throw new IllegalArgumentException("Not a coach request: " + e.getMessage(), e);
    }
  }

  /** The stream rides beside the payload, never in it: the model is shown the payload alone. */
  static String requestBody(CoachPayload payload, String stream) {
    try {
      return new JSONObject()
          .put("payload", new JSONObject(payload.toJson()))
          .put("stream", stream)
          .toString();
    } catch (JSONException e) {
      throw new IllegalStateException(e); // toJson wrote it, so it parses
    }
  }

  private static String read(InputStream in) throws IOException {
    try (InputStream stream = in) {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      int count;
      while ((count = stream.read(buffer)) != -1) {
        bytes.write(buffer, 0, count);
      }
      return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }
  }
}
