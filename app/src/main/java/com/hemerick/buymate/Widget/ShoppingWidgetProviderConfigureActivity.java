package com.hemerick.buymate.Widget;

import static com.hemerick.buymate.Widget.ShoppingWidgetProvider.ACTION_REFRESH;
import static com.hemerick.buymate.Widget.ShoppingWidgetProvider.ACTION_UPDATE_MY_WIDGET;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.HomeActivity;
import com.hemerick.buymate.InsertPasscodeActivity;
import com.hemerick.buymate.R;
import com.hemerick.buymate.SignUpActivity;

import java.util.ArrayList;
import java.util.HashSet;

public class ShoppingWidgetProviderConfigureActivity extends Activity implements WidgetConfigAdapter.OnNoteListener {


    public static final String SHARED_PRES = "prefs";
    public static final String KEY_BUTTON_TEXT = "keyButtonText";
    public static final String KEY_COUNT = "keyCount";

    public int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public UserSettings settings;
    ShopDatabase db;

    Button createNewListBtn;
    RecyclerView recyclerView;


    ArrayList<String> category_list;
    ImageView empty_image_view;
    String list_text = " ";

    Toolbar toolbar;

    Intent buttonIntent;

    ProgressBar progressBar;

    WidgetConfigAdapter adapter;


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
        setContentView(R.layout.shopping_widget_provider_configure);

        toolbar = findViewById(R.id.configureToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShoppingWidgetProviderConfigureActivity.super.onBackPressed();
            }
        });


        db = new ShopDatabase(getApplicationContext());

        createNewListBtn = findViewById(R.id.configureCreateButton);
        createNewListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settings.getIsAuthenticated().equals(UserSettings.NOT_AUTHENTICATED)) {

                    Intent intent = new Intent(ShoppingWidgetProviderConfigureActivity.this, SignUpActivity.class);
                    startActivity(intent);
                    finish();

                } else {


                    Intent intentHome, intentPasscode;
                    if (settings.getPassword().equals(UserSettings.NOT_SET_PASSWORD)) {
                        intentHome = new Intent(ShoppingWidgetProviderConfigureActivity.this, HomeActivity.class);
                        startActivity(intentHome);
                        finish();
                    } else {
                        intentPasscode = new Intent(ShoppingWidgetProviderConfigureActivity.this, InsertPasscodeActivity.class);
                        startActivity(intentPasscode);
                        finish();
                    }


                }
            }
        });


        progressBar = findViewById(R.id.ProgressBar);
        category_list = new ArrayList<>();


        adapter = new WidgetConfigAdapter(ShoppingWidgetProviderConfigureActivity.this, settings, category_list, this);
        recyclerView = findViewById(R.id.configureListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(ShoppingWidgetProviderConfigureActivity.this));
        recyclerView.setAdapter(adapter);

        empty_image_view = findViewById(R.id.empty);

        Intent configIntent = getIntent();
        Bundle extras = configIntent.getExtras();

        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, resultValue);

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        displayData();

        loadSharedPreferences();

    }

    public void displayData() {
        Cursor res = db.getCategory(ShoppingWidgetProviderConfigureActivity.this);
        if (res.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            empty_image_view.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            empty_image_view.setVisibility(View.GONE);

            HashSet<String> hash = new HashSet<>();
            while (res.moveToNext()) {
                hash.add(res.getString(1).trim());
            }
            res.close();

            category_list.addAll(hash);
        }
    }

    private String getListCount() {
        Cursor res = db.getItems(list_text, ShoppingWidgetProviderConfigureActivity.this);

        ArrayList<String> item_count = new ArrayList<>();
        while (res.moveToNext()) {
            item_count.add(res.getString(2).trim());
        }
        res.close();

        return String.valueOf(item_count.size());
    }

    public void confirmConfiguration() {
        if(checkConfirmWidget()){
            super.onBackPressed();
        }
    }


    public boolean checkConfirmWidget(){
        progressBar.setVisibility(View.VISIBLE);

        SharedPreferences prefs = getSharedPreferences(SHARED_PRES, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BUTTON_TEXT + appWidgetId, list_text);
        editor.putString(KEY_BUTTON_TEXT + appWidgetId + KEY_COUNT, getListCount());
        editor.apply();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        if (settings.getIsAuthenticated().equals(UserSettings.NOT_AUTHENTICATED)) {
            buttonIntent = new Intent(ShoppingWidgetProviderConfigureActivity.this, SignUpActivity.class);
        } else {
            if (settings.getPassword().equals(UserSettings.NOT_SET_PASSWORD)) {
                buttonIntent = new Intent(ShoppingWidgetProviderConfigureActivity.this, HomeActivity.class);
            } else {
                buttonIntent = new Intent(ShoppingWidgetProviderConfigureActivity.this, InsertPasscodeActivity.class);
            }
        }

        PendingIntent buttonPendingIntent = PendingIntent.getActivity(this, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);


        Intent serviceIntent = new Intent(this, ShoppingWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        Intent clickIntent = new Intent(this, ShoppingWidgetProvider.class);
        clickIntent.setAction(ACTION_REFRESH);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent clickPendingIntent = PendingIntent.getBroadcast(this, appWidgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);


        Intent refreshIntent = new Intent(this, ShoppingWidgetProvider.class);
        refreshIntent.setAction(ACTION_UPDATE_MY_WIDGET);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(this, appWidgetId, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);

        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.shopping_widget_provider);
        views.setOnClickPendingIntent(R.id.widget_add_icon, buttonPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_sync_icon, refreshPendingIntent);

        views.setRemoteAdapter(R.id.item_widget_stack_view, serviceIntent);

        views.setEmptyView(R.id.item_widget_stack_view, R.id.item_widget_empty_view);

        views.setTextViewText(R.id.category_text_view, list_text);
        views.setTextViewText(R.id.item_count_text_view, "[" + getListCount() + "]");

        views.setPendingIntentTemplate(R.id.item_widget_stack_view, clickPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
        progressBar.setVisibility(View.INVISIBLE);

        return true;

    }




    public void updateView() {

        if (settings.getCustomTheme().equals(UserSettings.DEFAULT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        if (settings.getCustomTheme().equals(UserSettings.LIGHT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }



        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            createNewListBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            createNewListBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            createNewListBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


    }

    public void loadSharedPreferences() {

        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);


        String theme = sharedPreferences.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);

        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        String password = sharedPreferences.getString(UserSettings.PASSWORD, UserSettings.NOT_SET_PASSWORD);
        settings.setPassword(password);

        String authenticated = sharedPreferences.getString(UserSettings.IS_AUTHENTICATED, UserSettings.NOT_AUTHENTICATED);
        settings.setIsAuthenticated(authenticated);


        updateView();
    }

    @Override
    public void onNoteClick(int position) {
        list_text = category_list.get(position);
        confirmConfiguration();
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public void onOptionClick(int position) {

    }
}