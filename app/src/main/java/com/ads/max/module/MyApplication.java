package com.ads.max.module;

import com.ads.max.ads.MaxAd;
import com.ads.max.application.AdsMultiDexApplication;
import com.ads.max.applovin.AppLovin;
import com.ads.max.applovin.AppOpenMax;
import com.ads.max.billing.AppPurchase;
import com.ads.max.config.AdjustConfig;
import com.ads.max.config.MaxAdConfig;
import com.ads.max.module.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends AdsMultiDexApplication {
    private final String APPSFLYER_TOKEN = "";
    private final String ADJUST_TOKEN = "";
    private final String EVENT_PURCHASE_ADJUST = "";
    private final String EVENT_AD_IMPRESSION_ADJUST = "";
    private static MyApplication context;

    public static MyApplication getApplication() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        AppLovin.getInstance().setNumShowAds(0);

        initBilling();
        initAds();

    }

    private void initAds() {
        String environment = BuildConfig.env_dev ? MaxAdConfig.ENVIRONMENT_DEVELOP : MaxAdConfig.ENVIRONMENT_PRODUCTION;
        maxAdConfig = new MaxAdConfig(this, environment);

        AdjustConfig adjustConfig = new AdjustConfig(!ADJUST_TOKEN.isEmpty(), ADJUST_TOKEN);
        adjustConfig.setEventAdImpression(EVENT_AD_IMPRESSION_ADJUST);
        adjustConfig.setEventNamePurchase(EVENT_PURCHASE_ADJUST);
        maxAdConfig.setAdjustConfig(adjustConfig);
        maxAdConfig.setAdjustTokenTiktok("123545");
        maxAdConfig.setApplovinSdkKey("3N4Mt8SNhOzkQnGb9oHsRRG1ItybcZDpJWN1fVAHLdRagxP-_k_ZXVaMAdMe5Otsmp6qJSXskfsrtakfRmPAGW");

        maxAdConfig.setIdAdResume(BuildConfig.ads_open_app);

        listTestDevice.add("ec25f576-da9b-6ce7-4778-b268cb87e431");
        listTestDevice.add("0572fe18-fbff-4345-8add-325721bdc4d8");
        maxAdConfig.setListDeviceTest(listTestDevice);
        maxAdConfig.setIntervalInterstitialAd(15);

        MaxAd.getInstance().init(this, maxAdConfig, true);

        AppLovin.getInstance().setDisableAdResumeWhenClickAds(true);
        AppOpenMax.getInstance().disableAppResumeWithActivity(SplashActivity.class);
    }

    private void initBilling() {
        List<String> listINAPId = new ArrayList<>();
        listINAPId.add("android.test.purchased");
        List<String> listSubsId = new ArrayList<>();
        AppPurchase.getInstance().initBilling(getApplication(), listINAPId, listSubsId);
    }

}
