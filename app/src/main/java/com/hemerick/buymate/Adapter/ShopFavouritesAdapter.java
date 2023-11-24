package com.hemerick.buymate.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.FavouritesFragment;
import com.hemerick.buymate.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ShopFavouritesAdapter extends RecyclerView.Adapter<ShopFavouritesAdapter.MyViewHolder> {

    private final Context context;
    private final OnNoteListener onNoteListener;
    private final ArrayList<String> shop_id;
    private final ArrayList<String> item_id;
    ShopDatabase db;
    boolean isEnable = false;
    boolean isSelectAll = false;
    ArrayList<String> selectList = new ArrayList<>();
    ArrayList<String> selectListCategory = new ArrayList<>();
    FavouritesFragment favouritesFragment;
    UserSettings settings;

    public ShopFavouritesAdapter(Context context, UserSettings settings, ArrayList<String> shop_id, ArrayList<String> item_id, OnNoteListener onNoteListener, FavouritesFragment favouritesFragment) {
        this.context = context;
        this.shop_id = shop_id;
        this.item_id = item_id;
        this.settings = settings;
        this.onNoteListener = onNoteListener;
        this.favouritesFragment = favouritesFragment;
        db = new ShopDatabase(context.getApplicationContext());
    }

    public static String formatNumber(double number, String unit) {
        if (number == 1.0) {
            return " ";
        } else {

            if (number == (long) number) {
                if (unit.trim().isEmpty()) {
                    return "[" + String.format("%.0f", number) + "]";
                } else {
                    return "[" + String.format("%.0f", number) + " " + unit.trim() + "]";
                }

            } else {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                if (unit.trim().isEmpty()) {
                    return "[" + decimalFormat.format(number) + "]";
                } else {
                    return "[" + decimalFormat.format(number) + " " + unit.trim() + "]";
                }

            }
        }
    }

    public static String formatNumberV2(double number) {
        if (number == (long) number) {
            return String.format("%.0f", number);
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            return decimalFormat.format(number);

        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.favourites_layout, parent, false);
        return new MyViewHolder(v, onNoteListener);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true);
        int shadowColor = typedValue.data;

        holder.cateBox.setText(String.valueOf(shop_id.get(position)));
        holder.textBox.setText(String.valueOf(item_id.get(position)));
        Cursor res = db.getStatus(String.valueOf(shop_id.get(position)), holder.textBox.getText().toString());
        while (res.moveToNext()) {
            holder.checkBox.setChecked(getStat(res.getInt(3)));
            if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
                String temp_price = res.getString(4);
                String temp_quantity = res.getString(9);
                String unit = res.getString(11);
                String temp_sum = String.valueOf(Double.parseDouble(temp_price) * Double.parseDouble(temp_quantity));
                holder.priceBox.setText(formatNumberV2(Double.parseDouble(temp_sum)));
                holder.quantityBox.setText(formatNumber(Double.parseDouble(temp_quantity), unit));

            } else {
                holder.priceBox.setText(formatNumberV2(Double.parseDouble(res.getString(4))));
                String qty = res.getString(9);
                String unit = res.getString(11);
                holder.quantityBox.setText(formatNumber(Double.parseDouble(qty), unit));

            }
            holder.favourites.setImageResource(checkFavourites(String.valueOf(shop_id.get(position)), String.valueOf(item_id.get(position))));


        }
        res.close();

        if (isEnable) {
            holder.options.setVisibility(View.GONE);
        } else {
            holder.options.setVisibility(View.VISIBLE);
        }

        if (selectList.isEmpty()) {
            holder.options.setVisibility(View.VISIBLE);
        } else {
            holder.options.setVisibility(View.GONE);
        }


        if (isSelectAll) {
            holder.cardView.setBackgroundColor(shadowColor);

        } else {
            if (selectList.contains(item_id.get(position))) {
                holder.cardView.setBackgroundColor(shadowColor);
            } else {
                holder.cardView.setBackgroundColor(Color.TRANSPARENT);
            }
        }

    }

    public void removeItem(int position) {
        shop_id.remove(position);
        item_id.remove(position);
    }

    public int checkFavourites(String category, String item) {
        Cursor res = db.getFavourites(category, item);
        int newIconResource = 0;
        int fav = 0;
        while (res.moveToNext()) {
            fav = res.getInt(10);
        }
        res.close();
        if (fav == 1) {
            newIconResource = R.drawable.final_favourites_colored_icon;

        }
        return newIconResource;
    }

    @Override
    public int getItemCount() {
        return shop_id.size();
    }

    public Context getContext() {
        return context;
    }

    public String getItemName(int position) {
        return item_id.get(position);
    }

    public String getCateName(int position) {
        return shop_id.get(position);
    }


    public boolean getStat(int stat) {
        return stat == 1;
    }

    public void refreshRemoved(int position) {
        item_id.remove(position);
        shop_id.remove(position);

    }


    public void longClick(int position) {
        onNoteListener.onOptionClick(position);
    }

    public boolean checkEmpty() {
        return item_id.isEmpty();
    }

    public void refreshUpdate(String task, int position) {
        item_id.set(position, task);
    }

    public boolean isSelected() {
        return isEnable;
    }

    public ArrayList<String> getSelectList() {
        return selectList;
    }

    public void setSelectList(int position) {
        selectList.remove(item_id.get(position));
        selectListCategory.remove(shop_id.get(position));
        if (selectList.isEmpty()) {
            isEnable = false;
            isSelectAll = false;
        }
        ((AppCompatActivity) getContext()).supportInvalidateOptionsMenu();
    }

    public ArrayList<String> getSelectListCategory() {
        return selectListCategory;
    }

    public void setSelectAll() {
        if (selectList.size() == shop_id.size()) {
            isEnable = false;
            isSelectAll = false;
            selectList.clear();
        } else {
            isSelectAll = true;
            selectList.clear();
            selectListCategory.clear();
            selectList.addAll(item_id);
            selectListCategory.addAll(shop_id);
        }
        notifyDataSetChanged();
        ((AppCompatActivity) getContext()).supportInvalidateOptionsMenu();
    }

    public void disableSelection() {
        isEnable = false;
        isSelectAll = false;
        selectList.clear();
        selectListCategory.clear();
        notifyDataSetChanged();
        ((AppCompatActivity) getContext()).supportInvalidateOptionsMenu();
    }

    public interface OnNoteListener {
        void onNoteClick(int position);

        void onItemLongClick(int position);

        void onOptionClick(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        CheckBox checkBox;
        TextView textBox;
        TextView cateBox;
        TextView priceBox;
        TextView quantityBox;
        TextView currencyBox;
        ImageView options;
        ImageView favourites;
        OnNoteListener onNoteListener;
        LinearLayout cardView;


        public MyViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            checkBox = itemView.findViewById(R.id.checkbox);
            textBox = itemView.findViewById(R.id.textbox);
            cateBox = itemView.findViewById(R.id.favourites_category_hidden_name);
            priceBox = itemView.findViewById(R.id.pricebox);
            quantityBox = itemView.findViewById(R.id.quantity_box);
            options = itemView.findViewById(R.id.optionbox);
            favourites = itemView.findViewById(R.id.favourite_box);
            currencyBox = itemView.findViewById(R.id.currencybox);

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                textBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.small_text));
                priceBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.small_text));
                currencyBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.small_text));
                quantityBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.small_text));
            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                textBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.medium_text));
                priceBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.maxi_text));
                currencyBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.maxi_text));
                quantityBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.medium_text));
            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                textBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.large_text));
                priceBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.medium_text));
                currencyBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.medium_text));
                quantityBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.large_text));
            }

            currencyBox.setText(settings.getCurrency());

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        db.updateStatus(cateBox.getText().toString(), textBox.getText().toString(), 1);
                        if (settings.getIsCrossDisabled().equals(UserSettings.NO_CROSS_NOT_DISABLED)) {
                            textBox.setPaintFlags(textBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        }
                    } else {
                        db.updateStatus(cateBox.getText().toString(), textBox.getText().toString(), 0);
                        if (settings.getIsCrossDisabled().equals(UserSettings.NO_CROSS_NOT_DISABLED)) {
                            textBox.setPaintFlags(textBox.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        }

                    }
                    ((AppCompatActivity) context).supportInvalidateOptionsMenu();
                    favouritesFragment.getSum();
                }
            });

            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isEnable) {

                    } else {
                        onNoteListener.onOptionClick(getAdapterPosition());
                    }
                }
            });
            this.onNoteListener = onNoteListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (isEnable) {
                TypedValue typedValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true);
                int shadowColor = typedValue.data;

                Drawable backgroundDrawable = cardView.getBackground();
                int backgroundColor = ((ColorDrawable) backgroundDrawable).getColor();
                String s = item_id.get(getAdapterPosition());
                String t = shop_id.get(getAdapterPosition());
                if (backgroundColor == shadowColor) {
                    cardView.setBackgroundColor(Color.TRANSPARENT);
                    selectList.remove(s);
                    selectListCategory.remove(t);
                    ((AppCompatActivity) v.getContext()).supportInvalidateOptionsMenu();
                    if (selectList.isEmpty()) {
                        isEnable = false;
                        ((AppCompatActivity) v.getContext()).supportInvalidateOptionsMenu();
                        notifyDataSetChanged();
                    }
                } else {
                    cardView.setBackgroundColor(shadowColor);
                    selectList.add(s);
                    selectListCategory.add(t);
                    ((AppCompatActivity) v.getContext()).supportInvalidateOptionsMenu();

                }

            } else {
                onNoteListener.onNoteClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true);
            int shadowColor = typedValue.data;
            if (!isEnable) {
                String s = item_id.get(getAdapterPosition());
                String t = shop_id.get(getAdapterPosition());
                cardView.setBackgroundColor(shadowColor);
                isEnable = true;
                selectList.add(s);
                selectListCategory.add(t);
                ((AppCompatActivity) v.getContext()).supportInvalidateOptionsMenu();
                notifyDataSetChanged();
            } else {
                String s = item_id.get(getAdapterPosition());
                String t = shop_id.get(getAdapterPosition());
                cardView.setBackgroundColor(shadowColor);
                selectList.add(s);
                selectListCategory.add(t);
            }
            return true;
        }
    }
}