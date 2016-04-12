package com.example.student_activity_manager;

/**
 * Created by Alexander on 29.03.2016.
 */
public class ScheduleTaskItem {
    @com.google.gson.annotations.SerializedName("id")
    private String id;

    @com.google.gson.annotations.SerializedName("schItemId")
    private  String schItemId;

    @com.google.gson.annotations.SerializedName("text")
    private  String text;

    @com.google.gson.annotations.SerializedName("isCompleted")
    private  boolean isCompleted;

    public ScheduleTaskItem(String text, String schItemId) {
        this.isCompleted = false;
        this.schItemId = schItemId;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public String getSchItemId() {
        return schItemId;
    }

    public String getText() {
        return text;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public void setSchItemId(String schItemItem) {
        this.schItemId = schItemItem;
    }

    public void setText(String text) {
        this.text = text;
    }
}
