package com.hemerick.buymate;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.AlarmClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hemerick.buymate.Adapter.ShopCategoryAdapter;
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
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;

import io.github.muddz.styleabletoast.StyleableToast;


public class HomeFragment extends Fragment implements ShopCategoryAdapter.OnNoteListener {

    SharedPreferences sharedPreferences;
    SwipeRefreshLayout swipeRefreshLayout;
    Context context = getContext();
    ShopDatabase db;
    ArrayList<String> category_list;
    HashSet<String> suggest_list;
    HashSet<String> suggest_unit_list;
    ArrayList<String> itemCheck;
    ArrayList<String> priceCheck;
    ArrayList<String> final_priceCheck;
    ArrayList<String> quantityCheck;
    ArrayList<String> unitCheck;
    ArrayList<String> selectList;
    Toolbar toolbar;
    RecyclerView recyclerView;
    FloatingActionButton createButton;

    Button empty_create_btn;
    ShopCategoryAdapter adapter;
    String categoryTitle = null;
    String description = null;
    String month;
    String year;
    String day;
    String time;
    String fullTimeWithSeconds;
    String formattedDate;
    SearchView searchView;
    EditText searchEditText;
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout collapsingToolbarLayout;
    TextView toolBarText_2;
    ImageView recyclerLayoutIcon;
    ImageView emptyView;
    ConstraintLayout recyclerLayout;
    LinearLayout emptyTextLayout;
    TextView emptyText1;
    TextView emptyText2;
    TextView emptyText3;
    LinearLayout sortBy;
    TextView sortByText;
    TextView pageDescriptionText;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    View headerView;

    ProgressBar progressBar;

    TextView toolbar_text_2;
    ActionBarDrawerToggle toggle;
    Calendar calendar;
    int currentHour;
    int currentMinutes;
    private UserSettings settings;


    //dialogs
    TimePickerDialog timePickerDialog;
    Dialog rename_dialog, show_sort_dialog, edit_dialog, login_warning_dialog,
            delete_dialog, show_category_dialog, bottom_dialog, menu_delete_dialog, show_share_dialog;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private InterstitialAd mInterstitialAd;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        settings = new UserSettings();


