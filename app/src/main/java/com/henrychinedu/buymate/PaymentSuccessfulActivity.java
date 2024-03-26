package com.henrychinedu.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.henrychinedu.buymate.Database.UserSettings;

public class PaymentSuccessfulActivity extends AppCompatActivity {

    private UserSettings settings;
    TextView header, header_text_1, header_text_2, header_text_4, header_text_5;

    CardView backupCard, adsCard, supportCard;
    Button backBtn;

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
        setContentView(R.layout.activity_payment_successful);


        header = findViewById(R.id.header);
        header_text_1 = findViewById(R.id.header_desc_1);
        header_text_2 = findViewById(R.id.header_desc_2);
        header_text_4 = findViewById(R.id.header_desc_4);
        header_text_5 = findViewById(R.id.header_desc_5);

        backupCard = findViewById(R.id.backup_card);
        adsCard = findViewById(R.id.ads_card);
        supportCard = findViewById(R.id.support_card);

        backupCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBackupDialog();
            }
        });

        adsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveAdsDialog();
            }
        });

        supportCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSupportDialog();
            }
        });

        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentHome = new Intent(PaymentSuccessfulActivity.this, HomeActivity.class);
                startActivity(intentHome);
                finish();
            }
        });

        loadSharedPreferences();

    }


    public void showBackupDialog() {
        Dialog dialog = new Dialog(PaymentSuccessfulActivity.this);
        dialog.setContentView(R.layout.custom_premium_backup_description_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.header);
        TextView sub_header = dialog.findViewById(R.id.sub_header);
        Button okBtn = dialog.findViewById(R.id.okBtn);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showRemoveAdsDialog() {
        Dialog dialog = new Dialog(PaymentSuccessfulActivity.this);
        dialog.setContentView(R.layout.custom_premium_remove_ads_description_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.header);
        TextView sub_header = dialog.findViewById(R.id.sub_header);
        Button okBtn = dialog.findViewById(R.id.okBtn);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    public void showSupportDialog() {
        Dialog dialog = new Dialog(PaymentSuccessfulActivity.this);
        dialog.setContentView(R.layout.custom_premium_support_description_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.header);
        TextView sub_header = dialog.findViewById(R.id.sub_header);
        Button okBtn = dialog.findViewById(R.id.okBtn);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void updateView() {

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            header_text_1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            header_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            header_text_4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            header_text_5.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            backBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));


        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            header_text_1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            header_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            header_text_4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            header_text_5.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            backBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {

            header_text_1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            header_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            header_text_4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            header_text_5.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            backBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_max_text));

        }
    }

    private void loadSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);

        String theme = sharedPreferences.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);

        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        updateView();
    }
}