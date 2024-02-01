package com.hemerick.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hemerick.buymate.Database.UserSettings;


public class MoreFragment extends Fragment {
    ConstraintLayout account_layout, message_layout, share_layout, rate_layout, about_layout, premium_layout, settings_layout, backup_layout, twitter_layout, website_layout;
    TextView premium_text, premium_text2, account_text, message_text, share_text, rate_text, rate_text2, premium_text_2, about_text, settings_text, backup_text, twitter_text, website_text;

    ImageView account_icon, backup_premium_icon;
    UserSettings settings;


    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_more, container, false);
        settings = new UserSettings();


        premium_text = rootView.findViewById(R.id.premiumText);
        premium_text2 = rootView.findViewById(R.id.premiumText2);
        premium_text_2 = rootView.findViewById(R.id.premiumText_2);
        account_text = rootView.findViewById(R.id.accountText);
        message_text = rootView.findViewById(R.id.messageText);
        share_text = rootView.findViewById(R.id.shareText);
        rate_text = rootView.findViewById(R.id.RateText);
        rate_text2 = rootView.findViewById(R.id.RateText2);
        about_text = rootView.findViewById(R.id.aboutText);
        settings_text = rootView.findViewById(R.id.settingText);
        backup_text = rootView.findViewById(R.id.backupText);

        twitter_text = rootView.findViewById(R.id.twitterText);
        website_text = rootView.findViewById(R.id.websiteText);

        backup_premium_icon = rootView.findViewById(R.id.backup_premium_icon);

        account_icon = rootView.findViewById(R.id.account_icon);

        twitter_layout = rootView.findViewById(R.id.twitterLayout);
        twitter_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        website_layout = rootView.findViewById(R.id.websiteLayout);
        website_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        share_layout = rootView.findViewById(R.id.shareLayout);
        share_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareMessage = "Check out this awesome shopping app! I highly recommend it.\nDownload it on Google Play.";
                String appLink = "https://play.google.com/store/apps/details?id=" + getContext().getPackageName();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage + "\n" + appLink);
                startActivity(Intent.createChooser(shareIntent, "Share via:"));
            }
        });
        premium_layout = rootView.findViewById(R.id.premiumLayout);

        premium_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)) {
                    intent = new Intent(getContext(), PaymentSuccessfulActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(getContext(), PremiumActivity.class);
                    startActivity(intent);
                }


            }
        });

        rate_layout = rootView.findViewById(R.id.RateLayout);
        rate_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getContext().getPackageName());
                Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent1);
            }
        });


        message_layout = rootView.findViewById(R.id.messageLayout);
        message_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (firebaseAuth.getCurrentUser() != null) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


                    String[] app_email = new String[]{"hemerickservices@gmail.com"};

                    String asterics = "*******";
                    String subject = getString(R.string.app_name) + " Feedback";
                    String email = firebaseUser.getEmail();
                    String app_version = getString(R.string.app_version);

                    String deviceInfo = asterics + "\n" +
                            "Account Email: " + email + "\n" +
                            "App Version: " + app_version + "\n" +
                            "Device: " + Build.DEVICE + "\n" +
                            "Model: " + Build.MODEL + "\n" +
                            "Brand: " + Build.BRAND + "\n" +
                            "OS Version: " + Build.VERSION.RELEASE + "\n" +
                            "SDK Version: " + Build.VERSION.SDK_INT + "\n" +
                            asterics + "\n";


                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:"));
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, app_email);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    emailIntent.putExtra(Intent.EXTRA_TEXT, deviceInfo);
                    if (emailIntent.resolveActivity(getContext().getPackageManager()) != null) {
                        startActivity(emailIntent);
                    } else {
                        Toast.makeText(getContext(), "No email client found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLogInWarningDialog();
                }
            }
        });

        backup_layout = rootView.findViewById(R.id.backupLayout);
        backup_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BackupActivity.class);
                startActivity(intent);

            }
        });

        settings_layout = rootView.findViewById(R.id.settingLayout);
        settings_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        about_layout = rootView.findViewById(R.id.aboutLayout);
        about_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AboutActivity.class);
                startActivity(intent);
            }
        });

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        account_layout = rootView.findViewById(R.id.accountLayout);
        account_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseUser == null) {
                    startActivity(new Intent(getContext(), LogInActivity.class));
                } else {
                    startActivity(new Intent(getContext(), UserActivity.class));
                }


            }
        });


        if (firebaseUser == null) {
            account_text.setText("Sign in");
            account_icon.setImageResource(R.drawable.final_regular_log_in_icon);
        } else {
            account_text.setText("Account");
            account_icon.setImageResource(R.drawable.final_regular_account_icon);
        }

        loadSharedPreferences();
        return rootView;
    }

    public void showLogInWarningDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.custom_logout_warning_dialog);
        dialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.header);
        TextView alertText = dialog.findViewById(R.id.alert_text);
        Button backup = dialog.findViewById(R.id.backup);
        TextView okBtn = dialog.findViewById(R.id.okBtn);

        alertText.setText("Please login to Buymate to be able to message us.");
        backup.setText("Login");


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
                Intent intent = new Intent(getContext(), LogInActivity.class);
                startActivity(intent);
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


    public boolean onBackPressed() {
        return false;
    }

    private void updateView() {

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            premium_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            premium_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            premium_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            account_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            message_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            share_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            rate_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            rate_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            backup_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            about_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            settings_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            twitter_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            website_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            premium_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            premium_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            premium_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            account_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            message_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            share_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            rate_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            rate_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            about_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            backup_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            settings_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            twitter_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            website_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            premium_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            premium_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            premium_text_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            account_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            message_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            share_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            rate_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            rate_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            backup_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            about_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            settings_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            twitter_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            website_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)) {
            backup_premium_icon.setImageResource(R.drawable.final_regular_open_icon);
        }
    }

    private void loadSharedPreferences() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);


        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        String lifetimePurchased = sharedPreferences.getString(UserSettings.IS_LIFETIME_PURCHASED, UserSettings.NO_LIFETIME_NOT_SUBSCRIBED);
        settings.setIsLifetimePurchased(lifetimePurchased);

        updateView();
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            account_text.setText("Sign in");
            account_icon.setImageResource(R.drawable.final_regular_log_in_icon);
        } else {
            account_text.setText("Account");
            account_icon.setImageResource(R.drawable.final_regular_account_icon);
        }


    }
}