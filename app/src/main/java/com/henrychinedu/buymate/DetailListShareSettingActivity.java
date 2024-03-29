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


public class DetailListShareSettingActivity extends AppCompatActivity {

    Toolbar toolbar;
    private UserSettings settings;

    private SwitchCompat shareWithPriceSwitch, shareWithQuantitySwitch, shareWithTotalSwitch;
    private TextView shareWithPriceText, shareWithQuantityText, shareWithTotalText;
    private TextView shareWithPriceSubText, shareWithQuantitySubText, shareWithTotalSubText;

    ConstraintLayout shareWithPriceLayout, shareWithQuantityLayout, shareWithTotalLayout;


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
        setContentView(R.layout.activity_detail_list_share_setting);


        toolbar = findViewById(R.id.swipeToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailListShareSettingActivity.super.onBackPressed();
            }
        });

        shareWithPriceText = findViewById(R.id.shareWithPriceTextHeader);
        shareWithQuantityText = findViewById(R.id.shareWithQuantityTextHeader);
        shareWithTotalText = findViewById(R.id.shareWithTotalTextHeader);

        shareWithPriceSubText = findViewById(R.id.shareWithPriceSubTextHeader);
        shareWithQuantitySubText = findViewById(R.id.shareWithQuantitySubTextHeader);
        shareWithTotalSubText = findViewById(R.id.shareWithTotalSubTextHeader);

        shareWithPriceSwitch = findViewById(R.id.disable_share_with_price_switch);
        shareWithQuantitySwitch = findViewById(R.id.disable_share_with_quantity_switch);
        shareWithTotalSwitch = findViewById(R.id.disable_share_with_total_switch);

        shareWithPriceLayout = findViewById(R.id.shareWithPriceLayout);
        shareWithQuantityLayout = findViewById(R.id.shareWithQuantityLayout);
        shareWithTotalLayout = findViewById(R.id.shareWithTotalLayout);

        shareWithPriceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWithPriceSwitch.setChecked(!shareWithPriceSwitch.isChecked());
            }
        });

        shareWithPriceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings.setIsSharePriceDisabled(UserSettings.NO_SHARE_PRICE_NOT_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_SHARE_PRICE_DISABLED, settings.getIsSharePriceDisabled());
                    editor.apply();
                } else {
                    settings.setIsSharePriceDisabled(UserSettings.YES_SHARE_PRICE_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_SHARE_PRICE_DISABLED, settings.getIsSharePriceDisabled());
                    editor.apply();
                }
            }
        });

        shareWithQuantityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWithQuantitySwitch.setChecked(!shareWithQuantitySwitch.isChecked());
            }
        });

        shareWithQuantitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings.setIsShareQuantityDisabled(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_SHARE_QUANTITY_DISABLED, settings.getIsShareQuantityDisabled());
                    editor.apply();
                } else {
                    settings.setIsShareQuantityDisabled(UserSettings.YES_SHARE_QUANTITY_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_SHARE_QUANTITY_DISABLED, settings.getIsShareQuantityDisabled());
                    editor.apply();
                }
            }
        });

        shareWithTotalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWithTotalSwitch.setChecked(!shareWithTotalSwitch.isChecked());
            }
        });
        shareWithTotalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings.setIsShareTotalDisabled(UserSettings.NO_SHARE_TOTAL_NOT_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_SHARE_TOTAL_DISABLED, settings.getIsShareTotalDisabled());
                    editor.apply();
                } else {
                    settings.setIsShareTotalDisabled(UserSettings.YES_SHARE_TOTAL_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_SHARE_TOTAL_DISABLED, settings.getIsShareTotalDisabled());
                    editor.apply();
                }
            }
        });


        loadSharedPreferences();

    }


    private void updateView() {

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            shareWithPriceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            shareWithPriceSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            shareWithQuantityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            shareWithQuantitySubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            shareWithTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            shareWithTotalSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));


        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            shareWithPriceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            shareWithPriceSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            shareWithQuantityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            shareWithQuantitySubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            shareWithTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            shareWithTotalSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            shareWithPriceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            shareWithPriceSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            shareWithQuantityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            shareWithQuantitySubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            shareWithTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            shareWithTotalSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
            shareWithPriceSwitch.setChecked(true);
        }

        if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
            shareWithQuantitySwitch.setChecked(true);
        }
        if (settings.getIsShareTotalDisabled().equals(UserSettings.NO_SHARE_TOTAL_NOT_DISABLED)) {
            shareWithTotalSwitch.setChecked(true);
        }

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

        String is_price_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_PRICE_DISABLED, UserSettings.NO_SHARE_PRICE_NOT_DISABLED);
        settings.setIsSharePriceDisabled(is_price_disabled);

        String is_quantity_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_QUANTITY_DISABLED, UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED);
        settings.setIsShareQuantityDisabled(is_quantity_disabled);

        String is_total_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_TOTAL_DISABLED, UserSettings.NO_SHARE_TOTAL_NOT_DISABLED);
        settings.setIsShareTotalDisabled(is_total_disabled);

        updateView();
    }

}