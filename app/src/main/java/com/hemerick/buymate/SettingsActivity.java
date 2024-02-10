package com.hemerick.buymate;

import android.app.Dialog;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

    TextView generalText, advancedText, securityText, removeAdsText, removeAdsSubText, darkModeText, keepScreenBrightText, textSizeText, lockAppText, swipeActionText, swipeActionSubText1, swipeActionSubText2, detailedListShareText, detailedListShareSubText, disablePriceText, multiplyText, multiplySubText, strikeText, currencyText, currencySubText, itemSuggestionText;
    Toolbar toolbar;
    SharedPreferences sharedPreferences;
    private SwitchCompat keepScreenBrightSwitch, disablePriceSwitch, multiplySwitch;
    private ConstraintLayout DarkModeLayout, RemoveAdsLayout, TextSizeLayout, SwipeActionLayout, DetailedSharingLayout, DisablePriceLayout, MultiplyLayout, StrikeLayout, CurrencyLayout, AppLockLayout, ItemSuggestionLayout;
    private UserSettings settings = new UserSettings();
    private PowerManager.WakeLock wakeLock;

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

        setContentView(R.layout.activity_settings);


        toolbar = findViewById(R.id.settingToolbar);


        DarkModeLayout = findViewById(R.id.themeLayout);
        RemoveAdsLayout = findViewById(R.id.RemoveAdsLayout);
        TextSizeLayout = findViewById(R.id.textSizeLayout);
        generalText = findViewById(R.id.generalHeaderText);
        advancedText = findViewById(R.id.advancedHeaderText);
        securityText = findViewById(R.id.securityHeaderText);
        removeAdsText = findViewById(R.id.removeAdsText);
        removeAdsSubText = findViewById(R.id.removeAdsSubText);
        darkModeText = findViewById(R.id.themeText);
        keepScreenBrightText = findViewById(R.id.screenText);
        detailedListShareText = findViewById(R.id.detailedListShareTextHeader);
        detailedListShareSubText = findViewById(R.id.detailedListShareSubText);
        textSizeText = findViewById(R.id.textSizeText);
        lockAppText = findViewById(R.id.lockText);
        swipeActionText = findViewById(R.id.swipeActionText);
        swipeActionSubText1 = findViewById(R.id.swipeActionSubText1);
        swipeActionSubText2 = findViewById(R.id.swipeActionSubText2);
        keepScreenBrightSwitch = findViewById(R.id.keepScreenBrightSwitcher);
        multiplySwitch = findViewById(R.id.multiplySwitcher);
        disablePriceSwitch = findViewById(R.id.disablePriceSwitcher);
        SwipeActionLayout = findViewById(R.id.swipeActionLayout);
        DetailedSharingLayout = findViewById(R.id.detailedListShareLayout);
        MultiplyLayout = findViewById(R.id.multiplyLayout);
        StrikeLayout = findViewById(R.id.StrikeLayout);
        multiplyText = findViewById(R.id.multiplyText);
        multiplySubText = findViewById(R.id.multiplySubText);
        disablePriceText = findViewById(R.id.disable_price_Text);
        DisablePriceLayout = findViewById(R.id.disablePriceLayout);
        itemSuggestionText = findViewById(R.id.item_suggestionText);

        strikeText = findViewById(R.id.strikeText);
        currencyText = findViewById(R.id.currencyTextHeader);
        currencySubText = findViewById(R.id.currencySubText);
        CurrencyLayout = findViewById(R.id.currencyLayout);
        AppLockLayout = findViewById(R.id.lockLayout);
        ItemSuggestionLayout = findViewById(R.id.itemSuggestionLayout);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        ItemSuggestionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ItemSuggestionActivity.class);
                startActivity(intent);
            }
        });

        RemoveAdsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, PremiumActivity.class);
                startActivity(intent);
            }
        });


        DarkModeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDarkThemeDialog();
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


    public void showDarkThemeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.set_theme_layout);

        TextView header = dialog.findViewById(R.id.title);

        RadioButton on = dialog.findViewById(R.id.radio_button_on);
        RadioButton off = dialog.findViewById(R.id.radio_button_off);
        RadioButton as_on_device = dialog.findViewById(R.id.radio_button_default);

        ConstraintLayout dim_layout = dialog.findViewById(R.id.dim_layout);
        TextView dim_text = dialog.findViewById(R.id.dimText);
        SwitchCompat dim_switch = dialog.findViewById(R.id.dimSwitcher);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            on.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            off.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            as_on_device.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            dim_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            on.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            off.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            as_on_device.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            dim_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {

            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            on.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            off.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            as_on_device.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            dim_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

        }

        if (settings.getCustomTheme().equals(UserSettings.DEFAULT_THEME)) {
            as_on_device.setChecked(true);
        }
        if (settings.getCustomTheme().equals(UserSettings.LIGHT_THEME)) {
            off.setChecked(true);
        }
        if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {
            on.setChecked(true);
        }

        if (settings.getIsDimThemeEnabled().equals(UserSettings.YES_DIM_THEME_ENABLED)) {
            dim_switch.setChecked(true);
        }


        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomTheme(UserSettings.DARK_THEME);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_THEME, settings.getCustomTheme());
                editor.apply();
                dialog.dismiss();
                Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }

        });

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomTheme(UserSettings.LIGHT_THEME);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_THEME, settings.getCustomTheme());
                editor.apply();
                dialog.dismiss();
                Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        as_on_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomTheme(UserSettings.DEFAULT_THEME);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_THEME, settings.getCustomTheme());
                editor.apply();
                dialog.dismiss();
                Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });


        dim_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings.setIsDimThemeEnabled(UserSettings.YES_DIM_THEME_ENABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_DIM_THEME_ENABLED, settings.getIsDimThemeEnabled());
                    editor.apply();
                    dialog.dismiss();
                    if(!off.isChecked()){
                        recreate();
                    }


                } else {
                    settings.setIsDimThemeEnabled(UserSettings.NO_DIM_THEME_NOT_ENABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_DIM_THEME_ENABLED, settings.getIsDimThemeEnabled());
                    editor.apply();
                    dialog.dismiss();
                    if(!off.isChecked()){
                        recreate();
                    }

                }

            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
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

            generalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            advancedText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            securityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            removeAdsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            removeAdsSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            multiplyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            multiplySubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
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
            itemSuggestionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            generalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            advancedText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            securityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            removeAdsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            removeAdsSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            multiplyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            multiplySubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));

            disablePriceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            strikeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            darkModeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            keepScreenBrightText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            textSizeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            lockAppText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            swipeActionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            detailedListShareText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            currencyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            itemSuggestionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            currencySubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            detailedListShareSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            swipeActionSubText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            swipeActionSubText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            generalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            advancedText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            securityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            removeAdsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            removeAdsSubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            multiplyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            multiplySubText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            disablePriceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            strikeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            darkModeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            keepScreenBrightText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            textSizeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            lockAppText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            swipeActionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            detailedListShareText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            itemSuggestionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
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

        if (settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)) {
            RemoveAdsLayout.setVisibility(View.GONE);
        }

        multiplySwitch.setChecked(settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED));

        disablePriceSwitch.setChecked(!settings.getIsPriceDisabled().equals(UserSettings.YES_PRICE_DISABLED));
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

        String dim = sharedPreferences.getString(UserSettings.IS_DIM_THEME_ENABLED, UserSettings.NO_DIM_THEME_NOT_ENABLED);
        settings.setIsDimThemeEnabled(dim);


        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        String leftSwipeAction = sharedPreferences.getString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, UserSettings.SWIPE_SHOW_OPTIONS);
        settings.setCustomLeftSwipeAction(leftSwipeAction);

        String rightSwipeAction = sharedPreferences.getString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, UserSettings.SWIPE_DELETE_ITEM);
        settings.setCustomRightSwipeAction(rightSwipeAction);

        String disables_swipe = sharedPreferences.getString(UserSettings.IS_SWIPE_DISABLED, UserSettings.NOT_DISABLED);
        settings.setIsSwipeDisabled(disables_swipe);

        String multiply_disabled = sharedPreferences.getString(UserSettings.IS_MULTIPLY_DISABLED, UserSettings.YES_MULTIPLY_DISABLED);
        settings.setIsMultiplyDisabled(multiply_disabled);

        String currency = sharedPreferences.getString(UserSettings.CURRENCY, UserSettings.CURRENCY_DOLLAR);
        settings.setCurrency(currency);

        String disablePrice = sharedPreferences.getString(UserSettings.IS_PRICE_DISABLED, UserSettings.NO_PRICE_NOT_DISABLED);
        settings.setIsPriceDisabled(disablePrice);

        String lifetimePurchased = sharedPreferences.getString(UserSettings.IS_LIFETIME_PURCHASED, UserSettings.NO_LIFETIME_NOT_SUBSCRIBED);
        settings.setIsLifetimePurchased(lifetimePurchased);

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
        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
        finish();
    }

}