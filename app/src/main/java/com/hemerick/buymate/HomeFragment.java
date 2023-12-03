package com.hemerick.buymate;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hemerick.buymate.Adapter.ShopCategoryAdapter;
import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;

import io.github.muddz.styleabletoast.StyleableToast;


public class HomeFragment extends Fragment implements ShopCategoryAdapter.OnNoteListener {
    Context context = getContext();
    ShopDatabase db;
    ArrayList<String> category_list;
    ArrayList<String> itemCheck;
    ArrayList<String> priceCheck;
    ArrayList<String> quantityCheck;
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
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    View headerView;
    TextView headerTextView1;
    TextView headerTextView2;
    TextView userName;
    TextView userEmail;

    TextView toolbar_text_2;
    ActionBarDrawerToggle toggle;
    Calendar calendar;
    int currentHour;
    int currentMinutes;
    private UserSettings settings;

    public static String formatNumberV2(double number) {
        if (number == (long) number) {
            return String.format("%.0f", number);
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            return decimalFormat.format(number);
        }
    }

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


        setHasOptionsMenu(true);
        drawerLayout = rootView.findViewById(R.id.drawerLayout);
        navigationView = rootView.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (itemId == R.id.nav_premium) {
                    StyleableToast.makeText(context, "Premium Clicked", R.style.custom_toast).show();
                } else if (itemId == R.id.nav_reminder) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                    }, 300);
                    showAlarmDialog("");
                } else if (itemId == R.id.nav_setting) {
                    navigationView.getCheckedItem().setChecked(false);
                    item.setChecked(true);
                    Intent intent = new Intent(context, SettingsActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.nav_rate_us) {
                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName());
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent1);
                } else if (itemId == R.id.nav_message) {
                    StyleableToast.makeText(context, "Message Clicked", R.style.custom_toast).show();
                }

                return true;
            }
        });

        headerView = navigationView.getHeaderView(0);
        toggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        context = getContext();
        appBarLayout = rootView.findViewById(R.id.app_bar);
        collapsingToolbarLayout = rootView.findViewById(R.id.collapsing_Toolbar);
        toolBarText_2 = rootView.findViewById(R.id.toolbar_text_2);
        toolbar.setNavigationIcon(R.drawable.final_nav_menu_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.clearFocus();
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
                            first_name = "User";
                        } else {
                            first_name = settings.getUsername().trim();
                        }
                        collapsingToolbarLayout.setTitle("Hi, " + first_name + "!");
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
        sortByText = rootView.findViewById(R.id.sortByText);
        sortBy = rootView.findViewById(R.id.sortBy);
        sortBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortByDialog();
            }
        });
        db = new ShopDatabase(context);

        category_list = new ArrayList<>();
        itemCheck = new ArrayList<>();
        priceCheck = new ArrayList<>();
        quantityCheck = new ArrayList<>();
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
                if (isGridEnabled) {
                    UserSettings.setGridLayoutEnabled(getContext(), false);
                    recyclerLayoutIcon.setImageResource(R.drawable.final_grid_icon);
                } else {
                    UserSettings.setGridLayoutEnabled(getContext(), true);
                    recyclerLayoutIcon.setImageResource(R.drawable.final_linear_icon);
                }

                updateRecyclerView();

            }
        });

        displayData();
        loadSharedPreferences();
        return rootView;

    }

    public void displayData() {
        Cursor resCategory = db.getCategory(getActivity());
        if (resCategory.getCount() == 0) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
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
                emptyText1.setText(getString(R.string.no_list));
            } else {
                emptyView.setImageResource(R.drawable.illustration_empty_list_1);
            }

            emptyTextLayout.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.VISIBLE);
            searchView.setQueryHint("No list to search");
        } else {
            LinkedHashSet<String> hash = new LinkedHashSet<>();
            while (resCategory.moveToNext()) {
                hash.add(resCategory.getString(1).trim());
            }
            resCategory.close();

            SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
            String sort = sharedPreferences.getString(UserSettings.CUSTOM_CATEGORY_SORT, UserSettings.DATE_ASCENDING);
            settings.setCustomCategorySort(sort);

            category_list.clear();
            category_list.addAll(hash);

            if (settings.getCustomCategorySort().equals(UserSettings.NAME_ASCENDING)) {
                Collections.sort(category_list);
            } else if (settings.getCustomCategorySort().equals(UserSettings.NAME_DESCENDING)) {
                Collections.sort(category_list, Collections.reverseOrder());
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
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        boolean checkSelected = adapter.isSelected();
        if (checkSelected) {
            selectList = new ArrayList<String>();
            selectList = adapter.getSelectList();
            collapsingToolbarLayout.setTitle(selectList.size() + "/" + category_list.size());
            inflater.inflate(R.menu.category_toolbar_hold_menu, menu);
            ArrayList<String> finalSelectList = selectList;
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
                        delete_heading.setText(getString(R.string.multiple_list_remove));
                    } else {
                        delete_heading.setText(getString(R.string.list_remove));
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

                                    if (!total_url.contains(old_url)) {
                                        FirebaseStorage storage = FirebaseStorage.getInstance();
                                        StorageReference storageReference = storage.getReference().child(old_url);
                                        storageReference.delete();
                                    }


                                }

                                category_list.remove(finalSelectList.get(i));
                                db.deleteCategory(finalSelectList.get(i));
                                StyleableToast.makeText(context, "List Deleted", R.style.custom_toast).show();
                            }
                            adapter.disableSelection();
                            if (category_list.size() == 0) {
                                adapter.checkEmpty();
                            } else {
                                adapter.notifyDataSetChanged();
                            }
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
        } else {
            collapsingToolbarLayout.setTitle(getString(R.string.app_name));
            inflater.inflate(R.menu.category_toolbar_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void showBottomDialogue() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.shopbottomlayout);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        boolean isFirstStart = sharedPreferences.getBoolean("isFirstStart", true);


        TextView addItemTextHeader = dialog.findViewById(R.id.addItemHeaderText);
        TextView addItemTextHeader2 = dialog.findViewById(R.id.addItemHeaderText2);
        AutoCompleteTextView descText = dialog.findViewById(R.id.desc_name);

        String[] suggestion_list = getResources().getStringArray(R.array.item_suggestions);
        ArrayAdapter<String> suggest_adapter = new ArrayAdapter<>(context, R.layout.unit_drop_down_layout, suggestion_list);
        descText.setAdapter(suggest_adapter);

        EditText descPrice = dialog.findViewById(R.id.desc_price);
        EditText descQuantity = dialog.findViewById(R.id.desc_quantity);
        LinearLayout addItemIllustrationLayout = dialog.findViewById(R.id.addItem_Illustration_Layout);
        Button cancelButton = dialog.findViewById(R.id.CancelButton);
        Button saveButton = dialog.findViewById(R.id.BtnSave);

        TextInputLayout textInputLayout = dialog.findViewById(R.id.desc_price_text_input_layout);
        textInputLayout.setPrefixText(settings.getCurrency());

        AutoCompleteTextView unitText = dialog.findViewById(R.id.unit_textView);
        String[] unit_list = getResources().getStringArray(R.array.units);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.unit_drop_down_layout, unit_list);
        unitText.setAdapter(adapter);

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            addItemTextHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            addItemTextHeader2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            descText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            descPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            unitText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            descQuantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
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
        }


        if (isFirstStart) {
            addItemIllustrationLayout.setVisibility(View.VISIBLE);
        } else {
            addItemTextHeader.setVisibility(View.GONE);
            addItemTextHeader2.setTypeface(null, Typeface.BOLD);
            addItemIllustrationLayout.setVisibility(View.VISIBLE);
        }
        Cursor res = db.getCategory(context);
        itemCheck.clear();
        while (res.moveToNext()) {
            itemCheck.add(res.getString(1).trim());
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

                String unit;
                if (unitText.getText().toString().isEmpty()) {
                    unit = " ";
                } else {
                    unit = unitText.getText().toString();
                }

                if (!descText.getText().toString().trim().isEmpty()) {
                    description = descText.getText().toString().trim();
                    if (descPrice.getText().toString().trim().isEmpty()) {
                        if (!itemCheck.contains(descText.getText().toString().trim())) {
                            if (descQuantity.getText().toString().isEmpty()) {
                                double quant = 1;
                                double temp_price = 0;
                                insertItem(categoryTitle, description, 0, temp_price, month, year, day, time, quant, unit);
                                dialog.dismiss();
                            } else {
                                double quant2 = Long.parseLong(descQuantity.getText().toString().trim());
                                double temp_price = 0;
                                insertItem(categoryTitle, description, 0, temp_price, month, year, day, time, quant2, unit);
                                dialog.dismiss();
                            }
                        } else {
                            StyleableToast.makeText(context, getString(R.string.list_exist_already), R.style.custom_toast_2).show();
                        }
                    } else {
                        if (!itemCheck.contains(descText.getText().toString().trim())) {
                            if (descQuantity.getText().toString().isEmpty()) {
                                double quant = 1;
                                double p = Double.parseDouble(descPrice.getText().toString().trim());
                                insertItem(categoryTitle, description, 0, p, month, year, day, time, quant, unit);
                                dialog.dismiss();
                            } else {
                                double quant2 = Double.parseDouble(descQuantity.getText().toString().trim());
                                double p2 = Double.parseDouble(descPrice.getText().toString().trim());
                                insertItem(categoryTitle, description, 0, p2, month, year, day, time, quant2, unit);
                                dialog.dismiss();
                            }
                        } else {
                            StyleableToast.makeText(context, getString(R.string.list_exist_already), R.style.custom_toast_2).show();
                        }
                    }
                } else {
                    StyleableToast.makeText(context, getString(R.string.insert_item_name), R.style.custom_toast_2).show();
                }

            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showCategoryDialog() {
        final Dialog dialog = new Dialog(context);
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
                        dialog.dismiss();
                    } else {
                        int count = 1;
                        String newItem = categoryName.getText().toString().trim() + " (" + count + ")";
                        while (itemCheck.contains(newItem)) {
                            count++;
                            newItem = categoryName.getText().toString().trim() + " (" + count + ")";
                        }
                        categoryTitle = newItem;
                        getDateNdTime();
                        showBottomDialogue();
                        dialog.dismiss();

                    }
                } else {
                    StyleableToast.makeText(context, getString(R.string.please_insert_name), R.style.custom_toast_2).show();
                }

            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
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

                    if (!total_url.contains(old_url)) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageReference = storage.getReference().child(old_url);
                        storageReference.delete();
                    }


                }

                category_list.remove(temp);
                db.deleteCategory(temp);


                db.deleteCategory(temp);
                adapter.refreshRemoved(position);
                adapter.notifyItemRemoved(position);
                StyleableToast.makeText(context, getString(R.string.removed), R.style.custom_toast_2).show();
                adapter.checkEmpty();

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.notifyItemChanged(position);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showEditDialog(String prevTask, int position) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_category_layout);
        TextView title = dialog.findViewById(R.id.cate_edit_title);
        LinearLayout renameLayout = dialog.findViewById(R.id.rename);
        TextView renameText = dialog.findViewById(R.id.renameText);
        LinearLayout shareLayout = dialog.findViewById(R.id.share);
        TextView shareText = dialog.findViewById(R.id.shareText);
        LinearLayout reminderLayout = dialog.findViewById(R.id.reminder);
        TextView reminderText = dialog.findViewById(R.id.alarmText);
        LinearLayout deleteLayout = dialog.findViewById(R.id.delete);
        TextView deleteText = dialog.findViewById(R.id.deleteText);

        title.setText(prevTask);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            renameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            shareText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            reminderText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            deleteText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            renameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            shareText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            reminderText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            deleteText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            renameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            shareText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            reminderText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            deleteText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }


        renameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialog(prevTask, position);
                dialog.dismiss();
            }
        });

        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShareDialog(prevTask);
                dialog.dismiss();
            }
        });

        reminderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showAlarmDialog(prevTask);
            }
        });

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(position);
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

    private void showSortByDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.sort_category_layout);

        TextView header = dialog.findViewById(R.id.sortBy_title);
        LinearLayout nameAscend = dialog.findViewById(R.id.name_ascend);
        CheckBox checkNameAscend = dialog.findViewById(R.id.check_name_ascend);
        TextView nameTextAscend = dialog.findViewById(R.id.sort_nameText_ascend);
        checkNameAscend.setClickable(false);
        LinearLayout nameDescend = dialog.findViewById(R.id.name_descend);
        CheckBox checkNameDescend = dialog.findViewById(R.id.check_name_descend);
        TextView nameTextDescend = dialog.findViewById(R.id.sort_nameText_descend);
        checkNameDescend.setClickable(false);
        LinearLayout dateAscend = dialog.findViewById(R.id.date_ascend);
        CheckBox checkDateAscend = dialog.findViewById(R.id.check_date_ascend);
        TextView dateTextAscend = dialog.findViewById(R.id.sort_dateText_ascend);
        checkDateAscend.setClickable(false);
        LinearLayout dateDescend = dialog.findViewById(R.id.date_descend);
        CheckBox checkDateDescend = dialog.findViewById(R.id.check_date_descend);
        TextView dateTextDescend = dialog.findViewById(R.id.sort_dateText_descend);
        checkDateDescend.setClickable(false);
        LinearLayout priceAscend = dialog.findViewById(R.id.price_ascend);
        CheckBox checkPriceAscend = dialog.findViewById(R.id.check_price_ascend);
        checkPriceAscend.setClickable(false);
        TextView priceTextAscend = dialog.findViewById(R.id.price_text_ascend);
        priceTextAscend.setText(getString(R.string.sort_price_text));
        LinearLayout priceDescend = dialog.findViewById(R.id.price_descend);
        CheckBox checkPriceDescend = dialog.findViewById(R.id.check_price_descend);
        checkPriceDescend.setClickable(false);
        TextView priceTextDescend = dialog.findViewById(R.id.price_text_descend);
        priceTextDescend.setText(getString(R.string.sort_price_text));
        LinearLayout quantityAscend = dialog.findViewById(R.id.quantity_ascend);
        quantityAscend.setVisibility(View.GONE);
        LinearLayout quantityDescend = dialog.findViewById(R.id.quantity_descend);
        quantityDescend.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
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
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
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
                    checkNameDescend.setChecked(true);
                    settings.setCustomCategorySort(UserSettings.NAME_DESCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
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
                    checkNameAscend.setChecked(false);
                    checkNameDescend.setChecked(false);
                    checkDateDescend.setChecked(false);
                    checkDateAscend.setChecked(true);
                    settings.setCustomCategorySort(UserSettings.DATE_ASCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
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
                    checkNameAscend.setChecked(false);
                    checkNameDescend.setChecked(false);
                    checkDateDescend.setChecked(true);
                    settings.setCustomCategorySort(UserSettings.DATE_DESCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
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
                    checkPriceAscend.setChecked(true);
                    settings.setCustomCategorySort(UserSettings.PRICE_ASCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
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
                    checkPriceDescend.setChecked(true);
                    settings.setCustomCategorySort(UserSettings.PRICE_DESCENDING);
                }
                SharedPreferences.Editor editor = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_CATEGORY_SORT, settings.getCustomCategorySort());
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

    public void showShareDialog(String prevTask) {
        StringBuilder result = new StringBuilder();


        Cursor res = db.getItems(prevTask, context);
        itemCheck.clear();
        while (res.moveToNext()) {
            itemCheck.add(res.getString(2).trim());
            priceCheck.add(res.getString(4).trim());
            quantityCheck.add(res.getString(9).trim());
        }
        res.close();


        double total = 0;
        for (int i = 0; i < itemCheck.size(); i++) {
            double priceIndex = Double.parseDouble(priceCheck.get(i));
            double quantityIndex = Double.parseDouble(quantityCheck.get(i));
            double PriceQuantityIndex = priceIndex * quantityIndex;
            total += PriceQuantityIndex;

            if (settings.getIsShareQuantityDisabled().equals(UserSettings.NO_SHARE_QUANTITY_NOT_DISABLED)) {
                result.append(quantityCheck.get(i));
            } else {
                result.append("\u25CF");
            }

            result.append("   " + itemCheck.get(i));

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
        intent.putExtra(Intent.EXTRA_TEXT, prevTask.toUpperCase() + "\n" + " \n" + items + "\n");

        String chooserTitle = getString(R.string.send_message_via);
        Intent chosenIntent = Intent.createChooser(intent, chooserTitle);
        startActivity(chosenIntent);


    }

    public void showRenameDialog(String prevName, int position) {
        final Dialog dialog = new Dialog(context);
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
                    dialog.dismiss();
                    StyleableToast.makeText(context, getString(R.string.list_rename_success), R.style.custom_toast).show();
                } else {
                    itemCheck.remove(prevName);
                    if (!newName.isEmpty()) {
                        if (!itemCheck.contains(newName)) {
                            boolean checkEditData = db.updateCategory(newName, prevName);
                            if (!checkEditData) {
                                Toast.makeText(context, R.string.rename_fail, Toast.LENGTH_SHORT).show();
                            } else {
                                StyleableToast.makeText(context, getString(R.string.list_rename_success), R.style.custom_toast).show();
                                dialog.dismiss();
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
                                StyleableToast.makeText(context, getString(R.string.rename_fail), R.style.custom_toast).show();
                            } else {
                                StyleableToast.makeText(context, getString(R.string.list_rename_success), R.style.custom_toast).show();
                                dialog.dismiss();
                                adapter.refreshUpdate(newItem, position);
                                adapter.notifyItemChanged(position);
                            }
                        }
                    } else {
                        StyleableToast.makeText(context, getString(R.string.please_insert_name), R.style.custom_toast_2).show();
                    }

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

    public void showAlarmDialog(String description) {


        calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentMinutes = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {

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
                    StyleableToast.makeText(context, getString(R.string.no_alarm_app), R.style.custom_toast).show();
                }

            }
        }, currentHour, currentMinutes, false);
        WindowManager.LayoutParams params = timePickerDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        timePickerDialog.getWindow().setAttributes(params);
        timePickerDialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_time_picker));
        timePickerDialog.show();
    }


    public void insertItem(String category, String description, int status, double price, String month, String year, String day, String time, double quantity, String unit) {

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        boolean isFirstStart = sharedPreferences.getBoolean("isFirstStart", true);
        String finalPrice = formatNumberV2(price);
        String finalQuantity = formatNumberV2(quantity);

        db.insertItem(category, description, status, finalPrice, month, year, day, time, finalQuantity, unit);

        UserSettings settings = (UserSettings) getActivity().getApplication();
        settings.setFirstStart(false);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putBoolean("isFirstStart", settings.getFirstStart());
        editor.apply();


        if (isFirstStart) {
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.success_dialog_popup);
            dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(false);
            dialog.show();

            Button okay = dialog.findViewById(R.id.okBtn);
            okay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Intent intent = new Intent(context, ItemActivity.class);
                    intent.putExtra("ITEM", category);
                    startActivity(intent);

                }
            });

        } else {
            StyleableToast.makeText(context, getString(R.string.list_created), R.style.custom_toast).show();
            Intent intent = new Intent(context, ItemActivity.class);
            intent.putExtra("ITEM", category);
            startActivity(intent);
        }


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

        time = timeFormat.format(date);
        month = currentMonth;
        year = currentYear;
        day = currentDay + " " + currentDayNumber;

        formattedDate = currentDayNumber + "/" + currentMonthNumber + "/" + currentYear;
    }

    private void filterList(String text) {
        ArrayList<String> filterList = new ArrayList<>();

        for (String item : category_list) {
            if (item.toLowerCase().contains(text.toLowerCase())) {
                filterList.add(item);
            }

        }
        adapter.setFilterList(filterList);

    }

    private void updateRecyclerView() {

        boolean isGridEnabled = UserSettings.isGridLockEnabled(getContext());
        if (isGridEnabled) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            recyclerLayoutIcon.setImageResource(R.drawable.final_linear_icon);

        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerLayoutIcon.setImageResource(R.drawable.final_grid_icon);
        }

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onNoteClick(int position) {
        String temp = category_list.get(position);
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
        MenuItem settingNav = navigationView.getMenu().findItem(R.id.nav_setting);
        MenuItem HomeNav = navigationView.getMenu().findItem(R.id.nav_home);
        settingNav.setChecked(false);
        HomeNav.setChecked(true);
        if (drawerLayout.isOpen()) {
            drawerLayout.close();
        }
        displayData();
        adapter.notifyDataSetChanged();
        super.onResume();
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

    @Override
    public void onStart() {
        MenuItem settingNav = navigationView.getMenu().findItem(R.id.nav_setting);
        MenuItem HomeNav = navigationView.getMenu().findItem(R.id.nav_home);
        settingNav.setChecked(false);
        HomeNav.setChecked(true);
        if (drawerLayout.isOpen()) {
            drawerLayout.close();
        }
        displayData();
        adapter.notifyDataSetChanged();
        super.onStart();
    }

    private void updateView() {

        if (settings.getCustomTheme().equals(UserSettings.LIGHT_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sortByText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            navigationView.setItemTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Small);
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyText3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            empty_create_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            toolBarText_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sortByText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            navigationView.setItemTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Menu);
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyText3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            empty_create_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            toolBarText_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sortByText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            navigationView.setItemTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Large);
            emptyText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyText3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            empty_create_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            toolBarText_2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));


        }
    }

    private void loadSharedPreferences() {

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);

        String theme = sharedPreferences.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);

        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        String is_price_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_PRICE_DISABLED, UserSettings.YES_SHARE_PRICE_DISABLED);
        settings.setIsSharePriceDisabled(is_price_disabled);

        String is_quantity_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_QUANTITY_DISABLED, UserSettings.YES_SHARE_QUANTITY_DISABLED);
        settings.setIsShareQuantityDisabled(is_quantity_disabled);

        String is_total_disabled = sharedPreferences.getString(UserSettings.IS_SHARE_TOTAL_DISABLED, UserSettings.YES_SHARE_TOTAL_DISABLED);
        settings.setIsShareTotalDisabled(is_total_disabled);

        String currency = sharedPreferences.getString(UserSettings.CURRENCY, UserSettings.CURRENCY_DOLLAR);
        settings.setCurrency(currency);

        String firstname = sharedPreferences.getString(UserSettings.USER_NAME, UserSettings.USER_NAME_NOT_SET);
        settings.setUsername(firstname);

        updateView();
    }
}