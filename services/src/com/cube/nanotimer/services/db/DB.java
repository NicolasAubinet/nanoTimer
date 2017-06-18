package com.cube.nanotimer.services.db;

public class DB {

  public static final String DB_NAME = "nanoTimerDB";
  public static final int DB_VERSION = 13;

  public static final String COL_ID = "id";

  public static final String TABLE_CUBETYPE = "cubetype";
  public static final String COL_CUBETYPE_NAME = "name";

  public static final String TABLE_SOLVETYPE = "solvetype";
  public static final String COL_SOLVETYPE_NAME = "name";
//  public static final String COL_SOLVETYPE_SESSION_START = "sessionstart";
  public static final String COL_SOLVETYPE_POSITION = "position";
  public static final String COL_SOLVETYPE_BLIND = "blind";
  public static final String COL_SOLVETYPE_SCRAMBLE_TYPE = "scrambletype";
  public static final String COL_SOLVETYPE_CUBETYPE_ID = "cubetype_id";

  public static final String TABLE_TIMEHISTORY = "timehistory";
  public static final String COL_TIMEHISTORY_TIMESTAMP = "timestamp";
  public static final String COL_TIMEHISTORY_TIME = "time";
  public static final String COL_TIMEHISTORY_SCRAMBLE = "scramble";
  public static final String COL_TIMEHISTORY_AVG5 = "avg5"; // column also used for "Mean of 3" for blind solve types
  public static final String COL_TIMEHISTORY_AVG12 = "avg12";
  public static final String COL_TIMEHISTORY_AVG50 = "avg50";
  public static final String COL_TIMEHISTORY_AVG100 = "avg100";
  public static final String COL_TIMEHISTORY_PLUSTWO = "plustwo";
  public static final String COL_TIMEHISTORY_PB = "pb";
  public static final String COL_TIMEHISTORY_SOLVETYPE_ID = "solvetype_id";

  public static final String TABLE_SOLVETYPESTEP = "solvetypestep";
  public static final String COL_SOLVETYPESTEP_NAME = "name";
  public static final String COL_SOLVETYPESTEP_POSITION = "position";
  public static final String COL_SOLVETYPESTEP_SOLVETYPE_ID = "solvetype_id";

  public static final String TABLE_TIMEHISTORYSTEP = "timehistorystep";
  public static final String COL_TIMEHISTORYSTEP_TIME = "time";
  public static final String COL_TIMEHISTORYSTEP_SOLVETYPESTEP_ID = "solvetypestep_id";
  public static final String COL_TIMEHISTORYSTEP_TIMEHISTORY_ID = "timehistory_id";

  public static final String TABLE_SESSION = "session";
  public static final String COL_SESSION_START = "start";
  public static final String COL_SESSION_SOLVETYPE_ID = "solvetype_id";

}
