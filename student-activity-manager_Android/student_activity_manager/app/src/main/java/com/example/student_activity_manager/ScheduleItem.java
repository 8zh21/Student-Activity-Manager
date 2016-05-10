package com.example.student_activity_manager;

/**
 * Created by Alexander on 18.03.2016.
 * Класс элемента таблицы который соответствует таблице элементов расписания
 */
public class ScheduleItem {
    @com.google.gson.annotations.SerializedName("id")
    private String id;

    @com.google.gson.annotations.SerializedName("userId")
    private  String userId;

    @com.google.gson.annotations.SerializedName("title")
    private  String title;

    @com.google.gson.annotations.SerializedName("classroom")
    private  String classroom;

    @com.google.gson.annotations.SerializedName("timeItemId")
    private  String timeItemId;

    @com.google.gson.annotations.SerializedName("day")
    private  int day;

    public ScheduleItem(String title, String classroom, String timeItemId, int day) {
        this.classroom = classroom;
        this.timeItemId = timeItemId;
        this.title = title;
        this.day = day;
    }

    @Override
    public String toString() {
        return title;
    }

    public String getClassroom() {
        return classroom;
    }

    public String getId() {
        return id;
    }

    public String getTimeItemId() {
        return timeItemId;
    }

    public String getTitle() {
        return title;
    }

    public String getUserId() {
        return userId;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimeItemId(String timeItemId) {
        this.timeItemId = timeItemId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
}
