package com.cube.nanotimer.gui;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import com.ankhsoft.filebrowser.FileBrowser;
import com.ankhsoft.filebrowser.OnFileSelectedListener;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.helper.DialogUtils;

import java.io.File;

public class ImportActivity extends Activity {
  private FileBrowser fileBrowser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.import_screen);

    initViews();
  }

  private void initViews() {
    fileBrowser = (FileBrowser) findViewById(R.id.fileBrowser);
    fileBrowser.setRootFolderDisplayName(getString(R.string.import_times));
    fileBrowser.setOnFileSelectedListener(new OnFileSelectedListener() {
      @Override
      public void onFileSelected(File parFile) {
        DialogUtils.showInfoMessage(ImportActivity.this, "Selected file: " + parFile.getName());
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
