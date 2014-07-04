package com.cube.nanotimer.activity.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.cube.nanotimer.R;

import java.util.ArrayList;
import java.util.List;

public class SelectorFragment extends DialogFragment {

  private static final String ARG_ID = "id";
  private static final String ARG_ITEMS = "items";

  private SelectionHandler handler;

  public static SelectorFragment newInstance(int id, ArrayList<String> items, SelectionHandler handler) {
    SelectorFragment f = new SelectorFragment(handler);
    Bundle bundle = new Bundle();
    bundle.putInt(ARG_ID, id);
    bundle.putStringArrayList(ARG_ITEMS, items);
    f.setArguments(bundle);
    return f;
  }

  private SelectorFragment(SelectionHandler handler) {
    this.handler = handler;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View v = getActivity().getLayoutInflater().inflate(R.layout.cube_type_list, null);
    ListView lvItems = (ListView) v.findViewById(R.id.lvItems);

    final int id = getArguments().getInt(ARG_ID);
    List items = getArguments().getStringArrayList(ARG_ITEMS);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item, items);
    lvItems.setAdapter(adapter);

    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();

    lvItems.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        handler.itemSelected(id, i);
        dialog.dismiss();
      }
    });

    return dialog;
  }
}
