package com.example.student_activity_manager;

/**
 * Created by Alexander on 26.04.2016.
 */
public class TaskItem {
    @com.google.gson.annotations.SerializedName("id")
    private String id;

    @com.google.gson.annotations.SerializedName("parentId")
    private String parentId;

    @com.google.gson.annotations.SerializedName("name")
    private String name;

    @com.google.gson.annotations.SerializedName("comment")
    private String comment;

    @com.google.gson.annotations.SerializedName("isCompleted")
    private boolean isCompleted;

    @com.google.gson.annotations.SerializedName("userId")
    private String userId;

    public  TaskItem(String parentId, String name, String comment, boolean isCompleted)
    {
        this.parentId = parentId;
        this.name = name;
        this.comment = comment;
        this.isCompleted = isCompleted;
    }

    public String getComment() {
        return comment;
    }

    public String getId() {
        return id;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public String getName() {
        return name;
    }

    public String getParentId() {
        return parentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
