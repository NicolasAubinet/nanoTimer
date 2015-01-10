package com.cube.nanotimer.gui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveHistory;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class GraphActivity extends Activity {

  private CubeType cubeType;
  private SolveType solveType;

  private LineChart chart;
  private Spinner spPeriod;

  public enum Period {
    DAY(1),
    WEEK(7),
    MONTH(31),
    YEAR(365),
    ALL(0);
    private int days;
    Period(int days) {
      this.days = days;
    }
    private long getPeriodStart() {
      if (days == 0) {
        return 0;
      }
      return System.currentTimeMillis() - (((long) days) * 24*60*60*1000);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    if (!App.INSTANCE.isProEnabled()) {
      finish();
      return;
    }
    super.onCreate(savedInstanceState);
    setContentView(R.layout.graph_screen);
    setTitle(R.string.progression_graph);

    cubeType = (CubeType) getIntent().getSerializableExtra("cubeType");
    solveType = (SolveType) getIntent().getSerializableExtra("solveType");

    // Labels
    ((TextView) findViewById(R.id.tvCubeType)).setText(cubeType.getName());
    ((TextView) findViewById(R.id.tvSolveType)).setText(solveType.getName());

    // Spinner
    spPeriod = (Spinner) findViewById(R.id.spPeriod);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.graph_periods, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spPeriod.setAdapter(adapter);
    spPeriod.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        refreshData();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });

    // Chart
    chart = (LineChart) findViewById(R.id.chart);
    chart.setDescription("");
    chart.setDrawLegend(false);
    chart.setHighlightEnabled(false);
    chart.setGridColor(getColor(R.color.gray600));
    chart.setBackgroundColor(getColor(R.color.white));
    chart.setDrawYValues(false);
  }

  private void refreshData() {
    Log.i("[NanoTimer]", "Get times starting at " + getSelectedPeriod().getPeriodStart() + " for period " + getSelectedPeriod());
    App.INSTANCE.getService().getHistory(solveType, getSelectedPeriod().getPeriodStart(), new DataCallback<SolveHistory>() {
      @Override
      public void onData(final SolveHistory data) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            refreshData(data);
          }
        });
      }
    });
  }

  private void refreshData(SolveHistory data) {
    ArrayList<Entry> times = new ArrayList<Entry>();
    ArrayList<String> xLabels = new ArrayList<String>();
    int timesCount = data.getSolveTimes().size();
    for (int i = 0; i < timesCount; i++) {
      SolveTime solveTime = data.getSolveTimes().get(i);
      if (solveTime.getTime() > 0) {
        times.add(new Entry(solveTime.getTime(), ((timesCount - 1) - i)));
        xLabels.add(FormatterService.INSTANCE.formatGraphDateTime(solveTime.getTimestamp(), getSelectedPeriod()));
      }
    }

    LineDataSet dataSet = new LineDataSet(times, getString(R.string.times));
    dataSet.setColor(getColor(R.color.iceblue));
    dataSet.setCircleColor(getColor(R.color.iceblue));
    dataSet.setCircleSize(3f);

    ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
    dataSets.add(dataSet);

    LineData chartData = new LineData(xLabels, dataSets);
    chart.setData(chartData);
    chart.invalidate();
//    chart.setValueFormatter(new ValueFormatter() {
//      @Override
//      public String getFormattedValue(float value) {
//        return FormatterService.INSTANCE.formatSolveTime((long) value);
//      }
//    });
  }

  private int getColor(int colorRes) {
    return getResources().getColor(colorRes);
  }

  private Period getSelectedPeriod() {
    int pos = spPeriod.getSelectedItemPosition();
    Period[] periods = Period.values();
    if (pos >= 0 && pos < periods.length) {
      return periods[pos];
    } else {
      return Period.DAY;
    }
  }

}
