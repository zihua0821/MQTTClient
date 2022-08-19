package com.example.mqtt_flower;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class device extends AppCompatActivity {

    private RecyclerView recyclerView;
    private deviceItemAdapter deviceItemAdapter;
    private ArrayList<InfoRootBean> items;
    int flushFlag = 0;
    String[] can = {""};
    AlertDialog alertDialog;
    AlertDialog d;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device);

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);



        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog = new AlertDialog.Builder(device.this)
                        .setTitle("可添加的设备")
                        .setItems(can, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                InfoRootBean item = new InfoRootBean();
                                item.Iname = can[which];
                                item.Iinfo = "汽车罐车（半挂车）";
                                items.add(item);
                                deviceItemAdapter.notifyDataSetChanged();
                            }
                        })
                        .setPositiveButton("刷新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create();
                alertDialog.show();
                if(alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)!=null) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            flushFlag++;
//                            if (flushFlag == 2){
//                                alertDialog.dismiss();
//                                can[0] = "device2";
//                                d = new AlertDialog.Builder(device.this)
//                                        .setTitle("可添加的设备")
//                                        .setItems(can, new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                InfoRootBean item = new InfoRootBean();
//                                                item.Iname = can[which];
//                                                item.Iinfo = "汽车罐车（半挂车）";
//                                                items.add(item);
//                                                deviceItemAdapter.notifyDataSetChanged();
//                                            }
//                                        })
//                                        .setPositiveButton("刷新", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                            }
//                                        })
//                                        .create();
//                                d.show();
//                            }
                        }
                    });
                }

            }
        });




        items = new ArrayList<>();
        InfoRootBean item1,item2;
        item1 = new InfoRootBean();
        item2 = new InfoRootBean();
        item1.Iname = "device1";
        item1.Iinfo = "汽车罐车（半挂车）";
//        item2.Iname = "device2";
//        item2.Iinfo = "汽车罐车（半挂车）";
        items.add(item1);
//        items.add(item2);
        recyclerView = findViewById(R.id.recyclerView);
        deviceItemAdapter = new deviceItemAdapter(this,items);
        recyclerView.setAdapter(deviceItemAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);


        startService(new Intent(this,DataService.class));

    }

}