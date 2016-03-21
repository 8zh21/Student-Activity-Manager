package com.example.student_activity_manager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Alexander on 21.03.2016.
 */
public class ScheduleItemAdapter extends ArrayAdapter<ScheduleItem> {

    Context mContext;
    int mLayoutResourceId;
    Spinner daySelector;

    public ScheduleItemAdapter(Context context, int layoutResourceId, Spinner daySelector) {
        super(context, layoutResourceId);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
        this.daySelector = daySelector;
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
        final TextView textView = (TextView) row.findViewById(R.id.sch_item_title);
        textView.setText(currentItem.getTitle());



        return row;
    }

}
