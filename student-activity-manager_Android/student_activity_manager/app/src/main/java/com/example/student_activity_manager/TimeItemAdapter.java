package com.example.student_activity_manager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/**
 * Created by Alexander on 21.03.2016.
 */
public class TimeItemAdapter extends ArrayAdapter<TimeItem> implements SpinnerAdapter{

    Context mContext;
    int mLayoutResourceId;

    public TimeItemAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);

        mContext = context;
        mLayoutResourceId = layoutResourceId;

    }

    @Override
    public  View getView(int position, View convertView, ViewGroup parent){
        View row = convertView;

        final TimeItem currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView text = (TextView) row;
        text.setText(currentItem.toString());

        return row;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        final TimeItem currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        row.setTag(currentItem);
        final TextView name = (TextView) row.findViewById(R.id.timeItemName);
        name.setText(currentItem.toString());

        final Button delButton = (Button) row.findViewById(R.id.delTimeItemButton);

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newSchItemActivity activity = (newSchItemActivity) mContext;
                activity.tryToDelTimeItem(currentItem);
            }
        });

        return row;
    }
}
