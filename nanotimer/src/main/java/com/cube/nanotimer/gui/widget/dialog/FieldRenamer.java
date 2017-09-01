package com.cube.nanotimer.gui.widget.dialog;

import java.io.Serializable;

public interface FieldRenamer extends Serializable {
  boolean renameField(int index, String newName);
}
