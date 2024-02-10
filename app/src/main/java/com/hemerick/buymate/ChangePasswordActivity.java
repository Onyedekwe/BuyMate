package com.hemerick.buymate;

import android.app.Dialog;
import android.content.Context;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.NetworkUtils.ConnectivityUtils;

import io.github.muddz.styleabletoast.StyleableToast;

public class ChangePasswordActivity extends AppCompatActivity {

    private UserSettings settings;

    SharedPreferences sharedPreferences;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    TextView forgot_password_text;
    TextInputEditText currentPassword, newPassword, confirmPassword;

    TextInputLayout current_password_parent, new_password_parent, confirm_password_parent;
    Button confirmBtn;

    ProgressBar progressBar;

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

        setContentView(R.layout.activity_change_password);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressBar = findViewById(R.id.progress_bar);

        current_password_parent = findViewById(R.id.current_password_box_parent);
        new_password_parent = findViewById(R.id.new_password_box_parent);
        confirm_password_parent = findViewById(R.id.confirm_password_box_parent);

        currentPassword = findViewById(R.id.current_password_box);
        newPassword = findViewById(R.id.new_password_box);
        confirmPassword = findViewById(R.id.confirm_password_box);

        confirmBtn = findViewById(R.id.confirmBtn);
        forgot_password_text = findViewById(R.id.change_password_description);


        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_password_parent.setErrorEnabled(false);
                new_password_parent.setErrorEnabled(false);
                confirm_password_parent.setErrorEnabled(false);
                changePassword();
            }
        });

        forgot_password_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangePasswordActivity.this, RecoverPasswordActivity.class);
                startActivity(intent);
            }
        });

        loadSharedPreferences();
    }


    public void changePassword() {
        progressBar.setVisibility(View.VISIBLE);
        ConnectivityUtils.checkInternetConnectivity(this, new ConnectivityUtils.InternetCheckListener() {
            @Override
            public void onInternetCheckComplete(boolean isInternetAvailable) {
                if(isInternetAvailable){
                    String current_password = currentPassword.getText().toString().trim();
                    String new_password = newPassword.getText().toString().trim();
                    String confirm_password = confirmPassword.getText().toString().trim();

                    if (!current_password.isEmpty()) {
                        if (!new_password.isEmpty()) {
                            if (!confirm_password.isEmpty()) {
                                if (new_password.length() >= 6) {
                                    if (new_password.equals(confirm_password)) {


                                        new Handler().postDelayed(new Runnable() {

                                            @Override
                                            public void run() {

                                                AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), current_password);
                                                firebaseUser.reauthenticate(credential)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    firebaseUser.updatePassword(new_password)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                                        StyleableToast.makeText(ChangePasswordActivity.this, getString(R.string.ChangePasswordActivity__passwordUpdateSuccess), R.style.custom_toast).show();
                                                                                        ChangePasswordActivity.super.onBackPressed();

                                                                                    } else {
                                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                                        StyleableToast.makeText(ChangePasswordActivity.this, getString(R.string.ChangePasswordActivity__passwordUpdateFailed), R.style.custom_toast).show();

                                                                                    }
                                                                                }
                                                                            });
                                                                } else {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    StyleableToast.makeText(ChangePasswordActivity.this,  getString(R.string.ChangePasswordActivity__error) + task.getException(), R.style.custom_toast).show();

                                                                }
                                                            }
                                                        });

                                            }
                                        }, 2000);

                                    } else {
                                        confirm_password_parent.setError(getString(R.string.ChangePasswordActivity__passwordNotMatched));
                                    }
                                } else {
                                    new_password_parent.setError(getString(R.string.ChangePasswordActivity__passwordLengthFailed));
                                }
                            } else {
                                confirm_password_parent.setError(getString(R.string.ChangePasswordActivity__retypePassword));
                            }
                        } else {
                            new_password_parent.setError(getString(R.string.ChangePasswordActivity__enterNewPassword));
                        }
                    } else {
                        current_password_parent.setError(getString(R.string.ChangePasswordActivity__enterCurrentPassword));
                    }
                }else{
                    progressBar.setVisibility(View.GONE);
                    final Dialog dialog = new Dialog(ChangePasswordActivity.this);
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
                        }
                    });

                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                    dialog.getWindow().setGravity(Gravity.BOTTOM);
                }
            }
        });
    }

    private void updateView() {


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            currentPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            newPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            confirmPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            confirmBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            forgot_password_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            currentPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            newPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            confirmPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            confirmBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            forgot_password_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {

            currentPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            newPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            confirmPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            confirmBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            forgot_password_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

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