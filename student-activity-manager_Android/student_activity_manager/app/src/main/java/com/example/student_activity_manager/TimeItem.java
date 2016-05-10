package com.example.student_activity_manager;

/**
 * Created by Alexander on 20.03.2016.
 * Класс элемента таблицы который соответствует таблице элементов времени
 */

public class TimeItem {
    @com.google.gson.annotations.SerializedName("id")
    private String id;

    @com.google.gson.annotations.SerializedName("userId")
    private  String userId;

    @com.google.gson.annotations.SerializedName("name")
    private String name;

    @com.google.gson.annotations.SerializedName("sh")
    private int sh;

    @com.google.gson.annotations.SerializedName("sm")
    private int sm;

    @com.google.gson.annotations.SerializedName("fh")
    private int fh;

    @com.google.gson.annotations.SerializedName("fm")
    private int fm;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFh() {
        return fh;
    }

    public int getFm() {
        return fm;
    }

    public int getSh() {
        return sh;
    }

    public int getSm() {
        return sm;
    }

    public void setFh(int fh) {
        this.fh = fh;
    }

    public void setFm(int fm) {
        this.fm = fm;
    }

    public void setSh(int sh) {
        this.sh = sh;
    }

    public void setSm(int sm) {
        this.sm = sm;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TimeItem(String name, int sh, int sm, int fh, int fm) {
        this.name = name;
        this.sh = sh;
        this.sm = sm;
        this.fh = fh;
        this.fm = fm;
    }

    @Override
    public String toString() {
        return name + " " + getStartTime() + " - " + getFinishTime();
    }

    public String getStartTime()
    {
        String hCorrection = "";
        String mCorrection = "";
        if (sh < 10)
            hCorrection = "0";
        if(sm < 10)
            mCorrection = "0";

        return hCorrection + String.valueOf(sh) + ":" + mCorrection + String.valueOf(sm);
    }

    public String getFinishTime()
    {
        String hCorrection = "";
        String mCorrection = "";
        if (fh < 10)
            hCorrection = "0";
        if(fm < 10)
            mCorrection = "0";

        return hCorrection + String.valueOf(fh) + ":" + mCorrection + String.valueOf(fm);
    }
}
