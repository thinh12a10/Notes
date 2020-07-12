package com.thinhnguyen.notes.until;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.thinhnguyen.notes.R;
import com.thinhnguyen.notes.activity.AddNoteActivity;
import com.thinhnguyen.notes.model.Alarm;
import com.thinhnguyen.notes.model.Note;

import java.util.ArrayList;
import java.util.Calendar;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    public static final String NOTIFICATION_NAME = "REMIDER_NOTE";

    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default";

    private Note mNote;
    private int mNoticationId;
    private int mAlarmType;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNoticationId = intent.getIntExtra(NOTIFICATION_ID , 0 );
        mAlarmType = intent.getIntExtra(NOTIFICATION_TYPE, Alarm.ONE_TIME);

        ArrayList<Integer> days_of_week = intent.getIntegerArrayListExtra("DAYS_OF_WEEK");

        if (days_of_week != null)
        {
            Calendar calendar = Calendar.getInstance();

            if (!days_of_week.contains(calendar.get(Calendar.DAY_OF_WEEK)))
            {
                return;
            }
        }

        DBHelper dbHelper = new DBHelper(context);
        mNote = dbHelper.getNoteById(mNoticationId);

        if (mAlarmType == Alarm.ONE_TIME)
        {
            mNote.setHasAlarm(0);
            dbHelper.updateNote(mNote);
            dbHelper.deleteAlarm(mNote.getId());
        }

        Notification notification = getNotification(context);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
        {
            int important = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_ID, NOTIFICATION_NAME, important);
            assert notificationManager!=null;

            //notificationManager.notify(mNoticationId, notification);
        }

        assert notificationManager != null;
        notificationManager.notify(mNoticationId , notification);
    }


    //TODO check to create Notification in this
    private Notification getNotification(Context context) {
        Intent intent = new Intent(context, AddNoteActivity.class);
        intent.putExtra("note", mNote);
        intent.putExtra("isNewNote", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, mNoticationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, default_notification_channel_id);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(mNote.getTitle()) ;
        builder.setContentText(mNote.getContent()) ;
        builder.setSmallIcon(R.drawable.ic_launcher_foreground) ;
        builder.setAutoCancel(true) ;
        builder.setChannelId(NOTIFICATION_CHANNEL_ID) ;
        return builder.build() ;
    }
}
