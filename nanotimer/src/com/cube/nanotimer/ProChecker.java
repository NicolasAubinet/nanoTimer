package com.cube.nanotimer;

import android.content.Context;
import android.content.pm.PackageManager;

public class ProChecker {
  private static final int VALID_PRO_VERSION_CODE = 2;

  public enum ProState { UNINSTALLED, ENABLED, INVALID_VERSION }

  public static ProState getProState(Context context) {
    PackageManager packageManager = context.getPackageManager();
    int versionCode = 0;
    int sigMatch = packageManager.checkSignatures(App.PRO_PACKAGE_NAME, context.getPackageName());
    try {
      versionCode = packageManager.getPackageInfo(App.PRO_PACKAGE_NAME, 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      // don't do anything (pro app might not be installed)
    }
    if (sigMatch == PackageManager.SIGNATURE_MATCH) {
      if (versionCode == VALID_PRO_VERSION_CODE) {
        return ProState.ENABLED;
      } else {
        return ProState.INVALID_VERSION;
      }
    }
    return ProState.UNINSTALLED;
  }

}
