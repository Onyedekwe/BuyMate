package com.hemerick.buymate.Widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.R;

import java.util.ArrayList;

public class WidgetConfigAdapter extends RecyclerView.Adapter<WidgetConfigAdapter.MyViewHolder> {

    private final Context context;
    private final OnNoteListener onNoteListener;
    ShopDatabase db;
    boolean isEnable = false;
    boolean isSelectAll = false;
    ArrayList<String> selectList = new ArrayList<>();
    UserSettings settings;
    private ArrayList<String> shop_id;


    public WidgetConfigAdapter(Context context, UserSettings settings, ArrayList<String> shop_id, OnNoteListener onNoteListener) {
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
        View v = LayoutInflater.from(context).inflate(R.layout.widget_config_item_layout, parent, false);
        return new MyViewHolder(v, onNoteListener);

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.cateName.setText(String.valueOf(shop_id.get(position)));

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
            holder.cateCount.setText(checked + "/" + items.size());

            int progress = (checked * 100) / items.size();

            holder.categoryProgressBar.setProgress(progress);

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




    public boolean isSelected() {
        return isEnable;
    }



    public interface OnNoteListener {
        void onNoteClick(int position);

        void onItemLongClick(int position);

        void onOptionClick(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        CardView categoryCardView;
        TextView cateName, cateCount;

        ProgressBar categoryProgressBar;
        OnNoteListener onNoteListener;


        public MyViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);

            categoryCardView = itemView.findViewById(R.id.category_cardView);
            cateName = itemView.findViewById(R.id.favourites_category_hidden_name);
            cateCount = itemView.findViewById(R.id.textView);

            categoryProgressBar = itemView.findViewById(R.id.categotyProgressBar);

            this.onNoteListener = onNoteListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                cateName.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.medium_text));
                cateCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.mini_text));
            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                cateName.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.max_max_text));
                cateCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.maxi_text));
            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                cateName.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.max_max_max_text));
                cateCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.max_max_text));
            }



        }

        @Override
        public void onClick(View v) {
                onNoteListener.onNoteClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return true;
        }
    }
}