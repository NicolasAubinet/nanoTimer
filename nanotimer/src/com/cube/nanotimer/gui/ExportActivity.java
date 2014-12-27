package com.cube.nanotimer.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.ExportActivity.ListItem.Type;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.CSVGenerator;
import com.cube.nanotimer.util.ExportCSVGenerator;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.util.helper.FileUtils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ExportResult;
import com.cube.nanotimer.vo.SolveType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExportActivity extends Activity {

  private ListView lvItems;
  private ExportListAdapter adapter;
  private final List<ListItem> liItems = new ArrayList<ListItem>();

  private CheckBox cbLimit;
  private EditText tfLimit;

  private static final String PREFS_NAME = "export";
  private static final String EXPORT_LIMIT_KEY = "limit";
  private static final String EXPORT_FILE_NAME = "export.csv";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.export_screen);

    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); // TODO keyboard still appears when coming back (like after sending a mail)

    lvItems = (ListView) findViewById(R.id.lvItems);

    adapter = new ExportListAdapter(this, R.id.lvItems, liItems);
    lvItems.setAdapter(adapter);

    tfLimit = (EditText) findViewById(R.id.tfLimit);

    cbLimit = (CheckBox) findViewById(R.id.cbLimit);
    cbLimit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
          tfLimit.setText(String.valueOf(getLastExportLimit()));
          tfLimit.setEnabled(true);
        } else {
          tfLimit.setText(R.string.no_limit);
          tfLimit.setEnabled(false);
        }
      }
    });

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
        export();
      }
    });
  }

  private void export() {
    List<Integer> solveTypeIds = new ArrayList<Integer>();
    synchronized (liItems) {
      for (ListItem it : liItems) {
        if (it.isSelected() && it.getType() == Type.SOLVETYPE) {
          solveTypeIds.add(it.getId());
        }
      }
    }
    if (solveTypeIds.isEmpty()) {
      DialogUtils.showInfoMessage(this, R.string.select_at_least_one_solve_type);
    }
    int limit = -1;
    if (cbLimit.isChecked()) {
      try {
        limit = Integer.parseInt(tfLimit.getText().toString());
        saveLastExportLimit(limit);
      } catch (NumberFormatException e) {
        Log.e("[NanoTimer]", "Export limit parsing exception");
      }
    }
    // TODO take care to not do this twice if the button is clicked multiple times (should be fixed with the loading indicator)
    // TODO display loading indicator while exporting times
    App.INSTANCE.getService().getExportFile(solveTypeIds, limit, new DataCallback<List<ExportResult>>() {
      @Override
      public void onData(List<ExportResult> data) {
        if (data != null && !data.isEmpty()) {
          CSVGenerator generator = new ExportCSVGenerator(data);
          File file = FileUtils.createCSVFile(ExportActivity.this, EXPORT_FILE_NAME, generator);
          sendExportFile(file);
//          FileUtils.deleteFile(ExportActivity.this, EXPORT_FILE_NAME); // TODO put back in (where i can)
        } else {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              DialogUtils.showInfoMessage(ExportActivity.this, R.string.no_data_to_export);
            }
          });
        }
      }
    });
  }

  private void sendExportFile(File file) {
    Uri uri = Uri.fromFile(file);
    // TODO fails to send attachment because it comes from private folder. see if possible to send from private folder (maybe from memory?)
    Intent i = new Intent(Intent.ACTION_SEND);
    // TODO should maybe limit the number of send options (lots of things appear in the send dialog)
    i.setType("text/plain");
    i.putExtra(Intent.EXTRA_EMAIL, "");
    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_mail_subject));
    i.putExtra(Intent.EXTRA_TEXT, getString(R.string.export_mail_body, FormatterService.INSTANCE.formatDateTime(System.currentTimeMillis())));
    i.putExtra(Intent.EXTRA_STREAM, uri);
    startActivityForResult(Intent.createChooser(i, getString(R.string.send_via)), 0);
  }

  private class ExportListAdapter extends ArrayAdapter<ListItem> {

    public ExportListAdapter(Context context, int textViewResourceId, List<ListItem> objects) {
      super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflater.inflate(R.layout.export_listitem, null); // inflating every time to avoid bugs in items on old htc (looks like caching bugs when selecting rows)

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

  private int getLastExportLimit() {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
    return prefs.getInt(EXPORT_LIMIT_KEY, 1000);
  }

  private void saveLastExportLimit(int limit) {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
    Editor editor = prefs.edit();
    editor.putInt(EXPORT_LIMIT_KEY, limit);
    editor.commit();
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
      this.selected = false;
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
