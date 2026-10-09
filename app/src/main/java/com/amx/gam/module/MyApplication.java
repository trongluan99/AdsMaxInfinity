package com.amx.gam.module;

import com.ads.amx.gam.admob.Admob;
import com.ads.amx.gam.admob.AppOpenManager;
import com.ads.amx.gam.ads.AmxGamAd;
import com.ads.amx.gam.application.AdsMultiDexApplication;
import com.ads.amx.gam.applovin.AppLovin;
import com.ads.amx.gam.applovin.AppOpenMax;
import com.ads.amx.gam.billing.AppPurchase;
import com.ads.amx.gam.config.AdjustConfig;
import com.ads.amx.gam.config.AmxGamAdConfig;
import com.amx.module.BuildConfig;

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
        Admob.getInstance().setNumToShowAds(0);

        initBilling();
        initAds();

    }

    private void initAds() {
        String environment = BuildConfig.env_dev ? AmxGamAdConfig.ENVIRONMENT_DEVELOP : AmxGamAdConfig.ENVIRONMENT_PRODUCTION;
        amxGamAdConfig = new AmxGamAdConfig(this, environment);

        AdjustConfig adjustConfig = new AdjustConfig(true, ADJUST_TOKEN);
        adjustConfig.setEventAdImpression(EVENT_AD_IMPRESSION_ADJUST);
        adjustConfig.setEventNamePurchase(EVENT_PURCHASE_ADJUST);
        amxGamAdConfig.setAdjustConfig(adjustConfig);
        amxGamAdConfig.setAdjustTokenTiktok("123545");


        amxGamAdConfig.setIdAdResume(BuildConfig.ads_open_app);

        listTestDevice.add("EC25F576DA9B6CE74778B268CB87E431");
        amxGamAdConfig.setListDeviceTest(listTestDevice);
        amxGamAdConfig.setIntervalInterstitialAd(15);

        AmxGamAd.getInstance().init(this, amxGamAdConfig, false);

        Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        AppLovin.getInstance().setDisableAdResumeWhenClickAds(true);
        Admob.getInstance().setOpenActivityAfterShowInterAds(true);
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        AppOpenMax.getInstance().disableAppResumeWithActivity(SplashActivity.class);
    }

    private void initBilling() {
        List<String> listINAPId = new ArrayList<>();
        listINAPId.add("android.test.purchased");
        List<String> listSubsId = new ArrayList<>();
        AppPurchase.getInstance().initBilling(getApplication(), listINAPId, listSubsId);
    }

}
