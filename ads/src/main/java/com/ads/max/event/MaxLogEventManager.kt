package com.ads.max.event

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import com.ads.max.funtion.AdType
import com.ads.max.util.AppUtil
import com.ads.max.util.SharePreferenceUtils
import com.applovin.mediation.MaxAd
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import java.math.BigDecimal
import java.util.Currency

object MaxLogEventManager {

    private const val TAG = "AmxLogEventManager"

    @JvmStatic
    fun logPaidAdjustWithTokenMax(adValue: MaxAd, adUnitId: String?, token: String?) {
        val adjustEvent = AdjustEvent(token)
        val value = adValue.revenue
        adjustEvent.setRevenue(value, "USD")
        adjustEvent.setOrderId(adUnitId)
        Adjust.trackEvent(adjustEvent)
    }

    @JvmStatic
    fun logPaidAdImpression(context: Context, adValue: MaxAd, adType: AdType) {
        logEventWithMaxAds(context, adValue)
        MaxAdjust.pushTrackEventApplovin(adValue, context)
        MaxAppsflyer.getInstance().pushTrackEventApplovin(adValue, adType)

        // Log revenue Facebook 30/05/2024
        val value = adValue.revenue * 26000
        AppEventsLogger.newLogger(context).logPurchase(BigDecimal.valueOf(value), Currency.getInstance("VND"))
    }

