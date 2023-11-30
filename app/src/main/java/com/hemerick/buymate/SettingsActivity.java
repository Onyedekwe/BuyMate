package com.hemerick.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.hemerick.buymate.Database.UserSettings;


public class SettingsActivity extends AppCompatActivity {

    TextView generalText, securityText, notificationText,
            darkModeText, keepScreenBrightText,
            textSizeText, lockAppText, swipeActionText, swipeActionSubText1, swipeActionSubText2,
            detailedListShareText, detailedListShareSubText, disablePriceText, multiplyText, strikeText,
            currencyText, currencySubText;
    Toolbar toolbar;
    SharedPreferences sharedPreferences;
    private SwitchCompat darkModeSwitch, keepScreenBrightSwitch, disablePriceSwitch, multiplySwitch;
    private ConstraintLayout NotificationLayout, TextSizeLayout, SwipeActionLayout,
            DetailedSharingLayout, DisablePriceLayout, MultiplyLayout, StrikeLayout, CurrencyLayout,
            AppLockLayout;
    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = new UserSettings();

        toolbar = findViewById(R.id.settingToolbar);

        NotificationLayout = findViewById(R.id.NotificationLayout);
        TextSizeLayout = findViewById(R.id.textSizeLayout);
        generalText = findViewById(R.id.generalHeaderText);
        securityText = findViewById(R.id.securityHeaderText);
        notificationText = findViewById(R.id.notificationText);
        darkModeText = findViewById(R.id.themeText);
        keepScreenBrightText = findViewById(R.id.screenText);
        detailedListShareText = findViewById(R.id.detailedListShareTextHeader);
        detailedListShareSubText = findViewById(R.id.detailedListShareSubText);
        textSizeText = findViewById(R.id.textSizeText);
        lockAppText = findViewById(R.id.lockText);
        swipeActionText = findViewById(R.id.swipeActionText);
        swipeActionSubText1 = findViewById(R.id.swipeActionSubText1);
        swipeActionSubText2 = findViewById(R.id.swipeActionSubText2);
        darkModeSwitch = findViewById(R.id.darkThemeSwitcher);
        keepScreenBrightSwitch = findViewById(R.id.keepScreenBrightSwitcher);
        multiplySwitch = findViewById(R.id.multiplySwitcher);
        disablePriceSwitch = findViewById(R.id.disablePriceSwitcher);
        SwipeActionLayout = findViewById(R.id.swipeActionLayout);
        DetailedSharingLayout = findViewById(R.id.detailedListShareLayout);
        MultiplyLayout = findViewById(R.id.multiplyLayout);
        StrikeLayout = findViewById(R.id.StrikeLayout);
        multiplyText = findViewById(R.id.multiplyText);
        disablePriceText = findViewById(R.id.disable_price_Text);
        DisablePriceLayout = findViewById(R.id.disablePriceLayout);

