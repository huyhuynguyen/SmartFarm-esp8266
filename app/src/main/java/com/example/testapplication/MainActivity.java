package com.example.testapplication;

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBarSolidMoisture, progressBarTemperature, progressBarHumidity;
    private TextView moistureStatus, temperatureStatus, humidityStatus, pumpTimeStart, pumpTimeEnd, lightTimeStart, lightTimeEnd, textView6, tvDevicesState;
    private ImageView imgMoisture;
    private Button btnData, btnPump, btnLight, btnSettingTimePump, btnSettingTimeLight, button;

    private double moisture, temperature, humidity;;
    private int maxMoistureProgress = 1024;
    private String colorOn = "#09FBE5";
    private String colorOff = "#CFD3D2";

    Timer timer;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBarSolidMoisture = (ProgressBar) findViewById(R.id.progressBarSoilMoisture);
        progressBarTemperature = (ProgressBar) findViewById(R.id.progressBarTemperature);
        progressBarHumidity = (ProgressBar) findViewById(R.id.progressBarHumidity);
        moistureStatus = (TextView) findViewById(R.id.moisture);
        temperatureStatus = (TextView) findViewById(R.id.temperature);
        humidityStatus = (TextView) findViewById(R.id.humidity);
        imgMoisture = (ImageView) findViewById(R.id.imgMoisture);
        btnData = (Button) findViewById(R.id.btnData);
        btnPump = (Button) findViewById(R.id.btnPump);
        btnLight = (Button) findViewById(R.id.btnLight);
        btnSettingTimePump = (Button) findViewById(R.id.btnSettime_pump);
        btnSettingTimeLight = (Button) findViewById(R.id.btnSettime_light);
        pumpTimeStart = (TextView) findViewById(R.id.pump_time_start);
        pumpTimeEnd = (TextView) findViewById(R.id.pump_time_end);
        lightTimeStart = (TextView) findViewById(R.id.light_time_start);
        lightTimeEnd = (TextView) findViewById(R.id.light_time_end);
        textView6 = (TextView) findViewById(R.id.textView6);
        tvDevicesState = (TextView) findViewById(R.id.tvDevicesState);

        DatabaseReference refTimePumpStart = FirebaseDatabase.getInstance().getReference("timeStartPump");
        DatabaseReference refTimePumpEnd = FirebaseDatabase.getInstance().getReference("timeEndPump");
        DatabaseReference refTimeLightStart = FirebaseDatabase.getInstance().getReference("timeStartLight");
        DatabaseReference refTimeLightEnd = FirebaseDatabase.getInstance().getReference("timeEndLight");

        ///--------------------- Light ----------------///
        DatabaseReference refLight = FirebaseDatabase.getInstance().getReference("LED");
        refLight.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue().toString().equals("0")) {
                    changeStateDown(btnLight, "LIGHT OFF");
                } else if (snapshot.getValue().toString().equals("1")) {
                    changeStateUp(btnLight, "LIGHT ON");
                }

                try {
                    scheduleTomorrow(refTimePumpStart, refTimePumpEnd, refTimeLightStart, refTimeLightEnd);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ///--------------------- Pump ----------------///
        DatabaseReference refMotor = FirebaseDatabase.getInstance().getReference("Motor");
        refMotor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue().toString().equals("0")) {
                    changeStateDown(btnPump, "PUMP OFF");
                } else if (snapshot.getValue().toString().equals("1")) {
                    changeStateUp(btnPump, "PUMP ON");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /* -------------------- Time Pump ----------------  */
        refTimePumpStart.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pumpTimeStart.setText(snapshot.getValue().toString());
                actionDevice(pumpTimeStart.getText().toString(), btnPump, "PUMP ON", "up", refMotor);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        refTimePumpEnd.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pumpTimeEnd.setText(snapshot.getValue().toString());
                actionDevice(pumpTimeEnd.getText().toString(), btnPump, "PUMP OFF", "down", refMotor);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /* -------------------- Time Light ----------------  */
        refTimeLightStart.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lightTimeStart.setText(snapshot.getValue().toString());
                actionDevice(lightTimeStart.getText().toString(), btnLight, "LIGHT ON", "up", refLight);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        refTimeLightEnd.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lightTimeEnd.setText(snapshot.getValue().toString());
                actionDevice(lightTimeEnd.getText().toString(), btnLight, "LIGHT OFF", "down", refLight);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference refDevicesState = FirebaseDatabase.getInstance().getReference("State");
        DatabaseReference refHumidity = FirebaseDatabase.getInstance().getReference("DoAm");
        DatabaseReference refTemperature = FirebaseDatabase.getInstance().getReference("NhietDo");
        DatabaseReference refMoisture = FirebaseDatabase.getInstance().getReference("DoAmDat");
        DatabaseReference refMoistureWeek = FirebaseDatabase.getInstance().getReference("DoAmDatWeek");
        DatabaseReference refTemperatureWeek = FirebaseDatabase.getInstance().getReference("NhietDoWeek");
        DatabaseReference refHumidityWeek = FirebaseDatabase.getInstance().getReference("DoAmWeek");

        /// -------------------- State connection device --------------- ///
        refDevicesState.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvDevicesState.setText(snapshot.getValue().toString().equals("1") ? "Connected" : "Disconnected");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ///--------------------- Humidity ----------------///
        ArrayList<Integer> arrHumidityValues = new ArrayList<Integer>();
        final Timer[] timerHumidity = {new Timer()};
        refHumidity.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int value = Integer.parseInt(snapshot.getValue().toString());
                progressBarHumidity.setProgress(value);
                humidityStatus.setText(snapshot.getValue().toString() + "%");

                // Set value for recent 7 days
                if (!arrHumidityValues.contains(value)) {
                    arrHumidityValues.add(value);
                }
                timerHumidity[0].cancel();
                timerHumidity[0] = new Timer();
                if (getTimeDelay() > 0) {
                    timerHumidity[0].schedule(new TimerTask() {
                        @Override
                        public void run() {
                            float avg = 0;
                            int sum = 0;
                            for (int index = 0; index<arrHumidityValues.size(); index ++) {
                                sum += arrHumidityValues.get(index);
                            }
                            avg = sum/arrHumidityValues.size();

                            float finalAvg = avg;
                            refHumidityWeek.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    DataSnapshot snapshot1 = task.getResult();
                                    ArrayList<Float> arrValue = new ArrayList<>();
                                    // Get all values
                                    for (DataSnapshot snapshot2: snapshot1.getChildren()) {
                                        arrValue.add(Float.parseFloat(snapshot2.getValue().toString()));
                                    }

                                    // Set all values
                                    int index = 1;
                                    for (DataSnapshot snapshot2: snapshot1.getChildren()) {
                                        if (index == 7) {
                                            snapshot2.getRef().setValue(finalAvg);
                                        } else {
                                            float value = arrValue.get(index);
                                            snapshot2.getRef().setValue(value);
                                        }
                                        index += 1;
                                    }
                                }
                            });
                        }
                    }, getTimeDelay());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ///--------------------- Temperature ----------------///
        ArrayList<Float> arrTemperatureValues = new ArrayList<Float>();
        final Timer[] timerTemperature = {new Timer()};
        refTemperature.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Float TemperatureBar = Float.parseFloat(snapshot.getValue().toString());
                int Temp = Math.round(TemperatureBar);
                progressBarTemperature.setProgress(Temp);
                temperatureStatus.setText(snapshot.getValue().toString() + (char) 0x00B0 + "C");

                // Set value for recent 7 days
                if (!arrTemperatureValues.contains(TemperatureBar)) {
                    arrTemperatureValues.add(TemperatureBar);
                }

                timerTemperature[0].cancel();
                timerTemperature[0] = new Timer();
                if (getTimeDelay() > 0) {
                    timerTemperature[0].schedule(new TimerTask() {
                        @Override
                        public void run() {
                            float avg = 0;
                            float sum = 0;
                            for (int index = 0; index<arrTemperatureValues.size(); index ++) {
                                sum += arrTemperatureValues.get(index);
                            }
                            avg = sum/arrTemperatureValues.size();

                            float finalAvg = avg;
                            refTemperatureWeek.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    DataSnapshot snapshot1 = task.getResult();
                                    ArrayList<Float> arrValue = new ArrayList<>();
                                    // Get all values
                                    for (DataSnapshot snapshot2: snapshot1.getChildren()) {
                                        arrValue.add(Float.parseFloat(snapshot2.getValue().toString()));
                                    }

                                    // Set all values
                                    int index = 1;
                                    for (DataSnapshot snapshot2: snapshot1.getChildren()) {
                                        if (index == 7) {
                                            snapshot2.getRef().setValue(finalAvg);
                                        } else {
                                            float value = arrValue.get(index);
                                            snapshot2.getRef().setValue(value);
                                        }
                                        index += 1;
                                    }
                                }
                            });
                        }
                    }, getTimeDelay());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ///--------------------- Soil Moisture ----------------///
        ArrayList<Integer> arrMoistureValues = new ArrayList<Integer>();
        final Timer[] timerMoisture = {new Timer()};
        refMoisture.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int value = Integer.parseInt(snapshot.getValue().toString());
                progressBarSolidMoisture.setProgress(value);
                moistureStatus.setText(snapshot.getValue().toString() + "%");

                // Set value for recent 7 days
                if (!arrMoistureValues.contains(value)) {
                    arrMoistureValues.add(value);
                }
                timerMoisture[0].cancel();
                timerMoisture[0] = new Timer();
                if (getTimeDelay() > 0) {
                    timerMoisture[0].schedule(new TimerTask() {
                        @Override
                        public void run() {
                            float avg = 0;
                            int sum = 0;
                            for (int index = 0; index<arrMoistureValues.size(); index ++) {
                                sum += arrMoistureValues.get(index);
                            }
                            avg = sum/arrMoistureValues.size();

                            float finalAvg = avg;
                            refMoistureWeek.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    DataSnapshot snapshot1 = task.getResult();
                                    ArrayList<Float> arrValue = new ArrayList<>();
                                    // Get all values
                                    for (DataSnapshot snapshot2: snapshot1.getChildren()) {
                                        arrValue.add(Float.parseFloat(snapshot2.getValue().toString()));
                                    }

                                    // Set all values
                                    int index = 1;
                                    for (DataSnapshot snapshot2: snapshot1.getChildren()) {
                                        if (index == 7) {
                                            snapshot2.getRef().setValue(finalAvg);
                                        } else {
                                            float value = arrValue.get(index);
                                            snapshot2.getRef().setValue(value);
                                        }
                                        index += 1;
                                    }
                                }
                            });
                        }
                    }, getTimeDelay());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Open data view
        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityStatistics();
            }
        });

        // Open setting view
        btnSettingTimePump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivitySettingTime("Pump");
            }
        });

        // Open setting view
        btnSettingTimeLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivitySettingTime("Light");
            }
        });

        // Manual click pump and light
        btnPump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnPump.getText().toString() == "PUMP OFF") {
                    refMotor.setValue(1);
                    changeStateUp(btnPump, "PUMP ON");
                } else {
                    refMotor.setValue(0);
                    changeStateDown(btnPump, "PUMP OFF");
                }
            }
        });

        btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnLight.getText().toString() == "LIGHT OFF") {
                    refLight.setValue(1);
                    changeStateUp(btnLight, "LIGHT ON");
                } else {
                    refLight.setValue(0);
                    changeStateDown(btnLight, "LIGHT OFF");
                }
            }
        });

    }

    /*
        Open activity statistics
      */
    public void openActivityStatistics() {
        Intent intent = new Intent(this, ActivityStatistics.class);
        startActivity(intent);
    }

    /*
        Open activity setting time
     */
    public void openActivitySettingTime(String device) {
        Intent intent = new Intent(this, ActivitySettime.class);
        if (device == "Pump") {
            intent.putExtra("Start", (String) pumpTimeStart.getText());
            intent.putExtra("End", (String) pumpTimeEnd.getText());
        } else if (device == "Light") {
            intent.putExtra("Start", (String) lightTimeStart.getText());
            intent.putExtra("End", (String) lightTimeEnd.getText());
        }

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
    public void autoChange(Button btn, String text, long delay, String key, DatabaseReference ref) {
        if (delay >= 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (key == "up") {
                        changeStateUp(btn, text);
                        ref.setValue(1);
                    } else if (key == "down") {
                        changeStateDown(btn, text);
                        ref.setValue(0);
                    }
                }
            }, delay);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    /*
        Get delay time
     */
    public long getTimeDelay() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        LocalDate currentDate = LocalDate.now();
        String dateNow = simpleDateFormat.format(c.getTime());
        String endDayStr = currentDate + " " + "23:58:00";
        try {
            Date dateNowFormat = (Date)simpleDateFormat.parse(dateNow);
            Date endDay = (Date)simpleDateFormat.parse(endDayStr);

            long getTimeNow = dateNowFormat.getTime();
            long delayUpdate = endDay.getTime() - getTimeNow;

            return  delayUpdate;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  1;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void actionDevice(String time, Button btn, String text, String key ,DatabaseReference ref) {
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void scheduleTomorrow(DatabaseReference refPumpStart, DatabaseReference refPumpEnd, DatabaseReference refLightStart, DatabaseReference refLightEnd) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        LocalDate currentDate = LocalDate.now();
        String dateNow = simpleDateFormat.format(c.getTime());
        String tomorrow = currentDate.plusDays(1) + " " + "01:30:00";
        Date date = (Date)simpleDateFormat.parse(tomorrow);
        Date dateNowFormat = (Date)simpleDateFormat.parse(dateNow);
        long delay = date.getTime() - dateNowFormat.getTime();
        if (delay >= 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    refPumpStart.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            String timeOriginal = dataSnapshot.getValue().toString();
                            refPumpStart.setValue("Waiting.....");
                            refPumpStart.setValue(timeOriginal);
                        }
                    });

                    refPumpEnd.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            String timeOriginal = dataSnapshot.getValue().toString();
                            refPumpEnd.setValue("Waiting.....");
                            refPumpEnd.setValue(timeOriginal);
                        }
                    });

                    refLightStart.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            String timeOriginal = dataSnapshot.getValue().toString();
                            refLightStart.setValue("Waiting.....");
                            refLightStart.setValue(timeOriginal);
                        }
                    });

                    refLightEnd.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            String timeOriginal = dataSnapshot.getValue().toString();
                            refLightEnd.setValue("Waiting.....");
                            refLightEnd.setValue(timeOriginal);
                        }
                    });
                }
            }, delay);
        }

    }
}