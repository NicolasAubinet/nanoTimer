package com.cube.nanotimer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.activity.widget.HistoryDetailFragment;
import com.cube.nanotimer.activity.widget.SelectionHandler;
import com.cube.nanotimer.activity.widget.SelectorFragment;
import com.cube.nanotimer.activity.widget.TimeChangedHandler;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.Utils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.CubeType.Type;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainScreenActivity extends Activity implements SelectionHandler, TimeChangedHandler {

  private Button buCubeType;
  private Button buSolveType;
  private ListView lvHistory;

  private CubeType curCubeType;
  private SolveType curSolveType;
  private List<CubeType> cubeTypes;
  private List<SolveType> solveTypes;

  private List<SolveTime> liHistory = new ArrayList<SolveTime>();
  private HistoryListAdapter adapter;

  private static final int ID_CUBETYPE = 1;
  private static final int ID_SOLVETYPE = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mainscreen);
    App.INSTANCE.setContext(this);

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
              SelectorFragment.newInstance(ID_CUBETYPE, types, MainScreenActivity.this));
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
              SelectorFragment.newInstance(ID_SOLVETYPE, types, MainScreenActivity.this));
        }
      }
    });

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

  @Override
  protected void onResume() {
    super.onResume();
    if (cubeTypes == null) {
      retrieveTypes();
    } else {
      refreshHistory();
    }
  }

  @Override
  public void onItemSelected(int id, int position) {
    if (id == ID_CUBETYPE) {
      if (position >= 0 && position < cubeTypes.size()) {
        curCubeType = cubeTypes.get(position);
        buCubeType.setText(curCubeType.getName());
        refreshSolveTypes();
      }
    } else if (id == ID_SOLVETYPE) {
      if (position >= 0 && position < solveTypes.size()) {
        curSolveType = solveTypes.get(position);
        buSolveType.setText(curSolveType.getName());
        refreshHistory();
      }
    }
  }

  private void retrieveTypes() {
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
          refreshButtonTexts();
          refreshSolveTypes();
        }
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
            curSolveType = solveTypes.get(0);
            refreshButtonTexts();
            refreshHistory();
          }
        }
      });
    }
  }

  private void refreshHistory() {
    App.INSTANCE.getService().getHistory(curSolveType, new DataCallback<List<SolveTime>>() {
      @Override
      public void onData(List<SolveTime> data) {
        liHistory.clear();
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

  private void refreshButtonTexts() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (curCubeType != null) {
          buCubeType.setText(curCubeType.getName());
        }
        if (curSolveType != null) {
          buSolveType.setText(curSolveType.getName());
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
