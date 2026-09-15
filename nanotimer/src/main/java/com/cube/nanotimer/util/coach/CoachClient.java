package com.cube.nanotimer.util.coach;

import android.content.Context;
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

  private final String planUrl;
  private final String token;

  CoachClient(String baseUrl, String token) {
    this.planUrl = baseUrl.replaceAll("/+$", "") + "/v1/plan";
    this.token = token;
  }

  /** The client this build was configured with, or null when it was built without a coach. */
  public static CoachClient configured(Context context) {
    String url = context.getString(R.string.coach_url);
    String token = context.getString(R.string.coach_token);
    return url.isEmpty() || token.isEmpty() ? null : new CoachClient(url, token);
  }

  /**
   * Blocks until the service answers or gives up, so never call it on the main thread.
   *
   * @param requestBody what {@link #requestBody} wrote, sent as it was kept: the service knows a
   *     repeated question by the payload's exact text
   */
  public CoachReply requestPlan(String requestBody) {
    HttpURLConnection connection = null;
    try {
      byte[] body = requestBody.getBytes(StandardCharsets.UTF_8);
      connection = (HttpURLConnection) new URL(planUrl).openConnection();
      connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
      connection.setReadTimeout(READ_TIMEOUT_MS);
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setFixedLengthStreamingMode(body.length);
      connection.setRequestProperty("Authorization", "Bearer " + token);
      connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
      try (OutputStream out = connection.getOutputStream()) {
        out.write(body);
      }
      int status = connection.getResponseCode();
      InputStream in = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
      return in == null ? CoachReply.unreadable() : CoachReply.parse(status, read(in));
    } catch (IOException e) {
      return CoachReply.unreachable();
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
