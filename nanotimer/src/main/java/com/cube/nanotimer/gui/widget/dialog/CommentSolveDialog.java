package com.cube.nanotimer.gui.widget.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.HistoryDetailDialog;
import com.cube.nanotimer.gui.widget.TimeChangedHandler;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;

public class CommentSolveDialog extends ConfirmDialog {

  private EditText tfComment;

  private static final String ARG_SOLVE_TIME = "solveType";

  private TimeChangedHandler handler;

  public static CommentSolveDialog newInstance(SolveTime solveTime, TimeChangedHandler handler) {
    CommentSolveDialog frag = new CommentSolveDialog();
    frag.handler = handler;

    Bundle args = new Bundle();
    args.putSerializable(ARG_SOLVE_TIME, solveTime);
    frag.setArguments(args);

    return frag;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    dialog = getDialog(R.string.confirm);
    return dialog;
  }

  @Override
  protected void onConfirm() {
    Bundle args = getArguments();
    SolveTime solveTime = (SolveTime) args.getSerializable(ARG_SOLVE_TIME);
    solveTime.setComment(tfComment.getText().toString());

    App.INSTANCE.getService().saveTime(solveTime, new DataCallback<SolveAverages>() {
      @Override
      public void onData(SolveAverages data) {
        if (handler != null) {
          handler.onTimeChanged(solveTime);
        }
      }
    });

    dialog.dismiss();
  }

  @Override
  protected View getCustomView() {
    LayoutInflater factory = LayoutInflater.from(getActivity());
    View view = factory.inflate(R.layout.comment_solve, null);
    tfComment = view.findViewById(R.id.tfComment);

    Bundle args = getArguments();
    SolveTime solveTime = (SolveTime) args.getSerializable(ARG_SOLVE_TIME);

    if (solveTime.getComment() != null) {
      tfComment.setText(solveTime.getComment());
    }

    return view;
  }

}
