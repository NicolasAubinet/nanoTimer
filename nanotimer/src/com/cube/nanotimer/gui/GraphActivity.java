package com.cube.nanotimer.gui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
import com.github.mikephil.charting.utils.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GraphActivity extends Activity {

  private CubeType cubeType;
  private SolveType solveType;
  private List<ChartTime> chartTimes;

  private LineChart chart;
  private Spinner spPeriod;
  private CheckBox cbSmooth;
  private SharedPreferences prefs;

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
      } else if (this == DAY) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTimeInMillis();
      }
      return System.currentTimeMillis() - (((long) days) * 24 * 60 * 60 * 1000);
    }
  }

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

    ((TextView) findViewById(R.id.tvCubeType)).setText(cubeType.getName());
    ((TextView) findViewById(R.id.tvSolveType)).setText(solveType.getName());

    prefs = getSharedPreferences("graph", 0);

    cbSmooth = (CheckBox) findViewById(R.id.cbSmooth);
    cbSmooth.setChecked(prefs.getBoolean("smooth", false));
    cbSmooth.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Editor editor = prefs.edit();
        editor.putBoolean("smooth", b);
        editor.commit();
        if (chartTimes != null) {
          refreshData();
        }
      }
    });

    spPeriod = (Spinner) findViewById(R.id.spPeriod);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.graph_periods, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spPeriod.setAdapter(adapter);
    spPeriod.setSelection(prefs.getInt("period", 0));
    spPeriod.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Editor editor = prefs.edit();
        editor.putInt("period", pos);
        editor.commit();
        getData();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });

    chart = (LineChart) findViewById(R.id.chart);
    chart.setDescription("");
    chart.setDrawLegend(false);
    chart.setHighlightEnabled(false);
    chart.setGridColor(getColor(R.color.gray600));
    chart.setBackgroundColor(getColor(R.color.white));
    chart.setDrawYValues(false);
    chart.setValueFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        return FormatterService.INSTANCE.formatSolveTime(Math.round((double) value));
      }
    });
    chart.getYLabels().setFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        return FormatterService.INSTANCE.formatGraphTimeYLabel(Math.round((double) value));
      }
    });
  }

  private void getData() {
    App.INSTANCE.getService().getHistory(solveType, getSelectedPeriod().getPeriodStart(), new DataCallback<SolveHistory>() {
      @Override
      public void onData(final SolveHistory data) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            chartTimes = getChartTimesFromSolveHistory(data);
            refreshData();
          }
        });
      }
    });
  }

  private void refreshData() {
    List<ChartTime> data = getChartTimes(cbSmooth.isChecked());
    chart.clear();
    if (data.isEmpty()) {
      return;
    }
    ArrayList<Entry> times = new ArrayList<Entry>();
    ArrayList<String> xLabels = new ArrayList<String>();
    for (int i = 0; i < data.size(); i++) {
      ChartTime solveTime = data.get(i);
      times.add(new Entry(solveTime.getTime(), times.size()));
//      xLabels.add(FormatterService.INSTANCE.formatGraphDateTime(solveTime.getTimestamp(), getSelectedPeriod()));
      xLabels.add(FormatterService.INSTANCE.formatDateTime(solveTime.getTimestamp()));
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

  /**
   * Return the times that will be displayed on the map
   * @param smooth if true, the times will be averaged (to smooth the graph data)
   * @return the chart data to display
   */
  private List<ChartTime> getChartTimes(boolean smooth) {
    if (!smooth) {
      return chartTimes;
    }
    int averageTimesCount = 2; // number of times to average together around each time (bigger will smooth out more)
    final int totalTimesToShow = 50; // maximum number of times to display (approximation, might be a bit more)
    final int timesToKeep = Math.max(1, chartTimes.size() / totalTimesToShow); // will keep 1 time for every timesToKeep times
    if (averageTimesCount < timesToKeep - 1) { // if too many times, adjust averageTimesCount to avoid losing times while averaging
      averageTimesCount = timesToKeep - 1;
    }
    List<ChartTime> times = new ArrayList<ChartTime>();
    for (int i = 0; i < chartTimes.size(); i += timesToKeep) {
      long total = 0;
      int start = Math.max(0, i - averageTimesCount);
      int end = Math.min(i + averageTimesCount + 1, chartTimes.size());
      for (int j = start; j < end; j++) {
        total += chartTimes.get(j).getTime();
      }
      long time = total / (end - start);
      times.add(new ChartTime(time, chartTimes.get(i).getTimestamp()));
    }
    return times;
  }

  private List<ChartTime> getChartTimesFromSolveHistory(SolveHistory solveHistory) {
    List<ChartTime> chartTimes = new ArrayList<ChartTime>();
    for (int i = solveHistory.getSolveTimes().size() - 1; i >= 0; i--) {
      SolveTime solveTime = solveHistory.getSolveTimes().get(i);
      if (solveTime.getTime() > 0) {
        chartTimes.add(new ChartTime(solveTime.getTime(), solveTime.getTimestamp()));
      }
    }
    return chartTimes;
  }

  class ChartTime {
    private long time;
    private long timestamp;

    ChartTime(long time, long timestamp) {
      this.time = time;
      this.timestamp = timestamp;
    }

    public long getTime() {
      return time;
    }

    public long getTimestamp() {
      return timestamp;
    }
  }

}
