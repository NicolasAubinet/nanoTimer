package com.cube.nanotimer.gui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.Options.AdsStyle;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.*;
import com.cube.nanotimer.gui.widget.ads.AdProvider;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.YesNoListener;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveHistory;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import com.startapp.android.publish.banner.Banner;

import java.util.*;

public class MainScreenActivity extends ActionBarActivity implements TimeChangedHandler, SelectionHandler {

  private Button buCubeType;
  private Button buSolveType;
  private ListView lvHistory;
  private TextView tvSolvesCount;

  private CubeType curCubeType;
  private SolveType curSolveType;
  private List<CubeType> cubeTypes;
  private List<SolveType> solveTypes;
  private int solvesCount;

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
    App.INSTANCE.setContext(this);
    AdProvider.init(this);
    setContentView(R.layout.mainscreen_screen);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    curCubeType = Utils.getCurrentCubeType(this);
    curSolveType = new SolveType(Utils.getCurrentSolveTypeId(this), "", false, curCubeType.getId());

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
          DialogUtils.showFragment(MainScreenActivity.this,
              SelectorFragmentDialog.newInstance(ID_CUBETYPE, types, true, MainScreenActivity.this));
        }
      }
    });
    buCubeType.setShadowLayer(1, 3f, 3f, getResources().getColor(R.color.black));

    buSolveType = (Button) findViewById(R.id.buSolveType);
    buSolveType.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (solveTypes != null) {
          ArrayList<String> types = new ArrayList<String>();
          for (SolveType t : solveTypes) {
            types.add(t.getName());
          }
          DialogUtils.showFragment(MainScreenActivity.this,
              SolveTypesFragmentDialog.newInstance(ID_SOLVETYPE, types, true, MainScreenActivity.this,
                  Options.INSTANCE.isSolveTypesShortcutEnabled()));
        }
      }
    });
    buSolveType.setShadowLayer(1, 3f, 3f, getResources().getColor(R.color.black));

    tvSolvesCount = (TextView) findViewById(R.id.tvSolvesCount);

    initHistoryList();

    Button buStart = (Button) findViewById(R.id.buStart);
    buStart.setShadowLayer(1, 3f, 3f, getResources().getColor(R.color.black));
    buStart.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent i = new Intent(MainScreenActivity.this, TimerActivity.class);
        i.putExtra("cubeType", curCubeType);
        i.putExtra("solveType", curSolveType);
        i.putExtra("solvesCount", solvesCount);
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
        DialogUtils.showFragment(MainScreenActivity.this,
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
            App.INSTANCE.getService().getPagedHistory(curSolveType, from, new DataCallback<SolveHistory>() {
              @Override
              public void onData(final SolveHistory data) {
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    setSolvesCount(data.getSolvesCount());
                    liHistory.addAll(data.getSolveTimes());
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
    App.INSTANCE.setContext(this);
    App.INSTANCE.onResume();
    AdProvider.resume();
    refreshCubeTypes();

    mixedAdBannerChance = new Random().nextInt(10) < 2; // 20% chance to not show banner in mixed mode
    showHideBannerAd();
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
        DialogUtils.showYesNoConfirmation(this, R.string.clear_history_solve_type_confirmation, new YesNoListener() {
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
      case R.id.itGraphs:
        if (Utils.checkProFeature(this)) {
          Intent i = new Intent(this, GraphActivity.class);
          i.putExtra("cubeType", curCubeType);
          i.putExtra("solveType", curSolveType);
          startActivity(i);
        }
        break;
      case R.id.itAbout:
        DialogUtils.showFragment(this, AboutDialog.newInstance());
        break;
    }
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    String cubeTypeText = buCubeType.getText().toString();
    String solveTypeText = buSolveType.getText().toString();
    String solvesCountText = tvSolvesCount.getText().toString();

    setContentView(R.layout.mainscreen_screen);
    initViews();

    buCubeType.setText(cubeTypeText);
    buSolveType.setText(solveTypeText);
    tvSolvesCount.setText(solvesCountText);
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
            if (ct.getId() == CubeType.THREE_BY_THREE.getId()) {
              defaultCubeType = ct;
            }
          }
          setCurCubeType(newCubeType != null ? newCubeType : defaultCubeType != null ? defaultCubeType : cubeTypes.get(0));
        } else {
          setCurCubeType(null);
        }
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
                  setCurSolveType(st);
                  foundType = true;
                }
              }
            }
            if (!foundType) {
              setCurSolveType(solveTypes.get(0));
            }
          } else {
            setCurSolveType(null);
          }
          refreshButtonTexts();
          refreshHistory();
        }
      });
    } else {
      setCurSolveType(null);
      solveTypes = Collections.EMPTY_LIST;
      refreshButtonTexts();
      refreshHistory();
    }
  }

  private void refreshHistory() {
    previousLastItem = 0;
    if (curSolveType != null) {
      App.INSTANCE.getService().getPagedHistory(curSolveType, new DataCallback<SolveHistory>() {
        @Override
        public void onData(final SolveHistory data) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              setSolvesCount(data.getSolvesCount());
              liHistory.clear();
              liHistory.addAll(data.getSolveTimes());
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
        setSolvesCount(solvesCount - 1);
        break;
      }
    }
  }

  @Override
  public void itemSelected(int id, int position) {
    if (position >= 0) {
      if (id == ID_CUBETYPE) {
        setCurCubeType(cubeTypes.get(position));
        buCubeType.setText(curCubeType.getName());
        refreshSolveTypes();
      } else if (id == ID_SOLVETYPE) {
        if (position >= 0 && position < solveTypes.size()) {
          setCurSolveType(solveTypes.get(position));
          buSolveType.setText(curSolveType.getName());
          refreshHistory();
        } else {
          // solve types shortcut
          Intent i = new Intent(this, SolveTypesActivity.class);
          i.putExtra("cubeType", curCubeType);
          startActivity(i);
        }
      }
    }
  }

  private void setSolvesCount(int solvesCount) {
    this.solvesCount = solvesCount;
    tvSolvesCount.setText(String.valueOf(solvesCount) + " " + getString(R.string.solves));
  }

  private void setCurCubeType(CubeType cubeType) {
    this.curCubeType = cubeType;
    Utils.setCurrentCubeType(this, cubeType);
  }

  private void setCurSolveType(SolveType solveType) {
    this.curSolveType = solveType;
    Utils.setCurrentSolveType(this, solveType);
  }

  private void showHideBannerAd() {
    AdsStyle adsStyle = Options.INSTANCE.getAdsStyle();
    Banner bannerAd = (Banner) findViewById(R.id.bannerAd);
    if (Options.INSTANCE.isAdsEnabled() &&
        (adsStyle == AdsStyle.BANNER ||
        (adsStyle == AdsStyle.MIXED && !AdProvider.wasInterstitialShown() && !mixedAdBannerChance))) {
      // Show banner add if the "banner" option is selected,
      // or if "mixed" is selected and that an interstitial was not shown when coming back here, + 20% chances to not show anything
      bannerAd.showBanner();
    } else {
      bannerAd.hideBanner();
    }
  }

  private class HistoryListAdapter extends ArrayAdapter<SolveTime> {
    private LayoutInflater inflater;

    public HistoryListAdapter(Context context, int textViewResourceId, List<SolveTime> objects) {
      super(context, textViewResourceId, objects);
      inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
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
