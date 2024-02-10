package com.hemerick.buymate.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.FavouritesFragment;


public class RecyclerViewFavTouchHelper extends ItemTouchHelper.SimpleCallback {

    private final ShopFavouritesAdapter adapter;
    FavouritesFragment favouritesFragment;
    ShopDatabase db;
    UserSettings settings;


    public RecyclerViewFavTouchHelper(ShopFavouritesAdapter adapter, UserSettings settings, FavouritesFragment favouritesFragment) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.favouritesFragment = favouritesFragment;
        this.settings = settings;

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
            adapter.setSelectList(viewHolder.getAdapterPosition());
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
        favouritesFragment.showDeleteDialog(position);
    }

    public void checkItem(int position) {
        String temp = adapter.getItemName(position);
        String temp_category = adapter.getCateName(position);
        int status = 0;
        Cursor res = db.getStatus(temp_category, temp);
        while (res.moveToNext()) {
            status = res.getInt(3);
        }
        if (status == 1) {
            db.updateStatus(temp_category, temp, 0);
        } else {
            db.updateStatus(temp_category, temp, 1);
        }
        adapter.notifyItemChanged(position);
    }

    public void changePrice(int position) {
        String temp = adapter.getItemName(position);
        // String temp_category = adapter.getCateName(position);
        favouritesFragment.showPriceDialog(temp, position);
    }

    public void rename(int position) {
        String temp = adapter.getItemName(position);
        // String temp_category = adapter.getCateName(position);
        favouritesFragment.showRenameDialog(temp, position);
    }

    public void changeQuantity(int position) {
        String temp = adapter.getItemName(position);
        // String temp_category = adapter.getCateName(position);
        favouritesFragment.showQuantityDialog(temp, position);
    }

    public void show_options(int position) {
        String temp = adapter.getItemName(position);
        // String temp_category = adapter.getCateName(position);
        favouritesFragment.showEditDialog(temp, position);
    }

    public void star_item(int position) {
        String temp = adapter.getItemName(position);
        String temp_category = adapter.getCateName(position);
        int star_status = 0;
        Cursor res = db.getFavourites(temp_category, temp);
        while (res.moveToNext()) {
            star_status = res.getInt(10);
        }
        if (star_status == 1) {
            db.updateFavourites(temp_category, temp, 0);
            adapter.removeItem(position);
        } else {
            db.updateFavourites(temp_category, temp, 1);
        }
        if (!adapter.checkEmpty()) {
            adapter.notifyDataSetChanged();
        }
        favouritesFragment.displayData();

    }
}
