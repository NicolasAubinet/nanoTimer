package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.cube.nanotimer.R;

import java.util.ArrayList;
import java.util.List;

public class SelectorFragmentDialog extends DialogFragment {

  protected static final String ARG_ID = "id";
  protected static final String ARG_TITLE = "title";
  protected static final String ARG_TOUCHOUT = "touchout";
  protected static final String ARG_ITEMS = "items";

  protected SelectionHandler handler;
  protected int id;
  protected List<String> liItems;
  protected ArrayAdapter<String> adapter;

  public static SelectorFragmentDialog newInstance(int id, ArrayList<String> items, boolean cancelTouchOutside, SelectionHandler handler) {
    return newInstance(id, items, null, cancelTouchOutside, handler);
  }

  public static SelectorFragmentDialog newInstance(int id, ArrayList<String> items, String title, boolean cancelTouchOutside, SelectionHandler handler) {
    SelectorFragmentDialog f = new SelectorFragmentDialog(handler);
    Bundle bundle = new Bundle();
    bundle.putInt(ARG_ID, id);
    bundle.putString(ARG_TITLE, title);
    bundle.putBoolean(ARG_TOUCHOUT, cancelTouchOutside);
    bundle.putStringArrayList(ARG_ITEMS, items);
    f.setArguments(bundle);
    return f;
  }

  protected SelectorFragmentDialog(SelectionHandler handler) {
    this.handler = handler;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View v = getActivity().getLayoutInflater().inflate(R.layout.simple_list, null);
    ListView lvItems = (ListView) v.findViewById(R.id.lvItems);

    id = getArguments().getInt(ARG_ID);
    String title = getArguments().getString(ARG_TITLE);
    boolean cancelOnTouchOutside = getArguments().getBoolean(ARG_TOUCHOUT);
    liItems = getArguments().getStringArrayList(ARG_ITEMS);
    adapter = getNewAdapter();
    lvItems.setAdapter(adapter);

    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();
    dialog.setCanceledOnTouchOutside(cancelOnTouchOutside);
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

  protected ArrayAdapter<String> getNewAdapter() {
    return new CustomAdapter(getActivity(), R.layout.resizable_simple_list_item, liItems);
  }

  protected class CustomAdapter extends ArrayAdapter<String> {
    public CustomAdapter(Context context, int id, List<String> list) {
      super(context, id, list);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflater.inflate(R.layout.resizable_simple_list_item, null);

      if (position >= 0 && position < liItems.size()) {
        String item = liItems.get(position);
        if (item != null) {
          TextView tvName = (TextView) view.findViewById(R.id.tvItem);
          tvName.setText(item);
        }
      }
      return view;
    }
  }

}
