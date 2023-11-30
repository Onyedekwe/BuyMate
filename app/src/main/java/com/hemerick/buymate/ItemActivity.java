package com.hemerick.buymate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.AlarmClock;
import android.provider.MediaStore;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hemerick.buymate.Adapter.RecyclerViewItemTouchHelper;
import com.hemerick.buymate.Adapter.ShopCopyAdapter;
import com.hemerick.buymate.Adapter.ShopItemAdapter;
import com.hemerick.buymate.Adapter.ShopMoveAdapter;
import com.hemerick.buymate.Database.Firebase;
import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

public class ItemActivity extends AppCompatActivity implements ShopItemAdapter.OnNoteListener {

    String category;

    String temp_item;
    Toolbar itemToolbar;
    SearchView searchView;
    ArrayList<String> Items;
    ArrayList<String> categoryList;
    RecyclerView recyclerView;
    ShopItemAdapter shopItemAdapter;
    ShopMoveAdapter shopMoveAdapter;
    ShopCopyAdapter shopCopyAdapter;
    ExtendedFloatingActionButton fab;
    ItemTouchHelper itemTouchHelper;
    ArrayList<String> Items_Prices_List;
    ArrayList<String> Items_Quantities_List;
    ShopDatabase db;
    Firebase firebase;
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
    //favourites
    ArrayList<String> Fav_Item_category;
    ArrayList<String> Item_Favourites_List;
    ArrayList<String> filter;
    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;

    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    int CAMERA_REQUEST = 2468;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);


        //get the sharedPreferences
        settings = (UserSettings) getApplication();


        //get the category name
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        category = extras.getString("ITEM").trim();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();


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

        //configure the recyclerview
        recyclerView = findViewById(R.id.items_recyclerView);
        shopItemAdapter = new ShopItemAdapter(this, settings, Items, this, category, ItemActivity.this);
        shopMoveAdapter = new ShopMoveAdapter(this, settings, categoryList, category, ItemActivity.this);
        shopCopyAdapter = new ShopCopyAdapter(this, settings, categoryList, category, ItemActivity.this);
        recyclerView.setAdapter(shopItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //configure the left and right swipe


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
        firebase = new Firebase(this);

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
                    eyeView.setImageResource(R.drawable.final_eye);

                } else {
                    settings.setIsItemEyeDisabled(UserSettings.YES_ITEM_EYE_DISABLED);
                    SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(UserSettings.IS_ITEM_EYE_DISABLED, settings.getIsItemEyeDisabled());
                    editor.apply();
                    getsum();

                    eyeView.setImageResource(R.drawable.final_eye_slash);

                }


            }
        });

        Fav_Item_category = new ArrayList<>();
        Item_Favourites_List = new ArrayList<>();

        Items_Check = new ArrayList<>();
        filter = new ArrayList<>();
        Items_Prices_List = new ArrayList<>();
        Items_Quantities_List = new ArrayList<>();

        loadSharedPreferences();

        if (settings.getIsSwipeDisabled().equals(UserSettings.NOT_DISABLED)) {
            itemTouchHelper = new ItemTouchHelper(new RecyclerViewItemTouchHelper(shopItemAdapter, settings, category, ItemActivity.this));
            itemTouchHelper.attachToRecyclerView(recyclerView);
        } else {
            itemTouchHelper = null;
        }


        displayData();
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

                    Dialog dialog = new Dialog(ItemActivity.this);
                    dialog.setContentView(R.layout.custom_delete_dialog);
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
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
                            for (String s : finalSelectList) {
                                db.deleteItem(category, s);
                                Items.remove(s);
                            }

                            if (Items.size() == 0) {
                                shopItemAdapter.checkEmpty();
                            }
                            shopItemAdapter.disableSelection();
                            getsum();
                            shopItemAdapter.notifyDataSetChanged();
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

            menu.findItem(R.id.move).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {

                    final Dialog dialog = new Dialog(shopItemAdapter.getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.movebottomlayout);
                    TextView textView = dialog.findViewById(R.id.item_move_title);
                    textView.setText(getString(R.string.move_to));
                    SearchView searchView = dialog.findViewById(R.id.search_bar);
                    Button button = dialog.findViewById(R.id.create_new_list);
                    displayCategoryData();
                    shopMoveAdapter.setFilterList(categoryList);
                    RecyclerView moveRecyclerView = dialog.findViewById(R.id.moveRecyclerView);
                    moveRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                    moveRecyclerView.setAdapter(shopMoveAdapter);

                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            filterList2(newText);
                            return true;
                        }
                    });

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showCategoryDialog("moved");
                        }
                    });

                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                    dialog.getWindow().setGravity(Gravity.BOTTOM);
                    return true;
                }
            });

            menu.findItem(R.id.copy).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    final Dialog dialog = new Dialog(shopItemAdapter.getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.movebottomlayout);
                    TextView textView = dialog.findViewById(R.id.item_move_title);
                    textView.setText(getString(R.string.copy_to));
                    SearchView searchView = dialog.findViewById(R.id.search_bar);
                    Button button = dialog.findViewById(R.id.create_new_list);
                    displayCategoryData();
                    shopCopyAdapter.setFilterList(categoryList);
                    RecyclerView copyRecyclerView = dialog.findViewById(R.id.moveRecyclerView);
                    copyRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                    copyRecyclerView.setAdapter(shopCopyAdapter);
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            filterList2(newText);
                            return true;
                        }
                    });

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showCategoryDialog("copy");
                        }
                    });

                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                    dialog.getWindow().setGravity(Gravity.BOTTOM);
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
                    StringBuilder items_selected = new StringBuilder();
                    for (String items : finalSelectList) {
                        items_selected.append(items).append("\n");
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(shopItemAdapter.getContext());
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
                                db.updateFavourites(category, finalSelectList.get(i), 0);
                                shopItemAdapter.notifyDataSetChanged();

                            }
                            shopItemAdapter.disableSelection();
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
            searchView.setQueryHint(getString(R.string.item_search_hint));
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
                    showShareDialog();
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


        Items_list_size_textbox.setText(getString(R.string.total_items_text) + " " + "(" + checked_count + "/" + Items.size() + ")");


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

        Items_list_size_textbox.setText(getString(R.string.total_items_text) + " " + "(" + checked_count + "/" + Items.size() + ")");

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
        fullTimeWithSeconds = seconds.format(date);
        formattedDate = currentDayNumber + "/" + currentMonthNumber + "/" + currentYear;
    }

    //dialog to add a new item
    public void showBottomDialogue() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.shopbottomlayout);

        LinearLayout addItemIllustrationLayout = dialog.findViewById(R.id.addItem_Illustration_Layout);

        TextView add_Item_Text_Header_1 = dialog.findViewById(R.id.addItemHeaderText);
        TextView add_Item_Text_Header_2 = dialog.findViewById(R.id.addItemHeaderText2);

        AutoCompleteTextView item_name_box = dialog.findViewById(R.id.desc_name);
        String[] suggestion_list = getResources().getStringArray(R.array.item_suggestions);
        ArrayAdapter<String> suggest_adapter = new ArrayAdapter<>(ItemActivity.this, R.layout.unit_drop_down_layout, suggestion_list);
        item_name_box.setAdapter(suggest_adapter);


        EditText item_price_box = dialog.findViewById(R.id.desc_price);
        EditText item_quantity_box = dialog.findViewById(R.id.desc_quantity);
        Button cancelButton = dialog.findViewById(R.id.CancelButton);
        Button saveButton = dialog.findViewById(R.id.BtnSave);

        TextInputLayout textInputLayout = dialog.findViewById(R.id.desc_price_text_input_layout);
        textInputLayout.setPrefixText(settings.getCurrency());

        AutoCompleteTextView unitText = dialog.findViewById(R.id.unit_textView);
        String[] unit_list = getResources().getStringArray(R.array.units);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.unit_drop_down_layout, unit_list);
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


        Items_Check.clear();
        Cursor res = db.getItems(category, ItemActivity.this);
        while (res.moveToNext()) {
            Items_Check.add(res.getString(2));
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
                        if (!Items_Check.contains(item_name_box.getText().toString().trim())) {
                            if (item_quantity_box.getText().toString().isEmpty()) {
                                double temp_quantity = 1;
                                double temp_price = 0;
                                insertItem(category, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                dialog.dismiss();
                            } else {
                                double temp_quantity = Double.parseDouble(item_quantity_box.getText().toString().trim());
                                double temp_price = 0;
                                insertItem(category, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(ItemActivity.this, getString(R.string.item_exist_already), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!Items_Check.contains(item_name_box.getText().toString().trim())) {
                            if (item_quantity_box.getText().toString().isEmpty()) {
                                double temp_quantity = 1;
                                double temp_price = Double.parseDouble(item_price_box.getText().toString().trim());
                                insertItem(category, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                dialog.dismiss();
                            } else {
                                double temp_quantity = Double.parseDouble(item_quantity_box.getText().toString().trim());
                                double temp_price = Double.parseDouble(item_price_box.getText().toString().trim());
                                insertItem(category, description, 0, temp_price, month, year, day, time, temp_quantity, unit);
                                dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(ItemActivity.this, getString(R.string.item_exist_already), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(ItemActivity.this, getString(R.string.insert_item_name), Toast.LENGTH_SHORT).show();
                }
                shopItemAdapter.notifyItemInserted(Items.size() + 1);
                getsum();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    //for inserting new item
    public void insertItem(String category, String description, int status, double price, String month, String year, String day, String time, double quantity, String unit) {

        String finalPrice = formatNumberV2(price);
        String finalQuantity = formatNumberV2(quantity);

        db.insertItem(category, description, status, finalPrice, month, year, day, time, finalQuantity, unit);
        firebase.insertNewData(category, description, status, finalPrice, month, year, day, time, finalQuantity, unit);
        Items.add(description);
        shopItemAdapter.notifyDataSetChanged();
        getsum();
    }

    public void showEditDialog(String prevTask, int position) {

        final Dialog dialog = new Dialog(ItemActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_item_layout);


        LinearLayout addImageLayout = dialog.findViewById(R.id.addImage);
        LinearLayout renameLayout = dialog.findViewById(R.id.editName);
        LinearLayout copyLayout = dialog.findViewById(R.id.copy);
        LinearLayout moveLayout = dialog.findViewById(R.id.move);
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
            optionFavourites.setText(R.string.remove_from_starred);
        } else {
            optionFavourites.setText(R.string.add_to_starred);
        }

        if (url.trim().isEmpty()) {
            addImageText.setText("Add image");
        } else {
            addImageText.setText("Update image");
        }
        //end of configuration

        addImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp_item = prevTask;
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

        copyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showCopyDailog(prevTask);
            }
        });

        moveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showMoveDialog(prevTask);
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

                int temp_fav = 0;
                Cursor res = db.getFavourites(category, prevTask);
                while (res.moveToNext()) {
                    temp_fav = res.getInt(10);
                }
                res.close();

                if (temp_fav == 1) {
                    db.updateFavourites(category, prevTask, 0);
                    Toast.makeText(ItemActivity.this, R.string.removed_from_starred, Toast.LENGTH_SHORT).show();
                } else {

                    db.updateFavourites(category, prevTask, 1);
                    Toast.makeText(ItemActivity.this, R.string.added_to_starred, Toast.LENGTH_SHORT).show();

                }
                dialog.dismiss();
                shopItemAdapter.notifyItemChanged(position);
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
                searchView.setQuery("", false);
                searchView.onActionViewCollapsed();
                shopItemAdapter.notifyItemChanged(position);
            }
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void showAddImageDialog() {
        final Dialog dialog = new Dialog(ItemActivity.this);
        dialog.setContentView(R.layout.custom_image_upload_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ((requestCode == CAMERA_REQUEST) && (resultCode == Activity.RESULT_OK)) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageData = stream.toByteArray();


            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            String email = firebaseUser.getEmail().trim();
            getDateNdTime();
            String path = email + "#" + day + "#" + month + "#" + year + "#" + fullTimeWithSeconds;

            StorageReference imageRef = storageReference.child(path);

            UploadTask uploadTask = imageRef.putBytes(imageData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ItemActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String url = taskSnapshot.getStorage().getDownloadUrl().toString();
                    db.updatePhoto(category, temp_item, url);
                }
            });

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showRenameDialog(String prevName, int position) {
        final Dialog dialog = new Dialog(shopItemAdapter.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.update_item_name_layout);

        TextView headingText = dialog.findViewById(R.id.Heading);
        EditText description = dialog.findViewById(R.id.description);
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
                        firebase.updateItemName(category.trim(), newName.trim(), prevName.trim());
                        if (!checkEditData) {
                            Toast.makeText(shopItemAdapter.getContext(), getString(R.string.rename_fail), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(shopItemAdapter.getContext(), getString(R.string.rename_success), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            shopItemAdapter.refreshUpdate(newName, position);
                            shopItemAdapter.notifyItemChanged(position);
                            getsum();
                        }
                    } else {
                        Toast.makeText(shopItemAdapter.getContext(), getString(R.string.item_exist_already), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(shopItemAdapter.getContext(), getString(R.string.insert_item_name), Toast.LENGTH_SHORT).show();
                }

            }
        });

        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                shopItemAdapter.notifyItemChanged(position);
            }
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showPriceDialog(String description, int position) {
        final Dialog dialog = new Dialog(shopItemAdapter.getContext());
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
        Cursor res = db.getPrice(category, description);
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
                    db.updatePrice(category, description, NewPrice);
                    firebase.updatePrice(category, description, NewPrice);
                    Toast.makeText(ItemActivity.this, getString(R.string.price_change_success), Toast.LENGTH_SHORT).show();
                    shopItemAdapter.notifyItemChanged(position);
                    getsum();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ItemActivity.this, getString(R.string.please_insert_price), Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                shopItemAdapter.notifyItemChanged(position);
            }
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void showQuantityDialog(String description, int position) {
        final Dialog dialog = new Dialog(shopItemAdapter.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.quantity_set_popup);
        TextView header = dialog.findViewById(R.id.header);
        EditText quantityValue = dialog.findViewById(R.id.quantity_name);

        AutoCompleteTextView unitText = dialog.findViewById(R.id.unit_textView);
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


        ExtendedFloatingActionButton quantitySaveBtn = dialog.findViewById(R.id.quantity_btnSave);
        quantitySaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!quantityValue.getText().toString().trim().isEmpty()) {
                    String NewQuantity = quantityValue.getText().toString().trim();
                    String NewUnit = unitText.getText().toString().trim();
                    db.updateQuantity(category, description, NewQuantity, NewUnit);
                    firebase.updateQuantity(category, description, NewQuantity, NewUnit);
                    Toast.makeText(ItemActivity.this, getString(R.string.quantity_change_success), Toast.LENGTH_SHORT).show();
                    shopItemAdapter.notifyItemChanged(position);
                    getsum();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ItemActivity.this, getString(R.string.please_insert_quantity), Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                shopItemAdapter.notifyItemChanged(position);
            }
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showDeleteDialog(int position) {
        String temp = shopItemAdapter.getItemName(position);

        Dialog dialog = new Dialog(ItemActivity.this);
        dialog.setContentView(R.layout.custom_delete_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
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
                String temp = shopItemAdapter.getItemName(position);
                db = new ShopDatabase(ItemActivity.this);
                db.deleteItem(category, temp);
                firebase.deleteItem(category, temp);
                shopItemAdapter.refreshRemoved(position);
                shopItemAdapter.notifyItemRemoved(position);
                shopItemAdapter.checkEmpty();
                getsum();
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shopItemAdapter.notifyItemChanged(position);
                dialog.dismiss();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                shopItemAdapter.notifyItemChanged(position);
            }
        });

        dialog.show();
    }

    public void showInfoDialog(int position) {
        final Dialog dialog = new Dialog(shopItemAdapter.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.info_item_layout);

        ImageView favouritesIcon = dialog.findViewById(R.id.favouritesIcon);
        ImageView reduce = dialog.findViewById(R.id.reduce);
        ImageView increase = dialog.findViewById(R.id.increase);

        TextView item_name = dialog.findViewById(R.id.item_name);
        TextView unit_price = dialog.findViewById(R.id.price_per_unit);
        TextView total_price = dialog.findViewById(R.id.total_price_item);
        LinearLayout quantity_parent = dialog.findViewById(R.id.quantity_parent);
        TextView quantity = dialog.findViewById(R.id.quantity);
        TextView unit = dialog.findViewById(R.id.unit);
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
                dialog.dismiss();
                showRenameDialog(temp, position);
            }
        });

        int temp_fav = 0;
        double temp_price = 0;
        double temp_quantity = 0;
        String temp_unit = null;


        String temp_month = null;
        String temp_year = null;
        String temp_day = null;
        String temp_time = null;

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
        }
        res.close();

        if (temp_fav == 1) {
            favouritesIcon.setImageResource(R.drawable.final_favourites_colored_icon);
        } else {
            favouritesIcon.setImageResource(R.drawable.final_star_icon);
        }


        unit_price.setText(formatNumberV2(temp_price));
        quantity.setText(formatNumberV2(temp_quantity));

        unit.setText(temp_unit.trim());
        date.setText(String.format("%s, %s %s", temp_day, temp_month, temp_year));
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
                    favouritesIcon.setImageResource(R.drawable.final_star_icon);
                    db.updateFavourites(category, temp, 0);
                    firebase.updateFavourites(category, temp, 0);
                    Toast.makeText(getApplicationContext(), R.string.removed_from_starred, Toast.LENGTH_SHORT).show();

                } else {
                    favouritesIcon.setImageResource(R.drawable.final_favourites_colored_icon);
                    db.updateFavourites(category, temp, 1);
                    firebase.updateFavourites(category, temp, 1);
                    Toast.makeText(getApplicationContext(), R.string.added_to_starred, Toast.LENGTH_SHORT).show();
                }
                shopItemAdapter.notifyItemChanged(position);
            }
        });

        quantity_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showQuantityDialog(shopItemAdapter.getItemName(position), position);
            }
        });

        reduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double temp_price;
                double temp_quantity;

                double check_quantity = Double.parseDouble(quantity.getText().toString());
                if (check_quantity > 0) {
                    if (unit_price.getText().toString().equals("")) {
                        temp_price = 0;
                    } else {
                        temp_price = Double.parseDouble(unit_price.getText().toString());
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

                if (unit_price.getText().toString().equals("")) {
                    temp_price = 0;
                } else {
                    temp_price = Double.parseDouble(unit_price.getText().toString());
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

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                searchView.setQuery("", false);
                searchView.onActionViewCollapsed();

                db.updateQuantity(category, temp, quantity.getText().toString(), unit.getText().toString());
                shopItemAdapter.notifyItemChanged(position);
                getsum();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void showAlarmDialog(String description) {


        calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentMinutes = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(ItemActivity.this, R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {

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
                    Toast.makeText(ItemActivity.this, getString(R.string.no_alarm_app), Toast.LENGTH_SHORT).show();
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
        final Dialog dialog = new Dialog(ItemActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.update_category_name_layout);

        TextView headingText = dialog.findViewById(R.id.renameHeading);
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
                    dialog.dismiss();
                } else {
                    Items_Check.remove(category);
                    if (!newName.isEmpty()) {
                        if (!Items_Check.contains(newName)) {
                            boolean checkEditData = db.updateCategory(newName, category);
                            firebase.updateCategoryName(newName, category);
                            if (!checkEditData) {
                                Toast.makeText(ItemActivity.this, R.string.rename_fail, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ItemActivity.this, getString(R.string.list_rename_success), Toast.LENGTH_SHORT).show();
                                itemToolbar.setTitle(newName);
                                category = newName;
                                dialog.dismiss();

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
                                Toast.makeText(ItemActivity.this, R.string.rename_fail, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ItemActivity.this, getString(R.string.list_rename_success), Toast.LENGTH_SHORT).show();
                                itemToolbar.setTitle(newName);
                                category = newName;
                                dialog.dismiss();

                            }
                        }
                    } else {
                        Toast.makeText(ItemActivity.this, getString(R.string.please_insert_name), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showShareDialog() {
        StringBuilder result = new StringBuilder();
        Cursor res = db.getItems(category, ItemActivity.this);
        Items_Check.clear();
        Items_Prices_List.clear();
        Items_Quantities_List.clear();
        while (res.moveToNext()) {
            Items_Check.add(res.getString(2).trim());
            Items_Prices_List.add(res.getString(4).trim());
            Items_Quantities_List.add(res.getString(9).trim());
        }
        res.close();


        double total = 0;
        for (int i = 0; i < Items_Check.size(); i++) {
            double priceIndex = Double.parseDouble(Items_Prices_List.get(i));
            double quantityIndex = Double.parseDouble(Items_Quantities_List.get(i));
            double PriceQuantityIndex = priceIndex * quantityIndex;
            total += PriceQuantityIndex;


            if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                result.append(Items_Quantities_List.get(i));
            } else {
                result.append("\u25CF");
            }

            result.append("   " + Items_Check.get(i));

            if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
                result.append("   ").append(PriceQuantityIndex);
            }

            result.append("\n\n");


        }

        if (settings.getIsShareTotalDisabled().equals(UserSettings.NO_SHARE_TOTAL_NOT_DISABLED)) {
            result.append("\n").append(getString(R.string.share_list_total)).append("       ").append(formatNumberV2(total));
        }

        String items = result.toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, category.toUpperCase() + "\n" + " \n" + items + "\n");

        String chooserTitle = getString(R.string.send_message_via);
        Intent chosenIntent = Intent.createChooser(intent, chooserTitle);
        startActivity(chosenIntent);

    }

    public void showCopyDailog(String itemName) {
        final Dialog dialog = new Dialog(ItemActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.movebottomlayout);
        single_selected_item = itemName;

        TextView textView = dialog.findViewById(R.id.item_move_title);
        textView.setText(getString(R.string.copy_to));
        SearchView searchView = dialog.findViewById(R.id.search_bar);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        Button button = dialog.findViewById(R.id.create_new_list);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        displayCategoryData();
        if (categoryList.isEmpty()) {
            searchView.setVisibility(View.GONE);
        }
        shopCopyAdapter.setFilterList(categoryList);
        RecyclerView copyRecyclerView = dialog.findViewById(R.id.moveRecyclerView);
        copyRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        copyRecyclerView.setAdapter(shopCopyAdapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList2(newText);
                return true;
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
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


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void showSortByDialog() {
        final Dialog dialog = new Dialog(ItemActivity.this);
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

    public void showMoveDialog(String itemName) {
        final Dialog dialog = new Dialog(shopItemAdapter.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.movebottomlayout);
        single_selected_item = itemName;

        TextView textView = dialog.findViewById(R.id.item_move_title);
        SearchView searchView = dialog.findViewById(R.id.search_bar);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        Button button = dialog.findViewById(R.id.create_new_list);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        textView.setText(getString(R.string.move_to));


        displayCategoryData();
        if (categoryList.isEmpty()) {
            searchView.setVisibility(View.GONE);
        }
        shopMoveAdapter.setFilterList(categoryList);
        RecyclerView moveRecyclerView = dialog.findViewById(R.id.moveRecyclerView);
        moveRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        moveRecyclerView.setAdapter(shopMoveAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList2(newText);
                return true;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
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


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showCategoryDialog(String checkCopyOrMoved) {
        final Dialog dialog = new Dialog(ItemActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.category_title_set_popup);
        getDateNdTime();


        EditText categoryName = dialog.findViewById(R.id.category_name);
        LinearLayout newCategoryIllustrationLayout = dialog.findViewById(R.id.newCategory_Illustration_Layout);
        TextView emptyText1 = dialog.findViewById(R.id.emptyText1);
        TextView emptyText2 = dialog.findViewById(R.id.emptyText2);
        TextView sugest1 = dialog.findViewById(R.id.Suggest_1);
        TextView sugest2 = dialog.findViewById(R.id.Suggest_2);
        TextView sugest3 = dialog.findViewById(R.id.Suggest_3);
        sugest3.setText(formattedDate);
        TextView sugest4 = dialog.findViewById(R.id.Suggest_4);
        TextView sugest5 = dialog.findViewById(R.id.Suggest_5);
        TextView sugest6 = dialog.findViewById(R.id.Suggest_6);
        TextView sugest7 = dialog.findViewById(R.id.Suggest_7);
        TextView sugest8 = dialog.findViewById(R.id.Suggest_8);
        Button categorySaveBtn = dialog.findViewById(R.id.category_btnSave);


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
                        dialog.dismiss();
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

                        dialog.dismiss();

                    }
                } else {
                    Toast.makeText(ItemActivity.this, getString(R.string.please_insert_name), Toast.LENGTH_SHORT).show();
                }

            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
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

    private void updateView() {
        if (settings.getCustomTheme().equals(UserSettings.LIGHT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }

        if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            Total_Summation_Textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            Items_list_size_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            Total_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            fab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            Total_Summation_Textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            Items_list_size_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            Total_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            fab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            Total_Summation_Textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            Items_list_size_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            Total_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            fab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            currency_textbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        if (settings.getIsItemEyeDisabled().equals(UserSettings.NO_ITEM_EYE_NOT_DISABLED)) {
            eyeView.setImageResource(R.drawable.final_eye);
        } else if (settings.getIsItemEyeDisabled().equals(UserSettings.YES_ITEM_EYE_DISABLED)) {
            eyeView.setImageResource(R.drawable.final_eye_slash);
        }

        currency_textbox.setText(settings.getCurrency());

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

        String is_price_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_PRICE_DISABLED, UserSettings.YES_SHARE_PRICE_DISABLED);
        settings.setIsSharePriceDisabled(is_price_disabled);

        String is_quantity_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_QUANTITY_DISABLED, UserSettings.YES_SHARE_QUANTITY_DISABLED);
        settings.setIsShareQuantityDisabled(is_quantity_disabled);

        String is_total_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_TOTAL_DISABLED, UserSettings.YES_SHARE_TOTAL_DISABLED);
        settings.setIsShareTotalDisabled(is_total_disabled);

        String multiply_disabled = sharedPreferences.getString(UserSettings.IS_MULTIPLY_DISABLED, UserSettings.NO_MULTIPLY_NOT_DISABLED);
        settings.setIsMultiplyDisabled(multiply_disabled);

        String itemEye_disabled = sharedPreferences.getString(UserSettings.IS_ITEM_EYE_DISABLED, UserSettings.NO_ITEM_EYE_NOT_DISABLED);
        settings.setIsItemEyeDisabled(itemEye_disabled);

        String cross = sharedPreferences.getString(UserSettings.IS_CROSS_DISABLED, UserSettings.NO_CROSS_NOT_DISABLED);
        settings.setIsCrossDisabled(cross);

        String currency = sharedPreferences.getString(UserSettings.CURRENCY, UserSettings.CURRENCY_DOLLAR);
        settings.setCurrency(currency);

        String disablePrice = sharedPreferences.getString(UserSettings.IS_PRICE_DISABLED, UserSettings.NO_PRICE_NOT_DISABLED);
        settings.setIsPriceDisabled(disablePrice);

        updateView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    protected void onResume() {

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

        super.onResume();
    }

    private void filterList(String text) {
        ArrayList<String> filterList = new ArrayList<>();

        for (String item : Items) {
            if (item.toLowerCase().contains(text.toLowerCase())) {
                filterList.add(item);
            }
        }
        shopItemAdapter.setFilterList(filterList);
    }

    private void filterList2(String text) {
        ArrayList<String> filterList = new ArrayList<>();

        for (String item : categoryList) {
            if (item.toLowerCase().contains(text.toLowerCase())) {
                filterList.add(item);
            }

        }
        shopCopyAdapter.setFilterList(filterList);
        shopMoveAdapter.setFilterList(filterList);
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

    public ArrayList<String> showCheckAll() {
        ArrayList<String> status = new ArrayList<>();
        for (int i = 0; i < Items.size(); i++) {
            Cursor res = db.getStatus(category, Items.get(i));
            while (res.moveToNext()) {
                status.add(String.valueOf(res.getInt(3)));
            }
            res.close();
        }
        return status;
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
        db.close();
        super.onDestroy();
    }

}