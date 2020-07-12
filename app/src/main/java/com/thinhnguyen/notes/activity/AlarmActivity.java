package com.thinhnguyen.notes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.thinhnguyen.notes.R;
import com.thinhnguyen.notes.model.Alarm;
import com.thinhnguyen.notes.until.DBHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class AlarmActivity extends AppCompatActivity {
    EditText mEditTextDate;
    EditText mEditTextHour;

    Spinner mSpinnerRepeatDate;

    Toolbar mToolBar;

    LinearLayout mLayoutCheckBox;
    LinearLayout mLayoutSelectRepeatTime;

    NumberPicker mHourPicker;
    NumberPicker mMinutePicker;

    ArrayList<CheckBox> mCheckBoxs = new ArrayList<>();

    ArrayList<String> mRepeatDateOptions;
    ArrayAdapter<String> mDateOptionsAdapter;

    Calendar mUserChoosedDate = Calendar.getInstance();

    int mDay;
    int mMonth;
    int mYear;
    int mMinute;
    int mHour;
    long mIntervalRepeat = 60*60*1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        initComponent();
        registerInputDateTimeEvent();

        Intent intent = getIntent();
        int noteId = intent.getIntExtra("noteId", -1);

        DBHelper dbHelper = new DBHelper(AlarmActivity.this);
        Alarm alarm = dbHelper.getAlarmByNoteId(noteId);

        if (alarm != null)
        {
            updateDisplayInfo(alarm);
        }
        else
        {
            getCurrentDate();
        }

        setUpActionBar();
        spinnerListener();
    }

    private void initComponent()
    {
        mEditTextDate = findViewById(R.id.edit_date);
        mEditTextHour = findViewById(R.id.edit_hour);

        mSpinnerRepeatDate = findViewById(R.id.spinner_repeat_time);
        mToolBar = findViewById(R.id.toolbar_add_alarm);


        CheckBox mondayCheckBox = findViewById(R.id.checkbox_monday);
        CheckBox tuesdayCheckBox = findViewById(R.id.checkbox_tuesday);
        CheckBox wendnesDayCheckBox = findViewById(R.id.checkbox_wendnesday);
        CheckBox thursdayCheckBox = findViewById(R.id.checkbox_thursday);
        CheckBox fridayCheckBox = findViewById(R.id.checkbox_friday);
        CheckBox saturdayCheckBox = findViewById(R.id.checkbox_saturday);
        CheckBox sundayCheckBox = findViewById(R.id.checkbox_sunday);

        mCheckBoxs.add(mondayCheckBox);
        mCheckBoxs.add(tuesdayCheckBox);
        mCheckBoxs.add(wendnesDayCheckBox);
        mCheckBoxs.add(thursdayCheckBox);
        mCheckBoxs.add(fridayCheckBox);
        mCheckBoxs.add(saturdayCheckBox);
        mCheckBoxs.add(sundayCheckBox);

        mLayoutCheckBox = findViewById(R.id.layout_checkbox);
        mLayoutSelectRepeatTime = findViewById(R.id.layout_repeat_time);

        mHourPicker = findViewById(R.id.number_picker_hour);
        mHourPicker.setMinValue(0);
        mHourPicker.setMaxValue(23);
        mHourPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%2d", value);
            }
        });

        mMinutePicker = findViewById(R.id.number_picker_minute);
        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(59);
        mMinutePicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%2d", value);
            }
        });

        mRepeatDateOptions = new ArrayList<>();
        AddDateOptions();
        mDateOptionsAdapter = new ArrayAdapter<>(AlarmActivity.this, android.R.layout.simple_list_item_1, mRepeatDateOptions);
        mSpinnerRepeatDate.setAdapter(mDateOptionsAdapter);
    }

    private void registerInputDateTimeEvent()
    {
        mEditTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        mEditTextHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker();
            }
        });
    }

    private void updateDisplayInfo(Alarm alarm)
    {
        Calendar dateStart = alarm.getDateStart();
        mDay = dateStart.get(Calendar.DATE);
        mMonth = dateStart.get(Calendar.MONTH);
        mYear = dateStart.get(Calendar.YEAR);
        mHour = dateStart.get(Calendar.HOUR_OF_DAY);
        mMinute = dateStart.get(Calendar.MINUTE);

        UpdateDateTimeText();

        mIntervalRepeat = alarm.getIntervalTime();

        int alarmType = alarm.getAlarmRepeatType();

        switch (alarmType)
        {
            case Alarm.ONE_TIME:
                mSpinnerRepeatDate.setSelection(0);
                break;
            case Alarm.EVERY_DAYS:
                mSpinnerRepeatDate.setSelection(1);
                break;
            case Alarm.REPEAT_BY_INTERVAL_TIME:
                mSpinnerRepeatDate.setSelection(2);
                int hour = (int) (mIntervalRepeat/(60*60*1000));
                int minute = (int) ((mIntervalRepeat - hour*60*60*1000)/(60*1000));
                mHourPicker.setValue(hour);
                mMinutePicker.setValue(minute);
                break;
            case Alarm.DAY_OF_WEEK:
                mSpinnerRepeatDate.setSelection(3);
                ArrayList<Integer> daysOfWeek = alarm.getDaysOfWeek();
                for (int i = 0; i < daysOfWeek.size(); i++)
                {
                    switch (daysOfWeek.get(i))
                    {
                        case Calendar.MONDAY:
                            mCheckBoxs.get(0).setChecked(true);
                            break;
                        case Calendar.TUESDAY:
                            mCheckBoxs.get(1).setChecked(true);
                            break;
                        case Calendar.WEDNESDAY:
                            mCheckBoxs.get(2).setChecked(true);
                            break;
                        case Calendar.THURSDAY:
                            mCheckBoxs.get(3).setChecked(true);
                            break;
                        case Calendar.FRIDAY:
                            mCheckBoxs.get(4).setChecked(true);
                            break;
                        case Calendar.SATURDAY:
                            mCheckBoxs.get(5).setChecked(true);
                            break;
                        case Calendar.SUNDAY:
                            mCheckBoxs.get(6).setChecked(true);
                            break;
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cancle, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_cancle)
        {
            setResult(RESULT_CANCELED);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpActionBar()
    {
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolBar.setNavigationIcon(R.drawable.ic_done);

        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int alarmRepeatType = 0;
                ArrayList<Integer> dayOfWeeks = new ArrayList<>();

                int selectedItem = mSpinnerRepeatDate.getSelectedItemPosition();

                if (selectedItem == 0)
                {
                    alarmRepeatType = Alarm.ONE_TIME;
                }
                else if (selectedItem == 1)
                {
                    alarmRepeatType = Alarm.EVERY_DAYS;
                }
                else if (selectedItem == 2)
                {
                    alarmRepeatType = Alarm.REPEAT_BY_INTERVAL_TIME;
                    mIntervalRepeat = mHourPicker.getValue()*60*60*1000 + mMinutePicker.getValue()*60*1000;
                    if (mIntervalRepeat == 0)
                    {
                        Toast.makeText(AlarmActivity.this, "Interval time must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else if (selectedItem == 3)
                {
                    if (mCheckBoxs.size() <= 0)
                    {
                        Toast.makeText(AlarmActivity.this, "You have not selected a date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    alarmRepeatType = Alarm.DAY_OF_WEEK;

                    for (int i = 0; i < mCheckBoxs.size(); i++)
                    {
                        if (mCheckBoxs.get(i).isChecked())
                        {
                            int checkboxId = mCheckBoxs.get(i).getId();
                            dayOfWeeks.add(getAlarmRepeatType(checkboxId));
                        }
                    }
                }

                Alarm alarm = new Alarm(0, mUserChoosedDate, mIntervalRepeat, alarmRepeatType, dayOfWeeks);

                Intent intent = new Intent();
                intent.putExtra("alarm", alarm);

                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void getCurrentDate()
    {
        mDay = mUserChoosedDate.get(Calendar.DAY_OF_MONTH);
        mMonth = mUserChoosedDate.get(Calendar.MONTH);
        mYear = mUserChoosedDate.get(Calendar.YEAR);
        mHour = mUserChoosedDate.get(Calendar.HOUR_OF_DAY);
        mMinute = mUserChoosedDate.get(Calendar.MINUTE);

        UpdateDateTimeText();
    }

    private void spinnerListener()
    {
        mSpinnerRepeatDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mLayoutSelectRepeatTime.setVisibility(View.GONE);
                mLayoutCheckBox.setVisibility(View.GONE);

                if (position == 2)
                {
                    mLayoutSelectRepeatTime.setVisibility(View.VISIBLE);
                }
                else if (position == 3)
                {
                    mLayoutCheckBox.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void openDatePicker()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(AlarmActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                int monthInDateTime = month + 1;
                mEditTextDate.setText(dayOfMonth + "/" + monthInDateTime + "/" + year);
                mUserChoosedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                mUserChoosedDate.set(Calendar.MONTH, month);
                mUserChoosedDate.set(Calendar.YEAR, year);
            }
        }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    private void openTimePicker()
    {
        TimePickerDialog timePickerDialog = new TimePickerDialog(AlarmActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mEditTextHour.setText(String.format("%2d", hourOfDay) + "h:" + String.format("%2d", minute) + "'");
                mUserChoosedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mUserChoosedDate.set(Calendar.MINUTE, minute);
            }
        }, mHour, mMinute, false);

        timePickerDialog.show();
    }


    private void AddDateOptions()
    {
        mRepeatDateOptions.add("Once time");
        mRepeatDateOptions.add("Every day");
        mRepeatDateOptions.add("Repeat at intervals");
        mRepeatDateOptions.add("Days in week");
    }

    private void UpdateDateTimeText()
    {
        mEditTextDate.setText(mDay + "/" + (mMonth + 1) + "/" + mYear);
        mEditTextHour.setText(String.format("%2d", mHour) + "h:" + String.format("%2d", mMinute) + "'");
    }

    private int getAlarmRepeatType(int checkBoxId)
    {
        switch (checkBoxId)
        {
            case R.id.checkbox_monday:
                return Calendar.MONDAY;
            case R.id.checkbox_tuesday:
                return Calendar.TUESDAY;
            case R.id.checkbox_wendnesday:
                return Calendar.WEDNESDAY;
            case R.id.checkbox_thursday:
                return Calendar.THURSDAY;
            case R.id.checkbox_friday:
                return Calendar.FRIDAY;
            case R.id.checkbox_saturday:
                return Calendar.SATURDAY;
            case R.id.checkbox_sunday:
                return Calendar.SUNDAY;
        }

        return -1;
    }
}
