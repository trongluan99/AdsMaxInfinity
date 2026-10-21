package com.ads.max.event

import android.content.Context
import android.os.Bundle
import com.ads.max.config.MaxAdConfig
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseAnalyticsUtil {
    private const val TAG = "FirebaseAnalyticsUtil"

    @JvmStatic
    fun logEventWithAds(context: Context, params: Bundle?) {
        FirebaseAnalytics.getInstance(context).logEvent("paid_ad_impression", params)
    }

    @JvmStatic
    fun logPaidAdImpressionValue(context: Context, bundle: Bundle?, mediationProvider: Int) {
        if (mediationProvider == MaxAdConfig.PROVIDER_MAX) {
            FirebaseAnalytics.getInstance(context).logEvent("max_paid_ad_impression_value", bundle)
        } else {
            FirebaseAnalytics.getInstance(context).logEvent("paid_ad_impression_value", bundle)
        }
    }

    @JvmStatic
    fun logClickAdsEvent(context: Context, bundle: Bundle?) {
        FirebaseAnalytics.getInstance(context).logEvent("event_user_click_ads", bundle)
    }

    @JvmStatic
    fun logCurrentTotalRevenueAd(context: Context, eventName: String, bundle: Bundle?) {
        FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle)
    }

    @JvmStatic
    fun logTotalRevenue001Ad(context: Context, bundle: Bundle?) {
        FirebaseAnalytics.getInstance(context).logEvent("paid_ad_impression_value_001", bundle)
    }
}
