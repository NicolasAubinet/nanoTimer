package com.cube.nanotimer.activity.widget.list;

import com.cube.nanotimer.activity.widget.SelectionHandler;

public interface ListEditor extends SelectionHandler {
  void createNewItem(int id, String item);
  void renameItem(int id, int position, String newName);
  void deleteItem(int id, int position);
}
