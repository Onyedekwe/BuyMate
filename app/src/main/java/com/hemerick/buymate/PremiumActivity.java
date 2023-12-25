package com.hemerick.buymate;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.muddz.styleabletoast.StyleableToast;

public class PremiumActivity extends AppCompatActivity {

    boolean isPremium = false;
    String currently_subscribed = "";


    Toolbar toolbar;
    private UserSettings settings;
    private PowerManager.WakeLock wakeLock;

    TextView header, sub_header, tip, no_network_header, no_network_sub_header, already_subscribed_header, already_subscribed_sub_header;

    TextView backupText, removeAdsText, insertImageText;
    TextView lifetime_currencyText, yearly_currencyText;
    TextView lifetime_subText, yearly_subText;

    LinearLayout lifetime_layout, yearly_layout, price_details_container, no_network_layout, already_sunscribed_layout;

    CardView lifetime_card, yearly_card;

    ArrayList<String> product_price_list;


    ExtendedFloatingActionButton upgradeBtn;

    String selected_plan = "yearly";
    CardView backup_card, remove_ads_card, insert_image_card;

    int shadowColor;
    int colorPrimary;
    int textColor;

    private BillingClient billingClient;

    boolean isSuccess = false;

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

        header = findViewById(R.id.header);
        sub_header = findViewById(R.id.sub_header);
        tip = findViewById(R.id.tip);

        no_network_header = findViewById(R.id.no_network_header);
        no_network_sub_header = findViewById(R.id.no_network_sub_header);

        already_subscribed_header = findViewById(R.id.already_subscribed_header);
        already_subscribed_sub_header = findViewById(R.id.already_subscribed_sub_header);


        backupText = findViewById(R.id.backupText);
        removeAdsText = findViewById(R.id.removeAdsText);
        insertImageText = findViewById(R.id.insertImageText);


        product_price_list = new ArrayList<>();

        lifetime_card = findViewById(R.id.lifetime_card);
        yearly_card = findViewById(R.id.yearly_card);


        lifetime_currencyText = findViewById(R.id.lifetime_currency_text);
        yearly_currencyText = findViewById(R.id.yearly_currency_text);


        lifetime_subText = findViewById(R.id.lifetime_sub_text);
        yearly_subText = findViewById(R.id.yearly_sub_text);

        lifetime_layout = findViewById(R.id.lifetime_layout);
        yearly_layout = findViewById(R.id.yearly_layout);

        price_details_container = findViewById(R.id.price_details_container);
        no_network_layout = findViewById(R.id.no_network_layout);

        already_sunscribed_layout = findViewById(R.id.already_subscribed_layout);


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


        lifetime_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                yearly_card.setCardBackgroundColor(Color.TRANSPARENT);

                yearly_layout.setBackgroundColor(shadowColor);
                yearly_subText.setTextColor(textColor);


