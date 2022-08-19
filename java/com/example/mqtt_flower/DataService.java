package com.example.mqtt_flower;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class DataService extends Service {
    public static final String TAG = "zihua";

    private float[] w = {0.2f,0.3f,0.5f};
    private static final String PHONENUMBER = "17796537883";
    private final String host = "tcp://iot-06z00ggskvigpa0.mqtt.iothub.aliyuncs.com";
    private final String userName = "APP1&gkihdSucJqN";
    private final String passWord = "d4f6b6dab0504b34b0aa1a14af8835e2da62ab5c2519e8acb87fa41bf26ad86a";
    private final String mqtt_id = "gkihdSucJqN.APP1|securemode=2,signmethod=hmacsha256,timestamp=2524608000000|";
    private DBOpenHelper dbOpenHelper;
    private SQLiteDatabase db;
    private Double longitude = 114.43979;//纬
    private Double latitude = 30.46922;//纬
    private Handler handler;
    String device;
    HashMap<String, Double> msgData = new HashMap<>();
    HashMap<String, String> msgDataStr = new HashMap<>();
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
    HashMap<String,ArrayList<Double>> oldData = new HashMap<>();
    HashMap<String,Double> newData = new HashMap<>();
    HashMap<String,Double> weightedAverage = new HashMap<>();
    HashMap<String,Double> ROC = new HashMap<>();

    private String[] deviceFlag = {"device2","1","温度","CurrentTemperature"};
    Boolean canSMS = false;//是否可以发短信
    Boolean SMSOK = false;//是否已经发送过短信
    String SMSmsg;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        DB_init();
        handler = new Handler() {
            @SuppressLint("HandlerLeak")
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {

                    case 1: //开机校验更新回传
                        break;
                    case 2:  // 反馈回传
                        break;
                    case 3:  //MQTT 收到消息回传   UTF8Buffer msg=new UTF8Buffer(object.toString());
                        device = msg.obj.toString().split("!")[0].split("/")[4];
                        deviceFlag[0] = device.replaceAll("_","").toLowerCase(Locale.ROOT);
                        SMSmsg = "尊敬的用户，您的移动式压力容器" + deviceFlag[0] + "出现警报，请及时联系驾驶员18171094591，并保持关注！";
                        initMsg(JSON.parseObject(msg.obj.toString().split("!")[1], newRootBean.class));
//                        getOldData();
//                        filter(new String[]{"temperature_1","temperature_2","pressure_1","pressure_2","liquid_1","liquid_2"},
//                                new Double[]{10.0,10.0,0.1,0.1,0.1,0.1});
//                        computeWA();
//                        computeROC();
//                        alert();
                        store();

                        break;
                    case 30:  //连接失败
                        break;
                    case 31:   //连接成功
//                        try {
//                            client.subscribe(mqtt_sub_topic, 1);
//                        } catch (MqttException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            client.subscribe(mqtt_sub_topic1,1);
//                        } catch (MqttException e) {
//                            e.printStackTrace();
//                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new MQTTConnecter(host,userName,passWord,mqtt_id,handler).start();
        return super.onStartCommand(intent, flags, startId);
    }
    private void DB_init() {
        dbOpenHelper = new DBOpenHelper(this, "test_db",null,1);;
        db = dbOpenHelper.getWritableDatabase();
    }
    private void initMsg(@NonNull newRootBean obj){
        longitude = obj.getLongitude();
        latitude = obj.getLatitude();

        msgData.put("CurrentTemperature",obj.getCurrentTemperature());
        msgData.put("Humidity",obj.getHumidity());
        msgData.put("water_bump",obj.getWater_bump());
        msgData.put("air_bump",obj.getAir_bump());
        msgData.put("WindLevel",obj.getWindLevel());
        msgData.put("shield",obj.getShield());

    }
    private void alert(){
        Boolean canWarn = false;
        for (Map.Entry<String,Double> entry:msgData.entrySet()
             ) {
            if (Math.abs(entry.getValue()) > TV.get(entry.getKey()) * 0.7 && Math.abs(entry.getValue()) <= TV.get(entry.getKey())){
                canWarn = true;
            }
        }
        for (Map.Entry<String,Double> entry:msgData.entrySet()
        ) {
            if (Math.abs(entry.getValue()) > TV.get(entry.getKey())){
                createNotification(new String[]{"二级预警","预警信息"});
                Toast.makeText(this,"二级预警:" + device,Toast.LENGTH_SHORT).show();
                canSMS = true;
                break;
            }else {
                canSMS = false;
            }
        }
        if (canSMS && !SMSOK){
            LV2Warning();
            SMSOK = true;
        }
        if (!canSMS){
            SMSOK = false;
        }
        if (canWarn){
            LV1Warning();
        }

    }
    private void getOldData(){
        String sql = String.format("select * from  %s limit 2 offset (select count(*) - 2 from %s)",deviceFlag[0],deviceFlag[0]);
        Cursor cursor = db.rawQuery(sql,null);
        ArrayList<Double> ALOldCurrentTemperature_1= new  ArrayList<>();
        ArrayList<Double> ALOldPressure_1= new  ArrayList<>();
        ArrayList<Double> ALOldLiquid_1= new  ArrayList<>();
        ArrayList<Double> ALOldInclination_1= new  ArrayList<>();
        ArrayList<Double> ALOldCurrentTemperature_2= new  ArrayList<>();
        ArrayList<Double> ALOldPressure_2= new  ArrayList<>();
        ArrayList<Double> ALOldLiquid_2= new  ArrayList<>();
        ArrayList<Double> ALOldInclination_2= new  ArrayList<>();
        //前两次数据添加到arr中
        for(int i = 0; i < 2; i++) {
            cursor.moveToNext();
            double oldCurrentTemperature_1 = Double.parseDouble(cursor.getString(cursor.getColumnIndex("newtemperature_1")));
            double oldPressure_1 = Double.parseDouble(cursor.getString(cursor.getColumnIndex("newpressure_1")));
            double oldVibration_1 = Double.parseDouble(cursor.getString(cursor.getColumnIndex("newliquid_1")));
            double oldInclination_1 = Double.parseDouble(cursor.getString(cursor.getColumnIndex("newinclination_1")));
            double oldCurrentTemperature_2 = Double.parseDouble(cursor.getString(cursor.getColumnIndex("newtemperature_2")));
            double oldPressure_2 = Double.parseDouble(cursor.getString(cursor.getColumnIndex("newpressure_2")));
            double oldVibration_2 = Double.parseDouble(cursor.getString(cursor.getColumnIndex("newliquid_2")));
            double oldInclination_2 = Double.parseDouble(cursor.getString(cursor.getColumnIndex("newinclination_2")));
            ALOldCurrentTemperature_1.add(oldCurrentTemperature_1);
            ALOldPressure_1.add(oldPressure_1);
            ALOldLiquid_1.add(oldVibration_1);
            ALOldInclination_1.add(oldInclination_1);
            ALOldCurrentTemperature_2.add(oldCurrentTemperature_2);
            ALOldPressure_2.add(oldPressure_2);
            ALOldLiquid_2.add(oldVibration_2);
            ALOldInclination_2.add(oldInclination_2);
        }
        cursor.close();
        //添加本次数据到arr中
        ALOldCurrentTemperature_1.add(msgData.get("temperature_1"));
        ALOldPressure_1.add(msgData.get("pressure_1"));
        ALOldLiquid_1.add(msgData.get("liquid_1"));
        ALOldInclination_1.add(msgData.get("inclination_1"));
        ALOldCurrentTemperature_2.add(msgData.get("temperature_2"));
        ALOldPressure_2.add(msgData.get("pressure_2"));
        ALOldLiquid_2.add(msgData.get("liquid_2"));
        ALOldInclination_2.add(msgData.get("inclination_2"));

        oldData.put("temperature_1",ALOldCurrentTemperature_1);
        oldData.put("temperature_2",ALOldCurrentTemperature_2);
        oldData.put("pressure_1",ALOldPressure_1);
        oldData.put("pressure_2",ALOldPressure_2);
        oldData.put("liquid_1",ALOldLiquid_1);
        oldData.put("liquid_2",ALOldLiquid_2);
        oldData.put("inclination_1",ALOldInclination_1);
        oldData.put("inclination_2",ALOldInclination_2);
    }
    private void filter(@NonNull String[] keys, Double[] regex){
        newData.putAll(msgData);
        for (int i = 0;i < keys.length;i++) {
            if (Collections.max(resdualArr(oldData.get(keys[i]))) > regex[i] ){
                replace(keys[i]);
            }
        }
    }
    private void computeWA(){
        for (Map.Entry<String,ArrayList<Double>> entry:oldData.entrySet()
        ) {
            weightedAverage.put(entry.getKey(),wAverage(entry.getValue(),w));
        }
    }
    private void computeROC() {
        for (Map.Entry<String,ArrayList<Double>> entry:oldData.entrySet()
        ){
            ROC.put(entry.getKey(),ROC(entry.getValue()));
        }
    }
    private void store(){
        ContentValues values = new ContentValues();
        values.put("Longitude",longitude);
        values.put("Latitude",latitude);
        for (Map.Entry<String,Double> entry:msgData.entrySet()){
            values.put(entry.getKey(),String.valueOf(entry.getValue()));
        }
        db.insert(deviceFlag[0],null,values);
    }


    private void replace(String key){
        newData.put(key,oldData.get(key).get(1));
        oldData.get(key).set(2,newData.get(key));
    }
    public ArrayList<Double> resdualArr(ArrayList<Double> arrayList){
        ArrayList<Double> arr = new ArrayList<>();
        double average = average(arrayList);
        for (int i = 0;i < arrayList.size();i++){
            arr.add(Math.abs(arrayList.get(i) - average));
        }
        return  arr;
    }
    public double average(ArrayList<Double> arr){
        double resault = 0;
        for (int i = 0; i < arr.size(); i++) {
            resault += arr.get(i);
        }
        return resault / arr.size();
    }
    public double wAverage(ArrayList<Double> arr,float[] weight){
        double resault = 0;
        for (int i = 0; i < arr.size(); i++) {
            resault += arr.get(i) * weight[i];
        }
        return resault;
    }
    public double variance(double average,ArrayList<Double> arr){
        double sum = 0;
        for(int i = 0; i < arr.size(); i++) {
            sum += (average - arr.get(i)) * (average - arr.get(i));
        }
        return sum / arr.size();
    }
    public double ROC(ArrayList<Double> arr){
        if (Math.abs(arr.get(1)-arr.get(0)) == 0){
            return 0;
        }
        return (Math.abs(arr.get(2)-arr.get(1))) / (Math.abs(arr.get(1)-arr.get(0)));
    }
    private void createNotification(String [] msg){
        String CHANNELID ="1";
        String CHANNELNAME = "channel1";

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //创建渠道
            /**
             * importance:用于表示渠道的重要程度。这可以控制发布到此频道的中断通知的方式。
             * 有以下6种重要性，是NotificationManager的静态常量，依次递增:
             * IMPORTANCE_UNSPECIFIED（值为-1）意味着用户没有表达重要性的价值。此值用于保留偏好设置，不应与实际通知关联。
             * IMPORTANCE_NONE（值为0）不重要的通知：不会在阴影中显示。
             * IMPORTANCE_MIN（值为1）最低通知重要性：只显示在阴影下，低于折叠。这不应该与Service.startForeground一起使用，因为前台服务应该是用户关心的事情，所以它没有语义意义来将其通知标记为最低重要性。如果您从Android版本O开始执行此操作，系统将显示有关您的应用在后台运行的更高优先级通知。
             * IMPORTANCE_LOW（值为2）低通知重要性：无处不在，但不侵入视觉。
             * IMPORTANCE_DEFAULT （值为3）：默认通知重要性：随处显示，产生噪音，但不会在视觉上侵入。
             * IMPORTANCE_HIGH（值为4）更高的通知重要性：随处显示，造成噪音和窥视。可以使用全屏的Intent。
             */
            NotificationChannel channel = new NotificationChannel(CHANNELID,CHANNELNAME,NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);//开启渠道
//            Intent intent = new Intent(MainActivity.this,login.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,0,intent,0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNELID);
            builder .setContentTitle(msg[0])//通知标题
                    .setContentText(msg[1])//通知内容
                    .setWhen(System.currentTimeMillis())//通知显示时间
//                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.appicon)
                    .setAutoCancel(true)//点击通知取消
                    //.setSound()
                    //第一个参数为手机静止时间，第二个参数为手机震动时间，周而复始
//                    .setVibrate(new long[] {0,1000,1000,1000})//手机震动
                    //第一个参数为LED等颜色，第二个参数为亮的时长，第三个参数为灭的时长
//                    .setLights(Color.BLUE,1000,1000)
                    /**表示通知的重要程度
                     * RIORITY_DEFAULT
                     * RIORITY_MIN
                     * RIORITY_LOW
                     * RIORITY_HIGE
                     * RIORITY_MAX
                     **/
                    .setPriority(NotificationCompat.PRIORITY_MAX);
            manager.notify(1,builder.build());
        } else {
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Title")
                    .setContentText("ContentText")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.appicon)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.appicon))
                    .build();
            manager.notify(1, notification);
        }
    }
    private void LV1Warning(){
        Log.d(TAG, "LV1Warning: ");
        Toast.makeText(this,"一级预警:" + device,Toast.LENGTH_SHORT).show();
        createNotification(new String[]{"一级预警","预警信息"});
    }
    private void LV2Warning(){
        Log.d(TAG, "LV2Warning: ");
        new SMSSender(PHONENUMBER, SMSmsg).start();
    }


    @Override
    public void onDestroy() {
        db.close();
        super.onDestroy();
    }
}