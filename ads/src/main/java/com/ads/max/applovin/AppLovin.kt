package com.ads.max.applovin

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.ads.max.R
import com.ads.max.billing.AppPurchase
import com.ads.max.dialog.PrepareLoadingAdsDialog
import com.ads.max.event.MaxLogEventManager
import com.ads.max.funtion.AdCallback
import com.ads.max.funtion.AdType
import com.ads.max.util.SharePreferenceUtils
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacer
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacerSettings
import com.applovin.mediation.nativeAds.adPlacer.MaxRecyclerAdapter
import com.applovin.sdk.AppLovinCmpService
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.applovin.sdk.AppLovinSdkUtils
import com.facebook.shimmer.ShimmerFrameLayout
import java.util.ArrayList
import java.util.Calendar

class AppLovin private constructor() {

    private var currentClicked = 0
    private var nativeId: String? = null
    private var numShowAds = 3
    private var maxClickAds = 100
    private var handlerTimeout: Handler? = null
    private var rdTimeout: Runnable? = null
    private var dialog: PrepareLoadingAdsDialog? = null
    private var isTimeout = false // xử lý timeout show ads

    @JvmField
    var isShowLoadingSplash = false // kiểm tra trạng thái ad splash, ko cho load, show khi đang show loading ads splash
    internal var isTimeDelay = false // xử lý delay time show ads, = true mới show ads
    private var context: Context? = null

    private var interstitialSplash: MaxInterstitialAd? = null
    private var interstitialAd: MaxInterstitialAd? = null
    private var nativeAdView: MaxNativeAdView? = null
    private var lastNativeAdLoader: MaxNativeAdLoader? = null
    private var loadedNativeAd: MaxAd? = null

    private var tokenAdjust: String? = null
    private var disableAdResumeWhenClickAds = false

    companion object {
        private const val TAG = "AppLovin"

        @Volatile
        private var instance: AppLovin? = null

        @JvmStatic
        fun getInstance(): AppLovin {
            if (instance == null) {
                instance = AppLovin().apply {
                    isShowLoadingSplash = false
                }
            }
            return instance!!
        }
    }

    fun getSdkKey(context: Context): String {
        val maxAdConfig = com.ads.max.ads.MaxAd.getInstance().adConfig
        if (maxAdConfig != null) {
            val configKey = maxAdConfig.applovinSdkKey
            if (!configKey.isNullOrBlank()) {
                return configKey.trim()
            }
        }
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            )
            val bundle = appInfo.metaData
            if (bundle != null) {
                val key = bundle.getString("applovin.sdk.key")
                if (!key.isNullOrBlank()) {
                    return key.trim()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading applovin.sdk.key from manifest: " + e.message)
        }
        val key = AppLovinSdk.getInstance(context).sdkKey
        return key ?: ""
    }

    fun init(context: Context, adCallback: AppLovinCallback?, tokenAdjust: String?) {
        init(context, null, adCallback, false, tokenAdjust)
    }

    fun init(context: Context, adCallback: AppLovinCallback?, enableDebug: Boolean?, tokenAdjust: String?) {
        init(context, null, adCallback, enableDebug, tokenAdjust)
    }

    fun init(
        context: Context,
        sdkKey: String?,
        adCallback: AppLovinCallback?,
        enableDebug: Boolean?,
        tokenAdjust: String?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = Application.getProcessName()
            val packageName = context.packageName
            if (packageName != processName) {
                WebView.setDataDirectorySuffix(processName)
            }
        }
        if (enableDebug == true) {
            AppLovinSdk.getInstance(context).settings.setVerboseLogging(true)
        }
        var finalKey = sdkKey
        if (finalKey.isNullOrBlank()) {
            finalKey = getSdkKey(context)
        }
        Log.d(TAG, "AppLovin SDK Key: $finalKey")
        if (finalKey.isEmpty()) {
            Log.e(
                TAG,
                "AppLovin SDK Key is empty! Please configure it in MaxAdConfig, init(..., sdkKey, ...), or AndroidManifest.xml (<meta-data android:name=\"applovin.sdk.key\" .../>)"
            )
        }
        try {
            val initConfigBuilder = AppLovinSdkInitializationConfiguration.builder(finalKey, context)
                .setMediationProvider(AppLovinMediationProvider.MAX)

            val adConfig = com.ads.max.ads.MaxAd.getInstance().adConfig
            if (adConfig != null && adConfig.listDeviceTest.isNotEmpty()) {
                val validUuids: MutableList<String> = ArrayList()
                for (deviceId in adConfig.listDeviceTest) {
                    val formatted = formatToUuidIfPossible(deviceId)
                    if (formatted != null) {
                        validUuids.add(formatted)
                    }
                }
                if (validUuids.isNotEmpty()) {
                    initConfigBuilder.setTestDeviceAdvertisingIds(validUuids)
                }
            }
            val initConfig = initConfigBuilder.build()
            AppLovinSdk.getInstance(context).initialize(initConfig) {
                Log.d(TAG, "init: applovin success. isInitialized=" + AppLovinSdk.getInstance(context).isInitialized)
                if (enableDebug == true) {
                    AppLovinSdk.getInstance(context).showMediationDebugger()
                }
                adCallback?.initAppLovinSuccess()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AppLovin SDK: " + e.message, e)
        }
        this.tokenAdjust = tokenAdjust
        this.context = context
    }

