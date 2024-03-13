package com.hemerick.buymate.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.NotesFragment;
import com.hemerick.buymate.R;

import java.util.ArrayList;

public class ShopNotesAdapter extends RecyclerView.Adapter<ShopNotesAdapter.MyViewHolder> {

    private final Context context;
    private final OnNoteListener onNoteListener;
    ShopDatabase db;
    boolean isEnable = false;
    boolean isSelectAll = false;
    ArrayList<String> selectListHeading = new ArrayList<>();
    ArrayList<String> selectListContent = new ArrayList<>();
    ArrayList<String> selectListDate = new ArrayList<>();
    NotesFragment notesFragment;
    UserSettings settings;
    private ArrayList<String> heading_id;
    private ArrayList<String> content_id;
    private ArrayList<String> dates_id;


    public ShopNotesAdapter(Context context, UserSettings settings, ArrayList<String> heading_id, ArrayList<String> content_id, ArrayList<String> dates_id, OnNoteListener onNoteListener, NotesFragment notesFragment) {
        this.context = context;
        this.heading_id = heading_id;
        this.content_id = content_id;
        this.dates_id = dates_id;
        this.onNoteListener = onNoteListener;
        this.notesFragment = notesFragment;
        this.settings = settings;
        db = new ShopDatabase(context);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.notes_layout, parent, false);
        return new MyViewHolder(v, onNoteListener);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true);
        int shadowColor = typedValue.data;

        holder.heading_box.setText(String.valueOf(heading_id.get(position)));
        if (content_id.get(position).trim().isEmpty()) {
            holder.content_box.setVisibility(View.GONE);
        } else {
            if (holder.content_box.getVisibility() == View.GONE) {
                holder.content_box.setVisibility(View.VISIBLE);
                holder.content_box.setText(String.valueOf(content_id.get(position)));
            } else {
                holder.content_box.setText(String.valueOf(content_id.get(position)));
            }
        }
        holder.date_box.setText(String.valueOf(dates_id.get(position)));


        if (isSelectAll) {
            holder.cardView.setBackgroundColor(shadowColor);
        } else {
            if (selectListHeading.contains(heading_id.get(position))) {
                holder.cardView.setBackgroundColor(shadowColor);
            } else {
                holder.cardView.setBackgroundColor(Color.TRANSPARENT);
            }
        }

    }


    @Override
    public int getItemCount() {
        return heading_id.size();
    }

    public Context getContext() {
        return context;
    }

    public String getHeadingName(int position) {
        return heading_id.get(position);
    }

    public String getContentName(int position) {
        return content_id.get(position);
    }

    public String getDateName(int position) {
        return dates_id.get(position);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setFilterList(ArrayList<String> filterList, ArrayList<String> filterList2, ArrayList<String> filterList3) {
        this.heading_id = filterList;
        this.content_id = filterList2;
        this.dates_id = filterList3;
        notifyDataSetChanged();
    }


    public boolean checkEmpty() {
        return heading_id.isEmpty();
    }


    public boolean isSelected() {
        return isEnable;
    }

    public ArrayList<String> getSelectListHeading() {
        return selectListHeading;
    }


    public ArrayList<String> getSelectListContent() {
        return selectListContent;
    }

    public ArrayList<String> getSelectListDate() {
        return selectListDate;
    }


    public void setSelectAll() {
        if (selectListHeading.size() == heading_id.size()) {
            isEnable = false;
            isSelectAll = false;
            selectListHeading.clear();
        } else {
            isSelectAll = true;
            selectListHeading.clear();
            selectListContent.clear();
            selectListDate.clear();
            selectListHeading.addAll(heading_id);
            selectListContent.addAll(content_id);
            selectListDate.addAll(dates_id);
        }
        notifyDataSetChanged();
        ((AppCompatActivity) getContext()).supportInvalidateOptionsMenu();
    }

    public void disableSelection() {
        isEnable = false;
        isSelectAll = false;
        selectListHeading.clear();
        selectListContent.clear();
        selectListDate.clear();
        ((AppCompatActivity) getContext()).supportInvalidateOptionsMenu();
    }

    public interface OnNoteListener {
        void onNoteClick(int position);

        void onItemLongClick(int position);

        void onOptionClick(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView heading_box;
        TextView content_box;
        TextView date_box;
        OnNoteListener onNoteListener;
        LinearLayout cardView;


        public MyViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            heading_box = itemView.findViewById(R.id.note_heading_box);
            content_box = itemView.findViewById(R.id.note_content_box);
            date_box = itemView.findViewById(R.id.date_box);
            cardView = itemView.findViewById(R.id.cardView);


            this.onNoteListener = onNoteListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                heading_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.small_text));
                content_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.small_text));
                date_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.small_text));
            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                heading_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.medium_text));
                content_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.medium_text));
                date_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.small_text));

            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                heading_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.large_text));
                content_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.large_text));
                date_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.medium_text));
            }

        }

        @Override
        public void onClick(View v) {

            if (isEnable) {
                TypedValue typedValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true);
                int shadowColor = typedValue.data;

                Drawable backgroundDrawable = cardView.getBackground();
                int backgroundColor = ((ColorDrawable) backgroundDrawable).getColor();
                String s = heading_id.get(getAdapterPosition());
                String t = content_id.get(getAdapterPosition());
                String p = dates_id.get(getAdapterPosition());
                if (backgroundColor == shadowColor) {
                    cardView.setBackgroundColor(Color.TRANSPARENT);
                    selectListHeading.remove(s);
                    selectListContent.remove(t);
                    selectListDate.remove(p);
                    ((AppCompatActivity) v.getContext()).supportInvalidateOptionsMenu();
                    if (selectListHeading.isEmpty()) {
                        isEnable = false;
                        ((AppCompatActivity) v.getContext()).supportInvalidateOptionsMenu();
                    }
                } else {
                    cardView.setBackgroundColor(shadowColor);
                    selectListHeading.add(s);
                    selectListContent.add(t);
                    selectListDate.add(p);
                    ((AppCompatActivity) v.getContext()).supportInvalidateOptionsMenu();
                }

            } else {
                onNoteListener.onItemLongClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true);
            int shadowColor = typedValue.data;
            if (!isEnable) {
                String s = heading_id.get(getAdapterPosition());
                String t = content_id.get(getAdapterPosition());
                String p = dates_id.get(getAdapterPosition());
                cardView.setBackgroundColor(shadowColor);
                isEnable = true;
                selectListHeading.add(s);
                selectListContent.add(t);
                selectListDate.add(p);
                ((AppCompatActivity) v.getContext()).supportInvalidateOptionsMenu();
            } else {
                String s = heading_id.get(getAdapterPosition());
                String t = content_id.get(getAdapterPosition());
                String p = dates_id.get(getAdapterPosition());
                cardView.setBackgroundColor(shadowColor);
                selectListHeading.add(s);
                selectListContent.add(t);
                selectListDate.add(p);

            }
            return true;
        }
    }
}