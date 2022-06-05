package com.example.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ActivityStatistics extends AppCompatActivity {

    private LineChart mChart;
    private LineChart moistureChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mChart = (LineChart) findViewById(R.id.linechar);
        moistureChart = (LineChart) findViewById(R.id.linechar2);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final Task<QuerySnapshot> logRef = db.collection("logs").get();
        List<Map<String, String>> tempList = new ArrayList<>();
        List<Map<String, String>> humiList = new ArrayList<>();
        List<Map<String, String>> moisureList = new ArrayList<>();
        List<Map<String, String>> lightList = new ArrayList<>();

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);

        moistureChart.setDragEnabled(true);
        moistureChart.setScaleEnabled(false);

        mChart.setNoDataText("Click to see the chart");
        logRef.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, String> myMap = new HashMap<String, String>();
                        switch (document.getData().get("sensor").toString()) {
                            case "Temperature":
                                myMap.put("date", document.getData().get("date").toString());
                                myMap.put("value", document.getData().get("value").toString());
                                tempList.add(myMap);
                                break;
                            case "Humidity":
                                myMap.put("date", document.getData().get("date").toString());
                                myMap.put("value", document.getData().get("value").toString());
                                humiList.add(myMap);
                                break;
                            case "Moisture":
                                myMap.put("date", document.getData().get("date").toString());
                                myMap.put("value", document.getData().get("value").toString());
                                moisureList.add(myMap);
                                break;
                            case "Light":
                                myMap.put("date", document.getData().get("date").toString());
                                myMap.put("value", document.getData().get("value").toString());
                                lightList.add(myMap);
                                break;
                        }
                    }

                    sortList(tempList);
                    sortList(humiList);
                    sortList(moisureList);
                    sortList(lightList);

                    // Line chart 1
                    LineDataSet set1 = lineSetStructure(humiList, "Humidity", Color.CYAN, Color.BLACK);
                    LineDataSet set2 = lineSetStructure(tempList, "Temperature" ,Color.RED, Color.RED);

                    LineData data = new LineData(set1, set2);
                    mChart.setData(data);

                    // Line chart 2
                    LineDataSet set3 = lineSetStructure(moisureList, "Soil moisture", Color.GREEN, Color.BLACK);
                    LineDataSet set4 = lineSetStructure(lightList, "Light", Color.BLUE, Color.BLACK);
                    LineData data2 = new LineData(set3, set4);
                    moistureChart.setData(data2);


                    // retrieve
//                    for (int i = 0; i < tempList.size(); i++) {
//                        System.out.println(i);
//                        System.out.println("my date is: "+tempList.get(i).get("date"));
//                        System.out.println("my value is: "+tempList.get(i).get("value"));
//                        Float value = Float.parseFloat(tempList.get(i).get("value"));
//                    }


                } else {
                    Log.d("Error", "Error getting documents: ", task.getException());
                }
            }
        });

        // Line 3
        moistureChart.setNoDataText("Click to see the chart");
    }

    public LineDataSet lineSetStructure(List<Map<String, String>> list, String label, int colorLine, int colorTextValue) {
        float x = (float) 0.0;
        ArrayList<Entry> yValues = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            float y = Float.parseFloat(list.get(i).get("value"));
            y = (float) Math.round(y);
            yValues.add(new Entry(x, y));
            x += 1;
        }

        LineDataSet set1 = new LineDataSet(yValues, label);
        set1.setFillAlpha(110);
        set1.setColor(colorLine);
        set1.setLineWidth(3f);
        set1.setValueTextColor(colorTextValue);
        set1.setValueTextSize(12);

        return set1;
    }

    public void sortList(List<Map<String, String>> list) {
        Collections.sort(list, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    Date dateO1 = utcFormat.parse(o1.get("date"));
                    Date dateO2 = utcFormat.parse(o2.get("date"));

                    return dateO2.compareTo(dateO1);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        });
    }
}