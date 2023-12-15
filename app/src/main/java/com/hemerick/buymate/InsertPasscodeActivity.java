package com.hemerick.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.chaos.view.PinView;
import com.hemerick.buymate.Database.UserSettings;

import java.util.concurrent.Executor;

import io.github.muddz.styleabletoast.StyleableToast;

public class InsertPasscodeActivity extends AppCompatActivity {

    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;


    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    TextView welcome_header, subHeader, finger_print_header, fingerPrintSubHeader;

    Button signInBtn;

    ImageView fingerPrintButton;


    PinView pinView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_passcode);

        settings = new UserSettings();

        welcome_header = findViewById(R.id.header);
        subHeader = findViewById(R.id.sub_header);
        pinView = findViewById(R.id.password_signin_pin);
        finger_print_header = findViewById(R.id.fingerprint_header);
        fingerPrintSubHeader = findViewById(R.id.fingerprint_subheader);
        fingerPrintButton = findViewById(R.id.fingerPrintImage);
        loadSharedPreferences();


        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        pinView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String text = pinView.getText().toString().trim();
                if (!(text.length() < 4)) {
                    inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    if (settings.getPassword().equals(text.trim())) {
                        StyleableToast.makeText(InsertPasscodeActivity.this, "Success", R.style.custom_toast).show();
                        Intent intent = new Intent(InsertPasscodeActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        StyleableToast.makeText(InsertPasscodeActivity.this, "Wrong pin", R.style.custom_toast_2).show();
                    }
                } else {
                    StyleableToast.makeText(InsertPasscodeActivity.this, "Insert 4 digit password", R.style.custom_toast_2).show();
                }

                return true;
            }
        });

        pinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 4) {
                    String text = pinView.getText().toString().trim();


                    inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    if (settings.getPassword().equals(text.trim())) {
                        StyleableToast.makeText(InsertPasscodeActivity.this, "Success", R.style.custom_toast).show();
                        Intent intent = new Intent(InsertPasscodeActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        StyleableToast.makeText(InsertPasscodeActivity.this, "Wrong pin", R.style.custom_toast_2).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        fingerPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settings.getIsFingerPrintDisabled().equals(UserSettings.YES_FINGERPRINT_DISABLED)) {
                    Dialog dialog = new Dialog(InsertPasscodeActivity.this);
                    dialog.setContentView(R.layout.custom_alert_fingerprint_dialog);
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    TextView header = dialog.findViewById(R.id.header);
                    TextView alertText = dialog.findViewById(R.id.alert_text);
                    Button okBtn = dialog.findViewById(R.id.okBtn);


                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                        header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        alertText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                        header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        alertText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                        header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        alertText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                    }

                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                } else {
                    BiometricManager biometricManager = BiometricManager.from(InsertPasscodeActivity.this);

                    switch (biometricManager.canAuthenticate()) {
                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            StyleableToast.makeText(InsertPasscodeActivity.this, "Device Not Supported", R.style.custom_toast).show();
                            break;

                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            StyleableToast.makeText(InsertPasscodeActivity.this, "Hardware Unavailable", R.style.custom_toast).show();
                            break;

                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            StyleableToast.makeText(InsertPasscodeActivity.this, "Fingerprint not set for this device", R.style.custom_toast).show();
                            break;

                        case BiometricManager.BIOMETRIC_SUCCESS:
                            break;

                    }

                    executor = ContextCompat.getMainExecutor(InsertPasscodeActivity.this);
                    biometricPrompt = new BiometricPrompt(InsertPasscodeActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {

                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);

                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            StyleableToast.makeText(InsertPasscodeActivity.this, "Success", R.style.custom_toast).show();
                            Intent intent = new Intent(InsertPasscodeActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();

                        }
                    });

                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Verify fingerprint")
                            .setNegativeButtonText("Use password")
                            .build();
                    biometricPrompt.authenticate(promptInfo);
                }
            }
        });
    }

    private void updateView() {

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            welcome_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            finger_print_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            fingerPrintSubHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            welcome_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            finger_print_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            fingerPrintSubHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            welcome_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            finger_print_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            fingerPrintSubHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

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

        String password = sharedPreferences.getString(UserSettings.PASSWORD, UserSettings.NOT_SET_PASSWORD);
        settings.setPassword(password);

        String fingerprint = sharedPreferences.getString(UserSettings.IS_FINGERPRINT_DISABLED, UserSettings.YES_FINGERPRINT_DISABLED);
        settings.setIsFingerPrintDisabled(fingerprint);

        updateView();
    }
}