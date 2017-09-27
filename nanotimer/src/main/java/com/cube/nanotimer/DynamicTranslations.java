package com.cube.nanotimer;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DynamicTranslations {

  private List<String> defaultSolveTypeStrings = new ArrayList<>();
  private Map<String, Integer> solveTypeNamesToResourceId = new HashMap<>();

  /**
   * Initialize the default solve type string (used to know if a solve type name is the default solve type),
   * and a map from each of the default solve types to their resource id, to allow to translate them
   * even after they were inserted into database.
   */
  public void init(Context context) {
    if (defaultSolveTypeStrings.size() > 0) {
      return; // already initialized, shouldn't do anything
    }

    // prepare locale change
    Resources res = context.getResources();
    Configuration conf = res.getConfiguration();
    Locale savedLocale = conf.locale;

    for (String languageCode : context.getResources().getStringArray(R.array.language_codes)) {
      // temporarily switch to that language
      conf.locale = new Locale(languageCode);
      res.updateConfiguration(conf, null); // second arg null means don't change

      // retrieve resources from desired locale
      defaultSolveTypeStrings.add(res.getString(R.string.def));

      addSolveTypeNameTranslationKey(res, R.string.def);
      addSolveTypeNameTranslationKey(res, R.string.one_handed);
      addSolveTypeNameTranslationKey(res, R.string.blindfolded);
      addSolveTypeNameTranslationKey(res, R.string.CFOP_steps);
      addSolveTypeNameTranslationKey(res, R.string.last_layer);
    }

    // restore original locale
    conf.locale = savedLocale;
    res.updateConfiguration(conf, null);
  }

  private void addSolveTypeNameTranslationKey(Resources res, int resourceId) {
    String solveTypeName = res.getString(resourceId);
    solveTypeNamesToResourceId.put(solveTypeName, resourceId);
  }

  public List<String> getDefaultSolveTypeStrings() {
    return defaultSolveTypeStrings;
  }

  public Integer getSolveTypeNameResourceId(String str) {
    return solveTypeNamesToResourceId.get(str);
  }

  public List<String> getSolveTypeNameVariants(String solveTypeName) {
    List<String> solveTypeNameVariants = new ArrayList<>();

    Integer resourceId = solveTypeNamesToResourceId.get(solveTypeName);
    if (resourceId == null) {
      solveTypeNameVariants.add(solveTypeName);
    } else {
      for (Map.Entry<String, Integer> entry : solveTypeNamesToResourceId.entrySet()) {
        if (resourceId.equals(entry.getValue())) {
          solveTypeNameVariants.add(entry.getKey());
        }
      }
    }

    return solveTypeNameVariants;
  }

}
