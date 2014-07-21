package com.cube.nanotimer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.activity.widget.HistoryDetailFragment;
import com.cube.nanotimer.activity.widget.SelectorFragment;
import com.cube.nanotimer.activity.widget.TimeChangedHandler;
import com.cube.nanotimer.activity.widget.list.ListEditor;
import com.cube.nanotimer.activity.widget.list.SolveTypesListDialog;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.Utils;
import com.cube.nanotimer.util.YesNoListener;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.CubeType.Type;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainScreenActivity extends FragmentActivity implements TimeChangedHandler {

  private Button buCubeType;
  private Button buSolveType;
  private ListView lvHistory;

  private CubeType curCubeType;
  private SolveType curSolveType;
  private List<CubeType> cubeTypes;
  private List<SolveType> solveTypes;

  private List<SolveTime> liHistory = new ArrayList<SolveTime>();
  private HistoryListAdapter adapter;

  private TypeListEditor typeListEditor = new TypeListEditor();
  private int previousLastItem = 0;

  private static final int ID_CUBETYPE = 1;
  private static final int ID_SOLVETYPE = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mainscreen);
    App.INSTANCE.setContext(this);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    initViews();
  }

  private void initViews() {
    buCubeType = (Button) findViewById(R.id.buCubeType);
    buCubeType.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (cubeTypes != null) {
          ArrayList<String> types = new ArrayList<String>();
          for (CubeType t : cubeTypes) {
            types.add(t.getName());
          }
          Utils.showFragment(MainScreenActivity.this,
              SelectorFragment.newInstance(ID_CUBETYPE, types, typeListEditor));
        }
      }
    });

    buSolveType = (Button) findViewById(R.id.buSolveType);
    buSolveType.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (solveTypes != null) {
          ArrayList<String> types = new ArrayList<String>();
          for (SolveType t : solveTypes) {
            types.add(t.getName());
          }
          Utils.showFragment(MainScreenActivity.this,
              SolveTypesListDialog.newInstance(ID_SOLVETYPE, types, typeListEditor));
        }
      }
    });

    initHistoryList();

    findViewById(R.id.buStart).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent i = new Intent(MainScreenActivity.this, TimerActivity.class);
        i.putExtra("cubeType", curCubeType);
        i.putExtra("solveType", curSolveType);
        startActivity(i);
      }
    });
  }

  private void initHistoryList() {
    adapter = new HistoryListAdapter(MainScreenActivity.this, R.id.lvHistory, liHistory);
    lvHistory = (ListView) findViewById(R.id.lvHistory);
    lvHistory.setAdapter(adapter);
    lvHistory.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Utils.showFragment(MainScreenActivity.this,
            HistoryDetailFragment.newInstance(liHistory.get(i), curCubeType, MainScreenActivity.this));
      }
    });
    lvHistory.setOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view.getId() == R.id.lvHistory && !liHistory.isEmpty()) {
          int lastVisibleItem = firstVisibleItem + visibleItemCount;
          if (totalItemCount == lastVisibleItem && lastVisibleItem != previousLastItem) {
            previousLastItem = lastVisibleItem;
            long from = liHistory.get(liHistory.size() - 1).getTimestamp();
            App.INSTANCE.getService().getHistory(curSolveType, from, new DataCallback<List<SolveTime>>() {
              @Override
              public void onData(List<SolveTime> data) {
                liHistory.addAll(data);
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    adapter.notifyDataSetChanged();
                  }
                });
              }
            });
          }
        }
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (cubeTypes == null) {
      refreshCubeTypes();
    } else {
      refreshHistory();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.mainscreen_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.itClearHistory:
        Utils.showYesNoConfirmation(this, R.string.clear_history_solve_type_confirmation, new YesNoListener() {
          @Override
          public void onYes() {
            if (curSolveType != null) {
              App.INSTANCE.getService().deleteHistory(curSolveType, new DataCallback<Void>() {
                @Override
                public void onData(Void data) {
                  refreshHistory();
                }
              });
            }
          }
        });
        break;
      case R.id.itOptions:
        startActivity(new Intent(this, OptionsActivity.class));
        break;
    }
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    String cubeTypeText = buCubeType.getText().toString();
    String solveTypeText = buSolveType.getText().toString();

    setContentView(R.layout.mainscreen);
    initViews();

    buCubeType.setText(cubeTypeText);
    buSolveType.setText(solveTypeText);
  }

  private void refreshCubeTypes() {
    App.INSTANCE.getService().getCubeTypes(new DataCallback<List<CubeType>>() {
      @Override
      public void onData(List<CubeType> data) {
        cubeTypes = data;
        if (cubeTypes != null && !cubeTypes.isEmpty()) {
          curCubeType = cubeTypes.get(0);
          for (CubeType ct : cubeTypes) {
            if (ct.getId() == Type.THREE_BY_THREE.getId()) {
              curCubeType = ct;
              break;
            }
          }
        } else {
          curCubeType = null;
        }
        refreshButtonTexts();
        refreshSolveTypes();
      }
    });
  }

  private void refreshSolveTypes() {
    if (curCubeType != null) {
      App.INSTANCE.getService().getSolveTypes(curCubeType, new DataCallback<List<SolveType>>() {
        @Override
        public void onData(List<SolveType> data) {
          solveTypes = data;
          if (solveTypes != null && !solveTypes.isEmpty()) {
            boolean foundType = false;
            if (curSolveType != null) {
              for (SolveType st : solveTypes) {
                if (curSolveType.getId() == st.getId()) {
                  curSolveType = st;
                  foundType = true;
                }
              }
            }
            if (!foundType) {
              curSolveType = solveTypes.get(0);
            }
          } else {
            curSolveType = null;
          }
          refreshButtonTexts();
          refreshHistory();
        }
      });
    } else {
      curSolveType = null;
      refreshButtonTexts();
      refreshHistory();
    }
  }

  private void refreshHistory() {
    previousLastItem = 0;
    liHistory.clear();
    if (curSolveType != null) {
      App.INSTANCE.getService().getHistory(curSolveType, new DataCallback<List<SolveTime>>() {
        @Override
        public void onData(List<SolveTime> data) {
          liHistory.addAll(data);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              adapter.notifyDataSetChanged();
              lvHistory.setSelection(0);
            }
          });
        }
      });
    } else {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          adapter.notifyDataSetChanged();
        }
      });
    }
  }

  private void refreshButtonTexts() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (curCubeType != null) {
          buCubeType.setText(curCubeType.getName());
        } else {
          buCubeType.setText("");
        }
        if (curSolveType != null) {
          buSolveType.setText(curSolveType.getName());
        } else {
          buSolveType.setText("");
        }
      }
    });
  }

  @Override
  public void onTimeChanged(SolveTime solveTime) {
    for (SolveTime st : liHistory) {
      if (st.getId() == solveTime.getId()) {
        st.setTime(solveTime.getTime());
        adapter.notifyDataSetChanged();
        break;
      }
    }
  }

  @Override
  public void onTimeDeleted(SolveTime solveTime) {
    for (Iterator<SolveTime> it = liHistory.iterator(); it.hasNext(); ) {
      SolveTime st = it.next();
      if (st.getId() == solveTime.getId()) {
        it.remove();
        adapter.notifyDataSetChanged();
        break;
      }
    }
  }

  private class TypeListEditor implements ListEditor {
    @Override
    public void createNewItem(int id, String item) {
      if (id == ID_SOLVETYPE) {
        SolveType st = new SolveType(item, curCubeType.getId());
        App.INSTANCE.getService().addSolveType(st, new DataCallback<Integer>() {
          @Override
          public void onData(Integer data) {
            refreshSolveTypes();
          }
        });
      }
    }

    @Override
    public void renameItem(int id, int position, String newName) {
      if (id == ID_SOLVETYPE) {
        SolveType st = solveTypes.get(position);
        st.setName(newName);
        App.INSTANCE.getService().updateSolveType(st, new DataCallback<Void>() {
          @Override
          public void onData(Void data) {
            refreshSolveTypes();
          }
        });
      }
    }

    @Override
    public void deleteItem(int id, int position) {
      if (id == ID_SOLVETYPE) {
        SolveType st = solveTypes.get(position);
        App.INSTANCE.getService().deleteSolveType(st, new DataCallback<Void>() {
          @Override
          public void onData(Void data) {
            refreshSolveTypes();
          }
        });
      }
    }

    @Override
    public void itemSelected(int id, int position) {
      if (position >= 0) {
        if (id == ID_CUBETYPE) {
          curCubeType = cubeTypes.get(position);
          buCubeType.setText(curCubeType.getName());
          refreshSolveTypes();
        } else if (id == ID_SOLVETYPE) {
          curSolveType = solveTypes.get(position);
          buSolveType.setText(curSolveType.getName());
          refreshHistory();
        }
      }
    }
  }

  private class HistoryListAdapter extends ArrayAdapter<SolveTime> {
    public HistoryListAdapter(Context context, int textViewResourceId, List<SolveTime> objects) {
      super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.history_list_item, null);
      }

      if (position >= 0 && position < liHistory.size()) {
        SolveTime st = liHistory.get(position);
        if (st != null) {
          ((TextView) view.findViewById(R.id.tvDate)).setText(FormatterService.INSTANCE.formatDateTime(st.getTimestamp()));
          ((TextView) view.findViewById(R.id.tvTime)).setText(FormatterService.INSTANCE.formatSolveTime(st.getTime()));
        }
      }
      return view;
    }
  }

}
