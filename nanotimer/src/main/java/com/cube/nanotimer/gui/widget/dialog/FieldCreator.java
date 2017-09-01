package com.cube.nanotimer.gui.widget.dialog;

import java.io.Serializable;
import java.util.Properties;

public interface FieldCreator extends Serializable {
  boolean createField(String name, Properties props);
}
