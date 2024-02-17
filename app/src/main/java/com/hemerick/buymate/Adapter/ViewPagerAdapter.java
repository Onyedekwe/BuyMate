package com.hemerick.buymate.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.hemerick.buymate.R;

public class ViewPagerAdapter extends PagerAdapter {

    Context context;
    int image[] = {
            R.drawable.illustration_always_remember_what_to_buy,
            R.drawable.illustration_budget_wisely,
            R.drawable.illustration_share_your_list
    };

    int heading[] = {
            R.string.ViewPagerAdapter_heading_1,
            R.string.ViewPagerAdapter_heading_2,
            R.string.ViewPagerAdapter_heading_3,
    };

    int description[] = {
            R.string.ViewPagerAdapter_sub_heading_1,
            R.string.ViewPagerAdapter_sub_heading_2,
            R.string.ViewPagerAdapter_sub_heading_3,
    };

    @Override
    public int getCount() {
        return heading.length;
    }

    public ViewPagerAdapter(Context context){
        this.context = context;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_layout, container,false);

        ImageView slideTitleImage = (ImageView) view.findViewById(R.id.onboardImage);
        TextView slideHeading = (TextView) view.findViewById(R.id.onboardHeading);
        TextView slideSubHeading = (TextView) view.findViewById(R.id.onboardsub_heading);

        slideTitleImage.setImageResource(image[position]);
        slideHeading.setText(heading[position]);
        slideSubHeading.setText(description[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {


        container.removeView((LinearLayout) object);
    }
}
