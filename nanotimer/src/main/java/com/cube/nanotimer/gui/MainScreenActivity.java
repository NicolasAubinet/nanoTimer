package com.cube.nanotimer.gui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.Options.AdsStyle;
import com.cube.nanotimer.ProChecker;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.AboutDialog;
import com.cube.nanotimer.gui.widget.HistoryDetailDialog;
import com.cube.nanotimer.gui.widget.ResultListener;
import com.cube.nanotimer.gui.widget.SelectionHandler;
import com.cube.nanotimer.gui.widget.SelectorFragmentDialog;
import com.cube.nanotimer.gui.widget.TimeChangedHandler;
import com.cube.nanotimer.gui.widget.ads.AdProvider;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.YesNoListener;
import com.cube.nanotimer.util.exportimport.ErrorListener;
import com.cube.nanotimer.util.exportimport.csvimport.CSVImporter;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.NameHolder;
import com.cube.nanotimer.vo.SolveHistory;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.vo.TimesSort;
import com.startapp.android.publish.banner.Banner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MainScreenActivity extends DrawerLayoutActivity implements TimeChangedHandler, SelectionHandler, ResultListener {

  private Spinner spCubeType;
  private Spinner spSolveType;
  private ListView lvHistory;
  private TextView tvSolvesCount;
  private TextView tvHistory;

  private MenuItem miSortMode;

  private CubeType curCubeType;
  private SolveType curSolveType;
  private final List<CubeType> cubeTypes = new ArrayList<>();
  private final List<SolveType> solveTypes = new ArrayList<>();
  private NameHolderSpinnerAdapter cubeTypesSpinnerAdapter;
  private NameHolderSpinnerAdapter solveTypesSpinnerAdapter;

  private int solvesCount;
  private TimesSort timesSort = TimesSort.TIMESTAMP;
  private boolean refreshingHistory;

  private final List<SolveTime> liHistory = new ArrayList<>();
  private HistoryListAdapter historyListAdapter;

  private int previousLastItem = 0;

  private Toast quitMessage;
  private boolean inQuitMode;
  private static final long QUIT_MODE_DELAY = 3000;

  private boolean mixedAdBannerChance; // chance to not display banner if in mixed ad mode
                                       // used to avoid displaying/hiding the banner by changing the screen orientation

  private static final int ID_CUBETYPE = 1;
  private static final int ID_SOLVETYPE = 2;
  private static final int ID_IMPORTEXPORT = 3;

  private static final int IMPORT_REQUEST_CODE = 1;

  private static final int REQUEST_READ_PERMISSIONS_CODE = 10;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    App.INSTANCE.setContext(this);
    AdProvider.init(this);
    setContentView(R.layout.mainscreen_screen);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    curCubeType = Utils.getCurrentCubeType(this);
    curSolveType = new SolveType(Utils.getCurrentSolveTypeId(this), "", false, null, curCubeType.getId());

    initViews();
  }

  @Override
  protected void initViews() {
    super.initViews();
    spCubeType = (Spinner) findViewById(R.id.spCubeType);
    cubeTypesSpinnerAdapter = new NameHolderSpinnerAdapter(this, R.id.spCubeType, cubeTypes, false);
    cubeTypesSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item_scaling_layout);
    spCubeType.setAdapter(cubeTypesSpinnerAdapter);
    spCubeType.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        itemSelected(ID_CUBETYPE, i);
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });

    spSolveType = (Spinner) findViewById(R.id.spSolveType);
    solveTypesSpinnerAdapter = new NameHolderSpinnerAdapter(this, R.id.spSolveType, solveTypes, true);
    solveTypesSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item_scaling_layout);
    spSolveType.setAdapter(solveTypesSpinnerAdapter);
    spSolveType.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        itemSelected(ID_SOLVETYPE, i);
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });

    tvSolvesCount = (TextView) findViewById(R.id.tvSolvesCount);
    tvHistory = (TextView) findViewById(R.id.tvHistory);

    initHistoryList();

    miSortMode = findMenuItem(R.id.itSortMode);
    setSortMode(TimesSort.TIMESTAMP);

    Button buStart = (Button) findViewById(R.id.buStart);
