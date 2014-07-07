package com.cube.nanotimer.services.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.cube.nanotimer.vo.R;

public class DBHelper extends SQLiteOpenHelper {

  protected static SQLiteDatabase db;
  private Context context;

  public DBHelper(Context context) {
    this(context, DB.DB_NAME);
  }

  public DBHelper(Context context, String dbName) {
    super(context, dbName, null, DB.DB_VERSION);
    this.context = context;
    if (db == null) {
      db = getWritableDatabase();
    }
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    this.db = db;
    createTables(db);
    insertDefaultValues();
  }

  public void createTables(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + DB.TABLE_CUBETYPE + "(" +
        DB.COL_ID + " INTEGER PRIMARY KEY, " +
        DB.COL_CUBETYPE_NAME + " TEXT NOT NULL " +
      ");"
    );

    db.execSQL("CREATE TABLE " + DB.TABLE_SOLVETYPE + "(" +
        DB.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        DB.COL_SOLVETYPE_NAME + " TEXT NOT NULL, " +
        DB.COL_SOLVETYPE_SESSION_START + " INTEGER DEFAULT 0, " +
        DB.COL_SOLVETYPE_CUBETYPE_ID + " INTEGER, " +
        "FOREIGN KEY (" + DB.COL_SOLVETYPE_CUBETYPE_ID + ") REFERENCES " + DB.TABLE_CUBETYPE + " (" + DB.COL_ID + ") " +
      ");"
    );

    db.execSQL("CREATE TABLE " + DB.TABLE_TIMEHISTORY + "(" +
        DB.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        DB.COL_TIMEHISTORY_TIMESTAMP + " INTEGER, " +
        DB.COL_TIMEHISTORY_TIME + " INTEGER, " +
        DB.COL_TIMEHISTORY_SCRAMBLE + " TEXT, " +
        DB.COL_TIMEHISTORY_AVG5 + " INTEGER, " +
        DB.COL_TIMEHISTORY_AVG12 + " INTEGER, " +
        DB.COL_TIMEHISTORY_AVG100 + " INTEGER, " +
        DB.COL_TIMEHISTORY_PLUSTWO + " INTEGER DEFAULT 0, " +
        DB.COL_TIMEHISTORY_SOLVETYPE_ID + " INTEGER, " +
        "FOREIGN KEY (" + DB.COL_TIMEHISTORY_SOLVETYPE_ID + ") REFERENCES " + DB.TABLE_SOLVETYPE + " (" + DB.COL_ID + ") " +
      ");"
    );
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + DB.TABLE_TIMEHISTORY + "_tmp");
    db.execSQL("DROP TABLE IF EXISTS " + DB.TABLE_SOLVETYPE + "_tmp");
    db.execSQL("DROP TABLE IF EXISTS " + DB.TABLE_CUBETYPE + "_tmp");

    db.execSQL("ALTER TABLE " + DB.TABLE_TIMEHISTORY + " RENAME TO " + DB.TABLE_TIMEHISTORY + "_tmp");
    db.execSQL("ALTER TABLE " + DB.TABLE_SOLVETYPE + " RENAME TO " + DB.TABLE_SOLVETYPE + "_tmp");
    db.execSQL("ALTER TABLE " + DB.TABLE_CUBETYPE + " RENAME TO " + DB.TABLE_CUBETYPE + "_tmp");

    createTables(db);

    db.execSQL("INSERT INTO " + DB.TABLE_CUBETYPE + "(" +
        DB.COL_ID + "," +
        DB.COL_CUBETYPE_NAME + ")" +
        " SELECT " +
        DB.COL_ID + "," +
        DB.COL_CUBETYPE_NAME +
        " FROM " +
        DB.TABLE_CUBETYPE + "_tmp"
    );

    db.execSQL("INSERT INTO " + DB.TABLE_SOLVETYPE + "(" +
            DB.COL_ID + "," +
            DB.COL_SOLVETYPE_NAME + "," +
            DB.COL_SOLVETYPE_CUBETYPE_ID + ")" +
            " SELECT " +
            DB.COL_ID + "," +
            DB.COL_SOLVETYPE_NAME + "," +
            DB.COL_SOLVETYPE_CUBETYPE_ID +
            " FROM " +
            DB.TABLE_SOLVETYPE + "_tmp"
    );

    db.execSQL("INSERT INTO " + DB.TABLE_TIMEHISTORY + "(" +
            DB.COL_ID + "," +
            DB.COL_TIMEHISTORY_TIMESTAMP + "," +
            DB.COL_TIMEHISTORY_TIME + "," +
            DB.COL_TIMEHISTORY_SCRAMBLE + "," +
            DB.COL_TIMEHISTORY_AVG5 + "," +
            DB.COL_TIMEHISTORY_AVG12 + "," +
            DB.COL_TIMEHISTORY_AVG100 + "," +
            DB.COL_TIMEHISTORY_SOLVETYPE_ID + ")" +
            " SELECT " +
            DB.COL_ID + "," +
            DB.COL_TIMEHISTORY_TIMESTAMP + "," +
            DB.COL_TIMEHISTORY_TIME + "," +
            DB.COL_TIMEHISTORY_SCRAMBLE + "," +
            DB.COL_TIMEHISTORY_AVG5 + "," +
            DB.COL_TIMEHISTORY_AVG12 + "," +
            DB.COL_TIMEHISTORY_AVG100 + "," +
            DB.COL_TIMEHISTORY_SOLVETYPE_ID +
            " FROM " +
            DB.TABLE_TIMEHISTORY + "_tmp"
    );
  }

  private void insertDefaultValues() {
    insertSolveType(getString(R.string.def), insertCubeType(1, getString(R.string.two_by_two)));
    insertSolveType(getString(R.string.def), insertCubeType(2, getString(R.string.three_by_three)));
    insertSolveType(getString(R.string.def), insertCubeType(3, getString(R.string.four_by_four)));
    insertSolveType(getString(R.string.def), insertCubeType(4, getString(R.string.five_by_five)));
    insertSolveType(getString(R.string.def), insertCubeType(5, getString(R.string.six_by_six)));
    insertSolveType(getString(R.string.def), insertCubeType(6, getString(R.string.seven_by_seven)));
    insertSolveType(getString(R.string.def), insertCubeType(7, getString(R.string.megaminx)));

    insertSolveType(getString(R.string.one_handed), 2);
    insertSolveType(getString(R.string.blindfolded), 2);
  }

  private int insertCubeType(int id, String name) {
    ContentValues values = new ContentValues();
    values.put(DB.COL_ID, id);
    values.put(DB.COL_CUBETYPE_NAME, name);
    return (int) db.insert(DB.TABLE_CUBETYPE, null, values);
  }

  private int insertSolveType(String name, int cubeTypeId) {
    ContentValues values = new ContentValues();
    values.put(DB.COL_SOLVETYPE_NAME, name);
    values.put(DB.COL_SOLVETYPE_CUBETYPE_ID, cubeTypeId);
    return (int) db.insert(DB.TABLE_SOLVETYPE, null, values);
  }

  private String getString(int resId) {
    return context.getString(resId);
  }

}
