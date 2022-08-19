package com.example.mqtt_flower;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.alibaba.fastjson.JSON;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;

import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private DBOpenHelper dbOpenHelper;
    private SQLiteDatabase db;
    private Handler updateHandler;
    private Runnable update;

    private TextView Text_CurrentTemperature ,Text_Humidity ,
            Text_water_bump,Text_air_bump,Text_WindLevel,Text_shield;


    private LocationClient mLocationClient;
    private MapView mMapView;
    private BaiduMap mBaiduMap = null;

    private String[] deviceFlag = {"device1","1","温度","CurrentTemperature"};
    HashMap<String,ImageView> imageViewHashMap = new HashMap<>();
    HashMap<String,Double> datas = new HashMap<>();
    HashMap<String, Double> TV = new HashMap<String,Double>(){{
        put("temperature_1",50.0);
        put("temperature_2",50.0);
        put("pressure_1",0.5);
        put("pressure_2",0.5);
        put("liquid_1",0.2);
        put("liquid_2",0.2);
        put("inclination_1",30.0);
        put("inclination_2",30.0);
    }};
    HashMap<String, Integer> comId = new HashMap<String,Integer>(){{
        put("temperature_1",R.drawable.temperature_com);
        put("temperature_2",R.drawable.temperature_com);
        put("pressure_1",R.drawable.pressure_com);
        put("pressure_2",R.drawable.pressure_com);
        put("liquid_1",R.drawable.vibration_com);
        put("liquid_2",R.drawable.vibration_com);
        put("inclination_1",R.drawable.inclination_com);
        put("inclination_2",R.drawable.inclination_com);
    }};
    HashMap<String, Integer> redId = new HashMap<String,Integer>(){{
        put("temperature_1",R.drawable.temperature_red);
        put("temperature_2",R.drawable.temperature_red);
        put("pressure_1",R.drawable.pressure_red);
        put("pressure_2",R.drawable.pressure_red);
        put("liquid_1",R.drawable.vibration_red);
        put("liquid_2",R.drawable.vibration_red);
        put("inclination_1",R.drawable.inclination_red);
        put("inclination_2",R.drawable.inclination_red);
    }};


    TextView title;
    public MainActivity() {
    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        deviceFlag[0] = intent.getStringExtra("device");
        setTitle(deviceFlag[0]);
//        title = findViewById(R.id.dname);
//        title.setText(deviceFlag[0]);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED //锁屏状态下显示
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD //解锁
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON //保持屏幕长亮
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); //打开屏幕c
        if(!EasyPermissions.hasPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS})){
//            EasyPermissions.requestPermissions(MainActivity.this,"短信",1,new String[]{Manifest.permission.SEND_SMS});
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.SEND_SMS}, 1);
        }
//        View decorView = getWindow().getDecorView();
//        Snackbar.make(decorView, String.valueOf(EasyPermissions.hasPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS})), Snackbar.LENGTH_SHORT).show();

        final String infoMsg = "" +
                "设备种类：移动式压力容器\n" +
                "设备类别：汽车罐车（半挂车）\n" +
                "使用单位：湖北××××物流有限责任公司\n" +
                "单位内编号：2021-01-20\n" +
                "设备代码：湖北省1\n" +
                "登记机关：dengji0413\n" +
                "检验机构：jianyan0413\n" +
                "登记证编号：" + deviceFlag[0] + "\n" +
                "下次检验日期：2023-11-30\n" +
                "";
        TextView info = findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("设备详细信息")
                        .setMessage(infoMsg)
                        .create()
                        ;
                alertDialog.show();

            }
        });

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //-----------温度页面-----------
        ImageView CurrentTemperature_1 = findViewById(R.id.Humidity);
        CurrentTemperature_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CurrentTemperature.class);
                deviceFlag[1] = "1";
                deviceFlag[2] = "湿度";
                deviceFlag[3] = "Humidity";
                intent.putExtra("deviceFlag",deviceFlag);
                startActivity(intent);
            }
        });
        //------------温度页面----------

        //-----------压强页面-----------
        ImageView image_Pressure_1 = findViewById(R.id.WindLevel);
        image_Pressure_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CurrentTemperature.class);
                deviceFlag[1] = "1";
                deviceFlag[2] = "风力";
                deviceFlag[3] = "WindLevel";
                intent.putExtra("deviceFlag",deviceFlag);
                startActivity(intent);
            }
        });
        //------------压强页面----------

        //-----------震动页面-----------
        ImageView image_Vibration_1 = findViewById(R.id.shield);
        image_Vibration_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CurrentTemperature.class);
                deviceFlag[1] = "1";
                deviceFlag[2] = "护罩";
                deviceFlag[3] = "shield";
                intent.putExtra("deviceFlag",deviceFlag);
                startActivity(intent);
            }
        });
        //------------震动页面----------

        //-----------角度页面-----------
        ImageView image_Inclination_1 = findViewById(R.id.CurrentTemperature);
        image_Inclination_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CurrentTemperature.class);
                deviceFlag[1] = "1";
                deviceFlag[2] = "温度";
                deviceFlag[3] = "CurrentTemperature";
                intent.putExtra("deviceFlag",deviceFlag);
                startActivity(intent);
            }
        });

        ImageView image_water_bump = findViewById(R.id.water_bump);
        image_water_bump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CurrentTemperature.class);
                deviceFlag[1] = "1";
                deviceFlag[2] = "水泵";
                deviceFlag[3] = "water_bump";
                intent.putExtra("deviceFlag",deviceFlag);
                startActivity(intent);
            }
        });
        ImageView image_air_bump = findViewById(R.id.air_bump);
        image_air_bump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CurrentTemperature.class);
                deviceFlag[1] = "1";
                deviceFlag[2] = "气泵";
                deviceFlag[3] = "air_bump";
                intent.putExtra("deviceFlag",deviceFlag);
                startActivity(intent);
            }
        });

