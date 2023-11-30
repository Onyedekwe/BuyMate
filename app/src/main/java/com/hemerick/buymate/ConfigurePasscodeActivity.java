package com.hemerick.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.chaos.view.PinView;
import com.hemerick.buymate.Database.UserSettings;

import java.util.concurrent.Executor;

import io.github.muddz.styleabletoast.StyleableToast;

public class ConfigurePasscodeActivity extends AppCompatActivity {

    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;
    Toolbar toolbar;

    boolean allowUncheckMethod = false;
    boolean allowCheckMethod = false;

    ConstraintLayout passwordLayout, removePasswordLayout;
    Switch fingerprintSwitcher;
    TextView passwordText, fingerPrintText, removePasswordText;

    boolean fingerprintSet = false;

    SharedPreferences sharedPreferences;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_passcode);

        settings = new UserSettings();


        toolbar = findViewById(R.id.app_lockToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfigurePasscodeActivity.super.onBackPressed();
            }
        });

        passwordLayout = findViewById(R.id.passwordLayout);
        removePasswordLayout = findViewById(R.id.remove_passwordLayout);
        passwordText = findViewById(R.id.passwordTextHeader);
        fingerPrintText = findViewById(R.id.fingerprint_TextHeader);
        fingerprintSwitcher = findViewById(R.id.fingerprint_switch);
        removePasswordText = findViewById(R.id.remove_passwordTextHeader);


        removePasswordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean remove = true;
                update_passcode_Dialog(remove);
            }
        });

        passwordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean remove = false;
                if (settings.getPassword().equals(UserSettings.NOT_SET_PASSWORD)) {
                    input_passcode_Dialog();

                } else {
                    update_passcode_Dialog(remove);
                }
            }
        });




        fingerprintSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (fingerprintSwitcher.isChecked()) {
                    if(allowCheckMethod){

                    if (settings.getPassword().equals(UserSettings.NOT_SET_PASSWORD)) {
                        input_fingerSet_passcode_Dialog();
                    } else {
                        BiometricManager biometricManager = BiometricManager.from(ConfigurePasscodeActivity.this);

                        switch (biometricManager.canAuthenticate()) {
                            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                                StyleableToast.makeText(ConfigurePasscodeActivity.this, "Device Not Supported", R.style.custom_toast).show();
                                allowUncheckMethod = false;
                                fingerprintSwitcher.setChecked(false);
                                break;

                            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                                StyleableToast.makeText(ConfigurePasscodeActivity.this, "Hardware Unavailable", R.style.custom_toast).show();
                                allowUncheckMethod = false;
                                fingerprintSwitcher.setChecked(false);
                                break;

                            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                                StyleableToast.makeText(ConfigurePasscodeActivity.this, "Fingerprint not set for this device", R.style.custom_toast).show();
                                allowUncheckMethod = false;
                                fingerprintSwitcher.setChecked(false);
                                break;

                            case BiometricManager.BIOMETRIC_SUCCESS:
                                break;

                        }

                        executor = ContextCompat.getMainExecutor(ConfigurePasscodeActivity.this);
                        biometricPrompt = new BiometricPrompt(ConfigurePasscodeActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {

                            @Override
                            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                                super.onAuthenticationError(errorCode, errString);
                                allowUncheckMethod = false;
                                fingerprintSwitcher.setChecked(false);
                            }

                            @Override
                            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                                super.onAuthenticationSucceeded(result);
                                settings.setIsFingerPrintDisabled(UserSettings.NO_FINGERPRINT_NOT_DISABLED);
                                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                                editor.putString(UserSettings.IS_FINGERPRINT_DISABLED, settings.getIsFingerPrintDisabled());
                                editor.apply();
                                allowUncheckMethod = true;
                                allowCheckMethod = false;
                            }

                            @Override
                            public void onAuthenticationFailed() {
                                super.onAuthenticationFailed();
                                allowUncheckMethod = false;
                                fingerprintSwitcher.setChecked(false);

                            }
                        });

                        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Verify fingerprint")
                                .setNegativeButtonText("Use password")
                                .build();
                        biometricPrompt.authenticate(promptInfo);
                    }

                    }

                } else {

                    if(allowUncheckMethod == true){

                        BiometricManager biometricManager = BiometricManager.from(ConfigurePasscodeActivity.this);

                    switch (biometricManager.canAuthenticate()) {
                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            StyleableToast.makeText(ConfigurePasscodeActivity.this, "Device Not Supported", R.style.custom_toast).show();
                            allowUncheckMethod = false;
                            fingerprintSwitcher.setChecked(false);
                            allowUncheckMethod = true;
                            break;

                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            StyleableToast.makeText(ConfigurePasscodeActivity.this, "Hardware Unavailable", R.style.custom_toast).show();
                            allowUncheckMethod = false;
                            fingerprintSwitcher.setChecked(false);
                            allowUncheckMethod = true;
                            break;

                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            StyleableToast.makeText(ConfigurePasscodeActivity.this, "Fingerprint not set for this device", R.style.custom_toast).show();
                            allowUncheckMethod = false;
                            fingerprintSwitcher.setChecked(false);
                            allowUncheckMethod = true;
                            break;

                        case BiometricManager.BIOMETRIC_SUCCESS:
                            break;

                    }

                    executor = ContextCompat.getMainExecutor(ConfigurePasscodeActivity.this);
                    biometricPrompt = new BiometricPrompt(ConfigurePasscodeActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {

                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            allowUncheckMethod = false;
                            fingerprintSwitcher.setChecked(true);
                            allowUncheckMethod = true;

                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            settings.setIsFingerPrintDisabled(UserSettings.YES_FINGERPRINT_DISABLED);
                            SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                            editor.putString(UserSettings.IS_FINGERPRINT_DISABLED, settings.getIsFingerPrintDisabled());
                            editor.apply();
                            fingerprintSwitcher.setChecked(false);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            allowUncheckMethod = false;
                            fingerprintSwitcher.setChecked(true);
                            allowUncheckMethod = true;
                        }
                    });

                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Verify fingerprint")
                            .setNegativeButtonText("Use password")
                            .build();
                    biometricPrompt.authenticate(promptInfo);
                }

                }
            }
        });


        loadSharedPreferences();

        if(fingerprintSwitcher.isChecked()){
            allowUncheckMethod = true;
        }else{
            allowCheckMethod = true;
        }
    }


    public void input_passcode_Dialog() {
        final Dialog dialog = new Dialog(ConfigurePasscodeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.set_password_layout);


        TextView Header = dialog.findViewById(R.id.header);
        TextView subHeader = dialog.findViewById(R.id.sub_header);
        PinView pinView = dialog.findViewById(R.id.password_pin);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        pinView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String text = pinView.getText().toString().trim();
                if (!(text.length() < 4)) {
                    dialog.dismiss();
                    inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    confirm_passcode_Dialog(text);
                } else {
                    StyleableToast.makeText(ConfigurePasscodeActivity.this, "Insert 4 digit password", R.style.custom_toast_2).show();
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
                    dialog.dismiss();
                    inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    confirm_passcode_Dialog(text);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pinView.requestFocus();
                        inputMethodManager.showSoftInput(pinView, InputMethodManager.SHOW_IMPLICIT);
                    }
                }, 400);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void confirm_passcode_Dialog(String password) {
        final Dialog dialog = new Dialog(ConfigurePasscodeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.set_password_layout);


        TextView Header = dialog.findViewById(R.id.header);
        Header.setText("Confirm Password");
        TextView subHeader = dialog.findViewById(R.id.sub_header);
        subHeader.setText("Re-insert password to proceed");
        PinView pinView = dialog.findViewById(R.id.password_pin);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        pinView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String text2 = pinView.getText().toString().trim();

                if (!(text2.length() < 4)) {
                    if (text2.trim().equals(password.trim())) {
                        dialog.dismiss();
                        inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        settings.setPassword(text2.trim());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(UserSettings.PASSWORD, settings.getPassword());
                        editor.apply();
                        StyleableToast.makeText(ConfigurePasscodeActivity.this, "Success", R.style.custom_toast).show();
                        removePasswordLayout.setVisibility(View.VISIBLE);
                    } else {
                        StyleableToast.makeText(ConfigurePasscodeActivity.this, "Password does not match", R.style.custom_toast_2).show();
                    }
                } else {
                    StyleableToast.makeText(ConfigurePasscodeActivity.this, "Insert 4 digit password", R.style.custom_toast_2).show();
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
                    if (text.trim().equals(password.trim())) {
                        dialog.dismiss();
                        inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);

                        settings.setPassword(text.trim());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(UserSettings.PASSWORD, settings.getPassword());
                        editor.apply();
                        removePasswordLayout.setVisibility(View.VISIBLE);

                    } else {
                        StyleableToast.makeText(ConfigurePasscodeActivity.this, "Password does not match", R.style.custom_toast_2).show();
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pinView.requestFocus();
                        inputMethodManager.showSoftInput(pinView, InputMethodManager.SHOW_IMPLICIT);
                    }
                }, 400);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    public void update_passcode_Dialog(Boolean remove) {
        final Dialog dialog = new Dialog(ConfigurePasscodeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.set_password_layout);


        TextView Header = dialog.findViewById(R.id.header);
        Header.setText("Insert Password");
        TextView subHeader = dialog.findViewById(R.id.sub_header);
        subHeader.setText("Help us verify that it is actually you, insert your current password");
        PinView pinView = dialog.findViewById(R.id.password_pin);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        pinView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String text = pinView.getText().toString().trim();
                if (!(text.length() < 4)) {

                    if (settings.getPassword().equals(text.trim())) {
                        if (remove.equals(true)) {
                            settings.setPassword(UserSettings.NOT_SET_PASSWORD);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(UserSettings.PASSWORD, settings.getPassword());
                            editor.apply();
                            settings.setIsFingerPrintDisabled(UserSettings.YES_FINGERPRINT_DISABLED);
                            editor.putString(UserSettings.IS_FINGERPRINT_DISABLED, settings.getIsFingerPrintDisabled());
                            editor.apply();
                            removePasswordLayout.setVisibility(View.GONE);
                            StyleableToast.makeText(ConfigurePasscodeActivity.this, "App lock removed", R.style.custom_toast).show();
                            ConfigurePasscodeActivity.super.onBackPressed();
                            dialog.dismiss();
                            inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);

                        } else {
                            dialog.dismiss();
                            inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);
                            input_passcode_Dialog();
                        }
                    } else {
                        StyleableToast.makeText(ConfigurePasscodeActivity.this, "Wrong Password", R.style.custom_toast_2).show();
                    }

                } else {
                    StyleableToast.makeText(ConfigurePasscodeActivity.this, "Insert 4 digit password", R.style.custom_toast_2).show();
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

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                pinView.requestFocus();
                inputMethodManager.showSoftInput(pinView, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    public void input_fingerSet_passcode_Dialog() {
        final Dialog dialog = new Dialog(ConfigurePasscodeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.set_password_layout);


        TextView Header = dialog.findViewById(R.id.header);
        TextView subHeader = dialog.findViewById(R.id.sub_header);
        PinView pinView = dialog.findViewById(R.id.password_pin);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        pinView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String text = pinView.getText().toString().trim();
                if (!(text.length() < 4)) {
                    dialog.dismiss();
                    inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    confirm_fingerSet_passcode_Dialog(text);
                } else {
                    StyleableToast.makeText(ConfigurePasscodeActivity.this, "Insert 4 digit password", R.style.custom_toast_2).show();
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
                    dialog.dismiss();
                    inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    confirm_fingerSet_passcode_Dialog(text);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pinView.requestFocus();
                        inputMethodManager.showSoftInput(pinView, InputMethodManager.SHOW_IMPLICIT);
                    }
                }, 400);
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                fingerprintSwitcher.setChecked(fingerprintSet);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void confirm_fingerSet_passcode_Dialog(String password) {
        final Dialog dialog = new Dialog(ConfigurePasscodeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.set_password_layout);


        TextView Header = dialog.findViewById(R.id.header);
        Header.setText("Confirm Password");
        TextView subHeader = dialog.findViewById(R.id.sub_header);
        subHeader.setText("Re-insert password to proceed");
        PinView pinView = dialog.findViewById(R.id.password_pin);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        pinView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String text2 = pinView.getText().toString().trim();

                if (!(text2.length() < 4)) {
                    if (text2.trim().equals(password.trim())) {

                        inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        settings.setPassword(text2.trim());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(UserSettings.PASSWORD, settings.getPassword());
                        editor.apply();
                        settings.setIsFingerPrintDisabled(UserSettings.NO_FINGERPRINT_NOT_DISABLED);
                        editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                        editor.putString(UserSettings.IS_FINGERPRINT_DISABLED, settings.getIsFingerPrintDisabled());
                        editor.apply();
                        StyleableToast.makeText(ConfigurePasscodeActivity.this, "Fingerprint Enabled", R.style.custom_toast).show();
                        fingerprintSet = true;
                        fingerprintSwitcher.setChecked(true);
                        dialog.dismiss();
                        removePasswordLayout.setVisibility(View.VISIBLE);
                    } else {
                        StyleableToast.makeText(ConfigurePasscodeActivity.this, "Password does not match", R.style.custom_toast_2).show();
                    }
                } else {
                    StyleableToast.makeText(ConfigurePasscodeActivity.this, "Insert 4 digit password", R.style.custom_toast_2).show();
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
                    if (text.trim().equals(password.trim())) {

                        inputMethodManager.showSoftInput(pinView, InputMethodManager.HIDE_IMPLICIT_ONLY);

                        settings.setPassword(text.trim());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(UserSettings.PASSWORD, settings.getPassword());
                        editor.apply();

                        settings.setIsFingerPrintDisabled(UserSettings.NO_FINGERPRINT_NOT_DISABLED);
                        editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                        editor.putString(UserSettings.IS_FINGERPRINT_DISABLED, settings.getIsFingerPrintDisabled());
                        editor.apply();
                        fingerprintSet = true;
                        fingerprintSwitcher.setChecked(true);
                        dialog.dismiss();
                        removePasswordLayout.setVisibility(View.VISIBLE);
                    } else {
                        StyleableToast.makeText(ConfigurePasscodeActivity.this, "Password does not match", R.style.custom_toast_2).show();
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            Header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            pinView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                pinView.requestFocus();
                inputMethodManager.showSoftInput(pinView, InputMethodManager.SHOW_IMPLICIT);

            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                fingerprintSwitcher.setChecked(fingerprintSet);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void updateView() {
        if (settings.getCustomTheme().equals(UserSettings.LIGHT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }

        if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            passwordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            fingerPrintText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            removePasswordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            passwordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            fingerPrintText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            removePasswordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            passwordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            fingerPrintText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            removePasswordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        if (settings.getPassword().equals(UserSettings.NOT_SET_PASSWORD)) {
            passwordText.setText("Set up password");
            removePasswordLayout.setVisibility(View.GONE);
        } else {
            passwordText.setText("Change password");
        }

        if(settings.getIsFingerPrintDisabled().equals(UserSettings.NO_FINGERPRINT_NOT_DISABLED)){
            fingerprintSwitcher.setChecked(true);
        }else{
            fingerprintSwitcher.setChecked(false);
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

        String password = sharedPreferences.getString(UserSettings.PASSWORD, UserSettings.NOT_SET_PASSWORD);
        settings.setPassword(password);

        String fingerprint = sharedPreferences.getString(UserSettings.IS_FINGERPRINT_DISABLED, UserSettings.YES_FINGERPRINT_DISABLED);
        settings.setIsFingerPrintDisabled(fingerprint);

        updateView();
    }


}