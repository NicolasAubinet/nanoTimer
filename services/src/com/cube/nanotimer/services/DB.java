package com.cube.nanotimer.services;

public class DB {

  public static final String DB_NAME = "nanoTimerDB";
  public static final int DB_VERSION = 1;

  public static final String COL_ID = "id";

  public static final String TABLE_CUBETYPE = "cubetype";
  public static final String COL_CUBETYPE_NAME = "name";

  public static final String TABLE_SOLVETYPE = "solvetype";
  public static final String COL_SOLVETYPE_NAME = "name";
  public static final String COL_SOLVETYPE_CUBETYPE_ID = "cubetype_id";

  public static final String TABLE_TIMEHISTORY = "timehistory";
  public static final String COL_TIMEHISTORY_TIMESTAMP = "timestamp";
  public static final String COL_TIMEHISTORY_TIME = "time";
  public static final String COL_TIMEHISTORY_SCRAMBLE = "scramble";
  public static final String COL_TIMEHISTORY_AVG5 = "avg5";
  public static final String COL_TIMEHISTORY_AVG12 = "avg12";
  public static final String COL_TIMEHISTORY_AVG100 = "avg100";
  public static final String COL_TIMEHISTORY_AVG1000 = "avg1000";
  public static final String COL_TIMEHISTORY_SOLVETYPE_ID = "solvetype_id";

}
