package com.hemerick.buymate;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.hemerick.buymate.Database.UserSettings;


public class SwipeSettingActivity extends AppCompatActivity {

    Toolbar toolbar;
    ConstraintLayout swipe_left_layout, swipe_right_layout, swipe_disable_layout;
    TextView swipeLeftHeader, swipeLeftSubHeader, swipeRightHeader,
            swipeRightSubHeader, swipeDisableTextHeader, swipeDescription;
    SwitchCompat swipeDisableSwitch;
    Button swipeLeftCustomButton, swipeRightCustomButton;
    private UserSettings settings;


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
        setContentView(R.layout.activity_swipe_setting);

        toolbar = findViewById(R.id.swipeToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwipeSettingActivity.super.onBackPressed();
            }
        });


        swipe_left_layout = findViewById(R.id.swipeLeftLayout);
        swipe_right_layout = findViewById(R.id.swipeRightLayout);
        swipe_disable_layout = findViewById(R.id.swipeDisableLayout);
        swipeLeftHeader = findViewById(R.id.swipeLeftTextHeader);
        swipeRightHeader = findViewById(R.id.swipeRightTextHeader);
        swipeDisableTextHeader = findViewById(R.id.swipeDisableTextHeader);
        swipeDescription = findViewById(R.id.swipe_description);
        swipeDisableSwitch = findViewById(R.id.disable_swipe_switch);
        swipeLeftSubHeader = findViewById(R.id.swipeLeftTextSubHeader);
        swipeRightSubHeader = findViewById(R.id.swipeRightTextSubHeader);

        swipeLeftCustomButton = findViewById(R.id.customize_swipeLeft_button);
        swipeRightCustomButton = findViewById(R.id.customize_swipeRight_button);


        swipe_left_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeLeftDialog();
            }
        });

        swipe_right_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeRightDialog();
            }
        });

        swipeLeftCustomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeLeftDialog();
            }
        });

        swipeRightCustomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeRightDialog();
            }
        });

        swipe_disable_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swipeDisableSwitch.isChecked()){
                    swipeDisableSwitch.setChecked(false);
                }else{
                    swipeDisableSwitch.setChecked(true);
                }
            }
        });

        swipe_disable_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swipeDisableSwitch.isChecked()){
                    swipeDisableSwitch.setChecked(false);
                }else{
                    swipeDisableSwitch.setChecked(true);
                }
            }
        });
        swipeDisableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    TypedValue typedValue = new TypedValue();
                    SwipeSettingActivity.this.getTheme().resolveAttribute(com.google.android.material.R.attr.itemTextAppearanceInactive, typedValue, true);
                    int shadowColor = typedValue.data;

                    settings.setIsSwipeDisabled(UserSettings.YES_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_SWIPE_DISABLED, settings.getIsSwipeDisabled());
                    editor.apply();

                    swipeDisableSwitch.setChecked(true);
                    swipe_left_layout.setClickable(false);
                    swipeLeftCustomButton.setClickable(false);
                    swipeLeftHeader.setTextColor(shadowColor);
                    swipeLeftCustomButton.setTextColor(shadowColor);
                    swipe_right_layout.setClickable(false);
                    swipeRightCustomButton.setClickable(false);
                    swipeRightHeader.setTextColor(shadowColor);
                    swipeRightCustomButton.setTextColor(shadowColor);
                } else {
                    TypedValue typedValue = new TypedValue();
                    SwipeSettingActivity.this.getTheme().resolveAttribute(androidx.constraintlayout.widget.R.attr.textFillColor, typedValue, true);
                    int shadowColor = typedValue.data;
                    SwipeSettingActivity.this.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
                    int shadowColor2 = typedValue.data;


                    settings.setIsSwipeDisabled(UserSettings.NOT_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_SWIPE_DISABLED, settings.getIsSwipeDisabled());
                    editor.apply();


                    swipe_left_layout.setClickable(true);
                    swipeLeftCustomButton.setClickable(true);
                    swipeLeftHeader.setTextColor(shadowColor);
                    swipeLeftCustomButton.setTextColor(shadowColor2);
                    swipe_right_layout.setClickable(true);
                    swipeRightCustomButton.setClickable(true);
                    swipeRightHeader.setTextColor(shadowColor);
                    swipeRightCustomButton.setTextColor(shadowColor2);
                }
            }
        });


        loadSharedPreferences();
    }

    public void swipeLeftDialog() {
        Dialog dialog = new Dialog(SwipeSettingActivity.this);
        dialog.setContentView(R.layout.custom_swipe_action_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.edit_title);

        RadioButton rename = dialog.findViewById(R.id.radio_button_rename);
        RadioButton check_item = dialog.findViewById(R.id.radio_button_check);
        RadioButton change_price = dialog.findViewById(R.id.radio_button_price);
        RadioButton change_quantity = dialog.findViewById(R.id.radio_button_quantity);
        RadioButton star = dialog.findViewById(R.id.radio_button_star);
        RadioButton delete = dialog.findViewById(R.id.radio_button_delete);
        RadioButton show_options = dialog.findViewById(R.id.radio_button_options);
        RadioButton do_noting = dialog.findViewById(R.id.radio_button_do_nothing);


        SharedPreferences sharedPreferences = SwipeSettingActivity.this.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String swipe = sharedPreferences.getString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, UserSettings.SWIPE_SHOW_OPTIONS);
        settings.setCustomLeftSwipeAction(swipe);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            rename.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            check_item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            change_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            change_quantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            star.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            delete.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            show_options.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            do_noting.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            rename.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            check_item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            change_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            change_quantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            star.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            delete.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            show_options.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            do_noting.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {

            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            rename.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            check_item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            change_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            change_quantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            star.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            delete.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            show_options.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            do_noting.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        if (settings.getCustomLeftSwipeAction().equals(UserSettings.SWIPE_RENAME)) {
            rename.setChecked(true);
        } else if (settings.getCustomLeftSwipeAction().equals(UserSettings.SWIPE_CHECK)) {
            check_item.setChecked(true);
        } else if (settings.getCustomLeftSwipeAction().equals(UserSettings.SWIPE_CHANGE_PRICE)) {
            change_price.setChecked(true);
        } else if (settings.getCustomLeftSwipeAction().equals(UserSettings.SWIPE_CHANGE_QUANTITY)) {
            change_quantity.setChecked(true);
        } else if (settings.getCustomLeftSwipeAction().equals(UserSettings.SWIPE_STAR_ITEM)) {
            star.setChecked(true);
        } else if (settings.getCustomLeftSwipeAction().equals(UserSettings.SWIPE_DELETE_ITEM)) {
            delete.setChecked(true);
        } else if (settings.getCustomLeftSwipeAction().equals(UserSettings.SWIPE_SHOW_OPTIONS)) {
            show_options.setChecked(true);
        } else if (settings.getCustomLeftSwipeAction().equals(UserSettings.SWIPE_DO_NOTHING)) {
            do_noting.setChecked(true);
        }

        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomLeftSwipeAction(UserSettings.SWIPE_RENAME);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, settings.getCustomLeftSwipeAction());
                editor.apply();
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__rename));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        check_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomLeftSwipeAction(UserSettings.SWIPE_CHECK);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, settings.getCustomLeftSwipeAction());
                editor.apply();
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__checkAndUncheckItem));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        change_price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomLeftSwipeAction(UserSettings.SWIPE_CHANGE_PRICE);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, settings.getCustomLeftSwipeAction());
                editor.apply();
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__changePrice));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        change_quantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomLeftSwipeAction(UserSettings.SWIPE_CHANGE_QUANTITY);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, settings.getCustomLeftSwipeAction());
                editor.apply();
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__changeQuantity));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomLeftSwipeAction(UserSettings.SWIPE_STAR_ITEM);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, settings.getCustomLeftSwipeAction());
                editor.apply();
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__starAndUnstar));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomLeftSwipeAction(UserSettings.SWIPE_DELETE_ITEM);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, settings.getCustomLeftSwipeAction());
                editor.apply();
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__delete));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        show_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomLeftSwipeAction(UserSettings.SWIPE_SHOW_OPTIONS);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, settings.getCustomLeftSwipeAction());
                editor.apply();
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__showOptions));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        do_noting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomLeftSwipeAction(UserSettings.SWIPE_DO_NOTHING);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, settings.getCustomLeftSwipeAction());
                editor.apply();
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__doNothing));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);

            }
        });

        dialog.show();

    }


    public void swipeRightDialog() {
        Dialog dialog = new Dialog(SwipeSettingActivity.this);
        dialog.setContentView(R.layout.custom_swipe_action_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.edit_title);

        RadioButton rename = dialog.findViewById(R.id.radio_button_rename);
        RadioButton check_item = dialog.findViewById(R.id.radio_button_check);
        RadioButton change_price = dialog.findViewById(R.id.radio_button_price);
        RadioButton change_quantity = dialog.findViewById(R.id.radio_button_quantity);
        RadioButton star = dialog.findViewById(R.id.radio_button_star);
        RadioButton delete = dialog.findViewById(R.id.radio_button_delete);
        RadioButton show_options = dialog.findViewById(R.id.radio_button_options);
        RadioButton do_noting = dialog.findViewById(R.id.radio_button_do_nothing);

        SharedPreferences sharedPreferences = SwipeSettingActivity.this.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String swipe = sharedPreferences.getString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, UserSettings.SWIPE_DELETE_ITEM);
        settings.setCustomRightSwipeAction(swipe);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            rename.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            check_item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            change_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            change_quantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            star.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            delete.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            show_options.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            do_noting.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            rename.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            check_item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            change_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            change_quantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            star.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            delete.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            show_options.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            do_noting.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        } else if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {

            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            rename.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            check_item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            change_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            change_quantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            star.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            delete.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            show_options.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            do_noting.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        if (settings.getCustomRightSwipeAction().equals(UserSettings.SWIPE_RENAME)) {
            rename.setChecked(true);
        } else if (settings.getCustomRightSwipeAction().equals(UserSettings.SWIPE_CHECK)) {
            check_item.setChecked(true);
        } else if (settings.getCustomRightSwipeAction().equals(UserSettings.SWIPE_CHANGE_PRICE)) {
            change_price.setChecked(true);
        } else if (settings.getCustomRightSwipeAction().equals(UserSettings.SWIPE_CHANGE_QUANTITY)) {
            change_quantity.setChecked(true);
        } else if (settings.getCustomRightSwipeAction().equals(UserSettings.SWIPE_STAR_ITEM)) {
            star.setChecked(true);
        } else if (settings.getCustomRightSwipeAction().equals(UserSettings.SWIPE_DELETE_ITEM)) {
            delete.setChecked(true);
        } else if (settings.getCustomRightSwipeAction().equals(UserSettings.SWIPE_SHOW_OPTIONS)) {
            show_options.setChecked(true);
        } else if (settings.getCustomRightSwipeAction().equals(UserSettings.SWIPE_DO_NOTHING)) {
            do_noting.setChecked(true);
        }

        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomRightSwipeAction(UserSettings.SWIPE_RENAME);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, settings.getCustomRightSwipeAction());
                editor.apply();

                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__rename));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        check_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomRightSwipeAction(UserSettings.SWIPE_CHECK);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, settings.getCustomRightSwipeAction());
                editor.apply();
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__checkAndUncheckItem));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        change_price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomRightSwipeAction(UserSettings.SWIPE_CHANGE_PRICE);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, settings.getCustomRightSwipeAction());
                editor.apply();
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__changePrice));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        change_quantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomRightSwipeAction(UserSettings.SWIPE_CHANGE_QUANTITY);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, settings.getCustomRightSwipeAction());
                editor.apply();
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__changeQuantity));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomRightSwipeAction(UserSettings.SWIPE_STAR_ITEM);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, settings.getCustomRightSwipeAction());
                editor.apply();
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__starAndUnstar));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomRightSwipeAction(UserSettings.SWIPE_DELETE_ITEM);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, settings.getCustomRightSwipeAction());
                editor.apply();
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__delete));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        show_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomRightSwipeAction(UserSettings.SWIPE_SHOW_OPTIONS);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, settings.getCustomRightSwipeAction());
                editor.apply();
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__showOptions));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });

        do_noting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCustomRightSwipeAction(UserSettings.SWIPE_DO_NOTHING);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, settings.getCustomRightSwipeAction());
                editor.apply();
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__doNothing));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });


        dialog.show();

    }


    private void updateView() {

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            swipeLeftHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            swipeRightHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            swipeDisableTextHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            swipeDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            swipeLeftSubHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));
            swipeRightSubHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));
            swipeLeftCustomButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            swipeRightCustomButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            swipeLeftHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            swipeRightHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            swipeDisableTextHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            swipeLeftSubHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            swipeRightSubHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            swipeDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            swipeLeftCustomButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            swipeRightCustomButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            swipeLeftHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            swipeRightHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            swipeDisableTextHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            swipeLeftSubHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            swipeRightSubHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            swipeDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            swipeLeftCustomButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            swipeRightCustomButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        switch (settings.getCustomLeftSwipeAction()) {
            case UserSettings.SWIPE_RENAME:
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__renameSwipeLeft));
                break;
            case UserSettings.SWIPE_CHECK:
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__checkAndUncheckLeft));
                break;
            case UserSettings.SWIPE_CHANGE_PRICE:
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__changePriceSwipeLeft));
                break;
            case UserSettings.SWIPE_CHANGE_QUANTITY:
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__changeQuantitySwipeLeft));
                break;
            case UserSettings.SWIPE_STAR_ITEM:
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__starAndUnstarLeft));
                break;
            case UserSettings.SWIPE_DELETE_ITEM:
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__deleteSwipeLeft));
                break;
            case UserSettings.SWIPE_SHOW_OPTIONS:
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__showOptionsSwipeLeft));
                break;
            case UserSettings.SWIPE_DO_NOTHING:
                swipeLeftSubHeader.setText(getString(R.string.SwipeSettingActivity__doNothingSwipeLeft));
                break;
        }

        switch (settings.getCustomRightSwipeAction()) {
            case UserSettings.SWIPE_RENAME:
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__renameSwipeRight));
                break;
            case UserSettings.SWIPE_CHECK:
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__checkAndUncheckRight));
                break;
            case UserSettings.SWIPE_CHANGE_PRICE:
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__changePriceSwipeRight));
                break;
            case UserSettings.SWIPE_CHANGE_QUANTITY:
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__changeQuantitySwipeRight));
                break;
            case UserSettings.SWIPE_STAR_ITEM:
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__starAndUnstarRight));
                break;
            case UserSettings.SWIPE_DELETE_ITEM:
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__deleteSwipeRight));
                break;
            case UserSettings.SWIPE_SHOW_OPTIONS:
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__showOptionsSwipeRight));
                break;
            case UserSettings.SWIPE_DO_NOTHING:
                swipeRightSubHeader.setText(getString(R.string.SwipeSettingActivity__doNothingSwipeRight));
                break;

        }
        if (settings.getIsSwipeDisabled().equals(UserSettings.YES_DISABLED)) {

            TypedValue typedValue = new TypedValue();
            SwipeSettingActivity.this.getTheme().resolveAttribute(com.google.android.material.R.attr.itemTextAppearanceInactive, typedValue, true);
            int shadowColor = typedValue.data;

            swipeDisableSwitch.setChecked(true);
            swipe_left_layout.setClickable(false);
            swipeLeftCustomButton.setClickable(false);
            swipeLeftHeader.setTextColor(shadowColor);
            swipeLeftCustomButton.setTextColor(shadowColor);
            swipe_right_layout.setClickable(false);
            swipeRightCustomButton.setClickable(false);
            swipeRightHeader.setTextColor(shadowColor);
            swipeRightCustomButton.setTextColor(shadowColor);
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

        String ss = sharedPreferences.getString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, UserSettings.SWIPE_SHOW_OPTIONS);
        settings.setCustomLeftSwipeAction(ss);


        String sw = sharedPreferences.getString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, UserSettings.SWIPE_DELETE_ITEM);
        settings.setCustomRightSwipeAction(sw);

        String disables_swipe = sharedPreferences.getString(UserSettings.IS_SWIPE_DISABLED, UserSettings.NOT_DISABLED);
        settings.setIsSwipeDisabled(disables_swipe);

        updateView();
    }

}