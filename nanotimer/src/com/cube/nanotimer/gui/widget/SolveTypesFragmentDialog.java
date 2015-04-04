package com.cube.nanotimer.gui.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.R;

import java.util.ArrayList;
import java.util.List;

public class SolveTypesFragmentDialog extends SelectorFragmentDialog {

  private static final String ARG_SHORTCUT = "shortcut";

  public static SolveTypesFragmentDialog newInstance(int id, ArrayList<String> items, boolean cancelTouchOutside,
                                                   SelectionHandler handler, boolean showShortcut) {
    SolveTypesFragmentDialog f = new SolveTypesFragmentDialog();
    f.handler = handler;
    Bundle bundle = new Bundle();
    bundle.putInt(ARG_ID, id);
    bundle.putBoolean(ARG_TOUCHOUT, cancelTouchOutside);
    bundle.putBoolean(ARG_SHORTCUT, showShortcut);

    if (Options.INSTANCE.isSolveTypesShortcutEnabled()) {
      items.add(App.INSTANCE.getContext().getString(R.string.add_more_solve_types));
    }
    bundle.putStringArrayList(ARG_ITEMS, items);

    f.setArguments(bundle);
    return f;
  }

  protected ArrayAdapter<String> getNewAdapter() {
    return new SolveTypeCustomAdapter(getActivity(), R.layout.resizable_simple_list_item, liItems);
  }

  protected class SolveTypeCustomAdapter extends CustomAdapter {
    public SolveTypeCustomAdapter(Context context, int id, List<String> list) {
      super(context, id, list);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
      View view = super.getView(position, convertView, parent);
      if (getArguments().getBoolean(ARG_SHORTCUT) && position == liItems.size() - 1) {
        TextView tvName = (TextView) view.findViewById(R.id.tvItem);
        tvName.setTextColor(getResources().getColor(R.color.gray600));
      }
      return view;
    }
  }

}
