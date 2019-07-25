package com.example.mylinechartview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> mDataList = new ArrayList<>();
    List<String> mXCoordinateList = new ArrayList<>();
    LineChartView line_chart_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        line_chart_view = findViewById(R.id.line_chart_view);

        mDataList.add("3");
        mDataList.add("6");
        mDataList.add("2");
        mDataList.add("9");
        mDataList.add("1");
        mDataList.add("7");
        mDataList.add("8");
        mDataList.add("10");

        mXCoordinateList.add("3");
        mXCoordinateList.add("6");
        mXCoordinateList.add("2");
        mXCoordinateList.add("9");
        mXCoordinateList.add("1");
        mXCoordinateList.add("7");
        mXCoordinateList.add("8");
        mXCoordinateList.add("10");

        line_chart_view.setData(mDataList,mXCoordinateList);
    }
}
