package com.hemerick.buymate.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hemerick.buymate.Database.Firebase;
import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.HomeActivity;
import com.hemerick.buymate.ItemActivity;
import com.hemerick.buymate.R;

import java.util.ArrayList;

public class ShopMoveAdapter extends RecyclerView.Adapter<ShopMoveAdapter.MyMoveViewHolder> {

    Context context;
    ArrayList<String> items;
    String category;
    ShopDatabase db;
    ItemActivity itemActivity;
    UserSettings settings;
    Firebase firebase;


    public ShopMoveAdapter(Context context, UserSettings settings, ArrayList<String> items, String category, ItemActivity itemActivity) {
        this.context = context;
        this.items = items;
        this.category = category;
        this.itemActivity = itemActivity;
        this.settings = settings;
        db = new ShopDatabase(context.getApplicationContext());
        firebase = new Firebase(context);
    }

    public void setFilterList(ArrayList<String> filterList) {
        this.items = filterList;
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public MyMoveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.move_item_layout, parent, false);
        return new MyMoveViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyMoveViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.nameView.setText(String.valueOf(items.get(position)));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertMoved(String.valueOf(items.get(position)));
            }
        });
    }

    public void insertMoved(String cats) {
        ArrayList<String> selectedItems = itemActivity.getSelectedItems();
        ArrayList<String> itemDestinationList = new ArrayList<>();
        String selectedCategory = cats;
        String previousCategory = category;

        Cursor res = db.getItems(selectedCategory, context);
        while (res.moveToNext()) {
            itemDestinationList.add(res.getString(2));
        }
        res.close();


        for (int i = 0; i < selectedItems.size(); i++) {
            String currentItem = selectedItems.get(i);
            if (itemDestinationList.contains(currentItem)) {
                int count = 1;
                String newItem = currentItem + "(" + count + ")";
                while (itemDestinationList.contains(newItem)) {
                    count++;
                    newItem = currentItem + "(" + count + ")";
                }
                int temp_fav = 0;
                String temp_price = null;
                String temp_quantity = null;
                int status = 0;

                String temp_month = null;
                String temp_year = null;
                String temp_day = null;
                String temp_time = null;
                String unit = null;

                Cursor res2 = db.getPrice(previousCategory, currentItem);
                while (res2.moveToNext()) {
                    status = res2.getInt(3);
                    temp_fav = res2.getInt(10);
                    temp_price = res2.getString(4);
                    temp_quantity = res2.getString(9);
                    temp_month = res2.getString(5);
                    temp_year = res2.getString(6);
                    temp_day = res2.getString(7);
                    temp_time = res2.getString(8);
                    unit = res2.getString(11);
                }
                res2.close();

                db.deleteItem(previousCategory, currentItem);
                db.insertItem(selectedCategory, newItem, status, temp_price, temp_month, temp_year, temp_day, temp_time, temp_quantity, unit);
                db.updateFavourites(selectedCategory, newItem, temp_fav);
                itemActivity.removeItem(currentItem);

            } else {
                db.moveItem(previousCategory, selectedCategory, currentItem);
                firebase.moveItem(previousCategory, selectedCategory, currentItem);
                itemActivity.removeItem(currentItem);
            }
        }
        int checkEmpty = itemActivity.getListCount();
        if (checkEmpty == 0) {
            Intent intent = new Intent(context, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        } else {

            Toast.makeText(context, context.getString(R.string.moved) + " " + selectedCategory, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, ItemActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("ITEM", previousCategory);
            context.startActivity(intent);
        }
    }


    public String getClicked() {
        return category;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MyMoveViewHolder extends RecyclerView.ViewHolder {

        TextView nameView;

        public MyMoveViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.move_category_box);

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                nameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.small_text));
            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                nameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.medium_text));
            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                nameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.large_text));
            }
        }
    }
}