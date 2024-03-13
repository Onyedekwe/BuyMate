package com.hemerick.buymate;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.hemerick.buymate.Database.UserSettings;


public class HomeActivity extends AppCompatActivity {
    private static final long DOUBLE_BACK_PRESS_INTERVAL = 2000;
    private long backPressedTime = 0;
    private PowerManager.WakeLock wakeLock;
    private UserSettings settings;

    private BottomNavigationView bottomNavigationView;


    AdView adView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = (UserSettings) getApplication();
        SharedPreferences sharedPreferences_theme = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String theme = sharedPreferences_theme.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);
        String dim = sharedPreferences_theme.getString(UserSettings.IS_DIM_THEME_ENABLED, UserSettings.NO_DIM_THEME_NOT_ENABLED);
        settings.setIsDimThemeEnabled(dim);

        if (settings.getCustomTheme().equals(UserSettings.DEFAULT_THEME)) {
            int currentNightMode = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {


                if (settings.getIsDimThemeEnabled().equals(UserSettings.YES_DIM_THEME_ENABLED)) {
                    setTheme(R.style.Dynamic_Dim);
                }
            }

        } else if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {

            if (settings.getIsDimThemeEnabled().equals(UserSettings.YES_DIM_THEME_ENABLED)) {
                setTheme(R.style.Dynamic_Dim);
            }
        }

        setContentView(R.layout.activity_home);


        bottomNavigationView = findViewById(R.id.bottombar);


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
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

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });

        adView = findViewById(R.id.adView);

        if (!settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)) {
            AdRequest adRequest = new AdRequest.Builder().build();
            SharedPreferences preferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
            long installDateMillis = preferences.getLong(UserSettings.KEY_INSTALL_DATE, 0);

            if (installDateMillis == 0) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(UserSettings.KEY_INSTALL_DATE, System.currentTimeMillis());
                editor.apply();
            } else {
                long currentTimeMillis = System.currentTimeMillis();
                long elapsedTimeMillis = currentTimeMillis - installDateMillis;
                if (elapsedTimeMillis >= UserSettings.SEVEN_DAYS_IN_MILLIS) {
                    adView.setVisibility(View.VISIBLE);
                    adView.loadAd(adRequest);
                }
            }
        }


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

        String lifetime = sharedPreferences.getString(UserSettings.IS_LIFETIME_PURCHASED, UserSettings.NO_LIFETIME_NOT_SUBSCRIBED);
        settings.setIsLifetimePurchased(lifetime);

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


        SharedPreferences sharedPreference = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);

        long launchTime = sharedPreference.getLong("LaunchTime", 0);
        if (launchTime == 0) {
            launchTime = System.currentTimeMillis();
            SharedPreferences.Editor editor = sharedPreference.edit();
            editor.putLong("LaunchTime", launchTime);
            editor.apply();
        }

        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - launchTime;

        long daysDifference = timeDifference / (1000 * 60 * 60 * 24);

        if (daysDifference >= 7) {
            SharedPreferences.Editor editor = sharedPreference.edit();
            editor.putLong("LaunchTime", currentTime);
            editor.apply();
            showRatingDialog();
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


    public void showRatingDialog() {

        Dialog rate_dialog = new Dialog(this);
        rate_dialog.setContentView(R.layout.custom_rate_us_dialog);
        rate_dialog.setCancelable(false);
        rate_dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        rate_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RatingBar ratingBar = rate_dialog.findViewById(R.id.ratingBar);
        TextView sub_header = rate_dialog.findViewById(R.id.sub_header);
        Button submitBtn = rate_dialog.findViewById(R.id.rateBtn);
        Button cancelBtn = rate_dialog.findViewById(R.id.cancelBtn);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            submitBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            submitBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            submitBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBar.getRating();
                if (rating > 0) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_link))));
                    }

                    rate_dialog.dismiss();

                } else {
                    Toast.makeText(HomeActivity.this, getString(R.string.custom_rate_us_dialog_emptyRate), Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rate_dialog.dismiss();
            }
        });

        rate_dialog.show();


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
                    Toast.makeText(getApplicationContext(), getString(R.string.HomeActivity__pressBack), Toast.LENGTH_SHORT).show();
                }
                backPressedTime = currentTime;
            }
        }
    }


}