//
//
//        imageViewHashMap.put("temperature_1",CurrentTemperature_1);
//        imageViewHashMap.put("temperature_2",CurrentTemperature_2);
//        imageViewHashMap.put("pressure_1",image_Pressure_1);
//        imageViewHashMap.put("pressure_2",image_Pressure_2);
//        imageViewHashMap.put("liquid_1",image_Vibration_1);
//        imageViewHashMap.put("liquid_2",image_Vibration_2);
//        imageViewHashMap.put("inclination_1",image_Inclination_1);
//        imageViewHashMap.put("inclination_2",image_Inclination_2);



        //------------角度页面----------

        //地理定位
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);

        mBaiduMap.setMyLocationEnabled(true);

        List<String> permissionList = new ArrayList<String>();

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            requestLocation();
        }

        ui_init();//变量初始化，规范化写法
        DB_init();
        setData();
        updateHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                setData();
//                changeIcon();
            }
        };
        update = new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(300);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    updateHandler.sendMessage(updateHandler.obtainMessage());
                }
            }
        };
        new Thread(update).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //填充选项菜单（读取XML文件、解析、加载到Menu组件上）
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.caidan, menu);
        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                //这里修改为你想修改的颜色
                drawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            }
        }

        return true;
    }

    //重写OptionsItemSelected(MenuItem item)来响应菜单项(MenuItem)的点击事件（根据id来区分是哪个item）
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
                 // Handle action bar item clicks here. The action bar will
                 // automatically handle clicks on the Home/Up button, so long
                 // as you specify a parent activity in AndroidManifest.xml.
                 switch (item.getItemId()) {
                     case R.id.start:
                         deviceFlag[0] = "device1";
                         MainActivity.this.setTitle(R.string.device1);
                         break;
                     case R.id.over:
                         deviceFlag[0] = "device2";
                         MainActivity.this.setTitle(R.string.device2);
                         break;

                     default:
                         break;
                     }
                 return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }





    private void ui_init() {
        Text_CurrentTemperature = findViewById(R.id.Text_CurrentTemperature);
        Text_Humidity = findViewById(R.id.Text_Humidity);

        Text_water_bump = findViewById(R.id.Text_water_bump);
        Text_air_bump = findViewById(R.id.Text_air_bump);

        Text_WindLevel = findViewById(R.id.Text_WindLevel);
        Text_shield = findViewById(R.id.Text_shield);
    }

    private class MyLocationListener extends BDAbstractLocationListener{

        @Override
        public void onReceiveLocation(BDLocation location) {
//            navigateTo(location);
            /*
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
            currentPosition.append("经度：").append(location.getLongitude()).append("\n");
            currentPosition.append("国家：").append(location.getCountry()).append("\n");
            currentPosition.append("省：").append(location.getProvince()).append("\n");
            currentPosition.append("市：").append(location.getCity()).append("\n");
            currentPosition.append("区：").append(location.getDistrict()).append("\n");
            currentPosition.append("村镇：").append(location.getTown()).append("\n");
            currentPosition.append("街道：").append(location.getStreet()).append("\n");
            currentPosition.append("地址：").append(location.getAddrStr()).append("\n");
            currentPosition.append("定位方式：");
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络");
            }
            locationInfo.setText(currentPosition);

             */
        }
    }


    private void navigateTo(double la,double lo){

        LatLng ll = new LatLng(la,lo);
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        mBaiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(16f);
        mBaiduMap.animateMapStatus(update);

        MyLocationData.Builder locationBulider = new MyLocationData.Builder();
        locationBulider.latitude(la);
        locationBulider.longitude(lo);
        MyLocationData locationData = locationBulider.build();
        mBaiduMap.setMyLocationData(locationData);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for(int result :grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有的权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精度

        option.setCoorType("bd0911");//百度经纬度坐标

        option.setScanSpan(1000);//定位请求间隔

        option.setOpenGps(true);

        option.setLocationNotify(true);

        option.setIgnoreKillProcess(false);

        option.SetIgnoreCacheException(false);

        option.setWifiCacheTimeOut(5 * 60 * 1000);

        option.setEnableSimulateGps(false);

        option.setIsNeedAddress(true);

        mLocationClient.setLocOption(option);
    }
    private void DB_init() {
        dbOpenHelper = new DBOpenHelper(this, "test_db",null,1);;
        db = dbOpenHelper.getWritableDatabase();
    }

    public void call(String phoneNumber){
        String action = Intent.ACTION_CALL;
        Intent intent = new Intent(action);
        //携带数据
        String number = phoneNumber;
        intent.setData(Uri.parse("tel:" + number));
        //start
        startActivity(intent);
    }
    private void setData(){
        String sql = String.format("select * from  %s limit 1 offset (select count(*) - 1 from %s)","device1","device1");
        Cursor cursor = db.rawQuery(sql,null);
        cursor.moveToNext();
        Double longitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex("longitude")));
        Double latitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex("latitude")));
        navigateTo(latitude,longitude);

