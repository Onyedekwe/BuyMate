package com.henrychinedu.buymate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.henrychinedu.buymate.Database.UserSettings;


public class CrossSettingActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView crossText, crossDescription;
    SwitchCompat crossSwitch;
    ConstraintLayout crossSwitchLayout;
    private UserSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new UserSettings();
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

        setContentView(R.layout.activity_cross_setting);

        toolbar = findViewById(R.id.swipeToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrossSettingActivity.super.onBackPressed();
            }
        });


        crossText = findViewById(R.id.crossDisableTextHeader);
        crossDescription = findViewById(R.id.cross_description);
        crossSwitch = findViewById(R.id.disable_cross_switch);
        crossSwitchLayout = findViewById(R.id.swipeDisableLayout);

        crossSwitchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crossSwitch.setChecked(!crossSwitch.isChecked());
            }
        });


        crossSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings.setIsCrossDisabled(UserSettings.YES_CROSS_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_CROSS_DISABLED, settings.getIsCrossDisabled());
                    editor.apply();
                } else {
                    settings.setIsCrossDisabled(UserSettings.NO_CROSS_NOT_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_CROSS_DISABLED, settings.getIsCrossDisabled());
                    editor.apply();
                }
            }
        });

        loadSharedPreferences();
    }

    private void updateView() {


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            crossText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            crossDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            crossText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            crossDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            crossText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            crossDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        }

        crossSwitch.setChecked(!settings.getIsCrossDisabled().equals(UserSettings.NO_CROSS_NOT_DISABLED));

    }

    private void loadSharedPreferences() {

        boolean wakeLockEnabled = UserSettings.isWakeLockEnabled(this);
        if (wakeLockEnabled) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");
            wakeLock.acquire();
        }

        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);

        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        String cross = sharedPreferences.getString(UserSettings.IS_CROSS_DISABLED, UserSettings.NO_CROSS_NOT_DISABLED);
        settings.setIsCrossDisabled(cross);

        updateView();
    }
}