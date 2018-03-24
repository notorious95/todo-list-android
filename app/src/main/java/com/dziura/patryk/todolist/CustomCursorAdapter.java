package com.dziura.patryk.todolist;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dziura.patryk.todolist.data.TaskContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CustomCursorAdapter extends RecyclerView.Adapter<CustomCursorAdapter.TaskViewHolder>{

    final private ListClickListener mOnClickListener;

    private Cursor mCursor;
    private Context mContext;
    private String mSelectedTheme;

    public interface ListClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public CustomCursorAdapter(ListClickListener listener, Context context, String theme) {
        mOnClickListener = listener;
        mContext = context;
        mSelectedTheme = theme;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutIdForList = 0;
        if (mSelectedTheme.equals("green"))
            layoutIdForList = R.layout.todo_list_task;
        else
            layoutIdForList = R.layout.todo_list_task_2;

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        View view = layoutInflater.inflate(layoutIdForList, parent, false);
        TaskViewHolder viewHolder = new TaskViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        int idDatabase = mCursor.getColumnIndex(TaskContract.Tasks._ID);
        int descriptionId = mCursor.getColumnIndex(TaskContract.Tasks.COLUMN_DESCRIPTION);
        int dateId = mCursor.getColumnIndex(TaskContract.Tasks.COLUMN_DATETIME);
        int priorityId = mCursor.getColumnIndex(TaskContract.Tasks.COLUMN_PRIORITY);
        int notificationId = mCursor.getColumnIndex(TaskContract.Tasks.COLUMN_NOTIFICATION);

        mCursor.moveToPosition(position);
        final int id = mCursor.getInt(idDatabase);
        String description = mCursor.getString(descriptionId);
        String priority = mCursor.getString(priorityId);
        long datetime = mCursor.getLong(dateId);
        String notification = mCursor.getString(notificationId);

        String formattedDate = setTimeText(datetime);
        //String formattedDate = "asd";
        //SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        //String formattedDate = sdf.format(datetime);

        holder.itemView.setTag(id);
        holder.taskDescriptionView.setText(description);
        if (priority.equals("H")) {
            holder.priorityImage.setVisibility(View.VISIBLE);
        }
        else {
            holder.priorityImage.setVisibility(View.GONE);
        }

        if (notification.equals("ON"))
            holder.notificationImage.setVisibility(View.VISIBLE);
        else
            holder.notificationImage.setVisibility(View.GONE);

        holder.dateView.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null)
            return 0;

        return mCursor.getCount();
    }

    public Cursor updateCursor(Cursor cursor){
        if (mCursor == cursor)
            return null;

        Cursor tmp = mCursor;
        mCursor = cursor;

        if (cursor != null)
            notifyDataSetChanged();

        return tmp;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener{

        TextView taskDescriptionView;
        TextView dateView;
        ImageButton priorityImage;
        ImageButton notificationImage;

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskDescriptionView = itemView.findViewById(R.id.description);
            dateView = itemView.findViewById(R.id.date);
            priorityImage = itemView.findViewById(R.id.priorityIcon);
            notificationImage = itemView.findViewById(R.id.notificationIcon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }

    private String setTimeText(long datetime) {
        SimpleDateFormat dayWeek = new SimpleDateFormat("EEEE");
        //SimpleDateFormat dateAndTime = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        SimpleDateFormat time = new SimpleDateFormat("HH:mm");
        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat dateYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dateMonth = new SimpleDateFormat("dd.MM");

        Date dateTommorow = null;
        Date dateToday = null;
        Date dateDayAfterTommorow = null;
        Date dateYesterday = null;
        //Date dateDayBeforeYesterday = null;
        Date dateAfterYear = null;
        Date dateCurrentYear = null;

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);

        //day = day-2;
        //String beforeYesterday = "" + day + "." + month + "." + year;
        //day++;
        day--;
        String yesterday = "" + day + "." + month + "." + year;
        day++;
        String today = "" + day + "." + month + "." + year;
        day++;
        String tommorow = "" + day + "." + month + "." + year;
        day++;
        String afterTommorow = "" + day + "." + month + "." + year;

        String currentYear = "" + year;
        year++;
        String afterYear = "" + year;

        try{
            dateDayAfterTommorow = date.parse(afterTommorow);
            dateTommorow = date.parse(tommorow);
            dateToday = date.parse(today);
            dateYesterday = date.parse(yesterday);
            //dateDayBeforeYesterday = date.parse(beforeYesterday);
            dateAfterYear = dateYear.parse(afterYear);
            dateCurrentYear = dateYear.parse(currentYear);

        }catch (ParseException e) {
            e.printStackTrace();
        }


        if (datetime > dateToday.getTime() && datetime < dateTommorow.getTime())
            return time.format(datetime);

        if (datetime > dateTommorow.getTime() && datetime < dateDayAfterTommorow.getTime())
            return "Tommorow : " + time.format(datetime);

        if (datetime < dateToday.getTime() && datetime > dateYesterday.getTime())
            return "Yesterday : " + time.format(datetime);

        if (datetime < dateYesterday.getTime()){
            if (datetime < dateCurrentYear.getTime())
                return date.format(datetime);
            else
                return dayWeek.format(datetime) + ", " + dateMonth.format(datetime) + " : " + time.format(datetime);
        }

        if (datetime > dateDayAfterTommorow.getTime()){
            if (datetime > dateAfterYear.getTime())
                return date.format(datetime) + " : " + time.format(datetime);
            else
                return dayWeek.format(datetime) + ", " + dateMonth.format(datetime) + " : " + time.format(datetime);
        }

        return date.format(datetime) + " : " + time.format(datetime);
    }
}
