package com.example.mqtt_flower;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//        View decorView = getWindow().getDecorView();
//
//        Snackbar.make(decorView, String.valueOf(Maximum), Snackbar.LENGTH_SHORT).show();


public class CurrentTemperature extends AppCompatActivity {
    private String CURRENT_PAGE = "温度";
    private String CURRENT_PAGE_E = "temperature_1";
    private float DISTANCE = 25f;
    private DBOpenHelper dbOpenHelper;
    private SQLiteDatabase db;

    private String[] device;
    public LineChart lineChart;
    LineData WAlineData;
    LineData VAlineData;
    LineData ROClineData;
    ImageView WA ;
    ImageView ROC ;

    TextView symbol;

    private ArrayList<Runnable> runnables;
    private Handler handler;
    private Runnable Update1;
    private Runnable Update2;
    private Runnable Update3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_current_temperature);


//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);



        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        Intent intent = getIntent();
        device = intent.getStringArrayExtra("deviceFlag");
        TextView backImage = findViewById(R.id.back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        device[0] = "device1";
        CURRENT_PAGE = device[2];
        CURRENT_PAGE_E = device[3];
        final TextView title = (TextView)findViewById(R.id.title);
        title.setText(CURRENT_PAGE + "实时曲线");
//        symbol = findViewById(R.id.sylbol);
//        switch (CURRENT_PAGE){
//            case "温度":
//                symbol.setText("℃");
//                break;
//            case "压力":
//                symbol.setText("MPa");
//                break;
//            case "液位":
//                symbol.setText("m");
//                break;
//            case "倾角":
//                symbol.setText("°");
//                break;
//        }
        DB_init();

        lineChart = findViewById(R.id.ChartTest);
        lineChart = initLineChart(lineChart);

//        ROClineData = setROCLinedata();

        lineChart.setData(setWALinedata());
        lineChart.invalidate();

        handler = new Handler();
        runnables = new ArrayList<>();
        Update1 = new Runnable() {
            @Override
            public void run() {
                lineChart.setData(setWALinedata());
                lineChart.invalidate();
                handler.postDelayed(this, 300);
            }
        };

//        Update3 = new Runnable() {
//            @Override
//            public void run() {
//                lineChart.setData(setROCLinedata());
//                lineChart.invalidate();
//                handler.postDelayed(this, 300);
//            }
//        };


        WA = findViewById(R.id.WA);
        ROC = findViewById(R.id.ROC);

        handler.postDelayed(Update1,300);
        runnables.add(Update1);

        WA.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                closeHandlers();
                handler.postDelayed(Update1,300);
                runnables.add(Update1);
                WA.setImageDrawable(getDrawable(R.drawable.btn_date_selected));
                ROC.setImageDrawable(getDrawable(R.drawable.btn_roc_com));
                lineChart.setData(setWALinedata());
                lineChart.invalidate();
            }
        });
        ROC.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {

            }
        });
    }
    private void closeHandlers(){
        for (Runnable runnable:runnables
             ) {
            handler.removeCallbacks(runnable);
            runnables.remove(runnable);
        }
    }
    private void DB_init() {
        dbOpenHelper = new DBOpenHelper(CurrentTemperature.this, "test_db",null,1);;
        db = dbOpenHelper.getWritableDatabase();
    }
    //设置折线图数据
    public LineData setWALinedata(){
        List<Entry> entries = new ArrayList<>();
//        List<Entry> wAverage = new ArrayList<>();
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        ArrayList<Float> data = new ArrayList<>();
        final ArrayList<String> dateArr = new ArrayList<>();

        String sql = String.format("select * from  %s limit 10 offset (select count(*) - 10 from %s)", device[0],device[0]);
        Cursor cursor = db.rawQuery(sql,null);
        if(device[1].equals("1")){
            for(int i = 0; i < 10; i++) {
                cursor.moveToNext();

                String date = cursor.getString(cursor.getColumnIndex("date")).split(" ")[1];
                dateArr.add(date);

                String CurrentTemperature = cursor.getString(cursor.getColumnIndex(CURRENT_PAGE_E ));
                float currentData = Float.parseFloat(CurrentTemperature);
                entries.add(new Entry(i,currentData));
                data.add(currentData);

//                String wACurrentTemperature = cursor.getString(cursor.getColumnIndex("wA" + CURRENT_PAGE_E + "_1"));
//                currentData = Float.parseFloat(wACurrentTemperature);
//                wAverage.add(new Entry(i, currentData));
//                data.add(currentData);
            }
        }
//        if(device[1].equals("2")){
//            for(int i = 0; i < 10; i++) {
//                cursor.moveToNext();
//
//                String date = cursor.getString(cursor.getColumnIndex("date")).split(" ")[1];
//                dateArr.add(date);
//
//                String CurrentTemperature = cursor.getString(cursor.getColumnIndex(CURRENT_PAGE_E + "_2"));
//                float currentData = Float.parseFloat(CurrentTemperature);
//                entries.add(new Entry(i, currentData));
//                data.add(currentData);
//
//                String wACurrentTemperature = cursor.getString(cursor.getColumnIndex("wA" + CURRENT_PAGE_E + "_2"));
//                currentData = Float.parseFloat(wACurrentTemperature);
//                wAverage.add(new Entry(i, currentData));
//                data.add(currentData);
//            }
//        }
        float max = Collections.max(data);
        float min = Collections.min(data);
        float mid = (max + min) / 2;
        float Maximum = mid + DISTANCE;
        float Minimum = mid - DISTANCE;
        float Granularity = 5f;
        while (max > Maximum || min < Minimum + Granularity){
            Granularity += 5f;
            Maximum += 25f;
            Minimum -= 25f;
        }
        Maximum = (float) (Math.ceil(Maximum / Granularity)) * Granularity;
        Minimum = (float) (Math.ceil(Minimum / Granularity)) * Granularity;

        this.lineChart.getAxisLeft().setGranularity(Granularity);
        this.lineChart.getAxisLeft().setAxisMaximum(Maximum);
        this.lineChart.getAxisLeft().setAxisMinimum(Minimum);

        this.lineChart.getXAxis().setLabelRotationAngle(-60);
        this.lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String label;
                switch ((int)value) {
                    case 0:
                        label = dateArr.get(0);
                        break;
                    case 1:
                        label = dateArr.get(1);
                        break;
                    case 2:
                        label = dateArr.get(2);
                        break;
                    case 3:
                        label = dateArr.get(3);
                        break;
                    case 4:
                        label = dateArr.get(4);
                        break;
                    case 5:
                        label = dateArr.get(5);
                        break;
                    case 6:
                        label = dateArr.get(6);
                        break;
                    case 7:
                        label = dateArr.get(7);
                        break;
                    case 8:
                        label = dateArr.get(8);
                        break;
                    case 9:
                        label = dateArr.get(9);
                        break;
                    default:
                        label = "";
                        break;


                }
                return label;
            }
        });


        LineDataSet lineDataSet1 = new LineDataSet(entries, CURRENT_PAGE  + "数据");
