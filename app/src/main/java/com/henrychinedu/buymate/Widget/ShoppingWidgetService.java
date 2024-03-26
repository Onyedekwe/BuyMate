package com.henrychinedu.buymate.Widget;


import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.Html;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.henrychinedu.buymate.Database.ShopDatabase;
import com.henrychinedu.buymate.R;

import java.util.ArrayList;
import java.util.List;

public class ShoppingWidgetService extends RemoteViewsService {

    public static final String SHARED_PRES = "prefs";
    public static final String KEY_BUTTON_TEXT = "keyButtonText";

    String buttonText;


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ShoppingWidgetItemFactory(this, intent);
    }

    class ShoppingWidgetItemFactory implements RemoteViewsService.RemoteViewsFactory {

        public final Context context;
        public final int appWidgetId;

        String month, day, time;

        List<String> Items = new ArrayList<>();

        List<String> Items_Date = new ArrayList<>();


        ShoppingWidgetItemFactory(Context context, Intent intent) {
            this.context = context;
            this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }


        @Override
        public void onCreate() {
            //connect to data source
            SharedPreferences prefs = context.getSharedPreferences(SHARED_PRES, Context.MODE_PRIVATE);
            buttonText = prefs.getString(KEY_BUTTON_TEXT + appWidgetId, "-- --");

            ShopDatabase db = new ShopDatabase(context);
            Cursor res = db.getItems(buttonText, context);
            while (res.moveToNext()) {

                if (res.getInt(3) == 1) {
                    Items.add("☑ " + res.getString(2));
                } else {
                    Items.add("\u2610 " + res.getString(2));
                }


                month = res.getString(5);
                day = res.getString(7);
                time = res.getString(8);

                String full_date = context.getString(R.string.ShoppingWidgetService__added) + " " + day + " " + month + " " + time;
                Items_Date.add(full_date);
            }
            res.close();
            db.close();

        }

        @Override
        public void onDataSetChanged() {

            SharedPreferences prefs = context.getSharedPreferences(SHARED_PRES, Context.MODE_PRIVATE);
            buttonText = prefs.getString(KEY_BUTTON_TEXT + appWidgetId, "-- --");

            ShopDatabase db = new ShopDatabase(context);
            //refresh data
            Items.clear();
            Items_Date.clear();
            Cursor res = db.getItems(buttonText, context);
            while (res.moveToNext()) {

                if (res.getInt(3) == 1) {
                    Items.add("☑ " + res.getString(2));
                } else {
                    Items.add("\u2610 " + res.getString(2));
                }

                month = res.getString(5);
                day = res.getString(7);
                time = res.getString(8);

                String full_date = context.getString(R.string.ShoppingWidgetService__added) + " " + day + " " + month + " " + time;
                Items_Date.add(full_date);
            }
            res.close();

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews views2 = new RemoteViews(context.getPackageName(), R.layout.shopping_widget_provider);
            views2.setTextViewText(R.id.item_count_text_view, "[" + Items.size() + "]");

            appWidgetManager.updateAppWidget(appWidgetId, views2);


            db.close();


        }

        @Override
        public void onDestroy() {
            //close the connection to data source
        }

        @Override
        public int getCount() {
            return Items.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shopping_widget_item);
            if (Items.get(position).contains("☑")) {
                String textWithStrikeThrough = "<strike>" + Items.get(position).replace("☑", "").trim() + "</strike>";
                views.setTextViewText(R.id.shopping_widget_item_text, Html.fromHtml(textWithStrikeThrough, Html.FROM_HTML_MODE_COMPACT));
                views.setTextViewText(R.id.shopping_widget_item_date_text, Items_Date.get(position));


            } else if (Items.get(position).contains("\u2610")) {
                views.setTextViewText(R.id.shopping_widget_item_text, Items.get(position).replace("\u2610", "").trim());
                views.setTextViewText(R.id.shopping_widget_item_date_text, Items_Date.get(position));
            }


            Intent fillIntent = new Intent();

            fillIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            fillIntent.putExtra("CATEGORY", buttonText);
            fillIntent.putExtra("POSITION", position);
            fillIntent.putExtra("SIZE", Items.size());
            views.setOnClickFillInIntent(R.id.shopping_widget_item_layout, fillIntent);

            //since our stack view contains only one text view, its our rootview, thats why we passed it here!


            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
