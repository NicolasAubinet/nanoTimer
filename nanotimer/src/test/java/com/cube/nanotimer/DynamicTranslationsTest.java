package com.cube.nanotimer;

import org.junit.Assert;
import org.junit.Test;

/** Which solve type names are known as the ones the app created. */
public class DynamicTranslationsTest {

  @Test
  public void theOneHandedTypeIsKnownByItsNameInAnyLanguage() {
    DynamicTranslations names = new DynamicTranslations();
    names.add("One-handed", R.string.one_handed);
    names.add("À une main", R.string.one_handed);
    names.add("Default", R.string.def);

    Assert.assertTrue(names.isOneHanded("One-handed"));
    Assert.assertTrue(names.isOneHanded("À une main"));
    Assert.assertFalse(names.isOneHanded("Default"));
    Assert.assertFalse(names.isOneHanded("OH")); // a renamed or hand-made type is not recognised
  }
}
