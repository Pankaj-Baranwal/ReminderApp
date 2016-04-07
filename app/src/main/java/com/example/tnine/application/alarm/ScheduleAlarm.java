package com.example.tnine.application.alarm;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import com.example.tnine.application.databasehandling.DBManipulation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by user on 16-02-2016.
 */
public class ScheduleAlarm{
    Context context;
    private DBManipulation databaseAdapter;
    public ScheduleAlarm(Context context)
    {
        this.context=context;
    }

    public void schedulealarm()
    {
        databaseAdapter = new DBManipulation(context);
        databaseAdapter.open();
        //Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
        Cursor cursor=databaseAdapter.getLatestTime();
        if(cursor.getCount()<1)
        {
            return;
        }

        cursor.moveToFirst();
        //  String name= cursor.getString(cursor.getColumnIndex("NAME"));
        long scheduledtime=Long.parseLong(cursor.getString(cursor.getColumnIndex("TIMESCHEDULED")));
        String id=cursor.getString(cursor.getColumnIndex("_id"));

        Intent intentAlarm = new Intent(context,AlertActivity.class);
        intentAlarm.putExtra("ID",id);
        // create the object
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //set the alarm for particular time
        alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledtime, PendingIntent.getActivity(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
        databaseAdapter.close();
        Toast.makeText(context, "Alarm Scheduled!!!", Toast.LENGTH_SHORT).show();
    }
}