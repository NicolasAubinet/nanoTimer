package com.cube.nanotimer.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.activity.widget.SelectionHandler;
import com.cube.nanotimer.activity.widget.SelectorFragment;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.CubeType.Type;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.List;

public class MainScreenActivity extends Activity implements SelectionHandler {

  private Button buCubeType;
  private Button buSolveType;
  private CubeType curCubeType;
  private SolveType curSolveType;
  private List<CubeType> cubeTypes;
  private List<SolveType> solveTypes;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mainscreen);
    App.setContext(this);

    retrieveTypes();

    buCubeType = (Button) findViewById(R.id.buCubeType);
    buCubeType.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (cubeTypes != null) {
          ArrayList<String> types = new ArrayList<String>();
          for (CubeType t : cubeTypes) {
            types.add(t.getName());
          }
          DialogFragment df = SelectorFragment.newInstance(types, MainScreenActivity.this);
          df.show(getFragmentManager(), "dialog");
        }
      }
    });

    buSolveType = (Button) findViewById(R.id.buSolveType);

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
  public void onItemSelected(int position) {
    if (position >= 0 && position < cubeTypes.size()) {
      curCubeType = cubeTypes.get(position);
      buCubeType.setText(curCubeType.getName());
    }
  }

  private void retrieveTypes() {
    App.getService().getCubeTypes(new DataCallback<List<CubeType>>() {
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
          buCubeType.setText(curCubeType.getName());
          App.getService().getSolveTypes(curCubeType, new DataCallback<List<SolveType>>() {
            @Override
            public void onData(List<SolveType> data) {
              solveTypes = data;
              if (solveTypes != null && !solveTypes.isEmpty()) {
                curSolveType = solveTypes.get(0);
                buSolveType.setText(curSolveType.getName());
              }
            }
          });
        }
      }
    });
  }

}
