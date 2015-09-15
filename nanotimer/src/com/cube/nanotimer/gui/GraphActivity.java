package com.cube.nanotimer.gui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
import com.cube.nanotimer.util.chart.ChartData;
import com.cube.nanotimer.util.chart.ChartUtils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.FrequencyData;
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

public class GraphActivity extends ActionBarActivity {

  private CubeType cubeType;
  private SolveType solveType;
  private List<ChartData> chartData;

  private LineChart chart;
  private Spinner spPeriod;
  private Spinner spGraphType;
  private CheckBox cbSmooth;
  private SharedPreferences prefs;

  enum Period {
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

  enum GraphType {
    PROGRESSION {
      @Override
      public String formatValue(float value) {
        return FormatterService.INSTANCE.formatSolveTime(Math.round((double) value));
      }

      @Override
      public String formatXLabel(long value) {
        return FormatterService.INSTANCE.formatDateTime(value);
      }
    },
    FREQUENCY {
      @Override
      public String formatValue(float value) {
        return FormatterService.INSTANCE.formatFloat(value, 2);
      }

      @Override
      public String formatXLabel(long value) {
        return FormatterService.INSTANCE.formatDate(value);
      }
    };

    public abstract String formatValue(float value);
    public abstract String formatXLabel(long value);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    if (!App.INSTANCE.isProEnabled()) {
      finish();
      return;
    }
    super.onCreate(savedInstanceState);
    setContentView(R.layout.graph_screen);

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
        if (chartData != null) {
          getData();
        }
      }
    });

    spPeriod = (Spinner) findViewById(R.id.spPeriod);
    configureSpinner(spPeriod, R.array.graph_periods, "period");
    spGraphType = (Spinner) findViewById(R.id.spGraphType);
    configureSpinner(spGraphType, R.array.graph_types, "graph_type");

    chart = (LineChart) findViewById(R.id.chart);
    chart.setDescription("");
    chart.setDrawLegend(false);
    chart.setHighlightEnabled(false);
    chart.setGridColor(getColor(R.color.gray800));
    chart.setBackgroundColor(getColor(R.color.graybg));
    chart.setDrawGridBackground(false);
    chart.setDrawYValues(false);
    chart.setNoDataText("");

    ValueFormatter valueFormatter = new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        return getSelectedGraphType().formatValue(value);
      }
    };

    chart.getXLabels().setSpaceBetweenLabels(1);
    chart.getXLabels().setTextColor(getColor(R.color.white));
    chart.getXLabels().setTextSize(12);
    chart.setValueFormatter(valueFormatter);

    chart.getYLabels().setTextColor(getColor(R.color.white));
    chart.getYLabels().setTextSize(12);
    chart.getYLabels().setFormatter(valueFormatter);
  }

  private Spinner configureSpinner(Spinner spinner, int dataArray, final String prefsKey) {
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, dataArray, R.layout.spinner_item);
    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
    spinner.setAdapter(adapter);
    spinner.setSelection(prefs.getInt(prefsKey, 0));
    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Editor editor = prefs.edit();
        editor.putInt(prefsKey, pos);
        editor.commit();
        getData();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });
    return spinner;
  }

  private void getData() {
    GraphType selectedGraphType = getSelectedGraphType();
    if (selectedGraphType == GraphType.PROGRESSION) {
      getProgressionData();
    } else if (selectedGraphType == GraphType.FREQUENCY) {
      getFrequencyData();
    }
  }

  private void getProgressionData() {
    App.INSTANCE.getService().getHistory(solveType, getSelectedPeriod().getPeriodStart(), new DataCallback<SolveHistory>() {
      @Override
      public void onData(final SolveHistory data) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            chartData = getChartTimesFromSolveHistory(data);
            refreshData();
          }
        });
      }
    });
  }

  private void getFrequencyData() {
    App.INSTANCE.getService().getFrequencyData(solveType, getSelectedPeriod().getPeriodStart(), new DataCallback<List<FrequencyData>>() {
      @Override
      public void onData(final List<FrequencyData> frequencyData) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            chartData = getChartDataFromFrequency(frequencyData);
            refreshData();
          }
        });
      }
    });
  }

  private void refreshData() {
    chart.setNoDataText(getString(R.string.no_data_found)); // done here to avoid displaying that message when data is loading
    GraphType selectedGraphType = getSelectedGraphType();
    List<ChartData> data;
    if (cbSmooth.isChecked()) {
      data = ChartUtils.getSmoothedChartTimes(chartData);
    } else {
      data = chartData;
    }
    chart.clear();
    if (data.isEmpty()) {
      return;
    }
    ArrayList<Entry> times = new ArrayList<Entry>();
    ArrayList<String> xLabels = new ArrayList<String>();
    for (ChartData solveTime : data) {
      times.add(new Entry(solveTime.getData(), times.size()));
      xLabels.add(selectedGraphType.formatXLabel(solveTime.getTimestamp()));
    }

    LineDataSet dataSet = new LineDataSet(times, "");//getString(R.string.times));
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

  private GraphType getSelectedGraphType() {
    int pos = spGraphType.getSelectedItemPosition();
    GraphType[] graphTypes = GraphType.values();
    if (pos >= 0 && pos < graphTypes.length) {
      return graphTypes[pos];
    } else {
      return GraphType.PROGRESSION;
    }
  }

  private List<ChartData> getChartTimesFromSolveHistory(SolveHistory solveHistory) {
    List<ChartData> chartTimes = new ArrayList<ChartData>();
    for (int i = solveHistory.getSolveTimes().size() - 1; i >= 0; i--) {
      SolveTime solveTime = solveHistory.getSolveTimes().get(i);
      if (solveTime.getTime() > 0) {
        chartTimes.add(new ChartData(solveTime.getTime(), solveTime.getTimestamp()));
      }
    }
    return chartTimes;
  }

  private List<ChartData> getChartDataFromFrequency(List<FrequencyData> frequencyData) {
    List<ChartData> chartData = new ArrayList<ChartData>();
    for (FrequencyData curFrequencyData : frequencyData) {
      chartData.add(new ChartData(curFrequencyData.getSolvesCount(), curFrequencyData.getDay()));
    }
    return chartData;
  }

}
