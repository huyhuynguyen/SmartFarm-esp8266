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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class ActivityStatistics extends AppCompatActivity {

    private LineChart mChart;
    private LineChart moistureChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mChart = (LineChart) findViewById(R.id.linechar);
        moistureChart = (LineChart) findViewById(R.id.linechar2);
        DatabaseReference refHumidity = FirebaseDatabase.getInstance().getReference("DoAmWeek");
        DatabaseReference refTemperature = FirebaseDatabase.getInstance().getReference("NhietDoWeek");
        DatabaseReference refMoisture = FirebaseDatabase.getInstance().getReference("DoAmDatWeek");

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);

        moistureChart.setDragEnabled(true);
        moistureChart.setScaleEnabled(false);

        mChart.setNoDataText("Click to see the chart");
        // Line 1
        refHumidity.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot snapshot1 = task.getResult();
                float x = (float) 0.0;
                ArrayList<Entry> yValues = new ArrayList<>();
                for (DataSnapshot snapshot2: snapshot1.getChildren()) {
                    String value = snapshot2.getValue().toString();
                    float y = Float.parseFloat(value);
                    yValues.add(new Entry(x, y));
                    x += 1;
                }

                LineDataSet set1 = new LineDataSet(yValues, "Humidity");
                set1.setFillAlpha(110);
                set1.setColor(Color.CYAN);
                set1.setLineWidth(3f);
                set1.setValueTextColor(Color.BLACK);
                set1.setValueTextSize(12);

                // Line 2
                refTemperature.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        DataSnapshot snapshot1 = task.getResult();
                        float x = (float) 0.0;
                        ArrayList<Entry> yValues2 = new ArrayList<>();
                        for (DataSnapshot snapshot2: snapshot1.getChildren()) {
                            String value = snapshot2.getValue().toString();
                            float y = Float.parseFloat(value);
                            yValues2.add(new Entry(x, y));
                            x += 1;
                        }
                        LineDataSet set2 = new LineDataSet(yValues2, "Temperature");
                        set2.setFillAlpha(110);
                        set2.setColor(Color.RED);
                        set2.setLineWidth(2f);
                        set2.setValueTextColor(Color.RED);
                        set2.setValueTextSize(12);

                        LineData data = new LineData(set1, set2);
                        mChart.setData(data);

                    }
                });
            }
        });

        // Line 3
        moistureChart.setNoDataText("Click to see the chart");
        refMoisture.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot snapshot1 = task.getResult();
                float x = (float) 0.0;
                ArrayList<Entry> moistureValue = new ArrayList<>();
                for (DataSnapshot snapshot2: snapshot1.getChildren()) {
                    String value = snapshot2.getValue().toString();
                    float y = Float.parseFloat(value);
                    moistureValue.add(new Entry(x, y));
                    x += 1;
                }

                Log.d("x = ", String.valueOf(x));
                LineDataSet setMoisture = new LineDataSet(moistureValue, "Soil moisture");
                setMoisture.setFillAlpha(110);
                setMoisture.setColor(Color.GREEN);
                setMoisture.setLineWidth(2f);
                setMoisture.setValueTextColor(Color.BLACK);
                setMoisture.setValueTextSize(14);

                LineData lineDataMoistureData = new LineData(setMoisture);
                moistureChart.setData(lineDataMoistureData);
            }
        });
    }
}