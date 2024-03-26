package com.henrychinedu.buymate.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.henrychinedu.buymate.Database.ShopDatabase;
import com.henrychinedu.buymate.Database.UserSettings;
import com.henrychinedu.buymate.ItemActivity;


public class RecyclerViewItemTouchHelper extends ItemTouchHelper.SimpleCallback {


    private final ShopItemAdapter adapter;
    ItemActivity itemActivity;
    String category;
    ShopDatabase db;
    UserSettings settings;


    public RecyclerViewItemTouchHelper(ShopItemAdapter adapter, UserSettings settings, String category, ItemActivity itemActivity) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.category = category;
        this.settings = settings;
        this.itemActivity = itemActivity;

    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        boolean checkIsEnabled = adapter.isSelected();
        if (checkIsEnabled) {
            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
            adapter.removeItemFromSelectList(viewHolder.getAdapterPosition());
        } else {
            db = new ShopDatabase(adapter.getContext());
            final int position = viewHolder.getAdapterPosition();
            if (direction == ItemTouchHelper.RIGHT) {

                SharedPreferences sharedPreferences = adapter.getContext().getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
                String sw = sharedPreferences.getString(UserSettings.CUSTOM_RIGHT_SWIPE_ACTION, UserSettings.SWIPE_DELETE_ITEM);
                settings.setCustomRightSwipeAction(sw);

                switch (settings.getCustomRightSwipeAction()) {
                    case UserSettings.SWIPE_RENAME:
                        rename(position);
                        break;
                    case UserSettings.SWIPE_CHECK:
                        checkItem(position);
                        break;
                    case UserSettings.SWIPE_CHANGE_PRICE:
                        changePrice(position);
                        break;
                    case UserSettings.SWIPE_CHANGE_QUANTITY:
                        changeQuantity(position);
                        break;
                    case UserSettings.SWIPE_STAR_ITEM:
                        star_item(position);
                        break;
                    case UserSettings.SWIPE_DELETE_ITEM:
                        showSwipedDeleteDialog(position);
                        break;
                    case UserSettings.SWIPE_SHOW_OPTIONS:
                        show_options(position);
                        break;
                    case UserSettings.SWIPE_DO_NOTHING:
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        break;

                }

            } else {

                SharedPreferences sharedPreferences = adapter.getContext().getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
                String sw = sharedPreferences.getString(UserSettings.CUSTOM_LEFT_SWIPE_ACTION, UserSettings.SWIPE_SHOW_OPTIONS);
                settings.setCustomLeftSwipeAction(sw);

                switch (settings.getCustomLeftSwipeAction()) {
                    case UserSettings.SWIPE_RENAME:
                        rename(position);
                        break;
                    case UserSettings.SWIPE_CHECK:
                        checkItem(position);
                        break;
                    case UserSettings.SWIPE_CHANGE_PRICE:
                        changePrice(position);
                        break;
                    case UserSettings.SWIPE_CHANGE_QUANTITY:
                        changeQuantity(position);
                        break;
                    case UserSettings.SWIPE_STAR_ITEM:
                        star_item(position);
                        break;
                    case UserSettings.SWIPE_DELETE_ITEM:
                        showSwipedDeleteDialog(position);
                        break;
                    case UserSettings.SWIPE_SHOW_OPTIONS:
                        show_options(position);
                        break;
                    case UserSettings.SWIPE_DO_NOTHING:
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        break;

                }
            }
        }

    }


    public void showSwipedDeleteDialog(int position) {
        itemActivity.showDeleteDialog(position);
    }

    public void checkItem(int position) {
        String temp = adapter.getItemName(position);
        int status = 0;
        Cursor res = db.getStatus(category, temp);
        while (res.moveToNext()) {
            status = res.getInt(3);
        }
        if (status == 1) {
            db.updateStatus(category, temp, 0);
        } else {
            db.updateStatus(category, temp, 1);
        }
        adapter.notifyItemChanged(position);
    }

    public void changePrice(int position) {
        String temp = adapter.getItemName(position);
        itemActivity.showPriceDialog(temp, position);
    }

    public void rename(int position) {
        String temp = adapter.getItemName(position);
        itemActivity.showRenameDialog(temp, position);
    }

    public void changeQuantity(int position) {
        String temp = adapter.getItemName(position);
        itemActivity.showQuantityDialog(temp, position);
    }

    public void show_options(int position) {
        String temp = adapter.getItemName(position);
        itemActivity.showEditDialog(temp, position);
    }

    public void star_item(int position) {
        String temp = adapter.getItemName(position);
        int star_status = 0;
        Cursor res = db.getFavourites(category, temp);
        while (res.moveToNext()) {
            star_status = res.getInt(10);
        }
        if (star_status == 1) {
            db.updateFavourites(category, temp, 0);
        } else {
            db.updateFavourites(category, temp, 1);
        }
        adapter.notifyItemChanged(position);
    }
}
