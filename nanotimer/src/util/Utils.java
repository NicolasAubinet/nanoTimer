package util;

public class Utils {

  public static String parseFloatToString(Float f) {
    return f == null ? null : String.valueOf(f);
  }

  public static String deleteLineBreaks(String s) {
//    return s.replace("\\r\\n|\\r|\\n", " ");
    return s.replace("\n", "");
  }

}
