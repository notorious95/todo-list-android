package com.dziura.patryk.todolist.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dziura.patryk.todolist.data.TaskContract.Tasks;

public class TaskDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tasksDatabase.db";
    private static final int VERSION = 12;

   TaskDbHelper(Context context){
       super(context, DATABASE_NAME, null, VERSION);
   }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE "    + Tasks.TABLE_NAME + " (" +
                Tasks._ID                + " INTEGER PRIMARY KEY, " +
                Tasks.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                Tasks.COLUMN_DATETIME + " INTEGER NOT NULL, " +
                Tasks.COLUMN_PRIORITY + " TEXT NOT NULL, " +
                Tasks.COLUMN_NOTIFICATION + " TEXT NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String DROP_TABLE = "DROP TABLE IF EXISTS " + Tasks.TABLE_NAME;
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}
