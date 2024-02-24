package com.hemerick.buymate.Widget;

import static com.hemerick.buymate.Widget.ShoppingWidgetProviderConfigureActivity.KEY_COUNT;
import static com.hemerick.buymate.Widget.ShoppingWidgetService.KEY_BUTTON_TEXT;
import static com.hemerick.buymate.Widget.ShoppingWidgetService.SHARED_PRES;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.HomeActivity;
import com.hemerick.buymate.InsertPasscodeActivity;
import com.hemerick.buymate.R;
import com.hemerick.buymate.SignUpActivity;

import java.util.ArrayList;

public class ShoppingWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "actionRefresh";
    public static final String ACTION_UPDATE_MY_WIDGET = "actionUPDATE";
    //preferable, use the name of the package+the_action_String

    UserSettings settings = new UserSettings();

    Intent buttonIntent;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
            String password = sharedPreferences.getString(UserSettings.PASSWORD, UserSettings.NOT_SET_PASSWORD);
            settings.setPassword(password);

            String authenticated = sharedPreferences.getString(UserSettings.IS_AUTHENTICATED, UserSettings.NOT_AUTHENTICATED);
            settings.setIsAuthenticated(authenticated);

            if (settings.getIsAuthenticated().equals(UserSettings.NOT_AUTHENTICATED)) {
                buttonIntent = new Intent(context, SignUpActivity.class);
            } else {
                if (settings.getPassword().equals(UserSettings.NOT_SET_PASSWORD)) {
                    buttonIntent = new Intent(context, HomeActivity.class);
                } else {
                    buttonIntent = new Intent(context, InsertPasscodeActivity.class);
                }
            }

            PendingIntent buttonPendingIntent = PendingIntent.getActivity(context, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            SharedPreferences prefs = context.getSharedPreferences(SHARED_PRES, Context.MODE_PRIVATE);
            String buttonText = prefs.getString(KEY_BUTTON_TEXT + appWidgetIds[i], "-- --");
            String count = prefs.getString(KEY_BUTTON_TEXT + appWidgetIds[i] + KEY_COUNT, "0");


            Intent serviceIntent = new Intent(context, ShoppingWidgetService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            Intent clickIntent = new Intent(context, ShoppingWidgetProvider.class);
            clickIntent.setAction(ACTION_REFRESH);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, appWidgetIds[i], clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);


            Intent refreshIntent = new Intent(context, ShoppingWidgetProvider.class);
            refreshIntent.setAction(ACTION_UPDATE_MY_WIDGET);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, appWidgetIds[i], refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);


            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shopping_widget_provider);
            views.setOnClickPendingIntent(R.id.widget_add_icon, buttonPendingIntent);
            views.setOnClickPendingIntent(R.id.widget_sync_icon, refreshPendingIntent);

            views.setRemoteAdapter(R.id.item_widget_stack_view, serviceIntent);
            views.setEmptyView(R.id.item_widget_stack_view, R.id.item_widget_empty_view);

            views.setTextViewText(R.id.category_text_view, buttonText);
            views.setTextViewText(R.id.item_count_text_view, "[" + count + "]");


            views.setPendingIntentTemplate(R.id.item_widget_stack_view, clickPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i], R.id.item_widget_stack_view);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shopping_widget_provider);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);


        if (ACTION_REFRESH.equals(intent.getAction())) {

            ArrayList<String> Items = new ArrayList<>();

            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            int position = intent.getIntExtra("POSITION", 0);
            String category = intent.getStringExtra("CATEGORY");

            ShopDatabase db = new ShopDatabase(context);
            Cursor res = db.getItems(category, context);
            while (res.moveToNext()) {
                Items.add(res.getString(2));
            }
            String actual_item = Items.get(position);

            int status = 0;
            res = db.getStatus(category, actual_item);
            while (res.moveToNext()) {
                status = res.getInt(3);
            }

            if (status == 1) {
                db.updateStatus(category, actual_item, 0);
            } else if (status == 0) {
                db.updateStatus(category, actual_item, 1);

            }

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.item_widget_stack_view);
        } else if (ACTION_UPDATE_MY_WIDGET.equals(intent.getAction())) {

            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.item_widget_stack_view);
        }


    }
}