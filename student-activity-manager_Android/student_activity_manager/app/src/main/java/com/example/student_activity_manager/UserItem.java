package com.example.student_activity_manager;

/**
 * Created by Alexander on 15.03.2016.
 */
public class UserItem
{
    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("EdInstId")
    private  String mEdInstId;

    @com.google.gson.annotations.SerializedName("FacultyId")
    private String mFacultyId;
    

    public UserItem() {}

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmEdInstId() {
        return mEdInstId;
    }

    public String getmFacultyId() {
        return mFacultyId;
    }

    public void setmFacultyId(String mFacultyId) {
        this.mFacultyId = mFacultyId;
    }

    public void setmEdInstId(String mEdInstId) {
        this.mEdInstId = mEdInstId;
    }

    @Override
    public String toString() {
        return "<User: " +
                "mId='" + mId + '\'' +
                ", mEdInstId='" + mEdInstId + '\'' +
                ", mFacultyId='" + mFacultyId + '\'' +
                '>';
    }
}
