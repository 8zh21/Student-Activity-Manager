package com.example.student_activity_manager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Alexander on 21.03.2016.
 * Адаптер данных (элементов времени),
 * конструирует представление элементов времени,
 * сортирует элементы
 */
public class TimeItemAdapter extends ArrayAdapter<TimeItem> implements SpinnerAdapter{

    private Context mContext;
    private int mLayoutResourceId;
    private Method hiderDropDownSpinner;
    private List<TimeItem> items;

    public TimeItemAdapter(Context context, int layoutResourceId, List<TimeItem> objs) {
        super(context, android.R.layout.simple_spinner_item, objs);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
        items = objs;

        try {
            hiderDropDownSpinner = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
            hiderDropDownSpinner.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getDropDownView(final int position, View convertView, final ViewGroup parent) {

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

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = (Spinner) ((Activity) mContext).findViewById(R.id.timeModelSpinner);
                spinner.setSelection(position);
                /**
                 * Hides a spinner's drop down.
                 */
                try {
                    hiderDropDownSpinner.invoke((Spinner) ((Activity) mContext).findViewById(R.id.timeModelSpinner));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return row;
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(items, new Comparator<TimeItem>() {
            @Override
            public int compare(TimeItem l, TimeItem r) {
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
