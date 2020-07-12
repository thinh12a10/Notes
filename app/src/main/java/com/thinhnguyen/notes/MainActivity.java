package com.thinhnguyen.notes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.thinhnguyen.notes.activity.AddNoteActivity;
import com.thinhnguyen.notes.adapter.NoteAdapter;
import com.thinhnguyen.notes.model.Note;
import com.thinhnguyen.notes.until.ConnectionHelper;
import com.thinhnguyen.notes.until.DBHelper;
import com.thinhnguyen.notes.until.NotificationReceiver;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Toolbar mToolBar;

    DrawerLayout mDrawerLayout;

    NavigationView mNavigation;

    ArrayList<Note> mNotes;

    NoteAdapter mNoteAdapter;

    ListView mLvNotes;

    AdView mAdView;

    EditText mEditTextSearch;

    Button mBtnSearch;
    Button mBtnCloseSearch;

    LinearLayout mLayoutSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponent();
        setupActionBar();
        registerButtonEvent();
        getNodesFromDB();
        registerLisviewEvent();

        mNavigation.setNavigationItemSelectedListener(this);

        mAdView = findViewById(R.id.adView);
        if (ConnectionHelper.isNetworkConnected(this))
        {
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
        else
        {
            mAdView.setVisibility(View.GONE);
        }

    }

    private void initComponent() {
        mToolBar = findViewById(R.id.toolbar_main);
        mLvNotes = findViewById(R.id.lv_notes);
        mDrawerLayout = findViewById(R.id.drawerLayout_Main);
        mNavigation = findViewById(R.id.navigation_main);
        mEditTextSearch = findViewById(R.id.edit_search);
        mBtnSearch = findViewById(R.id.btn_search);
        mBtnCloseSearch = findViewById(R.id.btn_close_search);
        mLayoutSearch = findViewById(R.id.layout_search);

        mNotes = new ArrayList<>();
        mNoteAdapter = new NoteAdapter(getApplicationContext(), mNotes);
        mLvNotes.setAdapter(mNoteAdapter);
    }

    private void setupActionBar()
    {
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolBar.setNavigationIcon(R.drawable.ic_dehaze);

        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void registerButtonEvent()
    {
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutSearch.setVisibility(View.VISIBLE);
                mBtnSearch.setVisibility(View.GONE);
                mEditTextSearch.requestFocus();

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(mEditTextSearch, 0);
            }
        });

        mBtnCloseSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutSearch.setVisibility(View.GONE);
                mEditTextSearch.setText("");
                mBtnSearch.setVisibility(View.VISIBLE);

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mEditTextSearch.getWindowToken(), 0);
            }
        });

        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mNoteAdapter.getFilter().filter(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.item_add:
                OpenAddNoteActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void OpenAddNoteActivity()
    {
        Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
        intent.putExtra("isNewNote", true);
        startActivityForResult(intent, 100);
    }

    private void getNodesFromDB()
    {
        DBHelper dbHelper = new DBHelper(MainActivity.this);
        mNotes.clear();
        mNotes.addAll(dbHelper.getNotes());
        mNoteAdapter.notifyDataSetChanged();

        mNoteAdapter.getFilter().filter(mEditTextSearch.getText());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK)
        {
            getNodesFromDB();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_clear:
                openClearDialog();
                break;
            case R.id.nav_clear_alarm:
                openClearAlarm();
                break;
            case R.id.nav_rate:
                openPlayStore();
        }

        return false;
    }

    private void openClearDialog()
    {
        mDrawerLayout.closeDrawer(GravityCompat.START);

        AlertDialog.Builder builder = new  AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Delete all notes");
        builder.setMessage("Are you sure you want to delete all notes?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DBHelper dbHelper = new DBHelper(getApplicationContext());

                ArrayList<Note> notes = dbHelper.getNotes();
                for (int i = 0; i < notes.size(); i++)
                {
                    if (notes.get(i).isHasAlarm() == 1)
                    {
                        removeNotification(notes.get(i).getId());
                    }
                }

                dbHelper.clearAllNotes();
                dbHelper.clearAllAlarms();
                getNodesFromDB();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void openClearAlarm()
    {
        mDrawerLayout.closeDrawer(GravityCompat.START);

        AlertDialog.Builder builder = new  AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Delete all reminders");
        builder.setMessage("Are you sure you want to delete all reminders?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DBHelper dbHelper = new DBHelper(getApplicationContext());

                ArrayList<Note> notes = dbHelper.getNotes();
                for (int i = 0; i < notes.size(); i++)
                {
                    if (notes.get(i).isHasAlarm() == 1)
                    {
                        Note currentNote = notes.get(i);

                        removeNotification(currentNote.getId());
                        currentNote.setHasAlarm(0);
                        dbHelper.updateNote(currentNote);
                    }
                }

                dbHelper.clearAllAlarms();
                getNodesFromDB();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void openPlayStore()
    {
        try
        {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName())));
        }
        catch (ActivityNotFoundException e)
        {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void registerLisviewEvent() {
        mLvNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                intent.putExtra("isNewNote", false);

                Note currentNote = (Note) mNoteAdapter.getItem(position);
                intent.putExtra("note", currentNote);

                startActivityForResult(intent, 100);
            }
        });

        mLvNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete note");
                builder.setMessage("Are you sure you want to delete the current note?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBHelper dbHelper = new DBHelper(getApplicationContext());

                        Note currentNote = (Note) mNoteAdapter.getItem(position);
                        int noteId = currentNote.getId();

                        removeNotification(noteId);
                        dbHelper.deleteNote(noteId);

                        if (mNotes.get(position).isHasAlarm() == 1)
                        {
                            dbHelper.deleteAlarm(noteId);
                        }

                        getNodesFromDB();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();

                return true;
            }
        });
    }


    private void removeNotification(int requestId)
    {
        //Check requestcode and intent
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
