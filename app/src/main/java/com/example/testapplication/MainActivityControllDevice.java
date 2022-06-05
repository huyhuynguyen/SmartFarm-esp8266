package com.example.testapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivityControllDevice extends AppCompatActivity {

    private TextView pumpTimeStart, pumpTimeEnd, lightTimeStart, lightTimeEnd, servoTimeStart, servoTimeEnd;
    private Button  btnPump, btnLight, btnServo ,btnSettingTimePump, btnSettingTimeLight, btnSettingTimeServo;
    private String colorOn = "#09FBE5";
    private String colorOff = "#CFD3D2";

    Timer timer;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_controll_device);

        btnPump = (Button) findViewById(R.id.btnPump);
        btnLight = (Button) findViewById(R.id.btnLight);
        btnServo = (Button) findViewById(R.id.btnServo);
        btnSettingTimePump = (Button) findViewById(R.id.btnSettime_pump);
        btnSettingTimeLight = (Button) findViewById(R.id.btnSettime_light);
        btnSettingTimeServo = (Button) findViewById(R.id.btnSettime_servo);
        pumpTimeStart = (TextView) findViewById(R.id.pump_time_start);
        pumpTimeEnd = (TextView) findViewById(R.id.pump_time_end);
        lightTimeStart = (TextView) findViewById(R.id.light_time_start);
        lightTimeEnd = (TextView) findViewById(R.id.light_time_end);
        servoTimeStart = (TextView) findViewById(R.id.servo_time_start);
        servoTimeEnd = (TextView) findViewById(R.id.servo_time_end);

        DatabaseReference refTimePumpStart = FirebaseDatabase.getInstance().getReference("timeStartPump");
        DatabaseReference refTimePumpEnd = FirebaseDatabase.getInstance().getReference("timeEndPump");
        DatabaseReference refTimeLightStart = FirebaseDatabase.getInstance().getReference("timeStartLight");
        DatabaseReference refTimeLightEnd = FirebaseDatabase.getInstance().getReference("timeEndLight");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        /// -------------------- Pump --------------- ///
        final DocumentReference pumpRef = db.collection("controls").document("pump");
        pumpRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String value = snapshot.getData().get("status").toString();
                    String timeStartValue = snapshot.getData().get("timeStart").toString();
                    String timeEndValue = snapshot.getData().get("timeEnd").toString();

                    // status
                    if (value.equals("false")) {
                        changeStateDown(btnPump, "PUMP OFF");
                    } else if (value.equals("true")) {
                        changeStateUp(btnPump, "PUMP ON");
                    }

                    // time
                    pumpTimeStart.setText(timeStartValue);
                    actionDevice(pumpTimeStart.getText().toString(), btnPump, "PUMP ON", "up", pumpRef);

                    pumpTimeEnd.setText(timeEndValue);
                    actionDevice(pumpTimeEnd.getText().toString(), btnPump, "PUMP OFF", "down", pumpRef);

                } else {
                    Log.d("null data", "Current data: null");
                }
            }
        });

        /// -------------------- Led --------------- ///
        final DocumentReference ledRef = db.collection("controls").document("led");
        ledRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String value = snapshot.getData().get("status").toString();
                    String timeStartValue = snapshot.getData().get("timeStart").toString();
                    String timeEndValue = snapshot.getData().get("timeEnd").toString();

                    // status
                    if (value.equals("false")) {
                        changeStateDown(btnLight, "LIGHT OFF");
                    } else if (value.equals("true")) {
                        changeStateUp(btnLight, "LIGHT ON");
                    }

                    // time
                    lightTimeStart.setText(timeStartValue);
                    actionDevice(lightTimeStart.getText().toString(), btnLight, "LIGHT ON", "up", ledRef);

                    lightTimeEnd.setText(timeEndValue);
                    actionDevice(lightTimeEnd.getText().toString(), btnLight, "LIGHT OFF", "down", ledRef);
                } else {
                    Log.d("null data", "Current data: null");
                }
            }
        });

        /* -------------------- Time Light ----------------  */
//        refTimeLightStart.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                lightTimeStart.setText(snapshot.getValue().toString());
//                actionDevice(lightTimeStart.getText().toString(), btnLight, "LIGHT ON", "up", refLight);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

