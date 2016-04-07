package com.example.tnine.application.alarm;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by user on 15-02-2016.
 */
public class AlertActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Creating an Alert Dialog Window */

        Alert alert = new Alert();
        Bundle bundle = new Bundle();
        bundle.putString("ID",getIntent().getStringExtra("ID"));
        alert.setArguments(bundle);
        /** Opening the Alert Dialog Window */
        alert.show(getSupportFragmentManager(), "Alert");
    }
}
