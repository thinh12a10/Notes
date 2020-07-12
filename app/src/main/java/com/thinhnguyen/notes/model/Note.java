package com.thinhnguyen.notes.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class Note implements Serializable {
    int mId;
    int mHasAlarm;

    String mTitle;
    String mContent;
    String mDate;

    public Note(int mId, String mTitle, String mContent, String mDate, int hasAlarm) {
        this.mId = mId;
        this.mTitle = mTitle;
        this.mContent = mContent;
        this.mDate = mDate;
        this.mHasAlarm = hasAlarm;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public int isHasAlarm() {
        return mHasAlarm;
    }

    public void setHasAlarm(int hasAlarm) {
        this.mHasAlarm = hasAlarm;
    }
}
