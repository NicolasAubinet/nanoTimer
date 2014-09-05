package com.cube.nanotimer.gui.widget.list;

import com.cube.nanotimer.gui.widget.SelectionHandler;

public interface ListEditor extends SelectionHandler {
  void createNewItem(int id, String item);
  void renameItem(int id, int position, String newName);
  void deleteItem(int id, int position);
}
