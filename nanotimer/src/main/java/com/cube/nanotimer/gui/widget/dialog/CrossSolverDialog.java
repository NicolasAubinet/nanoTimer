package com.cube.nanotimer.gui.widget.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.cube.nanotimer.Options;
import com.cube.nanotimer.Options.CrossNeutrality;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.NanoTimerDialogFragment;
import com.cube.nanotimer.scrambler.cross.CrossFace;
import com.cube.nanotimer.scrambler.cross.CrossFormatter;
import com.cube.nanotimer.scrambler.cross.CrossSolvers;
import com.cube.nanotimer.scrambler.cross.CrossSolvers.FaceSolutions;

import java.util.List;

/**
 * Shows the optimal CFOP cross solution(s) for the current 3x3 scramble. Read-only study aid; see
 * the cross solver plan. The BFS lookup is sub-millisecond but the per-face tables are built lazily
 * on a worker thread, so solving runs off the UI thread.
 */
public class CrossSolverDialog extends NanoTimerDialogFragment {

  private static final String ARG_SCRAMBLE = "scramble";

  // How many optimal solutions to list per face (the set can be large); the UI notes truncation.
  private static final int MAX_SOLUTIONS_SHOWN = 12;

  private String scramble;
  private final CrossSolvers solvers = new CrossSolvers();

  private Spinner spMode;
  private Spinner spFace;
  private LinearLayout llFace;
  private LinearLayout llResults;

  // Increments on each solve request so stale worker results (after a quick spinner change) are ignored.
  private int requestId;

  public static CrossSolverDialog newInstance(String scramble) {
    CrossSolverDialog frag = new CrossSolverDialog();
    Bundle args = new Bundle();
    args.putString(ARG_SCRAMBLE, scramble);
    frag.setArguments(args);
    return frag;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    scramble = getArguments().getString(ARG_SCRAMBLE);

    LayoutInflater inflater = LayoutInflater.from(getActivity());
    View view = inflater.inflate(R.layout.crosssolver_dialog, null);

    spMode = view.findViewById(R.id.spCrossMode);
    spFace = view.findViewById(R.id.spCrossFace);
    llFace = view.findViewById(R.id.llCrossFace);
    llResults = view.findViewById(R.id.llCrossResults);

    setUpModeSpinner();
    setUpFaceSpinner();
    llFace.setVisibility(
        Options.INSTANCE.getCrossNeutrality() == CrossNeutrality.FULL ? View.GONE : View.VISIBLE);

    AlertDialog dialog = new AlertDialog.Builder(getActivity())
        .setTitle(R.string.cross_solver)
        .setView(view)
        .setPositiveButton(R.string.close, null)
        .create();

    solve();
    return dialog;
  }

  private void setUpModeSpinner() {
    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
        android.R.layout.simple_spinner_item, new String[] {
            getString(R.string.cross_neutrality_specific),
            getString(R.string.cross_neutrality_dual),
            getString(R.string.cross_neutrality_full) });
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spMode.setAdapter(adapter);
    spMode.setSelection(Options.INSTANCE.getCrossNeutrality().ordinal());
    spMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        CrossNeutrality mode = CrossNeutrality.values()[position];
        Options.INSTANCE.setCrossNeutrality(mode);
        // The face only matters for Specific/Dual.
        llFace.setVisibility(mode == CrossNeutrality.FULL ? View.GONE : View.VISIBLE);
        solve();
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  private void setUpFaceSpinner() {
    CrossFace[] faces = CrossFace.values();
    String[] faceNames = new String[faces.length];
    for (int i = 0; i < faces.length; i++) {
      faceNames[i] = faces[i].name();
    }
    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
        android.R.layout.simple_spinner_item, faceNames);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spFace.setAdapter(adapter);
    spFace.setSelection(Options.INSTANCE.getCrossFaceIndex(CrossFace.D.ordinal()));
    spFace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        Options.INSTANCE.setCrossFaceIndex(position);
        solve();
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  private void solve() {
    final CrossNeutrality mode = CrossNeutrality.values()[spMode.getSelectedItemPosition()];
    final CrossFace face = CrossFace.values()[spFace.getSelectedItemPosition()];
    final int currentRequest = ++requestId;

    llResults.removeAllViews();
    llResults.addView(makeTextView(getString(R.string.cross_solving), false));

    new Thread(new Runnable() {
      @Override
      public void run() {
        final List<FaceSolutions> results = solvers.solve(scramble, mode, face);
        if (getActivity() == null) {
          return;
        }
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (currentRequest == requestId && isAdded()) {
              render(mode, results);
            }
          }
        });
      }
    }).start();
  }

  private void render(CrossNeutrality mode, List<FaceSolutions> results) {
    llResults.removeAllViews();
    if (mode == CrossNeutrality.SPECIFIC) {
      renderFaceSolutions(results.get(0), true);
    } else {
      for (final FaceSolutions fs : results) {
        renderCollapsibleFace(fs);
      }
    }
  }

  // Specific mode: a header then every optimal solution (capped).
  private void renderFaceSolutions(FaceSolutions fs, boolean expanded) {
    llResults.addView(makeTextView(faceHeader(fs), true));
    if (expanded) {
      addSolutionLines(fs);
    }
  }

  // Dual/Full mode: one tappable row per face; tapping expands its solution list.
  private void renderCollapsibleFace(final FaceSolutions fs) {
    final TextView header = makeTextView(faceHeader(fs), true);
    final LinearLayout solutionsContainer = new LinearLayout(getActivity());
    solutionsContainer.setOrientation(LinearLayout.VERTICAL);
    solutionsContainer.setVisibility(View.GONE);

    header.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (solutionsContainer.getChildCount() == 0) {
          fillSolutionLines(solutionsContainer, fs);
        }
        solutionsContainer.setVisibility(
            solutionsContainer.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
      }
    });

    llResults.addView(header);
    llResults.addView(solutionsContainer);
  }

  private void addSolutionLines(FaceSolutions fs) {
    fillSolutionLines(llResults, fs);
  }

  private void fillSolutionLines(ViewGroup container, FaceSolutions fs) {
    int shown = Math.min(fs.solutions.size(), MAX_SOLUTIONS_SHOWN);
    for (int i = 0; i < shown; i++) {
      String[] formatted = CrossFormatter.toCrossOnBottom(fs.face, fs.solutions.get(i));
      container.addView(makeTextView(joinMoves(formatted), false));
    }
    if (fs.solutions.size() > shown) {
      container.addView(makeTextView(
          getString(R.string.cross_more_solutions, fs.solutions.size() - shown), false));
    }
  }

  private String faceHeader(FaceSolutions fs) {
    if (fs.solutions.isEmpty() || fs.length == 0) {
      return getString(R.string.cross_face_already_solved, fs.face.name());
    }
    return getString(R.string.cross_face_summary, fs.face.name(), fs.length, fs.solutions.size());
  }

  private static String joinMoves(String[] moves) {
    StringBuilder sb = new StringBuilder();
    for (String m : moves) {
      if (m.isEmpty()) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(m);
    }
    return sb.toString();
  }

  private TextView makeTextView(String text, boolean header) {
    TextView tv = new TextView(getActivity());
    tv.setText(text);
    tv.setTextSize(header ? 16 : 15);
    int pad = (int) (4 * getResources().getDisplayMetrics().density);
    tv.setPadding(0, pad, 0, pad);
    return tv;
  }
}
