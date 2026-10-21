package com.ads.max.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAttribution
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEventFailure
import com.adjust.sdk.AdjustEventSuccess
import com.adjust.sdk.AdjustSessionFailure
import com.adjust.sdk.AdjustSessionSuccess
import com.adjust.sdk.LogLevel
import com.ads.max.applovin.AppLovin
import com.ads.max.applovin.AppLovinCallback
import com.ads.max.applovin.AppOpenMax
import com.ads.max.config.MaxAdConfig
import com.ads.max.event.MaxAdjust
import com.ads.max.event.MaxAppsflyer
import com.ads.max.util.AppUtil
import com.applovin.sdk.AppLovinCmpService
import com.facebook.FacebookSdk

class MaxAd private constructor() {

    var adConfig: MaxAdConfig? = null
        private set

    private var initCallback: MaxInitCallback? = null
    private var initAdSuccess: Boolean = false

    companion object {
        const val TAG_ADJUST = "MaxAdjust"
        const val TAG = "MaxAd"

        @Volatile
        private var INSTANCE: MaxAd? = null

        @JvmStatic
        @Synchronized
        fun getInstance(): MaxAd {
            if (INSTANCE == null) {
                INSTANCE = MaxAd()
            }
            return INSTANCE!!
        }
    }

    /**
     * Set count click to show ads interstitial when call showInterstitialAdByTimes()
     *
     * @param countClickToShowAds - default = 3
     */
    fun setCountClickToShowAds(countClickToShowAds: Int) {
        AppLovin.getInstance().setNumShowAds(countClickToShowAds)
    }

    /**
     * Set count click to show ads interstitial when call showInterstitialAdByTimes()
     *
     * @param countClickToShowAds Default value = 3
     * @param currentClicked      Default value = 0
     */
    fun setCountClickToShowAds(countClickToShowAds: Int, currentClicked: Int) {
        AppLovin.getInstance().setNumToShowAds(countClickToShowAds, currentClicked)
    }

    /**
     * Hiển thị form CMP (Google UMP) để user cập nhật lại quyền Privacy (GDPR/TCF v2.2)
     */
    fun showCmpForExistingUser(activity: Activity, listener: AppLovinCmpService.OnCompletedListener?) {
        AppLovin.getInstance().showCmpForExistingUser(activity, listener)
    }

    fun hasSupportedCmp(context: Context): Boolean {
        return AppLovin.getInstance().hasSupportedCmp(context)
    }

    /**
     * @param context
     * @param adConfig object used for SDK initialisation
     */
    fun init(context: Application, adConfig: MaxAdConfig) {
        init(context, adConfig, false)
    }

    /**
     * @param context
     * @param adConfig             object used for SDK initialisation
     * @param enableDebugMediation set show Mediation Debugger - use only for Max Mediation
     */
    fun init(context: Application, adConfig: MaxAdConfig?, enableDebugMediation: Boolean) {
        if (adConfig == null) {
            throw RuntimeException("Cant not set GamAdConfig null")
        }
        this.adConfig = adConfig
        AppOpenMax.getInstance().setApplication(context)
        AppUtil.VARIANT_DEV = adConfig.isVariantDev()
        Log.i(TAG, "Config variant dev: " + AppUtil.VARIANT_DEV)
        if (adConfig.isEnableAppsflyer()) {
            Log.i(TAG, "init appsflyer")
            MaxAppsflyer.enableAppsflyer = true
            val afToken = adConfig.appsflyerConfig?.appsflyerToken ?: ""
            MaxAppsflyer.getInstance().init(context, afToken, adConfig.isVariantDev())
        }
        if (adConfig.isEnableAdjust()) {
            val adjustConfig = adConfig.adjustConfig
            if (adjustConfig != null && !adjustConfig.adjustToken.isNullOrBlank()) {
                Log.i(TAG, "init adjust")
                MaxAdjust.enableAdjust = true
                setupAdjust(adConfig.isVariantDev(), adjustConfig.adjustToken)
            } else {
                Log.w(TAG, "Adjust token is empty. Skip init Adjust.")
            }
        }

        AppLovin.getInstance().init(context, adConfig.applovinSdkKey, object : AppLovinCallback() {
            override fun initAppLovinSuccess() {
                super.initAppLovinSuccess()
                initAdSuccess = true
                if (adConfig.isEnableAdResume) {
                    val app = adConfig.application ?: context
                    AppOpenMax.getInstance().init(app, adConfig.idAdResume)
                }
                initCallback?.initAdSuccess()
            }
        }, enableDebugMediation, adConfig.adjustTokenTiktok)

        try {
            if (!adConfig.facebookClientToken.isNullOrBlank()) {
                FacebookSdk.setClientToken(adConfig.facebookClientToken)
            }
            FacebookSdk.sdkInitialize(context)
        } catch (e: Exception) {
            Log.e(TAG, "FacebookSdk initialization failed: ${e.message}")
        }
    }

    fun setInitCallback(initCallback: MaxInitCallback) {
        this.initCallback = initCallback
        if (initAdSuccess) {
            initCallback.initAdSuccess()
        }
    }

    private fun setupAdjust(buildDebug: Boolean, adjustToken: String) {
        val currentAdConfig = adConfig ?: return
        val environment = if (buildDebug) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
        Log.i("Application", "setupAdjust: $environment")
        val config = AdjustConfig(currentAdConfig.application, adjustToken, environment)

        // Change the log level.
        config.setLogLevel(LogLevel.VERBOSE)
        config.enablePreinstallTracking()
        config.setOnAttributionChangedListener { attribution: AdjustAttribution ->
            Log.d(TAG_ADJUST, "Attribution callback called!")
            Log.d(TAG_ADJUST, "Attribution: $attribution")
        }

        // Set event success tracking delegate.
        config.setOnEventTrackingSucceededListener { eventSuccessResponseData: AdjustEventSuccess ->
            Log.d(TAG_ADJUST, "Event success callback called!")
            Log.d(TAG_ADJUST, "Event success data: $eventSuccessResponseData")
        }
        // Set event failure tracking delegate.
        config.setOnEventTrackingFailedListener { eventFailureResponseData: AdjustEventFailure ->
            Log.d(TAG_ADJUST, "Event failure callback called!")
            Log.d(TAG_ADJUST, "Event failure data: $eventFailureResponseData")
        }

        // Set session success tracking delegate.
        config.setOnSessionTrackingSucceededListener { sessionSuccessResponseData: AdjustSessionSuccess ->
            Log.d(TAG_ADJUST, "Session success callback called!")
            Log.d(TAG_ADJUST, "Session success data: $sessionSuccessResponseData")
        }

        // Set session failure tracking delegate.
        config.setOnSessionTrackingFailedListener { sessionFailureResponseData: AdjustSessionFailure ->
            Log.d(TAG_ADJUST, "Session failure callback called!")
            Log.d(TAG_ADJUST, "Session failure data: $sessionFailureResponseData")
        }

        config.enableSendingInBackground()
        Adjust.initSdk(config)
        currentAdConfig.application?.registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())
    }

    private class AdjustLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            Adjust.onResume()
        }

        override fun onActivityPaused(activity: Activity) {
            Adjust.onPause()
        }

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}
    }
}
