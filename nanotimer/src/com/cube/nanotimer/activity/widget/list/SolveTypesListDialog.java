package com.cube.nanotimer.activity.widget.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.cube.nanotimer.R;
import com.cube.nanotimer.activity.widget.SelectionHandler;

import java.util.ArrayList;
import java.util.List;

public class SolveTypesListDialog extends DialogFragment {

  private static final String ARG_ITEMS = "items";

  private int id;
  private SelectionHandler handler;

  private List<String> items;
  private ArrayAdapter<String> adapter;

  private ListView lvSolveTypes;

  public static SolveTypesListDialog newInstance(int id, ArrayList<String> items, SelectionHandler handler) {
    SolveTypesListDialog frag = new SolveTypesListDialog(id, handler);
    Bundle args = new Bundle();
    args.putStringArrayList(ARG_ITEMS, items);
    frag.setArguments(args);
    return frag;
  }

  private SolveTypesListDialog(int id, SelectionHandler handler) {
    this.id = id;
    this.handler = handler;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View lvView = getActivity().getLayoutInflater().inflate(R.layout.simple_list, null);
    lvSolveTypes = (ListView) lvView.findViewById(R.id.lvItems);

    items = new ArrayList<String>(getArguments().getStringArrayList(ARG_ITEMS));

    adapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item, items);
    lvSolveTypes.setAdapter(adapter);

    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(lvView).create();

    lvSolveTypes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        handler.itemSelected(id, i);
        dialog.dismiss();
      }
    });

    return dialog;
  }

  @Override
  public void show(FragmentManager manager, String tag) {
    if (manager.findFragmentByTag(tag) == null) {
      super.show(manager, tag);
    }
  }

}