    private fun formatToUuidIfPossible(id: String?): String? {
        if (id == null) return null
        val clean = id.trim().replace("-", "")
        if (clean.length == 32) {
            return String.format(
                "%s-%s-%s-%s-%s",
                clean.substring(0, 8),
                clean.substring(8, 12),
                clean.substring(12, 16),
                clean.substring(16, 20),
                clean.substring(20, 32)
            ).lowercase()
        } else if (id.trim().matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))) {
            return id.trim().lowercase()
        }
        Log.w(TAG, "Device test ID is not a valid UUID: $id. Ignoring.")
        return null
    }

    /**
     * Hiển thị form CMP (Google UMP) để user cập nhật lại quyền Privacy (GDPR/TCF v2.2)
     */
    fun showCmpForExistingUser(activity: Activity?, listener: AppLovinCmpService.OnCompletedListener?) {
        if (activity == null) return
        val cmpService = AppLovinSdk.getInstance(activity).cmpService
        if (cmpService != null && cmpService.hasSupportedCmp()) {
            cmpService.showCmpForExistingUser(activity) { error ->
                listener?.onCompleted(error)
            }
        } else {
            Log.w(TAG, "showCmpForExistingUser: CMP is not supported or not enabled in this region / app.")
            listener?.onCompleted(null)
        }
    }

    fun hasSupportedCmp(context: Context?): Boolean {
        if (context == null) return false
        val cmpService = AppLovinSdk.getInstance(context).cmpService
        return cmpService != null && cmpService.hasSupportedCmp()
    }

    fun setNumShowAds(numShowAds: Int) {
        this.numShowAds = numShowAds
    }

    fun setNumToShowAds(numShowAds: Int, currentClicked: Int) {
        this.numShowAds = numShowAds
        this.currentClicked = currentClicked
    }

    fun getInterstitialSplash(): MaxInterstitialAd? {
        return interstitialSplash
    }

    /**
     * Disable ad resume when user click ads and back to app
     */
    fun setDisableAdResumeWhenClickAds(disableAdResumeWhenClickAds: Boolean) {
        this.disableAdResumeWhenClickAds = disableAdResumeWhenClickAds
    }

    /**
     * Load quảng cáo Full tại màn SplashActivity
     * Sau khoảng thời gian timeout thì load ads và callback về cho View
     */
    fun loadSplashInterstitialAds(
        context: Context,
        id: String,
        timeOut: Long,
        timeDelay: Long,
        adListener: AppLovinCallback?
    ) {
        isTimeDelay = false
        isTimeout = false
        Log.i(
            TAG, "loadSplashInterstitialAds  start time loading:"
                    + Calendar.getInstance().timeInMillis
                    + " ShowLoadingSplash:" + isShowLoadingSplash
        )

        if (AppPurchase.getInstance().isPurchased(context)) {
            adListener?.onAdClosed()
            return
        }

        interstitialSplash = getInterstitialAds(context, id)
        Handler(Looper.getMainLooper()).postDelayed({
            // check delay show ad splash
            if (interstitialSplash != null && interstitialSplash?.isReady == true) {
                Log.i(TAG, "loadSplashInterstitialAds:show ad on delay ")
                val act = context as? Activity
                if (act != null && !act.isFinishing && !act.isDestroyed) {
                    onShowSplash(act, adListener)
                } else {
                    adListener?.onAdClosed()
                }
                return@postDelayed
            }
            Log.i(TAG, "loadSplashInterstitialAds: delay validate")
            isTimeDelay = true
        }, timeDelay)

        if (timeOut > 0) {
            handlerTimeout = Handler(Looper.getMainLooper())
            rdTimeout = Runnable {
                Log.e(TAG, "loadSplashInterstitialAds: on timeout")
                isTimeout = true
                if (interstitialSplash != null && interstitialSplash?.isReady == true) {
                    Log.i(TAG, "loadSplashInterstitialAds:show ad on timeout ")
                    val act = context as? Activity
                    if (act != null && !act.isFinishing && !act.isDestroyed) {
                        onShowSplash(act, adListener)
                    } else {
                        adListener?.onAdClosed()
                    }
                    return@Runnable
                }
                adListener?.onAdClosed()
                isShowLoadingSplash = false
            }
            rdTimeout?.let { handlerTimeout?.postDelayed(it, timeOut) }
        }

        isShowLoadingSplash = true

        interstitialSplash?.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                Log.e(
                    TAG, "loadSplashInterstitialAds end time loading success: "
                            + Calendar.getInstance().timeInMillis
                            + " time limit:" + isTimeout
                )
                if (isTimeout) return
                if (isTimeDelay) {
                    val act = context as? Activity
                    if (act != null && !act.isFinishing && !act.isDestroyed) {
                        onShowSplash(act, adListener)
                        Log.i(TAG, "loadSplashInterstitialAds: show ad on loaded ")
                    } else {
                        adListener?.onAdClosed()
                    }
                }
            }

            override fun onAdDisplayed(ad: MaxAd) {
                AppOpenMax.getInstance().setInterstitialShowing(true)
            }

            override fun onAdHidden(ad: MaxAd) {}

            override fun onAdClicked(ad: MaxAd) {
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
                MaxLogEventManager.logClickAdsEvent(context, ad.adUnitId)
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                Log.e(TAG, "onAdLoadFailed: " + error.message)
                if (isTimeout) return
                if (adListener != null) {
                    rdTimeout?.let { handlerTimeout?.removeCallbacks(it) }
                    Log.e(TAG, "loadSplashInterstitialAds: load fail " + error.message)
                    adListener.onAdFailedToLoad(error)
                }
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {}
        })
    }

    /**
     * Load quảng cáo Full tại màn SplashActivity
     * Sau khoảng thời gian timeout thì load ads và callback về cho View
     */
    fun loadSplashInterstitialAds(
        context: Context,
        id: String,
        timeOut: Long,
        timeDelay: Long,
        showSplashIfReady: Boolean,
        adListener: AppLovinCallback?
    ) {
        isTimeDelay = false
        isTimeout = false
        Log.i(
            TAG, "loadSplashInterstitialAds  start time loading:"
                    + Calendar.getInstance().timeInMillis
                    + " ShowLoadingSplash:" + isShowLoadingSplash
        )

        if (AppPurchase.getInstance().isPurchased(context)) {
            adListener?.onAdClosed()
            return
        }

        interstitialSplash = getInterstitialAds(context, id)
        Handler(Looper.getMainLooper()).postDelayed({
            // check delay show ad splash
            if (interstitialSplash != null && interstitialSplash?.isReady == true) {
                Log.i(TAG, "loadSplashInterstitialAds:show ad on delay ")
                val act = context as? Activity
                if (act != null && !act.isFinishing && !act.isDestroyed) {
                    if (showSplashIfReady) {
                        onShowSplash(act, adListener)
                    } else {
                        adListener?.onAdSplashReady()
                    }
                } else {
                    adListener?.onAdClosed()
                }
                return@postDelayed
            }
            Log.i(TAG, "loadSplashInterstitialAds: delay validate")
            isTimeDelay = true
        }, timeDelay)

        if (timeOut > 0) {
            handlerTimeout = Handler(Looper.getMainLooper())
            rdTimeout = Runnable {
                Log.e(TAG, "loadSplashInterstitialAds: on timeout")
                isTimeout = true
                if (interstitialSplash != null && interstitialSplash?.isReady == true) {
                    Log.i(TAG, "loadSplashInterstitialAds:show ad on timeout ")
                    val act = context as? Activity
                    if (act != null && !act.isFinishing && !act.isDestroyed) {
                        if (showSplashIfReady) {
                            onShowSplash(act, adListener)
                        } else {
                            adListener?.onAdSplashReady()
                        }
                    } else {
                        adListener?.onAdClosed()
                    }
                    return@Runnable
                }
                adListener?.onAdClosed()
                isShowLoadingSplash = false
            }
            rdTimeout?.let { handlerTimeout?.postDelayed(it, timeOut) }
        }

        isShowLoadingSplash = true

        interstitialSplash?.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                Log.e(
                    TAG, "loadSplashInterstitialAds end time loading success: "
                            + Calendar.getInstance().timeInMillis
                            + " time limit:" + isTimeout
                )
                if (isTimeout) return
                if (isTimeDelay) {
                    val act = context as? Activity
                    if (act != null && !act.isFinishing && !act.isDestroyed) {
                        if (showSplashIfReady) {
                            onShowSplash(act, adListener)
                        } else {
                            adListener?.onAdSplashReady()
                        }
                        Log.i(TAG, "loadSplashInterstitialAds: show ad on loaded ")
                    } else {
                        adListener?.onAdClosed()
                    }
                }
            }

            override fun onAdDisplayed(ad: MaxAd) {
                AppOpenMax.getInstance().setInterstitialShowing(true)
            }

            override fun onAdHidden(ad: MaxAd) {}

            override fun onAdClicked(ad: MaxAd) {
                MaxLogEventManager.logClickAdsEvent(context, ad.adUnitId)
                adListener?.onAdClicked()
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                Log.e(TAG, "onAdLoadFailed: " + error.message)
                if (isTimeout) return
                if (adListener != null) {
                    rdTimeout?.let { handlerTimeout?.removeCallbacks(it) }
                    Log.e(TAG, "loadSplashInterstitialAds: load fail " + error.message)
                    adListener.onAdFailedToLoad(error)
                }
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {}
        })
    }

    fun onShowSplash(activity: Activity, adListener: AppLovinCallback?) {
        isShowLoadingSplash = true
        Log.d(TAG, "onShowSplash: ")
        rdTimeout?.let { handlerTimeout?.removeCallbacks(it) }

        adListener?.onAdLoaded()
        val currentSplash = interstitialSplash
        if (currentSplash == null) {
            adListener?.onAdClosed()
            return
        }
        currentSplash.setRevenueListener { ad ->
            MaxLogEventManager.logPaidAdImpression(context ?: activity, ad, AdType.INTERSTITIAL)
            if (tokenAdjust != null) {
                MaxLogEventManager.logPaidAdjustWithTokenMax(ad, ad.adUnitId, tokenAdjust)
            }
        }
        currentSplash.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd) {}

            override fun onAdDisplayed(ad: MaxAd) {
                Log.d(TAG, "onAdDisplayed: ")
                AppOpenMax.getInstance().setInterstitialShowing(true)
                adListener?.onAdImpression()
            }

            override fun onAdHidden(ad: MaxAd) {
                val appCompat = activity as? AppCompatActivity
                Log.d(TAG, "onAdHidden: " + appCompat?.lifecycle?.currentState)
                AppOpenMax.getInstance().setInterstitialShowing(false)
                isShowLoadingSplash = false
                if (adListener != null && (appCompat == null || appCompat.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))) {
                    adListener.onAdClosed()
                    interstitialSplash = null
                    dialog?.dismiss()
                }
            }

            override fun onAdClicked(ad: MaxAd) {
                MaxLogEventManager.logClickAdsEvent(context ?: activity, currentSplash.adUnitId)
                adListener?.onAdClicked()
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {}

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                Log.d(TAG, "onAdDisplayFailed: " + error.message)
                AppOpenMax.getInstance().setInterstitialShowing(false)
                interstitialSplash = null
                isShowLoadingSplash = false
                if (adListener != null) {
                    adListener.onAdFailedToShow(error)
                    dialog?.dismiss()
                }
            }
        })

        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            try {
                if (dialog?.isShowing == true) {
                    dialog?.dismiss()
                }
                if (!activity.isDestroyed && !activity.isFinishing) {
                    dialog = PrepareLoadingAdsDialog(activity).apply {
                        setCancelable(false)
                        show()
                    }
                }
            } catch (e: Exception) {
                dialog = null
                e.printStackTrace()
                adListener?.onAdClosed()
                return
            }
            Handler(Looper.getMainLooper()).postDelayed({
                if (!activity.isDestroyed && !activity.isFinishing) {
                    currentSplash.showAd()
                }
            }, 800)
        } else {
            Log.e(TAG, "onShowSplash fail ")
            isShowLoadingSplash = false
        }
    }

    fun onCheckShowSplashWhenFail(
        activity: Activity,
        callback: AppLovinCallback,
        timeDelay: Int,
        tokenAdjust: String?
    ) {
        val splash = interstitialSplash
        if (splash != null && !isShowLoadingSplash) {
            Handler(activity.mainLooper).postDelayed({
                if (splash.isReady) {
                    Log.i(TAG, "show ad splash when show fail in background")
                    onShowSplash(activity, callback)
                } else {
                    callback.onAdClosed()
                }
            }, timeDelay.toLong())
        }
    }

    /**
     * Trả về 1 InterstitialAd và request Ads
     */
    fun getInterstitialAds(context: Context, id: String): MaxInterstitialAd? {
        if (AppPurchase.getInstance().isPurchased(context) || AppLovinHelper.getNumClickAdsPerDay(context, id) >= maxClickAds) {
            Log.d(TAG, "getInterstitialAds: ignore")
            return null
        }
        val activity = context as? Activity ?: run {
            Log.e(TAG, "getInterstitialAds: context must be an Activity")
            return null
        }
        val ad = MaxInterstitialAd(id, activity)
        ad.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                Log.d(TAG, "onAdLoaded: getInterstitialAds")
            }

            override fun onAdDisplayed(ad: MaxAd) {}

            override fun onAdHidden(ad: MaxAd) {}

            override fun onAdClicked(ad: MaxAd) {
                MaxLogEventManager.logClickAdsEvent(context, ad.adUnitId)
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                Log.e(TAG, "onAdLoadFailed: getInterstitialAds " + error.message)
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {}
        })
        requestInterstitialAds(ad)
        return ad
    }

    private fun requestInterstitialAds(maxInterstitialAd: MaxInterstitialAd?) {
        if (maxInterstitialAd != null && !maxInterstitialAd.isReady) {
            maxInterstitialAd.loadAd()
        }
    }

    /**
     * Bắt buộc hiển thị ads full và callback result
     */
    fun forceShowInterstitial(
        context: Context,
        interstitialAd: MaxInterstitialAd?,
        callback: AdCallback?,
        shouldReload: Boolean
    ) {
        currentClicked = numShowAds
        showInterstitialAdByTimes(context, interstitialAd, callback, shouldReload)
    }

    /**
     * Hiển thị ads theo số lần được xác định trước và callback result
     */
    fun showInterstitialAdByTimes(
        context: Context,
        interstitialAd: MaxInterstitialAd?,
        callback: AdCallback?,
        shouldReloadAds: Boolean
    ) {
        AppLovinHelper.setupAppLovinData(context)
        if (AppPurchase.getInstance().isPurchased(context)) {
            callback?.onAdClosed()
            return
        }
        if (interstitialAd == null || !interstitialAd.isReady) {
            callback?.onAdClosed()
            return
        }

        interstitialAd.setRevenueListener { ad ->
            MaxLogEventManager.logPaidAdImpression(context, ad, AdType.INTERSTITIAL)
            if (tokenAdjust != null) {
                MaxLogEventManager.logPaidAdjustWithTokenMax(ad, ad.adUnitId, tokenAdjust)
            }
        }
        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd) {}

            override fun onAdDisplayed(ad: MaxAd) {
                AppOpenMax.getInstance().setInterstitialShowing(true)
                SharePreferenceUtils.setLastImpressionInterstitialTime(context)
            }

            override fun onAdHidden(ad: MaxAd) {
                AppOpenMax.getInstance().setInterstitialShowing(false)
                val appCompat = context as? AppCompatActivity
                if (callback != null && (appCompat == null || appCompat.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))) {
                    callback.onAdClosed()
                    if (shouldReloadAds) {
                        requestInterstitialAds(interstitialAd)
                    }
                    dialog?.dismiss()
                }
                Log.d(TAG, "onAdHidden: " + appCompat?.lifecycle?.currentState)
            }

            override fun onAdClicked(ad: MaxAd) {
                MaxLogEventManager.logClickAdsEvent(context, ad.adUnitId)
                callback?.onAdClicked()
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {}

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                Log.e(TAG, "onAdDisplayFailed: " + error.message)
                AppOpenMax.getInstance().setInterstitialShowing(false)
                if (shouldReloadAds) {
                    requestInterstitialAds(interstitialAd)
                }
                if (callback != null) {
                    callback.onAdClosed()
                    dialog?.dismiss()
                }
            }
        })
        if (AppLovinHelper.getNumClickAdsPerDay(context, interstitialAd.adUnitId) < maxClickAds) {
            showInterstitialAd(context, interstitialAd, callback)
            return
        }
        callback?.onAdClosed()
    }

    /**
     * Kiểm tra và hiện thị ads
     */
    private fun showInterstitialAd(context: Context, interstitialAd: MaxInterstitialAd?, callback: AdCallback?) {
        currentClicked++
        if (currentClicked >= numShowAds && interstitialAd != null) {
            if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog?.isShowing == true) {
                        dialog?.dismiss()
                    }
                    val act = context as? Activity
                    if (act != null && !act.isFinishing && !act.isDestroyed) {
                        dialog = PrepareLoadingAdsDialog(act)
                        try {
                            callback?.onInterstitialShow()
                            dialog?.setCancelable(false)
                            dialog?.show()
                        } catch (e: Exception) {
                            callback?.onAdClosed()
                            return
                        }
                    }
                } catch (e: Exception) {
                    dialog = null
                    e.printStackTrace()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    val act = context as? Activity
                    if (act != null && !act.isFinishing && !act.isDestroyed) {
                        interstitialAd.showAd()
                    }
                }, 800)
            }
            currentClicked = 0
        } else if (callback != null) {
            try {
                if (dialog?.isShowing == true) {
                    dialog?.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                dialog = null
            }
            callback.onAdClosed()
        }
    }

    /**
     * Load quảng cáo Banner Trong Activity
     */
    fun loadBanner(mActivity: Activity, id: String) {
        val adContainer: FrameLayout? = mActivity.findViewById(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout? = mActivity.findViewById(R.id.shimmer_container_banner)
        if (adContainer != null && containerShimmer != null) {
            loadBanner(mActivity, id, adContainer, containerShimmer)
        }
    }

    fun loadBanner(mActivity: Activity, id: String, adCallback: AdCallback?) {
        val adContainer: FrameLayout? = mActivity.findViewById(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout? = mActivity.findViewById(R.id.shimmer_container_banner)
        if (adContainer != null && containerShimmer != null) {
            loadBanner(mActivity, id, adContainer, containerShimmer, adCallback)
        }
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment
     */
    fun loadBannerFragment(mActivity: Activity, id: String, rootView: View) {
        val adContainer: FrameLayout? = rootView.findViewById(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout? = rootView.findViewById(R.id.shimmer_container_banner)
        if (adContainer != null && containerShimmer != null) {
            loadBanner(mActivity, id, adContainer, containerShimmer)
        }
    }

    fun loadBannerFragment(mActivity: Activity, id: String, rootView: View, adCallback: AdCallback?) {
        val adContainer: FrameLayout? = rootView.findViewById(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout? = rootView.findViewById(R.id.shimmer_container_banner)
        if (adContainer != null && containerShimmer != null) {
            loadBanner(mActivity, id, adContainer, containerShimmer, adCallback)
        }
    }

    private fun loadBanner(
        mActivity: Activity,
        id: String,
        adContainer: FrameLayout,
        containerShimmer: ShimmerFrameLayout
    ) {
        if (AppPurchase.getInstance().isPurchased(mActivity)) {
            containerShimmer.visibility = View.GONE
            return
        }
        containerShimmer.visibility = View.VISIBLE
        containerShimmer.startShimmer()
        val adView = MaxAdView(id, mActivity)
        adView.setRevenueListener { ad ->
            MaxLogEventManager.logPaidAdImpression(mActivity, ad, AdType.BANNER)
            if (tokenAdjust != null) {
                MaxLogEventManager.logPaidAdjustWithTokenMax(ad, ad.adUnitId, tokenAdjust)
            }
        }
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val heightPx = mActivity.resources.getDimensionPixelSize(R.dimen.banner_height)
        adView.layoutParams = FrameLayout.LayoutParams(width, heightPx)
        (adView.parent as? ViewGroup)?.removeView(adView)
        adContainer.removeAllViews()
        adContainer.addView(adView)
        adView.setListener(object : MaxAdViewAdListener {
            override fun onAdExpanded(ad: MaxAd) {}

            override fun onAdCollapsed(ad: MaxAd) {}

            override fun onAdLoaded(ad: MaxAd) {
                Log.d(TAG, "onAdLoaded: banner")
                containerShimmer.stopShimmer()
                containerShimmer.visibility = View.GONE
                adContainer.visibility = View.VISIBLE
            }

            override fun onAdDisplayed(ad: MaxAd) {}

            override fun onAdHidden(ad: MaxAd) {}

            override fun onAdClicked(ad: MaxAd) {
                MaxLogEventManager.logClickAdsEvent(context ?: mActivity, ad.adUnitId)
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                Log.e(TAG, "onAdLoadFailed: banner " + error.message + " code:" + error.code)
                containerShimmer.stopShimmer()
                adContainer.visibility = View.GONE
                containerShimmer.visibility = View.GONE
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {}
        })
        adView.loadAd()
    }

    private fun loadBanner(
        mActivity: Activity,
        id: String,
        adContainer: FrameLayout,
        containerShimmer: ShimmerFrameLayout,
        adCallback: AdCallback?
    ) {
        if (AppPurchase.getInstance().isPurchased(mActivity)) {
            containerShimmer.visibility = View.GONE
            return
        }
        containerShimmer.visibility = View.VISIBLE
        containerShimmer.startShimmer()
        val adView = MaxAdView(id, mActivity)
        adView.setRevenueListener { ad ->
            MaxLogEventManager.logPaidAdImpression(mActivity, ad, AdType.BANNER)
            if (tokenAdjust != null) {
                MaxLogEventManager.logPaidAdjustWithTokenMax(ad, ad.adUnitId, tokenAdjust)
            }
        }
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val heightPx = mActivity.resources.getDimensionPixelSize(R.dimen.banner_height)
        adView.layoutParams = FrameLayout.LayoutParams(width, heightPx)
        (adView.parent as? ViewGroup)?.removeView(adView)
        adContainer.removeAllViews()
        adContainer.addView(adView)
        adView.setListener(object : MaxAdViewAdListener {
            override fun onAdExpanded(ad: MaxAd) {}

            override fun onAdCollapsed(ad: MaxAd) {}

            override fun onAdLoaded(ad: MaxAd) {
                Log.d(TAG, "onAdLoaded: banner")
                containerShimmer.stopShimmer()
                containerShimmer.visibility = View.GONE
                adContainer.visibility = View.VISIBLE
            }

            override fun onAdDisplayed(ad: MaxAd) {
                adCallback?.onAdImpression()
            }

            override fun onAdHidden(ad: MaxAd) {}

            override fun onAdClicked(ad: MaxAd) {
                MaxLogEventManager.logClickAdsEvent(context ?: mActivity, ad.adUnitId)
                adCallback?.onAdClicked()
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                Log.e(TAG, "onAdLoadFailed: banner " + error.message + " code:" + error.code)
                containerShimmer.stopShimmer()
                adContainer.visibility = View.GONE
                containerShimmer.visibility = View.GONE
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {}
        })
        adView.loadAd()
    }

    fun loadNative(mActivity: Activity, adUnitId: String) {
        val frameLayout: FrameLayout? = mActivity.findViewById(R.id.fl_adplaceholder)
        val containerShimmer: ShimmerFrameLayout? = mActivity.findViewById(R.id.shimmer_container_native)
        if (frameLayout != null && containerShimmer != null) {
            loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, R.layout.custom_native_max_free_size)
        }
    }

    fun loadNativeSmall(mActivity: Activity, adUnitId: String) {
        val frameLayout: FrameLayout? = mActivity.findViewById(R.id.fl_adplaceholder)
        val containerShimmer: ShimmerFrameLayout? = mActivity.findViewById(R.id.shimmer_container_native)
        if (frameLayout != null && containerShimmer != null) {
            loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, R.layout.custom_native_max_medium)
        }
    }

    fun loadNativeFragment(mActivity: Activity, adUnitId: String, parent: View) {
        val frameLayout: FrameLayout? = parent.findViewById(R.id.fl_adplaceholder)
        val containerShimmer: ShimmerFrameLayout? = parent.findViewById(R.id.shimmer_container_native)
        if (frameLayout != null && containerShimmer != null) {
            loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, R.layout.custom_native_max_free_size)
        }
    }

    fun loadNativeSmallFragment(mActivity: Activity, adUnitId: String, parent: View) {
        val frameLayout: FrameLayout? = parent.findViewById(R.id.fl_adplaceholder)
        val containerShimmer: ShimmerFrameLayout? = parent.findViewById(R.id.shimmer_container_native)
        if (frameLayout != null && containerShimmer != null) {
            loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, R.layout.custom_native_max_medium)
        }
    }

    fun createNativeAdView(activity: Activity, layoutCustomNative: Int): MaxNativeAdView {
        val binder = MaxNativeAdViewBinder.Builder(layoutCustomNative)
            .setTitleTextViewId(R.id.ad_headline)
            .setBodyTextViewId(R.id.ad_body)
            .setAdvertiserTextViewId(R.id.ad_advertiser)
            .setIconImageViewId(R.id.ad_app_icon)
            .setMediaContentViewGroupId(R.id.ad_media)
            .setOptionsContentViewGroupId(R.id.ad_options_view)
            .setCallToActionButtonId(R.id.ad_call_to_action)
            .build()
        return MaxNativeAdView(binder, activity)
    }

    fun createNativeAdLoader(activity: Activity, adUnitId: String): MaxNativeAdLoader {
        val nativeAdLoader = MaxNativeAdLoader(adUnitId, activity)
        nativeAdLoader.setRevenueListener { ad ->
            MaxLogEventManager.logPaidAdImpression(activity, ad, AdType.NATIVE)
            if (tokenAdjust != null) {
                MaxLogEventManager.logPaidAdjustWithTokenMax(ad, ad.adUnitId, tokenAdjust)
            }
        }
        return nativeAdLoader
    }

    fun renderNativeAd(loader: MaxNativeAdLoader?, adView: MaxNativeAdView?, ad: MaxAd?) {
        if (loader != null && adView != null && ad != null) {
            loader.render(adView, ad)
        }
    }

    fun destroyNativeAd(loader: MaxNativeAdLoader?, ad: MaxAd?) {
        if (loader != null) {
            if (ad != null) {
                loader.destroy(ad)
            }
            loader.destroy()
        }
    }

    fun destroyNativeAd(loader: MaxNativeAdLoader?) {
        loader?.destroy()
    }

    fun destroyNativeAd() {
        if (lastNativeAdLoader != null) {
            if (loadedNativeAd != null) {
                lastNativeAdLoader?.destroy(loadedNativeAd)
                loadedNativeAd = null
            }
            lastNativeAdLoader?.destroy()
            lastNativeAdLoader = null
        }
    }

    fun loadNativeAd(
        activity: Activity,
        containerShimmer: ShimmerFrameLayout?,
        nativeAdLayout: FrameLayout?,
        id: String,
        layoutCustomNative: Int
    ) {
        val currentContext = context ?: activity
        if (AppPurchase.getInstance().isPurchased(currentContext)) {
            containerShimmer?.visibility = View.GONE
            return
        }
        containerShimmer?.visibility = View.VISIBLE
        containerShimmer?.startShimmer()

        nativeAdLayout?.removeAllViews()
        nativeAdLayout?.visibility = View.GONE

        nativeAdView = createNativeAdView(activity, layoutCustomNative)
        val nativeAdLoader = createNativeAdLoader(activity, id)
        lastNativeAdLoader = nativeAdLoader

        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(adView: MaxNativeAdView?, ad: MaxAd) {
                Log.d(TAG, "onNativeAdLoaded")
                if (loadedNativeAd != null) {
                    nativeAdLoader.destroy(loadedNativeAd)
                }
                loadedNativeAd = ad

                containerShimmer?.stopShimmer()
                containerShimmer?.visibility = View.GONE
                if (nativeAdLayout != null && adView != null) {
                    (adView.parent as? ViewGroup)?.removeView(adView)
                    nativeAdLayout.visibility = View.VISIBLE
                    nativeAdLayout.removeAllViews()
                    nativeAdLayout.addView(adView)
                }
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                Log.e(TAG, "onAdFailedToLoad: " + error.message)
                containerShimmer?.stopShimmer()
                containerShimmer?.visibility = View.GONE
                nativeAdLayout?.visibility = View.GONE
            }

            override fun onNativeAdClicked(ad: MaxAd) {
                Log.d(TAG, "onNativeAdClicked")
                MaxLogEventManager.logClickAdsEvent(currentContext, ad.adUnitId)
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
            }

            override fun onNativeAdExpired(nativeAd: MaxAd) {
                Log.d(TAG, "onNativeAdExpired: reloading expired ad")
                nativeAdLoader.loadAd(createNativeAdView(activity, layoutCustomNative))
            }
        })
        nativeAdLoader.loadAd(nativeAdView)
    }

    fun loadNativeAd(
        activity: Activity,
        containerShimmer: ShimmerFrameLayout?,
        nativeAdLayout: FrameLayout?,
        id: String,
        layoutCustomNative: Int,
        callback: AppLovinCallback?
    ) {
        val currentContext = context ?: activity
        if (AppPurchase.getInstance().isPurchased(currentContext)) {
            containerShimmer?.visibility = View.GONE
            callback?.onAdClosed()
            return
        }
        containerShimmer?.visibility = View.VISIBLE
        containerShimmer?.startShimmer()

        nativeAdLayout?.removeAllViews()
        nativeAdLayout?.visibility = View.GONE

        nativeAdView = createNativeAdView(activity, layoutCustomNative)
        val nativeAdLoader = createNativeAdLoader(activity, id)
        lastNativeAdLoader = nativeAdLoader

        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(adView: MaxNativeAdView?, ad: MaxAd) {
                Log.d(TAG, "onNativeAdLoaded")
                if (loadedNativeAd != null) {
                    nativeAdLoader.destroy(loadedNativeAd)
                }
                loadedNativeAd = ad

                if (nativeAdLayout != null && containerShimmer != null) {
                    populateNativeAdView(adView, nativeAdLayout, containerShimmer)
                } else {
                    containerShimmer?.stopShimmer()
                    containerShimmer?.visibility = View.GONE
                    if (nativeAdLayout != null && adView != null) {
                        (adView.parent as? ViewGroup)?.removeView(adView)
                        nativeAdLayout.visibility = View.VISIBLE
                        nativeAdLayout.removeAllViews()
                        nativeAdLayout.addView(adView)
                    }
                }
                callback?.onNativeAdLoaded(adView, ad)
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                Log.e(TAG, "onAdFailedToLoad: " + error.message)
                containerShimmer?.stopShimmer()
                containerShimmer?.visibility = View.GONE
                nativeAdLayout?.visibility = View.GONE
                callback?.onAdFailedToLoad(error)
            }

            override fun onNativeAdClicked(ad: MaxAd) {
                Log.d(TAG, "onNativeAdClicked")
                MaxLogEventManager.logClickAdsEvent(currentContext, ad.adUnitId)
                callback?.onAdClicked()
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
            }

            override fun onNativeAdExpired(nativeAd: MaxAd) {
                Log.d(TAG, "onNativeAdExpired")
                callback?.onNativeAdExpired(nativeAd)
                nativeAdLoader.loadAd(createNativeAdView(activity, layoutCustomNative))
            }
        })
        nativeAdLoader.loadAd(nativeAdView)
    }

    fun populateNativeAdView(apNativeAd: View?, frAds: FrameLayout?, shimmerAds: ShimmerFrameLayout?) {
        if (shimmerAds != null) {
            shimmerAds.stopShimmer()
            shimmerAds.visibility = View.GONE
        }
        if (frAds != null) {
            frAds.visibility = View.VISIBLE
            frAds.removeAllViews()
            if (apNativeAd != null && apNativeAd.parent != null) {
                (apNativeAd.parent as? ViewGroup)?.removeAllViews()
            }
            if (apNativeAd != null) {
                frAds.addView(apNativeAd)
            }
        }
    }

    fun loadNativeAd(activity: Activity, id: String, layoutCustomNative: Int, callback: AppLovinCallback?) {
        val currentContext = context ?: activity
        if (AppPurchase.getInstance().isPurchased(currentContext)) {
            callback?.onAdClosed()
            return
        }

        nativeAdView = createNativeAdView(activity, layoutCustomNative)
        val nativeAdLoader = createNativeAdLoader(activity, id)
        lastNativeAdLoader = nativeAdLoader

        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(adView: MaxNativeAdView?, ad: MaxAd) {
                Log.d(TAG, "onNativeAdLoaded")
                if (loadedNativeAd != null) {
                    nativeAdLoader.destroy(loadedNativeAd)
                }
                loadedNativeAd = ad
                callback?.onNativeAdLoaded(adView, ad)
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                Log.e(TAG, "onAdFailedToLoad: " + error.message)
                callback?.onAdFailedToLoad(error)
            }

            override fun onNativeAdClicked(ad: MaxAd) {
                Log.d(TAG, "onNativeAdClicked")
                MaxLogEventManager.logClickAdsEvent(currentContext, ad.adUnitId)
                callback?.onAdClicked()
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
            }

            override fun onNativeAdExpired(nativeAd: MaxAd) {
                Log.d(TAG, "onNativeAdExpired")
                callback?.onNativeAdExpired(nativeAd)
                nativeAdLoader.loadAd(createNativeAdView(activity, layoutCustomNative))
            }
        })
        nativeAdLoader.loadAd(nativeAdView)
    }

    fun getNativeRepeatAdapter(
        activity: Activity,
        id: String,
        layoutCustomNative: Int,
        originalAdapter: RecyclerView.Adapter<*>,
        listener: MaxAdPlacer.Listener?,
        repeatingInterval: Int
    ): MaxRecyclerAdapter {
        val settings = MaxAdPlacerSettings(id)
        settings.repeatingInterval = repeatingInterval

        val adAdapter = MaxRecyclerAdapter(settings, originalAdapter, activity)
        adAdapter.adPlacer.setAdSize(-1, -1)

        val binder = MaxNativeAdViewBinder.Builder(layoutCustomNative)
            .setTitleTextViewId(R.id.ad_headline)
            .setBodyTextViewId(R.id.ad_body)
            .setAdvertiserTextViewId(R.id.ad_advertiser)
            .setIconImageViewId(R.id.ad_app_icon)
            .setMediaContentViewGroupId(R.id.ad_media)
            .setOptionsContentViewGroupId(R.id.ad_options_view)
            .setCallToActionButtonId(R.id.ad_call_to_action)
            .build()

        adAdapter.adPlacer.setNativeAdViewBinder(binder)
        if (listener != null) {
            adAdapter.setListener(listener)
        }
        return adAdapter
    }

    fun getNativeFixedPositionAdapter(
        activity: Activity,
        id: String,
        layoutCustomNative: Int,
        originalAdapter: RecyclerView.Adapter<*>,
        listener: MaxAdPlacer.Listener?,
        position: Int
    ): MaxRecyclerAdapter {
        val settings = MaxAdPlacerSettings(id)
        settings.addFixedPosition(position)

        val adAdapter = MaxRecyclerAdapter(settings, originalAdapter, activity)
        adAdapter.adPlacer.setAdSize(-1, -1)

        val binder = MaxNativeAdViewBinder.Builder(layoutCustomNative)
            .setTitleTextViewId(R.id.ad_headline)
            .setBodyTextViewId(R.id.ad_body)
            .setAdvertiserTextViewId(R.id.ad_advertiser)
            .setIconImageViewId(R.id.ad_app_icon)
            .setMediaContentViewGroupId(R.id.ad_media)
            .setOptionsContentViewGroupId(R.id.ad_options_view)
            .setCallToActionButtonId(R.id.ad_call_to_action)
            .build()

        adAdapter.adPlacer.setNativeAdViewBinder(binder)
        if (listener != null) {
            adAdapter.setListener(listener)
        }
        return adAdapter
    }

    fun getRewardAd(activity: Activity, id: String, callback: AppLovinCallback): MaxRewardedAd {
        val rewardedAd = MaxRewardedAd.getInstance(id, activity)
        rewardedAd.setListener(object : MaxRewardedAdListener {
            override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
                callback.onUserRewarded(reward)
                Log.d(TAG, "onUserRewarded: ")
            }

            override fun onAdLoaded(ad: MaxAd) {
                Log.d(TAG, "onAdLoaded: ")
                callback.onAdLoaded()
            }

            override fun onAdDisplayed(ad: MaxAd) {
                Log.d(TAG, "onAdDisplayed: ")
            }

            override fun onAdHidden(ad: MaxAd) {
                callback.onAdClosed()
                Log.d(TAG, "onAdHidden: ")
                rewardedAd.loadAd()
            }

            override fun onAdClicked(ad: MaxAd) {
                MaxLogEventManager.logClickAdsEvent(context ?: activity, ad.adUnitId)
                callback.onAdClicked()
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                Log.d(TAG, "onAdLoadFailed: " + error.message)
                callback.onAdFailedToLoad(error)
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                Log.d(TAG, "onAdDisplayFailed: " + error.message)
                rewardedAd.loadAd()
                callback.onAdFailedToShow(error)
            }
        })
        rewardedAd.loadAd()
        return rewardedAd
    }

    fun getRewardAd(activity: Activity, id: String): MaxRewardedAd {
        val rewardedAd = MaxRewardedAd.getInstance(id, activity)
        rewardedAd.loadAd()
        return rewardedAd
    }

    fun showRewardAd(activity: Activity, maxRewardedAd: MaxRewardedAd?, callback: AppLovinCallback?) {
        if (maxRewardedAd != null && maxRewardedAd.isReady) {
            maxRewardedAd.setRevenueListener { ad ->
                MaxLogEventManager.logPaidAdImpression(activity, ad, AdType.REWARDED)
                if (tokenAdjust != null) {
                    MaxLogEventManager.logPaidAdjustWithTokenMax(ad, ad.adUnitId, tokenAdjust)
                }
            }
            maxRewardedAd.setListener(object : MaxRewardedAdListener {
                override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
                    callback?.onUserRewarded(reward)
                    Log.d(TAG, "onUserRewarded: ")
                }

                override fun onAdLoaded(ad: MaxAd) {
                    Log.d(TAG, "onAdLoaded: ")
                    callback?.onAdLoaded()
                }

                override fun onAdDisplayed(ad: MaxAd) {
                    Log.d(TAG, "onAdDisplayed: ")
                    AppOpenMax.getInstance().setInterstitialShowing(true)
                }

                override fun onAdHidden(ad: MaxAd) {
                    AppOpenMax.getInstance().setInterstitialShowing(false)
                    callback?.onAdClosed()
                    Log.d(TAG, "onAdHidden: ")
                    maxRewardedAd.loadAd()
                }

                override fun onAdClicked(ad: MaxAd) {
                    MaxLogEventManager.logClickAdsEvent(context ?: activity, ad.adUnitId)
                    callback?.onAdClicked()
                    if (disableAdResumeWhenClickAds) {
                        AppOpenMax.getInstance().disableAdResumeByClickAction()
                    }
                }

                override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                    Log.d(TAG, "onAdLoadFailed: " + error.message)
                    callback?.onAdFailedToLoad(error)
                }

                override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                    Log.d(TAG, "onAdDisplayFailed: " + error.message)
                    AppOpenMax.getInstance().setInterstitialShowing(false)
                    maxRewardedAd.loadAd()
                    callback?.onAdFailedToShow(error)
                }
            })
            maxRewardedAd.showAd()
        } else {
            Log.e(TAG, "showRewardAd error - reward ad not ready")
            callback?.onAdFailedToShow(null)
        }
    }

    fun showRewardAd(activity: Activity, maxRewardedAd: MaxRewardedAd?, tokenAdjust: String?) {
        if (maxRewardedAd != null && maxRewardedAd.isReady) {
            maxRewardedAd.setRevenueListener { ad ->
                MaxLogEventManager.logPaidAdImpression(activity, ad, AdType.REWARDED)
                if (tokenAdjust != null) {
                    MaxLogEventManager.logPaidAdjustWithTokenMax(ad, ad.adUnitId, tokenAdjust)
                }
            }
            maxRewardedAd.showAd()
        } else {
            Log.e(TAG, "showRewardAd error - reward ad not ready")
        }
    }

    fun destroyInterstitial(interstitialAd: MaxInterstitialAd?) {
        interstitialAd?.destroy()
    }

    fun destroyRewardAd(rewardedAd: MaxRewardedAd?) {
        rewardedAd?.destroy()
    }

    fun destroyBanner(bannerAd: MaxAdView?) {
        bannerAd?.destroy()
    }

    fun destroyBanner(adContainer: ViewGroup?) {
        if (adContainer != null) {
            for (i in 0 until adContainer.childCount) {
                val child = adContainer.getChildAt(i)
                if (child is MaxAdView) {
                    child.destroy()
                }
            }
            adContainer.removeAllViews()
        }
    }

    fun loadMrec(
        mActivity: Activity,
        id: String,
        adContainer: FrameLayout?,
        containerShimmer: ShimmerFrameLayout?
    ) {
        loadMrec(mActivity, id, adContainer, containerShimmer, null)
    }

    fun loadMrec(
        mActivity: Activity,
        id: String,
        adContainer: FrameLayout?,
        containerShimmer: ShimmerFrameLayout?,
        adCallback: AdCallback?
    ) {
        if (AppPurchase.getInstance().isPurchased(mActivity)) {
            containerShimmer?.visibility = View.GONE
            return
        }
        containerShimmer?.visibility = View.VISIBLE
        containerShimmer?.startShimmer()
        val adView = MaxAdView(id, MaxAdFormat.MREC, mActivity)
        adView.setRevenueListener { ad ->
            MaxLogEventManager.logPaidAdImpression(mActivity, ad, AdType.BANNER)
            if (tokenAdjust != null) {
                MaxLogEventManager.logPaidAdjustWithTokenMax(ad, ad.adUnitId, tokenAdjust)
            }
        }
        val widthPx = AppLovinSdkUtils.dpToPx(mActivity, 300)
        val heightPx = AppLovinSdkUtils.dpToPx(mActivity, 250)
        adView.layoutParams = FrameLayout.LayoutParams(widthPx, heightPx)
        if (adContainer != null) {
            adContainer.removeAllViews()
            adContainer.addView(adView)
        }
        adView.setListener(object : MaxAdViewAdListener {
            override fun onAdExpanded(ad: MaxAd) {}

            override fun onAdCollapsed(ad: MaxAd) {}

            override fun onAdLoaded(ad: MaxAd) {
                Log.d(TAG, "onAdLoaded: mrec")
                containerShimmer?.stopShimmer()
                containerShimmer?.visibility = View.GONE
                adContainer?.visibility = View.VISIBLE
            }

            override fun onAdDisplayed(ad: MaxAd) {
                adCallback?.onAdImpression()
            }

            override fun onAdHidden(ad: MaxAd) {}

            override fun onAdClicked(ad: MaxAd) {
                MaxLogEventManager.logClickAdsEvent(context ?: mActivity, ad.adUnitId)
                adCallback?.onAdClicked()
                if (disableAdResumeWhenClickAds) {
                    AppOpenMax.getInstance().disableAdResumeByClickAction()
                }
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                Log.e(TAG, "onAdLoadFailed: mrec " + error.message + " code:" + error.code)
                containerShimmer?.stopShimmer()
                containerShimmer?.visibility = View.GONE
                adContainer?.visibility = View.GONE
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {}
        })
        adView.loadAd()
    }
}
