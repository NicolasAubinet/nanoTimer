package com.cube.nanotimer.activity.widget.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.Utils;
import com.cube.nanotimer.util.YesNoListener;

import java.util.ArrayList;
import java.util.List;

public class SolveTypesListDialog extends DialogFragment implements FieldRenamer {

  private final int ACTION_RENAME = 0;
  private final int ACTION_DELETE = 1;

  private static final String ARG_ITEMS = "items";

  private int id;
  private ListEditor listEditor;

  private List<String> items;
  private ArrayAdapter<String> adapter;

  private ListView lvSolveTypes;
  private EditText tfName;

  public static SolveTypesListDialog newInstance(int id, ArrayList<String> items, ListEditor listEditor) {
    SolveTypesListDialog frag = new SolveTypesListDialog(id, listEditor);
    Bundle args = new Bundle();
    args.putStringArrayList(ARG_ITEMS, items);
    frag.setArguments(args);
    return frag;
  }

  private SolveTypesListDialog(int id, ListEditor listEditor) {
    this.id = id;
    this.listEditor = listEditor;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View lvView = getActivity().getLayoutInflater().inflate(R.layout.solvetypes_list, null);
    lvSolveTypes = (ListView) lvView.findViewById(R.id.lvSolveTypes);
    tfName = (EditText) lvView.findViewById(R.id.tfName);

    items = new ArrayList<String>(getArguments().getStringArrayList(ARG_ITEMS));

    adapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item, items);
    lvSolveTypes.setAdapter(adapter);

    Button btnAdd = (Button) lvView.findViewById(R.id.btnAdd);
    btnAdd.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String itemName = tfName.getText().toString();
        if (!itemName.trim().isEmpty()) {
          if (!items.contains(itemName)) {
            tfName.setText("");
            items.add(itemName);
            adapter.notifyDataSetChanged();
            listEditor.createNewItem(id, itemName);
          } else {
            Utils.showInfoMessage(getActivity(), R.string.solveTypeAlreadyExists);
          }
        }
      }
    });

    lvSolveTypes.setOnCreateContextMenuListener(this);

    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(lvView).create();

    lvSolveTypes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        listEditor.itemSelected(id, i);
        dialog.dismiss();
      }
    });

    return dialog;
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    if (v.getId() == R.id.lvSolveTypes) {
      menu.setHeaderTitle(R.string.action);
      menu.add(v.getId(), ACTION_RENAME, 0, R.string.rename);
      menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
          showEditDialog(((AdapterContextMenuInfo) menuItem.getMenuInfo()).position);
          return true;
        }
      });
      menu.add(v.getId(), ACTION_DELETE, 0, R.string.delete);
      menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
          deleteItem(((AdapterContextMenuInfo) menuItem.getMenuInfo()).position);
          return true;
        }
      });
    }
  }

  private void showEditDialog(final int pos) {
    FieldDialog fieldDialog = FieldEditDialog.newInstance(this, pos, items.get(pos));
    Utils.showFragment(getActivity(), fieldDialog);
  }

  private void deleteItem(final int pos) {
    Utils.showYesNoConfirmation(getActivity(), getString(R.string.deleteSolveTypeConfirmation, items.get(pos)), new YesNoListener() {
      @Override
      public void onYes() {
        items.remove(pos);
        adapter.notifyDataSetChanged();
        listEditor.deleteItem(id, pos);
      }
    });
  }

  @Override
  public boolean renameField(int index, String newName) {
    if (!newName.isEmpty()) {
      if (!items.contains(newName)) {
        items.set(index, newName);
        adapter.notifyDataSetChanged();
        listEditor.renameItem(id, index, newName);
        return true;
      } else {
        if (items.get(index).equals(newName)) {
          // The name was not changed, do nothing
          return true;
        } else {
          Utils.showInfoMessage(getActivity(), R.string.solveTypeAlreadyExists);
        }
      }
    }
    return false;
  }
}
