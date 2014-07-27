package com.cube.nanotimer.activity.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.cube.nanotimer.R;

import java.util.ArrayList;
import java.util.List;

public class SelectorFragmentDialog extends DialogFragment {

  private static final String ARG_ID = "id";
  private static final String ARG_TITLE = "title";
  private static final String ARG_ITEMS = "items";

  private SelectionHandler handler;
  private int id;

  public static SelectorFragmentDialog newInstance(int id, ArrayList<String> items, SelectionHandler handler) {
    return newInstance(id, items, null, handler);
  }

  public static SelectorFragmentDialog newInstance(int id, ArrayList<String> items, String title, SelectionHandler handler) {
    SelectorFragmentDialog f = new SelectorFragmentDialog(handler);
    Bundle bundle = new Bundle();
    bundle.putInt(ARG_ID, id);
    bundle.putString(ARG_TITLE, title);
    bundle.putStringArrayList(ARG_ITEMS, items);
    f.setArguments(bundle);
    return f;
  }

  private SelectorFragmentDialog(SelectionHandler handler) {
    this.handler = handler;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View v = getActivity().getLayoutInflater().inflate(R.layout.simple_list, null);
    ListView lvItems = (ListView) v.findViewById(R.id.lvItems);

    id = getArguments().getInt(ARG_ID);
    String title = getArguments().getString(ARG_TITLE);
    List items = getArguments().getStringArrayList(ARG_ITEMS);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item, items);
    lvItems.setAdapter(adapter);

    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();
    if (title != null) {
      dialog.setTitle(title);
    }

    lvItems.setOnItemClickListener(new OnItemClickListener() {
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

  @Override
  public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);
    handler.itemSelected(id, -1);
  }

}
