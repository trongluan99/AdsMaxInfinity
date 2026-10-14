package com.ads.max.applovin;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.ads.max.ads.MaxAdCallback;
import com.ads.max.ads.wrapper.ApAdError;
import com.ads.max.billing.AppPurchase;
import com.ads.max.config.MaxAdConfig;
import com.ads.max.dialog.ResumeLoadingDialog;
import com.ads.max.event.MaxLogEventManager;
import com.ads.max.funtion.AdType;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdRevenueListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAppOpenAd;
import com.applovin.sdk.AppLovinSdk;

import java.util.ArrayList;
import java.util.List;

public class AppOpenMax implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private static final String TAG = "AppOpenMax";
    private MaxAppOpenAd appOpenAd;
    private Application myApplication;
    private static volatile AppOpenMax INSTANCE;
    private Activity currentActivity;
    private Dialog dialog = null;
    private final List<Class> disabledAppOpenList;
    private boolean isAppResumeEnabled = true;
    private boolean isInterstitialShowing = false;
    private boolean disableAdResumeByClickAction = false;
    private boolean displayAdResume = false;
    private boolean isInitialized = false; // on  - off ad resume on app
    private MaxAdCallback maxAdCallback;

    private MaxAppOpenAd openAdSplash;
    private boolean isShowingAd = false;

    public static synchronized AppOpenMax getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppOpenMax();
        }
        return INSTANCE;
    }

    private AppOpenMax() {
        disabledAppOpenList = new ArrayList<>();
    }

    public void init(Application application, String appOpenAdId) {
        isInitialized = true;
        disableAdResumeByClickAction = false;
        this.myApplication = application;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        loadAdResumeMax(application, appOpenAdId);
    }

    // Policy Max: Load sau Ad Splash
    public void loadAdResumeMax(Application application, String appOpenAdId) {
        if (appOpenAdId == null || appOpenAdId.trim().isEmpty()) {
            Log.e(TAG, "loadAdResumeMax: appOpenAdId is null or empty");
            return;
        }
        if (!AppLovinSdk.getInstance(application).isInitialized()) {
            Log.e(TAG, "loadAdResumeMax: AppLovin SDK is not initialized yet. Please wait for SDK initialization.");
            return;
        }
        appOpenAd = new MaxAppOpenAd(appOpenAdId, application);
        appOpenAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onAdLoaded: ");
                if (maxAdCallback != null) {
                    maxAdCallback.onAdLoaded();
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                displayAdResume = true;
                Log.d(TAG, "onAdDisplayed: ");
                if (maxAdCallback != null) {
                    maxAdCallback.onAdImpression();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.d(TAG, "onAdHidden: ");
                appOpenAd.loadAd();
                dismissDialogLoading();
                displayAdResume = false;
                if (maxAdCallback != null) {
                    maxAdCallback.onAdClosed();
                }
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.d(TAG, "onAdClicked: ");
                disableAdResumeByClickAction = true;
                if (maxAdCallback != null) {
                    maxAdCallback.onAdClicked();
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.d(TAG, "onAdLoadFailed: ");
                dismissDialogLoading();
                if (maxAdCallback != null) {
                    maxAdCallback.onAdFailedToLoad(new ApAdError(error));
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.d(TAG, "onAdDisplayFailed: ");
                appOpenAd.loadAd();
                dismissDialogLoading();
                if (maxAdCallback != null) {
                    maxAdCallback.onAdFailedToShow(new ApAdError(error));
                }
            }
        });
        appOpenAd.setRevenueListener(ad -> {
            MaxLogEventManager.logPaidAdImpression(application, ad, AdType.APP_OPEN);
            MaxLogEventManager.logPaidAdjustWithTokenMax(ad, ad.getAdUnitId(), MaxAdConfig.ADJUST_TOKEN_TIKTOK);
        });
        appOpenAd.loadAd();
    }

    /**
     * Disable app open app on specific activity
     *
     * @param activityClass
     */
    public void disableAppResumeWithActivity(Class activityClass) {
        Log.d(TAG, "disableAppResumeWithActivity: " + activityClass.getName());
        disabledAppOpenList.add(activityClass);
    }

    public void enableAppResumeWithActivity(Class activityClass) {
        Log.d(TAG, "enableAppResumeWithActivity: " + activityClass.getName());
        disabledAppOpenList.remove(activityClass);
    }

    public void disableAppResume() {
        isAppResumeEnabled = false;
    }

    public void enableAppResume() {
        isAppResumeEnabled = true;
    }

    public boolean isInterstitialShowing() {
        return isInterstitialShowing;
    }

    public void setInterstitialShowing(boolean interstitialShowing) {
        isInterstitialShowing = interstitialShowing;
    }

    /**
     * Call disable ad resume when click a button, auto enable ad resume in next start
     */
    public void disableAdResumeByClickAction() {
        disableAdResumeByClickAction = true;
    }

    public void setDisableAdResumeByClickAction(boolean disableAdResumeByClickAction) {
        this.disableAdResumeByClickAction = disableAdResumeByClickAction;
    }

    public void setAppOpenMaxCallback(MaxAdCallback maxAdCallback) {
        this.maxAdCallback = maxAdCallback;
    }


    private void showAdIfReady() {
        if (appOpenAd == null
                || !AppLovinSdk.getInstance(myApplication).isInitialized()
                || currentActivity == null
                || AppPurchase.getInstance().isPurchased(currentActivity)
        ) {
            return;
        }
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)
                && isNetworkAvailable()
        ) {
            try {
                dismissDialogLoading();
                dialog = new ResumeLoadingDialog(currentActivity);
                try {
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "showAdIfReady: " + e.getMessage());
            }
            if (appOpenAd.isReady()) {
                new Handler().postDelayed(() -> appOpenAd.showAd(), 500);
            } else {
                appOpenAd.loadAd();
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onResume() {
        if (!isAppResumeEnabled) {
            Log.d(TAG, "onResume: app resume is disabled");
            return;
        }

        if (disableAdResumeByClickAction) {
            Log.d(TAG, "onResume:ad resume disable ad by action");
            disableAdResumeByClickAction = false;
            return;
        }

        if (isInterstitialShowing) {
            Log.d(TAG, "onResume: interstitial is showing");
            return;
        }

        if (displayAdResume) {
            Log.d(TAG, "onResume: AppOpen is showing");
            return;
        }


        try {
            for (Class activity : disabledAppOpenList) {
                if (activity.getName().equals(currentActivity.getClass().getName())) {
                    Log.d(TAG, "onStart: activity is disabled");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        showAdIfReady();
    }

    private void dismissDialogLoading() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
        Log.d(TAG, "onActivityStarted: " + currentActivity);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
        Log.d(TAG, "onActivityResumed: " + currentActivity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;
        Log.d(TAG, "onActivityDestroyed: null");
    }

    public void loadOpenMaxSplash(Context context, String idOpenSplash, int timeDelay, int timeOut, boolean isShowAdIfReady, AppLovinCallback adCallback) {
        if (context != null && this.myApplication == null) {
            if (context instanceof Application) {
                this.myApplication = (Application) context;
            } else if (context.getApplicationContext() instanceof Application) {
                this.myApplication = (Application) context.getApplicationContext();
            }
        }
        if (!isNetworkAvailable(context)) {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    if (adCallback != null) {
                        adCallback.onNextAction();
                    }
                }
            }, timeDelay);
        } else if (idOpenSplash == null || idOpenSplash.trim().isEmpty()) {
            Log.e(TAG, "loadOpenMaxSplash: idOpenSplash is null or empty");
            if (adCallback != null) {
                adCallback.onNextAction();
            }
        } else {
            final long currentTimeMillis = System.currentTimeMillis();
            final Runnable timeOutRunnable = () -> {
                adCallback.onNextAction();
                isShowingAd = false;
            };
            final Handler handler = new Handler();
            handler.postDelayed(timeOutRunnable, timeOut);
            openAdSplash = new MaxAppOpenAd(idOpenSplash, context);
            openAdSplash.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd maxAd) {
                    handler.removeCallbacks(timeOutRunnable);
                    if (isShowAdIfReady) {
                        long elapsedTime = System.currentTimeMillis() - currentTimeMillis;
                        if (elapsedTime >= timeDelay) {
                            elapsedTime = 0L;
                        }

                        Handler handler1 = new Handler();
                        Runnable showAppOpenSplashRunnable = () -> {
                            showAdIfReady(context, adCallback);
                        };
                        handler1.postDelayed(showAppOpenSplashRunnable, elapsedTime);
                    } else {
                        adCallback.onAdSplashReady();
                    }
                }

                @Override
                public void onAdDisplayed(MaxAd maxAd) {
                    Log.d(TAG, "onAdDisplayed: ");
                }

                @Override
                public void onAdHidden(MaxAd maxAd) {
                    Log.d(TAG, "onAdHidden: ");
                }

                @Override
                public void onAdClicked(MaxAd maxAd) {
                    Log.d(TAG, "onAdClicked: ");
                }

                @Override
                public void onAdLoadFailed(String s, MaxError maxError) {
                    Log.d(TAG, "onAdLoadFailed: " + (maxError != null ? maxError.getMessage() : ""));
                    handler.removeCallbacks(timeOutRunnable);
                    if (adCallback != null) {
                        adCallback.onAdLoadFailed();
                        adCallback.onNextAction();
                    }
                }

                @Override
                public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
                    Log.d(TAG, "onAdDisplayFailed: " + (maxError != null ? maxError.getMessage() : ""));
                    handler.removeCallbacks(timeOutRunnable);
                    if (adCallback != null) {
                        adCallback.onAdDisplayFailed();
                        adCallback.onNextAction();
                    }
                }
            });
            openAdSplash.loadAd();
        }
    }

    private void showAdIfReady(Context context, AppLovinCallback adCallback) {
        if (!AppLovinSdk.getInstance(context).isInitialized()) {
            if (adCallback != null) {
                adCallback.onNextAction();
                return;
            }
            return;
        }

        if (openAdSplash != null && openAdSplash.isReady()) {
            openAdSplash.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd maxAd) {
                    if (adCallback != null) {
                        adCallback.onAdLoaded();
                    }
                }

                @Override
                public void onAdDisplayed(MaxAd maxAd) {
                    isShowingAd = true;
                    if (adCallback != null) {
                        adCallback.onAdDisplayed();
                    }
                }

                @Override
                public void onAdHidden(MaxAd maxAd) {
                    isShowingAd = false;
                    if (adCallback != null) {
                        adCallback.onNextAction();
                    }
                }

                @Override
                public void onAdClicked(MaxAd maxAd) {
                    if (adCallback != null) {
                        adCallback.onAdClicked();
                    }
                }

                @Override
                public void onAdLoadFailed(String s, MaxError maxError) {
                    if (adCallback != null) {
                        adCallback.onAdLoadFailed();
                    }
                }

                @Override
                public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
                    isShowingAd = false;
                    if (adCallback != null) {
                        adCallback.onAdDisplayFailed();
                        adCallback.onNextAction();
                    }
                }
            });
            openAdSplash.setRevenueListener(new MaxAdRevenueListener() {
                @Override
                public void onAdRevenuePaid(MaxAd maxAd) {
                    MaxLogEventManager.logPaidAdImpression(context, maxAd, AdType.APP_OPEN);
                    MaxLogEventManager.logPaidAdjustWithTokenMax(maxAd, maxAd.getAdUnitId(), MaxAdConfig.ADJUST_TOKEN_TIKTOK);
                }
            });
            openAdSplash.showAd();
        } else {
            if (adCallback != null) {
                adCallback.onNextAction();
            }
        }
    }

    public void onCheckShowSplashWhenFail(AppCompatActivity activity, AppLovinCallback adCallback, int timeDelay) {
        (new Handler(activity.getMainLooper())).postDelayed(new Runnable() {
            public void run() {
                if (openAdSplash != null && !isShowingAd) {
                    showAdIfReady(activity, adCallback);
                }

            }
        }, timeDelay);
    }

    public void setApplication(Application application) {
        this.myApplication = application;
    }

    private boolean isNetworkAvailable(Context context) {
        Context ctx = context != null ? context : myApplication;
        if (ctx == null) {
            return false;
        }
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                return false;
            }
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            Log.e(TAG, "Error checking network: " + e.getMessage());
            return true;
        }
    }

    private boolean isNetworkAvailable() {
        return isNetworkAvailable(myApplication);
    }

    public void destroy() {
        if (appOpenAd != null) {
            appOpenAd.destroy();
            appOpenAd = null;
        }
        if (openAdSplash != null) {
            openAdSplash.destroy();
            openAdSplash = null;
        }
    }
}