//        datas.put("temperature_1",Double.parseDouble(cursor.getString(cursor.getColumnIndex("temperature_1"))));
//        datas.put("temperature_2",Double.parseDouble(cursor.getString(cursor.getColumnIndex("temperature_2"))));
//        datas.put("pressure_1",Double.parseDouble(cursor.getString(cursor.getColumnIndex("pressure_1"))));
//        datas.put("pressure_2",Double.parseDouble(cursor.getString(cursor.getColumnIndex("pressure_2"))));
//        datas.put("liquid_1",Double.parseDouble(cursor.getString(cursor.getColumnIndex("liquid_1"))));
//        datas.put("liquid_2",Double.parseDouble(cursor.getString(cursor.getColumnIndex("liquid_2"))));
//        datas.put("inclination_1",Double.parseDouble(cursor.getString(cursor.getColumnIndex("inclination_1"))));
//        datas.put("inclination_2",Double.parseDouble(cursor.getString(cursor.getColumnIndex("inclination_2"))));

        String valtemperature_1 = "湿度:" + cursor.getString(cursor.getColumnIndex("Humidity")) + "%RH";
        String valtemperature_2 = "风力:" + cursor.getString(cursor.getColumnIndex("WindLevel")) + "m/s";
        String valpressure_1 = "护罩:" + cursor.getString(cursor.getColumnIndex("shield"));
        String valpressure_2 = "温度:" + cursor.getString(cursor.getColumnIndex("CurrentTemperature")) + "℃";
        String valliquid_1 = "水泵:" + cursor.getString(cursor.getColumnIndex("water_bump"));
        String valliquid_2 = "气泵:" + cursor.getString(cursor.getColumnIndex("air_bump"));
        Text_CurrentTemperature.setText(valpressure_2);
        Text_Humidity .setText(valtemperature_1);
        Text_water_bump.setText(valliquid_1);
        Text_air_bump.setText(valliquid_2);
        Text_WindLevel.setText(valtemperature_2);
        Text_shield.setText(valpressure_1);

        cursor.close();
    }
    private void changeIcon(){
        for (Map.Entry<String,Double> entry:datas.entrySet()
             ) {
            if (Math.abs(entry.getValue()) > TV.get(entry.getKey())){
                imageViewHashMap.get(entry.getKey()).setImageResource(redId.get(entry.getKey()));
            }else {
                imageViewHashMap.get(entry.getKey()).setImageResource(comId.get(entry.getKey()));
            }
        }
    }

}
