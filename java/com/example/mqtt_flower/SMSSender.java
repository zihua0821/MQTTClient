package com.example.mqtt_flower;

import android.telephony.SmsManager;

import java.util.List;

public class SMSSender extends Thread {
    private String phone;
    private String msg;

    public SMSSender(String phoneNum,String phoneMsg){
        this.phone = phoneNum;
        this.msg = phoneMsg;
    }
    @Override
    public void run() {
        sendSMS(phone,msg);
    }
    public void sendSMS(String phoneNumber,String message){
        SmsManager smsManager = SmsManager.getDefault();
        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, null, null);
        }
    }
}