    @JvmStatic
    private fun logEventWithMaxAds(context: Context, impressionData: MaxAd) {
        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val revenue = impressionData.revenue // In USD
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.AD_PLATFORM, "AppLovin")
            putString(FirebaseAnalytics.Param.AD_SOURCE, impressionData.networkName)
            putString(FirebaseAnalytics.Param.AD_FORMAT, impressionData.format.label)
            putString(FirebaseAnalytics.Param.AD_UNIT_NAME, impressionData.adUnitId)
            putDouble(FirebaseAnalytics.Param.VALUE, revenue)
            putString(FirebaseAnalytics.Param.CURRENCY, "USD") // All Applovin revenue is sent in USD
        }
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, params)
    }

    @JvmStatic
    private fun logEventWithAds(
        context: Context,
        revenue: Float,
        precision: Int,
        adUnitId: String?,
        network: String?,
        mediationProvider: Int
    ) {
        Log.d(
            TAG, String.format(
                "Paid event of value %.0f microcents in currency USD of precision %s%n occurred for ad unit %s from ad network %s.mediation provider: %s%n",
                revenue,
                precision,
                adUnitId,
                network, mediationProvider
            )
        )

        val params = Bundle().apply {
            putDouble("valuemicros", revenue.toDouble())
            putString("currency", "USD")
            // These values below won’t be used in ROAS recipe.
            // But log for purposes of debugging and future reference.
            putInt("precision", precision)
            putString("adunitid", adUnitId)
            putString("network", network)
        }

        // log revenue this ad
        logPaidAdImpressionValue(context, revenue / 1000000.0, precision, adUnitId, network, mediationProvider)
        FirebaseAnalyticsUtil.logEventWithAds(context, params)
        FacebookEventUtils.logEventWithAds(context, params)
        // update current total revenue ads
        SharePreferenceUtils.updateCurrentTotalRevenueAd(context, revenue)
        logCurrentTotalRevenueAd(context, "event_current_total_revenue_ad")

        // update current total revenue ads for event paid_ad_impression_value_0.01
        AppUtil.currentTotalRevenue001Ad += revenue
        SharePreferenceUtils.updateCurrentTotalRevenue001Ad(context, AppUtil.currentTotalRevenue001Ad)
        logTotalRevenue001Ad(context)

        logTotalRevenueAdIn3DaysIfNeed(context)
        logTotalRevenueAdIn7DaysIfNeed(context)
    }

    @JvmStatic
    private fun logPaidAdImpressionValue(
        context: Context,
        value: Double,
        precision: Int,
        adunitid: String?,
        network: String?,
        mediationProvider: Int
    ) {
        val params = Bundle().apply {
            putDouble("value", value)
            putString("currency", "USD")
            putInt("precision", precision)
            putString("adunitid", adunitid)
            putString("network", network)
        }

        MaxAdjust.logPaidAdImpressionValue(value, "USD")
        FirebaseAnalyticsUtil.logPaidAdImpressionValue(context, params, mediationProvider)
        FacebookEventUtils.logPaidAdImpressionValue(context, params, mediationProvider)
    }

    @JvmStatic
    fun logClickAdsEvent(context: Context, adUnitId: String?) {
        Log.d(TAG, String.format("User click ad for ad unit %s.", adUnitId))
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
        }

        FirebaseAnalyticsUtil.logClickAdsEvent(context, bundle)
        FacebookEventUtils.logClickAdsEvent(context, bundle)
    }

    @JvmStatic
    fun logCurrentTotalRevenueAd(context: Context, eventName: String) {
        val currentTotalRevenue = SharePreferenceUtils.getCurrentTotalRevenueAd(context)
        val bundle = Bundle().apply {
            putFloat("value", currentTotalRevenue)
        }

        FirebaseAnalyticsUtil.logCurrentTotalRevenueAd(context, eventName, bundle)
        FacebookEventUtils.logCurrentTotalRevenueAd(context, eventName, bundle)
    }

    @JvmStatic
    fun logTotalRevenue001Ad(context: Context) {
        val revenue = AppUtil.currentTotalRevenue001Ad
        if (revenue / 1000000 >= 0.01) {
            AppUtil.currentTotalRevenue001Ad = 0f
            SharePreferenceUtils.updateCurrentTotalRevenue001Ad(context, 0f)
            val bundle = Bundle().apply {
                putFloat("value", revenue / 1000000)
            }
            FirebaseAnalyticsUtil.logTotalRevenue001Ad(context, bundle)
            FacebookEventUtils.logTotalRevenue001Ad(context, bundle)
        }
    }

    @JvmStatic
    fun logTotalRevenueAdIn3DaysIfNeed(context: Context) {
        val installTime = SharePreferenceUtils.getInstallTime(context)
        if (!SharePreferenceUtils.isPushRevenue3Day(context)
            && (System.currentTimeMillis() - installTime >= 3L * 24 * 60 * 60 * 1000)
        ) {
            Log.d(TAG, "logTotalRevenueAdAt3DaysIfNeed: ")
            logCurrentTotalRevenueAd(context, "event_total_revenue_ad_in_3_days")
            SharePreferenceUtils.setPushedRevenue3Day(context)
        }
    }

    @JvmStatic
    fun logTotalRevenueAdIn7DaysIfNeed(context: Context) {
        val installTime = SharePreferenceUtils.getInstallTime(context)
        if (!SharePreferenceUtils.isPushRevenue7Day(context)
            && (System.currentTimeMillis() - installTime >= 7L * 24 * 60 * 60 * 1000)
        ) {
            Log.d(TAG, "logTotalRevenueAdAt7DaysIfNeed: ")
            logCurrentTotalRevenueAd(context, "event_total_revenue_ad_in_7_days")
            SharePreferenceUtils.setPushedRevenue7Day(context)
        }
    }

    @JvmStatic
    fun setEventNamePurchaseAdjust(eventNamePurchase: String) {
        MaxAdjust.setEventNamePurchase(eventNamePurchase)
    }

    @JvmStatic
    fun trackAdRevenue(id: String?) {
        MaxAdjust.trackAdRevenue(id)
    }

    @JvmStatic
    fun onTrackEvent(eventName: String?) {
        MaxAdjust.onTrackEvent(eventName)
    }

    @JvmStatic
    fun onTrackEvent(eventName: String?, id: String?) {
        MaxAdjust.onTrackEvent(eventName, id)
    }

    @JvmStatic
    fun onTrackRevenue(eventName: String?, revenue: Float, currency: String?) {
        MaxAdjust.onTrackRevenue(eventName, revenue, currency)
    }

    @JvmStatic
    fun onTrackRevenuePurchase(revenue: Float, currency: String?, idPurchase: String, typeIAP: Int) {
        MaxAdjust.onTrackRevenuePurchase(revenue, currency)
        MaxAppsflyer.getInstance().onTrackRevenuePurchase(revenue, currency ?: "", idPurchase, typeIAP)
    }

    @JvmStatic
    fun trackPlayStoreSubscription(
        priceMicros: Long,
        currency: String?,
        sku: String?,
        orderId: String?,
        signature: String?,
        purchaseToken: String?,
        purchaseTimeMillis: Long
    ) {
        MaxAdjust.trackPlayStoreSubscription(
            priceMicros,
            currency,
            sku,
            orderId,
            signature,
            purchaseToken,
            purchaseTimeMillis
        )
    }

    @JvmStatic
    fun pushTrackEventApplovin(ad: MaxAd, context: Context) {
        MaxAdjust.pushTrackEventApplovin(ad, context)
    }
}
