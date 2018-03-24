package com.dziura.patryk.todolist.tabs;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dziura.patryk.todolist.CustomCursorAdapter;
import com.dziura.patryk.todolist.R;
import com.dziura.patryk.todolist.TaskActivity;
import com.dziura.patryk.todolist.data.TaskContract;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NextDaysTasksFragment extends Fragment implements CustomCursorAdapter.ListClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private CustomCursorAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFloatingButton;

    private static final String TAG = "NEXT_TASKS_FRAGMENT";
    private static final int TASK_LOADER_ID = 2;
    private ContentResolver mResolver = null;
    private Context mainActivityContext;
    private String mSelectedTheme;

    public NextDaysTasksFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        Bundle bundle = this.getArguments();
        if (bundle != null)
            mSelectedTheme = bundle.getString("theme");
        if (mSelectedTheme.equals("blue"))
            rootView = inflater.inflate(R.layout.fragment_tab_2, container, false);
        else if (mSelectedTheme.equals("green"))
            rootView = inflater.inflate(R.layout.fragment_tab, container, false);

        mFloatingButton = rootView.findViewById(R.id.floating_button);
        mRecyclerView = rootView.findViewById(R.id.todo_list);
        mainActivityContext = getActivity().getApplicationContext();
        mResolver = mainActivityContext.getContentResolver();

        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<Cursor> cursorLoader = loaderManager.getLoader(TASK_LOADER_ID);

        if (cursorLoader == null) {
            loaderManager.initLoader(TASK_LOADER_ID, null, this);
        } else {
            loaderManager.restartLoader(TASK_LOADER_ID, null, this);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivityContext);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.hasFixedSize();
        mAdapter = new CustomCursorAdapter(this, mainActivityContext, mSelectedTheme);
        mRecyclerView.setAdapter(mAdapter);


        mFloatingButton.setOnClickListener(view -> {
            Intent addNewTaskIntent = new Intent(mainActivityContext, TaskActivity.class);
            addNewTaskIntent.setAction("add");
            addNewTaskIntent.putExtra("theme", mSelectedTheme);
            startActivity(addNewTaskIntent);
        });

        //getActivity().getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);

        return rootView;
    }


    @Override
    public void onListItemClick(int clickedItemIndex) {
        View v = mRecyclerView.getLayoutManager().findViewByPosition(clickedItemIndex);
        int id = (int) v.getTag();
        String stringId = Integer.toString(id);
        Intent startTaskActivity = new Intent(mainActivityContext, TaskActivity.class);
        startTaskActivity.setAction("modify");
        startTaskActivity.putExtra("id", stringId);
        startTaskActivity.putExtra("theme", mSelectedTheme);
        startActivity(startTaskActivity);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Cursor>(mainActivityContext) {

            Cursor mTask;

            @Override
            public void deliverResult(Cursor data) {
                mTask = data;
                super.deliverResult(data);
            }

            @Override
            protected void onStartLoading() {
                if (mTask != null)
                    deliverResult(mTask);
                else
                    forceLoad();
            }

            @Override
            public Cursor loadInBackground() {
                Date dateTommorow = null;
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH) + 1;
                int day = c.get(Calendar.DAY_OF_MONTH);

                day++;
                String tommorow = "" + day + "." + month + "." + year;

                try {
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    try {
                        dateTommorow = dateFormat.parse(tommorow);
                    } catch(ParseException e){
                        e.printStackTrace();
                    }

                    return mResolver.query(TaskContract.Tasks.CONTENT_URI,
                            null,
                            "datetime>?",
                            new String[]{String.valueOf(String.valueOf(dateTommorow.getTime()))},
                            TaskContract.Tasks.COLUMN_DATETIME + ", " + TaskContract.Tasks.COLUMN_PRIORITY);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.updateCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.updateCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<Cursor> cursorLoader = loaderManager.getLoader(TASK_LOADER_ID);

        if (cursorLoader == null) {
            loaderManager.initLoader(TASK_LOADER_ID, null, this);
        } else {
            loaderManager.restartLoader(TASK_LOADER_ID, null, this);
        }
    }
}


