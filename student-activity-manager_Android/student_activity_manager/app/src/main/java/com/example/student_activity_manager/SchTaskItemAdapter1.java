package com.example.student_activity_manager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Alexander on 29.03.2016.
 * Адаптер данных (задач для элементов расписания, конкретного эл-та)
 * конструирует представление для эл-та задачи,
 * фильтрует и сортирует задачи
 */
public class SchTaskItemAdapter1 extends ArrayAdapter<ScheduleTaskItem> {

    protected Context mContext;
    protected List<ScheduleTaskItem> items;
    private ArrayList<ScheduleTaskItem> originalItems;
    protected int mLayoutResourceId;

    public SchTaskItemAdapter1 (Context context, int layoutResourceId, List<ScheduleTaskItem> objs)
    {
        super(context, layoutResourceId, objs);

        mContext = context;
        items = objs;
        originalItems = new ArrayList<ScheduleTaskItem>(items);
        mLayoutResourceId = layoutResourceId;
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
        final TextView textView = (TextView) row.findViewById(R.id.taskText1);
        textView.setText(currentItem.getText());

        if (currentItem.getIsCompleted())
            textView.setBackgroundResource(R.color.softgreen);
        else
            textView.setBackgroundResource(R.color.softred);

        return row;
    }

    @Override
    public void notifyDataSetChanged() {
        sort();
        super.notifyDataSetChanged();
    }

    protected void sort()
    {
        Collections.sort(items, new Comparator<ScheduleTaskItem>() {
            @Override
            public int compare(ScheduleTaskItem l, ScheduleTaskItem r) {
                if (!l.getIsCompleted() && r.getIsCompleted())
                    return -1;
                else if (l.getIsCompleted() && !r.getIsCompleted())
                    return 1;
                else
                    return 0;
            }
        });
    }

    public void filter()
    {
        items.clear();
        CheckBox cb = ((CheckBox) ((Activity) mContext).findViewById(R.id.hideCompletedCheckBox));
        for(ScheduleTaskItem item : originalItems)
        {
            if((cb.isChecked() && !item.getIsCompleted()) ||
               !cb.isChecked())
            {
                items.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void add(ScheduleTaskItem object) {
        super.add(object);
        originalItems.add(object);
    }

    @Override
    public void remove(ScheduleTaskItem object) {
        super.remove(object);
        originalItems.remove(object);
    }
}
