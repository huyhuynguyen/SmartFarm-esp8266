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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBarSolidMoisture, progressBarTemperature, progressBarHumidity, progressBarLight;
    private TextView moistureStatus, temperatureStatus, humidityStatus, lightStatus, textView6, tvDevicesState;
    private ImageView imgMoisture;
    private Button btnData, button, btnControlDevice;
    private int maxMoistureProgress = 1024;

    Timer timer;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBarSolidMoisture = (ProgressBar) findViewById(R.id.progressBarSoilMoisture);
        progressBarTemperature = (ProgressBar) findViewById(R.id.progressBarTemperature);
        progressBarHumidity = (ProgressBar) findViewById(R.id.progressBarHumidity);
        progressBarLight = (ProgressBar) findViewById(R.id.progressBarLight);
        moistureStatus = (TextView) findViewById(R.id.moisture);
        temperatureStatus = (TextView) findViewById(R.id.temperature);
        humidityStatus = (TextView) findViewById(R.id.humidity);
        lightStatus = (TextView) findViewById(R.id.light);
        imgMoisture = (ImageView) findViewById(R.id.imgMoisture);
        btnData = (Button) findViewById(R.id.btnData);
        textView6 = (TextView) findViewById(R.id.textView6);
        tvDevicesState = (TextView) findViewById(R.id.tvDevicesState);
        btnControlDevice = (Button) findViewById(R.id.btnControlDevice);

        DatabaseReference refTimePumpStart = FirebaseDatabase.getInstance().getReference("timeStartPump");
        DatabaseReference refTimePumpEnd = FirebaseDatabase.getInstance().getReference("timeEndPump");
        DatabaseReference refTimeLightStart = FirebaseDatabase.getInstance().getReference("timeStartLight");
        DatabaseReference refTimeLightEnd = FirebaseDatabase.getInstance().getReference("timeEndLight");

        ///--------------------- Firestore ----------------///
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // real time firebase
        DatabaseReference refMoistureWeek = FirebaseDatabase.getInstance().getReference("DoAmDatWeek");
        DatabaseReference refTemperatureWeek = FirebaseDatabase.getInstance().getReference("NhietDoWeek");
        DatabaseReference refHumidityWeek = FirebaseDatabase.getInstance().getReference("DoAmWeek");

        // firestore database
        final DocumentReference deviceRef = db.collection("devices").document("esp");
        final DocumentReference humiRef = db.collection("sensors").document("humi");
        final DocumentReference tempRef = db.collection("sensors").document("temp");
        final DocumentReference lightRef = db.collection("sensors").document("li");
        final DocumentReference moistureRef = db.collection("sensors").document("moisture");

        /// -------------------- State connection device --------------- ///
        deviceRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String value = snapshot.getData().get("active").toString();
                    tvDevicesState.setText(value.equals("true") ? "Connected" : "Disconnected");
                } else {
                    Log.d("null data", "Current data: null");
                }
            }
        });

        ///--------------------- Humidity ----------------///
        ArrayList<Integer> arrHumidityValues = new ArrayList<Integer>();
        final Timer[] timerHumidity = {new Timer()};
        humiRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String value = snapshot.getData().get("value").toString();
                    Float TemperatureBar = Float.parseFloat(value);
                    int humi = Math.round(TemperatureBar);
                    progressBarHumidity.setProgress(humi);
                    humidityStatus.setText(humi + "%");

                    // Set value for recent 7 days
                    if (!arrHumidityValues.contains(humi)) {
                        arrHumidityValues.add(humi);
                    }

                    for (int index = 0; index<arrHumidityValues.size(); index ++) {
                        Log.i("Arr Humi", String.valueOf(arrHumidityValues.get(index)));
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
//                                refHumidityWeek.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
//                                        DataSnapshot snapshot1 = task.getResult();
//                                        ArrayList<Float> arrValue = new ArrayList<>();
//                                        // Get all values
//                                        for (DataSnapshot snapshot2: snapshot1.getChildren()) {
//                                            arrValue.add(Float.parseFloat(snapshot2.getValue().toString()));
//                                        }
//
//                                        // Set all values
//                                        int index = 1;
//                                        for (DataSnapshot snapshot2: snapshot1.getChildren()) {
//                                            if (index == 7) {
//                                                snapshot2.getRef().setValue(finalAvg);
//                                            } else {
//                                                float value = arrValue.get(index);
//                                                snapshot2.getRef().setValue(value);
//                                            }
//                                            index += 1;
//                                        }
//                                    }
//                                });
                            }
                        }, getTimeDelay());
                    }
                } else {
                    Log.d("null data", "Current data: null");
                }
            }
        });

        ///--------------------- Temperature ----------------///
        ArrayList<Float> arrTemperatureValues = new ArrayList<Float>();
        final Timer[] timerTemperature = {new Timer()};
        tempRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String value = snapshot.getData().get("value").toString();
                    Float TemperatureBar = Float.parseFloat(value);
                    int temp = Math.round(TemperatureBar);
                    progressBarTemperature.setProgress(temp);
                    temperatureStatus.setText(temp + "°" + "C");

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
//                                refTemperatureWeek.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
//                                        DataSnapshot snapshot1 = task.getResult();
//                                        ArrayList<Float> arrValue = new ArrayList<>();
//                                        // Get all values
//                                        for (DataSnapshot snapshot2: snapshot1.getChildren()) {
//                                            arrValue.add(Float.parseFloat(snapshot2.getValue().toString()));
//                                        }
//
//                                        // Set all values
//                                        int index = 1;
//                                        for (DataSnapshot snapshot2: snapshot1.getChildren()) {
//                                            if (index == 7) {
//                                                snapshot2.getRef().setValue(finalAvg);
//                                            } else {
//                                                float value = arrValue.get(index);
//                                                snapshot2.getRef().setValue(value);
//                                            }
//                                            index += 1;
//                                        }
//                                    }
//                                });
                            }
                        }, getTimeDelay());
                    }
                } else {
                    Log.d("null data", "Current data: null");
                }
            }
        });

        ///--------------------- Soil Moisture ----------------///
        ArrayList<Integer> arrMoistureValues = new ArrayList<Integer>();
        final Timer[] timerMoisture = {new Timer()};
        moistureRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String value = snapshot.getData().get("value").toString();
                    Float valueFloat = Float.parseFloat(value);
                    int soil_moisture = Math.round(valueFloat);
                    progressBarSolidMoisture.setProgress(soil_moisture);
                    moistureStatus.setText(soil_moisture + "%");

                    // Set value for recent 7 days
                    if (!arrMoistureValues.contains(soil_moisture)) {
                        arrMoistureValues.add(soil_moisture);
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
//                                refMoistureWeek.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
//                                        DataSnapshot snapshot1 = task.getResult();
//                                        ArrayList<Float> arrValue = new ArrayList<>();
//                                        // Get all values
//                                        for (DataSnapshot snapshot2: snapshot1.getChildren()) {
//                                            arrValue.add(Float.parseFloat(snapshot2.getValue().toString()));
//                                        }
//
//                                        // Set all values
//                                        int index = 1;
//                                        for (DataSnapshot snapshot2: snapshot1.getChildren()) {
//                                            if (index == 7) {
//                                                snapshot2.getRef().setValue(finalAvg);
//                                            } else {
//                                                float value = arrValue.get(index);
//                                                snapshot2.getRef().setValue(value);
//                                            }
//                                            index += 1;
//                                        }
//                                    }
//                                });
                            }
                        }, getTimeDelay());
                    }
                } else {
                    Log.d("null data", "Current data: null");
                }
            }
        });

        ///--------------------- Light ----------------///
        ArrayList<Integer> arrLightValues = new ArrayList<Integer>();
        final Timer[] timerLight = {new Timer()};
        lightRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String value = snapshot.getData().get("value").toString();
                    Float valueFloat = Float.parseFloat(value);
                    int light = Math.round(valueFloat);
                    progressBarLight.setProgress(light);
                    lightStatus.setText(light + "lux");

                    // Set value for recent 7 days
                    if (!arrLightValues.contains(light)) {
                        arrLightValues.add(light);
                    }
                    timerLight[0].cancel();
                    timerLight[0] = new Timer();
                    if (getTimeDelay() > 0) {
                        timerLight[0].schedule(new TimerTask() {
                            @Override
                            public void run() {
                                float avg = 0;
                                int sum = 0;
                                for (int index = 0; index<arrLightValues.size(); index ++) {
                                    sum += arrLightValues.get(index);
                                }
                                avg = sum/arrLightValues.size();

                                float finalAvg = avg;
//                                refMoistureWeek.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
//                                        DataSnapshot snapshot1 = task.getResult();
//                                        ArrayList<Float> arrValue = new ArrayList<>();
//                                        // Get all values
//                                        for (DataSnapshot snapshot2: snapshot1.getChildren()) {
//                                            arrValue.add(Float.parseFloat(snapshot2.getValue().toString()));
//                                        }
//
//                                        // Set all values
//                                        int index = 1;
//                                        for (DataSnapshot snapshot2: snapshot1.getChildren()) {
//                                            if (index == 7) {
//                                                snapshot2.getRef().setValue(finalAvg);
//                                            } else {
//                                                float value = arrValue.get(index);
//                                                snapshot2.getRef().setValue(value);
//                                            }
//                                            index += 1;
//                                        }
//                                    }
//                                });
                            }
                        }, getTimeDelay());
                    }
                } else {
                    Log.d("null data", "Current data: null");
                }
            }
        });

        // Open data view
        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityStatistics();
            }
        });

        /* -------------------- Control Device ----------------  */
        btnControlDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityControlDevice();
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
        Open activity control device
      */
    public void openActivityControlDevice() {
        Intent intent = new Intent(this, MainActivityControllDevice.class);
        startActivity(intent);
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

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void scheduleTomorrow(DatabaseReference refPumpStart, DatabaseReference refPumpEnd, DatabaseReference refLightStart, DatabaseReference refLightEnd) throws ParseException {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Calendar c = Calendar.getInstance();
//        LocalDate currentDate = LocalDate.now();
//        String dateNow = simpleDateFormat.format(c.getTime());
//        String tomorrow = currentDate.plusDays(1) + " " + "01:30:00";
//        Date date = (Date)simpleDateFormat.parse(tomorrow);
//        Date dateNowFormat = (Date)simpleDateFormat.parse(dateNow);
//        long delay = date.getTime() - dateNowFormat.getTime();
//        if (delay >= 0) {
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    refPumpStart.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//                        @Override
//                        public void onSuccess(DataSnapshot dataSnapshot) {
//                            String timeOriginal = dataSnapshot.getValue().toString();
//                            refPumpStart.setValue("Waiting.....");
//                            refPumpStart.setValue(timeOriginal);
//                        }
//                    });
//
//                    refPumpEnd.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//                        @Override
//                        public void onSuccess(DataSnapshot dataSnapshot) {
//                            String timeOriginal = dataSnapshot.getValue().toString();
//                            refPumpEnd.setValue("Waiting.....");
//                            refPumpEnd.setValue(timeOriginal);
//                        }
//                    });
//
//                    refLightStart.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//                        @Override
//                        public void onSuccess(DataSnapshot dataSnapshot) {
//                            String timeOriginal = dataSnapshot.getValue().toString();
//                            refLightStart.setValue("Waiting.....");
//                            refLightStart.setValue(timeOriginal);
//                        }
//                    });
//
//                    refLightEnd.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//                        @Override
//                        public void onSuccess(DataSnapshot dataSnapshot) {
//                            String timeOriginal = dataSnapshot.getValue().toString();
//                            refLightEnd.setValue("Waiting.....");
//                            refLightEnd.setValue(timeOriginal);
//                        }
//                    });
//                }
//            }, delay);
//        }
//
//    }
}