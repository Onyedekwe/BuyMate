package com.hemerick.buymate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hemerick.buymate.Adapter.RecyclerViewItemTouchHelper;
import com.hemerick.buymate.Adapter.ShopCopyAdapter;
import com.hemerick.buymate.Adapter.ShopItemAdapter;
import com.hemerick.buymate.Adapter.ShopMoveAdapter;
import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.DashedBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

import io.github.muddz.styleabletoast.StyleableToast;

public class ItemActivity extends AppCompatActivity implements ShopItemAdapter.OnNoteListener, SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeRefreshLayout;
    String category;
    ProgressBar progressBar;
    boolean isFirstStart;

    ConstraintLayout favSumLayout;

    String temp_item;
    Boolean is_photo_url_empty = false;
    Toolbar itemToolbar;
    SearchView searchView;
    EditText searchEditText;
    ArrayList<String> Items;
    ArrayList<String> categoryList;
    RecyclerView recyclerView;
    ShopItemAdapter shopItemAdapter;
    ShopMoveAdapter shopMoveAdapter;
    ShopCopyAdapter shopCopyAdapter;
    Button fab;
    ItemTouchHelper itemTouchHelper;
    ArrayList<String> Items_Prices_List;
    ArrayList<String> Final_Items_Prices_List;
    ArrayList<String> Items_Quantities_List;
    ArrayList<String> unitCheck;
    ShopDatabase db;
    TextView Total_Summation_Textbox;
    TextView Items_list_size_textbox;
    TextView currency_textbox;
    TextView Total_text;
    ArrayList<String> selectList;
    ImageView eyeView;
    int currentHour;
    int currentMinutes;
    String single_selected_item;
    String categoryTitle;
    Calendar calendar;
    Date date;
    String month;
    String year;
    String day;
    String time;
    String fullTimeWithSeconds;
    String formattedDate;
    //to check the items list for something
    ArrayList<String> Items_Check;

    HashSet<String> suggest_list;
    HashSet<String> suggest_unit_list;

    //favourites
    ArrayList<String> Fav_Item_category;
    ArrayList<String> Item_Favourites_List;
    ArrayList<String> filter;
    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;

    ProgressBar main_progress_bar;

    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    int CAMERA_REQUEST = 2468;
    int GALLERY_REQUEST = 1;

    LinearLayout emptyNotesLayout;
    TextView emptyText1;

    Dialog noNetworkDialog, category_dialog, upgrade_required_dialog,
            move_dialog, sort_by_dialog, copy_dialog, rename_dialog,
            timePickerDialog, info_dialog, delete_dialog, quantity_dialog,
            price_dialog, rename_dialog_2, add_image_dialog, edit_dialog,
            bottom_dialog, menu_unstar_dialog, menu_copy_dialog, menu_move_dialog,
            menu_delete_dialog, show_share_dialog, voice_input_dialog;


    AdView adView;

    private InterstitialAd mInterstitialAd;

    int adsCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = (UserSettings) getApplication();
        SharedPreferences sharedPreferences_theme = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String theme = sharedPreferences_theme.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);
        String dim = sharedPreferences_theme.getString(UserSettings.IS_DIM_THEME_ENABLED, UserSettings.NO_DIM_THEME_NOT_ENABLED);
        settings.setIsDimThemeEnabled(dim);

        if (settings.getCustomTheme().equals(UserSettings.DEFAULT_THEME)) {
            int currentNightMode = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {


                if (settings.getIsDimThemeEnabled().equals(UserSettings.YES_DIM_THEME_ENABLED)) {
                    setTheme(R.style.Dynamic_Dim);
                }
            }

        } else if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {

            if (settings.getIsDimThemeEnabled().equals(UserSettings.YES_DIM_THEME_ENABLED)) {
                setTheme(R.style.Dynamic_Dim);
            }
        }

        setContentView(R.layout.activity_item);


        //get the sharedPreferences


        //get the category name
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        category = extras.getString("ITEM").trim();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);

        //configure the toolbar
        itemToolbar = findViewById(R.id.cate_title);
        itemToolbar.setTitle(category);
        setSupportActionBar(itemToolbar);
        itemToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //configure the category's item list
        Items = new ArrayList<>();
        categoryList = new ArrayList<>();

        currency_textbox = findViewById(R.id.currency_box);

        main_progress_bar = findViewById(R.id.progress_bar);

        //configure the recyclerview
        recyclerView = findViewById(R.id.items_recyclerView);
        shopItemAdapter = new ShopItemAdapter(this, settings, Items, this, category, ItemActivity.this);
        shopMoveAdapter = new ShopMoveAdapter(this, settings, categoryList, category, ItemActivity.this);
        shopCopyAdapter = new ShopCopyAdapter(this, settings, categoryList, category, ItemActivity.this);
        recyclerView.setAdapter(shopItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        favSumLayout = findViewById(R.id.favSumLayout);
        emptyNotesLayout = findViewById(R.id.emptyNotesLayout);
        emptyText1 = findViewById(R.id.emptyTEXT1);

        //configure the left and right swipe

        suggest_list = new HashSet<>();
        suggest_unit_list = new HashSet<>();


        //configure the floating_action_button
        fab = findViewById(R.id.addItem_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialogue();
            }
        });

        Total_Summation_Textbox = findViewById(R.id.totalPrice);
        Items_list_size_textbox = findViewById(R.id.text1);
        Total_text = findViewById(R.id.text2);

        //configure other necessary items
        db = new ShopDatabase(this);


        eyeView = findViewById(R.id.eyeView);
        eyeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (settings.getIsItemEyeDisabled().equals(UserSettings.YES_ITEM_EYE_DISABLED)) {
                    settings.setIsItemEyeDisabled(UserSettings.NO_ITEM_EYE_NOT_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_ITEM_EYE_DISABLED, settings.getIsItemEyeDisabled());
                    editor.apply();
                    getsum();
                    eyeView.setImageResource(R.drawable.final_regular_eye);

                } else {
                    settings.setIsItemEyeDisabled(UserSettings.YES_ITEM_EYE_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_ITEM_EYE_DISABLED, settings.getIsItemEyeDisabled());
                    editor.apply();
                    getsum();

                    eyeView.setImageResource(R.drawable.final_regular_eye_slash_icon);

                }


            }
        });


        Fav_Item_category = new ArrayList<>();
        Item_Favourites_List = new ArrayList<>();

        Items_Check = new ArrayList<>();
        filter = new ArrayList<>();
        Items_Prices_List = new ArrayList<>();
        Final_Items_Prices_List = new ArrayList<>();
        Items_Quantities_List = new ArrayList<>();
        unitCheck = new ArrayList<>();

        loadSharedPreferences();

        if (settings.getIsSwipeDisabled().equals(UserSettings.NOT_DISABLED)) {
            itemTouchHelper = new ItemTouchHelper(new RecyclerViewItemTouchHelper(shopItemAdapter, settings, category, ItemActivity.this));
            itemTouchHelper.attachToRecyclerView(recyclerView);
        } else {
            itemTouchHelper = null;
        }

        displayData();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFirstStart) {
                    Dialog dialog = new Dialog(ItemActivity.this);
                    dialog.setContentView(R.layout.success_dialog_popup);
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);
                    dialog.show();


                    TextView heading = dialog.findViewById(R.id.dialogText);
                    TextView subHeading = dialog.findViewById(R.id.dialogText2);
                    Button okay = dialog.findViewById(R.id.okBtn);


                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                        heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        subHeading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        okay.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                        heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        subHeading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        okay.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                        heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        subHeading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        okay.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                    }




                    okay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            settings.setFirstStart(false);
                            SharedPreferences.Editor editor = ItemActivity.this.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                            editor.putBoolean("isFirstStart", settings.getFirstStart());
                            editor.apply();
                        }
                    });

                }
            }
        }, 1000);


        adsCounter = sharedPreferences_theme.getInt(UserSettings.ADS_COUNTER_KEY, 0);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });

        adView = findViewById(R.id.adView);


        if(!settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)){
            AdRequest adRequest = new AdRequest.Builder().build();

            SharedPreferences preferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
            long installDateMillis = preferences.getLong(UserSettings.KEY_INSTALL_DATE, 0);

            if(installDateMillis == 0){
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(UserSettings.KEY_INSTALL_DATE, System.currentTimeMillis());
                editor.apply();
            }else{
                long currentTimeMillis = System.currentTimeMillis();
                long elapsedTimeMillis = currentTimeMillis - installDateMillis;
                if(elapsedTimeMillis >= UserSettings.SEVEN_DAYS_IN_MILLIS){
                    adView.setVisibility(View.VISIBLE);
                    adView.loadAd(adRequest);
                }
            }

            InterstitialAd.load(ItemActivity.this, "ca-app-pub-4208883735301832/9606114397", adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    mInterstitialAd = null;
                }

                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    mInterstitialAd = interstitialAd;
                }
            });

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean checkSelected = shopItemAdapter.isSelected();
        MenuInflater inflater = getMenuInflater();
        if (checkSelected) {
            selectList = new ArrayList<String>();
            selectList = shopItemAdapter.getSelectList();
            itemToolbar.setTitle(selectList.size() + "/" + Items.size());
            inflater.inflate(R.menu.item_toolbar_hold_menu, menu);


            if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem menuItem = menu.getItem(i);
                    SpannableString spannableString = new SpannableString(menuItem.getTitle());
                    spannableString.setSpan(new RelativeSizeSpan(0.9f), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    menuItem.setTitle(spannableString);
                }

            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem menuItem = menu.getItem(i);
                    SpannableString spannableString = new SpannableString(menuItem.getTitle());
                    spannableString.setSpan(new RelativeSizeSpan(1.1f), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    menuItem.setTitle(spannableString);
                }
            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem menuItem = menu.getItem(i);
                    SpannableString spannableString = new SpannableString(menuItem.getTitle());
                    spannableString.setSpan(new RelativeSizeSpan(1.3f), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    menuItem.setTitle(spannableString);
                }
            }


            ArrayList<String> finalSelectList = selectList;
            menu.findItem(R.id.delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {

                    menu_delete_dialog = new Dialog(ItemActivity.this);
                    menu_delete_dialog.setContentView(R.layout.custom_delete_dialog);
                    menu_delete_dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
                    menu_delete_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                    StringBuilder items_selected = new StringBuilder();
                    for (String items : finalSelectList) {
                        items_selected.append("\u2022 ").append(items).append("\n");
                    }

                    TextView delete_heading = menu_delete_dialog.findViewById(R.id.delete_heading);
                    TextView delete_message = menu_delete_dialog.findViewById(R.id.delete_message);
                    Button deleteButton = menu_delete_dialog.findViewById(R.id.delete_button);
                    Button cancelButton = menu_delete_dialog.findViewById(R.id.cancel_button);

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                        delete_heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        delete_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                        delete_heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        delete_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                        delete_heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        delete_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                    }


                    if (finalSelectList.size() > 1) {
                        delete_heading.setText(getString(R.string.ItemActivity__deleteTheseItems));
                    } else {
                        delete_heading.setText(getString(R.string.ItemActivity__deleteThisItem));
                    }
                    delete_message.setText(items_selected.toString());

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (String s : finalSelectList) {

                                String photo_url = null;
                                Cursor res = db.getPhotourl(category, s);
                                while (res.moveToNext()) {
                                    photo_url = res.getString(12);
                                }
                                res.close();

                                if (!photo_url.trim().isEmpty()) {
                                    db.updatePhoto(category, s, " ");

                                    ArrayList<String> total_url = new ArrayList<>();
                                    res = db.getCategory(ItemActivity.this);
                                    while (res.moveToNext()) {
                                        total_url.add(res.getString(12));
                                    }
                                    res.close();

                                    if (!total_url.contains(photo_url)) {
                                        FirebaseStorage storage = FirebaseStorage.getInstance();
                                        StorageReference storageReference = storage.getReference().child(photo_url);
                                        storageReference.delete();
                                    }
                                }

                                db.deleteItem(category, s);
                                Items.remove(s);
                            }
                            if (Items.size() == 0) {
                                Intent intent = new Intent(ItemActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }else{
                                shopItemAdapter.disableSelection();
                                shopItemAdapter.notifyDataSetChanged();
                                menu_delete_dialog.dismiss();
                            }


                        }
                    });

                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            menu_delete_dialog.dismiss();
                        }
                    });

                    menu_delete_dialog.show();
                    return true;
                }
            });

            menu.findItem(R.id.move).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {

                    menu_move_dialog = new Dialog(shopItemAdapter.getContext());
                    menu_move_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    menu_move_dialog.setContentView(R.layout.movebottomlayout);

                    TextView emptyTEXT1 = menu_move_dialog.findViewById(R.id.emptyTEXT1);
                    LinearLayout emptyNotesLayout = menu_move_dialog.findViewById(R.id.emptyNotesLayout);
                    TextView textView = menu_move_dialog.findViewById(R.id.item_move_title);
                    textView.setText(getString(R.string.ItemActivity__moveTo));
                    SearchView searchView = menu_move_dialog.findViewById(R.id.search_bar);
                    EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
                    Button button = menu_move_dialog.findViewById(R.id.create_new_list);

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                    }


                    displayCategoryData();
                    shopMoveAdapter.setFilterList(categoryList);
                    RecyclerView moveRecyclerView = menu_move_dialog.findViewById(R.id.moveRecyclerView);
                    moveRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                    moveRecyclerView.setAdapter(shopMoveAdapter);

                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {

                            ArrayList<String> filterList = new ArrayList<>();

                            for (String item : categoryList) {
                                if (item.toLowerCase().contains(newText.toLowerCase())) {
                                    filterList.add(item);
                                }

                            }
                            shopCopyAdapter.setFilterList(filterList);
                            shopMoveAdapter.setFilterList(filterList);
                            emptyNotesLayout.setVisibility(View.GONE);
                            if (filterList.isEmpty()) {
                                emptyNotesLayout.setVisibility(View.VISIBLE);
                            }
                            return true;
                        }
                    });

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showCategoryDialog("moved");
                        }
                    });

                    menu_move_dialog.show();
                    menu_move_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    menu_move_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    menu_move_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                    menu_move_dialog.getWindow().setGravity(Gravity.BOTTOM);
                    return true;
                }
            });

            menu.findItem(R.id.copy).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    menu_copy_dialog = new Dialog(shopItemAdapter.getContext());
                    menu_copy_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    menu_copy_dialog.setContentView(R.layout.movebottomlayout);


                    LinearLayout emptyNotesLayout = menu_copy_dialog.findViewById(R.id.emptyNotesLayout);
                    TextView emptyTEXT1 = menu_copy_dialog.findViewById(R.id.emptyTEXT1);
                    TextView textView = menu_copy_dialog.findViewById(R.id.item_move_title);
                    textView.setText(getString(R.string.ItemActivity__copyTo));
                    SearchView searchView = menu_copy_dialog.findViewById(R.id.search_bar);
                    EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
                    Button button = menu_copy_dialog.findViewById(R.id.create_new_list);


                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                    }


                    displayCategoryData();
                    shopCopyAdapter.setFilterList(categoryList);
                    RecyclerView copyRecyclerView = menu_copy_dialog.findViewById(R.id.moveRecyclerView);
                    copyRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                    copyRecyclerView.setAdapter(shopCopyAdapter);
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            ArrayList<String> filterList = new ArrayList<>();

                            for (String item : categoryList) {
                                if (item.toLowerCase().contains(newText.toLowerCase())) {
                                    filterList.add(item);
                                }

                            }
                            shopCopyAdapter.setFilterList(filterList);
                            shopMoveAdapter.setFilterList(filterList);
                            emptyNotesLayout.setVisibility(View.GONE);
                            if (filterList.isEmpty()) {
                                emptyNotesLayout.setVisibility(View.VISIBLE);
                            }
                            return true;
                        }
                    });

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showCategoryDialog("copy");
                        }
                    });

                    menu_copy_dialog.show();
                    menu_copy_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    menu_copy_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    menu_copy_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                    menu_copy_dialog.getWindow().setGravity(Gravity.BOTTOM);
                    return true;
                }
            });
            menu.findItem(R.id.mark).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    shopItemAdapter.setSelectAll();

                    return true;
                }
            });
            menu.findItem(R.id.star).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    for (int i = 0; i < finalSelectList.size(); i++) {
                        db.updateFavourites(category, finalSelectList.get(i), 1);
                        shopItemAdapter.notifyDataSetChanged();
                    }
                    shopItemAdapter.disableSelection();
                    return true;
                }
            });
            menu.findItem(R.id.unStar).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {

                    menu_unstar_dialog = new Dialog(ItemActivity.this);
                    menu_unstar_dialog.setContentView(R.layout.custom_delete_dialog);
                    menu_unstar_dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
                    menu_unstar_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                    StringBuilder items_selected = new StringBuilder();
                    for (String items : finalSelectList) {
                        items_selected.append("\u2022 ").append(items).append("\n");
                    }

                    TextView delete_heading = menu_unstar_dialog.findViewById(R.id.delete_heading);
                    TextView delete_message = menu_unstar_dialog.findViewById(R.id.delete_message);
                    Button deleteButton = menu_unstar_dialog.findViewById(R.id.delete_button);
                    Button cancelButton = menu_unstar_dialog.findViewById(R.id.cancel_button);


                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                        delete_heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        delete_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                        delete_heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        delete_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
                    }

                    if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                        delete_heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        delete_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
                    }


                    delete_heading.setText(getString(R.string.ItemActivity__unstar));
                    delete_message.setText(items_selected.toString());
                    deleteButton.setText(getString(R.string.ItemActivity__unstar));
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            menu_unstar_dialog.dismiss();
                        }
                    });

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (int i = 0; i < finalSelectList.size(); i++) {
                                db.updateFavourites(category, finalSelectList.get(i), 0);
                                shopItemAdapter.notifyDataSetChanged();

                            }
                            shopItemAdapter.disableSelection();
                            menu_unstar_dialog.dismiss();
                        }
                    });
                    menu_unstar_dialog.show();
                    return true;
                }
            });
        } else {
            itemToolbar.setTitle(category);
            inflater.inflate(R.menu.item_toolbar_menu, menu);


            if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem menuItem = menu.getItem(i);
                    SpannableString spannableString = new SpannableString(menuItem.getTitle());
                    spannableString.setSpan(new RelativeSizeSpan(0.9f), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    menuItem.setTitle(spannableString);
                }

            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem menuItem = menu.getItem(i);
                    SpannableString spannableString = new SpannableString(menuItem.getTitle());
                    spannableString.setSpan(new RelativeSizeSpan(1.1f), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    menuItem.setTitle(spannableString);
                }
            }

            if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem menuItem = menu.getItem(i);
                    SpannableString spannableString = new SpannableString(menuItem.getTitle());
                    spannableString.setSpan(new RelativeSizeSpan(1.3f), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    menuItem.setTitle(spannableString);
                }
            }

            boolean check_checkList = check_if_one_checked();
            boolean checked_if_all_checkList = check_if_all_checked();
            MenuItem uncheckIcon = itemToolbar.getMenu().findItem(R.id.unCheck);
            MenuItem check = itemToolbar.getMenu().findItem(R.id.check);
            if (check_checkList) {
                uncheckIcon.setVisible(true);
                check.setVisible(!checked_if_all_checkList);

            } else {
                uncheckIcon.setVisible(false);
                check.setVisible(true);
            }


            MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                    return true;
                }
            };

            menu.findItem(R.id.search).setOnActionExpandListener(onActionExpandListener);
            searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            searchView.setQueryHint(getString(R.string.ItemActivity__searchHint));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterList(newText);
                    return true;
                }
            });

            menu.findItem(R.id.check).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    for (int i = 0; i < Items.size(); i++) {
                        db.updateStatus(category, Items.get(i), 1);
                    }
                    shopItemAdapter.notifyDataSetChanged();
                    return true;
                }
            });
            menu.findItem(R.id.unCheck).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    for (int i = 0; i < Items.size(); i++) {
                        db.updateStatus(category, Items.get(i), 0);
                    }
                    shopItemAdapter.notifyDataSetChanged();
                    return true;
                }
            });
            menu.findItem(R.id.reminder).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    showAlarmDialog(category);
                    return true;
                }
            });
            menu.findItem(R.id.rename).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    showRenameDialog();
                    return true;
                }
            });
            menu.findItem(R.id.share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    showShareOptionDialog(category);
                    return true;
                }
            });
            menu.findItem(R.id.sort).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    showSortByDialog();
                    return true;
                }
            });

            menu.findItem(R.id.voice).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    showGoogleVoiceDialog();
                    return true;
                }
            });

        }
        return true;

    }

    //show data
    @SuppressLint("SetTextI18n")
    public void displayData() {


        Items.clear();
        Items_Prices_List.clear();
        Items_Quantities_List.clear();

        Cursor res = db.getItems(category, getApplicationContext());

        while (res.moveToNext()) {
            Items.add(res.getString(2));
            Items_Prices_List.add(res.getString(4));
            Items_Quantities_List.add(res.getString(9));
        }
        res.close();


        double temp_sum = 0;


        if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
            for (int i = 0; i < Items_Prices_List.size(); i++) {
                Double temp_price = Double.parseDouble(Items_Prices_List.get(i));
                Double temp_quantity = Double.parseDouble(Items_Quantities_List.get(i));
                temp_sum += temp_price * temp_quantity;
            }
        } else {
            for (int i = 0; i < Items_Prices_List.size(); i++) {
                Double temp_price = Double.parseDouble(Items_Prices_List.get(i));
                temp_sum += temp_price;
            }
        }


        int checked_count = 0;
        ArrayList<String> seeChecked = new ArrayList<>();
        for (int i = 0; i < Items.size(); i++) {
            Cursor res_checked = db.getStatus(category, Items.get(i));
            while (res_checked.moveToNext()) {
                seeChecked.add(res_checked.getString(3));
            }
            res_checked.close();
        }
        for (String checked : seeChecked) {
            if (checked.equals(String.valueOf(1))) {
                checked_count++;
            }
        }


        if (settings.getIsItemEyeDisabled().equals(UserSettings.NO_ITEM_EYE_NOT_DISABLED)) {
            Total_Summation_Textbox.setText(formatNumber(temp_sum));
        } else {
            Total_Summation_Textbox.setText("*****");
        }


        Items_list_size_textbox.setText(getString(R.string.ItemActivity__itemsChecked) + " " + "(" + checked_count + "/" + Items.size() + ")");

        Cursor resItemName = db.getSuggestName();
        while (resItemName.moveToNext()) {
            suggest_list.add(resItemName.getString(1).trim());
        }


        resItemName = db.getSuggestUnit();
        while (resItemName.moveToNext()) {
            String temp_unit = resItemName.getString(1);
            if (!temp_unit.trim().isEmpty()) {
                suggest_unit_list.add(temp_unit);
            }
        }
        resItemName.close();

    }

    @SuppressLint("SetTextI18n")
    public void getsum() {

        Items_Prices_List.clear();
        Items_Quantities_List.clear();

        Cursor res = db.getItems(category, ItemActivity.this);
        while (res.moveToNext()) {
            Items_Prices_List.add(res.getString(4));
            Items_Quantities_List.add(res.getString(9));
        }
        res.close();


        double temp_sum = 0;

        if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
            for (int i = 0; i < Items_Prices_List.size(); i++) {
                double temp_price = Double.parseDouble(Items_Prices_List.get(i));
                double temp_quantity = Double.parseDouble(Items_Quantities_List.get(i));
                temp_sum += temp_price * temp_quantity;
            }
        } else {
            for (int i = 0; i < Items_Prices_List.size(); i++) {
                double temp_price = Double.parseDouble(Items_Prices_List.get(i));
                temp_sum += temp_price;
            }
        }


        int checked_count = 0;
        ArrayList<String> seeChecked = new ArrayList<>();
        for (int i = 0; i < Items.size(); i++) {
            Cursor res_checked = db.getStatus(category, Items.get(i));
            while (res_checked.moveToNext()) {
                seeChecked.add(res_checked.getString(3));
            }
            res_checked.close();
        }
        for (String checked : seeChecked) {
            if (checked.equals(String.valueOf(1))) {
                checked_count++;
            }
        }


        if (settings.getIsItemEyeDisabled().equals(UserSettings.NO_ITEM_EYE_NOT_DISABLED)) {
            Total_Summation_Textbox.setText(formatNumber(temp_sum));
        } else {
            Total_Summation_Textbox.setText("*****");
        }

        Items_list_size_textbox.setText(getString(R.string.ItemActivity__itemsChecked) + " " + "(" + checked_count + "/" + Items.size() + ")");

    }

    public void getDateNdTime() {

        calendar = Calendar.getInstance();
        date = calendar.getTime();

        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMMM", Locale.getDefault());
        String currentMonth = sdfMonth.format(date);

        SimpleDateFormat sdfMonthNumber = new SimpleDateFormat("MM", Locale.getDefault());
        String currentMonthNumber = sdfMonthNumber.format(date);

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = sdfYear.format(date);

        SimpleDateFormat sdfDay = new SimpleDateFormat("EEEE", Locale.getDefault());
        String currentDay = sdfDay.format(date);

        SimpleDateFormat sdfDayNumber = new SimpleDateFormat("dd", Locale.getDefault());
        String currentDayNumber = sdfDayNumber.format(date);

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        SimpleDateFormat seconds = new SimpleDateFormat("h:mm:ss a", Locale.getDefault());

        day = currentDay + " " + currentDayNumber;
        month = currentMonth;
        year = currentYear;
        time = timeFormat.format(date);
        fullTimeWithSeconds = String.valueOf(System.currentTimeMillis());
        formattedDate = currentDayNumber + "-" + currentMonthNumber + "-" + currentYear;
    }

    //dialog to add a new item
    public void showBottomDialogue() {

        bottom_dialog = new Dialog(this);
        bottom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottom_dialog.setContentView(R.layout.shopbottomlayout);

        LinearLayout addItemIllustrationLayout = bottom_dialog.findViewById(R.id.addItem_Illustration_Layout);

        TextView add_Item_Text_Header_1 = bottom_dialog.findViewById(R.id.addItemHeaderText);
        TextView add_Item_Text_Header_2 = bottom_dialog.findViewById(R.id.addItemHeaderText2);

        AutoCompleteTextView item_name_box = bottom_dialog.findViewById(R.id.desc_name);
        AutoCompleteTextView unitText = bottom_dialog.findViewById(R.id.unit_textView);


        EditText item_price_box = bottom_dialog.findViewById(R.id.desc_price);
        EditText item_quantity_box = bottom_dialog.findViewById(R.id.desc_quantity);
        Button cancelButton = bottom_dialog.findViewById(R.id.CancelButton);
        Button saveButton = bottom_dialog.findViewById(R.id.BtnSave);

        TextInputLayout textInputLayout = bottom_dialog.findViewById(R.id.desc_price_text_input_layout);
        textInputLayout.setPrefixText(settings.getCurrency());


        if (settings.getIsSuggestionDisabled().equals(UserSettings.YES_SUGGESTION_DISABLED)) {

        } else {
            ArrayList<String> temp_suggestion_list = new ArrayList<>(suggest_list);
            ArrayAdapter<String> suggest_adapter = new ArrayAdapter<>(ItemActivity.this, R.layout.unit_drop_down_layout, temp_suggestion_list);
            item_name_box.setAdapter(suggest_adapter);
            ArrayList<String> temp_suggestion_unit_list = new ArrayList<>(suggest_unit_list);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.unit_drop_down_layout, temp_suggestion_unit_list);
            unitText.setAdapter(adapter);
        }


        LinearLayout more_layout = bottom_dialog.findViewById(R.id.more_layout);
        LinearLayout price_quantity_layout = bottom_dialog.findViewById(R.id.price_quantity_layout);
        TextView more_text = bottom_dialog.findViewById(R.id.more_text);
        ImageView more_image = bottom_dialog.findViewById(R.id.more_icon);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            add_Item_Text_Header_1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            add_Item_Text_Header_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            item_name_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            item_price_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            unitText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            item_quantity_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            more_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            add_Item_Text_Header_1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            add_Item_Text_Header_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            item_name_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            item_price_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            unitText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            item_quantity_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            more_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            add_Item_Text_Header_1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            add_Item_Text_Header_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            item_name_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            item_price_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            unitText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            item_quantity_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            more_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        addItemIllustrationLayout.setVisibility(View.VISIBLE);
        add_Item_Text_Header_1.setText(getString(R.string.ItemActivity__addItemToList));
        add_Item_Text_Header_2.setVisibility(View.GONE);


        Items_Check.clear();
        Cursor res = db.getItems(category, ItemActivity.this);
        while (res.moveToNext()) {
            Items_Check.add(res.getString(2).toLowerCase());
        }
        res.close();

        more_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility_status = price_quantity_layout.getVisibility();
                if (visibility_status == View.GONE) {
                    price_quantity_layout.setVisibility(View.VISIBLE);
                    more_image.setImageResource(R.drawable.final_regular_arrow_drop_down_icon);
                } else if (visibility_status == View.VISIBLE) {
                    price_quantity_layout.setVisibility(View.GONE);
                    more_image.setImageResource(R.drawable.final_regular_arrow_drop_up_icon);
                }
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottom_dialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateNdTime();

                String unit;
                if (unitText.getText().toString().isEmpty()) {
                    unit = " ";
                } else {
                    unit = unitText.getText().toString();
                }

                if (!item_name_box.getText().toString().trim().isEmpty()) {
                    String description = item_name_box.getText().toString().trim();
                    if (item_price_box.getText().toString().trim().isEmpty()) {
                        if (!Items_Check.contains(item_name_box.getText().toString().trim().toLowerCase())) {
                            if (item_quantity_box.getText().toString().isEmpty()) {
                                double temp_quantity = 1;
                                double temp_price = 0;
                                insertItem(category, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                bottom_dialog.dismiss();
                            } else {
                                double temp_quantity = Double.parseDouble(item_quantity_box.getText().toString().trim());
                                double temp_price = 0;
                                insertItem(category, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                bottom_dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__alreadyExist), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!Items_Check.contains(item_name_box.getText().toString().trim().toLowerCase())) {
                            if (item_quantity_box.getText().toString().isEmpty()) {
                                double temp_quantity = 1;
                                double temp_price = Double.parseDouble(item_price_box.getText().toString().trim());
                                insertItem(category, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                bottom_dialog.dismiss();
                            } else {
                                double temp_quantity = Double.parseDouble(item_quantity_box.getText().toString().trim());
                                double temp_price = Double.parseDouble(item_price_box.getText().toString().trim());
                                insertItem(category, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                bottom_dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__alreadyExist), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__emptyName), Toast.LENGTH_SHORT).show();
                }
                shopItemAdapter.notifyItemInserted(Items.size() + 1);
                getsum();
            }
        });

        bottom_dialog.show();
        bottom_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bottom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bottom_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        bottom_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    //for inserting new item
    public void insertItem(String category, String description, int status, double price, String month, String year, String day, String time, double quantity, String unit) {

        String finalPrice = formatNumberV2(price);
        String finalQuantity = formatNumberV2(quantity);

        db.insertItem(category, description, status, finalPrice, month, year, day, time, finalQuantity, unit);
        Items.add(description);
        shopItemAdapter.notifyDataSetChanged();
        getsum();

        if (!suggest_list.contains(description.trim())) {
            db.insertSuggest(description);
        }

        if (!suggest_unit_list.contains(unit.trim())) {
            db.insertSuggestUnit(unit);
        }


    }

    public void showEditDialog(String prevTask, int position) {

        edit_dialog = new Dialog(ItemActivity.this);
        edit_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        edit_dialog.setContentView(R.layout.edit_item_layout);


        LinearLayout addImageLayout = edit_dialog.findViewById(R.id.addImage);
        LinearLayout renameLayout = edit_dialog.findViewById(R.id.editName);
        LinearLayout copyLayout = edit_dialog.findViewById(R.id.copy);
        LinearLayout moveLayout = edit_dialog.findViewById(R.id.move);
        LinearLayout changePriceLayout = edit_dialog.findViewById(R.id.editPrice);
        LinearLayout changeQuantityLayout = edit_dialog.findViewById(R.id.editQuantity);
        LinearLayout changeFavouritesLayout = edit_dialog.findViewById(R.id.editFavourites);
        LinearLayout deleteLayout = edit_dialog.findViewById(R.id.remove);
        LinearLayout infoLayout = edit_dialog.findViewById(R.id.information);

        TextView title = edit_dialog.findViewById(R.id.item_edit_title);
        TextView addImageText = edit_dialog.findViewById(R.id.edit_addImage_text);
        TextView renameText = edit_dialog.findViewById(R.id.edit_rename_text);
        TextView copyText = edit_dialog.findViewById(R.id.edit_copy_text);
        TextView moveText = edit_dialog.findViewById(R.id.edit_move_text);
        TextView priceText = edit_dialog.findViewById(R.id.edit_price_text);
        TextView quantityText = edit_dialog.findViewById(R.id.edit_quantity_text);
        TextView optionFavourites = edit_dialog.findViewById(R.id.edit_fav_box);
        TextView deleteText = edit_dialog.findViewById(R.id.edit_delete_text);
        TextView detailsText = edit_dialog.findViewById(R.id.edit_details_text);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            addImageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            renameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            copyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            moveText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            priceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            quantityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            optionFavourites.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            deleteText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            detailsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            renameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            addImageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            copyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            moveText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            priceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            quantityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            optionFavourites.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            deleteText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            detailsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            renameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            addImageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            copyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            moveText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            priceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            quantityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            optionFavourites.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            deleteText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            detailsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        title.setText(prevTask);
        //quick ChangefavLayout configuration
        int temp_fav = 0;
        String url = "";
        Cursor res = db.getFavourites(category, prevTask);
        while (res.moveToNext()) {
            temp_fav = res.getInt(10);
            url = res.getString(12);
        }
        if (temp_fav == 1) {
            optionFavourites.setText(R.string.ItemActivity__unstar);
        } else {
            optionFavourites.setText(R.string.ItemActivity__starItem);
        }

        if (url.trim().isEmpty()) {
            addImageText.setText(getString(R.string.ItemActivity__addPhoto));
        } else {
            addImageText.setText(getString(R.string.ItemActivity__updatePhoto));
        }


        //end of configuration

        addImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp_item = prevTask;
                edit_dialog.dismiss();
                showAddImageDialog();
            }
        });

        renameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialog(prevTask, position);
                edit_dialog.dismiss();
            }
        });

        copyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_dialog.dismiss();
                showCopyDialog(prevTask);
            }
        });

        moveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_dialog.dismiss();
                showMoveDialog(prevTask);
            }
        });

        changePriceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPriceDialog(prevTask, position);
                edit_dialog.dismiss();
            }
        });

        changeQuantityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuantityDialog(prevTask, position);
                edit_dialog.dismiss();
            }
        });

        changeFavouritesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int temp_fav = 0;
                Cursor res = db.getFavourites(category, prevTask);
                while (res.moveToNext()) {
                    temp_fav = res.getInt(10);
                }
                res.close();

                if (temp_fav == 1) {
                    db.updateFavourites(category, prevTask, 0);
                    Toast.makeText(ItemActivity.this, R.string.ItemActivity__unstarred, Toast.LENGTH_SHORT).show();
                } else {

                    db.updateFavourites(category, prevTask, 1);
                    Toast.makeText(ItemActivity.this, R.string.ItemActivity__itemStarred, Toast.LENGTH_SHORT).show();

                }
                edit_dialog.dismiss();
                shopItemAdapter.notifyItemChanged(position);
            }
        });

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(position);
                edit_dialog.dismiss();
            }
        });

        infoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog(position);
                edit_dialog.dismiss();
            }
        });

        edit_dialog.show();
        edit_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                searchView.setQuery("", false);
                searchView.onActionViewCollapsed();
                shopItemAdapter.notifyItemChanged(position);
            }
        });
        edit_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        edit_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        edit_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        edit_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void showAddImageDialog() {

        add_image_dialog = new Dialog(ItemActivity.this);
        add_image_dialog.setContentView(R.layout.custom_image_upload_dialog);
        add_image_dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        add_image_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        LinearLayout takePictureLayout = add_image_dialog.findViewById(R.id.takePictureLayout);
        LinearLayout uploadPictureLayout = add_image_dialog.findViewById(R.id.uploadPictureLayout);

        takePictureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                add_image_dialog.dismiss();
                if (firebaseUser != null) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    } else {
                    StyleableToast.makeText(ItemActivity.this, getString(R.string.ItemActivity__loginPrompt), R.style.custom_toast_2).show();
                    }
                }
        });

        uploadPictureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    add_image_dialog.dismiss();
                    if (firebaseUser != null) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, GALLERY_REQUEST);
                    } else {
                        StyleableToast.makeText(ItemActivity.this, getString(R.string.ItemActivity__loginPrompt), R.style.custom_toast_2).show();
                    }
            }
        });


        TextView title = add_image_dialog.findViewById(R.id.image_title);
        TextView title_2 = add_image_dialog.findViewById(R.id.image_title_2);
        String temp_text = "(" + temp_item + ")";
        title_2.setText(temp_text);
        if (!is_photo_url_empty) {
            title.setText(getString(R.string.ItemActivity__updatePhoto));
        } else {
            title.setText(getString(R.string.ItemActivity__addPhoto));
        }
        TextView takePictureText = add_image_dialog.findViewById(R.id.takePictureText);
        TextView uploadPictureText = add_image_dialog.findViewById(R.id.uploadPictureText);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            title_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            takePictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            uploadPictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            title_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            takePictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            uploadPictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            title_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            takePictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            uploadPictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        add_image_dialog.show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CAMERA_REQUEST) && (resultCode == Activity.RESULT_OK)) {
            recyclerView.setClickable(false);
            main_progress_bar.setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");


                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    String email = firebaseUser.getEmail().trim();
                    getDateNdTime();
                    String path = email + day + month + year + fullTimeWithSeconds + ".jpg";


                    boolean isInserted = false;

                    File directory = new File(ItemActivity.this.getFilesDir(), "Buymate_Images");
                    if (!directory.exists()) {
                        directory.mkdir();
                    }

                    File imageFile = new File(directory, path);
                    try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                        isInserted = imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (isInserted) {
                        String old_url = null;
                        Cursor res = db.getPhotourl(category, temp_item);
                        while (res.moveToNext()) {
                            old_url = res.getString(12);
                        }

                        db.updatePhoto(category, temp_item, path);



                        ArrayList<String> total_url = new ArrayList<>();

                        res = db.getCategory(ItemActivity.this);
                        while (res.moveToNext()) {
                            total_url.add(res.getString(12));
                        }
                        res.close();

                        if (!total_url.contains(old_url)) {

                            File imageFileToDelete = new File(directory, old_url);
                            if (imageFileToDelete.exists()) {
                                imageFileToDelete.delete();
                            }
                        }
                        recyclerView.setClickable(true);
                        main_progress_bar.setVisibility(View.INVISIBLE);
                        shopItemAdapter.notifyDataSetChanged();

                        if(!settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)){
                            if(mInterstitialAd != null){
                                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdClicked() {
                                        super.onAdClicked();
                                    }

                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        mInterstitialAd = null;
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                        mInterstitialAd = null;
                                    }

                                    @Override
                                    public void onAdImpression() {
                                        super.onAdImpression();
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        adsCounter = 0;
                                        SharedPreferences  sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);
                                        sharedPreferences.edit().putInt(UserSettings.ADS_COUNTER_KEY, adsCounter).apply();
                                        super.onAdShowedFullScreenContent();
                                    }
                                });
                                if(adsCounter >= 20){
                                    mInterstitialAd.show(ItemActivity.this);
                                }else{
                                    adsCounter = adsCounter + 1;
                                    SharedPreferences  sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);
                                    sharedPreferences.edit().putInt(UserSettings.ADS_COUNTER_KEY, adsCounter).apply();
                                }

                            }
                        }



                    } else {
                        Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__insertImageFailed), Toast.LENGTH_SHORT).show();
                    }
                }
            }, 1000);
        }

        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            try {

                recyclerView.setClickable(false);
                main_progress_bar.setVisibility(View.VISIBLE);

                ContentResolver contentResolver = getContentResolver();
                InputStream inputStream = contentResolver.openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        String email = firebaseUser.getEmail().trim();
                        getDateNdTime();
                        String path = email + day + month + year + fullTimeWithSeconds + ".jpg";


                        boolean isInserted = false;

                        File directory = new File(ItemActivity.this.getFilesDir(), "Buymate_Images");
                        if (!directory.exists()) {
                            directory.mkdir();
                        }

                        File imageFile = new File(directory, path);
                        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                            isInserted = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (isInserted) {
                            String old_url = null;
                            Cursor res = db.getPhotourl(category, temp_item);
                            while (res.moveToNext()) {
                                old_url = res.getString(12);
                            }

                            db.updatePhoto(category, temp_item, path);

                            ArrayList<String> total_url = new ArrayList<>();

                            res = db.getCategory(ItemActivity.this);
                            while (res.moveToNext()) {
                                total_url.add(res.getString(12));
                            }
                            res.close();

                            if (!total_url.contains(old_url)) {
                                File imageFileToDelete = new File(directory, old_url);
                                if (imageFileToDelete.exists()) {
                                    imageFileToDelete.delete();
                                }
                            }
                            recyclerView.setClickable(true);
                            main_progress_bar.setVisibility(View.GONE);
                            shopItemAdapter.notifyDataSetChanged();
                            if(!settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)){
                                if(mInterstitialAd != null){
                                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                        @Override
                                        public void onAdClicked() {
                                            super.onAdClicked();
                                        }

                                        @Override
                                        public void onAdDismissedFullScreenContent() {
                                            mInterstitialAd = null;
                                        }

                                        @Override
                                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                            mInterstitialAd = null;
                                        }

                                        @Override
                                        public void onAdImpression() {
                                            super.onAdImpression();
                                        }

                                        @Override
                                        public void onAdShowedFullScreenContent() {
                                            adsCounter = 0;
                                            SharedPreferences  sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);
                                            sharedPreferences.edit().putInt(UserSettings.ADS_COUNTER_KEY, adsCounter).apply();
                                            super.onAdShowedFullScreenContent();
                                        }
                                    });
                                    if(adsCounter >= 20){
                                        mInterstitialAd.show(ItemActivity.this);
                                    }else{
                                        adsCounter = adsCounter + 1;
                                        SharedPreferences  sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);
                                        sharedPreferences.edit().putInt(UserSettings.ADS_COUNTER_KEY, adsCounter).apply();
                                    }

                                }
                            }
                        } else {
                            Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__insertImageFailed), Toast.LENGTH_SHORT).show();
                        }


                    }
                }, 1000);


            } catch (Exception e) {
                Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__error) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
        if ((requestCode == 8080) && (resultCode == Activity.RESULT_OK)) {
            String voiceText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            showVoiceDialog(voiceText);
        }

    }

    public void showRenameDialog(String prevName, int position) {
        rename_dialog_2 = new Dialog(shopItemAdapter.getContext());
        rename_dialog_2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        rename_dialog_2.setContentView(R.layout.update_item_name_layout);

        TextView headingText = rename_dialog_2.findViewById(R.id.Heading);
        EditText description = rename_dialog_2.findViewById(R.id.description);
        ExtendedFloatingActionButton saveButton = rename_dialog_2.findViewById(R.id.save);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            description.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            headingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            description.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            headingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            description.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            headingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        description.setText(prevName);

        Items_Check = new ArrayList<>();

        Cursor res = db.getItems(category, ItemActivity.this);
        Items_Check.clear();
        while (res.moveToNext()) {
            Items_Check.add(res.getString(2).trim());
        }
        res.close();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = description.getText().toString().trim();

                if (!newName.isEmpty()) {
                    if (!Items_Check.contains(newName)) {
                        boolean checkEditData = db.updateItem(category, newName, prevName);
                        if (!checkEditData) {
                        } else {
                            rename_dialog_2.dismiss();
                            shopItemAdapter.refreshUpdate(newName, position);
                            shopItemAdapter.notifyItemChanged(position);
                            getsum();
                        }
                    } else {
                        Toast.makeText(shopItemAdapter.getContext(), getString(R.string.ItemActivity__alreadyExist), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(shopItemAdapter.getContext(), getString(R.string.ItemActivity__emptyName), Toast.LENGTH_SHORT).show();
                }

            }
        });

        rename_dialog_2.show();
        rename_dialog_2.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                shopItemAdapter.notifyItemChanged(position);
            }
        });
        rename_dialog_2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rename_dialog_2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        rename_dialog_2.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        rename_dialog_2.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showPriceDialog(String description, int position) {
        price_dialog = new Dialog(shopItemAdapter.getContext());
        price_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        price_dialog.setContentView(R.layout.price_update_popup);
        TextView header = price_dialog.findViewById(R.id.header);
        EditText priceValue = price_dialog.findViewById(R.id.price_name);

        TextInputLayout textInputLayout = price_dialog.findViewById(R.id.desc_price_text_input_layout);
        textInputLayout.setPrefixText(settings.getCurrency());

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            priceValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            priceValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            priceValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        double temp_price = 0;
        Cursor res = db.getPrice(category, description);
        while (res.moveToNext()) {
            temp_price = res.getDouble(4);
        }
        res.close();

        header.setText(description);
        priceValue.setText(formatNumberV2(temp_price));

        ImageButton priceSaveBtn = price_dialog.findViewById(R.id.price_btnSave);
        priceSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!priceValue.getText().toString().trim().isEmpty()) {
                    String NewPrice = priceValue.getText().toString().trim();
                    db.updatePrice(category, description, NewPrice);
                    shopItemAdapter.notifyItemChanged(position);
                    getsum();
                    price_dialog.dismiss();
                } else {
                    Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__emptyPrice), Toast.LENGTH_SHORT).show();
                }
            }
        });
        price_dialog.show();
        price_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                shopItemAdapter.notifyItemChanged(position);
            }
        });
        price_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        price_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        price_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        price_dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void showQuantityDialog(String description, int position) {
        quantity_dialog = new Dialog(shopItemAdapter.getContext());
        quantity_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        quantity_dialog.setContentView(R.layout.quantity_set_popup);
        TextView header = quantity_dialog.findViewById(R.id.header);
        EditText quantityValue = quantity_dialog.findViewById(R.id.quantity_name);

        AutoCompleteTextView unitText = quantity_dialog.findViewById(R.id.unit_textView);
        String[] unit_list = getResources().getStringArray(R.array.units);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ItemActivity.this, R.layout.unit_drop_down_layout, unit_list);
        unitText.setAdapter(arrayAdapter);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            quantityValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            unitText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            quantityValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            unitText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            quantityValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            unitText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        double temp_quantity = 0;
        String temp_unit = null;
        Cursor res = db.getQuantity(category, description);
        while (res.moveToNext()) {
            temp_quantity = res.getDouble(9);
            temp_unit = res.getString(11);
        }
        res.close();

        header.setText(description);
        quantityValue.setText(formatNumberV2(temp_quantity));
        if (!temp_unit.trim().isEmpty()) {
            unitText.setText(temp_unit);
        }


        ExtendedFloatingActionButton quantitySaveBtn = quantity_dialog.findViewById(R.id.quantity_btnSave);
        quantitySaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!quantityValue.getText().toString().trim().isEmpty()) {
                    String NewQuantity = quantityValue.getText().toString().trim();
                    String NewUnit = unitText.getText().toString().trim();
                    db.updateQuantity(category, description, NewQuantity, NewUnit);
                    shopItemAdapter.notifyItemChanged(position);
                    getsum();
                    quantity_dialog.dismiss();
                } else {
                    Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__emptyQuantity), Toast.LENGTH_SHORT).show();
                }

            }
        });
        quantity_dialog.show();
        quantity_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                shopItemAdapter.notifyItemChanged(position);
            }
        });
        quantity_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        quantity_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        quantity_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        quantity_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showDeleteDialog(int position) {
        String temp = shopItemAdapter.getItemName(position);

        delete_dialog = new Dialog(ItemActivity.this);
        delete_dialog.setContentView(R.layout.custom_delete_dialog);
        delete_dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        delete_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView delete_heading = delete_dialog.findViewById(R.id.delete_heading);
        TextView delete_message = delete_dialog.findViewById(R.id.delete_message);
        Button deleteButton = delete_dialog.findViewById(R.id.delete_button);
        Button cancelButton = delete_dialog.findViewById(R.id.cancel_button);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            delete_heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            delete_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            delete_heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            delete_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            delete_heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            delete_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }
        String temp_text = getString(R.string.ItemActivity__delete) + " " + temp;
        delete_heading.setText(temp_text);

        delete_message.setText(getString(R.string.ItemActivity__deleteWarning));

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = shopItemAdapter.getItemName(position);
                db = new ShopDatabase(ItemActivity.this);

                String photo_url = null;
                Cursor res = db.getPhotourl(category, temp);
                while (res.moveToNext()) {
                    photo_url = res.getString(12);
                }
                res.close();

                if (!photo_url.trim().isEmpty()) {
                    db.updatePhoto(category, temp, " ");

                    ArrayList<String> total_url = new ArrayList<>();
                    res = db.getCategory(ItemActivity.this);
                    while (res.moveToNext()) {
                        total_url.add(res.getString(12));
                    }
                    res.close();
                    File directory = new File(ItemActivity.this.getFilesDir(), "Buymate_Images");
                    if (!total_url.contains(photo_url)) {
                        File imageFileToDelete = new File(directory, photo_url);
                        if (imageFileToDelete.exists()) {
                            imageFileToDelete.delete();
                        }
                    }
                }

                db.deleteItem(category, temp);
                shopItemAdapter.refreshRemoved(position);
                shopItemAdapter.notifyItemRemoved(position);
                shopItemAdapter.checkEmpty();
                getsum();
                delete_dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shopItemAdapter.notifyItemChanged(position);
                delete_dialog.dismiss();
            }
        });

        delete_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                shopItemAdapter.notifyItemChanged(position);
            }
        });

        delete_dialog.show();
    }

    public void showInfoDialog(int position) {
        info_dialog = new Dialog(shopItemAdapter.getContext());
        info_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        info_dialog.setContentView(R.layout.info_item_layout);

        ImageView favouritesIcon = info_dialog.findViewById(R.id.favouritesIcon);

        CardView image_card = info_dialog.findViewById(R.id.image_card);

        ImageView itemImage = info_dialog.findViewById(R.id.item_image);
        ImageView itemImageNull = info_dialog.findViewById(R.id.item_image_null);
        ProgressBar progressBar = info_dialog.findViewById(R.id.progress_bar);

        ImageView reduce = info_dialog.findViewById(R.id.reduce);
        ImageView increase = info_dialog.findViewById(R.id.increase);

        TextView item_name = info_dialog.findViewById(R.id.item_name);
        TextView unit_price = info_dialog.findViewById(R.id.price_per_unit);
        TextView total_price = info_dialog.findViewById(R.id.total_price_item);
        LinearLayout quantity_parent = info_dialog.findViewById(R.id.quantity_parent);
        TextView quantity = info_dialog.findViewById(R.id.quantity);
        TextView unit = info_dialog.findViewById(R.id.unit);
        TextView day = info_dialog.findViewById(R.id.full_day);
        TextView date = info_dialog.findViewById(R.id.full_date);
        TextView time = info_dialog.findViewById(R.id.full_time);
        TextView quantityText = info_dialog.findViewById(R.id.quantity_text);
        TextView priceText = info_dialog.findViewById(R.id.price_text);
        TextView subTotalText = info_dialog.findViewById(R.id.subtotal_text);

        TextView currency_symbol = info_dialog.findViewById(R.id.currencybox);
        currency_symbol.setText(settings.getCurrency());


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            item_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            unit_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            total_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            quantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            unit.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            day.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            date.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            time.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            quantityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            priceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            subTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            currency_symbol.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            item_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            unit_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            total_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            quantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            unit.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            day.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            date.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            time.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            quantityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            priceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            subTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            currency_symbol.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            item_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            unit_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            total_price.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            quantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            unit.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            day.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            date.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            time.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            quantityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            priceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            subTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            currency_symbol.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        String temp = shopItemAdapter.getItemName(position);
        item_name.setText(temp);
        item_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info_dialog.dismiss();
                showRenameDialog(temp, position);
            }
        });

        int temp_fav = 0;
        double temp_price = 0;
        double unit_temp_price = 0;
        double temp_quantity = 0;
        String temp_unit = null;


        String temp_month = null;
        String temp_year = null;
        String temp_day = null;
        String temp_time = null;

        String photourl = null;

        Cursor res = db.getPrice(category, temp);
        while (res.moveToNext()) {
            temp_fav = res.getInt(10);
            temp_price = Double.parseDouble(res.getString(4));
            temp_quantity = Double.parseDouble(res.getString(9));
            temp_unit = res.getString(11);

            temp_month = res.getString(5);
            temp_year = res.getString(6);
            temp_day = res.getString(7);
            temp_time = res.getString(8);

            photourl = res.getString(12);

        }
        res.close();

        if (temp_fav == 1) {
            favouritesIcon.setImageResource(R.drawable.final_regular_favourites_colored_icon);
        } else {
            favouritesIcon.setImageResource(R.drawable.final_regular_star_icon);
        }

        String finalPhotourl = photourl;
        info_dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (!finalPhotourl.trim().isEmpty()) {
                    is_photo_url_empty = false;

                    File directory = new File(ItemActivity.this.getFilesDir(), "Buymate_Images");
                    File imageFile = new File(directory, finalPhotourl);

                    Bitmap retrieveBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                    itemImage.setImageBitmap(retrieveBitmap);
                    itemImage.setVisibility(View.VISIBLE);

                } else {
                    is_photo_url_empty = true;
                    itemImageNull.setVisibility(View.VISIBLE);
                }
            }
        });


        unit_price.setText(formatNumber(temp_price));
        unit_temp_price = temp_price;
        quantity.setText(formatNumberV2(temp_quantity));

        unit.setText(temp_unit.trim());
        day.setText(temp_day);
        String temp_text = temp_month + ", " + temp_year;
        date.setText(temp_text);
        time.setText(temp_time);

        if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
            double temp_total = temp_price * temp_quantity;
            total_price.setText(formatNumber(temp_total));
        } else {
            total_price.setText(formatNumber(temp_price));
        }


        itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp_item = temp;
                info_dialog.dismiss();
                showAddImageDialog();
            }
        });

        itemImageNull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp_item = temp;
                info_dialog.dismiss();
                showAddImageDialog();
            }
        });

        unit_price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info_dialog.dismiss();
                showPriceDialog(shopItemAdapter.getItemName(position), position);
            }
        });

        favouritesIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp_fav = 0;

                Cursor res = db.getPrice(category, temp);
                while (res.moveToNext()) {
                    temp_fav = res.getInt(10);

                }
                res.close();

                if (temp_fav == 1) {
                    favouritesIcon.setImageResource(R.drawable.final_regular_star_icon);
                    db.updateFavourites(category, temp, 0);
                    Toast.makeText(getApplicationContext(), R.string.ItemActivity__unstarred, Toast.LENGTH_SHORT).show();

                } else {
                    favouritesIcon.setImageResource(R.drawable.final_regular_favourites_colored_icon);
                    db.updateFavourites(category, temp, 1);
                    Toast.makeText(getApplicationContext(), R.string.ItemActivity__itemStarred, Toast.LENGTH_SHORT).show();
                }
                shopItemAdapter.notifyItemChanged(position);
            }
        });

        quantity_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info_dialog.dismiss();
                showQuantityDialog(shopItemAdapter.getItemName(position), position);
            }
        });

        double finalUnit_temp_price = unit_temp_price;
        reduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double temp_price;
                double temp_quantity;

                double check_quantity = Double.parseDouble(quantity.getText().toString());
                if (check_quantity > 0) {
                    if (finalUnit_temp_price == 0.0) {
                        temp_price = 0;
                    } else {
                        temp_price = finalUnit_temp_price;
                    }

                    temp_quantity = check_quantity;

                    double newQuanity = temp_quantity - 1;
                    if (newQuanity >= 0.0) {
                        quantity.setText(formatNumberV2(newQuanity));
                    } else {
                        quantity.setText(formatNumberV2(0));
                    }


                    if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
                        double temp_item_total = temp_price * Double.parseDouble(quantity.getText().toString());
                        total_price.setText(formatNumber(temp_item_total));
                        db.updateQuantity(category, temp, quantity.getText().toString(), unit.getText().toString());
                    }
                    getsum();
                }
            }
        });

        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double temp_price;
                double check_quantity = Double.parseDouble(quantity.getText().toString());

                if (finalUnit_temp_price == 0.0) {
                    temp_price = 0;
                } else {
                    temp_price = finalUnit_temp_price;
                }

                double temp_quantity = check_quantity + 1;


                double temp_item_sum = temp_price * temp_quantity;


                quantity.setText(formatNumberV2(temp_quantity));


                if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
                    total_price.setText(formatNumber(temp_item_sum));
                    db.updateQuantity(category, temp, quantity.getText().toString(), unit.getText().toString().trim());
                }


                getsum();


            }
        });

        info_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                searchView.setQuery("", false);
                searchView.onActionViewCollapsed();

                db.updateQuantity(category, temp, quantity.getText().toString(), unit.getText().toString());
                shopItemAdapter.notifyItemChanged(position);
                getsum();
            }
        });

        info_dialog.show();
        info_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        info_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        info_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        info_dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void showAlarmDialog(String description) {


        calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentMinutes = calendar.get(Calendar.MINUTE);

        timePickerDialog = new TimePickerDialog(ItemActivity.this, R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                currentHour = hourOfDay;
                currentMinutes = minute;

                Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                intent.putExtra(AlarmClock.EXTRA_HOUR, currentHour);
                intent.putExtra(AlarmClock.EXTRA_MINUTES, currentMinutes);
                intent.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.app_name) + ": " + description);

                if (intent.resolveActivity(ItemActivity.this.getPackageManager()) != null) {
                    startActivity(intent);

                } else {
                    Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__noAlarmApp), Toast.LENGTH_SHORT).show();
                }

            }
        }, currentHour, currentMinutes, false);
        WindowManager.LayoutParams params = timePickerDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        timePickerDialog.getWindow().setAttributes(params);
        timePickerDialog.getWindow().setBackgroundDrawable(ItemActivity.this.getDrawable(R.drawable.bg_time_picker));
        timePickerDialog.show();
    }

    public void showRenameDialog() {
        rename_dialog = new Dialog(ItemActivity.this);
        rename_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        rename_dialog.setContentView(R.layout.update_category_name_layout);

        TextView headingText = rename_dialog.findViewById(R.id.renameHeading);
        TextInputEditText description = rename_dialog.findViewById(R.id.description);
        ExtendedFloatingActionButton saveButton = rename_dialog.findViewById(R.id.save);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            description.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            headingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            description.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            headingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            description.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            headingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        description.setText(category);

        Cursor res = db.getCategory(ItemActivity.this);
        Items_Check.clear();
        while (res.moveToNext()) {
            Items_Check.add(res.getString(1).trim());
        }
        res.close();


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = description.getText().toString().trim();
                if (newName.trim().equals(category)) {
                    rename_dialog.dismiss();
                } else {
                    Items_Check.remove(category);
                    if (!newName.isEmpty()) {
                        if (!Items_Check.contains(newName)) {
                            boolean checkEditData = db.updateCategory(newName, category);
                            if (!checkEditData) {
                            } else {
                                itemToolbar.setTitle(newName);
                                category = newName;
                                rename_dialog.dismiss();

                            }
                        } else {
                            int count = 1;
                            String newItem = newName + " (" + count + ")";
                            while (Items_Check.contains(newItem)) {
                                count++;
                                newItem = newName + " (" + count + ")";
                            }
                            boolean checkEditData = db.updateCategory(newItem, category);
                            if (!checkEditData) {
                            } else {
                                itemToolbar.setTitle(newName);
                                category = newName;
                                rename_dialog.dismiss();

                            }
                        }
                    } else {
                        Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__emptyName), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        rename_dialog.show();
        rename_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        rename_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rename_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        rename_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        rename_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showShareTextDialog() {
        StringBuilder result = new StringBuilder();
        Cursor res = db.getItems(category, ItemActivity.this);
        Items_Check.clear();
        Items_Prices_List.clear();
        Items_Quantities_List.clear();
        while (res.moveToNext()) {
            Items_Check.add(res.getString(2).trim());
            Items_Prices_List.add(res.getString(4).trim());
            Items_Quantities_List.add(res.getString(9).trim());
            unitCheck.add(" " + res.getString(11));
        }
        res.close();


        double total = 0;
        double PriceQuantityIndex;
        for (int i = 0; i < Items_Check.size(); i++) {
            double priceIndex = Double.parseDouble(Items_Prices_List.get(i));
            double quantityIndex = Double.parseDouble(Items_Quantities_List.get(i));
            if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
                PriceQuantityIndex = priceIndex * quantityIndex;
            } else {
                PriceQuantityIndex = priceIndex;
            }
            total += PriceQuantityIndex;

            result.append("\u25CF");
            result.append(" " + Items_Check.get(i));

            if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                if (Integer.parseInt(Items_Quantities_List.get(i)) != 1) {
                    if (!unitCheck.get(i).trim().isEmpty()) {
                        result.append(" " + "[" + Items_Quantities_List.get(i) + " " + unitCheck.get(i).trim() + "]");
                    } else {
                        result.append(" " + "[" + Items_Quantities_List.get(i) + unitCheck.get(i).trim() + "]");
                    }
                }else{
                    if (!unitCheck.get(i).trim().isEmpty()) {
                        result.append(" " + "[" + Items_Quantities_List.get(i) + " " + unitCheck.get(i).trim() + "]");
                    }
                }
            }

            if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
                result.append("   ").append(formatNumber(PriceQuantityIndex));
            }

            result.append("\n");


        }

        if (settings.getIsShareTotalDisabled().equals(UserSettings.NO_SHARE_TOTAL_NOT_DISABLED)) {
            result.append("\n").append(getString(R.string.ItemActivity__Total)).append("       ").append(formatNumber(total));
        }

        String appLink = getString(R.string.app_link) + this.getPackageName();

        result.append("\n\n\n").append(getString(R.string.ItemActivity__shareBtmText1));
        result.append("\n\n").append(getString(R.string.ItemActivity__shareBtmText2));
        result.append("\n").append(appLink);

        String items = result.toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, category.toUpperCase() + "\n" + " \n" + items + "\n");

        String chooserTitle = getString(R.string.ItemActivity__shareIntentText);
        Intent chosenIntent = Intent.createChooser(intent, chooserTitle);
        startActivity(chosenIntent);

    }

    public void showDownloadPdfDialog(String prevTask) {


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Cursor res = db.getItems(category, ItemActivity.this);
                Items_Check.clear();
                Items_Prices_List.clear();
                Items_Quantities_List.clear();
                Final_Items_Prices_List.clear();
                while (res.moveToNext()) {
                    Items_Check.add(res.getString(2).trim());
                    Items_Prices_List.add(res.getString(4).trim());
                    Items_Quantities_List.add(res.getString(9).trim());
                    unitCheck.add(" " + res.getString(11));
                }
                res.close();


                double total = 0;
                double PriceQuantityIndex;
                for (int i = 0; i < Items_Check.size(); i++) {
                    double priceIndex = Double.parseDouble(Items_Prices_List.get(i));
                    double quantityIndex = Double.parseDouble(Items_Quantities_List.get(i));
                    if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
                        PriceQuantityIndex = priceIndex * quantityIndex;
                    } else {
                        PriceQuantityIndex = priceIndex;
                    }
                    Final_Items_Prices_List.add(String.valueOf(PriceQuantityIndex));
                    total += PriceQuantityIndex;

                }
                try {


                    String name = getString(R.string.app_name) + "_" + prevTask + "_" + System.currentTimeMillis() + ".pdf";
                    String filename = name.replaceAll(" ", "_");

                    File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getString(R.string.app_name));
                    if (!directory.exists()) {
                        directory.mkdirs(); // Create the directory if it doesn't exist
                    }


                    File pdfFile = new File(directory, filename);
                    PdfWriter writer = new PdfWriter(pdfFile.getAbsolutePath());
                    PdfDocument pdfDocument = new PdfDocument(writer);
                    Document document = new Document(pdfDocument);


                    int drawableResourceId = R.drawable.buymate_pdf_header;
                    Drawable drawable = ContextCompat.getDrawable(ItemActivity.this, drawableResourceId);

                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] imageBytes = stream.toByteArray();

                    ImageData imageData = ImageDataFactory.create(imageBytes);
                    Image image = new Image(imageData);

                    document.add(image);


                    int colorValue = ContextCompat.getColor(ItemActivity.this, R.color.pdf_table_header);
                    float[] rgb = new float[]{
                            Color.red(colorValue) / 255f,
                            Color.green(colorValue) / 255f,
                            Color.blue(colorValue) / 255f
                    };

                    com.itextpdf.kernel.colors.Color itextBlack = new DeviceRgb(rgb[0], rgb[1], rgb[2]);


                    colorValue = ContextCompat.getColor(ItemActivity.this, R.color.buymate_color_theme);
                    rgb = new float[]{
                            Color.red(colorValue) / 255f,
                            Color.green(colorValue) / 255f,
                            Color.blue(colorValue) / 255f
                    };

                    com.itextpdf.kernel.colors.Color itextBlue = new DeviceRgb(rgb[0], rgb[1], rgb[2]);

                    colorValue = ContextCompat.getColor(ItemActivity.this, R.color.white);
                    rgb = new float[]{
                            Color.red(colorValue) / 255f,
                            Color.green(colorValue) / 255f,
                            Color.blue(colorValue) / 255f
                    };

                    com.itextpdf.kernel.colors.Color itextWhite = new DeviceRgb(rgb[0], rgb[1], rgb[2]);

                    Paragraph introText = new Paragraph(prevTask)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.CENTER)
                            .setFontSize(24).setCharacterSpacing(2f)
                            .setBold()
                            .setFontColor(itextBlack);
                    document.add(introText);


                    getDateNdTime();

                    Paragraph date = new Paragraph(month + " " + day + "," + " " + year + " " + time)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.CENTER)
                            .setFontSize(15)
                            .setCharacterSpacing(2f);

                    document.add(date);


                    String currency = settings.getCurrency();
                    if (currency.equals("\u20A6")) {
                        currency = "NGN";
                    }

                    PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);


                    int count = 2;
                    if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                        count = count + 2;
                    }
                    if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
                        count = count + 1;
                    }


                    Table table = new Table(count);
                    table.setWidth(UnitValue.createPercentValue(100));
                    table.setMarginTop(20f);
                    table.setFontSize(18f);
                    table.setFontColor(itextBlack);

                    table.addCell(new Cell().add(new Paragraph(getString(R.string.ItemActivity__no))).setTextAlignment(TextAlignment.LEFT).setBold().setPaddingLeft(8f).setCharacterSpacing(1.5f).setFontColor(itextBlack));
                    table.addCell(new Cell().add(new Paragraph(getString(R.string.ItemActivity__item))).setTextAlignment(TextAlignment.LEFT).setBold().setPaddingLeft(8f).setCharacterSpacing(1.5f).setFontColor(itextBlack));


                    if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                        table.addCell(new Cell().add(new Paragraph(getString(R.string.ItemActivity__quantity))).setTextAlignment(TextAlignment.CENTER).setBold().setCharacterSpacing(1.5f).setFontColor(itextBlack));
                        table.addCell(new Cell().add(new Paragraph(getString(R.string.ItemActivity__unit))).setTextAlignment(TextAlignment.CENTER).setBold().setCharacterSpacing(1.5f).setFontColor(itextBlack));
                    }

                    if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
                        table.addCell(new Cell().add(new Paragraph(getString(R.string.ItemActivity__price) + "(" + currency + ")")).setTextAlignment(TextAlignment.CENTER).setBold().setCharacterSpacing(1.5f).setFontColor(itextBlack).setFont(font));
                    }


                    int num = 1;
                    for (int p = 0; p < Items_Check.size(); p++) {
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(num))).setTextAlignment(TextAlignment.LEFT).setPaddingLeft(8f));
                        table.addCell(new Cell().add(new Paragraph(Items_Check.get(p))).setTextAlignment(TextAlignment.LEFT).setPaddingLeft(8f));
                        if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                            table.addCell(new Cell().add(new Paragraph(Items_Quantities_List.get(p))).setTextAlignment(TextAlignment.CENTER));
                            table.addCell(new Cell().add(new Paragraph(unitCheck.get(p))).setTextAlignment(TextAlignment.CENTER));
                        }
                        if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
                            table.addCell(new Cell().add(new Paragraph(Final_Items_Prices_List.get(p))).setTextAlignment(TextAlignment.CENTER));
                        }
                        num = num + 1;
                    }


                    if (settings.getIsShareTotalDisabled().equals(UserSettings.NO_SHARE_TOTAL_NOT_DISABLED)) {


                        if (count == 2) {

                            table.addCell(new Cell(1, 1).add(new Paragraph(getString(R.string.ItemActivity__Total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));
                            table.addCell(new Cell(1, 1).add(new Paragraph(currency + " " + formatNumberV3(total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));

                        } else if (count == 3) {
                            table.addCell(new Cell(1, 1).add(new Paragraph(getString(R.string.ItemActivity__Total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));
                            table.addCell(new Cell(1, 2).add(new Paragraph(currency + " " + formatNumberV3(total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));


                        } else if (count == 5) {
                            table.addCell(new Cell(1, 2).add(new Paragraph(getString(R.string.ItemActivity__Total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));
                            table.addCell(new Cell(1, 3).add(new Paragraph(currency + " " + formatNumberV3(total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));

                        }
                    }

                    document.add(table);


                    Paragraph contact = new Paragraph(getString(R.string.ItemActivity__contactUs))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.CENTER)
                            .setFontSize(15)
                            .setMarginTop(50f)
                            .setCharacterSpacing(2f)
                            .setFontColor(itextBlack);
                    document.add(contact);

                    Paragraph email = new Paragraph(getString(R.string.app_email))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.CENTER)
                            .setFontSize(15)
                            .setCharacterSpacing(2f)
                            .setBold()
                            .setFontColor(itextBlue);
                    document.add(email);


                    Div dashedLine = new Div().setWidth(UnitValue.createPercentValue(100))
                            .setBorderBottom(new DashedBorder(1f))
                            .setMarginTop(20f);

                    document.add(dashedLine);

                    Paragraph close_text = new Paragraph(getString(R.string.ItemActivity__shareBtmText1))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.CENTER)
                            .setFontSize(15)
                            .setCharacterSpacing(2f)
                            .setMarginTop(20f)
                            .setFontColor(itextBlack);
                    document.add(close_text);

                    document.close();
                    progressBar.setVisibility(View.GONE);
                    show_share_dialog.dismiss();

                    StyleableToast.makeText(ItemActivity.this, getString(R.string.ItemActivity__pdfDownloadedTo) + directory, R.style.custom_toast_2).show();
                    if(!settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)){
                        if(mInterstitialAd != null){

                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    mInterstitialAd = null;
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                    mInterstitialAd = null;
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent();
                                }
                            });

                            mInterstitialAd.show(ItemActivity.this);
                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 3000);
    }

    private void showShareOptionDialog(String temp_item) {

        show_share_dialog = new Dialog(ItemActivity.this);
        show_share_dialog.setContentView(R.layout.custom_share_dialog);
        show_share_dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        show_share_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBar = show_share_dialog.findViewById(R.id.progress_bar);

        LinearLayout shareAsTextLayout = show_share_dialog.findViewById(R.id.textLayout);
        LinearLayout shareAsPDFLayout = show_share_dialog.findViewById(R.id.pdfLayout);


        shareAsTextLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShareTextDialog();
                show_share_dialog.dismiss();
            }
        });

        shareAsPDFLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                showDownloadPdfDialog(temp_item);
            }
        });

        TextView title = show_share_dialog.findViewById(R.id.title);
        TextView title_2 = show_share_dialog.findViewById(R.id.title_2);
        title_2.setText("(" + temp_item + ")");


        TextView textText = show_share_dialog.findViewById(R.id.asText);
        TextView pdfText = show_share_dialog.findViewById(R.id.asPDF);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            title_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            textText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            pdfText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            title_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            textText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            pdfText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            title_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            textText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            pdfText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        show_share_dialog.show();

    }

    public void showCopyDialog(String itemName) {
        copy_dialog = new Dialog(ItemActivity.this);
        copy_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        copy_dialog.setContentView(R.layout.movebottomlayout);
        single_selected_item = itemName;

        TextView textView = copy_dialog.findViewById(R.id.item_move_title);
        TextView emptyTEXT1 = copy_dialog.findViewById(R.id.emptyTEXT1);
        LinearLayout emptyNotesLayout = copy_dialog.findViewById(R.id.emptyNotesLayout);
        textView.setText(getString(R.string.ItemActivity__copyTo));
        SearchView searchView = copy_dialog.findViewById(R.id.search_bar);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        Button button = copy_dialog.findViewById(R.id.create_new_list);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        displayCategoryData();
        if (categoryList.isEmpty()) {
            searchView.setVisibility(View.GONE);
        }
        shopCopyAdapter.setFilterList(categoryList);
        RecyclerView copyRecyclerView = copy_dialog.findViewById(R.id.moveRecyclerView);
        copyRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        copyRecyclerView.setAdapter(shopCopyAdapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<String> filterList = new ArrayList<>();

                for (String item : categoryList) {
                    if (item.toLowerCase().contains(newText.toLowerCase())) {
                        filterList.add(item);
                    }

                }
                shopCopyAdapter.setFilterList(filterList);
                shopMoveAdapter.setFilterList(filterList);
                emptyNotesLayout.setVisibility(View.GONE);
                if (filterList.isEmpty()) {
                    emptyNotesLayout.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        copy_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialog("copy");
            }
        });


        copy_dialog.show();
        copy_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        copy_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        copy_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        copy_dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void showSortByDialog() {
        sort_by_dialog = new Dialog(ItemActivity.this);
        sort_by_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sort_by_dialog.setContentView(R.layout.sort_category_layout);

        LinearLayout nameAscend = sort_by_dialog.findViewById(R.id.name_ascend);
        CheckBox checkNameAscend = sort_by_dialog.findViewById(R.id.check_name_ascend);
        checkNameAscend.setClickable(false);
        LinearLayout nameDescend = sort_by_dialog.findViewById(R.id.name_descend);
        CheckBox checkNameDescend = sort_by_dialog.findViewById(R.id.check_name_descend);
        checkNameDescend.setClickable(false);
        LinearLayout dateAscend = sort_by_dialog.findViewById(R.id.date_ascend);
        CheckBox checkDateAscend = sort_by_dialog.findViewById(R.id.check_date_ascend);
        checkDateAscend.setClickable(false);
        LinearLayout dateDescend = sort_by_dialog.findViewById(R.id.date_descend);
        CheckBox checkDateDescend = sort_by_dialog.findViewById(R.id.check_date_descend);
        checkDateDescend.setClickable(false);
        LinearLayout priceAscend = sort_by_dialog.findViewById(R.id.price_ascend);
        CheckBox checkPriceAscend = sort_by_dialog.findViewById(R.id.check_price_ascend);
        checkPriceAscend.setClickable(false);
        LinearLayout priceDescend = sort_by_dialog.findViewById(R.id.price_descend);
        CheckBox checkPriceDescend = sort_by_dialog.findViewById(R.id.check_price_descend);
        checkPriceDescend.setClickable(false);
        LinearLayout quantityAscend = sort_by_dialog.findViewById(R.id.quantity_ascend);
        CheckBox checkQuantityAscend = sort_by_dialog.findViewById(R.id.check_quantity_ascend);
        checkQuantityAscend.setClickable(false);
        LinearLayout quantityDescend = sort_by_dialog.findViewById(R.id.quantity_descend);
        CheckBox checkQuantityDescend = sort_by_dialog.findViewById(R.id.check_quantity_descend);
        checkQuantityDescend.setClickable(false);

        TextView header = sort_by_dialog.findViewById(R.id.sortBy_title);
        TextView nameTextAscend = sort_by_dialog.findViewById(R.id.sort_nameText_ascend);
        TextView nameTextDescend = sort_by_dialog.findViewById(R.id.sort_nameText_descend);
        TextView dateTextAscend = sort_by_dialog.findViewById(R.id.sort_dateText_ascend);
        TextView dateTextDescend = sort_by_dialog.findViewById(R.id.sort_dateText_descend);
        TextView priceTextAscend = sort_by_dialog.findViewById(R.id.price_text_ascend);
        TextView priceTextDescend = sort_by_dialog.findViewById(R.id.price_text_descend);
        TextView quantityTextAscend = sort_by_dialog.findViewById(R.id.sort_quantityText_ascend);
        TextView quantityTextDescend = sort_by_dialog.findViewById(R.id.sort_quantityText_descend);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            nameTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            nameTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            dateTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            dateTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            priceTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            priceTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            quantityTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            quantityTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            nameTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            nameTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            dateTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            dateTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            priceTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            priceTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            quantityTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            quantityTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            nameTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            nameTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            dateTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            dateTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            priceTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            priceTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            quantityTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            quantityTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        SharedPreferences sharedPreferences = ItemActivity.this.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String sort = sharedPreferences.getString(UserSettings.CUSTOM_SORT, UserSettings.DATE_ASCENDING);
        settings.setCustomSort(sort);

        if (settings.getCustomSort().equals(UserSettings.NAME_ASCENDING)) {
            checkNameAscend.setChecked(true);
        } else if (settings.getCustomSort().equals(UserSettings.NAME_DESCENDING)) {
            checkNameDescend.setChecked(true);
        } else if (settings.getCustomSort().equals(UserSettings.DATE_ASCENDING)) {
            checkDateAscend.setChecked(true);
        } else if (settings.getCustomSort().equals(UserSettings.DATE_DESCENDING)) {
            checkDateDescend.setChecked(true);
        } else if (settings.getCustomSort().equals(UserSettings.PRICE_ASCENDING)) {
            checkPriceAscend.setChecked(true);
        } else if (settings.getCustomSort().equals(UserSettings.PRICE_DESCENDING)) {
            checkPriceDescend.setChecked(true);
        } else if (settings.getCustomSort().equals(UserSettings.QUANTITY_ASCENDING)) {
            checkQuantityAscend.setChecked(true);
        } else if (settings.getCustomSort().equals(UserSettings.QUANTITY_DESCENDING)) {
            checkQuantityDescend.setChecked(true);
        }
        nameAscend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkNameAscend.isChecked()) {
                    checkNameDescend.setChecked(false);
                    checkDateAscend.setChecked(false);
                    checkDateDescend.setChecked(false);
                    checkPriceAscend.setChecked(false);
                    checkPriceDescend.setChecked(false);
                    checkQuantityAscend.setChecked(false);
                    checkQuantityDescend.setChecked(false);
                    checkNameAscend.setChecked(true);
                    settings.setCustomSort(UserSettings.NAME_ASCENDING);
                }
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_SORT, settings.getCustomSort());
                editor.apply();
                displayData();
                shopItemAdapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sort_by_dialog.dismiss();
                    }
                }, 300);
            }
        });

        nameDescend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkNameDescend.isChecked()) {
                    checkNameAscend.setChecked(false);
                    checkDateAscend.setChecked(false);
                    checkDateDescend.setChecked(false);
                    checkPriceAscend.setChecked(false);
                    checkPriceDescend.setChecked(false);
                    checkQuantityAscend.setChecked(false);
                    checkQuantityDescend.setChecked(false);
                    checkNameDescend.setChecked(true);
                    settings.setCustomSort(UserSettings.NAME_DESCENDING);
                }
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_SORT, settings.getCustomSort());
                editor.apply();
                displayData();
                shopItemAdapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sort_by_dialog.dismiss();
                    }
                }, 300);
            }
        });

        dateAscend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkDateAscend.isChecked()) {
                    checkPriceAscend.setChecked(false);
                    checkNameAscend.setChecked(false);
                    checkNameDescend.setChecked(false);
                    checkDateDescend.setChecked(false);
                    checkPriceDescend.setChecked(false);
                    checkQuantityAscend.setChecked(false);
                    checkQuantityDescend.setChecked(false);
                    checkDateAscend.setChecked(true);
                    settings.setCustomSort(UserSettings.DATE_ASCENDING);
                }
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_SORT, settings.getCustomSort());
                editor.apply();
                displayData();
                shopItemAdapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sort_by_dialog.dismiss();
                    }
                }, 300);

            }
        });

        dateDescend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkDateDescend.isChecked()) {
                    checkDateAscend.setChecked(false);
                    checkPriceAscend.setChecked(false);
                    checkNameAscend.setChecked(false);
                    checkNameDescend.setChecked(false);
                    checkPriceDescend.setChecked(false);
                    checkQuantityAscend.setChecked(false);
                    checkQuantityDescend.setChecked(false);
                    checkDateDescend.setChecked(true);
                    settings.setCustomSort(UserSettings.DATE_DESCENDING);
                }
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_SORT, settings.getCustomSort());
                editor.apply();
                displayData();
                shopItemAdapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sort_by_dialog.dismiss();
                    }
                }, 300);
            }
        });

        priceAscend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPriceAscend.isChecked()) {
                    checkNameAscend.setChecked(false);
                    checkNameDescend.setChecked(false);
                    checkDateAscend.setChecked(false);
                    checkDateDescend.setChecked(false);
                    checkPriceDescend.setChecked(false);
                    checkQuantityAscend.setChecked(false);
                    checkQuantityDescend.setChecked(false);
                    checkPriceAscend.setChecked(true);
                    settings.setCustomSort(UserSettings.PRICE_ASCENDING);
                }
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_SORT, settings.getCustomSort());
                editor.apply();
                displayData();
                shopItemAdapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sort_by_dialog.dismiss();
                    }
                }, 300);
            }
        });

        priceDescend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPriceDescend.isChecked()) {
                    checkNameAscend.setChecked(false);
                    checkNameDescend.setChecked(false);
                    checkDateAscend.setChecked(false);
                    checkDateDescend.setChecked(false);
                    checkPriceAscend.setChecked(false);
                    checkQuantityAscend.setChecked(false);
                    checkQuantityDescend.setChecked(false);
                    checkPriceDescend.setChecked(true);
                    settings.setCustomSort(UserSettings.PRICE_DESCENDING);
                }
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_SORT, settings.getCustomSort());
                editor.apply();
                displayData();
                shopItemAdapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sort_by_dialog.dismiss();
                    }
                }, 300);
            }
        });

        quantityAscend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkQuantityAscend.isChecked()) {
                    checkNameAscend.setChecked(false);
                    checkNameDescend.setChecked(false);
                    checkDateAscend.setChecked(false);
                    checkDateDescend.setChecked(false);
                    checkPriceAscend.setChecked(false);
                    checkPriceDescend.setChecked(false);
                    checkQuantityDescend.setChecked(false);
                    checkQuantityAscend.setChecked(true);
                    settings.setCustomSort(UserSettings.QUANTITY_ASCENDING);
                }
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_SORT, settings.getCustomSort());
                editor.apply();
                displayData();
                shopItemAdapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sort_by_dialog.dismiss();
                    }
                }, 300);
            }
        });

        quantityDescend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkQuantityDescend.isChecked()) {
                    checkNameAscend.setChecked(false);
                    checkNameDescend.setChecked(false);
                    checkDateAscend.setChecked(false);
                    checkDateDescend.setChecked(false);
                    checkPriceAscend.setChecked(false);
                    checkPriceDescend.setChecked(false);
                    checkQuantityAscend.setChecked(false);
                    checkQuantityDescend.setChecked(true);

                    settings.setCustomSort(UserSettings.QUANTITY_DESCENDING);
                }
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_SORT, settings.getCustomSort());
                editor.apply();
                displayData();
                shopItemAdapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sort_by_dialog.dismiss();
                    }
                }, 300);
            }
        });

        sort_by_dialog.show();
        sort_by_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sort_by_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        sort_by_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        sort_by_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    public void showVoiceDialog(String voice_text) {

        voice_input_dialog = new Dialog(ItemActivity.this);
        voice_input_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        voice_input_dialog.setContentView(R.layout.voice_typing_layout);

        TextView headingText = voice_input_dialog.findViewById(R.id.Heading);
        TextInputEditText description = voice_input_dialog.findViewById(R.id.description);
        Button cancelBtn = voice_input_dialog.findViewById(R.id.cancelButton);
        Button addBtn = voice_input_dialog.findViewById(R.id.addBtn);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            description.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            headingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            addBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            description.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            headingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            addBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            description.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            headingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            addBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        description.setText(voice_text);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voice_input_dialog.dismiss();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String> descriptions = new ArrayList<>();

                for(String item : Items){
                    descriptions.add(item.toLowerCase());
                }

                if (!descriptions.contains(voice_text.toLowerCase())) {
                    getDateNdTime();
                    insertItem(category, voice_text, 0, 0, month, year, day, time, 1, " ");
                    voice_input_dialog.dismiss();
                }else {
                    Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__alreadyExist), Toast.LENGTH_SHORT).show();
                }

            }
        });

        voice_input_dialog.show();

        voice_input_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        voice_input_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        voice_input_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        voice_input_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    public void showGoogleVoiceDialog() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.ItemActivity__voicePrompt));
        startActivityForResult(intent, 8080);
    }


    public void showMoveDialog(String itemName) {
        move_dialog = new Dialog(shopItemAdapter.getContext());
        move_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        move_dialog.setContentView(R.layout.movebottomlayout);
        single_selected_item = itemName;

        TextView textView = move_dialog.findViewById(R.id.item_move_title);
        TextView emptyTEXT1 = move_dialog.findViewById(R.id.emptyTEXT1);
        LinearLayout emptyNotesLayout = move_dialog.findViewById(R.id.emptyNotesLayout);
        SearchView searchView = move_dialog.findViewById(R.id.search_bar);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        Button button = move_dialog.findViewById(R.id.create_new_list);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        textView.setText(getString(R.string.ItemActivity__moveTo));


        displayCategoryData();
        if (categoryList.isEmpty()) {
            searchView.setVisibility(View.GONE);
        }
        shopMoveAdapter.setFilterList(categoryList);
        RecyclerView moveRecyclerView = move_dialog.findViewById(R.id.moveRecyclerView);
        moveRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        moveRecyclerView.setAdapter(shopMoveAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<String> filterList = new ArrayList<>();

                for (String item : categoryList) {
                    if (item.toLowerCase().contains(newText.toLowerCase())) {
                        filterList.add(item);
                    }

                }
                shopCopyAdapter.setFilterList(filterList);
                shopMoveAdapter.setFilterList(filterList);
                emptyNotesLayout.setVisibility(View.GONE);
                if (filterList.isEmpty()) {
                    emptyNotesLayout.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        move_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialog("moved");
            }
        });


        move_dialog.show();
        move_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        move_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        move_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        move_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showUpgradeRequiredDialog() {
        upgrade_required_dialog = new Dialog(ItemActivity.this);
        upgrade_required_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        upgrade_required_dialog.setContentView(R.layout.upgrade_required_layout);


        TextView header = upgrade_required_dialog.findViewById(R.id.upgrade_text_1);
        TextView sub_header = upgrade_required_dialog.findViewById(R.id.upgrade_text_2);
        Button cancelBtn = upgrade_required_dialog.findViewById(R.id.cancelButton);
        ExtendedFloatingActionButton upgradeBtn = upgrade_required_dialog.findViewById(R.id.upgradeBtn);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upgrade_required_dialog.dismiss();
            }
        });

        upgradeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upgrade_required_dialog.dismiss();
                Intent upgrade_intent = new Intent(ItemActivity.this, PremiumActivity.class);
                startActivity(upgrade_intent);
            }
        });

        upgrade_required_dialog.show();
        upgrade_required_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        upgrade_required_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        upgrade_required_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        upgrade_required_dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void showCategoryDialog(String checkCopyOrMoved) {

        category_dialog = new Dialog(ItemActivity.this);
        category_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        category_dialog.setContentView(R.layout.category_title_set_popup);
        getDateNdTime();


        EditText categoryName = category_dialog.findViewById(R.id.category_name);
        LinearLayout newCategoryIllustrationLayout = category_dialog.findViewById(R.id.newCategory_Illustration_Layout);
        TextView emptyText1 = category_dialog.findViewById(R.id.emptyText1);
        TextView emptyText2 = category_dialog.findViewById(R.id.emptyText2);
        TextView sugest1 = category_dialog.findViewById(R.id.Suggest_1);
        TextView sugest2 = category_dialog.findViewById(R.id.Suggest_2);
        TextView sugest3 = category_dialog.findViewById(R.id.Suggest_3);
        sugest3.setText(formattedDate);
        TextView sugest4 = category_dialog.findViewById(R.id.Suggest_4);
        TextView sugest5 = category_dialog.findViewById(R.id.Suggest_5);
        TextView sugest6 = category_dialog.findViewById(R.id.Suggest_6);
        TextView sugest7 = category_dialog.findViewById(R.id.Suggest_7);
        TextView sugest8 = category_dialog.findViewById(R.id.Suggest_8);
        Button categorySaveBtn = category_dialog.findViewById(R.id.category_btnSave);


        newCategoryIllustrationLayout.setVisibility(View.VISIBLE);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            categoryName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sugest1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sugest2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sugest3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sugest4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sugest5.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sugest6.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sugest7.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sugest8.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            categorySaveBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            categoryName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sugest1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sugest2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sugest3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sugest4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sugest5.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sugest6.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sugest7.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sugest8.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            categorySaveBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            categoryName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sugest1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sugest2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sugest3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sugest4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sugest5.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sugest6.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sugest7.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sugest8.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            categorySaveBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        sugest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String existingText = categoryName.getText().toString();
                String newText = existingText + " " + sugest1.getText().toString();
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });

        sugest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String existingText = categoryName.getText().toString();
                String newText = existingText + " " + sugest2.getText().toString();
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String existingText = categoryName.getText().toString();
                String newText = existingText + " " + sugest3.getText().toString();
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String existingText = categoryName.getText().toString();
                String newText = existingText + " " + sugest4.getText().toString();
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String existingText = categoryName.getText().toString();
                String newText = existingText + " " + sugest5.getText().toString();
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String existingText = categoryName.getText().toString();
                String newText = existingText + " " + sugest6.getText().toString();
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String existingText = categoryName.getText().toString();
                String newText = existingText + " " + sugest7.getText().toString();
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String existingText = categoryName.getText().toString();
                String newText = existingText + " " + sugest8.getText().toString();
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });


        Cursor res = db.getCategory(ItemActivity.this);
        Items_Check.clear();
        while (res.moveToNext()) {
            Items_Check.add(res.getString(1).trim());
        }
        res.close();

        categorySaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!categoryName.getText().toString().trim().isEmpty()) {
                    categoryTitle = categoryName.getText().toString().trim();
                    if (!Items_Check.contains(categoryTitle)) {
                        getDateNdTime();
                        String check = checkCopyOrMoved;
                        if (Objects.equals(check, "moved")) {
                            shopMoveAdapter.insertMoved(categoryTitle);
                        } else if (Objects.equals(check, "copy")) {
                            shopCopyAdapter.insertMoved(categoryTitle);
                        }
                        category_dialog.dismiss();
                    } else {
                        int count = 1;
                        String newItem = categoryName.getText().toString().trim() + " (" + count + ")";
                        while (Items_Check.contains(newItem)) {
                            count++;
                            newItem = categoryName.getText().toString().trim() + " (" + count + ")";
                        }
                        categoryTitle = newItem;
                        String check = checkCopyOrMoved;
                        if (Objects.equals(check, "moved")) {
                            shopMoveAdapter.insertMoved(categoryTitle);
                        } else if (Objects.equals(check, "copy")) {
                            shopCopyAdapter.insertMoved(categoryTitle);
                        }

                        category_dialog.dismiss();

                    }
                } else {
                    Toast.makeText(ItemActivity.this, getString(R.string.ItemActivity__emptyNameCate), Toast.LENGTH_SHORT).show();
                }

            }
        });

        category_dialog.show();
        category_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        category_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        category_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        category_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void displayCategoryData() {
        Cursor res = db.getCategory(ItemActivity.this);
        HashSet<String> hash = new HashSet<>();
        categoryList.clear();
        while (res.moveToNext()) {
            hash.add(res.getString(1).trim());

        }
        res.close();
        categoryList.addAll(hash);
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).equals(category)) {
                categoryList.remove(i);
            }
        }
    }

    @Override
    public void onNoteClick(int position) {
        searchView.clearFocus();
        showInfoDialog(position);
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public void onOptionClick(int position) {
        searchView.clearFocus();
        String temp = shopItemAdapter.getItemName(position);
        showEditDialog(temp, position);
    }

    public static String formatNumber(double number) {
        return String.format("%,.2f", number);
    }


    //configure the menu

    public static String formatNumberV2(double number) {
        if (number == (long) number) {
            return String.format("%.0f", number);
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            return decimalFormat.format(number);
        }
    }

    public static String formatNumberV3(double number) {
        return String.format("%,.2f", number);
    }


    private void updateView() {

        SharedPreferences sharedPreferences_firstStart = ItemActivity.this.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        isFirstStart = sharedPreferences_firstStart.getBoolean("isFirstStart", true);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            Total_Summation_Textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            Items_list_size_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            Total_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            fab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            Total_Summation_Textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));
            Items_list_size_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            Total_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            fab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            Total_Summation_Textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_max_text));
            Items_list_size_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            Total_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            fab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        if (settings.getIsItemEyeDisabled().equals(UserSettings.NO_ITEM_EYE_NOT_DISABLED)) {
            eyeView.setImageResource(R.drawable.final_regular_eye);
        } else if (settings.getIsItemEyeDisabled().equals(UserSettings.YES_ITEM_EYE_DISABLED)) {
            eyeView.setImageResource(R.drawable.final_regular_eye_slash_icon);
        }

        currency_textbox.setText(settings.getCurrency());

        if (settings.getCustomTheme().equals(UserSettings.DEFAULT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        if (settings.getCustomTheme().equals(UserSettings.LIGHT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }


        if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

    }

    private void loadSharedPreferences() {

        boolean wakeLockEnabled = UserSettings.isWakeLockEnabled(this);
        if (wakeLockEnabled) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");
            wakeLock.acquire();
        }

        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);

        String theme = sharedPreferences.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);

        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        String disables_swipe = sharedPreferences.getString(UserSettings.IS_SWIPE_DISABLED, UserSettings.NOT_DISABLED);
        settings.setIsSwipeDisabled(disables_swipe);

        String is_price_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_PRICE_DISABLED, UserSettings.NO_SHARE_PRICE_NOT_DISABLED);
        settings.setIsSharePriceDisabled(is_price_disabled);

        String is_quantity_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_QUANTITY_DISABLED, UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED);
        settings.setIsShareQuantityDisabled(is_quantity_disabled);

        String is_total_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_TOTAL_DISABLED, UserSettings.NO_SHARE_TOTAL_NOT_DISABLED);
        settings.setIsShareTotalDisabled(is_total_disabled);

        String multiply_disabled = sharedPreferences.getString(UserSettings.IS_MULTIPLY_DISABLED, UserSettings.YES_MULTIPLY_DISABLED);
        settings.setIsMultiplyDisabled(multiply_disabled);

        String itemEye_disabled = sharedPreferences.getString(UserSettings.IS_ITEM_EYE_DISABLED, UserSettings.NO_ITEM_EYE_NOT_DISABLED);
        settings.setIsItemEyeDisabled(itemEye_disabled);

        String cross = sharedPreferences.getString(UserSettings.IS_CROSS_DISABLED, UserSettings.NO_CROSS_NOT_DISABLED);
        settings.setIsCrossDisabled(cross);

        String currency = sharedPreferences.getString(UserSettings.CURRENCY, UserSettings.CURRENCY_DOLLAR);
        settings.setCurrency(currency);

        String disablePrice = sharedPreferences.getString(UserSettings.IS_PRICE_DISABLED, UserSettings.NO_PRICE_NOT_DISABLED);
        settings.setIsPriceDisabled(disablePrice);

        String lifetimePurchased = sharedPreferences.getString(UserSettings.IS_LIFETIME_PURCHASED, UserSettings.NO_LIFETIME_NOT_SUBSCRIBED);
        settings.setIsLifetimePurchased(lifetimePurchased);


        String suggestion = sharedPreferences.getString(UserSettings.IS_SUGGESTION_DISABLED, UserSettings.NO_SUGGESTION_NOT_DISABLED);
        settings.setIsSuggestionDisabled(suggestion);


        updateView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        boolean isWakeLockEnabled = UserSettings.isWakeLockEnabled(this);

        if (isWakeLockEnabled) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");

            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }
        displayData();
        shopItemAdapter.notifyDataSetChanged();
    }

    private void filterList(String text) {
        ArrayList<String> filterList = new ArrayList<>();

        for (String item : Items) {
            if (item.toLowerCase().contains(text.toLowerCase())) {
                filterList.add(item);
            }
        }

        if (filterList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyNotesLayout.setVisibility(View.VISIBLE);
            favSumLayout.setVisibility(View.INVISIBLE);
        } else {
            shopItemAdapter.setFilterList(filterList);
            recyclerView.setVisibility(View.VISIBLE);
            emptyNotesLayout.setVisibility(View.GONE);
            favSumLayout.setVisibility(View.VISIBLE);
        }
    }


    boolean check_if_one_checked() {
        ArrayList<Integer> checkList = new ArrayList<>();
        for (int i = 0; i < Items.size(); i++) {
            Cursor res = db.getStatus(category, Items.get(i));
            while (res.moveToNext()) {
                checkList.add(res.getInt(3));
            }
            res.close();
        }
        return checkList.contains(1);

    }

    // checks if all are checked
    boolean check_if_all_checked() {
        ArrayList<Integer> checkList = new ArrayList<>();
        for (int i = 0; i < Items.size(); i++) {
            Cursor res = db.getStatus(category, Items.get(i));
            while (res.moveToNext()) {
                checkList.add(res.getInt(3));
            }
            res.close();
        }
        for (int i = 0; i < checkList.size(); i++) {
            if (checkList.get(i) != 1) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<String> getSelectedItems() {
        ArrayList<String> markList = new ArrayList<>();
        boolean isEnabled = shopItemAdapter.isSelected();
        if (isEnabled) {
            markList = shopItemAdapter.getSelectList();
        } else {
            markList.add(single_selected_item);
        }
        return markList;
    }



    public int getListCount() {
        return shopItemAdapter.getItemCount();
    }

    public void removeItem(String item) {
        Items.remove(item);
    }

    public void clearSelectedItems() {
        shopItemAdapter.disableSelection();
    }

    @Override
    public void onBackPressed() {
        ArrayList<String> markList = shopItemAdapter.getSelectList();
        if (!markList.isEmpty()) {
            shopItemAdapter.disableSelection();

        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }


    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
        recreate();

    }
}