        strikeText = findViewById(R.id.strikeText);
        currencyText = findViewById(R.id.currencyTextHeader);
        currencySubText = findViewById(R.id.currencySubText);
        CurrencyLayout = findViewById(R.id.currencyLayout);
        AppLockLayout = findViewById(R.id.lockLayout);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        NotificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    settings.setCustomTheme(UserSettings.DARK_THEME);
                } else {
                    settings.setCustomTheme(UserSettings.LIGHT_THEME);
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(UserSettings.CUSTOM_THEME, settings.getCustomTheme());
                editor.apply();
                updateView();
            }
        });

        keepScreenBrightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    UserSettings.setWakeLockEnabled(SettingsActivity.this, true);
                    loadSharedPreferences();
                } else {
                    UserSettings.setWakeLockEnabled(SettingsActivity.this, false);
                    wakeLock.release();
                }
            }
        });

        StrikeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, CrossSettingActivity.class);
                startActivity(intent);
            }
        });

        AppLockLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ConfigurePasscodeActivity.class);
                startActivity(intent);
            }
        });

        multiplySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings.setIsMultiplyDisabled(UserSettings.NO_MULTIPLY_NOT_DISABLED);
                } else {
                    settings.setIsMultiplyDisabled(UserSettings.YES_MULTIPLY_DISABLED);
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(UserSettings.IS_MULTIPLY_DISABLED, settings.getIsMultiplyDisabled());
                editor.apply();
            }
        });

        disablePriceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings.setIsPriceDisabled(UserSettings.NO_PRICE_NOT_DISABLED);
                } else {
                    settings.setIsPriceDisabled(UserSettings.YES_PRICE_DISABLED);
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(UserSettings.IS_PRICE_DISABLED, settings.getIsPriceDisabled());
                editor.apply();
            }
        });

        TextSizeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextSizeDialog();
            }
        });


        SwipeActionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, SwipeSettingActivity.class);
                startActivity(intent);
            }
        });


        DetailedSharingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, DetailListShareSettingActivity.class);
                startActivity(intent);
            }
        });

        CurrencyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrencyDialog();
            }
        });


        loadSharedPreferences();
    }


    public void showTextSizeDialog() {
        Dialog dialog = new Dialog(SettingsActivity.this);
        dialog.setContentView(R.layout.custom_textsize_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.edit_title);

        RadioButton smallTextButton = dialog.findViewById(R.id.radio_button_small);
        RadioButton mediumTextButton = dialog.findViewById(R.id.radio_button_medium);
        RadioButton largeTextButton = dialog.findViewById(R.id.radio_button_large);

        sharedPreferences = SettingsActivity.this.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String sort = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(sort);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            smallTextButton.setChecked(true);
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            smallTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            mediumTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            largeTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            mediumTextButton.setChecked(true);
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            smallTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            mediumTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            largeTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            largeTextButton.setChecked(true);
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            smallTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            mediumTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            largeTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        smallTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomTextSize(UserSettings.TEXT_SMALL);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_TEXT_SIZE, settings.getCustomTextSize());
                editor.apply();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }

        });

        mediumTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomTextSize(UserSettings.TEXT_MEDIUM);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_TEXT_SIZE, settings.getCustomTextSize());
                editor.apply();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        largeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomTextSize(UserSettings.TEXT_LARGE);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_TEXT_SIZE, settings.getCustomTextSize());
                editor.apply();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);


            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateView();
            }
        });

        dialog.show();
    }

    public void showCurrencyDialog() {
        Dialog dialog = new Dialog(SettingsActivity.this);
        dialog.setContentView(R.layout.custom_currency_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.edit_title);
        AutoCompleteTextView descCurrency = dialog.findViewById(R.id.desc_currency);

        Button cancelButton = dialog.findViewById(R.id.CurrencyCancelButton);
        Button saveButton = dialog.findViewById(R.id.currencyBtnSave);

        String currency = settings.getCurrency();

        descCurrency.setText(currency);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            descCurrency.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            descCurrency.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            descCurrency.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }



        String[] currency_list = getResources().getStringArray(R.array.currency);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(SettingsActivity.this, R.layout.unit_drop_down_layout, currency_list);
        descCurrency.setAdapter(adapter);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputted_symbol = descCurrency.getText().toString();
                if (!inputted_symbol.isEmpty()) {
                    settings.setCurrency(inputted_symbol);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.CURRENCY, settings.getCurrency());
                    editor.apply();
                    Toast.makeText(SettingsActivity.this, "Currency updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(SettingsActivity.this, "Currency field is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }


    private void updateView() {
        if (settings.getCustomTheme().equals(UserSettings.LIGHT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            if (darkModeSwitch.isChecked()) {
                darkModeSwitch.setChecked(false);
            }

        } else if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            if (!darkModeSwitch.isChecked()) {
                darkModeSwitch.setChecked(true);
            }

        }


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            generalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            securityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            notificationText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            multiplyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            disablePriceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            strikeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            darkModeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            keepScreenBrightText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            textSizeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            lockAppText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            swipeActionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            swipeActionSubText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            swipeActionSubText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            detailedListShareText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            detailedListShareSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            currencyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            currencySubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            generalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            securityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            notificationText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            multiplyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            disablePriceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            strikeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            darkModeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            keepScreenBrightText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            textSizeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            lockAppText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            swipeActionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            detailedListShareText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            currencyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            currencySubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            detailedListShareSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            swipeActionSubText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            swipeActionSubText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            generalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            securityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            notificationText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            multiplyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            disablePriceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            strikeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            darkModeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            keepScreenBrightText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            textSizeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            lockAppText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            swipeActionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            detailedListShareText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            currencyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            currencySubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            detailedListShareSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            swipeActionSubText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            swipeActionSubText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getIsSwipeDisabled().equals(UserSettings.YES_DISABLED)) {
            swipeActionSubText1.setText("Disabled");
            swipeActionSubText2.setVisibility(View.GONE);
        } else {

            switch (settings.getCustomLeftSwipeAction()) {
                case UserSettings.SWIPE_RENAME:
                    swipeActionSubText1.setText("Rename: swipe left");
                    break;

                case UserSettings.SWIPE_CHANGE_PRICE:
                    swipeActionSubText1.setText("Change price: swipe left");
                    break;

                case UserSettings.SWIPE_CHANGE_QUANTITY:
                    swipeActionSubText1.setText("Change quantity: swipe left");
                    break;

                case UserSettings.SWIPE_STAR_ITEM:
                    swipeActionSubText1.setText("Star & unstar item: swipe left");
                    break;

                case UserSettings.SWIPE_DELETE_ITEM:
                    swipeActionSubText1.setText("Delete: swipe left");
                    break;

                case UserSettings.SWIPE_CHECK:
                    swipeActionSubText1.setText("Check & uncheck item: swipe left");
                    break;

                case UserSettings.SWIPE_SHOW_OPTIONS:
                    swipeActionSubText1.setText("Show options: swipe left");
                    break;

                case UserSettings.SWIPE_DO_NOTHING:
                    swipeActionSubText1.setText("Do nothing: swipe left");
                    break;

            }

            switch (settings.getCustomRightSwipeAction()) {
                case UserSettings.SWIPE_RENAME:
                    swipeActionSubText2.setText("Rename: swipe right");
                    break;

                case UserSettings.SWIPE_CHANGE_PRICE:
                    swipeActionSubText2.setText("Change price: swipe right");
                    break;

                case UserSettings.SWIPE_CHANGE_QUANTITY:
                    swipeActionSubText2.setText("Change quantity: swipe right");
                    break;

                case UserSettings.SWIPE_STAR_ITEM:
                    swipeActionSubText2.setText("Star & unstar item: swipe right");
                    break;

                case UserSettings.SWIPE_CHECK:
                    swipeActionSubText2.setText("Check & uncheck item: swipe right");
                    break;

                case UserSettings.SWIPE_DELETE_ITEM:
                    swipeActionSubText2.setText("Delete: swipe right");
                    break;

                case UserSettings.SWIPE_SHOW_OPTIONS:
                    swipeActionSubText2.setText("Show options: swipe right");
                    break;

                case UserSettings.SWIPE_DO_NOTHING:
                    swipeActionSubText2.setText("Do nothing: swipe right");
                    break;

            }
        }

        multiplySwitch.setChecked(settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED));

        if(settings.getIsPriceDisabled().equals(UserSettings.YES_PRICE_DISABLED)){
            disablePriceSwitch.setChecked(false);
        }else{
            disablePriceSwitch.setChecked(true);
        }
    }

    private void loadSharedPreferences() {

        boolean wakeLockEnabled = UserSettings.isWakeLockEnabled(this);
        if (wakeLockEnabled) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");
            wakeLock.acquire();
            keepScreenBrightSwitch.setChecked(true);
        } else {
            keepScreenBrightSwitch.setChecked(false);
        }

        sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);

        String theme = sharedPreferences.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);

        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        String leftSwipeAction = sharedPreferences.getString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, UserSettings.SWIPE_SHOW_OPTIONS);
        settings.setCustomLeftSwipeAction(leftSwipeAction);

        String rightSwipeAction = sharedPreferences.getString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, UserSettings.SWIPE_DELETE_ITEM);
        settings.setCustomRightSwipeAction(rightSwipeAction);

        String disables_swipe = sharedPreferences.getString(UserSettings.IS_SWIPE_DISABLED, UserSettings.NOT_DISABLED);
        settings.setIsSwipeDisabled(disables_swipe);

        String multiply_disabled = sharedPreferences.getString(UserSettings.IS_MULTIPLY_DISABLED, UserSettings.NO_MULTIPLY_NOT_DISABLED);
        settings.setIsMultiplyDisabled(multiply_disabled);

        String currency = sharedPreferences.getString(UserSettings.CURRENCY, UserSettings.CURRENCY_DOLLAR);
        settings.setCurrency(currency);

        String disablePrice = sharedPreferences.getString(UserSettings.IS_PRICE_DISABLED, UserSettings.NO_PRICE_NOT_DISABLED);
        settings.setIsPriceDisabled(disablePrice);

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

        sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        boolean isWakeLockEnabled = UserSettings.isWakeLockEnabled(this);

        if (isWakeLockEnabled) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }

        String disables_swipe = sharedPreferences.getString(UserSettings.IS_SWIPE_DISABLED, UserSettings.NOT_DISABLED);
        settings.setIsSwipeDisabled(disables_swipe);

        if (settings.getIsSwipeDisabled().equals(UserSettings.YES_DISABLED)) {
            swipeActionSubText1.setText("Disabled");
            swipeActionSubText2.setVisibility(View.GONE);
        } else {
            swipeActionSubText2.setVisibility(View.VISIBLE);
            String leftSwipeAction = sharedPreferences.getString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, UserSettings.SWIPE_SHOW_OPTIONS);
            settings.setCustomLeftSwipeAction(leftSwipeAction);

            String rightSwipeAction = sharedPreferences.getString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, UserSettings.SWIPE_DELETE_ITEM);
            settings.setCustomRightSwipeAction(rightSwipeAction);


            switch (settings.getCustomLeftSwipeAction()) {
                case UserSettings.SWIPE_RENAME:
                    swipeActionSubText1.setText("Rename: swipe left");
                    break;

                case UserSettings.SWIPE_CHECK:
                    swipeActionSubText1.setText("Check & uncheck item: swipe left");
                    break;

                case UserSettings.SWIPE_CHANGE_PRICE:
                    swipeActionSubText1.setText("Change price: swipe left");
                    break;

                case UserSettings.SWIPE_CHANGE_QUANTITY:
                    swipeActionSubText1.setText("Change quantity: swipe left");
                    break;

                case UserSettings.SWIPE_STAR_ITEM:
                    swipeActionSubText1.setText("Star & unstar item: swipe left");
                    break;

                case UserSettings.SWIPE_DELETE_ITEM:
                    swipeActionSubText1.setText("Delete: swipe left");
                    break;

                case UserSettings.SWIPE_SHOW_OPTIONS:
                    swipeActionSubText1.setText("Show options: swipe left");
                    break;
                case UserSettings.SWIPE_DO_NOTHING:
                    swipeActionSubText1.setText("Do nothing: swipe left");
                    break;

            }

            switch (settings.getCustomRightSwipeAction()) {
                case UserSettings.SWIPE_RENAME:
                    swipeActionSubText2.setText("Rename: swipe right");
                    break;

                case UserSettings.SWIPE_CHECK:
                    swipeActionSubText2.setText("Check & uncheck item: swipe right");
                    break;

                case UserSettings.SWIPE_CHANGE_PRICE:
                    swipeActionSubText2.setText("Change price: swipe right");
                    break;

                case UserSettings.SWIPE_CHANGE_QUANTITY:
                    swipeActionSubText2.setText("Change quantity: swipe right");
                    break;

                case UserSettings.SWIPE_STAR_ITEM:
                    swipeActionSubText2.setText("Star & unstar item: swipe right");
                    break;

                case UserSettings.SWIPE_DELETE_ITEM:
                    swipeActionSubText2.setText("Delete: swipe right");
                    break;

                case UserSettings.SWIPE_SHOW_OPTIONS:
                    swipeActionSubText2.setText("Show options: swipe right");
                    break;
                case UserSettings.SWIPE_DO_NOTHING:
                    swipeActionSubText2.setText("Do nothing: swipe right");
                    break;

            }

        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}