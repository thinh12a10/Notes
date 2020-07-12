package com.thinhnguyen.notes.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

public class Alarm implements Serializable {
    public static final int EVERY_DAYS = 20;
    public static final int ONE_TIME = 21;
    public static final int REPEAT_BY_INTERVAL_TIME = 22;
    public static final int DAY_OF_WEEK = 23;

    Calendar mDateStart;

    long mIntervalTime = -1;

    int mAlarmRepeatType;

    int mNoteId;

    ArrayList<Integer> mDaysOfWeek;

    public Alarm(Calendar dateStart, int alarmRepeatType) {
        this.mDateStart = dateStart;
        this.mAlarmRepeatType = alarmRepeatType;
        mDaysOfWeek = new ArrayList<>();
    }

    public Alarm(Calendar dateStart, long intervalTime, int alarmRepeatType) {
        this.mDateStart = dateStart;
        this.mIntervalTime = intervalTime;
        this.mAlarmRepeatType = alarmRepeatType;
        mDaysOfWeek = new ArrayList<>();
    }

    public Alarm(int noteId, Calendar mDateStart, long mIntervalTime, int mAlarmRepeatType, ArrayList<Integer> mDaysOfWeek) {
        this.mNoteId = noteId;
        this.mDateStart = mDateStart;
        this.mIntervalTime = mIntervalTime;
        this.mAlarmRepeatType = mAlarmRepeatType;
        this.mDaysOfWeek = mDaysOfWeek;
    }

    public Calendar getDateStart() {
        return mDateStart;
    }

    public void setDateStart(Calendar dateStart) {
        this.mDateStart = dateStart;
    }

    public long getIntervalTime() {
        return mIntervalTime;
    }

    public void setIntervalTime(long intervalTime) {
        this.mIntervalTime = intervalTime;
    }

    public int getAlarmRepeatType() {
        return mAlarmRepeatType;
    }

    public void setAlarmRepeatType(int alarmRepeatType) {
        this.mAlarmRepeatType = alarmRepeatType;
    }

    public void addDayOfWeek(int dayOfWeek)
    {
        mDaysOfWeek.add(dayOfWeek);
    }

    public void setDaysOfWeek(ArrayList<Integer> daysOfWeek)
    {
        mDaysOfWeek = daysOfWeek;
    }

    public ArrayList<Integer> getDaysOfWeek()
    {
        return mDaysOfWeek;
    }

    public int getNoteId()
    {
        return mNoteId;
    }

    public void setNoteId(int noteId)
    {
        mNoteId = noteId;
    }

    public String getDaysOfWeekInJson()
    {
        JSONObject json = new JSONObject();
        try {
            json.put("uniqueArrays", new JSONArray(mDaysOfWeek));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String arrayList = json.toString();

        return arrayList;
    }
}
