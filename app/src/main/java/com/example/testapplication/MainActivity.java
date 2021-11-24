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

        // Receive data from setting view
        if (getIntent().getStringExtra("Device") != null) {
            if (getIntent().getStringExtra("Device").equals("Pump")) {
                pumpTimeStart.setText(getIntent().getStringExtra("Start"));
                pumpTimeEnd.setText(getIntent().getStringExtra("End"));
            } else if (getIntent().getStringExtra("Device").equals("Light")) {
                lightTimeStart.setText(getIntent().getStringExtra("Start"));
                lightTimeEnd.setText(getIntent().getStringExtra("End"));
            }
        }

        try {
            LocalDate currentDate = LocalDate.now();
            Calendar c = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateNow = simpleDateFormat.format(c.getTime());
            String datePumpTime = currentDate + " " + (String) pumpTimeStart.getText();
            String datePumpTimeEnd = currentDate.plusDays(1) + " " + (String) pumpTimeEnd.getText();
            String dateLightTime = currentDate + " " + (String) lightTimeStart.getText();
            String dateLightTimeEnd = currentDate.plusDays(1) + " " + (String) lightTimeEnd.getText();

            // Get 2 date (start-end) each device
            Date datePump = (Date)simpleDateFormat.parse(datePumpTime);
            Date datePumpEnd = (Date)simpleDateFormat.parse(datePumpTimeEnd);
            Date dateLight = (Date)simpleDateFormat.parse(dateLightTime);
            Date dateLightEnd = (Date)simpleDateFormat.parse(dateLightTimeEnd);

            // Compare date
            Date dateNowFormat = (Date)simpleDateFormat.parse(dateNow);
            int comparePump = dateNowFormat.compareTo(datePump);
            int comparePumpEnd = dateNowFormat.compareTo(datePumpEnd);
            int compareLight = dateNowFormat.compareTo(dateLight);
            int compareLightEnd = dateNowFormat.compareTo(dateLightEnd);

            // Delay date
            long getTimeNow = dateNowFormat.getTime();
            long delayStartPump = datePump.getTime() - getTimeNow;
            long delayStartLight = dateLight.getTime() - getTimeNow;
            long delayEndPump = datePumpEnd.getTime() - getTimeNow;
            long delayEndLight = dateLightEnd.getTime() - getTimeNow;

            timer = new Timer();
            // Check time to change state pump and light
            if (delayStartPump > 0) {
                autoChange(btnPump, "PUMP ON", delayStartPump, "up", refMotor);
            }

            if (delayEndPump > 0) {
                autoChange(btnPump, "PUMP OFF", delayEndPump, "down", refMotor);
            }

            if (delayStartLight > 0) {
                autoChange(btnLight, "LIGHT ON", delayStartLight, "up", refLight);
            }

            if (delayEndLight > 0) {
                autoChange(btnLight, "LIGHT OFF", delayEndLight, "down", refLight);
            }

            // Compare time to change state pump and light
            if (comparePump >= 0 && comparePumpEnd < 0) {
                changeStateUp(btnPump, "PUMP ON");
            } else {
                changeStateDown(btnPump, "PUMP OFF");
            }

            if (compareLight >= 0 && compareLightEnd < 0) {
                changeStateUp(btnLight, "LIGHT ON");
            } else  {
                changeStateDown(btnLight, "LIGHT OFF");
            }

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

        } catch (ParseException e) {
            e.printStackTrace();
        }

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    /*
        Get delay time
     */
    public long getTimeDelay() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        LocalDate currentDate = LocalDate.now();
        String dateNow = simpleDateFormat.format(c.getTime());
        String endDayStr = currentDate + " " + "15:33:10";
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

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public long myFunc(Button btn, DatabaseReference ref) {
//        LocalDate currentDate = LocalDate.now();
//        Calendar c = Calendar.getInstance();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String dateNow = simpleDateFormat.format(c.getTime());
//        String datePumpTime = currentDate + " " + (String) pumpTimeStart.getText();
//        String datePumpTimeEnd = currentDate.plusDays(1) + " " + (String) pumpTimeEnd.getText();
//        String dateLightTime = currentDate + " " + (String) lightTimeStart.getText();
//        String dateLightTimeEnd = currentDate.plusDays(1) + " " + (String) lightTimeEnd.getText();
//
//        try {
//            // Get 2 date (start-end) each device
//            Date datePump = (Date)simpleDateFormat.parse(datePumpTime);
//            Date datePumpEnd = (Date)simpleDateFormat.parse(datePumpTimeEnd);
//            Date dateLight = (Date)simpleDateFormat.parse(dateLightTime);
//            Date dateLightEnd = (Date)simpleDateFormat.parse(dateLightTimeEnd);
//
//            // Compare date
//            Date dateNowFormat = (Date)simpleDateFormat.parse(dateNow);
//            int comparePump = dateNowFormat.compareTo(datePump);
//            int comparePumpEnd = dateNowFormat.compareTo(datePumpEnd);
//            int compareLight = dateNowFormat.compareTo(dateLight);
//            int compareLightEnd = dateNowFormat.compareTo(dateLightEnd);
//
//            // Delay date
//            long getTimeNow = dateNowFormat.getTime();
//            long delayStartPump = datePump.getTime() - getTimeNow;
//            long delayStartLight = dateLight.getTime() - getTimeNow;
//            long delayEndPump = datePumpEnd.getTime() - getTimeNow;
//            long delayEndLight = dateLightEnd.getTime() - getTimeNow;
//
//            if (delayStartLight > 0) {
//                autoChange(btn, "LIGHT ON", delayStartLight, "up", ref);
//            }
//
//            if (delayEndLight > 0) {
//                autoChange(btn, "LIGHT OFF", delayEndLight, "down", ref);
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        return 1;
//    }
}