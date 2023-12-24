package com.hemerick.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Patterns;
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
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.NetworkUtils.Network;

import io.github.muddz.styleabletoast.StyleableToast;

public class LogInActivity extends AppCompatActivity {

    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;

    SharedPreferences sharedPreferences;

    private GoogleSignInClient googleSignInClient;

    int RC_SIGN_IN = 40;


    TextView sub_header, or_text, googleText, loginText1, loginText2, forgotPasswordText;
    TextInputEditText emailbox, passwordbox;
    TextInputLayout emailLayout, passwordLayout;
    Button loginBtn;

    ProgressBar progressBar;

    CardView googleCard;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        settings = new UserSettings();
        firebaseAuth = FirebaseAuth.getInstance();

        sub_header = findViewById(R.id.sub_header);
        or_text = findViewById(R.id.orText);
        googleText = findViewById(R.id.googleText);
        loginText1 = findViewById(R.id.loginText1);
        loginText2 = findViewById(R.id.loginText2);
        forgotPasswordText = findViewById(R.id.forgot_password_text);


        emailbox = findViewById(R.id.email_box);
        passwordbox = findViewById(R.id.password_box);


        emailLayout = findViewById(R.id.email_box_parent);
        passwordLayout = findViewById(R.id.password_box_parent);

        progressBar = findViewById(R.id.progress_bar);


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        googleSignInClient = GoogleSignIn.getClient(LogInActivity.this, googleSignInOptions);


        googleCard = findViewById(R.id.googleCard);
        googleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserGoogle();
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, RecoverPasswordActivity.class);
                startActivity(intent);
            }
        });

        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailLayout.setErrorEnabled(false);
                passwordLayout.setErrorEnabled(false);
                loginUser();
            }
        });

        loginText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        loadSharedPreferences();
    }


    public void loginUserGoogle() {
        if (Network.isNetworkAvailable(LogInActivity.this)) {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        } else {
            final Dialog dialog = new Dialog(LogInActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {


            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {


                firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {



                                    StyleableToast.makeText(LogInActivity.this, "Sign up successful", R.style.custom_toast).show();

                                    settings.setIsAuthenticated(UserSettings.IS_AUTHENTICATED);

                                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                                    editor.putString(UserSettings.IS_AUTHENTICATED, settings.getIsAuthenticated());
                                    editor.apply();


                                    Intent intent = new Intent(LogInActivity.this, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    startActivity(intent);

                                }
                            }
                        });

            }
        }, 2000);

    }

    public void loginUser() {

        if (Network.isNetworkAvailable(LogInActivity.this)) {
            String emailText = emailbox.getText().toString().trim();
            String passwordText = passwordbox.getText().toString().trim();

            if (!emailText.isEmpty()) {
                if (!passwordText.isEmpty()) {
                    if (isValidEmail(emailText)) {

                        firebaseAuth.signInWithEmailAndPassword(emailText, passwordText)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {


                                                StyleableToast.makeText(LogInActivity.this, "Log in successful", R.style.custom_toast).show();
                                                settings.setIsAuthenticated(UserSettings.IS_AUTHENTICATED);

                                                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                                                editor.putString(UserSettings.IS_AUTHENTICATED, settings.getIsAuthenticated());
                                                editor.apply();

                                                progressBar.setVisibility(View.INVISIBLE);

                                                Intent intent = new Intent(LogInActivity.this, HomeActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        }, 2000);

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        StyleableToast.makeText(LogInActivity.this, "Error: " + e.getMessage(), R.style.custom_toast).show();

                                    }
                                });

                    } else {
                        emailLayout.setError("Not a valid email address");
                    }
                } else {
                    passwordLayout.setError("Insert password");
                }
            } else {
                emailLayout.setError("Enter email address");
            }
        } else {
            final Dialog dialog = new Dialog(LogInActivity.this);
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

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    private void updateView() {


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            or_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            googleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            loginText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            loginText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            forgotPasswordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emailbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            passwordbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            loginBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            or_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            googleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            loginText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            loginText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            forgotPasswordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emailbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            passwordbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            loginBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            or_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            googleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            loginText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            loginText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            forgotPasswordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emailbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            passwordbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            loginBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
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


        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        String authenticated = sharedPreferences.getString(UserSettings.IS_AUTHENTICATED, UserSettings.NOT_AUTHENTICATED);
        settings.setIsAuthenticated(authenticated);

        updateView();
    }
}