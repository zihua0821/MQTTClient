package com.example.mqtt_flower;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class login extends AppCompatActivity {

    private SharedPreferences sp;
    private EditText et_name;
    private EditText et_pwd;
    private CheckBox remember_pwd;
    private CheckBox auto_login;
    private Button bt_register;
    private Button bt_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        setTitle("育苗护苗一体化绿色智能防沙固土装置");
        sp = getSharedPreferences("config", Context.MODE_PRIVATE);

        initView();


        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = et_name.getText().toString().trim();
                String pwd = et_pwd.getText().toString().trim();
                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)){
                    Toast.makeText(login.this,"用户或密码为空",Toast.LENGTH_SHORT).show();
                }else{
                    startService(new Intent(login.this,DataService.class));
                    Intent intent = new Intent(login.this,MainActivity.class);
                    startActivity(intent);
                }

            }
        });
        //第二次打开的时候从SP获取数据，画面同步
        boolean rememberpwd = sp.getBoolean("rememberpwd",false);//如果获取是空，就返回默认值
        boolean autologin = sp.getBoolean("autologin",false);

        if(rememberpwd){
            String name = sp.getString("name","");
            String pwd = sp.getString("pwd","");
            et_name.setText(name);
            et_pwd.setText(pwd);
            remember_pwd.setChecked(true);
        }
        if(autologin){
            auto_login.setChecked(true);

            Intent intent = new Intent(login.this,MainActivity.class);
            startActivity(intent);
            Toast.makeText(this,"我自动登录了",Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        et_name = findViewById(R.id.et_name);
        et_pwd = findViewById(R.id.et_pwd);
        remember_pwd = findViewById(R.id.remember_pwd);
        auto_login = findViewById(R.id.auto_login);
        bt_register = findViewById(R.id.bt_register);
        bt_login = findViewById(R.id.bt_login);

        //设置监听
        MyOnClickListener l = new MyOnClickListener();
        bt_login.setOnClickListener(l);
        bt_register.setOnClickListener(l);
    }

    private class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.bt_register:
                    break;

                //登录按钮
                case R.id.bt_login:
                    //登录操作
                    String name = et_name.getText().toString().trim();
                    String pwd = et_pwd.getText().toString().trim();
                    if(TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)){
                        Toast.makeText(login.this,"用户或密码为空",Toast.LENGTH_SHORT).show();
                    }else {
                        if(remember_pwd.isChecked()){
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("name",name);
                            editor.putString("pwd",pwd);
                            editor.putBoolean("rememberpwd",true);
                            editor.apply();
                        }

                        if(auto_login.isChecked()){
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean("autologin",true);
                            editor.apply();
                        }
                    }
                    break;
            }
        }
    }
}