//        LineDataSet lineDataSet2 = new LineDataSet(wAverage,device[0] + "加权移动平均滤波数据");
        lineDataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        lineDataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet1.setValueFormatter(new dataFormatter());
//        lineDataSet2.setValueFormatter(new dataFormatter());
//        lineDataSet2.setValueTextSize(9);
        lineDataSet1.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        });
//        lineDataSet2.setColor(Color.RED);
//        lineDataSet2.setCircleColor(Color.RED);
        dataSets.add(lineDataSet1);
//        dataSets.add(lineDataSet2);
        LineData lineData = new LineData(dataSets);

        cursor.close();
        return lineData;
    }


    public LineChart initLineChart(LineChart lineChart){
        lineChart.setDrawGridBackground(false);//设置折线图网格线
        lineChart.setDrawBorders(false);//设置边界
        lineChart.setDescription(null);
        lineChart.setTouchEnabled(false);
        XAxis xAxis = lineChart.getXAxis();
        YAxis yAxisLeft = lineChart.getAxisLeft();
        YAxis yAxisRight = lineChart.getAxisRight();
        setAxis(xAxis,yAxisLeft,yAxisRight);
        return lineChart;
    }

    public void setAxis(XAxis xAxis,YAxis yAxisLeft,YAxis yAxisRight){
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisLineWidth(1);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setEnabled(true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(9);
        xAxis.setLabelCount(12);
        xAxis.setGranularity(1.0f);

        yAxisLeft.setAxisLineWidth(1);
        yAxisLeft.setDrawAxisLine(true);
        yAxisLeft.setDrawGridLines(false);
        yAxisLeft.setEnabled(true);
        yAxisLeft.setLabelCount(11);
//        yAxisLeft.setAxisMinimum(20);
//        yAxisLeft.setAxisMaximum(70);
//        yAxisLeft.setGranularity(5.0f);
        yAxisLeft.setDrawGridLines(true);
        yAxisRight.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        closeHandlers();
        db.close();
        super.onDestroy();
    }
}