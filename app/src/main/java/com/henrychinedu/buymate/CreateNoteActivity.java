package com.henrychinedu.buymate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.henrychinedu.buymate.Database.ShopDatabase;
import com.henrychinedu.buymate.Database.UserSettings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView date_box;
    EditText heading_box;
    EditText content_box;
    String full_date;
    ArrayList<String> itemCheck;


    String prev_heading;
    String prev_content;
    int count_init = 0;
    LinearLayout full_layout;
    ShopDatabase db = new ShopDatabase(CreateNoteActivity.this);

    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new UserSettings();
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
        setContentView(R.layout.activity_create_note);

        toolbar = findViewById(R.id.note_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        date_box = findViewById(R.id.date_text);
        heading_box = findViewById(R.id.heading_text);
        content_box = findViewById(R.id.content_text);
        full_layout = findViewById(R.id.full_create_layout);
        itemCheck = new ArrayList<>();
        getDateNdTime();
        date_box.setText(full_date);


        heading_box.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MenuItem saveIcon = toolbar.getMenu().findItem(R.id.save);
                MenuItem shareIcon = toolbar.getMenu().findItem(R.id.share);
                MenuItem deleteIcon = toolbar.getMenu().findItem(R.id.delete);

                if (s.length() > 0) {
                    saveIcon.setVisible(true);
                    shareIcon.setVisible(false);
                    deleteIcon.setVisible(false);
                } else {
                    saveIcon.setVisible(!content_box.getText().toString().isEmpty());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        full_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content_box.requestFocus();
                content_box.setSelection(content_box.getText().length());
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(content_box, InputMethodManager.SHOW_IMPLICIT);
            }
        });


        content_box.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MenuItem saveIcon = toolbar.getMenu().findItem(R.id.save);
                MenuItem shareIcon = toolbar.getMenu().findItem(R.id.share);
                MenuItem deleteIcon = toolbar.getMenu().findItem(R.id.delete);
                if (s.length() > 0) {
                    saveIcon.setVisible(true);
                    shareIcon.setVisible(false);
                    deleteIcon.setVisible(false);
                } else {
                    saveIcon.setVisible(!heading_box.getText().toString().isEmpty());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                content_box.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(content_box, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 300);

        loadSharedPreferences();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_toolbar_create_menu, menu);
        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem menuItem = menu.getItem(i);
                CharSequence title = menuItem.getTitle();
                if (!(title instanceof SpannableString)) {
                    SpannableString spannableString = new SpannableString(title);
                    spannableString.setSpan(new RelativeSizeSpan(0.9f), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    menuItem.setTitle(spannableString);
                }
            }

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem menuItem = menu.getItem(i);
                CharSequence title = menuItem.getTitle();
                if (!(title instanceof SpannableString)) {
                    SpannableString spannableString = new SpannableString(title);
                    spannableString.setSpan(new RelativeSizeSpan(1.1f), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    menuItem.setTitle(spannableString);
                }
            }
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem menuItem = menu.getItem(i);
                CharSequence title = menuItem.getTitle();
                if (!(title instanceof SpannableString)) {
                    SpannableString spannableString = new SpannableString(title);
                    spannableString.setSpan(new RelativeSizeSpan(1.3f), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    menuItem.setTitle(spannableString);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save) {
            if (count_init > 0) {
                Cursor res = db.getNoteHeading();
                itemCheck.clear();
                while (res.moveToNext()) {
                    itemCheck.add(res.getString(1));
                }
                res.close();
                itemCheck.remove(prev_heading);
                String new_heading;
                if (heading_box.getText().toString().trim().isEmpty()) {
                    new_heading = getString(R.string.CreateNoteActivity__untitled);
                } else {
                    new_heading = heading_box.getText().toString();
                }
                if (!itemCheck.contains(new_heading)) {
                    boolean update = db.updateNote(prev_heading, new_heading, content_box.getText().toString(), date_box.getText().toString());
                    prev_heading = new_heading;
                    prev_content = content_box.getText().toString();
                    MenuItem saveIcon = toolbar.getMenu().findItem(R.id.save);
                    saveIcon.setVisible(false);
                    MenuItem shareIcon = toolbar.getMenu().findItem(R.id.share);
                    shareIcon.setVisible(true);
                    MenuItem deleteIcon = toolbar.getMenu().findItem(R.id.delete);
                    deleteIcon.setVisible(true);
                    heading_box.clearFocus();
                    content_box.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(content_box.getWindowToken(), 0);
                    if (update) {
                        count_init = count_init + 1;
                    }
                } else {
                    int count = 1;
                    String newItem = new_heading + " (" + count + ")";
                    while (itemCheck.contains(newItem)) {
                        count++;
                        newItem = new_heading + " (" + count + ")";
                    }
                    boolean update = db.updateNote(prev_heading, newItem, content_box.getText().toString(), date_box.getText().toString());
                    prev_heading = newItem;
                    prev_content = content_box.getText().toString();
                    MenuItem saveIcon = toolbar.getMenu().findItem(R.id.save);
                    saveIcon.setVisible(false);
                    MenuItem shareIcon = toolbar.getMenu().findItem(R.id.share);
                    shareIcon.setVisible(true);
                    MenuItem deleteIcon = toolbar.getMenu().findItem(R.id.delete);
                    deleteIcon.setVisible(true);
                    heading_box.clearFocus();
                    content_box.clearFocus();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(content_box.getWindowToken(), 0);

                    if (update) {
                        count_init = count_init + 1;
                    }
                }

            } else {

                Cursor res = db.getNoteHeading();
                itemCheck.clear();
                while (res.moveToNext()) {
                    itemCheck.add(res.getString(1));
                }
                res.close();
                String new_heading;
                if (heading_box.getText().toString().trim().isEmpty()) {
                    new_heading = getString(R.string.CreateNoteActivity__untitled);
                } else {
                    new_heading = heading_box.getText().toString();
                }

                if (!itemCheck.contains(new_heading)) {
                    boolean insert = db.insertNote(new_heading, content_box.getText().toString(), date_box.getText().toString());
                    prev_heading = new_heading;
                    prev_content = content_box.getText().toString();
                    MenuItem saveIcon = toolbar.getMenu().findItem(R.id.save);
                    saveIcon.setVisible(false);
                    MenuItem shareIcon = toolbar.getMenu().findItem(R.id.share);
                    shareIcon.setVisible(true);
                    MenuItem deleteIcon = toolbar.getMenu().findItem(R.id.delete);
                    deleteIcon.setVisible(true);
                    heading_box.clearFocus();
                    content_box.clearFocus();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(content_box.getWindowToken(), 0);
                    if (insert) {
                        count_init = count_init + 1;
                    }
                } else {
                    int count = 1;
                    String newItem = new_heading + " (" + count + ")";
                    while (itemCheck.contains(newItem)) {
                        count++;
                        newItem = new_heading + " (" + count + ")";
                    }
                    boolean insert = db.insertNote(newItem, content_box.getText().toString(), date_box.getText().toString());
                    prev_heading = newItem;
                    prev_content = content_box.getText().toString();
                    MenuItem saveIcon = toolbar.getMenu().findItem(R.id.save);
                    saveIcon.setVisible(false);
                    MenuItem shareIcon = toolbar.getMenu().findItem(R.id.share);
                    shareIcon.setVisible(true);
                    MenuItem deleteIcon = toolbar.getMenu().findItem(R.id.delete);
                    deleteIcon.setVisible(true);
                    heading_box.clearFocus();
                    content_box.clearFocus();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(content_box.getWindowToken(), 0);

                    if (insert) {
                        count_init = count_init + 1;
                    }
                }

                return true;
            }

        } else if (item.getItemId() == R.id.share) {
            showShareDialog();
        } else if (item.getItemId() == R.id.delete) {

            Dialog dialog = new Dialog(CreateNoteActivity.this);
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

            delete_heading.setText(getString(R.string.CreateNoteActivity__deleteNote));
            delete_message.setText(getString(R.string.CreateNoteActivity__deleteWarning));

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.deleteNote(heading_box.getText().toString(), content_box.getText().toString());
                    CreateNoteActivity.super.onBackPressed();
                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } else if (item.getItemId() == R.id.voice) {
            showGoogleVoiceDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void typeWithVoice(String text) {
        String previous_text;
        String new_text;

        if (content_box.hasFocus()) {
            previous_text = content_box.getText().toString().trim();
            new_text = text;
            if (previous_text.isEmpty()) {
                content_box.setText(new_text);
            } else {
                String temp_text = previous_text + " " + new_text;
                content_box.setText(temp_text);
            }

        } else if (heading_box.hasFocus()) {
            previous_text = heading_box.getText().toString().trim();
            new_text = text;
            if (previous_text.isEmpty()) {
                heading_box.setText(new_text);
            } else {
                String temp_text = previous_text + " " + new_text;
                heading_box.setText(temp_text);
            }
        } else {
            previous_text = content_box.getText().toString().trim();
            new_text = text;
            if (previous_text.isEmpty()) {
                content_box.setText(new_text);
            } else {
                String temp_text = previous_text + " " + new_text;
                content_box.setText(temp_text);
            }
        }

    }

    public void showGoogleVoiceDialog() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.CreateNoteActivity__voicePrompt));
        startActivityForResult(intent, 8080);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 8080) && (resultCode == Activity.RESULT_OK)) {
            String voiceText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            typeWithVoice(voiceText);
        }

    }

    public void getDateNdTime() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();


        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMMM", Locale.getDefault());
        String currentMonth = sdfMonth.format(date);

        // SimpleDateFormat sdfMonthNumber = new SimpleDateFormat("MM", Locale.getDefault());
        // String currentMonthNumber = sdfMonthNumber.format(date);

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = sdfYear.format(date);

        SimpleDateFormat sdfDay = new SimpleDateFormat("EEEE", Locale.getDefault());
        String currentDay = sdfDay.format(date);

        SimpleDateFormat sdfDayNumber = new SimpleDateFormat("dd", Locale.getDefault());
        String currentDayNumber = sdfDayNumber.format(date);

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        full_date = timeFormat.format(date) + " " + currentDay + " " + currentDayNumber + " " + currentMonth + " " + currentYear;

    }

    private void updateView() {


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            heading_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            content_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            date_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            heading_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            content_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            date_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            heading_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            content_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            date_box.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
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


        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        updateView();
    }

    public void showShareDialog() {
        String items = content_box.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, heading_box.getText().toString().toUpperCase() + "\n" + " \n" + items + "\n");
        String chooserTitle = getString(R.string.CreateNoteActivity__send_message_via);
        Intent chosenIntent = Intent.createChooser(intent, chooserTitle);
        startActivity(chosenIntent);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }


        ArrayList<String> heading_check = new ArrayList<>();
        ArrayList<String> content_check = new ArrayList<>();
        ArrayList<String> date_check = new ArrayList<>();


        String headBox = heading_box.getText().toString().trim();
        String contentBox = content_box.getText().toString().trim();
        String dateBox = date_box.getText().toString().trim();
        String totsText = headBox + contentBox;

        Cursor res = db.getNoteHeading();
        while (res.moveToNext()) {
            heading_check.add(res.getString(1).trim());
            content_check.add(res.getString(2).trim());
            date_check.add(res.getString(3).trim());
        }
        res.close();

        if (count_init > 0) {


            if (!heading_check.contains(prev_heading) && !content_check.contains(prev_content)) {
                if (!totsText.trim().isEmpty()) {
                    if (!headBox.trim().equals(prev_heading.trim()) | !contentBox.trim().equals(prev_content.trim())) {
                        res = db.getNoteHeading();
                        itemCheck.clear();
                        while (res.moveToNext()) {
                            itemCheck.add(res.getString(1));
                        }
                        res.close();
                        itemCheck.remove(prev_heading);
                        String new_heading;
                        if (heading_box.getText().toString().trim().isEmpty()) {
                            new_heading = getString(R.string.CreateNoteActivity__untitled);
                        } else {
                            new_heading = heading_box.getText().toString();
                        }
                        if (!itemCheck.contains(new_heading)) {
                            boolean update = db.updateNote(prev_heading, new_heading, content_box.getText().toString(), date_box.getText().toString());
                            prev_heading = new_heading.trim();
                            prev_content = content_box.getText().toString().trim();
                        } else {
                            int count = 1;
                            String newItem = new_heading + " (" + count + ")";
                            while (itemCheck.contains(newItem)) {
                                count++;
                                newItem = new_heading + " (" + count + ")";
                            }
                            boolean update = db.updateNote(prev_heading, newItem, content_box.getText().toString(), date_box.getText().toString());
                            prev_heading = newItem.trim();
                            prev_content = content_box.getText().toString().trim();
                        }
                    }
                } else {
                    db.deleteNote(prev_heading, prev_content);
                }
            }
        } else {
            if (count_init == 0) {

                if (!heading_check.contains(headBox) && !content_check.contains(contentBox) && !date_check.contains(dateBox)) {


                    if (!totsText.trim().isEmpty()) {
                        res = db.getNoteHeading();
                        itemCheck.clear();
                        while (res.moveToNext()) {
                            itemCheck.add(res.getString(1));
                        }
                        res.close();
                        String new_heading;
                        if (heading_box.getText().toString().trim().isEmpty()) {
                            new_heading = getString(R.string.CreateNoteActivity__untitled);
                        } else {
                            new_heading = heading_box.getText().toString();
                        }

                        if (!itemCheck.contains(new_heading)) {
                            boolean insert = db.insertNote(new_heading, content_box.getText().toString(), date_box.getText().toString());
                            prev_heading = new_heading.trim();
                            prev_content = content_box.getText().toString().trim();
                        } else {
                            int count = 1;
                            String newItem = new_heading + " (" + count + ")";
                            while (itemCheck.contains(newItem)) {
                                count++;
                                newItem = new_heading + " (" + count + ")";
                            }
                            boolean insert = db.insertNote(newItem, content_box.getText().toString(), date_box.getText().toString());
                            prev_heading = newItem.trim();
                            prev_content = content_box.getText().toString().trim();
                        }
                    }


                }
            }
        }
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


        ArrayList<String> heading_check = new ArrayList<>();
        ArrayList<String> content_check = new ArrayList<>();

        String headBox = heading_box.getText().toString().trim();
        String contentBox = content_box.getText().toString().trim();

        Cursor res = db.getNoteHeading();
        while (res.moveToNext()) {
            heading_check.add(res.getString(1).trim());
            content_check.add(res.getString(2).trim());
        }
        res.close();


        if (heading_check.contains(headBox)) {
            db.deleteNote(headBox, contentBox);
        }


    }


    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}