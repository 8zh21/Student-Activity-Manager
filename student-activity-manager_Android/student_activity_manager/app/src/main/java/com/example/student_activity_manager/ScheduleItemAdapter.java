package com.example.student_activity_manager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Alexander on 21.03.2016.
 */
public class ScheduleItemAdapter extends ArrayAdapter<ScheduleItem> {

    private Context mContext;
    private int mLayoutResourceId;
    private Spinner daySelector;
    private ArrayList<ScheduleItem> originalItems;
    private List<ScheduleItem> items;
    private Filter filter;

    public ScheduleItemAdapter(Context context, int layoutResourceId, List<ScheduleItem> objs) {
        super(context, layoutResourceId, objs);

        items = objs;
        originalItems = new ArrayList<ScheduleItem>(items);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final ScheduleItem currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        row.setTag(currentItem);
        final TextView titleView = (TextView) row.findViewById(R.id.sch_item_title);
        titleView.setText(currentItem.getTitle());

        final TextView timeSView = (TextView) row.findViewById(R.id.sch_item_timeS);
        final TextView timeFView = (TextView) row.findViewById(R.id.sch_item_timeF);

        TimeItem time = ((ScheduleActivity) mContext).getTimeItemFromId(currentItem.getTimeItemId());

        timeSView.setText(time.getStartTime());
        timeFView.setText(time.getFinishTime());

        return row;
    }

    public void filter(int day)
    {
        items.clear();
        for(ScheduleItem item : originalItems)
        {
            if(item.getDay() == day)
            {
                items.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(items, new Comparator<ScheduleItem>() {
            @Override
            public int compare(ScheduleItem lhs, ScheduleItem rhs) {
                TimeItem l = ScheduleActivity.getTimeItemFromId(lhs.getTimeItemId());
                TimeItem r = ScheduleActivity.getTimeItemFromId(rhs.getTimeItemId());

                if (l.getSh() < r.getSh())
                {
                    return -1;
                }
                else if (l.getSh() == r.getSh())
                {
                    if (l.getSm() < r.getSm())
                        return -1;
                    else if (l.getSm() > r.getSm())
                        return 1;
                    else
                        return 0;
                }
                else
                    return 0;
            }
        });


        super.notifyDataSetChanged();
    }
}
