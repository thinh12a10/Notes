package com.thinhnguyen.notes.until;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.thinhnguyen.notes.model.Alarm;
import com.thinhnguyen.notes.model.Note;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class DBHelper extends SQLiteOpenHelper {

    Context mContext;

    SQLiteDatabase mDataBase;

    private String DATABASE_PATH;
    private String DATABASE_FULLPATH;

    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    private final String COL_ID = "id";
    private final String COL_CONTENT = "content";
    private final String COL_TITLE = "title";
    private final String COL_TIME = "time";
    private final String COL_HAS_ALARM = "has_alarm";

    private final String COL_NOTE_ID = "note_id";
    private final String COL_START_DATE = "start_date";
    private final String COL_INTERVAL_TIME = "interval_time";
    private final String COL_REPEAT_TYPE = "repeat_type";
    private final String COL_DAYS_OF_WEEK = "days_of_week";

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;
        DATABASE_PATH = "data/data/" + mContext.getPackageName() + "/database";
        DATABASE_FULLPATH = DATABASE_PATH + DATABASE_NAME;

        if (!isExitingDB(DATABASE_FULLPATH))
        {
            File file = new File(DATABASE_PATH);
            file.mkdirs();

            extractAssetToDB(DATABASE_FULLPATH);
        }

        mDataBase = SQLiteDatabase.openOrCreateDatabase(DATABASE_FULLPATH, null);
    }

    public boolean isExitingDB(String fullPath)
    {
        File file = new File(fullPath);
        return file.exists();
    }

    private void extractAssetToDB(String fullPath)
    {
        try
        {
            InputStream inputStream = mContext.getAssets().open(DATABASE_NAME);

            File file = new File(fullPath);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];

            String content;
            int length;

            while ((length = inputStream.read(buffer)) > 0)
            {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }
        catch (IOException e)
        {

        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long AddNewNote(Note note)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TITLE, note.getTitle());
        contentValues.put(COL_CONTENT, note.getContent());
        contentValues.put(COL_TIME, note.getDate());
        contentValues.put(COL_HAS_ALARM, note.isHasAlarm());

        return mDataBase.insert("notes", null, contentValues);
    }

    public ArrayList<Note> getNotes()
    {
        ArrayList<Note> notes = new ArrayList<>();

        String query = "SELECT * FROM notes ORDER by id DESC";

        Cursor cursor = mDataBase.rawQuery(query, null);

        while (cursor.moveToNext())
        {
            int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
            String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
            String content = cursor.getString(cursor.getColumnIndex(COL_CONTENT));
            String date = cursor.getString(cursor.getColumnIndex(COL_TIME));
            int hasAlarm = cursor.getInt(cursor.getColumnIndex(COL_HAS_ALARM));

            Note note = new Note(id, title, content, date, hasAlarm);
            notes.add(note);
        }

        return notes;
    }

    public Note getNoteById(int noteID)
    {
        Note note = null;

        String query = "SELECT * FROM notes where id = ?";

        Cursor cursor = mDataBase.rawQuery(query, new String[]{String.valueOf(noteID)});

        while (cursor.moveToNext())
        {
            //int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
            String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
            String content = cursor.getString(cursor.getColumnIndex(COL_CONTENT));
            String date = cursor.getString(cursor.getColumnIndex(COL_TIME));
            int hasAlarm = cursor.getInt(cursor.getColumnIndex(COL_HAS_ALARM));

            note = new Note(noteID, title, content, date, hasAlarm);
        }

        return note;
    }

    public void clearAllNotes()
    {
        String query = "DELETE FROM notes";
        mDataBase.execSQL(query);
    }

    public void updateNote(Note note)
    {
        String query = "UPDATE notes SET title=?, content=?, has_alarm=? where id=?";
        mDataBase.execSQL(query, new Object[]{note.getTitle(), note.getContent(), note.isHasAlarm(), note.getId()});
    }

    public void deleteNote(int id)
    {
        String query = "DELETE FROM notes WHERE id=?";
        mDataBase.execSQL(query, new Object[]{id});
    }

    public int getLastInsertRowId()
    {
        int rowId = 0;
        String query = "select last_insert_rowid()";

        Cursor cursor = mDataBase.rawQuery(query, null);
        while (cursor.moveToNext())
        {
            rowId = cursor.getInt(0);
        }

        return rowId;
    }

    public long addNewAlarm(Alarm alarm)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NOTE_ID, alarm.getNoteId());
        contentValues.put(COL_START_DATE, alarm.getDateStart().getTimeInMillis());
        contentValues.put(COL_INTERVAL_TIME, alarm.getIntervalTime());
        contentValues.put(COL_REPEAT_TYPE, alarm.getAlarmRepeatType());
        contentValues.put(COL_DAYS_OF_WEEK, alarm.getDaysOfWeekInJson());

        return mDataBase.insert("alarm", null, contentValues);
    }

    public void updateAlarm(Alarm alarm)
    {
        String query = "UPDATE alarm SET start_date=?, interval_time=?, repeat_type=?, days_of_week=? where note_id=?";
        mDataBase.execSQL(query, new Object[]{alarm.getDateStart().getTimeInMillis(),
                alarm.getIntervalTime(), alarm.getAlarmRepeatType(), alarm.getDaysOfWeekInJson(), alarm.getNoteId()});
    }

    public Alarm getAlarmByNoteId(int noteId)
    {
        Alarm alarm = null;

        String query = "SELECT * FROM alarm where note_id = ?";

        Cursor cursor = mDataBase.rawQuery(query, new String[]{String.valueOf(noteId)});

        while (cursor.moveToNext())
        {
            long startDateInMilisecon = cursor.getLong(cursor.getColumnIndex(COL_START_DATE));

            Calendar startDate = Calendar.getInstance();
            startDate.setTimeInMillis(startDateInMilisecon);

            long intervalTime = cursor.getLong(cursor.getColumnIndex(COL_INTERVAL_TIME));
            int repeatType = cursor.getInt(cursor.getColumnIndex(COL_REPEAT_TYPE));
            String daysOfWeekInJson = cursor.getString(cursor.getColumnIndex(COL_DAYS_OF_WEEK));
            ArrayList<Integer> daysOfWeek = getDaysOfWeekFromJson(daysOfWeekInJson);

            alarm = new Alarm(noteId, startDate, intervalTime, repeatType, daysOfWeek);
        }

        return alarm;
    }

    private ArrayList<Integer> getDaysOfWeekFromJson(String daysOfWeekInJson)
    {
        ArrayList<Integer> daysOfWeek = new ArrayList<>();

        JSONObject json = null;
        JSONArray items = null;
        try {
            json = new JSONObject(daysOfWeekInJson);
            items = json.optJSONArray("uniqueArrays");

            for (int i = 0; i < items.length(); i++)
            {
                daysOfWeek.add(items.getInt(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return daysOfWeek;
    }

    public void deleteAlarm(int noteId)
    {
        String query = "DELETE FROM alarm WHERE note_id=?";
        mDataBase.execSQL(query, new Object[]{noteId});
    }

    public void clearAllAlarms()
    {
        String query = "DELETE FROM alarm";
        mDataBase.execSQL(query);
    }
}
