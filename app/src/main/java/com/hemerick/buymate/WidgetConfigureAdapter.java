package com.hemerick.buymate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class WidgetConfigureAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> list_cate;

    LayoutInflater inflater;

    public WidgetConfigureAdapter(Context context, ArrayList<String> list_cate) {
        this.context = context;
        this.list_cate = list_cate;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list_cate.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.move_item_layout, null);
        TextView textView = convertView.findViewById(R.id.move_category_box);
        textView.setText(list_cate.get(position));
        return convertView;
    }
}
