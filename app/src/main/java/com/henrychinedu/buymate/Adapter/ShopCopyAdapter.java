package com.henrychinedu.buymate.Adapter;

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

import com.henrychinedu.buymate.Database.ShopDatabase;
import com.henrychinedu.buymate.Database.UserSettings;
import com.henrychinedu.buymate.ItemActivity;
import com.henrychinedu.buymate.R;

import java.util.ArrayList;

public class ShopCopyAdapter extends RecyclerView.Adapter<ShopCopyAdapter.MyCopyViewHolder> {

    Context context;
    ArrayList<String> items;
    String category;
    ShopDatabase db;
    ItemActivity itemActivity;
    UserSettings settings;


    public ShopCopyAdapter(Context context, UserSettings settings, ArrayList<String> items, String category, ItemActivity itemActivity) {
        this.context = context;
        this.items = items;
        this.category = category;
        this.itemActivity = itemActivity;
        this.settings = settings;
        db = new ShopDatabase(context.getApplicationContext());
    }

    public void setFilterList(ArrayList<String> filterList) {
        this.items = filterList;
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public MyCopyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.move_item_layout, parent, false);
        return new MyCopyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCopyViewHolder holder, @SuppressLint("RecyclerView") int position) {
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
                String temp_photourl = null;

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
                    temp_photourl = res2.getString(12);
                }
                res2.close();

                db.insertItem(selectedCategory, newItem, status, temp_price, temp_month, temp_year, temp_day, temp_time, temp_quantity, unit);
                db.updateFavourites(selectedCategory, newItem, temp_fav);
                db.updatePhoto(selectedCategory, newItem, temp_photourl);


            } else {
                int temp_fav = 0;
                String temp_price = null;
                String temp_quantity = null;
                int status = 0;

                String temp_month = null;
                String temp_year = null;
                String temp_day = null;
                String temp_time = null;
                String unit = null;
                String temp_photourl = null;

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
                    temp_photourl = res2.getString(12);
                }
                res2.close();

                db.insertItem(selectedCategory, currentItem, status, temp_price, temp_month, temp_year, temp_day, temp_time, temp_quantity, unit);
                db.updateFavourites(selectedCategory, currentItem, temp_fav);
                db.updatePhoto(selectedCategory, currentItem, temp_photourl);
            }
        }

        Toast.makeText(context, context.getString(R.string.ShopCopyAdapter__copy_successful) + " " + selectedCategory, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(context, ItemActivity.class);
        intent.putExtra("ITEM", previousCategory);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MyCopyViewHolder extends RecyclerView.ViewHolder {

        TextView nameView;

        public MyCopyViewHolder(@NonNull View itemView) {
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