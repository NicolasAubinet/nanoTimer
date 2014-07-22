package com.cube.nanotimer.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.activity.widget.SelectionHandler;
import com.cube.nanotimer.activity.widget.SelectorFragment;
import com.cube.nanotimer.activity.widget.list.FieldDialog;
import com.cube.nanotimer.activity.widget.list.FieldEditDialog;
import com.cube.nanotimer.activity.widget.list.FieldRenamer;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.Utils;
import com.cube.nanotimer.util.YesNoListener;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveType;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import java.util.ArrayList;
import java.util.List;

public class SolveTypesActivity extends FragmentActivity implements SelectionHandler, FieldRenamer {

  private DragSortListView lvSolveTypes;
  private SolveTypeListAdapter adapter;
  private List<SolveType> liSolveTypes;

  private List<CubeType> cubeTypes;

  private static final int ACTION_RENAME = 0;
  private static final int ACTION_DELETE = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.solvetypes);

    Button buAdd = (Button) findViewById(R.id.buAdd);
    buAdd.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        showAddDialog();
      }
    });

    lvSolveTypes = (DragSortListView) findViewById(R.id.lvSolveTypes);
    liSolveTypes = new ArrayList<SolveType>();
    adapter = new SolveTypeListAdapter(this, R.id.lvSolveTypes, liSolveTypes);
    lvSolveTypes.setDropListener(new DropListener() {
      @Override
      public void drop(int from, int to) {
        if (from != to) {
          SolveType item = adapter.getItem(from);
          // TODO : call some service method to reorder
          liSolveTypes.remove(item);
          liSolveTypes.add(to, item);
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

    App.INSTANCE.getService().getCubeTypes(new DataCallback<List<CubeType>>() {
      @Override
      public void onData(List<CubeType> data) {
        cubeTypes = data;
        if (cubeTypes != null) {
          ArrayList<String> types = new ArrayList<String>();
          for (CubeType t : cubeTypes) {
            types.add(t.getName());
          }
          Utils.showFragment(SolveTypesActivity.this,
              SelectorFragment.newInstance(0, types, getString(R.string.choose_cube_type), SolveTypesActivity.this));
        } else {
          finish();
        }
      }
    });
  }

  private void showAddDialog() {
    // TODO
//    FieldDialog fieldDialog = FieldAddDialog.newInstance(this, pos, items.get(pos));
//    Utils.showFragment(this, fieldDialog);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem menuItem) {
    if (featureId == Window.FEATURE_CONTEXT_MENU) {
      final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
      if (menuItem.getItemId() == ACTION_RENAME) {
        FieldDialog fieldDialog = FieldEditDialog.newInstance(this, info.position, liSolveTypes.get(info.position).getName());
        Utils.showFragment(this, fieldDialog);
      } else if (menuItem.getItemId() == ACTION_DELETE) {
        Utils.showYesNoConfirmation(this, getString(R.string.delete_solve_type_confirmation, liSolveTypes.get(info.position).getName()),
            new YesNoListener() {
          @Override
          public void onYes() {
            App.INSTANCE.getService().deleteSolveType(liSolveTypes.get(info.position), new DataCallback<Void>() {
              @Override
              public void onData(Void data) {
                liSolveTypes.remove(info.position);
                refreshList();
              }
            });
          }
        });
      }
    }
    return super.onMenuItemSelected(featureId, menuItem);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    if (v.getId() == R.id.lvSolveTypes) {
      menu.setHeaderTitle(R.string.action);
      menu.add(v.getId(), ACTION_RENAME, 0, R.string.edit);
      menu.add(v.getId(), ACTION_DELETE, 0, R.string.delete);
    }
  }

  @Override
  public void itemSelected(int id, int position) {
    if (position < 0 || position >= cubeTypes.size()) {
      finish();
      return;
    }
    CubeType cubeType = cubeTypes.get(position);
    App.INSTANCE.getService().getSolveTypes(cubeType, new DataCallback<List<SolveType>>() {
      @Override
      public void onData(List<SolveType> data) {
        liSolveTypes.clear();
        liSolveTypes.addAll(data);
        refreshList();
      }
    });
  }

  @Override
  public boolean renameField(int index, String newName) {
    SolveType st = liSolveTypes.get(index);
    st.setName(newName);
    App.INSTANCE.getService().updateSolveType(st, new DataCallback<Void>() {
      @Override
      public void onData(Void data) {
        refreshList();
      }
    });
    return true;
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