        toolbar = rootView.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }

        swipeRefreshLayout = rootView.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.framelayoutContainer, new HomeFragment());
                transaction.commit();

            }
        });

        setHasOptionsMenu(true);
        drawerLayout = rootView.findViewById(R.id.drawerLayout);
        navigationView = rootView.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (itemId == R.id.nav_premium) {
                    navigationView.getCheckedItem().setChecked(false);
                    item.setChecked(true);
                    Intent intent;
                    if (settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)) {
                        intent = new Intent(getContext(), PaymentSuccessfulActivity.class);
                        startActivity(intent);
                    } else {
                        intent = new Intent(getContext(), PremiumActivity.class);
                        startActivity(intent);
                    }
                } else if (itemId == R.id.nav_reminder) {
                    navigationView.getCheckedItem().setChecked(false);
                    item.setChecked(true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                    }, 500);
                    showAlarmDialog("");

                } else if (itemId == R.id.nav_setting) {
                    navigationView.getCheckedItem().setChecked(false);
                    item.setChecked(true);
                    Intent intent = new Intent(context, SettingsActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.nav_rate_us) {
                    Uri uri = Uri.parse(getString(R.string.app_link) + context.getPackageName());
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent1);
                } else if (itemId == R.id.nav_message) {


                    if (firebaseAuth.getCurrentUser() != null) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


                        String[] app_email = new String[]{getString(R.string.app_email)};

                        String asterics = "*******";
                        String subject = getString(R.string.app_name) + " " + getString(R.string.HomeFragment__feedback);
                        String email = firebaseUser.getEmail();
                        String app_version = getString(R.string.app_version);

                        String deviceInfo = asterics + "\n" +
                                getString(R.string.HomeFragment__accountEmail) + email + "\n" +
                                getString(R.string.HomeFragment__appVersion) + app_version + "\n" +
                                getString(R.string.HomeFragment__device) + Build.DEVICE + "\n" +
                                getString(R.string.HomeFragment__model) + Build.MODEL + "\n" +
                                getString(R.string.HomeFragment__brand) + Build.BRAND + "\n" +
                                getString(R.string.HomeFragment__osVersion) + Build.VERSION.RELEASE + "\n" +
                                getString(R.string.HomeFragment__sdkVersion) + Build.VERSION.SDK_INT + "\n" +
                                asterics + "\n";


                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, app_email);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, deviceInfo);
                        emailIntent.setType("message/rfc822");
                        emailIntent.setPackage("com.google.android.gm");

                        // Check if there's an app to handle this intent
                        if (emailIntent.resolveActivity(getContext().getPackageManager()) != null) {
                            startActivity(emailIntent);
                        } else {
                            // If Gmail app is not installed, handle the intent with the chooser
                            startActivity(Intent.createChooser(emailIntent, getString(R.string.HomeFragment__send_email_via)));
                        }


                    } else {
                        showLogInWarningDialog();
                    }


                } else if (itemId == R.id.nav_about) {
                    Intent intent = new Intent(getContext(), AboutActivity.class);
                    startActivity(intent);
                }

                return true;
            }
        });

        headerView = navigationView.getHeaderView(0);
        toggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.HomeFragment__navigation_drawer_open, R.string.HomeFragment__navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        context = getContext();
        appBarLayout = rootView.findViewById(R.id.app_bar);
        collapsingToolbarLayout = rootView.findViewById(R.id.collapsing_Toolbar);
        toolBarText_2 = rootView.findViewById(R.id.toolbar_text_2);
        toolbar.setNavigationIcon(R.drawable.final_regular_nav_menu_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                boolean checkSelected = adapter.isSelected();
                if (!checkSelected) {
                    if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                        collapsingToolbarLayout.setTitle(getString(R.string.app_name));
                    } else {
                        String first_name;
                        if (settings.getUsername().equals(UserSettings.USER_NAME_NOT_SET)) {
                            first_name = getString(R.string.HomeFragment__defaultName);
                        } else {
                            first_name = settings.getUsername().trim();
                        }
                        collapsingToolbarLayout.setTitle(getString(R.string.HomeFragment__hiText) + " " + first_name + getString(R.string.HomeFragment__exclam));
                    }
                }
            }
        });

        searchView = rootView.findViewById(R.id.search_View);
        searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
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


        emptyView = rootView.findViewById(R.id.empty);
        emptyTextLayout = rootView.findViewById(R.id.emptyTextLayout);
        emptyText1 = rootView.findViewById(R.id.emptyText1);
        emptyText2 = rootView.findViewById(R.id.emptyText2);
        emptyText3 = rootView.findViewById(R.id.emptyText3);
        recyclerLayout = rootView.findViewById(R.id.recyclerLayout);

        toolbar_text_2 = rootView.findViewById(R.id.toolbar_text_2);

        recyclerLayoutIcon = rootView.findViewById(R.id.recyclerViewLayoutIcon);
        pageDescriptionText = rootView.findViewById(R.id.page_description_text);
        sortBy = rootView.findViewById(R.id.sortBy);
        sortBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortByDialog();
            }
        });

        sortByText = rootView.findViewById(R.id.sortBy_text);
        db = new ShopDatabase(context);

        category_list = new ArrayList<>();
        suggest_list = new HashSet<>();
        suggest_unit_list = new HashSet<>();
        itemCheck = new ArrayList<>();
        priceCheck = new ArrayList<>();
        final_priceCheck = new ArrayList<>();
        quantityCheck = new ArrayList<>();
        unitCheck = new ArrayList<>();
        recyclerView = rootView.findViewById(R.id.todo_list);


        adapter = new ShopCategoryAdapter(context, settings, category_list, this);
        recyclerView.setAdapter(adapter);
        updateRecyclerView();

        createButton = rootView.findViewById(R.id.fab);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialog();
            }
        });

        empty_create_btn = rootView.findViewById(R.id.empty_create_btn);
        empty_create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialog();
            }
        });

        recyclerLayoutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isGridEnabled = UserSettings.isGridLockEnabled(getContext());
                UserSettings.setGridLayoutEnabled(getContext(), !isGridEnabled);

                updateRecyclerView();

            }
        });


        displayData();
        loadSharedPreferences();

        if (!settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)) {
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
        }


        return rootView;

    }

    public void displayData() {
        Cursor resCategory = db.getCategory(context);
        if (resCategory.getCount() == 0) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);
            boolean isFirstStart = sharedPreferences.getBoolean("isFirstStart", true);

            recyclerLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            createButton.setVisibility(View.GONE);


            if (!isFirstStart) {
                emptyView.setImageResource(R.drawable.gif_cart_gif);
                ViewGroup.LayoutParams params = emptyView.getLayoutParams();
                params.width = 300;
                params.height = 200;
                emptyView.setLayoutParams(params);
                emptyText1.setText(getString(R.string.HomeFragment__no_list));
            } else {
                emptyView.setImageResource(R.drawable.illustration_empty_list_1);
                String[] suggestion_list = getResources().getStringArray(R.array.item_suggestions);
                String[] unit_list = getResources().getStringArray(R.array.units);
                for (String s : suggestion_list) {
                    db.insertSuggest(s);
                }
                for (String su : unit_list) {
                    db.insertSuggestUnit(su);
                }
            }

            emptyTextLayout.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.VISIBLE);
            searchView.setQueryHint(getString(R.string.HomeFragment__emptySearch));
        } else {
            LinkedHashSet<String> hash = new LinkedHashSet<>();
            while (resCategory.moveToNext()) {
                hash.add(resCategory.getString(1).trim());
            }
            resCategory.close();

            SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);
            String sort = sharedPreferences.getString(UserSettings.CUSTOM_CATEGORY_SORT, UserSettings.DATE_ASCENDING);
            settings.setCustomCategorySort(sort);

            category_list.clear();
            category_list.addAll(hash);

            if (settings.getCustomCategorySort().equals(UserSettings.NAME_ASCENDING)) {
                Collections.sort(category_list);
            } else if (settings.getCustomCategorySort().equals(UserSettings.NAME_DESCENDING)) {
                Collections.sort(category_list, Collections.reverseOrder());
            } else if (settings.getCustomCategorySort().equals(UserSettings.DATE_DESCENDING)) {
                Collections.reverse(category_list);
            } else if (settings.getCustomCategorySort().equals(UserSettings.PRICE_ASCENDING)) {
                ArrayList<Integer> intValuesOfCategoryList = new ArrayList<>();
                for (String item : category_list) {
                    ArrayList<String> intTempCount = new ArrayList<>();
                    Cursor res2 = db.getItems(item, context);
                    while (res2.moveToNext()) {
                        intTempCount.add(res2.getString(2));
                    }
                    res2.close();
                    intValuesOfCategoryList.add(intTempCount.size());
                }

                Comparator<Integer> sizeComparator = new Comparator<Integer>() {
                    @Override
                    public int compare(Integer index1, Integer index2) {
                        return intValuesOfCategoryList.get(index1) - intValuesOfCategoryList.get(index2);
                    }
                };

                ArrayList<Integer> indexes = new ArrayList<>();
                for (int i = 0; i < category_list.size(); i++) {
                    indexes.add(i);
                }

                Collections.sort(indexes, sizeComparator);

                ArrayList<String> sortedCategoryList = new ArrayList<>();
                for (int index : indexes) {
                    sortedCategoryList.add(category_list.get(index));
                }
                category_list.clear();
                category_list.addAll(sortedCategoryList);
                adapter.notifyDataSetChanged();
            } else if (settings.getCustomCategorySort().equals(UserSettings.PRICE_DESCENDING)) {
                ArrayList<Integer> intValuesOfCategoryList = new ArrayList<>();
                for (String item : category_list) {
                    ArrayList<String> intTempCount = new ArrayList<>();
                    Cursor res2 = db.getItems(item, context);
                    while (res2.moveToNext()) {
                        intTempCount.add(res2.getString(2));
                    }
                    res2.close();
                    intValuesOfCategoryList.add(intTempCount.size());
                }

                Comparator<Integer> sizeComparator = new Comparator<Integer>() {
                    @Override
                    public int compare(Integer index1, Integer index2) {
                        return intValuesOfCategoryList.get(index1) - intValuesOfCategoryList.get(index2);
                    }
                };

                ArrayList<Integer> indexes = new ArrayList<>();
                for (int i = 0; i < category_list.size(); i++) {
                    indexes.add(i);
                }

                Collections.sort(indexes, sizeComparator);

                ArrayList<String> sortedCategoryList = new ArrayList<>();
                for (int index : indexes) {
                    sortedCategoryList.add(category_list.get(index));
                }

                category_list.clear();
                category_list.addAll(sortedCategoryList);
                Collections.reverse(category_list);
                adapter.notifyDataSetChanged();

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

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        boolean checkSelected = adapter.isSelected();
        if (checkSelected) {
            selectList = new ArrayList<String>();
            selectList = adapter.getSelectList();
            collapsingToolbarLayout.setTitle(selectList.size() + "/" + category_list.size());
            menu.clear();
            inflater.inflate(R.menu.category_toolbar_hold_menu, menu);
            ArrayList<String> finalSelectList = selectList;
            menu.findItem(R.id.delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {

                    menu_delete_dialog = new Dialog(context);
                    menu_delete_dialog.setContentView(R.layout.custom_delete_dialog);
                    menu_delete_dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
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
                        delete_heading.setText(getString(R.string.HomeFragment__deleteTheseLists));
                    } else {
                        delete_heading.setText(getString(R.string.HomeFragment__deleteThisList));
                    }
                    delete_message.setText(items_selected.toString());

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (int i = 0; i < finalSelectList.size(); i++) {

                                ArrayList<String> total_items = new ArrayList<>();
                                Cursor res = db.getItems(finalSelectList.get(i), context);
                                while (res.moveToNext()) {
                                    total_items.add(res.getString(2));
                                }

                                String old_url = null;
                                for (int j = 0; j < total_items.size(); j++) {

                                    res = db.getPhotourl(finalSelectList.get(i), total_items.get(j));
                                    while (res.moveToNext()) {
                                        old_url = res.getString(12);
                                    }
                                    db.updatePhoto(finalSelectList.get(i), total_items.get(j), " ");


                                    ArrayList<String> total_url = new ArrayList<>();
                                    res = db.getCategory(context);
                                    while (res.moveToNext()) {
                                        total_url.add(res.getString(12));
                                    }
                                    res.close();

                                    File directory = new File(context.getFilesDir(), "Buymate_Images");
                                    if (!total_url.contains(old_url)) {
                                        File imageFileToDelete = new File(directory, old_url);
                                        if (imageFileToDelete.exists()) {
                                            imageFileToDelete.delete();
                                        }
                                    }


                                }

                                category_list.remove(finalSelectList.get(i));
                                db.deleteCategory(finalSelectList.get(i));

                            }
                            adapter.disableSelection();
                            searchEditText.setText("");

                            if (category_list.size() == 0) {
                                adapter.checkEmpty();
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                            menu_delete_dialog.dismiss();
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
            menu.findItem(R.id.mark).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    adapter.setSelectAll();
                    return true;
                }
            });
        } else {
            collapsingToolbarLayout.setTitle(getString(R.string.app_name));
            menu.clear();
            inflater.inflate(R.menu.category_toolbar_menu, menu);
            menu.findItem(R.id.setting).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    Intent intent = new Intent(context, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void showBottomDialogue() {
        bottom_dialog = new Dialog(context);
        bottom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottom_dialog.setContentView(R.layout.shopbottomlayout);

        sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);
        boolean isFirstStart = sharedPreferences.getBoolean("isFirstStart", true);


        TextView addItemTextHeader = bottom_dialog.findViewById(R.id.addItemHeaderText);
        TextView addItemTextHeader2 = bottom_dialog.findViewById(R.id.addItemHeaderText2);


        EditText descPrice = bottom_dialog.findViewById(R.id.desc_price);
        EditText descQuantity = bottom_dialog.findViewById(R.id.desc_quantity);
        LinearLayout addItemIllustrationLayout = bottom_dialog.findViewById(R.id.addItem_Illustration_Layout);
        Button cancelButton = bottom_dialog.findViewById(R.id.CancelButton);
        Button saveButton = bottom_dialog.findViewById(R.id.BtnSave);

        TextInputLayout textInputLayout = bottom_dialog.findViewById(R.id.desc_price_text_input_layout);
        textInputLayout.setPrefixText(settings.getCurrency());


        AutoCompleteTextView descText = bottom_dialog.findViewById(R.id.desc_name);
        AutoCompleteTextView unitText = bottom_dialog.findViewById(R.id.unit_textView);

        if (settings.getIsSuggestionDisabled().equals(UserSettings.YES_SUGGESTION_DISABLED)) {

        } else {
            ArrayList<String> temp_suggestion_list = new ArrayList<>(suggest_list);
            ArrayAdapter<String> suggest_adapter = new ArrayAdapter<>(context, R.layout.unit_drop_down_layout, temp_suggestion_list);
            descText.setAdapter(suggest_adapter);
            ArrayList<String> temp_suggestion_unit_list = new ArrayList<>(suggest_unit_list);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.unit_drop_down_layout, temp_suggestion_unit_list);
            unitText.setAdapter(adapter);
        }


        LinearLayout more_layout = bottom_dialog.findViewById(R.id.more_layout);
        LinearLayout price_quantity_layout = bottom_dialog.findViewById(R.id.price_quantity_layout);
        TextView more_text = bottom_dialog.findViewById(R.id.more_text);
        ImageView more_image = bottom_dialog.findViewById(R.id.more_icon);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            addItemTextHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            addItemTextHeader2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            descText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            descPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            unitText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            descQuantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            more_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            addItemTextHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            addItemTextHeader2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            descText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            descPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            unitText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            descQuantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            more_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            addItemTextHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            addItemTextHeader2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            descText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            descPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            unitText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            descQuantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            more_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        if (isFirstStart) {
            addItemIllustrationLayout.setVisibility(View.VISIBLE);
        } else {
            addItemTextHeader.setVisibility(View.VISIBLE);
            addItemTextHeader.setText(getString(R.string.HomeFragment__addItemToList));
            addItemTextHeader2.setVisibility(View.GONE);
            addItemIllustrationLayout.setVisibility(View.VISIBLE);
        }
        Cursor res = db.getCategory(context);
        itemCheck.clear();
        while (res.moveToNext()) {
            itemCheck.add(res.getString(1).trim().toLowerCase());
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

                String unit;
                if (unitText.getText().toString().isEmpty()) {
                    unit = " ";
                } else {
                    unit = unitText.getText().toString();
                }

                if (!descText.getText().toString().trim().isEmpty()) {
                    description = descText.getText().toString().trim();
                    if (descPrice.getText().toString().trim().isEmpty()) {
                        if (!itemCheck.contains(descText.getText().toString().trim().toLowerCase())) {
                            if (descQuantity.getText().toString().isEmpty()) {
                                double quant = 1;
                                double temp_price = 0;
                                insertItem(categoryTitle, description, 0, temp_price, month, year, day, time, quant, unit);
                                bottom_dialog.dismiss();
                            } else {
                                double quant2 = Long.parseLong(descQuantity.getText().toString().trim());
                                double temp_price = 0;
                                insertItem(categoryTitle, description, 0, temp_price, month, year, day, time, quant2, unit);
                                bottom_dialog.dismiss();
                            }
                        } else {
                            StyleableToast.makeText(context, getString(R.string.HomeFragment__list_exist_already), R.style.custom_toast_2).show();
                        }
                    } else {
                        if (!itemCheck.contains(descText.getText().toString().trim().toLowerCase())) {
                            if (descQuantity.getText().toString().isEmpty()) {
                                double quant = 1;
                                double p = Double.parseDouble(descPrice.getText().toString().trim());
                                insertItem(categoryTitle, description, 0, p, month, year, day, time, quant, unit);
                                bottom_dialog.dismiss();
                            } else {
                                double quant2 = Double.parseDouble(descQuantity.getText().toString().trim());
                                double p2 = Double.parseDouble(descPrice.getText().toString().trim());
                                insertItem(categoryTitle, description, 0, p2, month, year, day, time, quant2, unit);
                                bottom_dialog.dismiss();
                            }
                        } else {
                            StyleableToast.makeText(context, getString(R.string.HomeFragment__list_exist_already), R.style.custom_toast_2).show();
                        }
                    }
                } else {
                    StyleableToast.makeText(context, getString(R.string.HomeFragment__please_insert_name), R.style.custom_toast_2).show();
                }

            }
        });

        bottom_dialog.show();
        bottom_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bottom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bottom_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        bottom_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showCategoryDialog() {

        show_category_dialog = new Dialog(context);
        show_category_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        show_category_dialog.setContentView(R.layout.category_title_set_popup);
        getDateNdTime();


        EditText categoryName = show_category_dialog.findViewById(R.id.category_name);
        LinearLayout newCategoryIllustrationLayout = show_category_dialog.findViewById(R.id.newCategory_Illustration_Layout);
        TextView emptyText1 = show_category_dialog.findViewById(R.id.emptyText1);
        TextView emptyText2 = show_category_dialog.findViewById(R.id.emptyText2);
        TextView suggest_header = show_category_dialog.findViewById(R.id.Suggest_header);
        TextView sugest1 = show_category_dialog.findViewById(R.id.Suggest_1);
        TextView sugest2 = show_category_dialog.findViewById(R.id.Suggest_2);
        TextView sugest3 = show_category_dialog.findViewById(R.id.Suggest_3);
        sugest3.setText(formattedDate);
        TextView sugest4 = show_category_dialog.findViewById(R.id.Suggest_4);
        TextView sugest5 = show_category_dialog.findViewById(R.id.Suggest_5);
        TextView sugest6 = show_category_dialog.findViewById(R.id.Suggest_6);
        TextView sugest7 = show_category_dialog.findViewById(R.id.Suggest_7);
        TextView sugest8 = show_category_dialog.findViewById(R.id.Suggest_8);
        Button categorySaveBtn = show_category_dialog.findViewById(R.id.category_btnSave);


        newCategoryIllustrationLayout.setVisibility(View.VISIBLE);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            categoryName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            suggest_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
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
            suggest_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
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
            suggest_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
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
                String newText = sugest1.getText().toString() + " ";
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });

        sugest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newText = sugest2.getText().toString() + " ";
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newText = sugest3.getText().toString() + " ";
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newText = sugest4.getText().toString() + " ";
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newText = sugest5.getText().toString() + " ";
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newText = sugest6.getText().toString() + " ";
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newText = sugest7.getText().toString() + " ";
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });
        sugest8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newText = sugest8.getText().toString() + " ";
                categoryName.setText(newText);
                categoryName.setSelection(categoryName.getText().length());
            }
        });


        Cursor res = db.getCategory(context);
        itemCheck.clear();
        while (res.moveToNext()) {
            itemCheck.add(res.getString(1).trim());
        }
        res.close();

        categorySaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!categoryName.getText().toString().trim().isEmpty()) {
                    categoryTitle = categoryName.getText().toString().trim();
                    if (!itemCheck.contains(categoryTitle)) {
                        getDateNdTime();
                        showBottomDialogue();
                        show_category_dialog.dismiss();
                    } else {
                        int count = 1;
                        String newItem = categoryName.getText().toString().trim() + " (" + count + ")";
                        while (itemCheck.contains(newItem)) {
                            count++;
                            newItem = categoryName.getText().toString().trim() + " (" + count + ")";
                        }
                        categoryTitle = newItem;
                        getDateNdTime();
                        show_category_dialog.dismiss();
                        showBottomDialogue();


                    }
                } else {
                    StyleableToast.makeText(context, getString(R.string.HomeFragment__please_insert_name), R.style.custom_toast_2).show();
                }

            }
        });

        show_category_dialog.show();
        show_category_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        show_category_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        show_category_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        show_category_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showDeleteDialog(int position) {
        String temp = adapter.getItemName(position);
        delete_dialog = new Dialog(context);
        delete_dialog.setContentView(R.layout.custom_delete_dialog);
        delete_dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
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

        String temp_text = getString(R.string.HomeFragment__delete) + " " + temp;
        delete_heading.setText(temp_text);

        delete_message.setText(getString(R.string.HomeFragment__deleteWarning));

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = adapter.getItemName(position);
                db = new ShopDatabase(context);


                ArrayList<String> total_items = new ArrayList<>();
                Cursor res = db.getItems(temp, context);
                while (res.moveToNext()) {
                    total_items.add(res.getString(2));
                }

                String old_url = null;
                for (int j = 0; j < total_items.size(); j++) {

                    res = db.getPhotourl(temp, total_items.get(j));
                    while (res.moveToNext()) {
                        old_url = res.getString(12);
                    }
                    db.updatePhoto(temp, total_items.get(j), " ");


                    ArrayList<String> total_url = new ArrayList<>();
                    res = db.getCategory(context);
                    while (res.moveToNext()) {
                        total_url.add(res.getString(12));
                    }
                    res.close();


                    File directory = new File(context.getFilesDir(), "Buymate_Images");
                    if (!total_url.contains(old_url)) {
                        File imageFileToDelete = new File(directory, old_url);
                        if (imageFileToDelete.exists()) {
                            imageFileToDelete.delete();
                        }
                    }


                }

                db.deleteCategory(temp);
                category_list.remove(temp);
                if (category_list.isEmpty()) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.framelayoutContainer, new HomeFragment());
                    fragmentTransaction.commit();
                } else {
                    delete_dialog.dismiss();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.framelayoutContainer, new HomeFragment());
                    fragmentTransaction.commit();
                }


            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.notifyItemChanged(position);
                delete_dialog.dismiss();
            }
        });

        delete_dialog.show();
    }

    public void showLogInWarningDialog() {
        login_warning_dialog = new Dialog(context);
        login_warning_dialog.setContentView(R.layout.custom_logout_warning_dialog);
        login_warning_dialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        login_warning_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = login_warning_dialog.findViewById(R.id.header);
        TextView alertText = login_warning_dialog.findViewById(R.id.alert_text);
        Button backup = login_warning_dialog.findViewById(R.id.backup);
        TextView okBtn = login_warning_dialog.findViewById(R.id.okBtn);

        alertText.setText(getString(R.string.HomeFragment__loginPrompt));
        backup.setText(getString(R.string.HomeFragment__login));


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            alertText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            backup.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            alertText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            backup.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            alertText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            backup.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_warning_dialog.dismiss();
                Intent intent = new Intent(getContext(), LogInActivity.class);
                startActivity(intent);
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_warning_dialog.dismiss();
            }
        });

        login_warning_dialog.show();
    }

    private void showEditDialog(String prevTask, int position) {
        edit_dialog = new Dialog(context);
        edit_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        edit_dialog.setContentView(R.layout.edit_category_layout);
        TextView title = edit_dialog.findViewById(R.id.cate_edit_title);
        LinearLayout renameLayout = edit_dialog.findViewById(R.id.rename);
        TextView renameText = edit_dialog.findViewById(R.id.renameText);
        LinearLayout duplicateLayout = edit_dialog.findViewById(R.id.duplicate);
        TextView duplicateText = edit_dialog.findViewById(R.id.duplicateText);
        LinearLayout shareLayout = edit_dialog.findViewById(R.id.share);
        TextView shareText = edit_dialog.findViewById(R.id.shareText);
        LinearLayout reminderLayout = edit_dialog.findViewById(R.id.reminder);
        TextView reminderText = edit_dialog.findViewById(R.id.alarmText);
        LinearLayout deleteLayout = edit_dialog.findViewById(R.id.delete);
        TextView deleteText = edit_dialog.findViewById(R.id.deleteText);

        title.setText(prevTask);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            renameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            duplicateText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            shareText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            reminderText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            deleteText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            renameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            duplicateText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            shareText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            reminderText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            deleteText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            renameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            duplicateText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            shareText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            reminderText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            deleteText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        renameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialog(prevTask, position);
                edit_dialog.dismiss();
            }
        });

        duplicateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                duplicateList(prevTask);
                edit_dialog.dismiss();
            }
        });

        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShareOptionDialog(prevTask);
                edit_dialog.dismiss();
            }
        });

        reminderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_dialog.dismiss();
                showAlarmDialog(prevTask);

            }
        });

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(position);
                edit_dialog.dismiss();
            }
        });


        edit_dialog.show();
        edit_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                adapter.notifyItemChanged(position);
            }
        });
        edit_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        edit_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        edit_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        edit_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void duplicateList(String listName) {
        ArrayList<String> descriptions = new ArrayList<>();
        ArrayList<Integer> status = new ArrayList<>();
        ArrayList<String> price = new ArrayList<>();
        ArrayList<String> quantity = new ArrayList<>();
        ArrayList<Integer> favourites = new ArrayList<>();
        ArrayList<String> unit = new ArrayList<>();
        ArrayList<String> photourl = new ArrayList<>();

        Cursor res = db.getItems(listName, context);
        while (res.moveToNext()) {
            descriptions.add(res.getString(2));
            status.add(res.getInt(3));
            price.add(res.getString(4));
            quantity.add(res.getString(9));
            favourites.add(res.getInt(10));
            unit.add(res.getString(11));
            photourl.add(res.getString(12));
        }

        ArrayList<String> all_list = new ArrayList<>();

        res = db.getCategory(context);
        while (res.moveToNext()) {
            all_list.add(res.getString(1).trim());
        }
        res.close();


        if (all_list.contains(listName)) {
            int count = 1;
            String newItem = listName + " (" + count + ")";
            while (all_list.contains(newItem)) {
                count++;
                newItem = listName + " (" + count + ")";
            }
            listName = newItem;
        }
        getDateNdTime();
        for (int i = 0; i < descriptions.size(); i++) {
            insertItemV2(listName, descriptions.get(i), status.get(i), Double.parseDouble(price.get(i)), month, year, day, time, Double.parseDouble(quantity.get(i)), unit.get(i));
            db.updateFavourites(listName, descriptions.get(i), favourites.get(i));
            db.updatePhoto(listName, descriptions.get(i), photourl.get(i));
        }
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.framelayoutContainer, new HomeFragment());
        fragmentTransaction.commit();

    }


    private void showSortByDialog() {

        show_sort_dialog = new Dialog(context);
        show_sort_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        show_sort_dialog.setContentView(R.layout.sort_category_layout);

        TextView header = show_sort_dialog.findViewById(R.id.sortBy_title);
        LinearLayout nameAscend = show_sort_dialog.findViewById(R.id.name_ascend);
        CheckBox checkNameAscend = show_sort_dialog.findViewById(R.id.check_name_ascend);
        TextView nameTextAscend = show_sort_dialog.findViewById(R.id.sort_nameText_ascend);
        checkNameAscend.setClickable(false);
        LinearLayout nameDescend = show_sort_dialog.findViewById(R.id.name_descend);
        CheckBox checkNameDescend = show_sort_dialog.findViewById(R.id.check_name_descend);
        TextView nameTextDescend = show_sort_dialog.findViewById(R.id.sort_nameText_descend);
        checkNameDescend.setClickable(false);
        LinearLayout dateAscend = show_sort_dialog.findViewById(R.id.date_ascend);
        CheckBox checkDateAscend = show_sort_dialog.findViewById(R.id.check_date_ascend);
        TextView dateTextAscend = show_sort_dialog.findViewById(R.id.sort_dateText_ascend);
        checkDateAscend.setClickable(false);
        LinearLayout dateDescend = show_sort_dialog.findViewById(R.id.date_descend);
        CheckBox checkDateDescend = show_sort_dialog.findViewById(R.id.check_date_descend);
        TextView dateTextDescend = show_sort_dialog.findViewById(R.id.sort_dateText_descend);
        checkDateDescend.setClickable(false);
        LinearLayout priceAscend = show_sort_dialog.findViewById(R.id.price_ascend);
        CheckBox checkPriceAscend = show_sort_dialog.findViewById(R.id.check_price_ascend);
        checkPriceAscend.setClickable(false);
        TextView priceTextAscend = show_sort_dialog.findViewById(R.id.price_text_ascend);
        priceTextAscend.setText(getString(R.string.HomeFragment__sortPriceText));
        LinearLayout priceDescend = show_sort_dialog.findViewById(R.id.price_descend);
        CheckBox checkPriceDescend = show_sort_dialog.findViewById(R.id.check_price_descend);
        checkPriceDescend.setClickable(false);
        TextView priceTextDescend = show_sort_dialog.findViewById(R.id.price_text_descend);
        priceTextDescend.setText(getString(R.string.HomeFragment__sortPriceText));
        LinearLayout quantityAscend = show_sort_dialog.findViewById(R.id.quantity_ascend);
        quantityAscend.setVisibility(View.GONE);
        LinearLayout quantityDescend = show_sort_dialog.findViewById(R.id.quantity_descend);
        quantityDescend.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);
        String sort = sharedPreferences.getString(UserSettings.CUSTOM_CATEGORY_SORT, UserSettings.DATE_ASCENDING);
        settings.setCustomCategorySort(sort);

        if (settings.getCustomCategorySort().equals(UserSettings.NAME_ASCENDING)) {
            checkNameAscend.setChecked(true);
        } else if (settings.getCustomCategorySort().equals(UserSettings.NAME_DESCENDING)) {
            checkNameDescend.setChecked(true);
        } else if (settings.getCustomCategorySort().equals(UserSettings.DATE_ASCENDING)) {
            checkDateAscend.setChecked(true);
        } else if (settings.getCustomCategorySort().equals(UserSettings.DATE_DESCENDING)) {
            checkDateDescend.setChecked(true);
        } else if (settings.getCustomCategorySort().equals(UserSettings.PRICE_ASCENDING)) {
            checkPriceAscend.setChecked(true);
        } else if (settings.getCustomCategorySort().equals(UserSettings.PRICE_DESCENDING)) {
            checkPriceDescend.setChecked(true);
        }


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            nameTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            nameTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            dateTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            dateTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            priceTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            priceTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            nameTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            nameTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            dateTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            dateTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            priceTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            priceTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            nameTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            nameTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            dateTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            dateTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            priceTextAscend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            priceTextDescend.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        nameAscend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkNameAscend.isChecked()) {
                    checkNameAscend.setChecked(true);
                    checkNameDescend.setChecked(false);
                    checkDateAscend.setChecked(false);
                    checkDateDescend.setChecked(false);

                    settings.setCustomCategorySort(UserSettings.NAME_ASCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        show_sort_dialog.dismiss();
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
                    checkNameDescend.setChecked(true);
                    settings.setCustomCategorySort(UserSettings.NAME_DESCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        show_sort_dialog.dismiss();
                    }
                }, 300);
            }
        });

        dateAscend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkDateAscend.isChecked()) {
                    checkNameAscend.setChecked(false);
                    checkNameDescend.setChecked(false);
                    checkDateDescend.setChecked(false);
                    checkDateAscend.setChecked(true);
                    settings.setCustomCategorySort(UserSettings.DATE_ASCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        show_sort_dialog.dismiss();
                    }
                }, 300);
            }
        });

        dateDescend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkDateDescend.isChecked()) {
                    checkDateAscend.setChecked(false);
                    checkNameAscend.setChecked(false);
                    checkNameDescend.setChecked(false);
                    checkDateDescend.setChecked(true);
                    settings.setCustomCategorySort(UserSettings.DATE_DESCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        show_sort_dialog.dismiss();
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
                    checkPriceAscend.setChecked(true);
                    settings.setCustomCategorySort(UserSettings.PRICE_ASCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        show_sort_dialog.dismiss();
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
                    checkPriceDescend.setChecked(true);
                    settings.setCustomCategorySort(UserSettings.PRICE_DESCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
                editor.apply();
                displayData();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        show_sort_dialog.dismiss();
                    }
                }, 300);

            }
        });


        show_sort_dialog.show();
        show_sort_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        show_sort_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        show_sort_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        show_sort_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showShareTextDialog(String prevTask) {
        StringBuilder result = new StringBuilder();


        Cursor res = db.getItems(prevTask, context);
        itemCheck.clear();
        priceCheck.clear();
        quantityCheck.clear();
        unitCheck.clear();
        while (res.moveToNext()) {
            itemCheck.add(res.getString(2).trim());
            priceCheck.add(res.getString(4).trim());
            quantityCheck.add(res.getString(9).trim());
            unitCheck.add(" " + res.getString(11));

        }
        res.close();


        double total = 0;
        double PriceQuantityIndex;
        for (int i = 0; i < itemCheck.size(); i++) {
            double priceIndex = Double.parseDouble(priceCheck.get(i));
            double quantityIndex = Double.parseDouble(quantityCheck.get(i));

            if (settings.getIsMultiplyDisabled().equals(UserSettings.NO_MULTIPLY_NOT_DISABLED)) {
                PriceQuantityIndex = priceIndex * quantityIndex;
            } else {
                PriceQuantityIndex = priceIndex;
            }

            total += PriceQuantityIndex;


            result.append("\u25CF");
            result.append(" " + itemCheck.get(i));

            if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                if (Integer.parseInt(quantityCheck.get(i)) != 1) {
                    if (!unitCheck.get(i).trim().isEmpty()) {
                        result.append(" " + "[" + quantityCheck.get(i) + " " + unitCheck.get(i).trim() + "]");
                    } else {
                        result.append(" " + "[" + quantityCheck.get(i) + unitCheck.get(i).trim() + "]");
                    }
                } else {
                    if (!unitCheck.get(i).trim().isEmpty()) {
                        result.append(" " + "[" + quantityCheck.get(i) + " " + unitCheck.get(i).trim() + "]");
                    }
                }
            }

            if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
                result.append("   ").append(formatNumber(PriceQuantityIndex));
            }

            result.append("\n");
        }

        if (settings.getIsShareTotalDisabled().equals(UserSettings.NO_SHARE_TOTAL_NOT_DISABLED)) {
            result.append("\n").append(getString(R.string.HomeFragment__Total)).append("       ").append(formatNumber(total));

        }

        String appLink = getString(R.string.app_link) + getContext().getPackageName();

        result.append("\n\n\n").append(context.getString(R.string.HomeFragment__shareBtmText1));
        result.append("\n\n").append(context.getString(R.string.HomeFragment__shareBtmText2));
        result.append("\n").append(appLink);


        String items = result.toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, prevTask.toUpperCase() + "\n" + " \n" + items + "\n");

        String chooserTitle = getString(R.string.HomeFragment__send_message_via);
        Intent chosenIntent = Intent.createChooser(intent, chooserTitle);
        startActivity(chosenIntent);


    }

    public void showDownloadPdfDialog(String prevTask) {


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Cursor res = db.getItems(prevTask, context);
                itemCheck.clear();
                priceCheck.clear();
                final_priceCheck.clear();
                quantityCheck.clear();
                unitCheck.clear();
                while (res.moveToNext()) {
                    itemCheck.add(res.getString(2).trim());
                    priceCheck.add(res.getString(4).trim());
                    quantityCheck.add(res.getString(9).trim());
                    unitCheck.add(" " + res.getString(11));

                }
                res.close();


                double total = 0;
                double PriceQuantityIndex;
                for (int i = 0; i < itemCheck.size(); i++) {
                    double priceIndex = Double.parseDouble(priceCheck.get(i));
                    double quantityIndex = Double.parseDouble(quantityCheck.get(i));

                    if (settings.getIsMultiplyDisabled().equals(UserSettings.YES_MULTIPLY_DISABLED)) {
                        PriceQuantityIndex = priceIndex;
                    } else {
                        PriceQuantityIndex = priceIndex * quantityIndex;
                    }
                    final_priceCheck.add(String.valueOf(PriceQuantityIndex));
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


                    table.addCell(new Cell().add(new Paragraph(getString(R.string.HomeFragment__no))).setTextAlignment(TextAlignment.LEFT).setBold().setPaddingLeft(8f).setCharacterSpacing(1.5f).setFontColor(itextBlack));
                    table.addCell(new Cell().add(new Paragraph(getString(R.string.HomeFragment__item))).setTextAlignment(TextAlignment.LEFT).setBold().setPaddingLeft(8f).setCharacterSpacing(1.5f).setFontColor(itextBlack));


                    if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                        table.addCell(new Cell().add(new Paragraph(getString(R.string.HomeFragment__quantity))).setTextAlignment(TextAlignment.CENTER).setBold().setCharacterSpacing(1.5f).setFontColor(itextBlack));
                        table.addCell(new Cell().add(new Paragraph(getString(R.string.HomeFragment__unit))).setTextAlignment(TextAlignment.CENTER).setBold().setCharacterSpacing(1.5f).setFontColor(itextBlack));
                    }

                    if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
                        table.addCell(new Cell().add(new Paragraph(getString(R.string.HomeFragment__price) + "(" + currency + ")")).setTextAlignment(TextAlignment.CENTER).setBold().setCharacterSpacing(1.5f).setFontColor(itextBlack).setFont(font));
                    }


                    int num = 1;
                    for (int p = 0; p < itemCheck.size(); p++) {
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(num))).setTextAlignment(TextAlignment.LEFT).setPaddingLeft(8f));
                        table.addCell(new Cell().add(new Paragraph(itemCheck.get(p))).setTextAlignment(TextAlignment.LEFT).setPaddingLeft(8f));
                        if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                            table.addCell(new Cell().add(new Paragraph(quantityCheck.get(p))).setTextAlignment(TextAlignment.CENTER));
                            table.addCell(new Cell().add(new Paragraph(unitCheck.get(p))).setTextAlignment(TextAlignment.CENTER));
                        }
                        if (settings.getIsSharePriceDisabled().equals(UserSettings.NO_SHARE_PRICE_NOT_DISABLED)) {
                            table.addCell(new Cell().add(new Paragraph(final_priceCheck.get(p))).setTextAlignment(TextAlignment.CENTER));
                        }
                        num = num + 1;
                    }


                    if (settings.getIsShareTotalDisabled().equals(UserSettings.NO_SHARE_TOTAL_NOT_DISABLED)) {


                        if (count == 2) {

                            table.addCell(new Cell(1, 1).add(new Paragraph(getString(R.string.HomeFragment__Total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));
                            table.addCell(new Cell(1, 1).add(new Paragraph(currency + " " + formatNumberV3(total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));

                        } else if (count == 3) {
                            table.addCell(new Cell(1, 1).add(new Paragraph(getString(R.string.HomeFragment__Total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));
                            table.addCell(new Cell(1, 2).add(new Paragraph(currency + " " + formatNumberV3(total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));


                        } else if (count == 5) {
                            table.addCell(new Cell(1, 2).add(new Paragraph(getString(R.string.HomeFragment__Total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));
                            table.addCell(new Cell(1, 3).add(new Paragraph(currency + " " + formatNumberV3(total))).setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold().setCharacterSpacing(2f).setFontColor(itextWhite).setBackgroundColor(itextBlue).setFont(font).setPadding(8f));

                        }
                    }

                    document.add(table);


                    Paragraph contact = new Paragraph(getString(R.string.HomeFragment__contactUs))
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

                    Paragraph close_text = new Paragraph(getString(R.string.HomeFragment__shareBtmText1))
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
                    StyleableToast.makeText(context, context.getString(R.string.HomeFragment__pdfDownloadedTo) + directory, R.style.custom_toast_2).show();

                    if (!settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)) {
                        if (mInterstitialAd != null) {
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


    public void showRenameDialog(String prevName, int position) {
        rename_dialog = new Dialog(context);
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

        description.setText(prevName);

        Cursor res = db.getCategory(context);
        itemCheck.clear();
        while (res.moveToNext()) {
            itemCheck.add(res.getString(1).trim());
        }
        res.close();


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = description.getText().toString().trim();
                if (newName.trim().equals(prevName.trim())) {
                    rename_dialog.dismiss();
                } else {
                    itemCheck.remove(prevName);
                    if (!newName.isEmpty()) {
                        if (!itemCheck.contains(newName)) {
                            boolean checkEditData = db.updateCategory(newName, prevName);
                            if (!checkEditData) {
                            } else {
                                rename_dialog.dismiss();
                                adapter.refreshUpdate(newName, position);
                                adapter.notifyItemChanged(position);
                            }
                        } else {
                            int count = 1;
                            String newItem = newName + " (" + count + ")";
                            while (itemCheck.contains(newItem)) {
                                count++;
                                newItem = newName + " (" + count + ")";
                            }
                            boolean checkEditData = db.updateCategory(newItem, prevName);
                            if (!checkEditData) {
                            } else {
                                rename_dialog.dismiss();
                                adapter.refreshUpdate(newItem, position);
                                adapter.notifyItemChanged(position);
                            }
                        }
                    } else {
                        StyleableToast.makeText(context, getString(R.string.HomeFragment__please_insert_name), R.style.custom_toast_2).show();
                    }

                }
            }
        });

        rename_dialog.show();
        rename_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                adapter.notifyItemChanged(position);
            }
        });
        rename_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rename_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        rename_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        rename_dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showAlarmDialog(String description) {


        calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentMinutes = calendar.get(Calendar.MINUTE);


        timePickerDialog = new TimePickerDialog(getContext(), R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                currentHour = hourOfDay;
                currentMinutes = minute;

                Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                intent.putExtra(AlarmClock.EXTRA_HOUR, currentHour);
                intent.putExtra(AlarmClock.EXTRA_MINUTES, currentMinutes);
                intent.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.app_name) + ": " + description);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);

                } else {
                    StyleableToast.makeText(context, getString(R.string.HomeFragment__no_alarm_app), R.style.custom_toast).show();
                }

            }
        }, currentHour, currentMinutes, false);
        WindowManager.LayoutParams params = timePickerDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;


        timePickerDialog.getWindow().setAttributes(params);
        timePickerDialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_time_picker));
        timePickerDialog.show();

        timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                navigationView.getMenu().getItem(0).setChecked(true);
            }
        });
    }


    public void insertItem(String category, String description, int status, double price, String month, String year, String day, String time, double quantity, String unit) {
        if (!suggest_list.contains(description.trim())) {
            db.insertSuggest(description);
        }

        if (!suggest_unit_list.contains(unit.trim())) {
            db.insertSuggestUnit(unit);
        }
        String finalPrice = formatNumberV2(price);
        String finalQuantity = formatNumberV2(quantity);
        db.insertItem(category, description, status, finalPrice, month, year, day, time, finalQuantity, unit);

        Intent intent = new Intent(context, ItemActivity.class);
        intent.putExtra("ITEM", category);
        startActivity(intent);

    }

    public void insertItemV2(String category, String description, int status, double price, String month, String year, String day, String time, double quantity, String unit) {
        String finalPrice = formatNumberV2(price);
        String finalQuantity = formatNumberV2(quantity);
        db.insertItem(category, description, status, finalPrice, month, year, day, time, finalQuantity, unit);
    }

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

    public void getDateNdTime() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();


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

        time = timeFormat.format(date);
        month = currentMonth;
        year = currentYear;
        day = currentDay + " " + currentDayNumber;
        fullTimeWithSeconds = seconds.format(date);
        formattedDate = currentDayNumber + "-" + currentMonthNumber + "-" + currentYear;
    }

    private void filterList(String text) {
        if (!category_list.isEmpty()) {
            ArrayList<String> filterList = new ArrayList<>();

            for (String item : category_list) {
                if (item.toLowerCase().contains(text.toLowerCase())) {
                    filterList.add(item);
                }
            }
            adapter.setFilterList(filterList);

            emptyView.setVisibility(View.GONE);
            emptyTextLayout.setVisibility(View.GONE);
            emptyText1.setVisibility(View.GONE);
            emptyText2.setVisibility(View.GONE);
            emptyText3.setVisibility(View.GONE);
            empty_create_btn.setVisibility(View.GONE);

            if (filterList.isEmpty()) {
                emptyView.setImageResource(R.drawable.illustration_no_search_data);
                ViewGroup.LayoutParams params = emptyView.getLayoutParams();
                params.width = 200;
                params.height = 200;
                emptyView.setLayoutParams(params);
                emptyView.setVisibility(View.VISIBLE);
                emptyTextLayout.setVisibility(View.VISIBLE);
                emptyText1.setVisibility(View.VISIBLE);
                emptyText2.setVisibility(View.GONE);
                emptyText3.setVisibility(View.GONE);
                empty_create_btn.setVisibility(View.GONE);
                emptyText1.setText(getString(R.string.HomeFragment__noListFound));

            }
        }


    }

    private void showShareOptionDialog(String temp_item) {

        show_share_dialog = new Dialog(context);
        show_share_dialog.setContentView(R.layout.custom_share_dialog);
        show_share_dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        show_share_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        LinearLayout shareAsTextLayout = show_share_dialog.findViewById(R.id.textLayout);
        LinearLayout downaloadPDFLayout = show_share_dialog.findViewById(R.id.pdfLayout);
        progressBar = show_share_dialog.findViewById(R.id.progress_bar);

        shareAsTextLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShareTextDialog(temp_item);
            }
        });

        downaloadPDFLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                showDownloadPdfDialog(temp_item);
            }
        });

        TextView title = show_share_dialog.findViewById(R.id.title);
        TextView title_2 = show_share_dialog.findViewById(R.id.title_2);
        String temp_text = "(" + temp_item + ")";
        title_2.setText(temp_text);


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

    private void updateRecyclerView() {

        boolean isGridEnabled = UserSettings.isGridLockEnabled(getContext());
        if (isGridEnabled) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onNoteClick(int position) {
        String temp = adapter.getItemName(position);
        Intent intent = new Intent(context, ItemActivity.class);
        intent.putExtra("ITEM", temp);
        intent.putExtra("ACTIVITYINDEX", 1);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public void onOptionClick(int position) {
        String temp = category_list.get(position);
        showEditDialog(temp, position);
    }

    @Override
    public void onResume() {
        super.onResume();
        MenuItem settingNav = navigationView.getMenu().findItem(R.id.nav_setting);
        MenuItem HomeNav = navigationView.getMenu().findItem(R.id.nav_home);
        settingNav.setChecked(false);
        HomeNav.setChecked(true);
        if (drawerLayout.isOpen()) {
            drawerLayout.close();
        }
        displayData();
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onStart() {
        super.onStart();
        MenuItem settingNav = navigationView.getMenu().findItem(R.id.nav_setting);
        MenuItem HomeNav = navigationView.getMenu().findItem(R.id.nav_home);
        settingNav.setChecked(false);
        HomeNav.setChecked(true);
        if (drawerLayout.isOpen()) {
            drawerLayout.close();
        }
        displayData();
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onDestroyView() {
        db.close();
        super.onDestroyView();
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


    private void updateView() {


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            pageDescriptionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));
            navigationView.setItemTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Small);
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyText3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            empty_create_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sortByText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));
            toolBarText_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            pageDescriptionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            navigationView.setItemTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Menu);
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyText3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            empty_create_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sortByText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            toolBarText_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            pageDescriptionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            navigationView.setItemTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Large);
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyText3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            empty_create_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sortByText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            toolBarText_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));


        }


    }

    private void loadSharedPreferences() {

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);


        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        String is_price_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_PRICE_DISABLED, UserSettings.NO_SHARE_PRICE_NOT_DISABLED);
        settings.setIsSharePriceDisabled(is_price_disabled);

        String is_quantity_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_QUANTITY_DISABLED, UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED);
        settings.setIsShareQuantityDisabled(is_quantity_disabled);

        String is_total_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_TOTAL_DISABLED, UserSettings.NO_SHARE_TOTAL_NOT_DISABLED);
        settings.setIsShareTotalDisabled(is_total_disabled);

        String currency = sharedPreferences.getString(UserSettings.CURRENCY, UserSettings.CURRENCY_DOLLAR);
        settings.setCurrency(currency);

        String firstname = sharedPreferences.getString(UserSettings.USER_NAME, UserSettings.USER_NAME_NOT_SET);
        settings.setUsername(firstname);


        String multiply_disabled = sharedPreferences.getString(UserSettings.IS_MULTIPLY_DISABLED, UserSettings.YES_MULTIPLY_DISABLED);
        settings.setIsMultiplyDisabled(multiply_disabled);

        String lifetimePurchased = sharedPreferences.getString(UserSettings.IS_LIFETIME_PURCHASED, UserSettings.NO_LIFETIME_NOT_SUBSCRIBED);
        settings.setIsLifetimePurchased(lifetimePurchased);

        String suggestion = sharedPreferences.getString(UserSettings.IS_SUGGESTION_DISABLED, UserSettings.NO_SUGGESTION_NOT_DISABLED);
        settings.setIsSuggestionDisabled(suggestion);

        updateView();
    }

    @Override
    public void onPause() {

        super.onPause();

        if (rename_dialog != null && rename_dialog.isShowing()) {
            rename_dialog.dismiss();
        }
        if (show_sort_dialog != null && show_sort_dialog.isShowing()) {
            show_sort_dialog.dismiss();
        }
        if (edit_dialog != null && edit_dialog.isShowing()) {
            edit_dialog.dismiss();
        }
        if (login_warning_dialog != null && login_warning_dialog.isShowing()) {
            login_warning_dialog.dismiss();
        }
        if (delete_dialog != null && delete_dialog.isShowing()) {
            delete_dialog.dismiss();
        }
        if (show_category_dialog != null && show_category_dialog.isShowing()) {
            show_category_dialog.dismiss();
        }
        if (bottom_dialog != null && bottom_dialog.isShowing()) {
            bottom_dialog.dismiss();
        }
        if (menu_delete_dialog != null && menu_delete_dialog.isShowing()) {
            menu_delete_dialog.dismiss();
        }
        if (show_share_dialog != null && show_share_dialog.isShowing()) {
            show_share_dialog.dismiss();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }
}