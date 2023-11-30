package com.hemerick.buymate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.hemerick.buymate.Database.UserSettings;

import io.github.muddz.styleabletoast.StyleableToast;

public class ChangePasswordActivity extends AppCompatActivity {

    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;

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
        setContentView(R.layout.activity_change_password);

        settings = new UserSettings();
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


        String current_password = currentPassword.getText().toString().trim();
        String new_password = newPassword.getText().toString().trim();
        String confirm_password = confirmPassword.getText().toString().trim();

        if (!current_password.isEmpty()) {
            if (!new_password.isEmpty()) {
                if (!confirm_password.isEmpty()) {
                    if (new_password.length() >= 6) {
                        if (new_password.equals(confirm_password)) {

                            progressBar.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                ;

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
                                                                            StyleableToast.makeText(ChangePasswordActivity.this, "Password updated", R.style.custom_toast).show();
                                                                            ChangePasswordActivity.super.onBackPressed();

                                                                        } else {
                                                                            progressBar.setVisibility(View.INVISIBLE);
                                                                            StyleableToast.makeText(ChangePasswordActivity.this, "Password update failed", R.style.custom_toast).show();

                                                                        }
                                                                    }
                                                                });
                                                    } else {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        StyleableToast.makeText(ChangePasswordActivity.this, "Error: " + task.getException(), R.style.custom_toast).show();

                                                    }
                                                }
                                            });

                                }
                            }, 2000);

                        } else {
                            confirm_password_parent.setError("Password does not match");
                        }
                    } else {
                        new_password_parent.setError("Password must be at least 6 characters long");
                    }
                } else {
                    confirm_password_parent.setError("Retype password to proceed");
                }
            } else {
                new_password_parent.setError("Enter your new password");
            }
        } else {
            current_password_parent.setError("Enter your current password");
        }
    }

    private void updateView() {
        if (settings.getCustomTheme().equals(UserSettings.LIGHT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }

        if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        }

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
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");
            wakeLock.acquire();
        }

        sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);

        String theme = sharedPreferences.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);

        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);


        updateView();
    }

}