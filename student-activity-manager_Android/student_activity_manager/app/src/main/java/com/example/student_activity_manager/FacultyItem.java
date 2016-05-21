/**
 * Created by Alexander Podshiblov on 15.03.2016.
 * Класс элемента таблицы который соответствует таблице факультетов
 */

package com.example.student_activity_manager;

public class FacultyItem {
    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("name")
    private String mName;

    @com.google.gson.annotations.SerializedName("EdInstId")
    private String mEdInstId;

    public FacultyItem() {}

    public FacultyItem(String mName, String mEdInstId) {
        this.mName = mName;
        this.mEdInstId = mEdInstId;
    }

    public String getmId() {
        return mId;
    }

    public String getmName() {
        return mName;
    }

    public String getmEdInstId() {
        return mEdInstId;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public void setmEdInstId(String mEdInstId) {
        this.mEdInstId = mEdInstId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    @Override
    public String toString() {return mName;}
}
