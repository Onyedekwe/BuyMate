package com.henrychinedu.buymate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.henrychinedu.buymate.Database.UserSettings;

public class AcknowledgmentsActivity extends AppCompatActivity {
    private UserSettings settings;
    SharedPreferences sharedPreferences;
    Toolbar ack_toolbar;
    TextView ack_1_text, ack_2_text, ack_3_text, ack_4_text, ack_5_text, ack_6_text, ack_7_text, ack_8_text, ack_9_text, licence_text;
    ConstraintLayout ack_1_layout, ack_2_layout, ack_3_layout, ack_4_layout, ack_5_layout, ack_6_layout, ack_7_layout, ack_8_layout, ack_9_layout, licence_layout;

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

        setContentView(R.layout.activity_acknowledgments);


        ack_toolbar = findViewById(R.id.ackToolbar);
        ack_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ack_1_text = findViewById(R.id.ack_1_text);
        ack_2_text = findViewById(R.id.ack_2_text);
        ack_3_text = findViewById(R.id.ack_3_text);
        ack_4_text = findViewById(R.id.ack_4_text);
        ack_5_text = findViewById(R.id.ack_5_text);
        ack_6_text = findViewById(R.id.ack_6_text);
        ack_7_text = findViewById(R.id.ack_7_text);
        ack_8_text = findViewById(R.id.ack_8_text);
        ack_9_text = findViewById(R.id.ack_9_text);
        licence_text = findViewById(R.id.licence_text);


        ack_1_layout = findViewById(R.id.ack_1);
        ack_2_layout = findViewById(R.id.ack_2);
        ack_3_layout = findViewById(R.id.ack_3);
        ack_4_layout = findViewById(R.id.ack_4);
        ack_5_layout = findViewById(R.id.ack_5);
        ack_6_layout = findViewById(R.id.ack_6);
        ack_7_layout = findViewById(R.id.ack_7);
        ack_8_layout = findViewById(R.id.ack_8);
        ack_9_layout = findViewById(R.id.ack_9);
        licence_layout = findViewById(R.id.licence_layout);

        ack_1_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://github.com/koral--/android-gif-drawable");
            }
        });

        ack_2_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://github.com/Muddz/StyleableToast");
            }
        });

        ack_3_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://github.com/ChaosLeung/PinView");
            }
        });

        ack_4_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://github.com/square/picasso");
            }
        });

        ack_5_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://github.com/hdodenhof/CircleImageView");
            }
        });

        ack_6_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout");
            }
        });

        ack_7_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://firebase.google.com/docs/crashlytics/get-started?platform=android");
            }
        });

        ack_8_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://undraw.co/");
            }
        });

        ack_9_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://www.magicoon.com/");
            }
        });

        licence_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://www.apache.org/licenses/LICENSE-2.0.txt");
            }
        });


        loadSharedPreferences();

    }


    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private void updateView() {

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            ack_1_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            ack_2_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            ack_3_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            ack_4_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            ack_5_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            ack_6_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            ack_7_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            ack_8_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            ack_9_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            licence_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));


        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {


            ack_1_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            ack_2_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            ack_3_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            ack_4_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            ack_5_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            ack_6_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            ack_7_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            ack_8_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            ack_9_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            licence_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));


        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {

            ack_1_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            ack_2_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            ack_3_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            ack_4_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            ack_5_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            ack_6_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            ack_7_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            ack_8_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            ack_9_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            licence_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

        }

    }

    private void loadSharedPreferences() {

        boolean wakeLockEnabled = UserSettings.isWakeLockEnabled(this);
        if (wakeLockEnabled) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");
            wakeLock.acquire();
        }

        sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);


        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        updateView();
    }
}