package com.hemerick.buymate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.hemerick.buymate.Database.UserSettings;

public class ItemSuggestionActivity extends AppCompatActivity {
    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;

    Toolbar toolbar;
    Switch suggestionDisableSwitch;
    TextView suggestionDescription, suggestionHeader;
    ConstraintLayout suggestionDisableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new UserSettings();
        SharedPreferences sharedPreferences_theme = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String theme = sharedPreferences_theme.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);

        if (settings.getCustomTheme().equals(UserSettings.DIM_THEME)) {
            setTheme(R.style.Dynamic_Dim);
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
        suggestionDescription = findViewById(R.id.suggestion_description);
        suggestionHeader = findViewById(R.id.suggestionDisableTextHeader);

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
            suggestionDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            suggestionHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            suggestionDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            suggestionHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            suggestionDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        suggestionDisableSwitch.setChecked(settings.getIsSuggestionDisabled().equals(UserSettings.YES_SUGGESTION_DISABLED));


    }

    private void loadSharedPreferences() {

        boolean wakeLockEnabled = UserSettings.isWakeLockEnabled(this);
        if (wakeLockEnabled) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");
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