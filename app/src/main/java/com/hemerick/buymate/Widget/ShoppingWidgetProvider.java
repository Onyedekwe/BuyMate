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
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.HomeActivity;
import com.hemerick.buymate.InsertPasscodeActivity;
import com.hemerick.buymate.R;
import com.hemerick.buymate.SignUpActivity;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ShoppingWidgetProviderConfigureActivity ShoppingWidgetProviderConfigureActivity}
 */
public class ShoppingWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_TOAST = "actionToast";
    //preferable, use the name of the package+the_action_String
    public static final String EXTRA_ITEM_POSITION = "extraItemPosition";

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
            String buttonText = prefs.getString(KEY_BUTTON_TEXT + appWidgetIds[i], "Press me");
            String count = prefs.getString(KEY_BUTTON_TEXT + appWidgetIds[i] + KEY_COUNT, "0");




            Intent serviceIntent = new Intent(context, ShoppingWidgetService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            Intent clickIntent = new Intent(context, ShoppingWidgetProvider.class);
            clickIntent.setAction(ACTION_TOAST);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context,
                    0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shopping_widget_provider);
            views.setOnClickPendingIntent(R.id.widget_add_icon, buttonPendingIntent);

            views.setRemoteAdapter(appWidgetIds[i], R.id.item_widget_stack_view, serviceIntent);
            views.setEmptyView(R.id.item_widget_stack_view, R.id.item_widget_empty_view);

            views.setTextViewText(R.id.category_text_view, buttonText);
            views.setTextViewText(R.id.item_count_text_view, "[" + count + "]");


            views.setPendingIntentTemplate(R.id.item_widget_stack_view, clickPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shopping_widget_provider);

        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        String dimensions = "Min width: " + minWidth + "\nMax width: " + maxWidth
                + "\nMin height: " + minHeight + "\nMax height: " + maxHeight;

        Toast.makeText(context, dimensions, Toast.LENGTH_LONG).show();

        //     if(maxHeight > 100){
        //       views.setViewVisibility(R.id.example_widget_text, View.VISIBLE);
        //     views.setViewVisibility(R.id.example_widget_button, View.VISIBLE);
        // }else{
        //    views.setViewVisibility(R.id.example_widget_text, View.GONE);
        //   views.setViewVisibility(R.id.example_widget_button, View.GONE);
        //}

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Toast.makeText(context, "OnDeleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context) {
        Toast.makeText(context, "OnEnabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(Context context) {
        Toast.makeText(context, "OnDisabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);

        if (ACTION_TOAST.equals(intent.getAction())) {
            int appwidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            int clickedPosition = intent.getIntExtra( EXTRA_ITEM_POSITION, 0);
            Toast.makeText(context, "Clicked position: " + clickedPosition, Toast.LENGTH_SHORT).show();
        }
        super.onReceive(context, intent);
    }
}