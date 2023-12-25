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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.NetworkUtils.Network;

import java.util.List;

import io.github.muddz.styleabletoast.StyleableToast;

public class SignUpActivity extends AppCompatActivity {

    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;

    SharedPreferences sharedPreferences;
    private GoogleSignInClient googleSignInClient;

    int RC_SIGN_IN = 40;

    TextView sub_header, or_text, loginText1, loginText2;
    TextInputEditText fullnamebox, emailbox, passwordbox, confirmpasswordbox;
    TextInputLayout fullnameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    Button signUpButton;

    CardView googleCard;

    FirebaseAuth firebaseAuth;
    private BillingClient billingClient;

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        settings = new UserSettings();
        firebaseAuth = FirebaseAuth.getInstance();

        sub_header = findViewById(R.id.sub_header);
        or_text = findViewById(R.id.orText);

        loginText1 = findViewById(R.id.loginText1);
        loginText2 = findViewById(R.id.loginText2);

        fullnamebox = findViewById(R.id.fullname_box);
        emailbox = findViewById(R.id.email_box);
        passwordbox = findViewById(R.id.password_box);
        confirmpasswordbox = findViewById(R.id.confirm_password_box);

        fullnameLayout = findViewById(R.id.fulname_box_parent);
        emailLayout = findViewById(R.id.email_box_parent);
        passwordLayout = findViewById(R.id.password_box_parent);
        confirmPasswordLayout = findViewById(R.id.confirm_password_box_parent);

        progressBar = findViewById(R.id.progress_bar);


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        googleSignInClient = GoogleSignIn.getClient(SignUpActivity.this, googleSignInOptions);

        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases() // Optional: Handle pending purchases
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                        // Handle purchase updates
                    }
                })
                .build();


        googleCard = findViewById(R.id.googleCard);
        googleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateUserGoogle();
            }
        });


        signUpButton = findViewById(R.id.signUpBtn);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailLayout.setErrorEnabled(false);
                passwordLayout.setErrorEnabled(false);
                confirmPasswordLayout.setErrorEnabled(false);
                authenticateUser();
            }
        });

        loginText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        loadSharedPreferences();

    }

    public void authenticateUser() {

        if (Network.isNetworkAvailable(SignUpActivity.this)) {
            String fullnameText = fullnamebox.getText().toString().trim();
            String emailText = emailbox.getText().toString().trim();
            String passwordText = passwordbox.getText().toString().trim();
            String confirm_passwordText = confirmpasswordbox.getText().toString().trim();

            if (!fullnameText.isEmpty()) {

                if (!emailText.isEmpty()) {
                    if (!passwordText.isEmpty()) {
                        if (!confirm_passwordText.isEmpty()) {
                            if (isValidEmail(emailText)) {
                                if (passwordText.length() >= 6) {
                                    if (passwordText.equals(confirm_passwordText)) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        new Handler().postDelayed(new Runnable() {

                                            @Override
                                            public void run() {

                                                firebaseAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                                        if (task.isSuccessful()) {
                                                            StyleableToast.makeText(SignUpActivity.this, "Sign up successful", R.style.custom_toast).show();

                                                            settings.setIsAuthenticated(UserSettings.IS_AUTHENTICATED);
                                                            settings.setUsername(fullnameText);

                                                            SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                                                            editor.putString(UserSettings.IS_AUTHENTICATED, settings.getIsAuthenticated());
                                                            editor.putString(UserSettings.USER_NAME, settings.getUsername());
                                                            editor.apply();

                                                            progressBar.setVisibility(View.INVISIBLE);

                                                            checkIfLifetimeSubscribed();

                                                            Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                        } else {
                                                            StyleableToast.makeText(SignUpActivity.this, "Sign up failed", R.style.custom_toast).show();
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        if (e instanceof FirebaseAuthUserCollisionException) {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            emailLayout.setError("Email already registered");
                                                        }
                                                    }
                                                });

                                            }
                                        }, 2000);


                                    } else {
                                        confirmPasswordLayout.setError("Password does not match");
                                    }

                                } else {
                                    passwordLayout.setError("Password must be at least 6 characters long");
                                }

                            } else {
                                emailLayout.setError("Not a valid email address");

                            }

                        } else {
                            confirmPasswordLayout.setError("Retype password to proceed");
                        }

                    } else {
                        passwordLayout.setError("Insert password");
                    }

                } else {
                    emailLayout.setError("Enter email address");
                }
            } else {
                fullnameLayout.setError("Enter your username to proceed");
            }
        } else {
            final Dialog dialog = new Dialog(SignUpActivity.this);
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

    public void authenticateUserGoogle() {

        if (Network.isNetworkAvailable(SignUpActivity.this)) {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        } else {
            final Dialog dialog = new Dialog(SignUpActivity.this);
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


                                    StyleableToast.makeText(SignUpActivity.this, "Sign up successful", R.style.custom_toast).show();

                                    settings.setIsAuthenticated(UserSettings.IS_AUTHENTICATED);

                                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                                    editor.putString(UserSettings.IS_AUTHENTICATED, settings.getIsAuthenticated());
                                    editor.apply();

                                    progressBar.setVisibility(View.INVISIBLE);

                                    checkIfLifetimeSubscribed();

                                    Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }
                        });

            }
        }, 2000);


    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void checkIfLifetimeSubscribed(){
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(), new PurchasesResponseListener() {
                        @Override


                        public

                        void

                        onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list)

                        {

                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                boolean hasLifetimePayment = false;
                                for (Purchase purchase : list) {
                                    if (purchase.getOrderId().equals("com.hemerick.lifetime_subscription") &&
                                            purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                        hasLifetimePayment = true;
                                        break;
                                    }
                                }

                                // Proceed based on lifetime payment status
                                if (hasLifetimePayment) {
                                    settings.setIsLifetimePurchased(UserSettings.YES_LIFETIME_PURCHASED);
                                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                                    editor.putString(UserSettings.IS_LIFETIME_PURCHASED, settings.getIsLifetimePurchased());
                                    editor.apply();

                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Handle billing service disconnections
            }
        });

    }

    private void updateView() {


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            or_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            loginText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            loginText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            fullnamebox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emailbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            passwordbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            confirmpasswordbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            signUpButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));


        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            or_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            loginText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            loginText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            fullnamebox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emailbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            passwordbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            confirmpasswordbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            signUpButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            or_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

            loginText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            loginText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            fullnamebox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emailbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            passwordbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            confirmpasswordbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            signUpButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
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

        String firstname = sharedPreferences.getString(UserSettings.USER_NAME, UserSettings.USER_NAME_NOT_SET);
        settings.setUsername(firstname);

        String lifetimePurchased = sharedPreferences.getString(UserSettings.IS_LIFETIME_PURCHASED, UserSettings.NO_LIFETIME_NOT_SUBSCRIBED);
        settings.setIsLifetimePurchased(lifetimePurchased);

        updateView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isWakeLockEnabled = UserSettings.isWakeLockEnabled(this);

        if (isWakeLockEnabled) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");

            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }

    }

}