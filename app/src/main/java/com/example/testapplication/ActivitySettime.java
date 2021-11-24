package com.example.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

public class ActivitySettime extends AppCompatActivity {

    private TimePicker timePicker;
    private Button btnSetStart, btnSetEnd, btnDoneSave;
    private TextView timeStartText, timeEndText, deviceTitle;

    private boolean is24hView = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settime);

        timePicker = (TimePicker) findViewById(R.id.timePicker2);
        btnSetStart = (Button) findViewById(R.id.btnSet_start);
        btnSetEnd = (Button) findViewById(R.id.btnSet_end);
        btnDoneSave = (Button) findViewById(R.id.btnDoneSave);
        timeStartText = (TextView) findViewById(R.id.timeStart);
        timeEndText = (TextView) findViewById(R.id.timeEnd);
        deviceTitle = (TextView) findViewById(R.id.deviceTime_title);

        timePicker.setIs24HourView(is24hView);

        deviceTitle.setText(getIntent().getStringExtra("Device") + " time");
        timeStartText.setText(getIntent().getStringExtra("Start"));
        timeEndText.setText(getIntent().getStringExtra("End"));

        // Click to change time start and end
        btnSetStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSetStart.setBackgroundColor(Color.CYAN);
                btnSetEnd.setBackgroundColor(Color.parseColor("#2196F3"));
                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        timeStartText.setText(hourOfDay + ":" + minute + ":00");
                    }
                });
            }
        });

        btnSetEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSetEnd.setBackgroundColor(Color.CYAN);
                btnSetStart.setBackgroundColor(Color.parseColor("#2196F3"));
                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        timeEndText.setText(hourOfDay + ":" + minute + ":00");
                    }
                });
            }
        });

        // Open main view
        btnDoneSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnParentActivity(getIntent().getStringExtra("Device"));
            }
        });
    }

    /*
        Save data and go to parent activity
     */
    public void turnParentActivity(String device) {
        Intent intent = new Intent(ActivitySettime.this, MainActivity.class);
        intent.putExtra("Start", (String) timeStartText.getText());
        intent.putExtra("End", (String) timeEndText.getText());
        intent.putExtra("Device", device);
        startActivity(intent);
    }
}