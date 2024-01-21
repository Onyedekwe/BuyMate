package com.hemerick.buymate.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.HomeActivity;
import com.hemerick.buymate.R;

import java.util.ArrayList;

public class ShopCategoryAdapter extends RecyclerView.Adapter<ShopCategoryAdapter.MyViewHolder> {

    private final Context context;
    private final OnNoteListener onNoteListener;
    ShopDatabase db;
    boolean isEnable = false;
    boolean isSelectAll = false;
    ArrayList<String> selectList = new ArrayList<>();
    UserSettings settings;
    private ArrayList<String> shop_id;


    public ShopCategoryAdapter(Context context, UserSettings settings, ArrayList<String> shop_id, OnNoteListener onNoteListener) {
        this.context = context;
        this.shop_id = shop_id;
        this.onNoteListener = onNoteListener;
        this.settings = settings;
        db = new ShopDatabase(context.getApplicationContext());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilterList(ArrayList<String> filterList) {
        this.shop_id = filterList;
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.category_layout, parent, false);
        return new MyViewHolder(v, onNoteListener);

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true);
        int shadowColor = typedValue.data;


        context.getTheme().resolveAttribute(com.google.android.material.R.attr.backgroundColor, typedValue, true);
        int backgroundColor = typedValue.data;

        context.getTheme().resolveAttribute(android.R.attr.color, typedValue, true);
        int shadowColor2 = typedValue.data;

        holder.category.setText(String.valueOf(shop_id.get(position)));

        Cursor res = db.getItems(String.valueOf(shop_id.get(position)), context);
        ArrayList<String> items = new ArrayList<>();
        while (res.moveToNext()) {
            items.add(res.getString(2).trim());
        }

        ArrayList<Integer> status = new ArrayList<>();
        res = db.getItems(String.valueOf(shop_id.get(position)), context);
        while (res.moveToNext()) {
            status.add(res.getInt(3));
        }

        int checked = 0;
        for (int i : status) {
            if (i == 1) {
                checked++;
            }
            holder.num.setText(checked + "/" + items.size());

            int progress = (checked * 100) / items.size();

            holder.categoryProgress.setProgress(progress);

        }

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
            holder.categoryCardView.setBackgroundColor(shadowColor);
            holder.title_layout.setBackgroundColor(backgroundColor);
            holder.title_layout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_transparent_curved_rectangle));

        } else {
            if (selectList.contains(shop_id.get(position))) {
                holder.categoryCardView.setBackgroundColor(shadowColor);
                holder.title_layout.setBackgroundColor(backgroundColor);
                holder.title_layout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_transparent_curved_rectangle));

            } else {
                holder.categoryCardView.setBackgroundColor(Color.TRANSPARENT);
                holder.title_layout.setBackgroundColor(shadowColor2);
                holder.title_layout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_transparent_curved_rectangle));
            }
        }

    }

    @Override
    public int getItemCount() {
        return shop_id.size();
    }

    public Context getContext() {
        return context;
    }

    public String getItemName(int position) {
        return shop_id.get(position);
    }


    public void refreshRemoved(int position) {
        shop_id.remove(position);
    }

    public boolean isSelected() {
        return isEnable;
    }

    public void removeItemFromSelectList(int position) {
        selectList.remove(shop_id.get(position));
        if (selectList.isEmpty()) {
            isEnable = false;
            isSelectAll = false;
        }
        ((AppCompatActivity) getContext()).supportInvalidateOptionsMenu();
    }

    public void disableSelection() {
        isEnable = false;
        isSelectAll = false;
        selectList.clear();
        notifyDataSetChanged();
        ((AppCompatActivity) getContext()).supportInvalidateOptionsMenu();
    }

    public void setSelectAll() {
        if (selectList.size() == shop_id.size()) {
            isEnable = false;
            isSelectAll = false;
            selectList.clear();
        } else {
            isSelectAll = true;
            selectList.clear();
            selectList.addAll(shop_id);
        }
        notifyDataSetChanged();
        ((AppCompatActivity) getContext()).supportInvalidateOptionsMenu();
    }

    public void checkEmpty() {
        if (shop_id.isEmpty()) {
            Intent intent = new Intent(context, HomeActivity.class);
            context.startActivity(intent);
        } else {
            notifyDataSetChanged();
        }
    }

    public ArrayList<String> getSelectList() {
        return selectList;
    }

    public void refreshUpdate(String task, int position) {
        shop_id.set(position, task);
    }

    public interface OnNoteListener {
        void onNoteClick(int position);

        void onItemLongClick(int position);

        void onOptionClick(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        LinearLayout categoryCardView;
        ConstraintLayout title_layout;
        TextView category;
        ProgressBar categoryProgress;
        TextView num;
        ImageView options;
        OnNoteListener onNoteListener;


        public MyViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);

            categoryCardView = itemView.findViewById(R.id.categoryLinearLayout);
            title_layout = itemView.findViewById(R.id.title_layout);
            category = itemView.findViewById(R.id.favourites_category_hidden_name);
            categoryProgress = itemView.findViewById(R.id.categotyProgressBar);
            num = itemView.findViewById(R.id.textView);
            options = itemView.findViewById(R.id.categotyOptionbox);

            this.onNoteListener = onNoteListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                category.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.small_text));
                num.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.small_text));
            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                category.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.medium_text));
                num.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.medium_text));
            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                category.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.large_text));
                num.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.large_text));
            }

            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNoteListener.onOptionClick(getAdapterPosition());
                }
            });


        }

        @Override
        public void onClick(View v) {
            if (isEnable) {
                TypedValue typedValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true);
                int shadowColor = typedValue.data;

                context.getTheme().resolveAttribute(com.google.android.material.R.attr.backgroundColor, typedValue, true);
                int baColor = typedValue.data;

                context.getTheme().resolveAttribute(android.R.attr.color, typedValue, true);
                int shadowColor2 = typedValue.data;

                Drawable backgroundDrawable = categoryCardView.getBackground();
                int backgroundColor = ((ColorDrawable) backgroundDrawable).getColor();
                String s = shop_id.get(getAdapterPosition());
                if (backgroundColor == shadowColor) {
                    categoryCardView.setBackgroundColor(Color.TRANSPARENT);
                    title_layout.setBackgroundColor(shadowColor2);
                    title_layout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_transparent_curved_rectangle));
                    selectList.remove(s);
                    ((AppCompatActivity) v.getContext()).supportInvalidateOptionsMenu();
                    if (selectList.isEmpty()) {
                        isEnable = false;
                        ((AppCompatActivity) v.getContext()).supportInvalidateOptionsMenu();
                        notifyDataSetChanged();
                    }
                } else {
                    categoryCardView.setBackgroundColor(shadowColor);
                    title_layout.setBackgroundColor(baColor);
                    title_layout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_transparent_curved_rectangle));
                    selectList.add(s);
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
                isEnable = true;
                String s = shop_id.get(getAdapterPosition());
                selectList.add(s);
                categoryCardView.setBackgroundColor(shadowColor);
                title_layout.setBackgroundColor(Color.TRANSPARENT);
                ((AppCompatActivity) v.getContext()).supportInvalidateOptionsMenu();
                notifyDataSetChanged();

            } else {
                String s = shop_id.get(getAdapterPosition());
                categoryCardView.setBackgroundColor(shadowColor);
                title_layout.setBackgroundColor(Color.TRANSPARENT);
                selectList.add(s);
            }
            return true;
        }
    }
}