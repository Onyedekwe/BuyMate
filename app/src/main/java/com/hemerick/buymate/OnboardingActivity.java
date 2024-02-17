package com.hemerick.buymate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hemerick.buymate.Adapter.ViewPagerAdapter;
import com.hemerick.buymate.Database.UserSettings;

public class OnboardingActivity extends AppCompatActivity {

    ViewPager viewPager;
    LinearLayout linearLayout;
            Button skipBtn, backBtn, nextBtn;
            ImageButton back_top_btn;
    TextView[] dots;
    ViewPagerAdapter viewPagerAdapter;

    private UserSettings settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        settings = new UserSettings();

        skipBtn = findViewById(R.id.skip_button);
        backBtn = findViewById(R.id.back_button);
        nextBtn = findViewById(R.id.next_button);
        back_top_btn = findViewById(R.id.back_top_button);

        back_top_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                settings.setIsAppFirstStart(UserSettings.NO_APP_NOT_FIRST_START);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.APP_FIRST_START, settings.getIsAppFirstStart());
                editor.apply();

                Intent i = new Intent(OnboardingActivity.this, SignUpActivity.class);
                startActivity(i);
                finish();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getItem(0) > 0)
                    viewPager.setCurrentItem(getItem(-1), true);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getItem(0) < 2)
                    viewPager.setCurrentItem(getItem(1), true);
                else{

                    settings.setIsAppFirstStart(UserSettings.NO_APP_NOT_FIRST_START);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.APP_FIRST_START, settings.getIsAppFirstStart());
                    editor.apply();

                    Intent i = new Intent(OnboardingActivity.this, SignUpActivity.class);
                    startActivity(i);
                    finish();
                }

            }
        });

        viewPager = findViewById(R.id.onboard_viewpager);
        linearLayout = (LinearLayout) findViewById(R.id.indicator_layout);

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        setIndicator(0);
        viewPager.addOnPageChangeListener(viewListener);

    }

    public void setIndicator(int position){
        dots = new TextView[3];
        linearLayout.removeAllViews();

        for(int i = 0; i < dots.length; i++){
            dots[i] = new TextView(this);
            dots[i].setText("●");
            dots[i].setTextSize(20);
            dots[i].setTextColor(getResources().getColor(R.color.dark_grey, getApplicationContext().getTheme()));
            linearLayout.addView(dots[i]);
        }

        dots[position].setTextColor(getResources().getColor(R.color.black, getApplicationContext().getTheme()));
    }
   ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
       @Override
       public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

       }

       @Override
       public void onPageSelected(int position) {
        setIndicator(position);

        if(position > 0){
            backBtn.setVisibility(View.VISIBLE);
        }else{
            backBtn.setVisibility(View.INVISIBLE);
        }

        if(position == 2){
            nextBtn.setText(getString(R.string.OnboardingActivity__Start));
        }else{
            nextBtn.setText(getString(R.string.OnboardingActivity__Next));
        }

       }

       @Override
       public void onPageScrollStateChanged(int state) {

       }
   } ;

    private int getItem(int i){
        return  viewPager.getCurrentItem() + i;
    }


}