package com.cube.nanotimer.gui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import com.ankhsoft.filebrowser.FileBrowser;
import com.ankhsoft.filebrowser.OnFileSelectedListener;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.helper.Utils;

import java.io.File;

public class ImportActivity extends Activity {
  private FileBrowser fileBrowser;

  public static final String FILE_SELECTED_ACTION = "com.cube.nanotimer.FILE_SELECTED";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.import_screen);
    if (!Utils.checkProFeature(this)) {
      finish();
      return;
    }

    initViews();
  }

  private void initViews() {
    fileBrowser = (FileBrowser) findViewById(R.id.fileBrowser);
    fileBrowser.setRootFolderDisplayName(getString(R.string.storage));
    fileBrowser.setOnFileSelectedListener(new OnFileSelectedListener() {
      @Override
      public void onFileSelected(File file) {
        Intent result = new Intent(FILE_SELECTED_ACTION);
        result.putExtra("file", file);
        setResult(Activity.RESULT_OK, result);
        finish();
      }
    });
  }

  @Override
  public void onBackPressed() {
    if (fileBrowser.isRootFolder(fileBrowser.getCurrentFolder())) {
      super.onBackPressed();
    } else {
      fileBrowser.moveToParent();
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    File currentFolder = fileBrowser.getCurrentFolder();

    setContentView(R.layout.import_screen);
    initViews();

    fileBrowser.displayFolder(currentFolder);
  }
}
