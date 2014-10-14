package com.cube.nanotimer.gui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.AddStepsDialog;
import com.cube.nanotimer.gui.widget.SelectionHandler;
import com.cube.nanotimer.gui.widget.SelectorFragmentDialog;
import com.cube.nanotimer.gui.widget.StepsCreator;
import com.cube.nanotimer.gui.widget.list.FieldAddDialog;
import com.cube.nanotimer.gui.widget.list.FieldCreator;
import com.cube.nanotimer.gui.widget.list.FieldDialog;
import com.cube.nanotimer.gui.widget.list.FieldEditDialog;
import com.cube.nanotimer.gui.widget.list.FieldRenamer;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.YesNoListener;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveHistory;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.vo.SolveTypeStep;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import java.util.ArrayList;
import java.util.List;

public class SolveTypesActivity extends ActionBarActivity implements SelectionHandler, FieldRenamer, FieldCreator, StepsCreator {

  private DragSortListView lvSolveTypes;
  private SolveTypeListAdapter adapter;
  private List<SolveType> liSolveTypes = new ArrayList<SolveType>();

  private List<CubeType> cubeTypes;
  private CubeType curCubeType;

  private static final int ACTION_RENAME = 0;
  private static final int ACTION_DELETE = 1;
  private static final int ACTION_CREATESTEPS = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.solvetypes_screen);
    getSupportActionBar().setTitle("");

    initViews();

    CubeType cubeType = (CubeType) getIntent().getSerializableExtra("cubeType");
    if (cubeType != null) {
      setCubeType(cubeType);
    } else {
      App.INSTANCE.getService().getCubeTypes(true, new DataCallback<List<CubeType>>() {
        @Override
        public void onData(List<CubeType> data) {
          cubeTypes = data;
          if (cubeTypes != null) {
            ArrayList<String> types = new ArrayList<String>();
            for (CubeType t : cubeTypes) {
              types.add(t.getName());
            }
            DialogUtils.showFragment(SolveTypesActivity.this,
                SelectorFragmentDialog.newInstance(0, types, getString(R.string.choose_cube_type), false, SolveTypesActivity.this));
          } else {
            finish();
          }
        }
      });
    }
  }

  private void initViews() {
    lvSolveTypes = (DragSortListView) findViewById(R.id.lvSolveTypes);
    adapter = new SolveTypeListAdapter(this, R.id.lvSolveTypes, liSolveTypes);
    lvSolveTypes.setDropListener(new DropListener() {
      @Override
      public void drop(int from, int to) {
        if (from != to) {
          SolveType item = adapter.getItem(from);
          liSolveTypes.remove(item);
          liSolveTypes.add(to, item);
          App.INSTANCE.getService().saveSolveTypesOrder(liSolveTypes, null);
          refreshList();
        }
      }
    });
    lvSolveTypes.setAdapter(adapter);

    DragSortController controller = new DragSortController(lvSolveTypes);
    controller.setDragHandleId(R.id.imgMove);
    lvSolveTypes.setFloatViewManager(controller);
    lvSolveTypes.setOnTouchListener(controller);

    lvSolveTypes.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        registerForContextMenu(lvSolveTypes);
        openContextMenu(view);
        unregisterForContextMenu(lvSolveTypes);
      }
    });

    if (curCubeType != null) {
      getSupportActionBar().setTitle(curCubeType.getName());
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    setContentView(R.layout.solvetypes_screen);
    initViews();
  }

  private void showAddDialog() {
    FieldAddDialog dialog = FieldAddDialog.newInstance(this);
    DialogUtils.showFragment(this, dialog);
  }

  @Override
  public boolean onContextItemSelected(MenuItem menuItem) {
    final int position = ((AdapterContextMenuInfo) menuItem.getMenuInfo()).position;
    if (menuItem.getItemId() == ACTION_RENAME) {
      FieldDialog fieldDialog = FieldEditDialog.newInstance(this, position, liSolveTypes.get(position).getName());
      DialogUtils.showFragment(this, fieldDialog);
    } else if (menuItem.getItemId() == ACTION_DELETE) {
      DialogUtils.showYesNoConfirmation(this, getString(R.string.delete_solve_type_confirmation, liSolveTypes.get(position).getName()),
          new YesNoListener() {
            @Override
            public void onYes() {
              App.INSTANCE.getService().deleteSolveType(liSolveTypes.get(position), new DataCallback<Void>() {
                @Override
                public void onData(Void data) {
                  liSolveTypes.remove(position);
                  refreshList();
                }
              });
            }
          });
    } else if (menuItem.getItemId() == ACTION_CREATESTEPS) {
      App.INSTANCE.getService().getHistory(liSolveTypes.get(position), new DataCallback<SolveHistory>() {
        @Override
        public void onData(final SolveHistory data) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if (data.getSolveTimes().isEmpty()) {
                DialogUtils.showFragment(SolveTypesActivity.this, AddStepsDialog.newInstance(SolveTypesActivity.this, position));
              } else {
                DialogUtils.showYesNoConfirmation(SolveTypesActivity.this, R.string.solvetype_has_times_addsteps, new YesNoListener() {
                  @Override
                  public void onYes() {
                    DialogUtils.showFragment(SolveTypesActivity.this, AddStepsDialog.newInstance(SolveTypesActivity.this, position));
                  }
                });
              }
            }
          });
        }
      });
    }
    return super.onContextItemSelected(menuItem);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    if (v.getId() == R.id.lvSolveTypes) {
      int position = ((AdapterContextMenuInfo) menuInfo).position;
      menu.setHeaderTitle(R.string.action);
      menu.add(v.getId(), ACTION_RENAME, 0, R.string.rename);
      menu.add(v.getId(), ACTION_DELETE, 0, R.string.delete);
      if (position >= 0 && position < liSolveTypes.size() && !liSolveTypes.get(position).hasSteps()) {
        menu.add(v.getId(), ACTION_CREATESTEPS, 0, R.string.add_steps);
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.solvetypes_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.itAdd:
        showAddDialog();
        break;
    }
    return true;
  }

  @Override
  public void itemSelected(int id, int position) {
    if (position < 0 || position >= cubeTypes.size()) {
      finish();
      return;
    }
    setCubeType(cubeTypes.get(position));
  }

  private void setCubeType(CubeType cubeType) {
    curCubeType = cubeType;
    App.INSTANCE.getService().getSolveTypes(curCubeType, new DataCallback<List<SolveType>>() {
      @Override
      public void onData(List<SolveType> data) {
        liSolveTypes.clear();
        liSolveTypes.addAll(data);
        refreshList();
      }
    });
    if (curCubeType != null) {
      getSupportActionBar().setTitle(curCubeType.getName());
    }
  }

  @Override
  public boolean renameField(int index, String newName) {
    boolean res = checkSolveTypeName(newName, index);
    if (!res) {
      return res;
    }
    SolveType st = liSolveTypes.get(index);
    st.setName(newName.trim());
    App.INSTANCE.getService().updateSolveType(st, new DataCallback<Void>() {
      @Override
      public void onData(Void data) {
        refreshList();
      }
    });
    return true;
  }

  @Override
  public boolean createField(String name) {
    boolean res = checkSolveTypeName(name, null);
    if (!res) {
      return res;
    }
    SolveType st = new SolveType(name, curCubeType.getId());
    liSolveTypes.add(st);
    App.INSTANCE.getService().addSolveType(st, new DataCallback<Integer>() {
      @Override
      public void onData(Integer data) {
        refreshList();
      }
    });
    return true;
  }

  private boolean checkSolveTypeName(String name, Integer index) {
    if ("".equals(name.trim())) {
      return false;
    }
    for (int i = 0; i < liSolveTypes.size(); i++) {
      if (liSolveTypes.get(i).getName().equals(name)) {
        if (index == null || i != index) {
          DialogUtils.showInfoMessage(this, R.string.solve_type_already_exists);
          return false;
        } else {
          // The name was not changed, do nothing
          return true;
        }
      }
    }
    return true;
  }

  @Override
  public void addSteps(final List<String> stepNames, final int pos) {
    // delete existing times (if there are some) before adding the steps
    App.INSTANCE.getService().deleteHistory(liSolveTypes.get(pos), new DataCallback<Void>() {
      @Override
      public void onData(Void data) {
        SolveTypeStep[] steps = new SolveTypeStep[stepNames.size()];
        for (int i = 0; i < stepNames.size(); i++) {
          SolveTypeStep step = new SolveTypeStep();
          step.setName(stepNames.get(i));
          steps[i] = step;
        }
        SolveType solveType = liSolveTypes.get(pos);
        solveType.setSteps(steps);
        App.INSTANCE.getService().addSolveTypeSteps(solveType, new DataCallback<Void>() {
          @Override
          public void onData(Void data) {
            refreshList();
          }
        });
      }
    });
  }

  private void refreshList() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        adapter.notifyDataSetChanged();
      }
    });
  }

  private class SolveTypeListAdapter extends ArrayAdapter<SolveType> {

    public SolveTypeListAdapter(Context context, int id, List<SolveType> list) {
      super(context, id, list);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.solvetypes_list_item, null);
      }

      if (position >= 0 && position < liSolveTypes.size()) {
        SolveType item = liSolveTypes.get(position);
        if (item != null) {
          TextView tvName = (TextView) view.findViewById(R.id.tvSolveType);
          tvName.setText(item.getName());

          TextView tvStepsCount = (TextView) view.findViewById(R.id.tvStepsCount);
          if (item.hasSteps()) {
            StringBuilder stepsCount = new StringBuilder();
            stepsCount.append("(").append(item.getSteps().length).append(" ").append(getString(R.string.steps)).append(")");
            tvStepsCount.setText(stepsCount.toString());
          } else {
            tvStepsCount.setText("");
          }
        }
      }
      return view;
    }
  }

}
