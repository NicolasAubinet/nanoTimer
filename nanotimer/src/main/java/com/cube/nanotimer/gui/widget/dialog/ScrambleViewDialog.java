package com.cube.nanotimer.gui.widget.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.NanoTimerDialogFragment;

import org.json.JSONObject;

/**
 * On-demand 2D diagram of the current scramble's solved-into state, so cubers can
 * check they scrambled correctly. Hosts a transparent WebView that renders via the
 * vendored cubing.js bundle (see {@code assets/scramble/scramble.html} and
 * {@code ScrambleViewNotation}).
 *
 * <p>The page is served through {@link WebViewAssetLoader} from the secure
 * {@code https://appassets.androidplatform.net/} origin (not {@code file://}), and
 * the whole Java&lt;-&gt;JS surface is a single {@code ntRender(key, scramble)} call.
 * If the WebView is unavailable or the page fails to load, we fall back to showing
 * the scramble as text.</p>
 */
public class ScrambleViewDialog extends NanoTimerDialogFragment {

  private static final String ARG_KEY = "key";
  private static final String ARG_SCRAMBLE = "scramble";

  private static final String BASE_URL = "https://appassets.androidplatform.net/assets/scramble/scramble.html";

  private WebView webView;

  public static ScrambleViewDialog newInstance(String renderKey, String cubingScramble) {
    ScrambleViewDialog frag = new ScrambleViewDialog();
    Bundle args = new Bundle();
    args.putString(ARG_KEY, renderKey);
    args.putString(ARG_SCRAMBLE, cubingScramble);
    frag.setArguments(args);
    return frag;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final String key = getArguments().getString(ARG_KEY);
    final String scramble = getArguments().getString(ARG_SCRAMBLE);

    View view = LayoutInflater.from(getActivity()).inflate(R.layout.scrambleview_dialog, null);
    webView = view.findViewById(R.id.wvScramble);
    final TextView fallback = view.findViewById(R.id.tvScrambleFallback);

    boolean webViewReady = setupWebView(key, scramble, fallback);
    if (!webViewReady) {
      showFallback(fallback, scramble);
    }

    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.scramble_view)
        .setView(view)
        .setPositiveButton(R.string.close, null)
        .create();
  }

  private boolean setupWebView(final String key, final String scramble, final TextView fallback) {
    try {
      final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
          .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(getActivity()))
          .build();

      WebSettings settings = webView.getSettings();
      settings.setJavaScriptEnabled(true);

      // Blend onto the dialog's card background.
      webView.setBackgroundColor(Color.TRANSPARENT);

      webView.setWebViewClient(new WebViewClientCompat() {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest request) {
          return assetLoader.shouldInterceptRequest(request.getUrl());
        }

        @Override
        public void onPageFinished(WebView v, String url) {
          render(v, key, scramble);
        }

        @Override
        public void onReceivedError(WebView v, WebResourceRequest request,
            androidx.webkit.WebResourceErrorCompat error) {
          if (request.isForMainFrame()) {
            // The bundle/page itself failed to load — degrade to the text scramble.
            showFallback(fallback, scramble);
          }
        }
      });

      webView.loadUrl(BASE_URL);
      return true;
    } catch (Throwable t) {
      // e.g. no WebView implementation installed/updatable on this device.
      return false;
    }
  }

  // The entire Java->JS call: hand the renderer the puzzle key + scramble string.
  private void render(WebView v, String key, String scramble) {
    String js = "window.ntRender(" + JSONObject.quote(key) + "," + JSONObject.quote(scramble) + ");";
    v.evaluateJavascript(js, null);
  }

  private void showFallback(TextView fallback, String scramble) {
    if (webView != null) {
      webView.setVisibility(View.GONE);
    }
    fallback.setText(scramble);
    fallback.setVisibility(View.VISIBLE);
  }

  @Override
  public void onDestroyView() {
    if (webView != null) {
      webView.destroy();
      webView = null;
    }
    super.onDestroyView();
  }
}
