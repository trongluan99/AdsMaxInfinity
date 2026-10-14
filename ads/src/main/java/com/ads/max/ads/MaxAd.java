package com.ads.max.ads;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEventFailure;
import com.adjust.sdk.AdjustEventSuccess;
import com.adjust.sdk.AdjustSessionFailure;
import com.adjust.sdk.AdjustSessionSuccess;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;
import com.ads.max.applovin.AppLovin;
import com.ads.max.applovin.AppLovinCallback;
import com.ads.max.applovin.AppOpenMax;
import com.ads.max.config.MaxAdConfig;
import com.ads.max.event.MaxAdjust;
import com.ads.max.event.MaxAppsflyer;
import com.ads.max.util.AppUtil;
import com.facebook.FacebookSdk;

public class MaxAd {
    public static final String TAG_ADJUST = "MaxAdjust";
    public static final String TAG = "MaxAd";
    private static volatile MaxAd INSTANCE;
    private MaxAdConfig adConfig;
    private MaxInitCallback initCallback;
    private Boolean initAdSuccess = false;

    public static synchronized MaxAd getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MaxAd();
        }
        return INSTANCE;
    }

    /**
     * Set count click to show ads interstitial when call showInterstitialAdByTimes()
     *
     * @param countClickToShowAds - default = 3
     */
    public void setCountClickToShowAds(int countClickToShowAds) {
        AppLovin.getInstance().setNumShowAds(countClickToShowAds);
    }

    /**
     * Set count click to show ads interstitial when call showInterstitialAdByTimes()
     *
     * @param countClickToShowAds Default value = 3
     * @param currentClicked      Default value = 0
     */
    public void setCountClickToShowAds(int countClickToShowAds, int currentClicked) {
        AppLovin.getInstance().setNumToShowAds(countClickToShowAds, currentClicked);
    }

    /**
     * Hiển thị form CMP (Google UMP) để user cập nhật lại quyền Privacy (GDPR/TCF v2.2)
     */
    public void showCmpForExistingUser(Activity activity, com.applovin.sdk.AppLovinCmpService.OnCompletedListener listener) {
        AppLovin.getInstance().showCmpForExistingUser(activity, listener);
    }

    public boolean hasSupportedCmp(Context context) {
        return AppLovin.getInstance().hasSupportedCmp(context);
    }


    /**
     * @param context
     * @param adConfig object used for SDK initialisation
     */
    public void init(Application context, MaxAdConfig adConfig) {
        init(context, adConfig, false);
    }

    /**
     * @param context
     * @param adConfig             object used for SDK initialisation
     * @param enableDebugMediation set show Mediation Debugger - use only for Max Mediation
     */
    public void init(Application context, MaxAdConfig adConfig, Boolean enableDebugMediation) {
        if (adConfig == null) {
            throw new RuntimeException("Cant not set GamAdConfig null");
        }
        this.adConfig = adConfig;
        AppOpenMax.getInstance().setApplication(context);
        AppUtil.VARIANT_DEV = adConfig.isVariantDev();
        Log.i(TAG, "Config variant dev: " + AppUtil.VARIANT_DEV);
        if (adConfig.isEnableAppsflyer()) {
            Log.i(TAG, "init appsflyer");
            MaxAppsflyer.enableAppsflyer = true;
            MaxAppsflyer.getInstance().init(context, adConfig.getAppsflyerConfig().getAppsflyerToken(), this.adConfig.isVariantDev());
        }
        if (adConfig.isEnableAdjust()) {
            if (adConfig.getAdjustConfig() != null && adConfig.getAdjustConfig().getAdjustToken() != null && !adConfig.getAdjustConfig().getAdjustToken().trim().isEmpty()) {
                Log.i(TAG, "init adjust");
                MaxAdjust.enableAdjust = true;
                setupAdjust(adConfig.isVariantDev(), adConfig.getAdjustConfig().getAdjustToken());
            } else {
                Log.w(TAG, "Adjust token is empty. Skip init Adjust.");
            }
        }

        AppLovin.getInstance().init(context, adConfig.getApplovinSdkKey(), new AppLovinCallback() {
            @Override
            public void initAppLovinSuccess() {
                super.initAppLovinSuccess();
                initAdSuccess = true;
                if (adConfig.isEnableAdResume()) {
                    AppOpenMax.getInstance().init(adConfig.getApplication(), adConfig.getIdAdResume());
                }
                if (initCallback != null) {
                    initCallback.initAdSuccess();
                }
            }
        }, enableDebugMediation, adConfig.getAdjustTokenTiktok());

        FacebookSdk.setClientToken(adConfig.getFacebookClientToken());
        FacebookSdk.sdkInitialize(context);
    }

    public void setInitCallback(MaxInitCallback initCallback) {
        this.initCallback = initCallback;
        if (initAdSuccess)
            initCallback.initAdSuccess();
    }

    private void setupAdjust(Boolean buildDebug, String adjustToken) {

        String environment = buildDebug ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION;
        Log.i("Application", "setupAdjust: " + environment);
        AdjustConfig config = new AdjustConfig(adConfig.getApplication(), adjustToken, environment);

        // Change the log level.
        config.setLogLevel(LogLevel.VERBOSE);
        config.enablePreinstallTracking();
        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                Log.d(TAG_ADJUST, "Attribution callback called!");
                Log.d(TAG_ADJUST, "Attribution: " + attribution.toString());
            }
        });

        // Set event success tracking delegate.
        config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
            @Override
            public void onEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                Log.d(TAG_ADJUST, "Event success callback called!");
                Log.d(TAG_ADJUST, "Event success data: " + eventSuccessResponseData.toString());
            }
        });
        // Set event failure tracking delegate.
        config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
            @Override
            public void onEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                Log.d(TAG_ADJUST, "Event failure callback called!");
                Log.d(TAG_ADJUST, "Event failure data: " + eventFailureResponseData.toString());
            }
        });

        // Set session success tracking delegate.
        config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
            @Override
            public void onSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                Log.d(TAG_ADJUST, "Session success callback called!");
                Log.d(TAG_ADJUST, "Session success data: " + sessionSuccessResponseData.toString());
            }
        });

        // Set session failure tracking delegate.
        config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
            @Override
            public void onSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
                Log.d(TAG_ADJUST, "Session failure callback called!");
                Log.d(TAG_ADJUST, "Session failure data: " + sessionFailureResponseData.toString());
            }
        });


        config.enableSendingInBackground();
        Adjust.initSdk(config);
        adConfig.getApplication().registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
    }

    private static final class AdjustLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }
    }

    public MaxAdConfig getAdConfig() {
        return adConfig;
    }
}