//        refTimeLightEnd.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                lightTimeEnd.setText(snapshot.getValue().toString());
//                actionDevice(lightTimeEnd.getText().toString(), btnLight, "LIGHT OFF", "down", refLight);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        final DocumentReference servoRef = db.collection("controls").document("servo");
        servoRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String value = snapshot.getData().get("status").toString();
                    String timeStartValue = snapshot.getData().get("timeStart").toString();
                    String timeEndValue = snapshot.getData().get("timeEnd").toString();
                    if (value.equals("false")) {
                        changeStateDown(btnServo, "SERVO OFF");
                    } else if (value.equals("true")) {
                        changeStateUp(btnServo, "SERVO ON");
                    }

                    // time
                    servoTimeStart.setText(timeStartValue);
                    actionDevice(servoTimeStart.getText().toString(), btnServo, "SERVO ON", "up", servoRef);

                    servoTimeEnd.setText(timeEndValue);
                    actionDevice(servoTimeEnd.getText().toString(), btnServo, "SERVO OFF", "down", servoRef);
                } else {
                    Log.d("null data", "Current data: null");
                }
            }
        });

        // Manual click pump, light and servo
        btnPump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnPump.getText().toString() == "PUMP OFF") {
                    pumpRef.update("status", true);
                    changeStateUp(btnPump, "PUMP ON");
                } else {
                    pumpRef.update("status", false);
                    changeStateDown(btnPump, "PUMP OFF");
                }
            }
        });

        btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnLight.getText().toString() == "LIGHT OFF") {
                    ledRef.update("status", true);
                    changeStateUp(btnLight, "LIGHT ON");
                } else {
                    ledRef.update("status", false);
                    changeStateDown(btnLight, "LIGHT OFF");
                }
            }
        });

        btnServo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnServo.getText().toString() == "SERVO OFF") {
                    servoRef.update("status", true);
                    changeStateUp(btnServo, "SERVO ON");
                } else {
                    servoRef.update("status", false);
                    changeStateDown(btnServo, "SERVO OFF");
                }
            }
        });

        // --------------------- Open setting view --------------------- //
        btnSettingTimePump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivitySettingTime("Pump", (String) pumpTimeStart.getText(), (String) pumpTimeEnd.getText());
            }
        });

        btnSettingTimeLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivitySettingTime("Led", (String) lightTimeStart.getText(), (String) lightTimeEnd.getText());
            }
        });

        btnSettingTimeServo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivitySettingTime("Servo", (String) servoTimeStart.getText(), (String) servoTimeEnd.getText());
            }
        });
    }

    /*
        Open activity setting time
     */
    public void openActivitySettingTime(String device, String startTimeText, String endTimeText) {
        Intent intent = new Intent(this, ActivitySettime.class);
        intent.putExtra("Start", startTimeText);
        intent.putExtra("End", endTimeText);
        intent.putExtra("Device", device);
        startActivity(intent);
    }

    /*
            Change state button up
         */
    public void changeStateUp(Button btn, String text) {
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.parseColor(colorOn));
    }

    /*
        Change state button down
     */
    public void changeStateDown(Button btn, String text) {
        btn.setText(text);
        btn.setTextColor(Color.BLACK);
        btn.setBackgroundColor(Color.parseColor(colorOff));
    }

    /*
        Auto change state
     */
    public void autoChange(Button btn, String text, long delay, String key, DocumentReference ref) {
        if (delay >= 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (key == "up") {
                        changeStateUp(btn, text);
                        ref.update("status", true);
                    } else if (key == "down") {
                        changeStateDown(btn, text);
                        ref.update("status", false);
                    }
                }
            }, delay);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void actionDevice(String time, Button btn, String text, String key, DocumentReference ref) {
        LocalDate currentDate = LocalDate.now();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateNow = simpleDateFormat.format(c.getTime());
        String dateTime = currentDate + " " + time;
        try {
            Date date = (Date)simpleDateFormat.parse(dateTime);
            Date dateNowFormat = (Date)simpleDateFormat.parse(dateNow);
            long getTimeNow = dateNowFormat.getTime();
            long delayStart = date.getTime() - getTimeNow;

            timer = new Timer();
            // Check time to change state pump and light
            if (delayStart > 0) {
                autoChange(btn, text, delayStart, key, ref);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}