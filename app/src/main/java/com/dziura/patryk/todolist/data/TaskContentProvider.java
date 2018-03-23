package com.dziura.patryk.todolist.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.dziura.patryk.todolist.data.TaskContract.*;


public class TaskContentProvider extends ContentProvider{

    private TaskDbHelper mtaskDbHelper;

    @Override
    public boolean onCreate() {
        mtaskDbHelper = new TaskDbHelper(getContext());

        return true;
    }


    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mtaskDbHelper.getReadableDatabase();
        Cursor resultCursor;

        resultCursor = db.query(Tasks.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
        resultCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return resultCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mtaskDbHelper.getWritableDatabase();
        Uri resultUri;

        long id = db.insert(Tasks.TABLE_NAME, null, values);
        if ( id > 0 ) {
            resultUri = ContentUris.withAppendedId(Tasks.CONTENT_URI, id);
        } else {
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mtaskDbHelper.getWritableDatabase();
        int result;

        result = db.delete(Tasks.TABLE_NAME, selection, selectionArgs);
        if (result > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mtaskDbHelper.getWritableDatabase();
        int result;

        result = db.update(Tasks.TABLE_NAME, values, selection, selectionArgs);
        if (result > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return result;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Unsupported operation");
    }
}
