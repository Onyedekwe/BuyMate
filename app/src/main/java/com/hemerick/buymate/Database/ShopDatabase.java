package com.hemerick.buymate.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ShopDatabase extends SQLiteOpenHelper {
    private final UserSettings settings = new UserSettings();

    public ShopDatabase(Context context) {
        super(context, "ShopDatabase", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table ShopTable(_id INTEGER primary key autoincrement,category TEXT, description TEXT, status INTEGER, price TEXT, month TEXT, year TEXT, day TEXT, time TEXT, quantity TEXT, favourites INTEGER, unit TEXT, photourl TEXT)");
        db.execSQL("create Table NoteTable(_id INTEGER primary key autoincrement,heading TEXT, content TEXT, date TEXT)");
        db.execSQL("create Table SuggestTable(_id INTEGER primary key autoincrement, itemName TEXT)");
        db.execSQL("create Table SuggestUnitTable(_id INTEGER primary key autoincrement, itemUnit TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) onCreate(db);
    }

    public void insertItem(String category, String description, int status, String price, String month, String year, String day, String time, String quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("category", category);
        contentValues.put("description", description);
        contentValues.put("status", status);
        contentValues.put("price", price);
        contentValues.put("month", month);
        contentValues.put("year", year);
        contentValues.put("day", day);
        contentValues.put("time", time);
        contentValues.put("quantity", quantity);
        contentValues.put("unit", unit);
        contentValues.put("photourl", " ");
        long result = db.insert("ShopTable", null, contentValues);

    }


    public boolean insertNote(String heading, String content, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("heading", heading);
        contentValues.put("content", content);
        contentValues.put("date", date);
        long result = db.insert("NoteTable", null, contentValues);
        return result != -1;


    }

    public boolean insertSuggest(String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("itemName", itemName);
        long result = db.insert("SuggestTable", null, contentValues);
        return result != -1;
    }

    public boolean insertSuggestUnit(String itemUnit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("itemUnit", itemUnit);
        long result = db.insert("SuggestUnitTable", null, contentValues);
        return result != -1;
    }


    public boolean updateItem(String category, String description, String temp_description) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("description", description);
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ? ", new String[]{category, temp_description});
        if (cursor.getCount() > 0) {
            long result = db.update("ShopTable", contentValues, "category = ? AND description = ? ", new String[]{category, temp_description});
            return result != -1;

        } else {
            return false;
        }
    }

    public boolean updateNote(String heading, String new_heading, String new_content, String new_date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("heading", new_heading);
        contentValues.put("content", new_content);
        contentValues.put("date", new_date);
        Cursor cursor = db.rawQuery("select * from NoteTable where heading = ? ", new String[]{heading});
        if (cursor.getCount() > 0) {
            long result = db.update("NoteTable", contentValues, "heading = ? ", new String[]{heading});
            return result != -1;
        } else {
            return false;
        }
    }

    public boolean updateCategory(String category, String temp_category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("category", category);
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? ", new String[]{temp_category});
        if (cursor.getCount() > 0) {
            long result = db.update("ShopTable", contentValues, "category = ? ", new String[]{temp_category});
            return result != -1;
        } else {
            return false;
        }
    }

    public boolean moveItem(String temp_category, String category, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("category", category);
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ? ", new String[]{temp_category, description});
        if (cursor.getCount() > 0) {
            long result = db.update("ShopTable", contentValues, "category = ? AND description = ? ", new String[]{temp_category, description});
            return result != -1;
        } else {
            return false;

        }
    }


    public void updateStatus(String category, String description, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", status);
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ? ", new String[]{category, description});
        if (cursor.getCount() > 0) {
            long result = db.update("ShopTable", contentValues, "category = ? AND description = ? ", new String[]{category, description});
        } else {
        }
    }

    public void updateFavourites(String category, String description, int favourites) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("favourites", favourites);
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ? ", new String[]{category, description});
        if (cursor.getCount() > 0) {
            long result = db.update("ShopTable", contentValues, "category = ? AND description = ? ", new String[]{category, description});
        } else {
        }
    }

    public void updatePhoto(String category, String description, String photourl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("photourl", photourl);
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ? ", new String[]{category, description});
        if (cursor.getCount() > 0) {
            long result = db.update("ShopTable", contentValues, "category = ? AND description = ? ", new String[]{category, description});
        } else {
        }
    }

    public boolean updatePrice(String category, String description, String price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("price", price);
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ? ", new String[]{category, description});
        if (cursor.getCount() > 0) {
            long result = db.update("ShopTable", contentValues, "category = ? AND description = ? ", new String[]{category, description});
            return result != -1;
        } else {
            return false;
        }
    }


    public boolean updateQuantity(String category, String description, String quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("quantity", quantity);
        contentValues.put("unit", unit);
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ? ", new String[]{category, description});
        if (cursor.getCount() > 0) {
            long result = db.update("ShopTable", contentValues, "category = ? AND description = ? ", new String[]{category, description});
            return result != -1;
        } else {
            return false;
        }
    }

    public void deleteItem(String category, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ?", new String[]{category, description});
        if (cursor.getCount() > 0) {
            long result = db.delete("ShopTable", "category = ? AND description = ?", new String[]{category, description});
            db.close();
        }
    }

    public void deleteCategory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("select * from ShopTable where category = ?", new String[]{category});
        if (cursor.getCount() > 0) {
            long result = db.delete("ShopTable", "category = ?", new String[]{category});
            db.close();
        }
    }


    public void deleteAllList() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ShopTable", null, null);
        db.close();
    }

    public void deleteAllSuggest() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("SuggestTable", null, null);
        db.close();
    }

    public void deleteAllSuggestUnit() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("SuggestUnitTable", null, null);
        db.close();
    }

    public void deleteAllNote() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("NoteTable", null, null);
        db.close();
    }


    public void deleteNote(String heading, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("select * from NoteTable where heading = ? AND content = ?", new String[]{heading, content});
        if (cursor.getCount() > 0) {
            long result = db.delete("NoteTable", "heading = ? AND content = ?", new String[]{heading, content});
            db.close();
        }
    }

    public Cursor getCategory(Context context) {
        SQLiteDatabase db = this.getWritableDatabase();

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String sort = sharedPreferences.getString(UserSettings.CUSTOM_CATEGORY_SORT, UserSettings.DATE_ASCENDING);
        settings.setCustomCategorySort(sort);
        Cursor res = null;

        if (settings.getCustomCategorySort().equals(UserSettings.NAME_ASCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ", null);
        } else if (settings.getCustomCategorySort().equals(UserSettings.NAME_DESCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ", null);
        } else if (settings.getCustomCategorySort().equals(UserSettings.DATE_ASCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ORDER BY _id ASC", null);
        } else if (settings.getCustomCategorySort().equals(UserSettings.DATE_DESCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ORDER BY _id ASC", null);
        } else if (settings.getCustomCategorySort().equals(UserSettings.PRICE_ASCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ", null);
        } else if (settings.getCustomCategorySort().equals(UserSettings.PRICE_DESCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ", null);
        }
        return res;
    }


    public Cursor getNoteHeading() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM NoteTable ", null);
    }

    public Cursor getSuggestName() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM SuggestTable ", null);

    }

    public Cursor getSuggestUnit() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM SuggestUnitTable ", null);
    }


    public Cursor getItems(String category, Context context) {
        SQLiteDatabase db = this.getWritableDatabase();

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String sort = sharedPreferences.getString(UserSettings.CUSTOM_SORT, UserSettings.DATE_ASCENDING);
        settings.setCustomSort(sort);
        Cursor res = null;

        if (settings.getCustomSort().equals(UserSettings.NAME_ASCENDING)) {
            res = db.rawQuery("select * from ShopTable  where category = ? order by description asc", new String[]{category});
        } else if (settings.getCustomSort().equals(UserSettings.NAME_DESCENDING)) {
            res = db.rawQuery("select * from ShopTable  where category = ? order by description desc", new String[]{category});
        } else if (settings.getCustomSort().equals(UserSettings.DATE_ASCENDING)) {
            res = db.rawQuery("select * from ShopTable  where category = ? order by _id asc", new String[]{category});
        } else if (settings.getCustomSort().equals(UserSettings.DATE_DESCENDING)) {
            res = db.rawQuery("select * from ShopTable  where category = ? order by _id desc", new String[]{category});
        } else if (settings.getCustomSort().equals(UserSettings.PRICE_ASCENDING)) {
            res = db.rawQuery("select * from ShopTable  where category = ? order by cast(price as real) asc", new String[]{category});
        } else if (settings.getCustomSort().equals(UserSettings.PRICE_DESCENDING)) {
            res = db.rawQuery("select * from ShopTable  where category = ? order by cast(price as real) desc", new String[]{category});
        } else if (settings.getCustomSort().equals(UserSettings.QUANTITY_ASCENDING)) {
            res = db.rawQuery("select * from ShopTable  where category = ? order by cast(quantity as real) asc", new String[]{category});
        } else if (settings.getCustomSort().equals(UserSettings.QUANTITY_DESCENDING)) {
            res = db.rawQuery("select * from ShopTable  where category = ? order by cast(quantity as real) desc", new String[]{category});
        }
        return res;
    }

    public Cursor getStarredCategory(Context context) {
        SQLiteDatabase db = this.getWritableDatabase();

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String sort = sharedPreferences.getString(UserSettings.CUSTOM_FAV_SORT, UserSettings.DATE_ASCENDING);
        settings.setCustomFavSort(sort);
        Cursor res = null;

        if (settings.getCustomFavSort().equals(UserSettings.NAME_ASCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ORDER BY description ASC", null);
        } else if (settings.getCustomFavSort().equals(UserSettings.NAME_DESCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ORDER BY description DESC", null);
        } else if (settings.getCustomFavSort().equals(UserSettings.DATE_ASCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ORDER BY _id ASC", null);
        } else if (settings.getCustomFavSort().equals(UserSettings.DATE_DESCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ORDER BY _id DESC", null);
        } else if (settings.getCustomFavSort().equals(UserSettings.PRICE_ASCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ORDER BY CAST(price AS REAL) ASC", null);
        } else if (settings.getCustomFavSort().equals(UserSettings.PRICE_DESCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ORDER BY CAST(price AS REAL) DESC", null);
        } else if (settings.getCustomFavSort().equals(UserSettings.QUANTITY_ASCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ORDER BY CAST(quantity AS REAL) ASC", null);
        } else if (settings.getCustomFavSort().equals(UserSettings.QUANTITY_DESCENDING)) {
            res = db.rawQuery("SELECT * FROM ShopTable ORDER BY CAST(quantity AS REAL) DESC", null);
        }
        return res;

    }


    public Cursor getNoteContent(String heading) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from NoteTable where heading = ?", new String[]{heading});
    }


    public Cursor getStatus(String category, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ?", new String[]{category, description});
        return cursor;
    }


    public Cursor getFavourites(String category, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ?", new String[]{category, description});
        return cursor;
    }

    public Cursor getPhotourl(String category, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ?", new String[]{category, description});
        return cursor;
    }

    public Cursor getQuantity(String category, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from ShopTable where category = ? AND description = ?", new String[]{category, description});
        return cursor;
    }

    public Cursor getPrice(String category, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from ShopTable where category = ? AND description = ?", new String[]{category, description});
    }


}
