package com.cube.nanotimer.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.ExportActivity.ListItem.Type;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExportActivity extends Activity {

  private ListView lvItems;
  private ExportListAdapter adapter;
  private final List<ListItem> liItems = new ArrayList<ListItem>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.export_screen);

    lvItems = (ListView) findViewById(R.id.lvItems);

    adapter = new ExportListAdapter(this, R.id.lvItems, liItems);
    lvItems.setAdapter(adapter);

    App.INSTANCE.getService().getCubeTypes(false, new DataCallback<List<CubeType>>() {
      @Override
      public void onData(final List<CubeType> data) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            for (CubeType ct : data) {
              synchronized (liItems) {
                liItems.add(new ListItem(Type.CUBETYPE, ct.getId(), ct.getName()));
              }
              App.INSTANCE.getService().getSolveTypes(ct, new DataCallback<List<SolveType>>() {
                @Override
                public void onData(final List<SolveType> data) {
                  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      addSolveTypesToList(data);
                    }
                  });
                }
              });
            }
          }
        });
      }
    });

    Button buExport = (Button) findViewById(R.id.buExport);
    buExport.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        synchronized (liItems) {
          for (ListItem it : liItems) {
            if (it.isSelected()) {
              // TODO display dialog with times limit and send mode (mail or sd file)
              Log.i("[NanoTimer]", "Export: " + it.getId() + "\t" + it.getName());
            }
          }
        }
      }
    });
  }

  private class ExportListAdapter extends ArrayAdapter<ListItem> {
    private LayoutInflater inflater;

    public ExportListAdapter(Context context, int textViewResourceId, List<ListItem> objects) {
      super(context, textViewResourceId, objects);
      inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        view = inflater.inflate(R.layout.export_listitem, null);
      }

      synchronized (liItems) {
        if (position >= 0 && position < liItems.size()) {
          ListItem st = liItems.get(position);
          if (st != null) {
            ((TextView) view.findViewById(R.id.tvTitle)).setText(st.getName());
            ImageView imgTab = (ImageView) view.findViewById(R.id.imgTab);
            View spacerView = view.findViewById(R.id.spacerView);
            LayoutParams layoutParams = (LayoutParams) spacerView.getLayoutParams();
            if (st.getType() == Type.CUBETYPE) {
              layoutParams.width = 15;
              imgTab.setVisibility(View.GONE);
            } else if (st.getType() == Type.SOLVETYPE) {
              layoutParams.width = 30;
              imgTab.setVisibility(View.VISIBLE);
            }
            spacerView.setLayoutParams(layoutParams);

            CheckBox cbSelected = (CheckBox) view.findViewById(R.id.cbSelected);
            cbSelected.setChecked(st.isSelected());
            cbSelected.setOnCheckedChangeListener(new OnCheckedChangeListener() {
              @Override
              public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkedStatusChanged(position, b);
              }
            });
          }
        }
      }
      return view;
    }
  }

  public void checkedStatusChanged(int i, boolean checked) {
    synchronized (liItems) {
      ListItem it = liItems.get(i);
      it.setSelected(checked);
      if (it.getType() == Type.CUBETYPE) {
        for (int j = i + 1; j < liItems.size() && liItems.get(j).getType() == Type.SOLVETYPE; j++) {
          liItems.get(j).setSelected(checked);
        }
      } else if (it.getType() == Type.SOLVETYPE) {
        boolean allSolveTypesSelected = true;
        ListItem parentCubeType = getCubeTypeOf(it);
        List<ListItem> solveTypes = getSolveTypesOf(parentCubeType);
        for (ListItem st : solveTypes) {
          if (!st.isSelected()) {
            allSolveTypesSelected = false;
            break;
          }
        }
        if (allSolveTypesSelected) {
          parentCubeType.setSelected(true);
        }
      }
      adapter.notifyDataSetChanged();
    }
  }

  public ListItem getCubeTypeOf(ListItem solveTypeItem) {
    if (solveTypeItem.getType() == Type.CUBETYPE) {
      return solveTypeItem;
    }
    ListItem cubeType = null;
    // Search the solveTypeItem index
    int ind = getListItemIndex(solveTypeItem);
    if (ind >= 0) {
      // Search for parent cube type
      boolean found = false;
      synchronized (liItems) {
        for (int i = ind - 1; i >= 0 && !found; i--) {
          if (liItems.get(i).getType() == Type.CUBETYPE) {
            found = true;
            cubeType = liItems.get(i);
          }
        }
      }
    }
    return cubeType;
  }

  private List<ListItem> getSolveTypesOf(ListItem cubeTypeItem) {
    if (cubeTypeItem.getType() == Type.SOLVETYPE) {
      return Collections.emptyList();
    }
    List<ListItem> solveTypes = null;
    int ind = getListItemIndex(cubeTypeItem);
    if (ind >= 0) {
      solveTypes = new ArrayList<ListItem>();
      synchronized (liItems) {
        for (int i = ind + 1; i < liItems.size() && liItems.get(i).getType() != Type.CUBETYPE; i++) {
          solveTypes.add(liItems.get(i));
        }
      }
    }
    return solveTypes;
  }

  private int getListItemIndex(ListItem listItem) {
    synchronized (liItems) {
      for (int i = 0; i < liItems.size(); i++) {
        if (liItems.get(i).equals(listItem)) {
          return i;
        }
      }
    }
    return -1;
  }

  private void addSolveTypesToList(List<SolveType> solveTypes) {
    if (solveTypes != null && solveTypes.size() > 0) {
      synchronized (liItems) {
        int cubeTypeId = solveTypes.get(0).getCubeTypeId();
        for (int i = 0; i < liItems.size(); i++) {
          ListItem itCubeType = liItems.get(i);
          if (itCubeType.getType() == Type.CUBETYPE && itCubeType.getId() == cubeTypeId) {
            // found corresponding cube type
            for (int j = solveTypes.size() - 1; j >= 0; j--) {
              SolveType solveType = solveTypes.get(j);
              liItems.add(i + 1, new ListItem(Type.SOLVETYPE, solveType.getId(), solveType.getName()));
            }
            break;
          }
        }
        adapter.notifyDataSetChanged();
      }
    }
  }

  static class ListItem {
    enum Type { CUBETYPE, SOLVETYPE };

    private Type type;
    private int id;
    private String name;
    private boolean selected;

    private ListItem(Type type, int id, String name) {
      this.type = type;
      this.id = id;
      this.name = name;
      this.selected = true;
    }

    public Type getType() {
      return type;
    }

    public int getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public boolean isSelected() {
      return selected;
    }

    public void setSelected(boolean selected) {
      this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ListItem)) return false;

      ListItem listItem = (ListItem) o;
      if (id != listItem.id) return false;
      if (selected != listItem.selected) return false;
      if (name != null ? !name.equals(listItem.name) : listItem.name != null) return false;
      if (type != listItem.type) return false;
      return true;
    }
  }

}