//    buStart.setShadowLayer(1, 3f, 3f, getResources().getColor(R.color.black));
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
    historyListAdapter = new HistoryListAdapter(MainScreenActivity.this, R.id.lvHistory, liHistory);
    lvHistory = (ListView) findViewById(R.id.lvHistory);
    lvHistory.setAdapter(historyListAdapter);
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
        if (view.getId() == R.id.lvHistory && !liHistory.isEmpty() && !refreshingHistory) {
          int lastVisibleItem = firstVisibleItem + visibleItemCount;
          if (totalItemCount == lastVisibleItem && lastVisibleItem != previousLastItem) {
            previousLastItem = lastVisibleItem;
            long from;
            if (timesSort == TimesSort.TIME) {
              from = liHistory.get(liHistory.size() - 1).getTime();
            } else {
              from = liHistory.get(liHistory.size() - 1).getTimestamp();
            }
            App.INSTANCE.getService().getPagedHistory(curSolveType, from, timesSort, new DataCallback<SolveHistory>() {
              @Override
              public void onData(final SolveHistory data) {
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    setSolvesCount(data.getSolvesCount());
                    liHistory.addAll(data.getSolveTimes());
                    historyListAdapter.notifyDataSetChanged();
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

    ProChecker.ProState proState = ProChecker.getProState(this);
    if (proState == ProChecker.ProState.ENABLED) {
      findViewById(R.id.tvUpdateProApp).setVisibility(View.GONE);
    } else {
      if (proState == ProChecker.ProState.INVALID_VERSION) {
        findViewById(R.id.tvUpdateProApp).setVisibility(View.VISIBLE);
      } else {
        findViewById(R.id.tvUpdateProApp).setVisibility(View.GONE);
      }
      AdProvider.resume();
    }

    refreshCubeTypes();

    setSortMode(TimesSort.TIMESTAMP);

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
      case R.id.itSettings:
        startActivity(new Intent(this, OptionsActivity.class));
        break;
      case R.id.itSortMode:
        if (timesSort == TimesSort.TIMESTAMP) {
          setSortMode(TimesSort.TIME);
        } else if (timesSort == TimesSort.TIME) {
          setSortMode(TimesSort.TIMESTAMP);
        }
        break;
      case R.id.itGraphs:
        if (Utils.checkProFeature(this)) {
          Intent i = new Intent(this, GraphActivity.class);
          i.putExtra("cubeType", curCubeType);
          i.putExtra("solveType", curSolveType);
          startActivity(i);
        }
        break;
      case R.id.itImportExport:
        if (Utils.checkProFeature(this)) {
          ArrayList<String> items = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.import_export)));
          DialogUtils.showFragment(this, SelectorFragmentDialog.newInstance(ID_IMPORTEXPORT, items, true, this));
        }
        break;
      case R.id.itAbout:
        DialogUtils.showFragment(this, AboutDialog.newInstance());
        break;
    }
    return true;
  }

  /*@Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    int cubeTypeItemPosition = spCubeType.getSelectedItemPosition();
    int solveTypeItemPosition = spSolveType.getSelectedItemPosition();
    String solvesCountText = tvSolvesCount.getText().toString();
    String historyText = tvHistory.getText().toString();

    setContentView(R.layout.mainscreen_screen);
    initViews();

    spCubeType.setSelection(cubeTypeItemPosition);
    spSolveType.setSelection(solveTypeItemPosition);
    tvSolvesCount.setText(solvesCountText);
    tvHistory.setText(historyText);
    showHideBannerAd();
  }*/

  private void refreshDataSet(final ArrayAdapter arrayAdapter) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        arrayAdapter.notifyDataSetChanged();
      }
    });
  }

  private void refreshCubeTypes() {
    App.INSTANCE.getService().getCubeTypes(false, new DataCallback<List<CubeType>>() {
      @Override
      public void onData(List<CubeType> data) {
        cubeTypes.clear();
        cubeTypes.addAll(data);

        if (!cubeTypes.isEmpty()) {
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

          final CubeType newCurCubeType = newCubeType != null ? newCubeType : defaultCubeType != null ? defaultCubeType : cubeTypes.get(0);
          setCurCubeType(newCurCubeType);

          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              refreshDataSet(cubeTypesSpinnerAdapter);
              spCubeType.setSelection(cubeTypes.indexOf(newCurCubeType));
            }
          });
        } else {
          setCurCubeType(null);
        }
        refreshDataSet(cubeTypesSpinnerAdapter);
        refreshSolveTypes();
      }
    });
  }

  private void refreshSolveTypes() {
    if (curCubeType != null) {
      App.INSTANCE.getService().getSolveTypes(curCubeType, new DataCallback<List<SolveType>>() {
        @Override
        public void onData(List<SolveType> data) {
          solveTypes.clear();
          solveTypes.addAll(data);
          SolveType newCurSolveType = null;

          if (!solveTypes.isEmpty()) {
            boolean foundType = false;
            if (curSolveType != null) {
              for (SolveType st : solveTypes) {
                if (curSolveType.getId() == st.getId()) {
                  newCurSolveType = st;
                  foundType = true;
                }
              }
            }
            if (!foundType) {
              newCurSolveType = solveTypes.get(0);
            }

            setCurSolveType(newCurSolveType);
            final SolveType finalCurSolveType = newCurSolveType;

            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                refreshDataSet(solveTypesSpinnerAdapter);
                spSolveType.setSelection(solveTypes.indexOf(finalCurSolveType));
              }
            });
          } else {
            setCurSolveType(null);
            refreshDataSet(solveTypesSpinnerAdapter);
          }
          refreshHistory();
        }
      });
    } else {
      setCurSolveType(null);
      solveTypes.clear();
      refreshDataSet(solveTypesSpinnerAdapter);
      refreshHistory();
    }
  }

  public void refreshHistory() {
    previousLastItem = 0;
    if (curSolveType != null) {
      refreshingHistory = true;
      App.INSTANCE.getService().getPagedHistory(curSolveType, timesSort, new DataCallback<SolveHistory>() {
        @Override
        public void onData(final SolveHistory data) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              setSolvesCount(data.getSolvesCount());
              liHistory.clear();
              liHistory.addAll(data.getSolveTimes());
              historyListAdapter.notifyDataSetChanged();
              lvHistory.setSelection(0);
              refreshingHistory = false;
            }
          });
        }
      });
    } else {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          liHistory.clear();
          historyListAdapter.notifyDataSetChanged();
        }
      });
    }
  }

  @Override
  public void onResult(Object... params) {
    refreshHistory();
    refreshSolveTypes();
  }

  private void setSortMode(TimesSort timesSort) {
    if (miSortMode == null) {
      return;
    }
    if (timesSort == TimesSort.TIMESTAMP) {
      tvHistory.setText(R.string.history);
      miSortMode.setTitle(R.string.show_best_times);
    } else {
      tvHistory.setText(R.string.best_times);
      miSortMode.setTitle(R.string.show_history);
    }
    if (this.timesSort != timesSort) {
      this.timesSort = timesSort;
      refreshHistory();
    }
  }

  @Override
  public void onTimeChanged(SolveTime solveTime) {
    SolveTime historyTime = null;
    for (SolveTime st : liHistory) {
      if (st.getId() == solveTime.getId()) {
        historyTime = st;
        break;
      }
    }
    if (solveTime != null) {
      updateListTime(historyTime);
    }
  }

  private void updateListTime(final SolveTime solveTime) {
    App.INSTANCE.getService().getSolveTime(solveTime.getId(), new DataCallback<SolveTime>() {
      @Override
      public void onData(final SolveTime data) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            solveTime.setTime(data.getTime());
            solveTime.setPb(data.isPb());
            historyListAdapter.notifyDataSetChanged();
          }
        });
      }
    });
  }

  @Override
  public void onTimeDeleted(SolveTime solveTime) {
    for (Iterator<SolveTime> it = liHistory.iterator(); it.hasNext(); ) {
      SolveTime st = it.next();
      if (st.getId() == solveTime.getId()) {
        it.remove();
        historyListAdapter.notifyDataSetChanged();
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
        refreshSolveTypes();
      } else if (id == ID_SOLVETYPE) {
        if (position >= 0 && position < solveTypes.size()) {
          setCurSolveType(solveTypes.get(position));
          refreshHistory();
        } else {
          // solve types shortcut
          Intent i = new Intent(this, SolveTypesActivity.class);
          i.putExtra("cubeType", curCubeType);
          startActivity(i);
        }
      } else if (id == ID_IMPORTEXPORT) {
        if (position == 0) {
          tryLaunchImportActivity();
        } else if (position == 1) {
          startActivity(new Intent(this, ExportActivity.class));
        }
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == IMPORT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      File file = (File) data.getSerializableExtra("file");
      new CSVImporter(this, this, new ErrorListener() {
        @Override
        public void onError(final String message) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              DialogUtils.showOkDialog(MainScreenActivity.this, getString(R.string.import_error), message);
            }
          });
        }
      }).importData(file);
    }
  }

  private void tryLaunchImportActivity() {
    if (VERSION.SDK_INT >= 19) {
      final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
      if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
//          DialogUtils.showConfirmCancelDialog(MainScreenActivity.this, R.string.import_requires_read_permissions,
//            R.string.confirm, R.string.cancel, new YesNoListener() {
//            @Override
//            public void onYes() {
//              ActivityCompat.requestPermissions(MainScreenActivity.this, new String[] { permission }, REQUEST_READ_PERMISSIONS_CODE);
//            }
//          });
//        } else {
        ActivityCompat.requestPermissions(this, new String[] { permission }, REQUEST_READ_PERMISSIONS_CODE);
//        }
      } else {
        launchImportActivity();
      }
    } else {
      launchImportActivity();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode) {
      case REQUEST_READ_PERMISSIONS_CODE:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          launchImportActivity();
        } else {
          DialogUtils.showShortInfoMessage(this, R.string.read_permission_denied_cant_import);
        }
        break;
      default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  private void launchImportActivity() {
    startActivityForResult(new Intent(this, ImportActivity.class), IMPORT_REQUEST_CODE);
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
          if (st.isPb()) {
            view.findViewById(R.id.imgPb).setVisibility(View.VISIBLE);
          } else {
            view.findViewById(R.id.imgPb).setVisibility(View.GONE);
          }
        }
      }
      return view;
    }
  }

  private class NameHolderSpinnerAdapter<T extends NameHolder> extends ArrayAdapter<T> {
    private LayoutInflater inflater;
    private List<T> nameHolders;
    private boolean isSolveTypes;
    private View previousView;

    public NameHolderSpinnerAdapter(Context context, int resource, List<T> objects, boolean isSolveTypes) {
      super(context, resource, objects);
      this.inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      this.nameHolders = objects;
      this.isSolveTypes = isSolveTypes;
    }

    @Override
    public int getCount() {
      int count = super.getCount();
      if (isShowingEditSolveTypesShortcut()) {
        count += 1;
      }
      return count;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view;
      if (position >= 0 && position < nameHolders.size()) {
        view = getView(R.layout.spinner_item, position, convertView);
      } else {
        view = previousView;
      }
      previousView = view;
      return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
      return getView(R.layout.spinner_dropdown_item, position, convertView);
    }

    private View getView(int itemResourceId, int position, View convertView) {
      TextView view = (TextView) convertView;
      if (view == null) {
        view = (TextView) inflater.inflate(itemResourceId, null);
      }

      if (position >= 0 && position < nameHolders.size()) {
        T nameHolder = nameHolders.get(position);
        view.setText(nameHolder.getName());
      } else if (position >= nameHolders.size() && isShowingEditSolveTypesShortcut()) {
        view.setText(R.string.edit_solve_types_dots);
      }

      return view;
    }

    private boolean isShowingEditSolveTypesShortcut() {
      return isSolveTypes && Options.INSTANCE.isSolveTypesShortcutEnabled();
    }
  }

}
