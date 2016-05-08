package com.example.student_activity_manager;

import java.util.ArrayList;

/**
 * Created by Alexander on 01.05.2016.
 */
public class TaskItemWrap {
    public TaskItem item;
    public boolean isOpen;
    public int level;
    public ArrayList<TaskItemWrap> childs;

    public TaskItemWrap(TaskItem item)
    {
        this.item = item;
        childs = null;
        isOpen = false;
        level = -1;
    }
}
