package com.cube.nanotimer.gui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * EditText that guarantees a backspace always reaches the view's OnKeyListener, even from a soft
 * keyboard on an empty field. Most soft keyboards do not dispatch KEYCODE_DEL through the key event
 * path; they call InputConnection.deleteSurroundingText instead, which an OnKeyListener never sees.
 * This converts that delete into a real KEYCODE_DEL key event so key-based handling works uniformly
 * across hardware and soft keyboards.
 */
public class BackspaceAwareEditText extends AppCompatEditText {

  public BackspaceAwareEditText(Context context) {
    super(context);
  }

  public BackspaceAwareEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public BackspaceAwareEditText(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
    InputConnection ic = super.onCreateInputConnection(outAttrs);
    if (ic == null) {
      return null;
    }
    return new BackspaceInputConnection(ic);
  }

  private class BackspaceInputConnection extends InputConnectionWrapper {
    public BackspaceInputConnection(InputConnection target) {
      super(target, true);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
      // A plain single-char backspace is rerouted as a key event so the OnKeyListener sees it
      // (including when the field is empty, where deleteSurroundingText would otherwise be a no-op).
      if (beforeLength == 1 && afterLength == 0) {
        return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
      }
      return super.deleteSurroundingText(beforeLength, afterLength);
    }
  }

}
