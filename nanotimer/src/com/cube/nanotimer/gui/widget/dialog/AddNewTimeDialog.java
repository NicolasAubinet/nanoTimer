package com.cube.nanotimer.gui.widget.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.HistoryRefreshHandler;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

public class AddNewTimeDialog extends ConfirmDialog {

  private HistoryRefreshHandler historyRefreshHandler;
  private SolveType solveType;

  public static AddNewTimeDialog newInstance(HistoryRefreshHandler historyRefreshHandler, SolveType solveType) {
    AddNewTimeDialog frag = new AddNewTimeDialog(historyRefreshHandler, solveType);
    return frag;
  }

  private AddNewTimeDialog(HistoryRefreshHandler historyRefreshHandler, SolveType solveType) {
    this.historyRefreshHandler = historyRefreshHandler;
    this.solveType = solveType;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    dialog = getDialog(R.string.add);
    return dialog;
  }

  @Override
  protected void onConfirm() {
    int minutes = ((WheelView) view.findViewById(R.id.wvMinutes)).getCurrentItem();
    int seconds = ((WheelView) view.findViewById(R.id.wvSeconds)).getCurrentItem();
    int hundreds = ((WheelView) view.findViewById(R.id.wvHundreds)).getCurrentItem();
    long time = minutes * 60000;
    time += seconds * 1000;
    time += hundreds * 10;

    SolveTime solveTime = new SolveTime();
    solveTime.setTime(time);
    solveTime.setTimestamp(System.currentTimeMillis());
    solveTime.setSolveType(solveType);

    App.INSTANCE.getService().saveTime(solveTime, new DataCallback<SolveAverages>() {
      @Override
      public void onData(SolveAverages data) {
        if (historyRefreshHandler != null) {
          historyRefreshHandler.refreshHistory();
        }
      }
    });

    dialog.dismiss();
  }

  @Override
  protected View getCustomView() {
    LayoutInflater factory = LayoutInflater.from(getActivity());
    View view = factory.inflate(R.layout.add_new_time_dialog, null);

    WheelView minutes = (WheelView) view.findViewById(R.id.wvMinutes);
    minutes.setViewAdapter(new NumericWheelAdapter(getActivity(), 0, 59));
    minutes.setCurrentItem(0);
    minutes.setCyclic(true);

    WheelView seconds = (WheelView) view.findViewById(R.id.wvSeconds);
    seconds.setViewAdapter(new NumericWheelAdapter(getActivity(), 0, 59));
    seconds.setCurrentItem(0);
    seconds.setCyclic(true);

    WheelView hundreds = (WheelView) view.findViewById(R.id.wvHundreds);
    hundreds.setViewAdapter(new NumericWheelAdapter(getActivity(), 0, 99));
    hundreds.setCurrentItem(0);
    hundreds.setCyclic(true);

    return view;
  }

}
