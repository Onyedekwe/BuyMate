package com.hemerick.buymate.Database;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class UserSettings extends Application {
    public static final String PREFERENCES = "preferences";

    //theme
    public static final String CUSTOM_THEME = "customTheme";
    public static final String DEFAULT_THEME = "defaultTheme";
    public static final String LIGHT_THEME = "lightTheme";
    public static final String DARK_THEME = "darkTheme";
    private String customTheme;

    //keepScreenBright
    public static final String WAKE_LOCK_ENABLED = "wakeLockEnabled";


    //RecyclerViewLayout
    public static final String GRID_LAYOUT_ENABLED = "isGrid";

    //sort
    public static final String CUSTOM_SORT = "customSort";
    public static final String CUSTOM_FAV_SORT = "customFavSort";
    public static final String CUSTOM_CATEGORY_SORT = "customCategorySort";
    public static final String NAME_ASCENDING = "nameAscending";
    public static final String NAME_DESCENDING = "nameDescending";
    public static final String DATE_ASCENDING = "dateAscending";
    public static final String DATE_DESCENDING = "dateDescending";
    public static final String PRICE_ASCENDING = "priceAscending";
    public static final String PRICE_DESCENDING = "priceDescending";
    public static final String QUANTITY_ASCENDING = "quantityAscending";
    public static final String QUANTITY_DESCENDING = "quantityDescending";

    //textsize
    public static final String CUSTOM_TEXT_SIZE = "customTextSize";
    public static final String TEXT_SMALL = "textSmall";
    public static final String TEXT_MEDIUM = "textMedium";
    public static final String TEXT_LARGE = "textLarge";


    //swipeaction
    public static final String CUSTOM_LEFT_SWIPE_ACTION = "customLeftSwipeAction";
    public static final String CUSTOM_RIGHT_SWIPE_ACTION = "customRightSwipeAction";
    public static final String SWIPE_RENAME = "swipeRename";
    public static final String SWIPE_CHANGE_PRICE = "swipeChangePrice";
    public static final String SWIPE_CHANGE_QUANTITY = "swipeChangeQuantity";
    public static final String SWIPE_STAR_ITEM = "swipeStarItem";
    public static final String SWIPE_DELETE_ITEM = "swipeDeleteItem";
    public static final String SWIPE_SHOW_OPTIONS = "swipeShowOptions";
    public static final String SWIPE_CHECK = "swipeCheck";
    public static final String SWIPE_DO_NOTHING = "swipeDoNothing";


    public static final String IS_SWIPE_DISABLED = "isSwipeDisabled";
    public static final String YES_DISABLED = "yesDisabled";
    public static final String NOT_DISABLED = "notDisabled";


    public static final String IS_SHARE_PRICE_DISABLED = "isSharePriceDisabled";
    public static final String YES_SHARE_PRICE_DISABLED = "yesSharePriceDisabled";
    public static final String NO_SHARE_PRICE_NOT_DISABLED = "noSharePriceNotDisabled";

    public static final String IS_SHARE_QUANTITY_DISABLED = "isShareQuantityDisabled";
    public static final String YES_SHARE_QUANTITY_DISABLED = "yesShareQuantityDisabled";
    public static final String NO_SHARE_QUANTITY_NOT_DISABLED = "noShareQuantityNotDisabled";

    public static final String IS_SHARE_TOTAL_DISABLED = "isShareTotalDisabled";
    public static final String YES_SHARE_TOTAL_DISABLED = "yesShareTotalDisabled";
    public static final String NO_SHARE_TOTAL_NOT_DISABLED = "noShareTotalNotDisabled";


    //multiply_Layout
    public static final String IS_MULTIPLY_DISABLED = "isMultiplyDisabled";
    public static final String YES_MULTIPLY_DISABLED = "yesMultiplyDisabled";
    public static final String NO_MULTIPLY_NOT_DISABLED = "noMultiplyNotDisabled";

    public static final String IS_ITEM_EYE_DISABLED = "isItemEyeDisabled";
    public static final String YES_ITEM_EYE_DISABLED = "yesItemEyeDisabled";
    public static final String NO_ITEM_EYE_NOT_DISABLED = "noItemEyeNotDisabled";

    public static final String IS_FAV_EYE_DISABLED = "isFavEyeDisabled";
    public static final String YES_FAV_EYE_DISABLED = "yesFavEyeDisabled";
    public static final String NO_FAV_EYE_NOT_DISABLED = "noFavEyeNotDisabled";


    public static final String IS_CROSS_DISABLED = "isCrossDisabled";
    public static final String YES_CROSS_DISABLED = "yesCrossDisabled";
    public static final String NO_CROSS_NOT_DISABLED = "noCrossNotDisabled";

    public static final String CURRENCY = "currency";
    public static final String CURRENCY_DOLLAR = "$";

    public static final String PASSWORD = "password";
    public static final String NOT_SET_PASSWORD = "notSetPassword";


    public static final String IS_FINGERPRINT_DISABLED = "isFingerPrintDisabled";
    public static final String YES_FINGERPRINT_DISABLED = "yesFingerPrintDisabled";
    public static final String NO_FINGERPRINT_NOT_DISABLED = "noFingerPrintNotDisabled";
    private String isFingerPrintDisabled;


    public static final String IS_PRICE_DISABLED = "isPriceDisabled";
    public static final String YES_PRICE_DISABLED = "yesPriceDisabled";
    public static final String NO_PRICE_NOT_DISABLED = "noPriceNotDisabled";
    private String isPriceDisabled;

    public static final String IS_PREMIUM_SUBSCRIBED = "isPremiumSubscribed";
    public static final String YES_SUBSCRIBED = "yesSubscribed";
    public static final String NOT_SUBSCRIBED = "notSubscribed";
    private String isPremiumSubscribed;

    public String getIsPremiumSubscribed() {
        return isPremiumSubscribed;
    }


    public void setIsPremiumSubscribed(String isPremiumSubscribed) {
        this.isPremiumSubscribed = isPremiumSubscribed;
    }

    public static final String IS_LIFETIME_PURCHASED = "isLifetimePurchased";
        public static final String YES_LIFETIME_PURCHASED = "yesLifetimePurchased";
    public static final String NO_LIFETIME_NOT_SUBSCRIBED = "noLifetimeNotPurchased";
    private String isLifetimePurchased;

    public String getIsLifetimePurchased() {
        return isLifetimePurchased;
    }

    public void setIsLifetimePurchased(String isLifetimePurchased) {
        this.isLifetimePurchased = isLifetimePurchased;
    }

    public static final String IS_AUTHENTICATED = "isAuthenticated";
    public static final String YES_AUTHENTICATED = "yesAuthenticated";
    public static final String NOT_AUTHENTICATED = "notAuthenticated";
    private String isAuthenticated;





    public String getIsAuthenticated() {
        return isAuthenticated;
    }

    public void setIsAuthenticated(String isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }



    public static final String USER_NAME = "userName";
    public static final String USER_NAME_NOT_SET = "userNameNotSet";
    private String username;


    private Boolean isFirstStart = true;

    private String customSort;
    private String customFavSort;
    private String customCategorySort;

    private String customTextSize;

    private String customLeftSwipeAction;
    private String customRightSwipeAction;

    private String isSwipeDisabled;

    private String isSharePriceDisabled;
    private String isShareQuantityDisabled;
    private String IsShareTotalDisabled;

    private String IsMultiplyDisabled;

    private String IsItemEyeDisabled;
    private String IsFavEyeDisabled;

    private String IsCrossDisabled;

    private String currency;
    private String password;


    public static void setGridLayoutEnabled(Context context, boolean enabled) {
        SharedPreferences preference = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean(GRID_LAYOUT_ENABLED, enabled);
        editor.apply();

    }

    public static boolean isGridLockEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getBoolean(GRID_LAYOUT_ENABLED, false);
    }

    public static void setWakeLockEnabled(Context context, boolean enabled) {
        SharedPreferences preference = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean(WAKE_LOCK_ENABLED, enabled);
        editor.apply();
    }

    public static boolean isWakeLockEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getBoolean(WAKE_LOCK_ENABLED, false);
    }


    public Boolean getFirstStart() {
        return isFirstStart;
    }

    public void setFirstStart(Boolean firstStart) {
        isFirstStart = firstStart;
    }


    public String getCustomTheme() {
        return customTheme;
    }

    public void setCustomTheme(String customTheme) {
        this.customTheme = customTheme;
    }


    public String getCustomSort() {
        return customSort;
    }

    public void setCustomSort(String customSort) {
        this.customSort = customSort;
    }

    public String getCustomFavSort() {
        return customFavSort;
    }

    public void setCustomFavSort(String customFavSort) {
        this.customFavSort = customFavSort;
    }

    public String getCustomCategorySort() {
        return customCategorySort;
    }

    public void setCustomCategorySort(String customCategorySort) {
        this.customCategorySort = customCategorySort;
    }

    public String getCustomTextSize() {
        return customTextSize;
    }

    public void setCustomTextSize(String customTextSize) {
        this.customTextSize = customTextSize;
    }

    public String getCustomLeftSwipeAction() {
        return customLeftSwipeAction;
    }

    public void setCustomLeftSwipeAction(String customLeftSwipeAction) {
        this.customLeftSwipeAction = customLeftSwipeAction;
    }

    public String getCustomRightSwipeAction() {
        return customRightSwipeAction;
    }

    public void setCustomRightSwipeAction(String customRightSwipeAction) {
        this.customRightSwipeAction = customRightSwipeAction;
    }

    public String getIsSwipeDisabled() {
        return isSwipeDisabled;
    }

    public void setIsSwipeDisabled(String isSwipeDisabled) {
        this.isSwipeDisabled = isSwipeDisabled;
    }

    public String getIsSharePriceDisabled() {
        return isSharePriceDisabled;
    }

    public void setIsSharePriceDisabled(String isSharePriceDisabled) {
        this.isSharePriceDisabled = isSharePriceDisabled;
    }

    public String getIsShareQuantityDisabled() {
        return isShareQuantityDisabled;
    }

    public void setIsShareQuantityDisabled(String isShareQuantityDisabled) {
        this.isShareQuantityDisabled = isShareQuantityDisabled;
    }

    public String getIsShareTotalDisabled() {
        return IsShareTotalDisabled;
    }

    public void setIsShareTotalDisabled(String isShareTotalDisabled) {
        IsShareTotalDisabled = isShareTotalDisabled;
    }

    public String getIsMultiplyDisabled() {
        return IsMultiplyDisabled;
    }

    public void setIsMultiplyDisabled(String isMultiplyDisabled) {
        IsMultiplyDisabled = isMultiplyDisabled;
    }

    public String getIsItemEyeDisabled() {
        return IsItemEyeDisabled;
    }

    public void setIsItemEyeDisabled(String isItemEyeDisabled) {
        IsItemEyeDisabled = isItemEyeDisabled;
    }

    public String getIsFavEyeDisabled() {
        return IsFavEyeDisabled;
    }

    public void setIsFavEyeDisabled(String isFavEyeDisabled) {
        IsFavEyeDisabled = isFavEyeDisabled;
    }

    public String getIsCrossDisabled() {
        return IsCrossDisabled;
    }

    public void setIsCrossDisabled(String isCrossDisabled) {
        IsCrossDisabled = isCrossDisabled;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIsFingerPrintDisabled() {
        return isFingerPrintDisabled;
    }

    public void setIsFingerPrintDisabled(String isFingerPrintDisabled) {
        this.isFingerPrintDisabled = isFingerPrintDisabled;
    }

    public String getIsPriceDisabled() {
        return isPriceDisabled;
    }

    public void setIsPriceDisabled(String isPriceDisabled) {
        this.isPriceDisabled = isPriceDisabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
