package com.cube.nanotimer.gui.widget.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.cube.nanotimer.R;
import com.cube.nanotimer.util.coach.CoachClient;
import com.cube.nanotimer.util.coach.CoachCodeReply;
import com.cube.nanotimer.util.coach.CoachPlans;
import com.cube.nanotimer.util.coach.CoachRefusal;
import com.cube.nanotimer.util.helper.DialogUtils;

/**
 * Where an access code is typed in: the few installs the owner lets in for free get one from him.
 * The dialog stays open on a code that was not taken, saying why.
 */
public class CoachCodeDialog extends ConfirmDialog {

  private EditText tfCode;
  private TextView tvError;
  private Runnable onGranted;

  /** @param onGranted run on the main thread once a code has let this install in */
  public static CoachCodeDialog newInstance(Runnable onGranted) {
    CoachCodeDialog dialog = new CoachCodeDialog();
    dialog.onGranted = onGranted;
    return dialog;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    dialog = getDialog(R.string.confirm);
    return dialog;
  }

  @Override
  protected View getCustomView() {
    View view = LayoutInflater.from(getActivity()).inflate(R.layout.coach_code, null);
    tfCode = view.findViewById(R.id.tfCoachCode);
    tvError = view.findViewById(R.id.tvCoachCodeError);
    return view;
  }

  @Override
  protected void onConfirm() {
    final String code = tfCode.getText().toString().trim();
    final Activity activity = getActivity();
    final CoachClient client = CoachClient.configured(activity);
    if (code.isEmpty() || client == null) {
      return;
    }
    setAsking(true);
    new Thread(new Runnable() {
      @Override
      public void run() {
        final CoachCodeReply reply = client.redeem(code);
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            show(reply);
          }
        });
      }
    }, "coach-code").start();
  }

  private void show(CoachCodeReply reply) {
    if (!isAdded()) {
      return; // closed while the coach was asked: the access is kept, and the row reads it next time
    }
    if (reply.getKind() != CoachCodeReply.Kind.GRANTED) {
      setAsking(false);
      tvError.setText(failure(reply));
      tvError.setVisibility(View.VISIBLE);
      return;
    }
    CoachPlans.turnOn(getActivity(), null, null); // weekly plans come with the coach
    DialogUtils.showInfoMessage(getActivity(), R.string.coach_code_granted);
    if (onGranted != null) {
      onGranted.run();
    }
    dismiss();
  }

  private static int failure(CoachCodeReply reply) {
    if (reply.getKind() == CoachCodeReply.Kind.UNREACHABLE) {
      return R.string.coach_refused_unreachable;
    }
    if (reply.getKind() == CoachCodeReply.Kind.UNREADABLE) {
      return R.string.coach_refused_update;
    }
    if (reply.getRefusal() == CoachRefusal.CODE_REFUSED) {
      return R.string.coach_code_refused;
    }
    return reply.getRefusal() == CoachRefusal.RATE_LIMITED ? R.string.coach_refused_busy
        : R.string.coach_refused_unavailable;
  }

  /** One request at a time: the confirm button and the field wait for the answer. */
  private void setAsking(boolean asking) {
    ((AlertDialog) dialog).getButton(Dialog.BUTTON_POSITIVE).setEnabled(!asking);
    tfCode.setEnabled(!asking);
    if (asking) {
      tvError.setVisibility(View.GONE);
    }
  }
}
