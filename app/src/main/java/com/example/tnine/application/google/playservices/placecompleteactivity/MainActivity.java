package com.example.tnine.application.google.playservices.placecompleteactivity;


import com.example.tnine.application.R;
import com.example.tnine.application.alarm.ScheduleAlarm;
import com.example.tnine.application.contentlist.ContentList;
import com.example.tnine.application.databasehandling.DBManipulation;
import com.example.tnine.application.network.NetworkOperation;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    /**
     * Request code for the autocomplete activity. This will be used to identify results from the
     * autocomplete activity in onActivityResult.
     */
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final int DATE_DIALOG_ID = 0;
    private static final int TIME_DIALOG_ID=1;
    private static final String KEY="AIzaSyBT0dPGIOTmmuh5XKdzS4Adkbyj9FEHdjM";
    private static final String COR_API="https://maps.googleapis.com/maps/api/timezone/json?location=";

    TimeZone t= TimeZone.getDefault();
    final String MY_TIME_ZONE=t.getID();

    private String pAdd="",rTitle,repeat="";
    private int year,month,day,hour,minute;  // declare  the variables
    private double lon,lat;
    private String time_zone=TimeZone.getDefault().toString();
    private long rawOffset;
    private long dstOffset;
    private DBManipulation databaseAdapter;
    private long alarmOffset,alarmdiff;

    private AutoCompleteTextView openButton;
    private EditText title;
    private TextView text1;
    private TextView text2;
    private ImageView min1, min2, hr1, hr2;
    private TextView date1;
    private TextView date2;
    private Button cancel;
    private Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        openButton = (AutoCompleteTextView) findViewById(R.id.location);
        title=(EditText)findViewById(R.id.edittext);
        text1=(TextView)findViewById(R.id.text1);
        text2=(TextView)findViewById(R.id.text2);
        min1=(ImageView)findViewById(R.id.min1);
        min2=(ImageView)findViewById(R.id.min2);
        hr1=(ImageView)findViewById(R.id.hr1);
        hr2=(ImageView)findViewById(R.id.hr2);
        date1=(TextView)findViewById(R.id.date1);
        date2=(TextView)findViewById(R.id.date2);
        cancel=(Button)findViewById(R.id.cancel);
        save=(Button)findViewById(R.id.save);


        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        Calendar cl = Calendar.getInstance();
        //long time = cl.getTimeInMillis();
        year = cl.get(Calendar.YEAR);
        month = cl.get(Calendar.MONTH);
        day = cl.get(Calendar.DAY_OF_MONTH);
        hour = cl.get(Calendar.HOUR_OF_DAY);
        minute = cl.get(Calendar.MINUTE);
        min2.setRotation(minute*6);
        hr2.setRotation((minute * 0.5f)+(hour*30));
        date2.setText(year+"-"+month+"-"+day);
        //String timeFormat="HH:MM";
        String dateFormat="YYYY-MM-DD";
        date1.setText(dateFormat);

        openButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    openAutocompleteActivity();
                    openButton.clearFocus();
                }
            }
        });
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAutocompleteActivity();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verify();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnBack();
            }
        });
    }

    private void verify()
    {
        rTitle=title.getText().toString();
        if(pAdd.equals(""))
            Toast.makeText(getApplicationContext(), "Please Enter the Location", Toast.LENGTH_SHORT).show();
        else if (rTitle.equals(""))
            Toast.makeText(getApplicationContext(), "Please Enter the title", Toast.LENGTH_SHORT).show();
        else
            //Schedule Alarm
            schedulealarm();
    }

    private void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            //Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called after the autocomplete activity has finished to return its result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                //Log.i(TAG, "Place Selected: " + place.getName());

                // Format the place's details and display them in the TextView.
                String pName=(place.getName()).toString();
                String pId= place.getId();
                pAdd=(place.getAddress()).toString();
                text1.setText(pName);
                openButton.setText(pName);

                //Get Place Details using Place ID
                try {
                    NetworkOperation n = new NetworkOperation("https://maps.googleapis.com/maps/api/place/details/json?placeid="+pId+"&key=" + "AIzaSyAhWSDLsQBZkETws9s78XTDW3b7bvVfhBU");
                    AsyncTask<Void, Void, String> x = n.execute();
                    String response = x.get();
                    //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                    if (response == null) {
                        Toast.makeText(getApplicationContext(), "Error!!! Please check your connection and try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try
                    {
                        JSONObject jsonRootObject= new JSONObject(response);
                        JSONObject location= ((jsonRootObject.getJSONObject("result")).getJSONObject("geometry")).getJSONObject("location");
                        lat=location.optDouble("lat");
                        lon=location.optDouble("lng");

                        //Toast.makeText(getApplicationContext(), lat+" "+lon, Toast.LENGTH_SHORT).show();

                        getTimeZone();
                    }
                    catch (JSONException e)
                    {
                        android.util.Log.e("JSONError",e.getMessage());
                    }
                }
                catch (Exception e)
                {
                    android.util.Log.e("NetworkError",e.getMessage());
                }

            }
            else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                PlaceAutocomplete.getStatus(this, data);
                //Log.e(TAG, "Error: Status = " + status.toString());
            }
            else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }
    }

    /**
     * Helper method to format information about a place nicely.
     */
   /* private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
            CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }*/

    public void showDate(View v)
    {
        this.showDialog(DATE_DIALOG_ID);

    }

    public void showTime(View v)
    {
        this.showDialog(TIME_DIALOG_ID);
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int yearSelected,
                                      int monthOfYear, int dayOfMonth) {
                    year = yearSelected;
                    month = monthOfYear;
                    day = dayOfMonth;
                    GregorianCalendar c1 = new GregorianCalendar(year, month, day);
                    long timesec = c1.getTimeInMillis();
                    String time1 = getDate(timesec);
                    date1.setText(time1.substring(0, time1.indexOf(" ")));
                    alarmTime();
                    String time2 = getDate(alarmOffset);
                    date2.setText(time2.substring(0, time2.indexOf(" ")));
                    float hrr=Float.parseFloat(time2.substring(time2.indexOf(" "), time2.length()-6));
                    if (hrr<12)
                        hrr=hrr*30;
                    else
                        hrr=(hrr-12)*30;
                    min2.setRotation(Float.parseFloat(time2.substring(time2.length() - 5, time2.length()-3)) * 6);
                    hr2.setRotation((Float.parseFloat(time2.substring(time2.length() - 5, time2.length()-3)) * 0.5f)+hrr);
                    //clock2.setText(time2.substring(time2.indexOf(" "),time2.length()-3));
                    //long timeUpdated=time1.getTimeInMillis();
                    //String Date=getDate(timeUpdated);
                    //btnsetDate.setText(Date.substring(0, Date.indexOf(':') - 3));
                }
            };

    // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener()
            {
                public void onTimeSet(TimePicker view, int hourOfDay, int min)
                {
                    hour = hourOfDay;
                    minute = min;
                    float hr;
                    if (hour<12)
                        hr=hour*30;
                    else
                        hr=(hour-12)*30;
                    min1.setRotation(minute*6);
                    hr1.setRotation((minute * 0.5f)+hr);
                    alarmTime();
                    String time2= getDate(alarmOffset);
                    date2.setText(time2.substring(0, time2.indexOf(" ")));
                    float hrr=Float.parseFloat(time2.substring(time2.indexOf(" "), time2.length()-6));
                    if (hrr<12)
                        hrr=hrr*30;
                    else
                        hrr=(hrr-12)*30;
                    min2.setRotation(Float.parseFloat(time2.substring(time2.length() - 5, time2.length()-3)) * 6);
                    hr2.setRotation((Float.parseFloat(time2.substring(time2.length() - 5, time2.length()-3)) * 0.5f)+hrr);
                    //TODO: Add ImageButton
                    //btnsetTime.setText(hour+":"+minute);
                }
            };

    @Override
    protected Dialog onCreateDialog(int id)
    {
        final Calendar c = Calendar.getInstance();
        int  mYear = c.get(Calendar.YEAR);
        int  mMonth = c.get(Calendar.MONTH);
        int  mDay = c.get(Calendar.DAY_OF_MONTH);
        int  mHour = c.get(Calendar.HOUR_OF_DAY);
        int  mMinute = c.get(Calendar.MINUTE);
        switch (id)
        {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener, mHour, mMinute, false);

        }
        return null;
    }

    public  String getDate(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currenTimeZone = calendar.getTime();
            return sdf.format(currenTimeZone);
        }
        catch (Exception e) {
            return "";
        }
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override

    public void onBackPressed() {

        // TODO Auto-generated method stub

        super.onBackPressed();
    }

    private void returnBack()
    {
        finish();
    }

    private void getTimeZone(){

        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date());
            Timestamp timestamp = Timestamp.valueOf(currentTimeStamp);
            long sectimestamp=timestamp.getTime();
            NetworkOperation n = new NetworkOperation(COR_API + lat+","+lon+"&timestamp="+(sectimestamp/1000)+"&apikey=" + KEY);
            AsyncTask<Void, Void, String> x = n.execute();
            String response = x.get();

            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            try {
                JSONObject jsonRootObject = new JSONObject(response);
                time_zone= jsonRootObject.optString("timeZoneId");
                GregorianCalendar cl= new GregorianCalendar(TimeZone.getTimeZone(time_zone));
                year = cl.get(Calendar.YEAR);
                month = cl.get(Calendar.MONTH);
                day = cl.get(Calendar.DAY_OF_MONTH);
                hour = cl.get(Calendar.HOUR_OF_DAY);
                minute = cl.get(Calendar.MINUTE);
                /*min1.setRotation(minute*6);
                float hr;
                if (hour<12)
                    hr=hour*30;
                else
                    hr=(hour-12)*30;
                hr1.setRotation((minute * 0.5f) + hr);
                Log.e("Fifth", "Entered fifth");
               */
                date1.setText(year + "-" + month + "-" + day);
                alarmTime();

                String time2= getDate(alarmOffset);
                date2.setText(time2.substring(0, time2.indexOf(" ")));
                /*float hrr;
                hrr=Float.parseFloat(time2.substring(time2.indexOf(" "), time2.length()-6));
                if (hrr<12)
                    hrr=hrr*30;
                else
                    hrr=(hrr-12)*30;
                min2.setRotation(Float.parseFloat(time2.substring(time2.length() - 5, time2.length()-3)) * 6);
                hr2.setRotation((Float.parseFloat(time2.substring(time2.length() - 5, time2.length()-3)) * 0.5f)+hrr);
                Log.e("Sixth", "Entered sixth");
                */
                //String time1=getDate(cl.getTimeInMillis());
                //Date d= cl.getTime();
                //date1.setText(time1.substring(0,time1.indexOf(" ")));
                rawOffset=jsonRootObject.getInt("rawOffset")*1000;
                dstOffset=jsonRootObject.getInt("dstOffset")*1000;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
        catch(Exception e)
        {

        }
    }

    void alarmTime() {
        try {

            TimeZone.setDefault(TimeZone.getTimeZone(time_zone));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date());
            Timestamp timestamp = Timestamp.valueOf(currentTimeStamp);
            //long current=timestamp.getTime();


            String str_date = "" + year + "-" + (month + 1) + "-" + day + " " + hour + ":" + minute + ":00";
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(str_date);

            alarmOffset = date.getTime();

            TimeZone.setDefault(TimeZone.getTimeZone(MY_TIME_ZONE));
            Date d = new Date();
            long current = d.getTime();
            alarmdiff = alarmOffset - current;
        } catch (Exception e) {
            android.util.Log.e("Error", e.getMessage());
        }
    }

    void schedulealarm()
    {
            if (alarmdiff < 0) {
                Toast.makeText(getApplicationContext(), "Cannot set alarm for a past time.", Toast.LENGTH_SHORT).show();
                return;
            }

            databaseAdapter = new DBManipulation(getApplicationContext());
            databaseAdapter.open();
        //TODO: Enter correct entry and improve here for listview
            databaseAdapter.insertEntry(rTitle, pAdd, repeat, alarmOffset);
            databaseAdapter.close();

            ScheduleAlarm obj = new ScheduleAlarm(getApplicationContext());
            obj.schedulealarm();
            Toast.makeText(getApplicationContext(), "Alarm Scheduled successfully. " + getDate(alarmOffset), Toast.LENGTH_SHORT).show();

            finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id= item.getItemId();
        if(id==R.id.action_list)
        {
            Intent intentUpdateName = new Intent(getApplicationContext(), ContentList.class);
            startActivity(intentUpdateName);
        }
        return super.onOptionsItemSelected(item);
    }
}
