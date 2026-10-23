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
        maxAdConfig?.applovinSdkKey = "3N4Mt8SNhOzkQnGb9oHsRRG1ItybcZDpJWN1fVAHLdRagxP-_k_ZXVaMAdMe5Otsmp6qJSXskfsrtakfRmPAGW"

        maxAdConfig?.idAdResume = BuildConfig.ads_open_app

        listTestDevice.add("7bf31318-1917-4e70-8c03-1ce88b8bfd17")
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
