package com.example.student_activity_manager;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Alexander on 07.05.2016.
 * Адаптер данных (задач),
 * конструирует представление для задачи
 */
public class TaskItemAdapter extends ArrayAdapter<TaskItemWrap> {
    private Context mContext;
    private int mLayoutResourceId;
    private List<TaskItemWrap> items;

    public TaskItemAdapter(Context context, int layoutResourceId, List<TaskItemWrap> objs) {
        super(context, layoutResourceId, objs);

        items = objs;
        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final TaskItemWrap currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        row.setTag(currentItem);

        final TextView taskName = (TextView) row.findViewById(R.id.task_name);
        taskName.setText(currentItem.item.getName());

        final ImageView ico = (ImageView) row.findViewById(R.id.tree_ico);
        final ImageView completed = (ImageView) row.findViewById(R.id.completed_ico);
        final ImageView line = (ImageView) row.findViewById(R.id.tree_line);

        final TextView progressText = (TextView) row.findViewById(R.id.task_progress_text);
        final ProgressBar progressBar = (ProgressBar) row.findViewById(R.id.task_progress);

        if (currentItem.childs == null || currentItem.childs.size() == 0)
        {
            progressText.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            ico.setVisibility(View.GONE);
        }
        else
        {
            progressText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            ico.setVisibility(View.VISIBLE);

            int imgRes;
            if (currentItem.isOpen)
                imgRes = R.drawable.minus;
            else
                imgRes = R.drawable.plus;
            ico.setImageResource(imgRes);

            Pair<Integer, Integer> pr = getProgress(currentItem);
            Pair<Integer, Integer> fullPr = getFullProgress(currentItem);
            progressText.setText(pr.first.toString() + " / " + pr.second.toString() +
                    " ( " + fullPr.first.toString() + " / " + fullPr.second.toString() + " )");

            //progressBar.setMax(pr.second);
            //progressBar.setProgress(pr.first);
            progressBar.setMax(fullPr.second);
            progressBar.setProgress(fullPr.first);
        }

        int indent = (int) dipToPixels(mContext, 36);
        int padding = (int) dipToPixels(mContext, 6);

        if (currentItem.item.getParentId().equals("root")) {
            line.setVisibility(View.GONE);
            row.setPadding(padding, padding, padding, padding);
        }
        else {
            line.setVisibility(View.VISIBLE);
            row.setPadding((currentItem.level - 1) * indent + padding, padding, padding, padding);
        }

        int compImgRes;
        if (currentItem.item.isCompleted())
            compImgRes = R.drawable.completed_true;
        else
            compImgRes = R.drawable.completed_false;
        completed.setImageResource(compImgRes);

        return row;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private Pair<Integer, Integer> getProgress(TaskItemWrap item)
    {
        int completed = 0;
        int tasks = 0;

        for (TaskItemWrap child : item.childs)
        {
            tasks++;
            if (child.item.isCompleted())
                completed++;
        }

        return new Pair<>(completed, tasks);
    }

    private Pair<Integer, Integer> getFullProgress(TaskItemWrap item)
    {
        if(item.childs == null || item.childs.size() == 0)
        {
            return new Pair<>(0, 0);
        }
        else
        {
            int c = 0;
            int t = 0;

            for (TaskItemWrap child : item.childs)
            {
                Pair<Integer, Integer> childRes = getFullProgress(child);
                if (child.item.isCompleted())
                    c++;

                c += childRes.first;
                t += 1 + childRes.second;
            }

            return new Pair<>(c, t);
        }
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }
}
