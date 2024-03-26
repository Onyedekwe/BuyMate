package com.henrychinedu.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.henrychinedu.buymate.Database.ShopDatabase;
import com.henrychinedu.buymate.Database.UserSettings;

import io.github.muddz.styleabletoast.StyleableToast;

public class ItemSuggestionActivity extends AppCompatActivity {
    private UserSettings settings;

    ShopDatabase db;

    Toolbar toolbar;
    SwitchCompat suggestionDisableSwitch;
    TextView suggestionHeader, suggestionResetHeader;
    ConstraintLayout suggestionDisableLayout, suggestionResetLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new ShopDatabase(this);

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

        setContentView(R.layout.activity_item_suggestion);


        toolbar = findViewById(R.id.suggestionToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemSuggestionActivity.super.onBackPressed();
            }
        });

        suggestionDisableSwitch = findViewById(R.id.disable_suggestion_switch);
        suggestionDisableLayout = findViewById(R.id.suggestionDisableLayout);
        suggestionHeader = findViewById(R.id.suggestionDisableTextHeader);

        suggestionResetLayout = findViewById(R.id.suggestionResetLayout);
        suggestionResetHeader = findViewById(R.id.suggestionResetTextHeader);

        suggestionResetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSuggestionResetDialog();
            }
        });

        suggestionDisableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestionDisableSwitch.setChecked(!suggestionDisableSwitch.isChecked());
            }
        });
        suggestionDisableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings.setIsSuggestionDisabled(UserSettings.YES_SUGGESTION_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_SUGGESTION_DISABLED, settings.getIsSuggestionDisabled());
                    editor.apply();
                } else {
                    settings.setIsSuggestionDisabled(UserSettings.NO_SUGGESTION_NOT_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_SUGGESTION_DISABLED, settings.getIsSuggestionDisabled());
                    editor.apply();
                }
            }
        });


        loadSharedPreferences();
    }

    private void updateView() {

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            suggestionHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            suggestionResetHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            suggestionHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            suggestionResetHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            suggestionHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            suggestionResetHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

        }


        suggestionDisableSwitch.setChecked(settings.getIsSuggestionDisabled().equals(UserSettings.YES_SUGGESTION_DISABLED));


    }

    public void showSuggestionResetDialog() {
        Dialog dialog = new Dialog(ItemSuggestionActivity.this);
        dialog.setContentView(R.layout.custom_suggestion_reset_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.header);
        TextView reset_suggestion_text = dialog.findViewById(R.id.delete_account_text);
        Button cancelBtn = dialog.findViewById(R.id.CancelButton);
        Button resetBtn = dialog.findViewById(R.id.BtnSave);
        ProgressBar progressBar = dialog.findViewById(R.id.progress_bar);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            reset_suggestion_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            resetBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            reset_suggestion_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            resetBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            reset_suggestion_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            resetBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                db.deleteAllSuggest();
                db.deleteAllSuggestUnit();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String[] suggestion_list = getResources().getStringArray(R.array.item_suggestions);
                        String[] unit_list = getResources().getStringArray(R.array.units);
                        for (String s : suggestion_list) {
                            db.insertSuggest(s);
                        }
                        for (String su : unit_list) {
                            db.insertSuggestUnit(su);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        StyleableToast.makeText(ItemSuggestionActivity.this, getString(R.string.ItemSuggestionActivity__success), R.style.custom_toast_2).show();
                        dialog.dismiss();
                    }
                }, 3000);

            }
        });

        dialog.show();
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

        String suggestion = sharedPreferences.getString(UserSettings.IS_SUGGESTION_DISABLED, UserSettings.NO_SUGGESTION_NOT_DISABLED);
        settings.setIsSuggestionDisabled(suggestion);

        updateView();
    }
}