package com.hemerick.buymate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hemerick.buymate.Adapter.RecyclerViewFavTouchHelper;
import com.hemerick.buymate.Adapter.ShopFavouritesAdapter;
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

import io.github.muddz.styleabletoast.StyleableToast;


public class FavouritesFragment extends Fragment implements ShopFavouritesAdapter.OnNoteListener {

    SwipeRefreshLayout swipeRefreshLayout;
    Context context;
    Toolbar toolbar;
    RecyclerView recyclerView;

    ProgressBar progressBar;
    UserSettings settings;
    ShopDatabase db;
    ArrayList<String> itemFavourites;
    ArrayList<String> category;
    ArrayList<String> itemCheck;
    ArrayList<String> priceCheck;
    ArrayList<String> quantityCheck;

    HashSet<String> suggest_list;
    HashSet<String> suggest_unit_list;

    String temp_item;
    String temp_category;
    Boolean is_photo_url_empty = false;

    ShopFavouritesAdapter adapter;
    TextView totalItems;
    TextView total;
    TextView subTotalText;

    TextView empty_text1;
    TextView empty_text2;

    TextView empty_text3;

    TextView currency_textbox;

    ItemTouchHelper itemTouchHelper;
    Button fab;

    Calendar calendar;
    Date date;
    String month;
    String year;
    String day;
    String time;
    String fullTimeWithSeconds;

    ImageView eyeView;

    long totalPrice;
    FavouritesFragment fragment;

    double price = 0;
    double quantity = 0;
    ArrayList<Double> sum;

    LinearLayout emptyFavLayout;
    LinearLayout favSumLayout;
    CollapsingToolbarLayout collapsingToolbarLayout;

    ArrayList<String> selectList;
    ArrayList<String> selectListCategory;

    ProgressBar main_progress_bar;

    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    Dialog show_share_dialog;


    int CAMERA_REQUEST = 2468;
    int GALLERY_REQUEST = 1;

    int adsCounter;

    private InterstitialAd mInterstitialAd;

