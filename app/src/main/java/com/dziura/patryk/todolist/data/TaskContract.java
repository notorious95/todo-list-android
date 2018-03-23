package com.dziura.patryk.todolist.data;


import android.net.Uri;
import android.provider.BaseColumns;

public class TaskContract {
    public static final String AUTHORITY = "com.dziura.patryk.todolist";
    public static final Uri BASE_CONTENT_URI =android.net.Uri.parse("content://" + AUTHORITY);

    public static final class Tasks implements BaseColumns{
        public static final String TABLE_NAME = "tasks";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DATETIME = "datetime";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_NOTIFICATION = "notification";
    }
}
