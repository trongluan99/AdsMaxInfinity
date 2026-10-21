package com.ads.max.applovin

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.ads.max.ads.MaxAdCallback
import com.ads.max.ads.wrapper.ApAdError
import com.ads.max.billing.AppPurchase
import com.ads.max.config.MaxAdConfig
import com.ads.max.dialog.ResumeLoadingDialog
import com.ads.max.event.MaxLogEventManager
import com.ads.max.funtion.AdType
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.applovin.sdk.AppLovinSdk
import java.util.concurrent.CopyOnWriteArrayList

class AppOpenMax private constructor() : Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private var appOpenAd: MaxAppOpenAd? = null
    private var myApplication: Application? = null
    private var currentActivity: Activity? = null
    private var dialog: Dialog? = null
    private val disabledAppOpenList: MutableList<Class<*>> = CopyOnWriteArrayList()
    private var isAppResumeEnabled = true
    private var isInterstitialShowing = false
    private var disableAdResumeByClickAction = false
    private var displayAdResume = false
    private var isInitialized = false // on - off ad resume on app
    private var maxAdCallback: MaxAdCallback? = null

    private var openAdSplash: MaxAppOpenAd? = null
    private var isShowingAd = false

    companion object {
        private const val TAG = "AppOpenMax"

        @Volatile
        private var INSTANCE: AppOpenMax? = null

        @JvmStatic
        @Synchronized
        fun getInstance(): AppOpenMax {
            if (INSTANCE == null) {
                INSTANCE = AppOpenMax()
            }
            return INSTANCE!!
        }
    }

    fun init(application: Application, appOpenAdId: String?) {
        isInitialized = true
        disableAdResumeByClickAction = false
        this.myApplication = application
        this.myApplication?.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        loadAdResumeMax(application, appOpenAdId)
    }

    // Policy Max: Load sau Ad Splash
    fun loadAdResumeMax(application: Application, appOpenAdId: String?) {
        if (appOpenAdId.isNullOrBlank()) {
            Log.e(TAG, "loadAdResumeMax: appOpenAdId is null or empty")
            return
        }
        if (!AppLovinSdk.getInstance(application).isInitialized) {
            Log.e(TAG, "loadAdResumeMax: AppLovin SDK is not initialized yet. Please wait for SDK initialization.")
            return
        }
        appOpenAd = MaxAppOpenAd(appOpenAdId, application).apply {
            setListener(object : MaxAdListener {
                override fun onAdLoaded(ad: MaxAd) {
                    Log.d(TAG, "onAdLoaded: ")
                    maxAdCallback?.onAdLoaded()
                }

                override fun onAdDisplayed(ad: MaxAd) {
                    displayAdResume = true
                    Log.d(TAG, "onAdDisplayed: ")
                    maxAdCallback?.onAdImpression()
                }

                override fun onAdHidden(ad: MaxAd) {
                    Log.d(TAG, "onAdHidden: ")
                    appOpenAd?.loadAd()
                    dismissDialogLoading()
                    displayAdResume = false
                    maxAdCallback?.onAdClosed()
                }

                override fun onAdClicked(ad: MaxAd) {
                    Log.d(TAG, "onAdClicked: ")
                    disableAdResumeByClickAction = true
                    maxAdCallback?.onAdClicked()
                }

                override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                    Log.d(TAG, "onAdLoadFailed: ")
                    dismissDialogLoading()
                    maxAdCallback?.onAdFailedToLoad(ApAdError(error))
                }

                override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                    Log.d(TAG, "onAdDisplayFailed: ")
                    appOpenAd?.loadAd()
                    dismissDialogLoading()
                    maxAdCallback?.onAdFailedToShow(ApAdError(error))
                }
            })
            setRevenueListener { ad ->
                MaxLogEventManager.logPaidAdImpression(application, ad, AdType.APP_OPEN)
                MaxLogEventManager.logPaidAdjustWithTokenMax(ad, ad.adUnitId, MaxAdConfig.ADJUST_TOKEN_TIKTOK)
            }
            loadAd()
        }
    }

    /**
     * Disable app open app on specific activity
     *
     * @param activityClass
     */
    fun disableAppResumeWithActivity(activityClass: Class<*>) {
        Log.d(TAG, "disableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList.add(activityClass)
    }

    fun enableAppResumeWithActivity(activityClass: Class<*>) {
        Log.d(TAG, "enableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList.remove(activityClass)
    }

    fun disableAppResume() {
        isAppResumeEnabled = false
    }

    fun enableAppResume() {
        isAppResumeEnabled = true
    }

    fun isInterstitialShowing(): Boolean {
        return isInterstitialShowing
    }

    fun setInterstitialShowing(interstitialShowing: Boolean) {
        isInterstitialShowing = interstitialShowing
    }

    /**
     * Call disable ad resume when click a button, auto enable ad resume in next start
     */
    fun disableAdResumeByClickAction() {
        disableAdResumeByClickAction = true
    }

    fun setDisableAdResumeByClickAction(disableAdResumeByClickAction: Boolean) {
        this.disableAdResumeByClickAction = disableAdResumeByClickAction
    }

    fun setAppOpenMaxCallback(maxAdCallback: MaxAdCallback?) {
        this.maxAdCallback = maxAdCallback
    }

    private fun showAdIfReady() {
        val app = myApplication ?: return
        val current = currentActivity ?: return
        val openAd = appOpenAd ?: return

        if (current.isFinishing || current.isDestroyed) {
            return
        }

        if (!AppLovinSdk.getInstance(app).isInitialized || AppPurchase.getInstance().isPurchased(current)) {
            return
        }
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            && isNetworkAvailable()
        ) {
            try {
                dismissDialogLoading()
                if (!current.isFinishing && !current.isDestroyed) {
                    dialog = ResumeLoadingDialog(current)
                    dialog?.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "showAdIfReady: " + e.message)
                dialog = null
            }
            if (openAd.isReady) {
                Handler(Looper.getMainLooper()).postDelayed({
                    val act = currentActivity
                    if (act != null && !act.isFinishing && !act.isDestroyed && openAd.isReady) {
                        openAd.showAd()
                    }
                }, 500)
            } else {
                openAd.loadAd()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onResume() {
        if (!isAppResumeEnabled) {
            Log.d(TAG, "onResume: app resume is disabled")
            return
        }

        if (disableAdResumeByClickAction) {
            Log.d(TAG, "onResume:ad resume disable ad by action")
            disableAdResumeByClickAction = false
            return
        }

        if (isInterstitialShowing) {
            Log.d(TAG, "onResume: interstitial is showing")
            return
        }

        if (displayAdResume) {
            Log.d(TAG, "onResume: AppOpen is showing")
            return
        }

        try {
            val current = currentActivity
            if (current != null) {
                for (activity in disabledAppOpenList) {
                    if (activity.name == current.javaClass.name) {
                        Log.d(TAG, "onStart: activity is disabled")
                        return
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showAdIfReady()
    }

    private fun dismissDialogLoading() {
        try {
            if (dialog?.isShowing == true) {
                dialog?.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            dialog = null
        }
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
        Log.d(TAG, "onActivityStarted: $currentActivity")
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        Log.d(TAG, "onActivityResumed: $currentActivity")
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
        Log.d(TAG, "onActivityDestroyed: null")
    }

    fun loadOpenMaxSplash(
        context: Context?,
        idOpenSplash: String?,
        timeDelay: Int,
        timeOut: Int,
        isShowAdIfReady: Boolean,
        adCallback: AppLovinCallback?
    ) {
        val ctx = context ?: myApplication
        if (ctx == null) {
            adCallback?.onNextAction()
            return
        }
        if (this.myApplication == null) {
            if (ctx is Application) {
                this.myApplication = ctx
            } else if (ctx.applicationContext is Application) {
                this.myApplication = ctx.applicationContext as Application
            }
        }
        if (!isNetworkAvailable(ctx)) {
            Handler(Looper.getMainLooper()).postDelayed({
                adCallback?.onNextAction()
            }, timeDelay.toLong())
        } else if (idOpenSplash.isNullOrBlank()) {
            Log.e(TAG, "loadOpenMaxSplash: idOpenSplash is null or empty")
            adCallback?.onNextAction()
        } else {
            val currentTimeMillis = System.currentTimeMillis()
            val timeOutRunnable = Runnable {
                adCallback?.onNextAction()
                isShowingAd = false
            }
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(timeOutRunnable, timeOut.toLong())
            openAdSplash = MaxAppOpenAd(idOpenSplash, ctx).apply {
                setListener(object : MaxAdListener {
                    override fun onAdLoaded(maxAd: MaxAd) {
                        handler.removeCallbacks(timeOutRunnable)
                        if (isShowAdIfReady) {
                            var elapsedTime = System.currentTimeMillis() - currentTimeMillis
                            if (elapsedTime >= timeDelay) {
                                elapsedTime = 0L
                            }

                            val handler1 = Handler(Looper.getMainLooper())
                            val showAppOpenSplashRunnable = Runnable {
                                showAdIfReady(ctx, adCallback)
                            }
                            handler1.postDelayed(showAppOpenSplashRunnable, elapsedTime)
                        } else {
                            adCallback?.onAdSplashReady()
                        }
                    }

                    override fun onAdDisplayed(maxAd: MaxAd) {
                        Log.d(TAG, "onAdDisplayed: ")
                    }

                    override fun onAdHidden(maxAd: MaxAd) {
                        Log.d(TAG, "onAdHidden: ")
                    }

                    override fun onAdClicked(maxAd: MaxAd) {
                        Log.d(TAG, "onAdClicked: ")
                    }

                    override fun onAdLoadFailed(s: String, maxError: MaxError) {
                        Log.d(TAG, "onAdLoadFailed: " + maxError.message)
                        handler.removeCallbacks(timeOutRunnable)
                        adCallback?.onAdLoadFailed()
                        adCallback?.onNextAction()
                    }

                    override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
                        Log.d(TAG, "onAdDisplayFailed: " + maxError.message)
                        handler.removeCallbacks(timeOutRunnable)
                        adCallback?.onAdDisplayFailed()
                        adCallback?.onNextAction()
                    }
                })
                loadAd()
            }
        }
    }

    private fun showAdIfReady(context: Context?, adCallback: AppLovinCallback?) {
        if (context == null || !AppLovinSdk.getInstance(context).isInitialized) {
            adCallback?.onNextAction()
            return
        }

        val act = context as? Activity
        if (act != null && (act.isFinishing || act.isDestroyed)) {
            adCallback?.onNextAction()
            return
        }

        val splash = openAdSplash
        if (splash != null && splash.isReady) {
            splash.setListener(object : MaxAdListener {
                override fun onAdLoaded(maxAd: MaxAd) {
                    adCallback?.onAdLoaded()
                }

                override fun onAdDisplayed(maxAd: MaxAd) {
                    isShowingAd = true
                    adCallback?.onAdDisplayed()
                }

                override fun onAdHidden(maxAd: MaxAd) {
                    isShowingAd = false
                    adCallback?.onNextAction()
                }

                override fun onAdClicked(maxAd: MaxAd) {
                    adCallback?.onAdClicked()
                }

                override fun onAdLoadFailed(s: String, maxError: MaxError) {
                    adCallback?.onAdLoadFailed()
                }

                override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
                    isShowingAd = false
                    adCallback?.onAdDisplayFailed()
                    adCallback?.onNextAction()
                }
            })
            splash.setRevenueListener { maxAd ->
                MaxLogEventManager.logPaidAdImpression(context, maxAd, AdType.APP_OPEN)
                MaxLogEventManager.logPaidAdjustWithTokenMax(maxAd, maxAd.adUnitId, MaxAdConfig.ADJUST_TOKEN_TIKTOK)
            }
            splash.showAd()
        } else {
            adCallback?.onNextAction()
        }
    }

    fun onCheckShowSplashWhenFail(activity: AppCompatActivity, adCallback: AppLovinCallback?, timeDelay: Int) {
        Handler(activity.mainLooper).postDelayed({
            if (openAdSplash != null && !isShowingAd) {
                showAdIfReady(activity, adCallback)
            }
        }, timeDelay.toLong())
    }

    fun setApplication(application: Application) {
        this.myApplication = application
    }

    private fun isNetworkAvailable(context: Context?): Boolean {
        val ctx = context ?: myApplication ?: return false
        return try {
            val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network: " + e.message)
            true
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return isNetworkAvailable(myApplication)
    }

    fun destroy() {
        if (appOpenAd != null) {
            appOpenAd?.destroy()
            appOpenAd = null
        }
        if (openAdSplash != null) {
            openAdSplash?.destroy()
            openAdSplash = null
        }
    }
}
