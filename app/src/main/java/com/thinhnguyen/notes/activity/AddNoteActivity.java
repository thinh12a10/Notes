package com.thinhnguyen.notes.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.thinhnguyen.notes.R;
import com.thinhnguyen.notes.model.Alarm;
import com.thinhnguyen.notes.model.Note;
import com.thinhnguyen.notes.until.DBHelper;
import com.thinhnguyen.notes.until.NotificationReceiver;
import com.thinhnguyen.notes.until.RequestCodeDefine;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddNoteActivity extends AppCompatActivity {
    Toolbar mToolbar;

    EditText mEditTitle;
    EditText mEditNoteContent;

    Button mButtonAlarm;
    Button mButtonAlarmOff;

    Note mNoteToUpdate;
    Note mNewNote;

    Alarm mAlarm;

    boolean mIsNewNote;
    boolean mHasExitedInDB = false;

    int mIsHasAlarm = 0;

    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default";

    int mRequestId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        initComponent();
        actionBar();
        getIntentInfo();
        registerButtonEvent();
        updateAlarmOffDisplay();
    }

    private void initComponent() {
        mToolbar = findViewById(R.id.toolbar_add_note);
        mEditTitle = findViewById(R.id.edit_input_title);
        mEditNoteContent = findViewById(R.id.edit_input_note);
        mButtonAlarm = findViewById(R.id.btn_alarm_add);
        mButtonAlarmOff = findViewById(R.id.btn_alarm_off);
    }

    private void actionBar()
    {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_done);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsNewNote)
                {
                    AddnewNote();
                }
                else
                {
                    UpdateNote();
                }
            }
        });
    }

    private void getIntentInfo() {
        Intent intent = getIntent();
        mIsNewNote = intent.getBooleanExtra("isNewNote", true);

        if (!mIsNewNote)
        {
            mNoteToUpdate = (Note) intent.getSerializableExtra("note");
            mEditTitle.setText(mNoteToUpdate.getTitle());
            mEditNoteContent.setText(mNoteToUpdate.getContent());

            mRequestId = mNoteToUpdate.getId();
            mIsHasAlarm = mNoteToUpdate.isHasAlarm();
            if (mIsHasAlarm == 1)
            {
                mHasExitedInDB = true;
            }
        }
    }

    private void registerButtonEvent()
    {
        mButtonAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddNoteActivity.this, AlarmActivity.class);
                intent.putExtra("noteId", mRequestId);
                startActivityForResult(intent, RequestCodeDefine.ADD_ALARM_REQUEST_CODE);
            }
        });

        mButtonAlarmOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddNoteActivity.this);
                builder.setTitle("Remove reminder");
                builder.setMessage("Are you sure you want to delete reminder?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mIsHasAlarm = 0;
                        updateAlarmOffDisplay();
                        removeNotification();

                        if (!mIsNewNote)
                        {
                            mNoteToUpdate.setHasAlarm(mIsHasAlarm);
                            DBHelper dbHelper = new DBHelper(AddNoteActivity.this);
                            dbHelper.updateNote(mNoteToUpdate);
                            dbHelper.deleteAlarm(mNoteToUpdate.getId());
                        }
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();
            }
        });
    }

    private void updateAlarmOffDisplay()
    {
        if (mIsHasAlarm == 1)
        {
            mButtonAlarmOff.setVisibility(View.VISIBLE);
        }
        else
        {
            mButtonAlarmOff.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cancle, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.item_cancle:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RequestCodeDefine.ADD_ALARM_REQUEST_CODE && resultCode == RESULT_OK)
        {
            mAlarm = (Alarm) data.getSerializableExtra("alarm");
            mIsHasAlarm = 1;
            updateAlarmOffDisplay();
        }
        else if (requestCode == RequestCodeDefine.ADD_ALARM_REQUEST_CODE && resultCode == RESULT_CANCELED)
        {

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void AddnewNote()
    {
        String title = mEditTitle.getText().toString();
        String content = mEditNoteContent.getText().toString();

        if (isValidInput(title, content))
        {
            Date currentTime = new Date();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'Thg' MM yyyy HH:mm");
            String timeStr = dateFormat.format(currentTime);

            mNewNote = new Note(0, title, content, timeStr, mIsHasAlarm);

            DBHelper dbHelper = new DBHelper(AddNoteActivity.this);
            mRequestId = (int) dbHelper.AddNewNote(mNewNote);

            if (mAlarm != null && mIsHasAlarm == 1)
            {
                scheduleNotification(mAlarm.getAlarmRepeatType());
                mAlarm.setNoteId(mRequestId);
                dbHelper.addNewAlarm(mAlarm);
            }

            setResult(RESULT_OK);
            finish();
        }
        else
        {
            Toast.makeText(AddNoteActivity.this, "You have not entered the information", Toast.LENGTH_SHORT).show();
        }
    }

    private void UpdateNote()
    {
        String title = mEditTitle.getText().toString();
        String content = mEditNoteContent.getText().toString();

        if (isValidInput(title, content))
        {
            mNoteToUpdate.setTitle(title);
            mNoteToUpdate.setContent(content);
            mNoteToUpdate.setHasAlarm(mIsHasAlarm);

            DBHelper dbHelper = new DBHelper(AddNoteActivity.this);
            dbHelper.updateNote(mNoteToUpdate);

            mRequestId = mNoteToUpdate.getId();

            if (mAlarm != null && mIsHasAlarm == 1)
            {
                scheduleNotification(mAlarm.getAlarmRepeatType());
                mAlarm.setNoteId(mRequestId);

                if (!mHasExitedInDB)
                {
                    dbHelper.addNewAlarm(mAlarm);
                }
                else
                {
                    dbHelper.updateAlarm(mAlarm);
                }
            }

            setResult(RESULT_OK);
            finish();
        }
        else
        {
            Toast.makeText(AddNoteActivity.this, "You have not entered the information", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidInput(String title, String content)
    {
        return !title.isEmpty() || !content.isEmpty();
    }

    private void scheduleNotification(int alarmType)
    {
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, mRequestId);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_TYPE, alarmType);

        if (alarmType == Alarm.DAY_OF_WEEK)
        {
            notificationIntent.putExtra("DAYS_OF_WEEK", mAlarm.getDaysOfWeek());
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, mRequestId , notificationIntent , PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        assert alarmManager != null;

        switch (alarmType)
        {
            case Alarm.ONE_TIME:
                alarmManager.set(AlarmManager.RTC_WAKEUP , mAlarm.getDateStart().getTimeInMillis(), pendingIntent);
                break;
            case Alarm.EVERY_DAYS:
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mAlarm.getDateStart().getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                break;
            case Alarm.REPEAT_BY_INTERVAL_TIME:
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mAlarm.getDateStart().getTimeInMillis(), mAlarm.getIntervalTime(), pendingIntent);
                break;
            case Alarm.DAY_OF_WEEK:
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mAlarm.getDateStart().getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                break;
        }
    }

    private void removeNotification()
    {
        //Check requestcode and intent
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, mRequestId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

}
