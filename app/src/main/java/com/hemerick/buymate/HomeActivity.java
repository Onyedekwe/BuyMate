package com.hemerick.buymate;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hemerick.buymate.Database.UserSettings;


public class HomeActivity extends AppCompatActivity {
    private static final long DOUBLE_BACK_PRESS_INTERVAL = 2000;
    private long backPressedTime = 0;
    private PowerManager.WakeLock wakeLock;
    private UserSettings settings;

    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = (UserSettings) getApplication();
        SharedPreferences sharedPreferences_theme = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String theme = sharedPreferences_theme.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);

        if (settings.getCustomTheme().equals(UserSettings.DIM_THEME)) {
            setTheme(R.style.Dynamic_Dim);
        }

        setContentView(R.layout.activity_home);


        bottomNavigationView = findViewById(R.id.bottombar);


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.myhome) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.framelayoutContainer, new HomeFragment());
                    fragmentTransaction.commit();
                    item.setChecked(true);

                } else if (itemId == R.id.myfavourties) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.framelayoutContainer, new FavouritesFragment());
                    fragmentTransaction.commit();
                    item.setChecked(true);
                } else if (itemId == R.id.mynotes) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.framelayoutContainer, new NotesFragment());
                    fragmentTransaction.commit();
                    item.setChecked(true);
                } else if (itemId == R.id.mymore) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.framelayoutContainer, new MoreFragment());
                    fragmentTransaction.commit();
                    item.setChecked(true);
                }

                return false;
            }
        });

        loadSharedPreferences();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.framelayoutContainer, new HomeFragment());
        fragmentTransaction.commit();


    }


    @SuppressLint("ResourceType")
    private void updateView() {

        if (settings.getCustomTheme().equals(UserSettings.DEFAULT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        if (settings.getCustomTheme().equals(UserSettings.LIGHT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }


        if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            bottomNavigationView.setItemTextAppearanceActive(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Caption);
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            bottomNavigationView.setItemTextAppearanceActive(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Body1);
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            bottomNavigationView.setItemTextAppearanceActive(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Medium);
        }


    }

    private void loadSharedPreferences() {

        boolean wakeLockEnabled = UserSettings.isWakeLockEnabled(this);
        if (wakeLockEnabled) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");
            wakeLock.acquire();
        }

        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);

        String theme = sharedPreferences.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);

        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        updateView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);

        boolean isWakeLockEnabled = UserSettings.isWakeLockEnabled(this);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.framelayoutContainer);

        if (isWakeLockEnabled) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");

            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }


        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            bottomNavigationView.setItemTextAppearanceActive(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Caption);
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            bottomNavigationView.setItemTextAppearanceActive(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Body1);
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            bottomNavigationView.setItemTextAppearanceActive(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Medium);
        }

        if (fragment instanceof NotesFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.framelayoutContainer, new NotesFragment());
            fragmentTransaction.commit();
            bottomNavigationView.getMenu().getItem(2).setChecked(true);
        }

        if (fragment instanceof MoreFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.framelayoutContainer, new MoreFragment());
            fragmentTransaction.commit();
            bottomNavigationView.getMenu().getItem(3).setChecked(true);
        }

        if (fragment instanceof HomeFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.framelayoutContainer, new HomeFragment());
            fragmentTransaction.commit();
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
        }


    }

    @Override
    public void onBackPressed() {

        long currentTime = System.currentTimeMillis();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.framelayoutContainer);
        if (fragment instanceof FavouritesFragment) {
            if (!((FavouritesFragment) fragment).onBackPressed()) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.framelayoutContainer, new HomeFragment());
                fragmentTransaction.commit();
                bottomNavigationView.getMenu().getItem(0).setChecked(true);
            }
        } else if (fragment instanceof NotesFragment) {
            if (!((NotesFragment) fragment).onBackPressed()) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.framelayoutContainer, new HomeFragment());
                fragmentTransaction.commit();
                bottomNavigationView.getMenu().getItem(0).setChecked(true);

            }
        } else if (fragment instanceof MoreFragment) {
            if (!((MoreFragment) fragment).onBackPressed()) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.framelayoutContainer, new HomeFragment());
                fragmentTransaction.commit();
                bottomNavigationView.getMenu().getItem(0).setChecked(true);

            }
        } else {
            if (((HomeFragment) fragment).onBackPressed()) {

            } else {
                if (backPressedTime + DOUBLE_BACK_PRESS_INTERVAL > currentTime) {
                    super.onBackPressed();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.back_to_exit), Toast.LENGTH_SHORT).show();
                }
                backPressedTime = currentTime;
            }
        }
    }


}