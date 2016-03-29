package com.example.student_activity_manager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Alexander on 29.03.2016.
 */
public class SchTaskItemAdapter2 extends SchTaskItemAdapter1 {
    public SchTaskItemAdapter2 (Context context, int layoutResourceId, List<ScheduleTaskItem> objs) {
        super(context, layoutResourceId, objs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final ScheduleTaskItem currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        row.setTag(currentItem);
        final TextView textView = (TextView) row.findViewById(R.id.taskText2);
        textView.setText(currentItem.getText());

        ScheduleItem schItem = ((ScheduleTasksActivity) mContext).getScheduleItemById(currentItem.getSchItemId());
        TimeItem timeItem = ScheduleActivity.getTimeItemFromId(schItem.getTimeItemId());

        final TextView dayView = (TextView) row.findViewById(R.id.dayOfWeekTextView);
        dayView.setText(getDayFromNumber(schItem.getDay()));

        final TextView timeSView = (TextView) row.findViewById(R.id.sch_item_timeS2);
        final TextView timeFView = (TextView) row.findViewById(R.id.sch_item_timeF2);
        timeSView.setText(timeItem.getStartTime());
        timeFView.setText(timeItem.getFinishTime());

        final TextView titleView = (TextView) row.findViewById(R.id.sch_item_title2);
        titleView.setText(schItem.getTitle());



        if (currentItem.getIsCompleted())
            row.setBackgroundResource(R.color.softgreen);
        else
            row.setBackgroundResource(R.color.softred);

        return row;
    }

    private String getDayFromNumber(int n)
    {
        return ((Activity)mContext).getResources().getStringArray(R.array.days)[n];
    }

    @Override
    protected void sort() {

        Collections.sort(items, new Comparator<ScheduleTaskItem>() {
            @Override
            public int compare(ScheduleTaskItem l, ScheduleTaskItem r) {
                if (!l.getIsCompleted() && r.getIsCompleted())
                    return -1;
                else if (l.getIsCompleted() && !r.getIsCompleted())
                    return 1;
                else {
                    int lDay = ((ScheduleTasksActivity) mContext).getScheduleItemById(l.getSchItemId()).getDay();
                    int rDay = ((ScheduleTasksActivity) mContext).getScheduleItemById(r.getSchItemId()).getDay();
                    int currentDay = (new Date()).getDay() - 1;
                    if (lDay == rDay)
                        return 0;
                    else if (lDay < rDay)
                    {
                        if (lDay < currentDay && rDay > currentDay)
                            return 1;
                        else
                            return -1;
                    }
                    else //(lDay > rDay)
                    {
                        if (rDay < currentDay && lDay > currentDay)
                            return -1;
                        else
                            return 1;
                    }
                }
            }
        });
    }
}
