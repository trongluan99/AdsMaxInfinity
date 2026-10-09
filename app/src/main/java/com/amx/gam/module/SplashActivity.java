package com.amx.gam.module;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ads.amx.gam.admob.AppOpenManager;
import com.ads.amx.gam.billing.AppPurchase;
import com.ads.amx.gam.funtion.AdCallback;
import com.ads.amx.gam.funtion.PurchaseListener;
import com.amx.module.R;
import com.applovin.sdk.AppLovinSdk;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppPurchase.getInstance().setPurchaseListener(new PurchaseListener() {
            @Override
            public void onProductPurchased(String productId, String transactionDetails) {
                Log.d("LuanDev", "onProductPurchased: ");
                Toast.makeText(SplashActivity.this, "OK", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void displayErrorMessage(String errorMsg) {
                Log.d("LuanDev", "displayErrorMessage: ");
            }

            @Override
            public void onUserCancelBilling() {
                Log.d("LuanDev", "onUserCancelBilling: ");
            }
        });

        findViewById(R.id.btn_iap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppPurchase.getInstance().subscribe(SplashActivity.this, "android.test.purchased");
            }
        });

        findViewById(R.id.btn_ads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppOpenManager.getInstance().loadAdOpenSplash2id(SplashActivity.class, SplashActivity.this,
                        "ca-app-pub-3940256099942544/9257395921",
                        "ca-app-pub-3940256099942544/9257395921", 25000, new AdCallback() {
                            @Override
                            public void onNextAction() {
                                super.onNextAction();
                                Toast.makeText(SplashActivity.this, "Ads", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        AppLovinSdk.getInstance( this ).showMediationDebugger();

    }
}