package com.ads.max.module;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ads.max.applovin.AppLovinCallback;
import com.ads.max.applovin.AppOpenMax;
import com.ads.max.cmp.MaxCmpManager;
import com.applovin.sdk.AppLovinSdk;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final String TEST_DEVICE_HASHED_ID = "F74AB1433AE9EA0486D9345C12781028";

    // Set false nếu bạn muốn sau khi nhận consent thì vào thẳng Main (bỏ qua Splash Ads)
    private static final boolean SHOW_OPEN_AD_SPLASH = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initDebugViews();
        initCmpFlow();
    }

    /**
     * Bật các tiện ích debug khi chạy ở môi trường DEV
     */
    private void initDebugViews() {
        View layoutDebug = findViewById(R.id.layoutDebug);
        if (BuildConfig.env_dev) {
            layoutDebug.setVisibility(View.VISIBLE);
            TextView txtDebugInfo = findViewById(R.id.txtDebugInfo);
            txtDebugInfo.setText("DEBUG MODE | AppOpen ID: " + BuildConfig.ads_open_app);

            // Nút test ép hiển thị lại form CMP
            findViewById(R.id.btn_test_cmp_splash).setOnClickListener(v -> {
                Toast.makeText(SplashActivity.this, "Requesting CMP Form...", Toast.LENGTH_SHORT).show();
                MaxCmpManager.getInstance().requestConsent(
                        SplashActivity.this,
                        true,
                        TEST_DEVICE_HASHED_ID,
                        true,
                        formError -> {
                            if (formError != null) {
                                Toast.makeText(SplashActivity.this, "CMP Error: " + formError.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SplashActivity.this, "CMP Completed! Can request ads: " + MaxCmpManager.getInstance().canRequestAds(SplashActivity.this), Toast.LENGTH_LONG).show();
                            }
                        }
                );
            });

            // Mở nhanh Mediation Debugger tại Splash (khi cần)
            findViewById(R.id.btn_mediation_debugger_splash).setOnClickListener(v -> {
                AppLovinSdk.getInstance(SplashActivity.this).showMediationDebugger();
            });

            // Bỏ qua splash để vào thẳng Main
            findViewById(R.id.btn_skip_splash).setOnClickListener(v -> {
                startMainActivity();
            });
        } else {
            layoutDebug.setVisibility(View.GONE);
        }
    }

    /**
     * Flow chuẩn: Splash -> Show CMP -> User nhấn Consent -> Vào Main
     */
    private void initCmpFlow() {
        boolean isDebugEEA = BuildConfig.env_dev;
        // Ở bản DEV, resetConsent = true để mỗi lần mở app đều luôn hiển thị form CMP test
        boolean resetConsent = BuildConfig.env_dev;

        Log.d(TAG, "initCmpFlow: requesting consent...");
        MaxCmpManager.getInstance().requestConsent(
                SplashActivity.this,
                isDebugEEA,
                TEST_DEVICE_HASHED_ID,
                resetConsent,
                formError -> {
                    if (formError != null) {
                        Log.e(TAG, "CMP Consent error: " + formError.getMessage() + " code: " + formError.getErrorCode());
                    } else {
                        Log.d(TAG, "CMP Consent finished. Can request ads: " + MaxCmpManager.getInstance().canRequestAds(SplashActivity.this));
                    }

                    // Sau khi nhận consent:
                    if (SHOW_OPEN_AD_SPLASH && !BuildConfig.ads_open_app.isEmpty()) {
                        // Load Splash Ad với timeout ngắn (3.5s), xong ad hoặc fail là vào Main
                        loadSplashAd();
                    } else {
                        // Vào thẳng MainActivity
                        startMainActivity();
                    }
                }
        );
    }

    private void loadSplashAd() {
        Log.d(TAG, "loadSplashAd: id=" + BuildConfig.ads_open_app);
        AppOpenMax.getInstance().loadOpenMaxSplash(
                SplashActivity.this,
                BuildConfig.ads_open_app,
                0,
                3500, // Timeout 3.5s để user không phải đợi lâu nếu ads no-fill
                true,
                new AppLovinCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        startMainActivity();
                    }
                }
        );
    }

    private void startMainActivity() {
        if (!isFinishing()) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}