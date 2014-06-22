package com.cube.nanotimer.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.cube.nanotimer.App;
import com.cube.nanotimer.CubeType;
import com.cube.nanotimer.R;
import com.cube.nanotimer.activity.widget.SelectionHandler;
import com.cube.nanotimer.activity.widget.SelectorFragment;

import java.util.ArrayList;

public class MainScreenActivity extends Activity implements SelectionHandler {

  private Button buCubeType;
  private CubeType curCubeType;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mainscreen);
    App.setContext(this);

    buCubeType = (Button) findViewById(R.id.buCubeType);
    buCubeType.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        ArrayList<String> types = new ArrayList<String>();
        for (CubeType t : CubeType.values()) {
          types.add(t.getName());
        }
        DialogFragment df = SelectorFragment.newInstance(types, MainScreenActivity.this);
        df.show(getFragmentManager(), "dialog");
      }
    });

    findViewById(R.id.buStart).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent i = new Intent(MainScreenActivity.this, TimerActivity.class);
        i.putExtra("type", curCubeType);
        startActivity(i);
      }
    });

    curCubeType = CubeType.THREE_BY_THREE;
  }

  @Override
  public void onItemSelected(int position) {
    curCubeType = CubeType.values()[position];
    buCubeType.setText(curCubeType.getName());
  }

}
