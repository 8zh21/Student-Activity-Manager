/**
 * Created by Alexander Podshiblov on 15.03.2016.
 * Класс элемента таблицы который соответствует таблице учебных заведений
 */

package com.example.student_activity_manager;

public class EdInstItem {
    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("name")
    private String mName;

    public EdInstItem() {}

    public EdInstItem(String mName) {
        this.mName = mName;
    }

    public String getmId() {
        return mId;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    @Override
    public String toString() {return mName;}
}
