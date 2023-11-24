package com.hemerick.buymate;

import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;


import com.hemerick.buymate.Database.UserSettings;


public class SplashActivity extends AppCompatActivity {

    private UserSettings settings;
    LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        settings = new UserSettings();
        linearLayout = findViewById(R.id.appName);
        loadSharedPreferences();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               linearLayout.setVisibility(View.VISIBLE);
            }
        }, 1000);

        if(settings.getIsAuthenticated().equals(UserSettings.NOT_AUTHENTICATED)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, SignUpActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }, 3000);
        }else{

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (settings.getPassword().equals(UserSettings.NOT_SET_PASSWORD)) {
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                }else {
                    intent = new Intent(SplashActivity.this, InsertPasscodeActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }, 3000);

        }
    }

    private void loadSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);

        String password = sharedPreferences.getString(UserSettings.PASSWORD, UserSettings.NOT_SET_PASSWORD);
        settings.setPassword(password);

        String authenticated = sharedPreferences.getString(UserSettings.IS_AUTHENTICATED,UserSettings.NOT_AUTHENTICATED);
        settings.setIsAuthenticated(authenticated);
    }
}