    public static String formatNumber(double number) {
        return String.format("%,.2f", number);
    }

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_favourites, container, false);
        context = getContext();
        settings = new UserSettings();
        collapsingToolbarLayout = rootView.findViewById(R.id.collapsing_Toolbar);
        toolbar = rootView.findViewById(R.id.favouritesToolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }

        setHasOptionsMenu(true);

        //configure the floating_action_button
        fab = rootView.findViewById(R.id.addItem_fav_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialogue();
            }
        });

        fragment = new FavouritesFragment();
        db = new ShopDatabase(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();


        itemFavourites = new ArrayList<>();
        category = new ArrayList<>();
        sum = new ArrayList<>();
        itemCheck = new ArrayList<>();
        priceCheck = new ArrayList<>();
        quantityCheck = new ArrayList<>();
        recyclerView = rootView.findViewById(R.id.favourites_list);
        totalItems = rootView.findViewById(R.id.text1);
        total = rootView.findViewById(R.id.totalPrice);
        subTotalText = rootView.findViewById(R.id.text2);
        emptyFavLayout = rootView.findViewById(R.id.emptyFavouritesLayout);
        favSumLayout = rootView.findViewById(R.id.favSumLayout);
        adapter = new ShopFavouritesAdapter(context, settings, category, itemFavourites, this, FavouritesFragment.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        suggest_list = new HashSet<>();
        suggest_unit_list = new HashSet<>();

        main_progress_bar = rootView.findViewById(R.id.progress_bar);

        empty_text1 = rootView.findViewById(R.id.empty_text1);
        empty_text2 = rootView.findViewById(R.id.empty_text2);
        empty_text3 = rootView.findViewById(R.id.empty_text3);

        currency_textbox = rootView.findViewById(R.id.currency_box);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                displayData();
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

            }
        });

        eyeView = rootView.findViewById(R.id.eyeView);
        eyeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (settings.getIsFavEyeDisabled().equals(UserSettings.YES_FAV_EYE_DISABLED)) {
                    settings.setIsFavEyeDisabled(UserSettings.NO_FAV_EYE_NOT_DISABLED);
                    SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_FAV_EYE_DISABLED, settings.getIsFavEyeDisabled());
                    editor.apply();
                    getSum();
                    eyeView.setImageResource(R.drawable.final_regular_eye);

                } else {
                    settings.setIsFavEyeDisabled(UserSettings.YES_FAV_EYE_DISABLED);
                    SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_FAV_EYE_DISABLED, settings.getIsFavEyeDisabled());
                    editor.apply();
                    getSum();

                    eyeView.setImageResource(R.drawable.final_regular_eye_slash_icon);

                }


            }
        });

        loadSharedPreferences();

        //configure the left and right swipe
        if (settings.getIsSwipeDisabled().equals(UserSettings.NOT_DISABLED)) {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerViewFavTouchHelper(adapter, settings, FavouritesFragment.this));
            itemTouchHelper.attachToRecyclerView(recyclerView);
        } else {
            itemTouchHelper = null;
        }

        displayData();




        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, "ca-app-pub-4208883735301832/9606114397", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
            }
        });


        return rootView;

    }

    private void showBottomDialogue() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.shopbottomlayout);

        LinearLayout addItemIllustrationLayout = dialog.findViewById(R.id.addItem_Illustration_Layout);

        TextView add_Item_Text_Header_1 = dialog.findViewById(R.id.addItemHeaderText);
        TextView add_Item_Text_Header_2 = dialog.findViewById(R.id.addItemHeaderText2);

        AutoCompleteTextView item_name_box = dialog.findViewById(R.id.desc_name);
        AutoCompleteTextView unitText = dialog.findViewById(R.id.unit_textView);

        EditText item_price_box = dialog.findViewById(R.id.desc_price);
        EditText item_quantity_box = dialog.findViewById(R.id.desc_quantity);
        Button cancelButton = dialog.findViewById(R.id.CancelButton);
        Button saveButton = dialog.findViewById(R.id.BtnSave);

        TextInputLayout textInputLayout = dialog.findViewById(R.id.desc_price_text_input_layout);
        textInputLayout.setPrefixText(settings.getCurrency());


        if (settings.getIsSuggestionDisabled().equals(UserSettings.YES_SUGGESTION_DISABLED)) {

        } else {
            ArrayList<String> temp_suggestion_list = new ArrayList<>(suggest_list);
            ArrayAdapter<String> suggest_adapter = new ArrayAdapter<>(context, R.layout.unit_drop_down_layout, temp_suggestion_list);
            item_name_box.setAdapter(suggest_adapter);
            ArrayList<String> temp_suggestion_unit_list = new ArrayList<>(suggest_unit_list);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.unit_drop_down_layout, temp_suggestion_unit_list);
            unitText.setAdapter(adapter);
        }


        LinearLayout more_layout = dialog.findViewById(R.id.more_layout);
        LinearLayout price_quantity_layout = dialog.findViewById(R.id.price_quantity_layout);
        TextView more_text = dialog.findViewById(R.id.more_text);
        ImageView more_image = dialog.findViewById(R.id.more_icon);

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
        add_Item_Text_Header_1.setText(getString(R.string.FavouritesFragment__addItemToList));
        add_Item_Text_Header_2.setVisibility(View.GONE);

        String CATEGORY = getString(R.string.FavouritesFragment__untitled);

        itemCheck.clear();
        Cursor res = db.getItems(CATEGORY, context);
        while (res.moveToNext()) {
            itemCheck.add(res.getString(2).toLowerCase());
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
                dialog.dismiss();
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
                        if (!itemCheck.contains(item_name_box.getText().toString().toLowerCase().trim())) {
                            if (item_quantity_box.getText().toString().isEmpty()) {
                                double temp_quantity = 1;
                                double temp_price = 0;
                                insertItem(CATEGORY, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                dialog.dismiss();
                            } else {
                                double temp_quantity = Double.parseDouble(item_quantity_box.getText().toString().trim());
                                double temp_price = 0;
                                insertItem(CATEGORY, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(context, getString(R.string.FavouritesFragment__alreadyExist), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!itemCheck.contains(item_name_box.getText().toString().trim())) {
                            if (item_quantity_box.getText().toString().isEmpty()) {
                                double temp_quantity = 1;
                                double temp_price = Double.parseDouble(item_price_box.getText().toString().trim());
                                insertItem(CATEGORY, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                dialog.dismiss();
                            } else {
                                double temp_quantity = Double.parseDouble(item_quantity_box.getText().toString().trim());
                                double temp_price = Double.parseDouble(item_price_box.getText().toString().trim());
                                insertItem(CATEGORY, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(context, getString(R.string.FavouritesFragment__alreadyExist), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(context, getString(R.string.FavouritesFragment__emptyName), Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    //for inserting new item
    public void insertItem(String cate, String description, int status, double price, String month, String year, String day, String time, double quantity, String unit) {

        String finalPrice = formatNumberV2(price);
        String finalQuantity = formatNumberV2(quantity);

        db.insertItem(cate, description, status, finalPrice, month, year, day, time, finalQuantity, unit);
        db.updateFavourites(cate, description, 1);
        itemFavourites.add(description);
        category.add(cate);
        adapter.notifyDataSetChanged();
        getSum();

        if (!suggest_list.contains(description.trim())) {
            db.insertSuggest(description);
        }

        if (!suggest_unit_list.contains(unit.trim())) {
            db.insertSuggestUnit(unit);
        }
    }

    public void getDateNdTime() {

        calendar = Calendar.getInstance();
        date = calendar.getTime();

        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMMM", Locale.getDefault());
        String currentMonth = sdfMonth.format(date);

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = sdfYear.format(date);

        SimpleDateFormat sdfDay = new SimpleDateFormat("EEEE", Locale.getDefault());
        String currentDay = sdfDay.format(date);

        SimpleDateFormat sdfDayNumber = new SimpleDateFormat("dd", Locale.getDefault());
        String currentDayNumber = sdfDayNumber.format(date);

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());


        day = currentDay + " " + currentDayNumber;
        month = currentMonth;
        year = currentYear;
        time = timeFormat.format(date);
        fullTimeWithSeconds = String.valueOf(System.currentTimeMillis());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        if (itemFavourites.isEmpty()) {
            collapsingToolbarLayout.setTitle(getString(R.string.FavouritesFragment__toolbarStarred));
        } else {
            boolean checkSelected = adapter.isSelected();
            if (checkSelected) {
                selectList = new ArrayList<String>();
                selectListCategory = new ArrayList<String>();
                selectList = adapter.getSelectList();
                selectListCategory = adapter.getSelectListCategory();
                collapsingToolbarLayout.setTitle(selectList.size() + "/" + itemFavourites.size());
                inflater.inflate(R.menu.favourites_toolbar_hold_menu, menu);

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
                ArrayList<String> finalSelectListCategory = selectListCategory;
                menu.findItem(R.id.delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(@NonNull MenuItem item) {


                        Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.custom_delete_dialog);
                        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                        StringBuilder items_selected = new StringBuilder();
                        for (String items : finalSelectList) {
                            items_selected.append("\u2022 ").append(items).append("\n");
                        }

                        TextView delete_heading = dialog.findViewById(R.id.delete_heading);
                        TextView delete_message = dialog.findViewById(R.id.delete_message);
                        Button deleteButton = dialog.findViewById(R.id.delete_button);
                        Button cancelButton = dialog.findViewById(R.id.cancel_button);


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
                            delete_heading.setText(getString(R.string.FavouritesFragment__multipleDelete));
                        } else {
                            delete_heading.setText(getString(R.string.FavouritesFragment__singleDelete));
                        }
                        delete_message.setText(items_selected.toString());

                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (int i = 0; i < finalSelectList.size(); i++) {
                                    itemFavourites.remove(finalSelectList.get(i));
                                    category.remove(finalSelectListCategory.get(i));
                                    adapter.notifyDataSetChanged();

                                    String photo_url = null;
                                    Cursor res = db.getPhotourl(selectListCategory.get(i), selectList.get(i));
                                    while (res.moveToNext()) {
                                        photo_url = res.getString(12);
                                    }

                                    if (!photo_url.trim().isEmpty()) {
                                        db.updatePhoto(selectListCategory.get(i), selectList.get(i), " ");

                                        ArrayList<String> total_url = new ArrayList<>();
                                        res = db.getCategory(context);
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
                                    db.deleteItem(selectListCategory.get(i), selectList.get(i));

                                    getSum();
                                }

                                if (itemFavourites.size() == 0) {
                                    boolean checkEmpty = adapter.checkEmpty();
                                    if (checkEmpty) {
                                        recyclerView.setVisibility(View.GONE);
                                        emptyFavLayout.setVisibility(View.VISIBLE);
                                        favSumLayout.setVisibility(View.GONE);
                                    }
                                }
                                adapter.disableSelection();
                                dialog.dismiss();
                            }
                        });

                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                        return true;
                    }
                });
                menu.findItem(R.id.mark).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(@NonNull MenuItem item) {
                        adapter.setSelectAll();

                        return true;
                    }
                });

                menu.findItem(R.id.unStar).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(@NonNull MenuItem item) {

                        Dialog menu_unstar_dialog = new Dialog(context);
                        menu_unstar_dialog.setContentView(R.layout.custom_delete_dialog);
                        menu_unstar_dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
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

                        delete_heading.setText(getString(R.string.FavouritesFragment__removeFromStarred));
                        delete_message.setText(items_selected.toString());
                        deleteButton.setText(getString(R.string.FavouritesFragment__remove));
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
                                    db.updateFavourites(finalSelectListCategory.get(i), finalSelectList.get(i), 0);
                                    itemFavourites.remove(finalSelectList.get(i));
                                    category.remove(finalSelectListCategory.get(i));
                                    adapter.notifyDataSetChanged();
                                    getSum();
                                }
                                if (itemFavourites.size() == 0) {
                                    boolean checkEmpty = adapter.checkEmpty();
                                    if (checkEmpty) {
                                        recyclerView.setVisibility(View.GONE);
                                        emptyFavLayout.setVisibility(View.VISIBLE);
                                        favSumLayout.setVisibility(View.GONE);
                                    }
                                }
                                adapter.disableSelection();
                                menu_unstar_dialog.dismiss();
                            }
                        });
                        menu_unstar_dialog.show();
                        return true;
                    }
                });

            } else {
                collapsingToolbarLayout.setTitle(getString(R.string.FavouritesFragment__toolbarStarred));
                inflater.inflate(R.menu.favourites_toolbar_menu, menu);

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
                MenuItem uncheckIcon = toolbar.getMenu().findItem(R.id.unCheck);
                MenuItem check = toolbar.getMenu().findItem(R.id.check);
                if (check_checkList) {
                    uncheckIcon.setVisible(true);
                    check.setVisible(!checked_if_all_checkList);

                } else {
                    uncheckIcon.setVisible(false);
                    check.setVisible(true);
                }

                menu.findItem(R.id.check).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(@NonNull MenuItem item) {
                        for (int i = 0; i < itemFavourites.size(); i++) {
                            db.updateStatus(category.get(i), itemFavourites.get(i), 1);
                        }
                        adapter.notifyDataSetChanged();
                        return true;
                    }
                });
                menu.findItem(R.id.unCheck).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(@NonNull MenuItem item) {
                        for (int i = 0; i < itemFavourites.size(); i++) {
                            db.updateStatus(category.get(i), itemFavourites.get(i), 0);
                        }
                        adapter.notifyDataSetChanged();
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

                menu.findItem(R.id.share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(@NonNull MenuItem item) {
                        showShareOptionDialog(collapsingToolbarLayout.getTitle().toString());
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
        }
    }


    public void displayData() {
        itemFavourites.clear();
        category.clear();
        //get total category
        Cursor res = db.getStarredCategory(context);
        ArrayList<String> cat = new ArrayList<>();
        ArrayList<String> ite = new ArrayList<>();
        while (res.moveToNext()) {
            cat.add(res.getString(1).trim());
            ite.add(res.getString(2).trim());
        }
        res.close();

        int favourite;
        for (int j = 0; j < cat.size(); j++) {
            res = db.getFavourites(cat.get(j), ite.get(j));
            while (res.moveToNext()) {
                favourite = res.getInt(10);
                if (favourite == 1) {
                    itemFavourites.add(ite.get(j));
                    category.add(cat.get(j));

                }
            }
        }

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


        if (itemFavourites.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyFavLayout.setVisibility(View.VISIBLE);
            favSumLayout.setVisibility(View.GONE);
            ((AppCompatActivity) getContext()).supportInvalidateOptionsMenu();

        } else {
            getSum();
        }
    }

    @SuppressLint("SetTextI18n")
    public void getSum() {
        sum.clear();

        if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
            for (int j = 0; j < category.size(); j++) {
                Cursor res = db.getPrice(category.get(j), itemFavourites.get(j));
                while (res.moveToNext()) {
                    price = Double.parseDouble(res.getString(4));
                    quantity = Double.parseDouble(res.getString(9));
                    sum.add(price * quantity);
                }
            }
        } else {
            for (int j = 0; j < category.size(); j++) {
                Cursor res = db.getPrice(category.get(j), itemFavourites.get(j));
                while (res.moveToNext()) {
                    price = Double.parseDouble(res.getString(4));
                    sum.add(price);
                }
            }
        }


        long su = 0;
        for (int i = 0; i < sum.size(); i++) {
            su += sum.get(i);
        }

        int checked_count = 0;
        ArrayList<String> seeChecked = new ArrayList<>();
        for (int i = 0; i < itemFavourites.size(); i++) {
            Cursor res_checked = db.getStatus(category.get(i), itemFavourites.get(i));
            while (res_checked.moveToNext()) {
                seeChecked.add(res_checked.getString(3));
            }
        }
        for (String checked : seeChecked) {
            if (checked.equals(String.valueOf(1))) {
                checked_count++;
            }
        }


        totalItems.setText(getString(R.string.FavouritesFragment__itemsChecked) + " " + "(" + checked_count + "/" + itemFavourites.size() + ")");

        if (settings.getIsFavEyeDisabled().equals(UserSettings.NO_FAV_EYE_NOT_DISABLED)) {
            total.setText(formatNumber(su));
        } else {
            total.setText("*****");
        }
        totalPrice = su;

    }

    @Override
    public void onNoteClick(int position) {

        showInfoDialog(position);

    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public void onOptionClick(int position) {
        String temp = adapter.getItemName(position);
        showEditDialog(temp, position);
    }

    public void showInfoDialog(int position) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.info_item_layout);

        CardView image_card = dialog.findViewById(R.id.image_card);

        ImageView favouritesIcon = dialog.findViewById(R.id.favouritesIcon);
        ImageView reduce = dialog.findViewById(R.id.reduce);
        ImageView increase = dialog.findViewById(R.id.increase);

        ImageView itemImage = dialog.findViewById(R.id.item_image);
        ImageView itemImageNull = dialog.findViewById(R.id.item_image_null);
        ProgressBar progressBar = dialog.findViewById(R.id.progress_bar);

        TextView item_name = dialog.findViewById(R.id.item_name);
        TextView unit_price = dialog.findViewById(R.id.price_per_unit);
        TextView total_price = dialog.findViewById(R.id.total_price_item);
        LinearLayout quantity_parent = dialog.findViewById(R.id.quantity_parent);
        TextView quantity = dialog.findViewById(R.id.quantity);
        TextView unit = dialog.findViewById(R.id.unit);
        TextView day = dialog.findViewById(R.id.full_day);
        TextView date = dialog.findViewById(R.id.full_date);
        TextView time = dialog.findViewById(R.id.full_time);
        TextView quantityText = dialog.findViewById(R.id.quantity_text);
        TextView priceText = dialog.findViewById(R.id.price_text);
        TextView subTotalText = dialog.findViewById(R.id.subtotal_text);

        TextView currency_symbol = dialog.findViewById(R.id.currencybox);
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

        String temp = adapter.getItemName(position);
        item_name.setText(temp);
        item_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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

        Cursor res = db.getPrice(category.get(position), temp);
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
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                if (!finalPhotourl.trim().isEmpty()) {
                    is_photo_url_empty = false;

                    File directory = new File(context.getFilesDir(), "Buymate_Images");
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
        date.setText(temp_month + ", " + temp_year);
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
                temp_category = category.get(position);
                dialog.dismiss();
                showAddImageDialog();
            }
        });

        itemImageNull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp_item = temp;
                temp_category = category.get(position);
                dialog.dismiss();
                showAddImageDialog();
            }
        });

        unit_price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showPriceDialog(adapter.getItemName(position), position);
            }
        });

        favouritesIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp_fav = 0;

                Cursor res = db.getPrice(category.get(position), temp);
                while (res.moveToNext()) {
                    temp_fav = res.getInt(10);

                }
                res.close();

                if (temp_fav == 1) {
                    favouritesIcon.setImageResource(R.drawable.final_regular_star_icon);
                    db.updateFavourites(category.get(position), temp, 0);
                    Toast.makeText(context, R.string.FavouritesFragment__itemUnstarred, Toast.LENGTH_SHORT).show();


                } else {
                    favouritesIcon.setImageResource(R.drawable.final_regular_favourites_colored_icon);
                    db.updateFavourites(category.get(position), temp, 1);
                    Toast.makeText(context, R.string.FavouritesFragment__itemStarred, Toast.LENGTH_SHORT).show();
                }

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


                    double newQuantity = temp_quantity - 1;
                    if (newQuantity >= 0.0) {
                        quantity.setText(formatNumberV2(newQuantity));
                    } else {
                        quantity.setText(formatNumberV2(0));
                    }


                    if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
                        double temp_item_total = temp_price * Double.parseDouble(quantity.getText().toString());
                        total_price.setText(formatNumber(temp_item_total));
                        db.updateQuantity(category.get(position), temp, quantity.getText().toString(), unit.getText().toString().trim());
                    }
                    getSum();
                }
            }
        });


        quantity_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showQuantityDialog(adapter.getItemName(position), position);
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
                    db.updateQuantity(category.get(position), temp, quantity.getText().toString(), unit.getText().toString().trim());
                }
                getSum();

            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                db.updateQuantity(category.get(position), temp, quantity.getText().toString().trim(), unit.getText().toString().trim());
                displayData();
                adapter.notifyDataSetChanged();

            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void showEditDialog(String prevTask, int position) {

        final Dialog dialog = new Dialog(adapter.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_item_layout);

        LinearLayout addImageLayout = dialog.findViewById(R.id.addImage);
        LinearLayout renameLayout = dialog.findViewById(R.id.editName);
        LinearLayout copyLayout = dialog.findViewById(R.id.copy);
        copyLayout.setVisibility(View.GONE);
        LinearLayout moveLayout = dialog.findViewById(R.id.move);
        moveLayout.setVisibility(View.GONE);
        LinearLayout changePriceLayout = dialog.findViewById(R.id.editPrice);
        LinearLayout changeQuantityLayout = dialog.findViewById(R.id.editQuantity);
        LinearLayout changeFavouritesLayout = dialog.findViewById(R.id.editFavourites);
        LinearLayout deleteLayout = dialog.findViewById(R.id.remove);
        LinearLayout infoLayout = dialog.findViewById(R.id.information);

        TextView title = dialog.findViewById(R.id.item_edit_title);
        TextView addImageText = dialog.findViewById(R.id.edit_addImage_text);
        TextView renameText = dialog.findViewById(R.id.edit_rename_text);
        TextView copyText = dialog.findViewById(R.id.edit_copy_text);
        TextView moveText = dialog.findViewById(R.id.edit_move_text);
        TextView priceText = dialog.findViewById(R.id.edit_price_text);
        TextView quantityText = dialog.findViewById(R.id.edit_quantity_text);
        TextView optionFavourites = dialog.findViewById(R.id.edit_fav_box);
        TextView deleteText = dialog.findViewById(R.id.edit_delete_text);
        TextView detailsText = dialog.findViewById(R.id.edit_details_text);

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
            addImageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            renameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
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
            addImageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            renameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            copyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            moveText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            priceText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            quantityText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            optionFavourites.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            deleteText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            detailsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        title.setText(prevTask);

        int fav = 0;
        String url = "";
        Cursor res = db.getFavourites(category.get(position), prevTask);
        while (res.moveToNext()) {
            fav = res.getInt(10);
            url = res.getString(12);
        }

        if (fav == 1) {
            optionFavourites.setText(R.string.FavouritesFragment__unstar);
        } else if (fav == 0) {
            optionFavourites.setText(R.string.FavouritesFragment__star);
        }
        if (url.trim().isEmpty()) {
            addImageText.setText(getString(R.string.FavouritesFragment__addPhoto));
        } else {
            addImageText.setText(getString(R.string.FavouritesFragment__updatePhoto));
        }


        addImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp_item = prevTask;
                temp_category = adapter.getCateName(position);
                dialog.dismiss();
                showAddImageDialog();
            }
        });
        renameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialog(prevTask, position);
                dialog.dismiss();
            }
        });

        changePriceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPriceDialog(prevTask, position);
                dialog.dismiss();
            }
        });

        changeQuantityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuantityDialog(prevTask, position);
                dialog.dismiss();
            }
        });

        changeFavouritesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int fav = 0;

                Cursor res = db.getFavourites(category.get(position), prevTask);
                while (res.moveToNext()) {
                    fav = res.getInt(10);
                }

                if (fav == 1) {
                    db.updateFavourites(category.get(position), prevTask, 0);
                    Toast.makeText(adapter.getContext(), R.string.FavouritesFragment__itemUnstarred, Toast.LENGTH_SHORT).show();
                    itemFavourites.remove(position);
                    category.remove(position);
                    if (itemFavourites.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyFavLayout.setVisibility(View.VISIBLE);
                        favSumLayout.setVisibility(View.GONE);
                        ((AppCompatActivity) getContext()).supportInvalidateOptionsMenu();
                    } else {
                        getSum();
                    }
                }

                dialog.dismiss();
                adapter.notifyItemChanged(position);
                adapter.notifyDataSetChanged();
            }
        });

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(position);
                dialog.dismiss();
            }
        });

        infoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog(position);
                dialog.dismiss();
            }
        });


        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                adapter.notifyItemChanged(position);

            }
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void showAddImageDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_image_upload_dialog);
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout takePictureLayout = dialog.findViewById(R.id.takePictureLayout);
        LinearLayout uploadPictureLayout = dialog.findViewById(R.id.uploadPictureLayout);

        takePictureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                dialog.dismiss();
                if (firebaseUser != null) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                } else {
                    Toast.makeText(context, getString(R.string.FavouritesFragment__loginPrompt), Toast.LENGTH_SHORT).show();
                }


            }
        });

        uploadPictureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                dialog.dismiss();
                if (firebaseUser != null) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, GALLERY_REQUEST);
                } else {
                    Toast.makeText(context, getString(R.string.FavouritesFragment__loginPrompt), Toast.LENGTH_SHORT).show();
                }

            }
        });


        TextView title = dialog.findViewById(R.id.image_title);
        TextView title_2 = dialog.findViewById(R.id.image_title_2);
        String temp_text = "(" + temp_item + ")";
        title_2.setText(temp_text);
        if (!is_photo_url_empty) {
            title.setText(getString(R.string.FavouritesFragment__updatePhoto));
        } else {
            title.setText(getString(R.string.FavouritesFragment__addPhoto));
        }
        TextView takePictureText = dialog.findViewById(R.id.takePictureText);
        TextView uploadPictureText = dialog.findViewById(R.id.uploadPictureText);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            takePictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            uploadPictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            takePictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            uploadPictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            takePictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            uploadPictureText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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

                    File directory = new File(context.getFilesDir(), "Buymate_Images");
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
                        Cursor res = db.getPhotourl(temp_category, temp_item);
                        while (res.moveToNext()) {
                            old_url = res.getString(12);
                        }

                        db.updatePhoto(temp_category, temp_item, path);

                        ArrayList<String> total_url = new ArrayList<>();

                        res = db.getCategory(context);
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
                        adapter.notifyDataSetChanged();

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
                                        SharedPreferences  sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, context.MODE_PRIVATE);
                                        sharedPreferences.edit().putInt(UserSettings.ADS_COUNTER_KEY, adsCounter).apply();
                                        super.onAdShowedFullScreenContent();
                                    }
                                });
                                if(adsCounter >= 20){
                                    mInterstitialAd.show(getActivity());
                                }else{
                                    adsCounter = adsCounter + 1;
                                    SharedPreferences  sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, context.MODE_PRIVATE);
                                    sharedPreferences.edit().putInt(UserSettings.ADS_COUNTER_KEY, adsCounter).apply();
                                }

                            }
                        }

                    } else {
                        Toast.makeText(context, getString(R.string.FavouritesFragment__insertImageFailed), Toast.LENGTH_SHORT).show();
                    }
                }
            }, 1000);


        }

        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            try {

                recyclerView.setClickable(false);
                main_progress_bar.setVisibility(View.VISIBLE);

                ContentResolver contentResolver = context.getContentResolver();
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

                        File directory = new File(context.getFilesDir(), "Buymate_Images");
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
                            Cursor res = db.getPhotourl(temp_category, temp_item);
                            while (res.moveToNext()) {
                                old_url = res.getString(12);
                            }

                            db.updatePhoto(temp_category, temp_item, path);

                            ArrayList<String> total_url = new ArrayList<>();

                            res = db.getCategory(context);
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
                            adapter.notifyDataSetChanged();
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
                                            SharedPreferences  sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, context.MODE_PRIVATE);
                                            sharedPreferences.edit().putInt(UserSettings.ADS_COUNTER_KEY, adsCounter).apply();
                                            super.onAdShowedFullScreenContent();
                                        }
                                    });
                                    if(adsCounter >= 20){
                                        mInterstitialAd.show(getActivity());
                                    }else{
                                        adsCounter = adsCounter + 1;
                                        SharedPreferences  sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, context.MODE_PRIVATE);
                                        sharedPreferences.edit().putInt(UserSettings.ADS_COUNTER_KEY, adsCounter).apply();
                                    }

                                }
                            }

                        } else {
                            Toast.makeText(context, getString(R.string.FavouritesFragment__insertImageFailed), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 1000);

            } catch (Exception e) {
                Toast.makeText(context, getString(R.string.FavouritesFragment__error) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
        if ((requestCode == 8080) && (resultCode == Activity.RESULT_OK)) {
            String voiceText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            showVoiceDialog(voiceText);
        }


    }

    public void showRenameDialog(String prevName, int position) {
        final Dialog dialog = new Dialog(adapter.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.update_item_name_layout);

        TextView headingText = dialog.findViewById(R.id.Heading);
        TextInputEditText description = dialog.findViewById(R.id.description);
        ExtendedFloatingActionButton saveButton = dialog.findViewById(R.id.save);

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

        itemCheck = new ArrayList<>();

        Cursor res = db.getItems(category.get(position), context);
        itemCheck.clear();
        while (res.moveToNext()) {
            itemCheck.add(res.getString(2).trim());
        }
        res.close();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = description.getText().toString().trim();

                if (!newName.isEmpty()) {
                    if (!itemCheck.contains(newName)) {
                        boolean checkEditData = db.updateItem(category.get(position), newName, prevName);
                        if (checkEditData) {
                        } else {
                            dialog.dismiss();
                            adapter.refreshUpdate(newName, position);
                            adapter.notifyItemChanged(position);
                            getSum();
                        }
                    } else {
                        Toast.makeText(adapter.getContext(), getString(R.string.FavouritesFragment__alreadyExist), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(adapter.getContext(), getString(R.string.FavouritesFragment__emptyName), Toast.LENGTH_SHORT).show();
                }

            }
        });

        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                adapter.notifyItemChanged(position);
            }
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showPriceDialog(String description, int position) {
        final Dialog dialog = new Dialog(adapter.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.price_update_popup);
        TextView header = dialog.findViewById(R.id.header);
        EditText priceValue = dialog.findViewById(R.id.price_name);

        TextInputLayout textInputLayout = dialog.findViewById(R.id.desc_price_text_input_layout);
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
        Cursor res = db.getPrice(category.get(position), description);
        while (res.moveToNext()) {
            temp_price = res.getDouble(4);
        }
        res.close();

        header.setText(description);
        priceValue.setText(formatNumberV2(temp_price));

        ImageButton priceSaveBtn = dialog.findViewById(R.id.price_btnSave);
        priceSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!priceValue.getText().toString().trim().isEmpty()) {
                    String NewPrice = priceValue.getText().toString().trim();
                    db.updatePrice(category.get(position), description, NewPrice);
                    adapter.notifyItemChanged(position);
                    getSum();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, getString(R.string.FavouritesFragment__emptyPrice), Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                adapter.notifyItemChanged(position);
            }
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void showVoiceDialog(String voice_text) {

        Dialog voice_input_dialog = new Dialog(context);
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

                for(String item : itemFavourites){
                    descriptions.add(item.toLowerCase());
                }

                if (!descriptions.contains(voice_text.toLowerCase())) {
                    String CATEGORY = getString(R.string.FavouritesFragment__untitled);
                    getDateNdTime();
                    insertItem(CATEGORY, voice_text, 0, 0, month, year, day, time, 1, " ");
                    voice_input_dialog.dismiss();
                }else {
                    Toast.makeText(context, getString(R.string.FavouritesFragment__alreadyExist), Toast.LENGTH_SHORT).show();
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
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.FavouritesFragment__voicePrompt));
        startActivityForResult(intent, 8080);
    }

    public void showQuantityDialog(String description, int position) {
        final Dialog dialog = new Dialog(adapter.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.quantity_set_popup);
        TextView header = dialog.findViewById(R.id.header);
        EditText quantityValue = dialog.findViewById(R.id.quantity_name);

        AutoCompleteTextView unitText = dialog.findViewById(R.id.unit_textView);
        String[] unit_list = getResources().getStringArray(R.array.units);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.unit_drop_down_layout, unit_list);
        unitText.setAdapter(arrayAdapter);


        double temp_quantity = 0;
        String temp_unit = null;
        Cursor res = db.getQuantity(category.get(position), description);
        while (res.moveToNext()) {
            temp_quantity = res.getDouble(9);
            temp_unit = res.getString(11);
        }
        res.close();


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

        header.setText(description);
        quantityValue.setText(formatNumberV2(temp_quantity));
        if (!temp_unit.trim().isEmpty()) {
            unitText.setText(temp_unit);
        }

        ExtendedFloatingActionButton quantitySaveBtn = dialog.findViewById(R.id.quantity_btnSave);
        quantitySaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!quantityValue.getText().toString().trim().isEmpty()) {
                    String NewQuantity = quantityValue.getText().toString().trim();
                    String NewUnit = unitText.getText().toString().trim();
                    db.updateQuantity(category.get(position), description, NewQuantity, NewUnit);
                    adapter.notifyItemChanged(position);
                    getSum();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, getString(R.string.FavouritesFragment__emptyQuantity), Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                adapter.notifyItemChanged(position);
            }
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void showSortByDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.sort_category_layout);

        LinearLayout nameAscend = dialog.findViewById(R.id.name_ascend);
        CheckBox checkNameAscend = dialog.findViewById(R.id.check_name_ascend);
        checkNameAscend.setClickable(false);
        LinearLayout nameDescend = dialog.findViewById(R.id.name_descend);
        CheckBox checkNameDescend = dialog.findViewById(R.id.check_name_descend);
        checkNameDescend.setClickable(false);
        LinearLayout dateAscend = dialog.findViewById(R.id.date_ascend);
        CheckBox checkDateAscend = dialog.findViewById(R.id.check_date_ascend);
        checkDateAscend.setClickable(false);
        LinearLayout dateDescend = dialog.findViewById(R.id.date_descend);
        CheckBox checkDateDescend = dialog.findViewById(R.id.check_date_descend);
        checkDateDescend.setClickable(false);
        LinearLayout priceAscend = dialog.findViewById(R.id.price_ascend);
        CheckBox checkPriceAscend = dialog.findViewById(R.id.check_price_ascend);
        checkPriceAscend.setClickable(false);
        LinearLayout priceDescend = dialog.findViewById(R.id.price_descend);
        CheckBox checkPriceDescend = dialog.findViewById(R.id.check_price_descend);
        checkPriceDescend.setClickable(false);
        LinearLayout quantityAscend = dialog.findViewById(R.id.quantity_ascend);
        CheckBox checkQuantityAscend = dialog.findViewById(R.id.check_quantity_ascend);
        checkQuantityAscend.setClickable(false);
        LinearLayout quantityDescend = dialog.findViewById(R.id.quantity_descend);
        CheckBox checkQuantityDescend = dialog.findViewById(R.id.check_quantity_descend);
        checkQuantityDescend.setClickable(false);


        TextView header = dialog.findViewById(R.id.sortBy_title);
        TextView nameTextAscend = dialog.findViewById(R.id.sort_nameText_ascend);
        TextView nameTextDescend = dialog.findViewById(R.id.sort_nameText_descend);
        TextView dateTextAscend = dialog.findViewById(R.id.sort_dateText_ascend);
        TextView dateTextDescend = dialog.findViewById(R.id.sort_dateText_descend);
        TextView priceTextAscend = dialog.findViewById(R.id.price_text_ascend);
        TextView priceTextDescend = dialog.findViewById(R.id.price_text_descend);
        TextView quantityTextAscend = dialog.findViewById(R.id.sort_quantityText_ascend);
        TextView quantityTextDescend = dialog.findViewById(R.id.sort_quantityText_descend);

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


        SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String sort = sharedPreferences.getString(UserSettings.CUSTOM_FAV_SORT, UserSettings.DATE_ASCENDING);
        settings.setCustomFavSort(sort);

        if (settings.getCustomFavSort().equals(UserSettings.NAME_ASCENDING)) {
            checkNameAscend.setChecked(true);
        } else if (settings.getCustomFavSort().equals(UserSettings.NAME_DESCENDING)) {
            checkNameDescend.setChecked(true);
        } else if (settings.getCustomFavSort().equals(UserSettings.DATE_ASCENDING)) {
            checkDateAscend.setChecked(true);
        } else if (settings.getCustomFavSort().equals(UserSettings.DATE_DESCENDING)) {
            checkDateDescend.setChecked(true);
        } else if (settings.getCustomFavSort().equals(UserSettings.PRICE_ASCENDING)) {
            checkPriceAscend.setChecked(true);
        } else if (settings.getCustomFavSort().equals(UserSettings.PRICE_DESCENDING)) {
            checkPriceDescend.setChecked(true);
        } else if (settings.getCustomFavSort().equals(UserSettings.QUANTITY_ASCENDING)) {
            checkQuantityAscend.setChecked(true);
        } else if (settings.getCustomFavSort().equals(UserSettings.QUANTITY_DESCENDING)) {
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
                    settings.setCustomFavSort(UserSettings.NAME_ASCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_FAV_SORT, settings.getCustomFavSort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
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
                    settings.setCustomFavSort(UserSettings.NAME_DESCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_FAV_SORT, settings.getCustomFavSort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
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
                    settings.setCustomFavSort(UserSettings.DATE_ASCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_FAV_SORT, settings.getCustomFavSort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
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
                    settings.setCustomFavSort(UserSettings.DATE_DESCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_FAV_SORT, settings.getCustomFavSort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
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
                    settings.setCustomFavSort(UserSettings.PRICE_ASCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_FAV_SORT, settings.getCustomFavSort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
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
                    settings.setCustomFavSort(UserSettings.PRICE_DESCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_FAV_SORT, settings.getCustomFavSort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
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
                    settings.setCustomFavSort(UserSettings.QUANTITY_ASCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_FAV_SORT, settings.getCustomFavSort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
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

                    settings.setCustomFavSort(UserSettings.QUANTITY_DESCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_FAV_SORT, settings.getCustomFavSort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 300);
            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void showUpgradeRequiredDialog() {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upgrade_required_layout);


        TextView header = dialog.findViewById(R.id.upgrade_text_1);
        TextView sub_header = dialog.findViewById(R.id.upgrade_text_2);
        Button cancelBtn = dialog.findViewById(R.id.cancelButton);
        ExtendedFloatingActionButton upgradeBtn = dialog.findViewById(R.id.upgradeBtn);


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
                dialog.dismiss();
            }
        });

        upgradeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent upgrade_intent = new Intent(context, PremiumActivity.class);
                startActivity(upgrade_intent);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void showShareTextDialog() {
        StringBuilder result = new StringBuilder();
        ArrayList<String> temp_category = new ArrayList<>();
        ArrayList<String> temp_favourites = new ArrayList<>();
        ArrayList<String> temp_price_list = new ArrayList<>();
        ArrayList<String> temp_quantity_list = new ArrayList<>();
        ArrayList<String> unitCheck = new ArrayList<>();

        temp_category.addAll(category);
        temp_favourites.addAll(itemFavourites);

        for (int i = 0; i < temp_favourites.size(); i++) {

            Cursor res = db.getPrice(temp_category.get(i), temp_favourites.get(i));
            while (res.moveToNext()) {
                temp_price_list.add(res.getString(4).trim());
                temp_quantity_list.add(res.getString(9).trim());
                unitCheck.add(" " + res.getString(11));

            }
            res.close();
        }

        double total = 0;
        double PriceQuantityIndex;
        for (int i = 0; i < itemFavourites.size(); i++) {
            double priceIndex = Double.parseDouble(temp_price_list.get(i));
            double quantityIndex = Double.parseDouble(temp_quantity_list.get(i));
            if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
                PriceQuantityIndex = priceIndex * quantityIndex;
            } else {
                PriceQuantityIndex = priceIndex;
            }
            total += PriceQuantityIndex;


            result.append("\u25CF");
            result.append(" " + itemFavourites.get(i));

            if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                if (Integer.parseInt(temp_quantity_list.get(i)) != 1) {
                    if (!unitCheck.get(i).trim().isEmpty()) {
                        result.append(" " + "[" + temp_quantity_list.get(i) + " " + unitCheck.get(i).trim() + "]");
                    } else {
                        result.append(" " + "[" + temp_quantity_list.get(i) + unitCheck.get(i).trim() + "]");
                    }
                }
            }


            if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
                result.append("   ").append(formatNumber(PriceQuantityIndex));
            }

            result.append("\n");
        }

        if (settings.getIsShareTotalDisabled().equals(UserSettings.NO_SHARE_TOTAL_NOT_DISABLED)) {
            result.append("\n").append(getString(R.string.FavouritesFragment__Total)).append("       ").append(formatNumber(total));
        }

        String appLink = "https://play.google.com/store/apps/details?id=" + getContext().getPackageName();

        result.append("\n\n\n").append(getString(R.string.FavouritesFragment__shareBtmText1));
        result.append("\n\n").append(getString(R.string.FavouritesFragment__shareBtmText2));
        result.append("\n").append(appLink);

        String items = result.toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.FavouritesFragment__toolbarStarred) + "\n" + " \n" + items + "\n");

        String chooserTitle = getString(R.string.FavouritesFragment__shareIntentText);
        Intent chosenIntent = Intent.createChooser(intent, chooserTitle);
        startActivity(chosenIntent);

    }

    public void showDownloadPdfDialog(String prevTask) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> temp_category = new ArrayList<>();
                ArrayList<String> temp_favourites = new ArrayList<>();
                ArrayList<String> temp_price_list = new ArrayList<>();
                ArrayList<String> temp_final_price_list = new ArrayList<>();
                ArrayList<String> temp_quantity_list = new ArrayList<>();
                ArrayList<String> unitCheck = new ArrayList<>();

                temp_category.addAll(category);
                temp_favourites.addAll(itemFavourites);

                for (int i = 0; i < temp_favourites.size(); i++) {

                    Cursor res = db.getPrice(temp_category.get(i), temp_favourites.get(i));
                    while (res.moveToNext()) {
                        temp_price_list.add(res.getString(4).trim());
                        temp_quantity_list.add(res.getString(9).trim());
                        unitCheck.add(" " + res.getString(11));

                    }
                    res.close();
                }

                double total = 0;
                double PriceQuantityIndex;
                for (int i = 0; i < itemFavourites.size(); i++) {
                    double priceIndex = Double.parseDouble(temp_price_list.get(i));
                    double quantityIndex = Double.parseDouble(temp_quantity_list.get(i));
                    if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
                        PriceQuantityIndex = priceIndex * quantityIndex;
                    } else {
                        PriceQuantityIndex = priceIndex;
                    }
                    temp_final_price_list.add(String.valueOf(PriceQuantityIndex));
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
                    Drawable drawable = ContextCompat.getDrawable(context, drawableResourceId);

                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] imageBytes = stream.toByteArray();

                    ImageData imageData = ImageDataFactory.create(imageBytes);
                    Image image = new Image(imageData);

                    document.add(image);


                    int colorValue = ContextCompat.getColor(context, R.color.pdf_table_header);
                    float[] rgb = new float[]{
                            Color.red(colorValue) / 255f,
                            Color.green(colorValue) / 255f,
                            Color.blue(colorValue) / 255f
                    };

                    com.itextpdf.kernel.colors.Color itextBlack = new DeviceRgb(rgb[0], rgb[1], rgb[2]);


                    colorValue = ContextCompat.getColor(context, R.color.buymate_color_theme);
                    rgb = new float[]{
                            Color.red(colorValue) / 255f,
                            Color.green(colorValue) / 255f,
                            Color.blue(colorValue) / 255f
                    };

                    com.itextpdf.kernel.colors.Color itextBlue = new DeviceRgb(rgb[0], rgb[1], rgb[2]);

                    colorValue = ContextCompat.getColor(context, R.color.white);
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

                    table.addCell(new Cell().add(new Paragraph(getString(R.string.FavouritesFragment__no))).setTextAlignment(TextAlignment.LEFT).setBold().setPaddingLeft(8f).setCharacterSpacing(1.5f).setFontColor(itextBlack));
                    table.addCell(new Cell().add(new Paragraph(getString(R.string.FavouritesFragment__item))).setTextAlignment(TextAlignment.LEFT).setBold().setPaddingLeft(8f).setCharacterSpacing(1.5f).setFontColor(itextBlack));


                    if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                        table.addCell(new Cell().add(new Paragraph(getString(R.string.FavouritesFragment__quantity))).setTextAlignment(TextAlignment.CENTER).setBold().setCharacterSpacing(1.5f).setFontColor(itextBlack));
                        table.addCell(new Cell().add(new Paragraph(getString(R.string.FavouritesFragment__unit))).setTextAlignment(TextAlignment.CENTER).setBold().setCharacterSpacing(1.5f).setFontColor(itextBlack));
                    }

                    if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
                        table.addCell(new Cell().add(new Paragraph(getString(R.string.FavouritesFragment__price) + "(" + currency + ")")).setTextAlignment(TextAlignment.CENTER).setBold().setCharacterSpacing(1.5f).setFontColor(itextBlack).setFont(font));
                    }


                    int num = 1;
                    for (int p = 0; p < temp_favourites.size(); p++) {
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(num))).setTextAlignment(TextAlignment.LEFT).setPaddingLeft(8f));
                        table.addCell(new Cell().add(new Paragraph(temp_favourites.get(p))).setTextAlignment(TextAlignment.LEFT).setPaddingLeft(8f));
                        if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                            table.addCell(new Cell().add(new Paragraph(temp_quantity_list.get(p))).setTextAlignment(TextAlignment.CENTER));
                            table.addCell(new Cell().add(new Paragraph(unitCheck.get(p))).setTextAlignment(TextAlignment.CENTER));
                        }
                        if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
                            table.addCell(new Cell().add(new Paragraph(temp_final_price_list.get(p))).setTextAlignment(TextAlignment.CENTER));
                        }
                        num = num + 1;
                    }


                    if (settings.getIsShareTotalDisabled().equals(UserSettings.NO_SHARE_TOTAL_NOT_DISABLED)) {


                        if (count == 2) {

                            table.addCell(new Cell(1, 1).add(new Paragraph(getString(R.string.FavouritesFragment__Total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));
                            table.addCell(new Cell(1, 1).add(new Paragraph(currency + " " + formatNumberV3(total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));

                        } else if (count == 3) {
                            table.addCell(new Cell(1, 1).add(new Paragraph(getString(R.string.FavouritesFragment__Total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));
                            table.addCell(new Cell(1, 2).add(new Paragraph(currency + " " + formatNumberV3(total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));


                        } else if (count == 5) {
                            table.addCell(new Cell(1, 2).add(new Paragraph(getString(R.string.FavouritesFragment__Total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));
                            table.addCell(new Cell(1, 3).add(new Paragraph(currency + " " + formatNumberV3(total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));

                        }
                    }

                    document.add(table);


                    Paragraph contact = new Paragraph(getString(R.string.FavouritesFragment__contactUs))
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

                    Paragraph close_text = new Paragraph(getString(R.string.FavouritesFragment__shareBtmText1))
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

                    StyleableToast.makeText(context, context.getString(R.string.FavouritesFragment__pdfDownloadedTo) + directory, R.style.custom_toast_2).show();
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

                            mInterstitialAd.show(getActivity());
                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 3000);
    }

    private void showShareOptionDialog(String temp_item) {

        show_share_dialog = new Dialog(context);
        show_share_dialog.setContentView(R.layout.custom_share_dialog);
        show_share_dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
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

    boolean check_if_one_checked() {
        ArrayList<Integer> checkList = new ArrayList<>();
        for (int i = 0; i < itemFavourites.size(); i++) {
            Cursor res = db.getStatus(category.get(i), itemFavourites.get(i));
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
        for (int i = 0; i < itemFavourites.size(); i++) {
            Cursor res = db.getStatus(category.get(i), itemFavourites.get(i));
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

    public void showDeleteDialog(int position) {
        String temp = adapter.getItemName(position);

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_delete_dialog);
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView delete_heading = dialog.findViewById(R.id.delete_heading);
        TextView delete_message = dialog.findViewById(R.id.delete_message);
        Button deleteButton = dialog.findViewById(R.id.delete_button);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);


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

        String temp_text = getString(R.string.FavouritesFragment__singleDelete) + " " + temp;
        delete_heading.setText(temp_text);

        delete_message.setText(getString(R.string.FavouritesFragment__deleteWarning));

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db = new ShopDatabase(adapter.getContext());

                String photo_url = null;
                Cursor res = db.getPhotourl(adapter.getCateName(position), itemFavourites.get(position));
                while (res.moveToNext()) {
                    photo_url = res.getString(12);
                }

                if (!photo_url.trim().isEmpty()) {
                    db.updatePhoto(adapter.getCateName(position), itemFavourites.get(position), " ");

                    ArrayList<String> total_url = new ArrayList<>();
                    res = db.getCategory(context);
                    while (res.moveToNext()) {
                        total_url.add(res.getString(12));
                    }
                    res.close();

                    File directory = new File(context.getFilesDir(), "Buymate_Images");
                    if (!total_url.contains(photo_url)) {
                        File imageFileToDelete = new File(directory, photo_url);
                        if (imageFileToDelete.exists()) {
                            imageFileToDelete.delete();
                        }
                    }
                }

                db.deleteItem(adapter.getCateName(position), itemFavourites.get(position));
                category.remove(position);
                itemFavourites.remove(position);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
                if (itemFavourites.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyFavLayout.setVisibility(View.VISIBLE);
                    favSumLayout.setVisibility(View.GONE);
                } else {
                    getSum();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.notifyItemChanged(position);
                dialog.dismiss();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                adapter.notifyItemChanged(position);
            }
        });

        dialog.show();
    }


    public boolean onBackPressed() {
        boolean check = adapter.isSelected();
        if (check) {
            adapter.disableSelection();
            adapter.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        db.close();
        super.onDestroyView();
    }

    public void showNoNetworkDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.no_connection_layout);

        TextView no_connection_Text1 = dialog.findViewById(R.id.no_connection_text_1);
        TextView no_connection_Text2 = dialog.findViewById(R.id.no_connection_text_2);
        TextView no_connection_Text3 = dialog.findViewById(R.id.no_connection_text_3);
        Button try_again_btn = dialog.findViewById(R.id.try_again);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            no_connection_Text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            no_connection_Text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            no_connection_Text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            try_again_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {

            no_connection_Text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            no_connection_Text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            no_connection_Text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            try_again_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {

            no_connection_Text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            no_connection_Text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            no_connection_Text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            try_again_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

        }


        try_again_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void updateView() {

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            totalItems.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            total.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            subTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            empty_text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            empty_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            empty_text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            fab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            totalItems.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            total.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));
            subTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            empty_text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            empty_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            fab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            empty_text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            totalItems.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            total.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_max_text));
            subTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            empty_text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            empty_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            fab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            empty_text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        if (settings.getIsFavEyeDisabled().equals(UserSettings.NO_FAV_EYE_NOT_DISABLED)) {
            eyeView.setImageResource(R.drawable.final_regular_eye);
        } else if (settings.getIsFavEyeDisabled().equals(UserSettings.YES_FAV_EYE_DISABLED)) {
            eyeView.setImageResource(R.drawable.final_regular_eye_slash_icon);
        }

        currency_textbox.setText(settings.getCurrency());
    }

    private void loadSharedPreferences() {

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);


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

        String FavEye_disabled = sharedPreferences.getString(UserSettings.IS_FAV_EYE_DISABLED, UserSettings.NO_FAV_EYE_NOT_DISABLED);
        settings.setIsFavEyeDisabled(FavEye_disabled);

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

        adsCounter = sharedPreferences.getInt(UserSettings.ADS_COUNTER_KEY, 0);

        updateView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }
}