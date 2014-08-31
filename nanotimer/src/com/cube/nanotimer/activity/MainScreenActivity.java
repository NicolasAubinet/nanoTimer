package com.cube.nanotimer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;
import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.Options.AdsStyle;
import com.cube.nanotimer.R;
import com.cube.nanotimer.activity.widget.AboutDialog;
import com.cube.nanotimer.activity.widget.HistoryDetailDialog;
import com.cube.nanotimer.activity.widget.SelectionHandler;
import com.cube.nanotimer.activity.widget.SelectorFragmentDialog;
import com.cube.nanotimer.activity.widget.TimeChangedHandler;
import com.cube.nanotimer.activity.widget.ads.AdProvider;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.Utils;
import com.cube.nanotimer.util.YesNoListener;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.CubeType.Type;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import com.startapp.android.publish.banner.Banner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MainScreenActivity extends FragmentActivity implements TimeChangedHandler, SelectionHandler {

  private Button buCubeType;
  private Button buSolveType;
  private ListView lvHistory;

  private CubeType curCubeType;
  private SolveType curSolveType;
  private List<CubeType> cubeTypes;
  private List<SolveType> solveTypes;

  private List<SolveTime> liHistory = new ArrayList<SolveTime>();
  private HistoryListAdapter adapter;

  private int previousLastItem = 0;

  private Toast quitMessage;
  private boolean inQuitMode;
  private static final long QUIT_MODE_DELAY = 3000;

  private boolean mixedAdBannerChance; // chance to not display banner if in mixed ad mode
                                       // used to avoid displaying/hiding the banner by changing the screen orientation

  private static final int ID_CUBETYPE = 1;
  private static final int ID_SOLVETYPE = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AdProvider.init(this);
    setContentView(R.layout.mainscreen_screen);
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
              SelectorFragmentDialog.newInstance(ID_CUBETYPE, types, true, MainScreenActivity.this));
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
              SelectorFragmentDialog.newInstance(ID_SOLVETYPE, types, true, MainScreenActivity.this));
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
            HistoryDetailDialog.newInstance(liHistory.get(i), curCubeType, MainScreenActivity.this));
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
              public void onData(final List<SolveTime> data) {
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    liHistory.addAll(data);
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
    AdProvider.resume();
    refreshCubeTypes();

    mixedAdBannerChance = new Random().nextInt(10) < 2; // 20% chance to not show banner in mixed mode
    showHideBannerAd();
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    AdProvider.pause();
  }

  @Override
  public void onBackPressed() {
    if (AdProvider.isInterstialAppnextDisplayed()) {
      // pressing backspace on appnext ad does not hide it by default.
      // using this to "force" the user to click on the close button, otherwise he could select the "interstitial" option
      // and just push 2 times on backspace to never really see the ads.
      // with this condition, he can't do it as it would close the app.
      return;
    }
    if (inQuitMode) {
      if (quitMessage != null) {
        quitMessage.cancel();
      }
      // TODO : should maybe close the app completely (see if better to keep it in background or to kill it)
      super.onBackPressed();
    } else {
      quitMessage = Toast.makeText(this, R.string.backspace_exit, Toast.LENGTH_LONG);
      quitMessage.show();
      inQuitMode = true;
      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          quitMessage.cancel();
          inQuitMode = false;
        }
      }, QUIT_MODE_DELAY);
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
      case R.id.itAbout:
        Utils.showFragment(this, AboutDialog.newInstance());
        break;
    }
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    String cubeTypeText = buCubeType.getText().toString();
    String solveTypeText = buSolveType.getText().toString();

    setContentView(R.layout.mainscreen_screen);
    initViews();

    buCubeType.setText(cubeTypeText);
    buSolveType.setText(solveTypeText);
    showHideBannerAd();
  }

  private void refreshCubeTypes() {
    App.INSTANCE.getService().getCubeTypes(false, new DataCallback<List<CubeType>>() {
      @Override
      public void onData(List<CubeType> data) {
        cubeTypes = data;
        if (cubeTypes != null && !cubeTypes.isEmpty()) {
          CubeType defaultCubeType = null;
          CubeType newCubeType = null;
          for (CubeType ct : cubeTypes) {
            if (curCubeType != null && curCubeType.getId() == ct.getId()) {
              newCubeType = ct;
            }
            if (ct.getId() == Type.THREE_BY_THREE.getId()) {
              defaultCubeType = ct;
            }
          }
          curCubeType = newCubeType != null ? newCubeType : defaultCubeType != null ? defaultCubeType : cubeTypes.get(0);
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
      solveTypes = Collections.EMPTY_LIST;
      refreshButtonTexts();
      refreshHistory();
    }
  }

  private void refreshHistory() {
    previousLastItem = 0;
    if (curSolveType != null) {
      App.INSTANCE.getService().getHistory(curSolveType, new DataCallback<List<SolveTime>>() {
        @Override
        public void onData(final List<SolveTime> data) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              liHistory.clear();
              liHistory.addAll(data);
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
          liHistory.clear();
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

  private void showHideBannerAd() {
    AdsStyle adsStyle = Options.INSTANCE.getAdsStyle();
    Banner bannerAd = (Banner) findViewById(R.id.bannerAd);
    if (adsStyle == AdsStyle.BANNER || (adsStyle == AdsStyle.MIXED && !AdProvider.wasInterstitialShown() && !mixedAdBannerChance)) {
      // Show banner add if the "banner" option is selected,
      // or if "mixed" is selected and that an interstitial was not shown when coming back here, + 20% chances to not show anything
      bannerAd.showBanner();
    } else {
      bannerAd.hideBanner();
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
