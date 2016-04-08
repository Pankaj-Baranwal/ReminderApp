package com.example.tnine.application.contentlist;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tnine.application.R;
import com.example.tnine.application.alarm.ScheduleAlarm;
import com.example.tnine.application.databasehandling.DBManipulation;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ContentList extends AppCompatActivity {

    private DBManipulation databaseAdapter;
    //private Cursor cursorName,cursorEdit;
	// private String selectedWord = null;
    //private long selectedWordId;


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        databaseAdapter = new DBManipulation(getApplicationContext());
        databaseAdapter.open();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_name);

        getSupportActionBar();

        ListView listViewSetName = (ListView) findViewById(R.id.listName);
		  
		 
		  Cursor cursorName=databaseAdapter.getAllEntries();
        if(cursorName.getCount()==0)
        {
            Toast.makeText(getApplicationContext(), "No reminder to display.", Toast.LENGTH_LONG).show();
        }else{
            if(cursorName.moveToFirst()){
                do {

                }while (cursorName.moveToNext());
            }
        }
		String[] allName = new String[]{"TITLE","TIMESCHEDULED","COUNTRY"};
		  
        int viewId[]={R.id.textViewTitle,R.id.textViewTime,R.id.textViewCountry};
	 
		  
		  
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,R.layout.show_text,cursorName,allName,viewId);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex==cursor.getColumnIndex("TIMESCHEDULED")){
                    TextView tv=(TextView) view;
                    String text=cursor.getString(cursor.getColumnIndex("TIMESCHEDULED"));
                    tv.setText("");
                    text.replace(", ", " ")
                            .replace(",", " ");
                    long date;
                    int i=0;
                    while (i<text.length()){
                        if (text.indexOf(" ", i)<0) {
                            date = Long.parseLong(text.substring(i, text.length()));
                            Date time_txt =new Date(date);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                            tv.setText(tv.getText() + "" + dateFormat.format(time_txt));
                            i=text.length();
                        }
                        else {
                            date = Long.parseLong(text.substring(i, text.indexOf(" ", i)));
                            Date time_txt =new Date(date);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                            dateFormat.format(time_txt);
                            tv.setText(tv.getText() + "" + dateFormat.format(time_txt) + ", ");
                            i=text.indexOf(" ", i);
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        if (listViewSetName != null)
            listViewSetName.setAdapter(adapter);

        registerForContextMenu(listViewSetName);
	}
	
	 @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
     {
         super.onCreateContextMenu(menu, v, menuInfo);
         menu.setHeaderTitle("Select The Action");
         menu.add(0, v.getId(), 0, "Delete");
         menu.add(0, v.getId(), 0, "Modify");
         //menu.add(0, v.getId(), 0, "Repeat");
     }
	 @Override
     public boolean onContextItemSelected(MenuItem item)
     { 

         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                
         int position=info.position;
         databaseAdapter = new DBManipulation(getApplicationContext());
         databaseAdapter.open();
         // selectedWord = ((TextView) info.targetView).getText().toString();

         final Cursor cursor=databaseAdapter.getAllEntries();
         cursor.moveToPosition(position);
         int id=Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id")));
                    
         if(item.getTitle()=="Delete")
         {
             databaseAdapter.deleteEntry(id);
             ScheduleAlarm obj = new ScheduleAlarm(getApplicationContext());
             obj.schedulealarm();
             databaseAdapter.close();
             finish();
         }
         else if(item.getTitle()=="Modify") {
             Intent intentUpdateName = new Intent(getApplicationContext(), UpdateValue.class);
             intentUpdateName.putExtra("ID", id);
             databaseAdapter.close();
             startActivity(intentUpdateName);
             // scheduleAlarm();
         }
         else if(item.getTitle()=="Repeat") {
             final Dialog dialog1 = new Dialog(ContentList.this);
             dialog1.setContentView(R.layout.dialog_repeat);
             dialog1.setTitle("Repeat Alarm");
             Button daily=(Button)dialog1.findViewById(R.id.daily);
             Button weekly=(Button)dialog1.findViewById(R.id.weekly);
             Button monthly=(Button)dialog1.findViewById(R.id.monthly);
             Button yearly=(Button)dialog1.findViewById(R.id.yearly);

             Button cancel_dialog = (Button) dialog1.findViewById(R.id.cancel);
             cancel_dialog.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     dialog1.cancel();
                 }
             });
             dialog1.show();
             daily.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
/*                     databaseAdapter.insertEntry(rTitle, pAdd, repeat, alarmOffset);
                     databaseAdapter.close();
                     long timer = cursor.getColumnIndex();
                     long nextUpdateTimeMillis = timer + 525600 * DateUtils.MINUTE_IN_MILLIS;
                     String title= cursor.getString(cursor.getColumnIndex(databaseAdapter.TITLE));
                     String country=cursor.getString(cursor.getColumnIndex(databaseAdapter.COUNTRY));
                     String repeat=cursor.getString(cursor.getColumnIndex(databaseAdapter.REPEAT));
                     databaseAdapter.updateEntry(title, country, repeat, nextUpdateTimeMillis, id);
                     r.stop();
                     v.cancel();
                     obj.schedulealarm();
                     getActivity().finish();
                     ScheduleAlarm obj = new ScheduleAlarm(getApplicationContext());
                     obj.schedulealarm();
                     Toast.makeText(getApplicationContext(), "Alarm Scheduled successfully. " + getDate(alarmOffset), Toast.LENGTH_SHORT).show();
                     */
                 }
             });
         }
         else
             return false;
         return true;
     }

    @Override
    public void onBackPressed() {
        databaseAdapter.close();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
