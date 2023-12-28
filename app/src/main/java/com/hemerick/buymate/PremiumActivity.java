package com.hemerick.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList;
import com.hemerick.buymate.Database.UserSettings;
import com.hemerick.buymate.NetworkUtils.Network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.muddz.styleabletoast.StyleableToast;

public class PremiumActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    Toolbar toolbar;

    ProgressBar progressBar;
    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;

    TextView header, sub_header, tip, no_network_header, no_network_sub_header;

    TextView backupText, removeAdsText, insertImageText;
    TextView lifetime_currencyText;
    TextView lifetime_subText, prev_lifetime_currency_text;

    LinearLayout lifetime_layout, price_details_container, no_network_layout;

    CardView lifetime_card;

    ArrayList<String> product_price_list;


    ExtendedFloatingActionButton upgradeBtn;

    String selected_plan = "yearly";
    CardView backup_card, remove_ads_card, insert_image_card;

    int shadowColor;
    int colorPrimary;
    int textColor;

    String LIFETIME_PRODUCT_ID = "com.hemerick.lifetime_subscription";
    String YEARLY_PRODUCT_ID = "com.hemerick.yearly_subscription";

    String price = "";
    Boolean isPremium = false;

    private BillingClient billingClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);

        settings = new UserSettings();

        toolbar = findViewById(R.id.premuium_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PremiumActivity.super.onBackPressed();
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
        insertImageText = findViewById(R.id.insertImageText);


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
                btn_upgrade_click(getCurrentFocus());
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
        insert_image_card = findViewById(R.id.insert_image_card);
        insert_image_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInsertImageDialog();
            }
        });

        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(Network.isNetworkAvailable(PremiumActivity.this)){
                    billingClient = BillingClient.newBuilder(PremiumActivity.this)
                            .enablePendingPurchases().setListener(PremiumActivity.this).build();
                    billingClient.startConnection(new BillingClientStateListener() {
                        @Override
                        public void onBillingServiceDisconnected() {

                        }

                        @Override
                        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                            if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                ExecutorService executorService = Executors.newSingleThreadExecutor();
                                executorService.execute(() -> {
                                    try{
                                        billingClient.queryPurchasesAsync(
                                                QueryPurchasesParams.newBuilder()
                                                        .setProductType(BillingClient.ProductType.INAPP)
                                                        .build(),
                                                ((billingResult1, list) -> {
                                                    for(Purchase purchase : list){
                                                        if(purchase!=null && purchase.isAcknowledged()){
                                                            isPremium = true;
                                                        }
                                                    }
                                                }));
                                    }catch (Exception ex){
                                        isPremium = false;
                                    }
                                    runOnUiThread(() -> {
                                        try{
                                            Thread.sleep(1000);
                                        }catch (InterruptedException e){
                                            e.printStackTrace();
                                        }
                                        if(isPremium){
                                            Toast.makeText(getApplicationContext(), "Premium is enabled", Toast.LENGTH_SHORT).show();
                                            price_details_container.setVisibility(View.GONE);
                                            progressBar.setVisibility(View.INVISIBLE);

                                        }else{
                                            Toast.makeText(getApplicationContext(), "Premium is not enabled", Toast.LENGTH_SHORT).show();
                                            getPrice();
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }

                                    });

                                });
                            }
                        }
                    });
                }else{
                    no_network_layout.setVisibility(View.VISIBLE);
                    price_details_container.setVisibility(View.GONE);
                    progressBar.setVisibility(View.INVISIBLE);
                }

            }
        }, 1000);



        getColors();
        loadSharedPreferences();

    }


    public void btn_upgrade_click(View view) {
      if(billingClient.isReady()) {
          initiatePurchase();
      }
      else{
          billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
          billingClient.startConnection(new BillingClientStateListener() {
              @Override
              public void onBillingServiceDisconnected() {

              }

              @Override
              public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                        initiatePurchase();
                    }else {
                        Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
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
                        for(ProductDetails productDetails : list){
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


    public void getPrice(){
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

                        for(ProductDetails productDetails : list){

                            ImmutableList productDetailsParamsList =
                                    ImmutableList.of(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .build()
                            );
                            price  = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
                        }
                    }
                });
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                lifetime_currencyText.setText(price);
            }
        });
    }


    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

       if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null){
           for(Purchase purchase : list){
             handlePurchase(purchase);
           }
       }
       else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
           Toast.makeText(PremiumActivity.this, "ITEM_ALREADY_OWNED", Toast.LENGTH_SHORT).show();
       }
       else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED){
           Toast.makeText(PremiumActivity.this, "FEATURE_NOT_SUPPORTED", Toast.LENGTH_SHORT).show();
       }
       else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED){
           Toast.makeText(PremiumActivity.this, "USER_CANCELED", Toast.LENGTH_SHORT).show();
       }
       else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR){
           Toast.makeText(PremiumActivity.this, "DEVELOPER_ERROR", Toast.LENGTH_SHORT).show();
       }
       else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE){
           Toast.makeText(PremiumActivity.this, "ITEM_UNAVAILABLE", Toast.LENGTH_SHORT).show();
       }
       else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR){
           Toast.makeText(PremiumActivity.this, "NETWORK_ERROR", Toast.LENGTH_SHORT).show();
       }
       else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED){
           Toast.makeText(PremiumActivity.this, "SERVICE_DISCONNECTED", Toast.LENGTH_SHORT).show();
       }
       else {
           Toast.makeText(getApplicationContext(), "Error: "+billingResult.getDebugMessage(),Toast.LENGTH_SHORT ).show();
       }


    }

    private void handlePurchase(Purchase purchase) {

        if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
            if(!verifyValidSignature(purchase.getOriginalJson(),purchase.getSignature())){
                Toast.makeText(getApplicationContext(), "Error: invalid purchase", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!purchase.isAcknowledged()){
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams,acknowledgePurchaseResponseListener);
                settings.setIsLifetimePurchased(UserSettings.YES_LIFETIME_PURCHASED);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.IS_LIFETIME_PURCHASED, settings.getIsLifetimePurchased());
                editor.apply();
            }else{
                settings.setIsLifetimePurchased(UserSettings.YES_LIFETIME_PURCHASED);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.IS_LIFETIME_PURCHASED, settings.getIsLifetimePurchased());
                editor.apply();
                recreate();
            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
       //not necessary     Toast.makeText(getApplicationContext(), "UNSPECIFIED_STATE", Toast.LENGTH_SHORT).show();
        }
    }


    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                settings.setIsLifetimePurchased(UserSettings.YES_LIFETIME_PURCHASED);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.IS_LIFETIME_PURCHASED, settings.getIsLifetimePurchased());
                editor.apply();
            }
        }
    };


    private boolean verifyValidSignature(String signedData, String signature){
            try{
                String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgthYJCUO0n1jRUAZ/BUkeuV6x/2Y3OZVZhG28fbuqF32v4ZkFCG6aJW0V9NXpkRdQn5G6lf4b4Mkg8qN0eZQv2V46Uxs8134e/cTmWK7WYY+VzXX5YxHMu0eVNMI2q/xf5A3UhPTZHZjzKLJI4K8ncg9UIRO/awvjCILMr45l9tBScDmxMw7hAChbyS3bu8rmRSTT6csV9Ukf3wrTIncKbS3NRq5crm56g8RGCR5yzrYk3R+b8DZO4gsWz4sFEqdvxHAPMAh88SgwyT52oM6QuVcP/03itx9R3jje4nqJgxQTRasdkJEPUR53/05DOtklgl/8MwnBixweUBCyWinmQIDAQAB";
                return Security.verifyPurchase(base64Key, signedData, signature);
            }catch (IOException e){
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

    public void showInsertImageDialog() {
        Dialog dialog = new Dialog(PremiumActivity.this);
        dialog.setContentView(R.layout.custom_premium_insert_image_description_dialog);
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
            insertImageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));

            lifetime_subText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            prev_lifetime_currency_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));


            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));
            tip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            no_network_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            no_network_sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));


            backupText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            removeAdsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            insertImageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));

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
            insertImageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));

            lifetime_subText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            prev_lifetime_currency_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_max_text));


            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
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

        String lifetimePurchased = sharedPreferences.getString(UserSettings.IS_LIFETIME_PURCHASED, UserSettings.NO_LIFETIME_NOT_SUBSCRIBED);
        settings.setIsLifetimePurchased(lifetimePurchased);

        String firstname = sharedPreferences.getString(UserSettings.USER_NAME, UserSettings.USER_NAME_NOT_SET);
        settings.setUsername(firstname);

        updateView();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(billingClient != null){
            billingClient.endConnection();
        }
    }
}