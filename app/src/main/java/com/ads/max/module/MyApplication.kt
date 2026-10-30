package com.ads.max.module

import com.ads.max.ads.MaxAd
import com.ads.max.application.AdsMultiDexApplication
import com.ads.max.applovin.AppLovin
import com.ads.max.applovin.AppOpenMax
import com.ads.max.billing.AppPurchase
import com.ads.max.config.AdjustConfig
import com.ads.max.config.MaxAdConfig

class MyApplication : AdsMultiDexApplication() {

    companion object {
        @JvmStatic
        var application: MyApplication? = null
            private set
    }

    private val APPSFLYER_TOKEN = ""
    private val ADJUST_TOKEN = ""
    private val EVENT_PURCHASE_ADJUST = ""
    private val EVENT_AD_IMPRESSION_ADJUST = ""

    override fun onCreate() {
        super.onCreate()
        application = this
        AppLovin.getInstance().setNumShowAds(0)

        initBilling()
        initAds()
    }

    private fun initAds() {
        val environment = if (BuildConfig.env_dev) MaxAdConfig.ENVIRONMENT_DEVELOP else MaxAdConfig.ENVIRONMENT_PRODUCTION
        maxAdConfig = MaxAdConfig(this, environment)

        val adjustConfig = AdjustConfig(ADJUST_TOKEN.isNotEmpty(), ADJUST_TOKEN)
        adjustConfig.eventAdImpression = EVENT_AD_IMPRESSION_ADJUST
        adjustConfig.eventNamePurchase = EVENT_PURCHASE_ADJUST
        maxAdConfig?.adjustConfig = adjustConfig
        maxAdConfig?.adjustTokenTiktok = "123545"
        maxAdConfig?.applovinSdkKey = "szyUmwGA0KXmgPKVL0LcrqGVCNJUXwL6FOdJtte_WI9GRLkuJRUAxA0NXFC2gxegRxaCJk2gX1CIfIbfQBQ9wW"

        maxAdConfig?.idAdResume = BuildConfig.ads_open_app

        listTestDevice.add("0CFFB360968837ECF982C1E38F8D975F")
        listTestDevice.add("87684781-c920-4435-907c-7e6f6162078c")
        listTestDevice.add("ec25f576-da9b-6ce7-4778-b268cb87e431")
        listTestDevice.add("0572fe18-fbff-4345-8add-325721bdc4d8")
        maxAdConfig?.listDeviceTest = listTestDevice
        maxAdConfig?.intervalInterstitialAd = 15

        AppLovin.getInstance().setDisableAdResumeWhenClickAds(true)
        AppOpenMax.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
    }

    /**
     * Khởi tạo MAX SDK sau khi luồng CMP (Consent Management Platform) đã hoàn tất ở Splash
     */
    fun initMaxAds(onInitSuccess: (() -> Unit)? = null) {
        maxAdConfig?.let { config ->
            MaxAd.getInstance().init(this, config, false) {
                onInitSuccess?.invoke()
            }
        } ?: run {
            onInitSuccess?.invoke()
        }
    }

    private fun initBilling() {
        val listINAPId: MutableList<String> = ArrayList()
        listINAPId.add("android.test.purchased")
        val listSubsId: MutableList<String> = ArrayList()
        AppPurchase.getInstance().initBilling(this, listINAPId, listSubsId)
    }
}