package com.hemerick.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hemerick.buymate.Adapter.ShopNotesAdapter;
import com.hemerick.buymate.Database.ShopDatabase;
import com.hemerick.buymate.Database.UserSettings;

import java.util.ArrayList;

import io.github.muddz.styleabletoast.StyleableToast;

public class NotesFragment extends Fragment implements ShopNotesAdapter.OnNoteListener {

    SwipeRefreshLayout swipeRefreshLayout;
    Context context;
    Toolbar toolbar;
    RecyclerView recyclerView;
    FloatingActionButton floatingActionButton;

    ShopDatabase db;
    ArrayList<String> note_heading_list;
    ArrayList<String> note_content_list;
    ArrayList<String> note_date_list;

    ArrayList<String> selectListHeading;
    ArrayList<String> selectListContent;
    ArrayList<String> selectListDate;
    CollapsingToolbarLayout collapsingToolbarLayout;
    LinearLayout emptyNotesLayout;
    SearchView searchView;
    EditText searchEditText;
    ShopNotesAdapter shopNotesAdapter;

    ImageView emptyImageView;
    TextView emptyTEXT1;
    TextView emptyTEXT2;
    TextView emptyTEXT3;

    UserSettings settings;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_notes, container, false);

        context = getContext();
        collapsingToolbarLayout = rootView.findViewById(R.id.collapsing_Toolbar);
        toolbar = rootView.findViewById(R.id.notesToolbar);
        settings = new UserSettings();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }

        setHasOptionsMenu(true);
        searchView = rootView.findViewById(R.id.search_View);
        searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        emptyImageView = rootView.findViewById(R.id.empty);
        emptyTEXT1 = rootView.findViewById(R.id.emptyTEXT1);
        emptyTEXT2 = rootView.findViewById(R.id.emptyTEXT2);
        emptyTEXT3 = rootView.findViewById(R.id.emptyTEXT3);

        recyclerView = rootView.findViewById(R.id.notes_list);
        note_heading_list = new ArrayList<>();
        note_content_list = new ArrayList<>();
        note_date_list = new ArrayList<>();
        floatingActionButton = rootView.findViewById(R.id.notes_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = "new";
                Intent intent = new Intent(getContext(), CreateNoteActivity.class);
                intent.putExtra("STATUS", status);
                startActivity(intent);
            }
        });
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

        swipeRefreshLayout = rootView.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchEditText.setText("");
                        displayData();
                        shopNotesAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                } , 3000) ;
            }
        });


        db = new ShopDatabase(getActivity());
        shopNotesAdapter = new ShopNotesAdapter(context, settings, note_heading_list, note_content_list, note_date_list, this, NotesFragment.this);
        recyclerView.setAdapter(shopNotesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        emptyNotesLayout = rootView.findViewById(R.id.emptyNotesLayout);

        displayData();
        loadSharedPreferences();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        boolean checkSelected = shopNotesAdapter.isSelected();
        if (checkSelected) {
            selectListHeading = new ArrayList<String>();
            selectListContent = new ArrayList<String>();
            selectListDate = new ArrayList<String>();
            selectListHeading = shopNotesAdapter.getSelectListHeading();
            selectListContent = shopNotesAdapter.getSelectListContent();
            selectListDate = shopNotesAdapter.getSelectListDate();
            collapsingToolbarLayout.setTitle(selectListHeading.size() + "/" + note_heading_list.size());
            inflater.inflate(R.menu.note_toolbar_hold_menu, menu);
            ArrayList<String> finalSelectListHeading = selectListHeading;
            ArrayList<String> finalSelectListContent = selectListContent;
            ArrayList<String> finalSelectListDate = selectListDate;

            menu.findItem(R.id.delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {


                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.custom_delete_dialog);
                    dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                    StringBuilder items_selected = new StringBuilder();
                    for (String items : finalSelectListHeading) {
                        items_selected.append("\u2022 ").append(items).append("\n");
                    }


                    TextView delete_heading = dialog.findViewById(R.id.delete_heading);
                    TextView delete_message = dialog.findViewById(R.id.delete_message);
                    Button deleteButton = dialog.findViewById(R.id.delete_button);
                    Button cancelButton = dialog.findViewById(R.id.cancel_button);

                    if (finalSelectListHeading.size() > 1) {
                        delete_heading.setText(getString(R.string.multiple_notes_remove));
                    } else {
                        delete_heading.setText(getString(R.string.single_note_remove));
                    }
                    delete_message.setText(items_selected.toString());

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (int i = 0; i < finalSelectListHeading.size(); i++) {
                                note_heading_list.remove(finalSelectListHeading.get(i));
                                note_content_list.remove(finalSelectListContent.get(i));
                                note_date_list.remove(finalSelectListDate.get(i));
                                shopNotesAdapter.notifyDataSetChanged();
                                db.deleteNote(selectListHeading.get(i), selectListContent.get(i));
                            }

                            if (note_heading_list.size() == 0) {
                                boolean checkEmpty = shopNotesAdapter.checkEmpty();
                                if (checkEmpty) {
                                    recyclerView.setVisibility(View.GONE);
                                    emptyNotesLayout.setVisibility(View.VISIBLE);
                                }
                            }
                            StyleableToast.makeText(context, "Note Deleted", R.style.custom_toast).show();
                            shopNotesAdapter.disableSelection();
                            searchEditText.setText("");
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
                    shopNotesAdapter.setSelectAll();

                    return true;
                }
            });
        } else {
            collapsingToolbarLayout.setTitle(getString(R.string.fragment_notes_toolbar_title));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void displayData() {
        note_heading_list.clear();
        note_content_list.clear();
        note_date_list.clear();
        //get total category
        Cursor res = db.getNoteHeading();
        if (res.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyNotesLayout.setVisibility(View.VISIBLE);
            searchView.setQueryHint("No note to search");
        } else {
            while (res.moveToNext()) {
                note_heading_list.add(res.getString(1));
                note_content_list.add(res.getString(2));
                note_date_list.add(res.getString(3));
            }
            res.close();

        }
    }


    public boolean onBackPressed() {
        boolean check = shopNotesAdapter.isSelected();
        if (check) {
            shopNotesAdapter.disableSelection();
            shopNotesAdapter.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onNoteClick(int position) {

    }

    @Override
    public void onItemLongClick(int position) {
        String temp_heading = shopNotesAdapter.getHeadingName(position);
        String temp_content = shopNotesAdapter.getContentName(position);
        String temp_date = shopNotesAdapter.getDateName(position);
        Intent intent = new Intent(getContext(), UpdateNoteActivity.class);
        intent.putExtra("HEADING", temp_heading);
        intent.putExtra("CONTENT", temp_content);
        intent.putExtra("DATE", temp_date);
        startActivity(intent);
    }

    @Override
    public void onOptionClick(int position) {

    }

    private void filterList(String text) {

        ArrayList<String> filterList = new ArrayList<>();
        ArrayList<String> filterList2 = new ArrayList<>();
        ArrayList<String> filterList3 = new ArrayList<>();


        ArrayList<String> searchList = new ArrayList<>();
        for (int i = 0; i < note_heading_list.size(); i++) {
            searchList.add(note_heading_list.get(i) + note_content_list.get(i));
        }
        for (int j = 0; j < note_heading_list.size(); j++) {
            String item = searchList.get(j);
            if (item.toLowerCase().contains(text.toLowerCase())) {
                filterList.add(note_heading_list.get(j));
                Cursor res = db.getNoteContent(note_heading_list.get(j));
                while (res.moveToNext()) {
                    filterList2.add(res.getString(2));
                    filterList3.add(res.getString(3));
                }
                res.close();
            }

        }
        shopNotesAdapter.setFilterList(filterList, filterList2, filterList3);
        emptyNotesLayout.setVisibility(View.GONE);
        emptyTEXT1.setVisibility(View.GONE);
        emptyTEXT2.setVisibility(View.GONE);
        emptyTEXT3.setVisibility(View.GONE);
        emptyTEXT1.setText("No note found");

        if (filterList.isEmpty()) {
            emptyImageView.setImageResource(R.drawable.illustration_no_search_data);
            ViewGroup.LayoutParams params = emptyImageView.getLayoutParams();
            params.width = 200;
            params.height = 200;
            emptyImageView.setLayoutParams(params);
            emptyNotesLayout.setVisibility(View.VISIBLE);
            emptyTEXT1.setVisibility(View.VISIBLE);
            emptyTEXT2.setVisibility(View.GONE);
            emptyTEXT3.setVisibility(View.GONE);
            emptyTEXT1.setText("No note found");

        }

    }


    @Override
    public void onDestroyView() {
        db.close();
        super.onDestroyView();
    }

    private void updateView() {

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyTEXT2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            emptyTEXT3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyTEXT2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            emptyTEXT3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyTEXT1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyTEXT2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            emptyTEXT3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

    }

    private void loadSharedPreferences() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);


        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        updateView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }
}