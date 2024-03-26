package com.henrychinedu.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList;
import com.henrychinedu.buymate.Database.UserSettings;
import com.henrychinedu.buymate.NetworkUtils.ConnectivityUtils;
import com.henrychinedu.buymate.NetworkUtils.Security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PremiumActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;

    ProgressBar progressBar;
    private UserSettings settings;

    TextView header, sub_header, tip, no_network_header, no_network_sub_header;

    TextView backupText, removeAdsText, supportText;
    TextView lifetime_currencyText;
    TextView lifetime_subText, prev_lifetime_currency_text;

    LinearLayout price_details_container;
    LinearLayout no_network_layout;

    CardView lifetime_card;

    ArrayList<String> product_price_list;


    ExtendedFloatingActionButton upgradeBtn;

    CardView backup_card, remove_ads_card, support_card;

    int shadowColor;
    int colorPrimary;
    int textColor;

    String LIFETIME_PRODUCT_ID;

    String price = "";
    Boolean isPremium = false;

    private BillingClient billingClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new UserSettings();
        SharedPreferences sharedPreferences_theme = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);
        String theme = sharedPreferences_theme.getString(UserSettings.CUSTOM_THEME, UserSettings.LIGHT_THEME);
        settings.setCustomTheme(theme);
        String dim = sharedPreferences_theme.getString(UserSettings.IS_DIM_THEME_ENABLED, UserSettings.NO_DIM_THEME_NOT_ENABLED);
        settings.setIsDimThemeEnabled(dim);

        LIFETIME_PRODUCT_ID = getString(R.string.app_lifetime_product_id);

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

        setContentView(R.layout.activity_premium);


        toolbar = findViewById(R.id.premuium_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PremiumActivity.super.onBackPressed();
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                recreate();
            }
        });

        progressBar = findViewById(R.id.progress_bar);

        header = findViewById(R.id.header);
        sub_header = findViewById(R.id.sub_header);
        tip = findViewById(R.id.tip);

        no_network_header = findViewById(R.id.no_network_header);
        no_network_sub_header = findViewById(R.id.no_network_sub_header);


        backupText = findViewById(R.id.backupText);
        removeAdsText = findViewById(R.id.removeAdsText);
        supportText = findViewById(R.id.supportText);


        product_price_list = new ArrayList<>();

        lifetime_card = findViewById(R.id.lifetime_card);


        lifetime_currencyText = findViewById(R.id.lifetime_currency_text);


        lifetime_subText = findViewById(R.id.lifetime_sub_text);
        prev_lifetime_currency_text = findViewById(R.id.prev_lifetime_currency_text);

        price_details_container = findViewById(R.id.price_details_container);
        no_network_layout = findViewById(R.id.no_network_layout);


        upgradeBtn = findViewById(R.id.upgrade_btn);
        upgradeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            progressBar.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    btn_upgrade_click(getCurrentFocus());
                    }
                }, 2000);

            }
        });


        backup_card = findViewById(R.id.backup_card);
        backup_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBackupDialog();
            }
        });
        remove_ads_card = findViewById(R.id.remove_ads_card);
        remove_ads_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveAdsDialog();
            }
        });

        support_card = findViewById(R.id.support_card);
        support_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSupportDialog();
            }
        });

        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
                ConnectivityUtils.checkInternetConnectivity(PremiumActivity.this, new ConnectivityUtils.InternetCheckListener() {
                    @Override
                    public void onInternetCheckComplete(boolean isInternetAvailable) {
                        if (isInternetAvailable) {
                            progressBar.setVisibility(View.GONE);
                            billingClient = BillingClient.newBuilder(PremiumActivity.this)
                                    .enablePendingPurchases().setListener(PremiumActivity.this).build();
                            billingClient.startConnection(new BillingClientStateListener() {
                                @Override
                                public void onBillingServiceDisconnected() {

                                }

                                @Override
                                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                                        executorService.execute(() -> {
                                            try {
                                                billingClient.queryPurchasesAsync(
                                                        QueryPurchasesParams.newBuilder()
                                                                .setProductType(BillingClient.ProductType.INAPP)
                                                                .build(),
                                                        ((billingResult1, list) -> {
                                                            for (Purchase purchase : list) {
                                                                if (purchase != null && purchase.isAcknowledged()) {
                                                                    isPremium = true;
                                                                }
                                                            }
                                                        }));
                                            } catch (Exception ex) {
                                                isPremium = false;
                                            }
                                            runOnUiThread(() -> {
                                                try {
                                                    Thread.sleep(1000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                if (isPremium) {
                                                    price_details_container.setVisibility(View.GONE);
                                                    progressBar.setVisibility(View.INVISIBLE);

                                                } else {
                                                    getPrice();
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }

                                            });

                                        });
                                    }
                                }
                            });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            no_network_layout.setVisibility(View.VISIBLE);
                            price_details_container.setVisibility(View.GONE);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        }, 1000);


        getColors();
        loadSharedPreferences();

    }


    public void btn_upgrade_click(View view) {
        if (billingClient.isReady()) {
            initiatePurchase();
        } else {
            billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingServiceDisconnected() {

                }

                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.PaymentSuccessfulActivity__error) + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void initiatePurchase() {
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder().setProductList(
                                ImmutableList.of(QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(LIFETIME_PRODUCT_ID)
                                        .setProductType(BillingClient.ProductType.INAPP)
                                        .build()))
                        .build();

        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                new ProductDetailsResponseListener() {
                    @Override
                    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                        for (ProductDetails productDetails : list) {
                            ImmutableList productDetailsList = ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .build()
                            );
                            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsList)
                                    .build();
                            billingClient.launchBillingFlow(PremiumActivity.this, billingFlowParams);
                        }
                    }
                });
    }


    public void getPrice() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(
                                ImmutableList.of(QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(LIFETIME_PRODUCT_ID)
                                        .setProductType(BillingClient.ProductType.INAPP)
                                        .build()))
                        .build();

                billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
                    @Override
                    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {

                        for (ProductDetails productDetails : list) {

                            ImmutableList productDetailsParamsList =
                                    ImmutableList.of(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetails)
                                                    .build()
                                    );
                            price = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
                        }
                    }
                });
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lifetime_currencyText.setText(price);
            }
        });
    }


    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
            for (Purchase purchase : list) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Toast.makeText(PremiumActivity.this, getString(R.string.PaymentSuccessfulActivity__itemOwnedAlready), Toast.LENGTH_SHORT).show();
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
            Toast.makeText(PremiumActivity.this, getString(R.string.PaymentSuccessfulActivity__featureNotSupported), Toast.LENGTH_SHORT).show();
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(PremiumActivity.this, getString(R.string.PaymentSuccessfulActivity__userCancelled), Toast.LENGTH_SHORT).show();
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
            Toast.makeText(PremiumActivity.this, getString(R.string.PaymentSuccessfulActivity__developerError), Toast.LENGTH_SHORT).show();
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
            Toast.makeText(PremiumActivity.this, getString(R.string.PaymentSuccessfulActivity__itemUnavailable), Toast.LENGTH_SHORT).show();
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR) {
            Toast.makeText(PremiumActivity.this, getString(R.string.PaymentSuccessfulActivity__networkError), Toast.LENGTH_SHORT).show();
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
            Toast.makeText(PremiumActivity.this, getString(R.string.PaymentSuccessfulActivity__serviceDisconnected), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.PaymentSuccessfulActivity__error) + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private void handlePurchase(Purchase purchase) {

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                Toast.makeText(getApplicationContext(), getString(R.string.PaymentSuccessfulActivity__errorInvalidPurchase), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                settings.setIsLifetimePurchased(UserSettings.YES_LIFETIME_PURCHASED);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.IS_LIFETIME_PURCHASED, settings.getIsLifetimePurchased());
                editor.apply();
            } else {
                settings.setIsLifetimePurchased(UserSettings.YES_LIFETIME_PURCHASED);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.IS_LIFETIME_PURCHASED, settings.getIsLifetimePurchased());
                editor.apply();
                recreate();
            }
        }
    }


    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                settings.setIsLifetimePurchased(UserSettings.YES_LIFETIME_PURCHASED);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.IS_LIFETIME_PURCHASED, settings.getIsLifetimePurchased());
                editor.apply();
            }
        }
    };


    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq7CHHtgU2ryVQhB+lmi7R72LFw3JTdMZhrndfzq8DOwfdokR0eQo76iPDasSB22SFqAK1yg7DP5/NycITq3XryjKXYl3OBAujLR25hqvm0ozRJBh9TNeMYSSElzgs765Us0DHDWWGOKrrYIeYN/lJt8xGh4xXhBxURdJQOhRCtmKOAgufQLZYoTv4CB3l9NfcT+2vHdYU/L74mk57kOJqttgu9A0xVjLM3yFK5hOpsIiOS6TIFk7BDufEvRuqtQZWOL4ncwqhdagfuDLB9pnR7KEhd3ea9swmAzlSJ101vNYgAenGrGNukRe08mZ96rOSy9/UQRKJetyIIUsKMVgKQIDAQAB";
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }


    public void getColors() {
        TypedValue typedValue1 = new TypedValue();
        TypedValue typedValue2 = new TypedValue();
        TypedValue typedValue3 = new TypedValue();

        PremiumActivity.this.getTheme().resolveAttribute(androidx.appcompat.R.attr.color, typedValue1, true);
        shadowColor = typedValue1.data;

        PremiumActivity.this.getTheme().resolveAttribute(com.chaos.view.R.attr.colorPrimary, typedValue2, true);
        colorPrimary = typedValue2.data;

        PremiumActivity.this.getTheme().resolveAttribute(androidx.constraintlayout.widget.R.attr.textFillColor, typedValue3, true);
        textColor = typedValue3.data;
    }


    public void showBackupDialog() {
        Dialog dialog = new Dialog(PremiumActivity.this);
        dialog.setContentView(R.layout.custom_premium_backup_description_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.header);
        TextView sub_header = dialog.findViewById(R.id.sub_header);
        Button okBtn = dialog.findViewById(R.id.okBtn);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showRemoveAdsDialog() {
        Dialog dialog = new Dialog(PremiumActivity.this);
        dialog.setContentView(R.layout.custom_premium_remove_ads_description_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.header);
        TextView sub_header = dialog.findViewById(R.id.sub_header);
        Button okBtn = dialog.findViewById(R.id.okBtn);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    public void showSupportDialog() {
        Dialog dialog = new Dialog(PremiumActivity.this);
        dialog.setContentView(R.layout.custom_premium_support_description_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_transparent_curved_rectangle_2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView header = dialog.findViewById(R.id.header);
        TextView sub_header = dialog.findViewById(R.id.sub_header);
        Button okBtn = dialog.findViewById(R.id.okBtn);


        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void updateView() {

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_SMALL)) {

            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            tip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));
            no_network_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            no_network_sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

            backupText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));
            removeAdsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));
            supportText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));

            lifetime_subText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            prev_lifetime_currency_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));


            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));
            tip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            no_network_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            no_network_sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));


            backupText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            removeAdsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            supportText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));

            lifetime_subText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            prev_lifetime_currency_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));

            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_max_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_max_text));
            tip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            no_network_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            no_network_sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));


            backupText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            removeAdsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            supportText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));

            lifetime_subText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            prev_lifetime_currency_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_max_text));


            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
        }
    }

    private void loadSharedPreferences() {

        boolean wakeLockEnabled = UserSettings.isWakeLockEnabled(this);
        if (wakeLockEnabled) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:KeepScreeOn");
            wakeLock.acquire();
        }

        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE);


        String textSize = sharedPreferences.getString(UserSettings.CUSTOM_TEXT_SIZE, UserSettings.TEXT_MEDIUM);
        settings.setCustomTextSize(textSize);

        String lifetimePurchased = sharedPreferences.getString(UserSettings.IS_LIFETIME_PURCHASED, UserSettings.NO_LIFETIME_NOT_SUBSCRIBED);
        settings.setIsLifetimePurchased(lifetimePurchased);

        String firstname = sharedPreferences.getString(UserSettings.USER_NAME, UserSettings.USER_NAME_NOT_SET);
        settings.setUsername(firstname);

        updateView();
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}