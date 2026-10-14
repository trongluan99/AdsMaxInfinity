package com.ads.max.module;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ads.max.applovin.AppLovin;
import com.ads.max.applovin.AppLovinCallback;
import com.ads.max.billing.AppPurchase;
import com.ads.max.funtion.AdCallback;
import com.ads.max.funtion.PurchaseListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinSdk;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private MaxInterstitialAd maxInterstitialAd;
    private MaxRewardedAd maxRewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadBannerAd();
        loadNativeAd();
        preloadInterstitialAd();
        preloadRewardedAd();
    }

    private void initViews() {
        findViewById(R.id.btn_show_inter).setOnClickListener(v -> showInterstitialAd());
        findViewById(R.id.btn_show_reward).setOnClickListener(v -> showRewardedAd());
        findViewById(R.id.btn_mediation_debugger).setOnClickListener(v -> {
            AppLovinSdk.getInstance(MainActivity.this).showMediationDebugger();
        });
        findViewById(R.id.btn_test_iap).setOnClickListener(v -> {
            AppPurchase.getInstance().setPurchaseListener(new PurchaseListener() {
                @Override
                public void onProductPurchased(String productId, String transactionDetails) {
                    Toast.makeText(MainActivity.this, "Purchase success: " + productId, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void displayErrorMessage(String errorMsg) {
                    Toast.makeText(MainActivity.this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUserCancelBilling() {
                    Toast.makeText(MainActivity.this, "User cancelled billing", Toast.LENGTH_SHORT).show();
                }
            });
            AppPurchase.getInstance().subscribe(MainActivity.this, "android.test.purchased");
        });
        findViewById(R.id.btn_cmp_privacy).setOnClickListener(v -> {
            AppLovin.getInstance().showCmpForExistingUser(MainActivity.this, error -> {
                if (error == null) {
                    Toast.makeText(MainActivity.this, "Privacy settings updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "CMP: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * 1. Load Banner Ad (ở dưới đáy màn hình)
     */
    private void loadBannerAd() {
        AppLovin.getInstance().loadBanner(this, BuildConfig.ad_banner, new AdCallback() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Banner onAdLoaded");
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "Banner onAdClicked");
            }
        });
    }

    /**
     * 2. Load Native Ad (ở giữa màn hình)
     */
    private void loadNativeAd() {
        AppLovin.getInstance().loadNative(this, BuildConfig.ad_native);
    }

    /**
     * 3. Preload Interstitial Ad
     */
    private void preloadInterstitialAd() {
        maxInterstitialAd = AppLovin.getInstance().getInterstitialAds(this, BuildConfig.ad_interstitial_splash);
    }

    private void showInterstitialAd() {
        if (maxInterstitialAd != null && maxInterstitialAd.isReady()) {
            AppLovin.getInstance().forceShowInterstitial(this, maxInterstitialAd, new AdCallback() {
                @Override
                public void onAdClosed() {
                    Log.d(TAG, "Interstitial onAdClosed");
                    Toast.makeText(MainActivity.this, "Interstitial Closed", Toast.LENGTH_SHORT).show();
                    preloadInterstitialAd();
                }
            }, true);
        } else {
            Toast.makeText(this, "Interstitial not ready yet, reloading...", Toast.LENGTH_SHORT).show();
            preloadInterstitialAd();
        }
    }

    /**
     * 4. Preload & Show Rewarded Ad
     */
    private void preloadRewardedAd() {
        maxRewardedAd = AppLovin.getInstance().getRewardAd(this, BuildConfig.ad_reward, new AppLovinCallback() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Reward onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(MaxError error) {
                Log.e(TAG, "Reward onAdFailedToLoad: " + (error != null ? error.getMessage() : ""));
            }
        });
    }

    private void showRewardedAd() {
        if (maxRewardedAd != null && maxRewardedAd.isReady()) {
            AppLovin.getInstance().showRewardAd(this, maxRewardedAd, new AppLovinCallback() {
                @Override
                public void onUserRewarded(MaxReward reward) {
                    Toast.makeText(MainActivity.this, "User rewarded: " + reward.getAmount() + " " + reward.getLabel(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAdClosed() {
                    Log.d(TAG, "Reward onAdClosed");
                    preloadRewardedAd();
                }
            });
        } else {
            Toast.makeText(this, "Rewarded ad not ready yet, reloading...", Toast.LENGTH_SHORT).show();
            preloadRewardedAd();
        }
    }
}
