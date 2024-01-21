package com.hemerick.buymate.Widget;


import static com.hemerick.buymate.Widget.ShoppingWidgetProvider.EXTRA_ITEM_POSITION;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.R;

import java.util.ArrayList;
import java.util.List;

public class ShoppingWidgetService extends RemoteViewsService {

    public static final String SHARED_PRES = "prefs";
    public static final String KEY_BUTTON_TEXT = "keyButtonText";

    private final int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    String buttonText;



    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ShoppingWidgetItemFactory(this.getApplicationContext(), intent);
    }

    class ShoppingWidgetItemFactory implements RemoteViewsService.RemoteViewsFactory {

        private final Context context;
        private final int appWidgetId;

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
            buttonText = prefs.getString(KEY_BUTTON_TEXT + appWidgetId, "Press me");

            String month, year, day, time;

            ShopDatabase db = new ShopDatabase(getApplicationContext());
            Cursor res = db.getItems(buttonText, getApplicationContext());
            while (res.moveToNext()) {

                if(res.getInt(3) == 1){
                    Items.add(res.getString(2) + " \u2611");
                }else{
                    Items.add(res.getString(2));
                }

                month = res.getString(5);
                day = res.getString(7);
                time = res.getString(8);

                String full_date = "Added " + day + ", " + month + ", " + time;
                Items_Date.add(full_date);
            }
            res.close();

            db.close();


        }

        @Override
        public void onDataSetChanged() {

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
            views.setTextViewText(R.id.shopping_widget_item_text, Items.get(position));
            views.setTextViewText(R.id.shopping_widget_item_date_text, Items_Date.get(position));


            Bundle extras = new Bundle();
            extras.putInt(EXTRA_ITEM_POSITION, position);


            Intent fillIntent = new Intent();
            fillIntent.putExtras(extras);

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
            return false;
        }
    }
}
