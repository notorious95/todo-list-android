package com.dziura.patryk.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dziura.patryk.todolist.data.TaskContract;
import com.dziura.patryk.todolist.utilities.NotificationReceiver;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class TaskActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener{

    /**
     * Views
     */
    private EditText mDescriptionEditText;
    private TextView mDateTextView;
    private TextView mTimeTextView;
    private ImageButton mDateImage;
    private ImageButton mTimeImage;
    private Switch mNotificationSwitch;
    private Switch mPrioritySwitch;
    private TextView mNotificationSummary;
    private TextView mPrioritySummary;
    private FloatingActionButton mRemoveActionButton;


    /**
     * Variables that define current datetime
     */
    private int mCurrentYear;
    private int mCurrentMonth;
    private int mCurrentDay;
    private int mCurrentHour;
    private int mCurrentMinute;
    private boolean mToday = true;

    private String mPriority = "N";
    private String mNotification = "OFF";
    private String updatingTaskId = null;
    private static String mSelectedTheme;

    private Set<Integer> activatedNotificationSet;

    private static final String TASK_NOTIFICATION_CHANNEL_ID = "task_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mSelectedTheme = intent.getStringExtra("theme");
        if (mSelectedTheme.equals("blue")) {
            setTheme(R.style.AppTheme2);
            setContentView(R.layout.activity_task_2);
        } else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_task);
        }

        activatedNotificationSet = new HashSet<>();

        mDescriptionEditText = findViewById(R.id.descriptionEditText);
        mDateTextView = findViewById(R.id.dateTextView);
        mDateImage = findViewById(R.id.dateImage);
        mTimeTextView = findViewById(R.id.timeTextView);
        mTimeImage = findViewById(R.id.timeImage);
        mNotificationSwitch = findViewById(R.id.notificationsSwitch);
        mNotificationSummary = findViewById(R.id.notificationSummary);
        mPrioritySwitch = findViewById(R.id.prioritySwitch);
        mPrioritySummary = findViewById(R.id.prioritySummary);
        mRemoveActionButton = findViewById(R.id.removeActionButton);

        getCurrentDate();
        setTextViews();

        mNotificationSwitch.setOnCheckedChangeListener((view, bool) -> {
            if (bool) {
                mNotificationSummary.setText("Notification enabled");
                mNotification = "ON";
            }
            else {
                mNotificationSummary.setText("Notification disabled");
                mNotification = "OFF";
            }
        });

        mPrioritySwitch.setOnCheckedChangeListener((view, bool) -> {
            if (bool) {
                mPrioritySummary.setText("High priority task");
                mPriority = "H";
            } else {
                mPrioritySummary.setText("Normal task");
                mPriority = "N";
            }
        });

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, this, mCurrentYear, mCurrentMonth, mCurrentDay);
        mDateImage.setOnClickListener(v -> {
            datePickerDialog.show();
        });

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this, this, mCurrentHour, 0, true);
        mTimeImage.setOnClickListener(v -> {
            timePickerDialog.show();
        });


        if (intent.getAction().equals("modify")){

            getSupportActionBar().setTitle("Modify Task");
            mRemoveActionButton.setVisibility(View.VISIBLE);
            updatingTaskId = intent.getStringExtra("id");

            mRemoveActionButton.setOnClickListener(v -> {
                Uri uri = TaskContract.Tasks.CONTENT_URI;
                uri = uri.buildUpon().appendPath(updatingTaskId).build();
                int result = getContentResolver().delete(uri, "_id=?", new String[]{updatingTaskId});
                if (result > 0){
                    cancelNotificationAlarm(this, Integer.parseInt(updatingTaskId));
                    Toast.makeText(this, "Task has been deleted.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            updateTaskView(updatingTaskId);
        }
    }

    private void updateTaskView(final String taskId){
        new AsyncTask<Void, Void, Cursor>(){

            @Override
            protected Cursor doInBackground(Void... voids) {
                return getContentResolver().query(TaskContract.Tasks.CONTENT_URI,
                        null,
                        "_id=?",
                        new String[]{taskId},
                        null);
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                cursor.moveToFirst();
                String description = cursor.getString(cursor.getColumnIndex(TaskContract.Tasks.COLUMN_DESCRIPTION));
                String priority = cursor.getString(cursor.getColumnIndex(TaskContract.Tasks.COLUMN_PRIORITY));
                String notification = cursor.getString(cursor.getColumnIndex(TaskContract.Tasks.COLUMN_NOTIFICATION));
                long datetime = cursor.getLong(cursor.getColumnIndex(TaskContract.Tasks.COLUMN_DATETIME));

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String formattedTime = sdf.format(datetime);

                sdf = new SimpleDateFormat("dd.MM.yyyy");
                String formattedDate = sdf.format(datetime);

                mDescriptionEditText.setText(description);
                if (priority.equals("H")) {
                    mPrioritySwitch.setChecked(true);
                    mPrioritySummary.setText("High priority task");
                }
                else {
                    mPrioritySwitch.setChecked(false);
                    mPrioritySummary.setText("Normal task");
                }

                if (notification.equals("ON")) {
                    mNotificationSwitch.setChecked(true);
                    mNotificationSummary.setText("Notification enabled");
                }
                else {
                    mNotificationSwitch.setChecked(false);
                    mNotificationSummary.setText("Notification disabled");
                }

                mTimeTextView.setText(formattedTime);
                mDateTextView.setText(formattedDate);

                cursor.close();
            }
        }.execute();
    }

    private long insertDatetimeToDb(){
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String dateTime = mDateTextView.getText().toString() + " " + mTimeTextView.getText().toString();
        try {
            Date date = dateFormat.parse(dateTime);
            return date.getTime();
        } catch(ParseException e){
            e.printStackTrace();
        }

        return 1;
    }

    private void getCurrentDate(){
        Calendar c = Calendar.getInstance();
        mCurrentYear = c.get(Calendar.YEAR);
        mCurrentMonth = c.get(Calendar.MONTH);
        mCurrentDay = c.get(Calendar.DAY_OF_MONTH);
        mCurrentHour = c.get(Calendar.HOUR_OF_DAY);
        mCurrentMinute = c.get(Calendar.MINUTE);
    }

    private void setTextViews(){
        int month = mCurrentMonth + 1;
        if (mCurrentMonth < 10)
            mDateTextView.setText("" + mCurrentDay + ".0" + month + "." + mCurrentYear);
        else
            mDateTextView.setText("" + mCurrentDay + "." + month + "." + mCurrentYear);

        if (mCurrentMinute < 10)
            mTimeTextView.setText("" + mCurrentHour + ":0" + mCurrentMinute);
        else
            mTimeTextView.setText("" + mCurrentHour + ":" + mCurrentMinute);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_task_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemThatWasSelected = item.getItemId();
        int notificationId = 0;
        if (menuItemThatWasSelected == R.id.menu_item_done) {
            if (TextUtils.isEmpty(mDescriptionEditText.getText().toString()))
                Toast.makeText(getBaseContext(), "You must add some description.", Toast.LENGTH_LONG).show();
            else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TaskContract.Tasks.COLUMN_DESCRIPTION, mDescriptionEditText.getText().toString());
                contentValues.put(TaskContract.Tasks.COLUMN_DATETIME, insertDatetimeToDb());
                contentValues.put(TaskContract.Tasks.COLUMN_PRIORITY, mPriority);
                contentValues.put(TaskContract.Tasks.COLUMN_NOTIFICATION, mNotification);
                if (updatingTaskId == null) {
                    Uri uri = getContentResolver().insert(TaskContract.Tasks.CONTENT_URI, contentValues);
                    if (uri != null) {
                        Toast.makeText(getBaseContext(), "Your task has been added.", Toast.LENGTH_SHORT).show();
                        notificationId = Integer.parseInt(uri.getLastPathSegment().toString());
                        if (mNotification.equals("ON")) {
                            activatedNotificationSet.add(notificationId);
                            scheduleNotification(this, notificationId, mDescriptionEditText.getText().toString());
                        }
                    }
                } else{
                    int result = getContentResolver().update(TaskContract.Tasks.CONTENT_URI, contentValues, "_id=?", new String[]{updatingTaskId});
                    if (result > 0) {
                        Toast.makeText(getBaseContext(), "Your task has been updated.", Toast.LENGTH_SHORT).show();
                        notificationId = Integer.parseInt(updatingTaskId);
                        if (mNotification.equals("ON")) {
                            activatedNotificationSet.add(notificationId);
                            scheduleNotification(this, notificationId, mDescriptionEditText.getText().toString());
                        } else
                            cancelNotificationAlarm(this, notificationId);
                    }
                }
                if (mNotification.equals("OFF"))
                    activatedNotificationSet.remove(notificationId);
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month++;
//        mToday = false;
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//        try {
//            int day = dayOfMonth + 1;
//            Date pickedDate = sdf.parse("" + day + "/" + month + "/" + year);
//            if (pickedDate.before(new Date())) {
//                Toast.makeText(this, "You chose wrong date. Try again.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            pickedDate = sdf.parse("" + dayOfMonth + "/" + month + "/" + year);
//            if (pickedDate.before(new Date())) {
//                mToday = true;
//            }
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        if (month < 10)
            mDateTextView.setText("" + dayOfMonth + ".0" + month + "." + year);
        else
            mDateTextView.setText("" + dayOfMonth + "." + month + "." + year);

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//        if (mToday && hourOfDay <= mCurrentHour){
//            if ((hourOfDay == mCurrentHour && minute <= mCurrentMinute) || (hourOfDay < mCurrentHour)){
//                Toast.makeText(this, "You chose wrong time. Try again.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }
        if (minute < 10)
            mTimeTextView.setText("" + hourOfDay + ":0" + minute);
        else
            mTimeTextView.setText("" + hourOfDay + ":" + minute);

    }

    public void scheduleNotification(Context context, int notificationId, String description) {

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    TASK_NOTIFICATION_CHANNEL_ID,
                    "task notificaion channel",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, TASK_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent2))
                .setSmallIcon(R.drawable.notification)
                .setLargeIcon(largeIcon(context))
                .setContentTitle("Task reminder")
                .setContentText(description)
                .setStyle(new NotificationCompat.BigTextStyle().bigText((description)))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context, notificationId))
                .addAction(showTaskList(context, notificationId))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        Notification notification = notificationBuilder.build();

        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.setAction("" + notificationId);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        SimpleDateFormat dateAndTime = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date date = null;
        try {
            date = dateAndTime.parse(mDateTextView.getText().toString() + " " + mTimeTextView.getText().toString());
        }catch (ParseException e){
            e.printStackTrace();
        }

        long setDateTime = date.getTime();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, setDateTime, pendingIntent);
    }

    private static PendingIntent contentIntent(Context context, int id) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        startActivityIntent.setAction(Integer.toString(id));
        return PendingIntent.getActivity(
                context,
                id,
                startActivityIntent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    private static NotificationCompat.Action showTaskList(Context context, int id) {
        Intent editTask = new Intent(context, MainActivity.class);
        editTask.setAction(Integer.toString(id));
        PendingIntent editTaskPendingIntent = PendingIntent.getActivity(
                context,
                id,
                editTask,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Action showTasksAction = new NotificationCompat.Action(R.drawable.edit_task,
                "Show task list",
                editTaskPendingIntent);

        return showTasksAction;
    }

    private static void cancelNotificationAlarm(Context context, int id){
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.setAction("" + id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();

        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.notification);
        return largeIcon;
    }
}
