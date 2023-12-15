package com.hemerick.buymate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hemerick.buymate.Adapter.RecyclerViewFavTouchHelper;
import com.hemerick.buymate.Adapter.ShopFavouritesAdapter;
import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class FavouritesFragment extends Fragment implements ShopFavouritesAdapter.OnNoteListener {
    Context context;
    Toolbar toolbar;
    RecyclerView recyclerView;

    UserSettings settings;
    ShopDatabase db;
    ArrayList<String> itemFavourites;
    ArrayList<String> category;
    ArrayList<String> itemCheck;
    ArrayList<String> priceCheck;
    ArrayList<String> quantityCheck;

    String temp_item;
    String temp_category;

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

    int CAMERA_REQUEST = 2468;

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

        main_progress_bar = rootView.findViewById(R.id.progress_bar);

        empty_text1 = rootView.findViewById(R.id.empty_text1);
        empty_text2 = rootView.findViewById(R.id.empty_text2);
        empty_text3 = rootView.findViewById(R.id.empty_text3);

        currency_textbox = rootView.findViewById(R.id.currency_box);


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
                    eyeView.setImageResource(R.drawable.final_eye);

                } else {
                    settings.setIsFavEyeDisabled(UserSettings.YES_FAV_EYE_DISABLED);
                    SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_FAV_EYE_DISABLED, settings.getIsFavEyeDisabled());
                    editor.apply();
                    getSum();

                    eyeView.setImageResource(R.drawable.final_eye_slash);

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
        String[] suggestion_list = getResources().getStringArray(R.array.item_suggestions);
        ArrayAdapter<String> suggest_adapter = new ArrayAdapter<>(context, R.layout.unit_drop_down_layout, suggestion_list);
        item_name_box.setAdapter(suggest_adapter);

        EditText item_price_box = dialog.findViewById(R.id.desc_price);
        EditText item_quantity_box = dialog.findViewById(R.id.desc_quantity);
        Button cancelButton = dialog.findViewById(R.id.CancelButton);
        Button saveButton = dialog.findViewById(R.id.BtnSave);

        TextInputLayout textInputLayout = dialog.findViewById(R.id.desc_price_text_input_layout);
        textInputLayout.setPrefixText(settings.getCurrency());

        AutoCompleteTextView unitText = dialog.findViewById(R.id.unit_textView);
        String[] unit_list = getResources().getStringArray(R.array.units);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.unit_drop_down_layout, unit_list);
        unitText.setAdapter(adapter);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            add_Item_Text_Header_1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            add_Item_Text_Header_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            item_name_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            item_price_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            unitText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            item_quantity_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
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
        }

        addItemIllustrationLayout.setVisibility(View.VISIBLE);
        add_Item_Text_Header_1.setVisibility(View.GONE);
        add_Item_Text_Header_2.setTypeface(null, Typeface.BOLD);

        String CATEGORY = "Untitled";

        itemCheck.clear();
        Cursor res = db.getItems(CATEGORY, context);
        while (res.moveToNext()) {
            itemCheck.add(res.getString(2));
        }
        res.close();

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
                        if (!itemCheck.contains(item_name_box.getText().toString().trim())) {
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
                            Toast.makeText(context, getString(R.string.item_exist_already), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(context, getString(R.string.item_exist_already), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(context, getString(R.string.insert_item_name), Toast.LENGTH_SHORT).show();
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
        SimpleDateFormat seconds = new SimpleDateFormat("h:mm:ss a", Locale.getDefault());

        day = currentDay + " " + currentDayNumber;
        month = currentMonth;
        year = currentYear;
        time = timeFormat.format(date);
        fullTimeWithSeconds = seconds.format(date);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        if (itemFavourites.isEmpty()) {
            collapsingToolbarLayout.setTitle(getString(R.string.fragment_favourites_toolbar_title));
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

                        if (finalSelectList.size() > 1) {
                            delete_heading.setText(getString(R.string.multiple_remove));
                        } else {
                            delete_heading.setText(getString(R.string.single_remove));
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
                                        favSumLayout.setVisibility(View.INVISIBLE);
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
                        StringBuilder items_selected = new StringBuilder();
                        for (String items : finalSelectList) {
                            items_selected.append(items).append("\n");
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
                        builder.setTitle(getString(R.string.single_unstar));
                        builder.setMessage(items_selected.toString());
                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {

                            }
                        });
                        builder.setPositiveButton(getString(R.string.remove_star), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                                        favSumLayout.setVisibility(View.INVISIBLE);
                                    }
                                }
                                adapter.disableSelection();
                            }

                        });

                        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        Button a = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        if (a != null) {
                            a.setTextColor(Color.GRAY);
                        }

                        Button b = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        if (b != null) {
                            b.setTextColor(Color.RED);
                            b.setPadding(50, 0, 10, 0);
                        }
                        return true;
                    }
                });
            } else {
                collapsingToolbarLayout.setTitle(getString(R.string.fragment_favourites_toolbar_title));
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
                        showShareDialog();
                        return true;
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share) {
            Toast.makeText(context, "Share Clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.sort) {
            Toast.makeText(context, "Check Clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.remove) {
            Toast.makeText(context, "Remove Clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.mark) {
            Toast.makeText(context, "Mark Clicked", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
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
        if (itemFavourites.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyFavLayout.setVisibility(View.VISIBLE);
            favSumLayout.setVisibility(View.GONE);


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


        totalItems.setText(getString(R.string.total_items_text) + " " + "(" + checked_count + "/" + itemFavourites.size() + ")");

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


        ImageView favouritesIcon = dialog.findViewById(R.id.favouritesIcon);
        ImageView reduce = dialog.findViewById(R.id.reduce);
        ImageView increase = dialog.findViewById(R.id.increase);

        ImageView itemImage = dialog.findViewById(R.id.item_image);
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
            favouritesIcon.setImageResource(R.drawable.final_favourites_colored_icon);
        } else {
            favouritesIcon.setImageResource(R.drawable.final_star_icon);
        }

        String finalPhotourl = photourl;
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (!finalPhotourl.trim().isEmpty()) {

                    progressBar.setVisibility(View.VISIBLE);

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference();
                    StorageReference imageRef = storageReference.child(finalPhotourl);
                    try {
                        final File localFile = File.createTempFile(finalPhotourl, "jpg");
                        imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                itemImage.setImageBitmap(bitmap);
                                itemImage.setVisibility(View.VISIBLE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                itemImage.setImageResource(R.drawable.final_image_not_found_icon);
                            }
                        });
                    } catch (Exception ex) {
                        Toast.makeText(context, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }, 3000);

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
                    favouritesIcon.setImageResource(R.drawable.final_star_icon);
                    db.updateFavourites(category.get(position), temp, 0);
                    Toast.makeText(context, R.string.removed_from_starred, Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);

                } else {
                    favouritesIcon.setImageResource(R.drawable.final_favourites_colored_icon);
                    db.updateFavourites(category.get(position), temp, 1);
                    Toast.makeText(context, R.string.added_to_starred, Toast.LENGTH_SHORT).show();
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
                adapter.notifyDataSetChanged();
                getSum();
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
            optionFavourites.setText(R.string.remove_from_starred);
        } else if (fav == 0) {
            optionFavourites.setText(R.string.add_to_starred);
        }
        if (url.trim().isEmpty()) {
            addImageText.setText("Add image");
        } else {
            addImageText.setText("Update image");
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
                    Toast.makeText(adapter.getContext(), R.string.removed_from_starred, Toast.LENGTH_SHORT).show();
                    itemFavourites.remove(position);
                    category.remove(position);
                    if (itemFavourites.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyFavLayout.setVisibility(View.VISIBLE);
                        favSumLayout.setVisibility(View.INVISIBLE);
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
                dialog.dismiss();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });


        TextView title = dialog.findViewById(R.id.image_title);
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
        if ((requestCode == CAMERA_REQUEST) && (resultCode == Activity.RESULT_OK)) {
            recyclerView.setClickable(false);
            main_progress_bar.setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageData = stream.toByteArray();


                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    String email = firebaseUser.getEmail().trim();
                    getDateNdTime();
                    String path = email + day + month + year + fullTimeWithSeconds + ".jpg";

                    StorageReference imageRef = storageReference.child(path);

                    UploadTask uploadTask = imageRef.putBytes(imageData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

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
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference storageReference = storage.getReference().child(old_url);
                                storageReference.delete();
                            }
                            recyclerView.setClickable(true);
                            main_progress_bar.setVisibility(View.INVISIBLE);

                        }
                    });
                }
            }, 3000);


        }

        super.onActivityResult(requestCode, resultCode, data);
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
                        if (!checkEditData) {
                            Toast.makeText(adapter.getContext(), getString(R.string.rename_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(adapter.getContext(), getString(R.string.rename_fail), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            adapter.refreshUpdate(newName, position);
                            adapter.notifyItemChanged(position);
                            getSum();
                        }
                    } else {
                        Toast.makeText(adapter.getContext(), getString(R.string.item_exist_already), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(adapter.getContext(), getString(R.string.please_insert_name), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, getString(R.string.price_change_success), Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);
                    getSum();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, getString(R.string.please_insert_price), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, getString(R.string.quantity_change_success), Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);
                    getSum();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, getString(R.string.please_insert_quantity), Toast.LENGTH_SHORT).show();
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

    public void showShareDialog() {
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

            result.append("\n\n");

        }

        if (settings.getIsShareTotalDisabled().equals(UserSettings.NO_SHARE_TOTAL_NOT_DISABLED)) {
            result.append("\n").append(getString(R.string.share_list_total)).append("       ").append(formatNumber(total));
        }

        String items = result.toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, " \n" + items + "\n");

        String chooserTitle = getString(R.string.send_message_via);
        Intent chosenIntent = Intent.createChooser(intent, chooserTitle);
        startActivity(chosenIntent);

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

        delete_heading.setText(getString(R.string.remove) + " " + temp);

        delete_message.setText(getString(R.string.remove_item_warning));

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

                    if (!total_url.contains(photo_url)) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageReference = storage.getReference().child(photo_url);
                        storageReference.delete();
                    }
                }

                db.deleteItem(adapter.getCateName(position), itemFavourites.get(position));
                category.remove(position);
                itemFavourites.remove(position);
                adapter.notifyDataSetChanged();
                if (itemFavourites.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyFavLayout.setVisibility(View.VISIBLE);
                    favSumLayout.setVisibility(View.INVISIBLE);
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

    private void updateView() {

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            totalItems.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            total.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            subTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            empty_text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            empty_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            empty_text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            totalItems.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            total.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            subTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            empty_text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            empty_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            empty_text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            totalItems.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            total.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            subTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            empty_text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            empty_text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            empty_text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        if (settings.getIsFavEyeDisabled().equals(UserSettings.NO_FAV_EYE_NOT_DISABLED)) {
            eyeView.setImageResource(R.drawable.final_eye);
        } else if (settings.getIsFavEyeDisabled().equals(UserSettings.YES_FAV_EYE_DISABLED)) {
            eyeView.setImageResource(R.drawable.final_eye_slash);
        }

        currency_textbox.setText(settings.getCurrency());
    }

    private void loadSharedPreferences() {

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);


        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        String disables_swipe = sharedPreferences.getString(UserSettings.IS_SWIPE_DISABLED, UserSettings.NOT_DISABLED);
        settings.setIsSwipeDisabled(disables_swipe);

        String is_price_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_PRICE_DISABLED, UserSettings.YES_SHARE_PRICE_DISABLED);
        settings.setIsSharePriceDisabled(is_price_disabled);

        String is_quantity_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_QUANTITY_DISABLED, UserSettings.YES_SHARE_QUANTITY_DISABLED);
        settings.setIsShareQuantityDisabled(is_quantity_disabled);

        String is_total_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_TOTAL_DISABLED, UserSettings.YES_SHARE_TOTAL_DISABLED);
        settings.setIsShareTotalDisabled(is_total_disabled);

        String multiply_disabled = sharedPreferences.getString(UserSettings.IS_MULTIPLY_DISABLED, UserSettings.NO_MULTIPLY_NOT_DISABLED);
        settings.setIsMultiplyDisabled(multiply_disabled);

        String FavEye_disabled = sharedPreferences.getString(UserSettings.IS_FAV_EYE_DISABLED, UserSettings.NO_FAV_EYE_NOT_DISABLED);
        settings.setIsFavEyeDisabled(FavEye_disabled);

        String cross = sharedPreferences.getString(UserSettings.IS_CROSS_DISABLED, UserSettings.NO_CROSS_NOT_DISABLED);
        settings.setIsCrossDisabled(cross);

        String currency = sharedPreferences.getString(UserSettings.CURRENCY, UserSettings.CURRENCY_DOLLAR);
        settings.setCurrency(currency);

        String disablePrice = sharedPreferences.getString(UserSettings.IS_PRICE_DISABLED, UserSettings.NO_PRICE_NOT_DISABLED);
        settings.setIsPriceDisabled(disablePrice);

        updateView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }
}