package com.henrychinedu.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.henrychinedu.buymate.Database.Firebase;
import com.henrychinedu.buymate.Database.ShopDatabase;
import com.henrychinedu.buymate.Database.UserSettings;
import com.henrychinedu.buymate.NetworkUtils.ConnectivityUtils;

import java.util.List;

public class BackupActivity extends AppCompatActivity {

    private UserSettings settings;

    Toolbar toolbar;
    ShopDatabase db;


    TextView backup_text, restore_text, backup_text_2, restore_text_2;

    CardView backup_card, restore_card;

    ProgressBar progressBar;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

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
        setContentView(R.layout.activity_backup);

        toolbar = findViewById(R.id.backupToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackupActivity.super.onBackPressed();
            }
        });


        db = new ShopDatabase(BackupActivity.this);

        progressBar = findViewById(R.id.progress_bar);

        backup_text = findViewById(R.id.backup_text);
        restore_text = findViewById(R.id.restore_text);

        backup_text_2 = findViewById(R.id.backup_text_2);
        restore_text_2 = findViewById(R.id.restore_text_2);


        backup_card = findViewById(R.id.backup_card);


        backup_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupData();
            }
        });

        restore_card = findViewById(R.id.restore_card);
        restore_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreData();
            }
        });

        loadSharedPreferences();

    }

    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

        }
    };


    public void backupData() {
        progressBar.setVisibility(View.VISIBLE);

        ConnectivityUtils.checkInternetConnectivity(this, new ConnectivityUtils.InternetCheckListener() {
            @Override
            public void onInternetCheckComplete(boolean isInternetAvailable) {
                if (isInternetAvailable) {
                    progressBar.setVisibility(View.GONE);
                    if (settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Firebase firebase = new Firebase(BackupActivity.this);
                            firebase.deleteData();
                            firebase.backupData();

                        } else {
                            showLoginDialog(getString(R.string.BackupActivity__loginBeforeBackup));
                        }

                    } else {
                        showUpgradeRequiredDialog();
                    }
                } else {
                    int code = 1;
                    progressBar.setVisibility(View.GONE);
                    showNoNetworkDialog(code);
                }
            }
        });

    }


    public void restoreData() {

        progressBar.setVisibility(View.VISIBLE);
        ConnectivityUtils.checkInternetConnectivity(this, new ConnectivityUtils.InternetCheckListener() {
            @Override
            public void onInternetCheckComplete(boolean isInternetAvailable) {
                if (isInternetAvailable) {
                    progressBar.setVisibility(View.GONE);
                    if (settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            showRestoreWarningDialog();
                        } else {
                            showLoginDialog(getString(R.string.BackupActivity__loginBeforeRestore));
                        }
                    } else {
                        showUpgradeRequiredDialog();
                    }
                } else {
                    int code = 2;
                    progressBar.setVisibility(View.GONE);
                    showNoNetworkDialog(code);
                }
            }
        });
    }


    public void showNoNetworkDialog(int code) {
        final Dialog dialog = new Dialog(BackupActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.no_connection_layout);

        TextView no_connection_Text1 = dialog.findViewById(R.id.no_connection_text_1);
        TextView no_connection_Text2 = dialog.findViewById(R.id.no_connection_text_2);
        TextView no_connection_Text3 = dialog.findViewById(R.id.no_connection_text_3);
        Button try_again_btn = dialog.findViewById(R.id.try_again);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            no_connection_Text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            no_connection_Text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            no_connection_Text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            try_again_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            no_connection_Text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            no_connection_Text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            no_connection_Text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            try_again_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {

            no_connection_Text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            no_connection_Text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            no_connection_Text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            try_again_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

        }


        try_again_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (code == 1) {
                    backupData();
                } else if (code == 2) {
                    restoreData();
                }
            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showUpgradeRequiredDialog() {
        Dialog dialog = new Dialog(BackupActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upgrade_required_layout);


        TextView header = dialog.findViewById(R.id.upgrade_text_1);
        TextView sub_header = dialog.findViewById(R.id.upgrade_text_2);
        Button cancelBtn = dialog.findViewById(R.id.cancelButton);
        ExtendedFloatingActionButton upgradeBtn = dialog.findViewById(R.id.upgradeBtn);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        upgradeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent upgrade_intent = new Intent(BackupActivity.this, PremiumActivity.class);
                startActivity(upgrade_intent);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void showRestoreWarningDialog() {
        Dialog dialog = new Dialog(BackupActivity.this);
        dialog.setContentView(R.layout.custom_logout_warning_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.header);
        TextView alertText = dialog.findViewById(R.id.alert_text);
        Button backup = dialog.findViewById(R.id.backup);
        TextView okBtn = dialog.findViewById(R.id.okBtn);
        header.setText(getString(R.string.BackupActivity__restoreHeader));
        alertText.setText(getString(R.string.BackupActivity__restoreWarning));
        backup.setText(getString(R.string.BackupActivity__restoreProceedBtn));
        okBtn.setText(getString(R.string.BackupActivity__restoreCancelBtn));

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            alertText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            backup.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            alertText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            backup.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            alertText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            backup.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Firebase firebase = new Firebase(BackupActivity.this);
                firebase.getAllList();
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showLoginDialog(String text) {
        Dialog dialog = new Dialog(BackupActivity.this);
        dialog.setContentView(R.layout.custom_login_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.header);
        TextView loginText = dialog.findViewById(R.id.login_text);
        Button loginBtn = dialog.findViewById(R.id.login_btn);
        TextView cancelBtn = dialog.findViewById(R.id.cancel_btn);

        loginText.setText(text);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            loginText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            loginBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            loginText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            loginBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            loginText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            loginBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(BackupActivity.this, LogInActivity.class));
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateView() {


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            backup_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            restore_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            backup_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            restore_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            backup_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));
            restore_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));
            backup_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            restore_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {

            backup_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_max_text));
            restore_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_max_text));
            backup_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            restore_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
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

        String lifetimePurchased = sharedPreferences.getString(UserSettings.IS_LIFETIME_PURCHASED, UserSettings.NO_LIFETIME_NOT_SUBSCRIBED);
        settings.setIsLifetimePurchased(lifetimePurchased);


        updateView();
    }
}