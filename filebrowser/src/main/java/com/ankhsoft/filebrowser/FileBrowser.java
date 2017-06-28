package com.ankhsoft.filebrowser;

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class FileBrowser extends LinearLayout {
  private List<File> liFiles = new ArrayList<File>();
  private File rootFolder;
  private File defaultFolder;
  private File currentFolder;

  private LayoutInflater inflater;
  private FileListAdapter adapter;
  private TextView tvCurrentFolder;
  private FileComparator fileComparator = new FileComparator();

  private String[] extensionFilters;
  private OnFileSelectedListener fileSelectedListener;
  private String rootFolderDisplayName = "/";
  private boolean showHiddenFiles;

  public FileBrowser(Context context) {
    super(context);
    init(context);
  }

  public FileBrowser(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    inflater = LayoutInflater.from(context);
    inflater.inflate(R.layout.file_browser, this);
    rootFolder = getDefaultFolder();
    defaultFolder = rootFolder;
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    initViews();
  }

  protected void initViews() {
    tvCurrentFolder = (TextView) findViewById(R.id.tvCurrentFolder);

    final ListView lvFiles = (ListView) findViewById(R.id.lvFiles);
    adapter = new FileListAdapter(getContext(), R.id.lvFiles, liFiles);
    lvFiles.setAdapter(adapter);
    lvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        File selectedFile = liFiles.get(i);
        if (selectedFile.isDirectory()) {
          displayFolder(selectedFile);
          lvFiles.setSelection(0);
        } else {
          if (fileSelectedListener != null) {
            fileSelectedListener.onFileSelected(selectedFile);
          }
        }
      }
    });

    displayFolder(defaultFolder);
  }

  protected File getDefaultFolder() {
    return Environment.getExternalStorageDirectory();
  }

  /**
   * Display <i>folder</i> contents.
   */
  public void displayFolder(File folder) {
    currentFolder = folder;
    liFiles.clear();
    if (!isRootFolder(folder)) {
      liFiles.add(folder.getParentFile());
      tvCurrentFolder.setText(folder.getName());
    } else {
      tvCurrentFolder.setText(rootFolderDisplayName);
    }

    List<File> files = new ArrayList<File>(Arrays.asList(folder.listFiles()));
    Iterator<File> iterator = files.iterator();
    while (iterator.hasNext()) {
      File file = iterator.next();
      if (!showHiddenFiles && file.getName().startsWith(".")) {
        iterator.remove();
      }
    }
    Collections.sort(files, fileComparator);
    liFiles.addAll(files);
    adapter.notifyDataSetChanged();
  }

  /**
   * Returns true if <i>file</i> is the root folder.
   */
  public boolean isRootFolder(File file) {
    return file.getAbsolutePath().equals(rootFolder.getAbsolutePath());
  }

  /**
   * Move to parent folder.
   */
  public void moveToParent() {
    if (!isRootFolder(currentFolder)) {
      displayFolder(currentFolder.getParentFile());
    }
  }

  /**
   * Returns the displayed folder.
   */
  public File getCurrentFolder() {
    return currentFolder;
  }

  /**
   * Set the root folder above which we can not go up.
   * @param rootFolder root folder
   */
  public void setRootFolder(File rootFolder) {
    this.rootFolder = rootFolder;
  }

  /**
   * Set the default folder where the file browser will start.
   * @param defaultFolder default folder
   */
  public void setDefaultFolder(File defaultFolder) {
    this.defaultFolder = defaultFolder;
  }

  /**
   * Set the file filters containing extensions of files to display.
   * If null, no filter will be applied.
   * @param extensionFilters file extensionFilters
   */
  public void setExtensionFilters(String[] extensionFilters) {
    this.extensionFilters = extensionFilters;
  }

  /**
   * Set the listener that will be called when a file is selected.
   * @param fileSelectedListener file selected listener
   */
  public void setOnFileSelectedListener(OnFileSelectedListener fileSelectedListener) {
    this.fileSelectedListener = fileSelectedListener;
  }

  /**
   * If true, the current folder name will be displayed on top.
   */
  public void setShowCurrentFolderName(boolean showCurrentFolderName) {
    if (showCurrentFolderName) {
      tvCurrentFolder.setVisibility(View.VISIBLE);
    } else {
      tvCurrentFolder.setVisibility(View.GONE);
    }
  }

  /**
   * Set the name displayed in folder name bar when in root folder.
   */
  public void setRootFolderDisplayName(String rootFolderDisplayName) {
    this.rootFolderDisplayName = rootFolderDisplayName;
    if (isRootFolder(currentFolder)) {
      tvCurrentFolder.setText(rootFolderDisplayName);
    }
  }

  /**
   * If true, the hidden files (starting with a '.') will be displayed
   */
  public void setShowHiddenFiles(boolean showHiddenFiles) {
    if (this.showHiddenFiles != showHiddenFiles) {
      this.showHiddenFiles = showHiddenFiles;
      if (currentFolder != null) {
        displayFolder(currentFolder);
      }
    }
  }

  private class FileListAdapter extends ArrayAdapter<File> {

    public FileListAdapter(Context context, int resource, List<File> objects) {
      super(context, resource, objects);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        view = inflater.inflate(R.layout.file_list_item, null);
      }

      if (position >= 0 && position < liFiles.size()) {
        File file = liFiles.get(position);

        ImageView imgIcon = (ImageView) view.findViewById(R.id.imgIcon);
        if (file.isDirectory()) {
          imgIcon.setImageResource(R.drawable.folder);
        } else {
          imgIcon.setImageResource(R.drawable.file);
          if (!isMatchingFilter(file.getName())) {
            view.setVisibility(View.GONE);
          }
        }

        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        String fileName;
        if (position == 0 && !isRootFolder(currentFolder)) {
          fileName = "..";
        } else {
          fileName = file.getName();
          if (file.isDirectory()) {
            fileName += "/";
          }
        }
        tvName.setText(fileName);
      }
      return view;
    }
  }

  private boolean isMatchingFilter(String fileName) {
    boolean matchesFilter = true;
    if (extensionFilters != null) {
      matchesFilter = false;
      for (String filter : extensionFilters) {
        String curFilter = filter;
        if (!curFilter.startsWith(".")) {
          curFilter = "." + curFilter;
        }
        if (fileName.endsWith(curFilter)) {
          matchesFilter = true;
          break;
        }
      }
    }
    return matchesFilter;
  }

  class FileComparator implements Comparator<File> {
    @Override
    public int compare(File f1, File f2) {
      if (f1 == null) {
        return -1;
      } else if (f2 == null) {
        return 1;
      }
      boolean f1Folder = f1.isDirectory();
      boolean f2Folder = f2.isDirectory();
      if (f1Folder && !f2Folder) {
        return -1;
      }
      if (!f1Folder && f2Folder) {
        return 1;
      }
      return f1.getName().compareToIgnoreCase(f2.getName());
    }
  }
}
