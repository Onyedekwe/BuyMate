package com.hemerick.buymate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

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
        }, 500);


        if (settings.getIsAppFirstStart().equals(UserSettings.YES_APP_FIRST_START)) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1500);

        } else {

            if (settings.getIsAuthenticated().equals(UserSettings.NOT_AUTHENTICATED)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent signup_intent = new Intent(SplashActivity.this, SignUpActivity.class);
                        startActivity(signup_intent);
                        finish();
                    }
                }, 1500);
            } else {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intentHome, intentPasscode;
                        if (settings.getPassword().equals(UserSettings.NOT_SET_PASSWORD)) {
                            intentHome = new Intent(SplashActivity.this, HomeActivity.class);
                            startActivity(intentHome);
                            finish();
                        } else {
                            intentPasscode = new Intent(SplashActivity.this, InsertPasscodeActivity.class);
                            startActivity(intentPasscode);
                            finish();
                        }
                    }
                }, 1500);

            }

        }

    }

    public void updateView() {


        if (settings.getCustomTheme().equals(UserSettings.DEFAULT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        if (settings.getCustomTheme().equals(UserSettings.LIGHT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }


        if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.buymate_color_theme));

        getWindow().getDecorView().setSystemUiVisibility(0);


    }

    private void loadSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);

        String theme = sharedPreferences.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);


        String password = sharedPreferences.getString(UserSettings.PASSWORD, UserSettings.NOT_SET_PASSWORD);
        settings.setPassword(password);

        String authenticated = sharedPreferences.getString(UserSettings.IS_AUTHENTICATED, UserSettings.NOT_AUTHENTICATED);
        settings.setIsAuthenticated(authenticated);

        String isAppFirstStart = sharedPreferences.getString(UserSettings.APP_FIRST_START, UserSettings.YES_APP_FIRST_START);
        settings.setIsAppFirstStart(isAppFirstStart);

        updateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().getDecorView().setSystemUiVisibility(0);

    }
}