                lifetime_card.setCardBackgroundColor(colorPrimary);
                lifetime_layout.setBackgroundColor(colorPrimary);
                lifetime_subText.setTextColor(Color.WHITE);
                selected_plan = "lifetime";
            }
        });

        yearly_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lifetime_card.setCardBackgroundColor(Color.TRANSPARENT);


                lifetime_layout.setBackgroundColor(shadowColor);
                lifetime_subText.setTextColor(textColor);

                yearly_card.setCardBackgroundColor(colorPrimary);
                yearly_layout.setBackgroundColor(colorPrimary);
                yearly_subText.setTextColor(Color.WHITE);
                selected_plan = "yearly";
            }
        });

        getColors();
        loadSharedPreferences();

        billingClient = BillingClient.newBuilder(PremiumActivity.this).setListener(purchasesUpdatedListener).enablePendingPurchases().build();

        if (Network.isNetworkAvailable(PremiumActivity.this)) {
            if (settings.getIsLifetimePurchased().equals(UserSettings.YES_LIFETIME_PURCHASED)) {
                price_details_container.setVisibility(View.GONE);
                no_network_layout.setVisibility(View.GONE);
                informLifetimePayment();
                already_sunscribed_layout.setVisibility(View.VISIBLE);
            } else {
                checkIfSubscribed();
            }
        } else {
            price_details_container.setVisibility(View.GONE);
            no_network_layout.setVisibility(View.VISIBLE);
        }



    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                for (Purchase purchase : list) {
                    if (purchase.getProducts().contains("com.hemerick.yearly_subscription")) {
                        handlePurchase(purchase);
                    } else if (purchase.getProducts().contains("com.hemerick.lifetime_subscription")) {
                        grantLifetimeAccess(purchase);
                    }


                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {

                isSuccess = true;
                ConnectionClass.premium = true;
                ConnectionClass.locked = false;

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
                //textview.setText("Feature Not Supported")
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                //textview.setText("Billing_unavailable")
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                //textview.setText("User Cancelled")
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
                //textview.setText("Developer Error")
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                //textview.setText("ITEM UNAVAILABLE")
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR) {
                //textview.setText("NETWORK ERROR")
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                //textview.setText("Service_Disconnected")
            } else {
                Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };


    void handlePurchase(final Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                }
            }
        };
        billingClient.consumeAsync(consumeParams, listener);
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                Toast.makeText(getApplicationContext(), "Error : Invalid Purchase", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);

                price_details_container.setVisibility(View.GONE);
                no_network_layout.setVisibility(View.GONE);
                informYearlySubscription();
                already_sunscribed_layout.setVisibility(View.VISIBLE);

                //textview.setText("Subscribed")
            }
            ConnectionClass.premium = true;
            ConnectionClass.locked = false;
            //textview.setVisibility(Gone);
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            //textview.setText("Subscrption pending")
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            //textview.setText("Unspecified State")

        }
    }


    private void grantLifetimeAccess(final Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                Toast.makeText(getApplicationContext(), "Error : Invalid Purchase", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);

                settings.setIsLifetimePurchased(UserSettings.YES_LIFETIME_PURCHASED);
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(UserSettings.IS_LIFETIME_PURCHASED, settings.getIsLifetimePurchased());
                editor.apply();

                price_details_container.setVisibility(View.GONE);
                no_network_layout.setVisibility(View.GONE);
                informLifetimePayment();
                already_sunscribed_layout.setVisibility(View.VISIBLE);

            }
            // Grant lifetime access
            ConnectionClass.premium = true;
            ConnectionClass.locked = false;
            //textview.setVisibility(Gone);
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            //textview.setText("Subscrption pending")
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            //textview.setText("Unspecified State")
        }

    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //textview.setText("Subscribed");
                isSuccess = true;
                ConnectionClass.premium = true;
                ConnectionClass.locked = false;
            }
        }
    };


    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgthYJCUO0n1jRUAZ/BUkeuV6x/2Y3OZVZhG28fbuqF32v4ZkFCG6aJW0V9NXpkRdQn5G6lf4b4Mkg8qN0eZQv2V46Uxs8134e/cTmWK7WYY+VzXX5YxHMu0eVNMI2q/xf5A3UhPTZHZjzKLJI4K8ncg9UIRO/awvjCILMr45l9tBScDmxMw7hAChbyS3bu8rmRSTT6csV9Ukf3wrTIncKbS3NRq5crm56g8RGCR5yzrYk3R+b8DZO4gsWz4sFEqdvxHAPMAh88SgwyT52oM6QuVcP/03itx9R3jje4nqJgxQTRasdkJEPUR53/05DOtklgl/8MwnBixweUBCyWinmQIDAQAB";
            return Security.verifyPurchase(base64Key.trim(), signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    private void getSubPrice() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {

                            List<QueryProductDetailsParams.Product> productList = new ArrayList<>();

                            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder().setProductId("com.hemerick.yearly_subscription").setProductType(BillingClient.ProductType.SUBS).build();
                            productList.add(product);


                            QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(productList).build();

                            billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
                                @Override
                                public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                                    // Handle the response containing details of the queried subscription product(s).

                                    // Process details for each subscription product
                                    for (ProductDetails productDetails : list) {
                                        String formattedPrice = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                                        product_price_list.add(formattedPrice);
                                        // Print or use the extracted information as needed

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
                            for (int i = 0; i < product_price_list.size(); i++) {

                                yearly_currencyText.setText(product_price_list.get(i));
                                getOneTimePrice();

                            }
                        }
                    });
                }
            }
        });
    }

    private void getOneTimePrice() {
        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId("com.hemerick.lifetime_subscription").setProductType(BillingClient.ProductType.INAPP).build())).build();

        billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                for (ProductDetails productDetails : list) {
                    String formattedPrice = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
                    lifetime_currencyText.setText(formattedPrice);
                }
            }
        });
    }

    public void btn_upgrade_click(View view) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (selected_plan.equals("yearly")) {

                    String productId = "com.hemerick.yearly_subscription";
                    QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(BillingClient.ProductType.SUBS).build())).build();
                    billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
                        @Override
                        public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                            for (ProductDetails productDetails : list) {
                                String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
                                ImmutableList productDetailsParamsList = ImmutableList.of(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).setOfferToken(offerToken).build());
                                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build();
                                billingClient.launchBillingFlow(PremiumActivity.this, billingFlowParams);
                            }
                        }
                    });
                } else if (selected_plan.equals("lifetime")) {
                    String productId = "com.hemerick.lifetime_subscription";
                    QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(BillingClient.ProductType.INAPP).build())).build();
                    billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
                        @Override
                        public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                            for (ProductDetails productDetails : list) {
                                ImmutableList productDetailsParamsList = ImmutableList.of(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).build());
                                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build();
                                billingClient.launchBillingFlow(PremiumActivity.this, billingFlowParams);
                            }
                        }
                    });
                }
            }
        });
    }

    private void checkIfSubscribed(){

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), new PurchasesResponseListener() {
                        @Override
                        public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {

                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                boolean isSubscribedToYearlyPlan = false;
                                for (Purchase purchase : list) {
                                    if (purchase.getOrderId().equals("com.hemerick.yearly_subscription") && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                        isSubscribedToYearlyPlan = true;
                                        break;
                                    }
                                }

                                // Proceed based on subscription status
                                if (isSubscribedToYearlyPlan) {
                                    price_details_container.setVisibility(View.GONE);
                                    no_network_layout.setVisibility(View.GONE);
                                    informYearlySubscription();
                                    already_sunscribed_layout.setVisibility(View.VISIBLE);
                                } else {
                                    getSubPrice();
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Handle billing service disconnections
            }
        });
    }

    private void informYearlySubscription(){
        String user_name = settings.getUsername();

        String headerText = "Thanks for sticking with us, " + user_name + "!";
        String subheaderText = "Your yearly support means the world!\nYour access to all the premium features is active.";

        already_subscribed_header.setText(headerText);
        already_subscribed_sub_header.setText(subheaderText);
    }

    private void informLifetimePayment(){
        String user_name = settings.getUsername();
        String app_name = getString(R.string.app_name);

        String headerText = "Thanks for joining the " +app_name+ " Lifetime Club!";
        String subheaderText = "Your access to everything is permanently unlocked.";

        already_subscribed_header.setText(headerText);
        already_subscribed_sub_header.setText(subheaderText);
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
            already_subscribed_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            already_subscribed_sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

            backupText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));
            removeAdsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));
            insertImageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mini_text));

            lifetime_subText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));
            yearly_subText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));


            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_text));

        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_MEDIUM)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_text));
            tip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            no_network_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            no_network_sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            already_subscribed_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            already_subscribed_sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

            backupText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            removeAdsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));
            insertImageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.maxi_text));

            lifetime_subText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
            yearly_subText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));

            upgradeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.medium_text));
        }

        if (settings.getCustomTextSize().equals(UserSettings.TEXT_LARGE)) {
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_max_text));
            sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_max_max_text));
            tip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            no_network_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            no_network_sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            already_subscribed_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            already_subscribed_sub_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

            backupText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            removeAdsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));
            insertImageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.max_max_text));

            lifetime_subText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));
            yearly_subText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.large_text));

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

    public void quit_click(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}