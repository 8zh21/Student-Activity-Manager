package com.example.student_activity_manager;

/**
 * Created by Alexander on 15.03.2016.
 */
public class UserItem
{
    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("FacultyId")
    private String mFacultyId;
    

    public UserItem() {}

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmFacultyId() {
        return mFacultyId;
    }

    public void setmFacultyId(String mFacultyId) {
        this.mFacultyId = mFacultyId;
    }

}
