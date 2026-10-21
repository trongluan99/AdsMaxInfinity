package com.ads.max.event

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.AdjustPlayStoreSubscription
import com.ads.max.ads.MaxAd

object MaxAdjust {
    @JvmField
    var enableAdjust: Boolean = false
    private var eventNamePurchase: String = ""

    @JvmStatic
    fun setEventNamePurchase(eventNamePurchase: String) {
        MaxAdjust.eventNamePurchase = eventNamePurchase
    }

    @JvmStatic
    fun trackAdRevenue(id: String?) {
        val adjustAdRevenue = AdjustAdRevenue(id)
        Adjust.trackAdRevenue(adjustAdRevenue)
    }

    @JvmStatic
    fun onTrackEvent(eventName: String?) {
        val event = AdjustEvent(eventName)
        Adjust.trackEvent(event)
    }

    @JvmStatic
    fun onTrackEvent(eventName: String?, id: String?) {
        val event = AdjustEvent(eventName)
        event.setCallbackId(id)
        Adjust.trackEvent(event)
    }

    @JvmStatic
    fun onTrackRevenue(eventName: String?, revenue: Float, currency: String?) {
        val event = AdjustEvent(eventName)
        event.setRevenue(revenue / 1000000.0, currency)
        Adjust.trackEvent(event)
    }

    @JvmStatic
    fun onTrackRevenuePurchase(revenue: Float, currency: String?) {
        if (enableAdjust) {
            onTrackRevenue(eventNamePurchase, revenue, currency)
        }
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
        if (!enableAdjust) {
            return
        }
        val subscription = AdjustPlayStoreSubscription(
            priceMicros, currency, sku, orderId, signature, purchaseToken
        )
        subscription.setPurchaseTime(purchaseTimeMillis)
        Adjust.trackPlayStoreSubscription(subscription)
    }

    @JvmStatic
    fun pushTrackEventApplovin(ad: com.applovin.mediation.MaxAd, context: Context?) {
        if (enableAdjust) {
            val adjustAdRevenue = AdjustAdRevenue("applovin_max_sdk")
            adjustAdRevenue.setRevenue(ad.revenue, "USD")
            adjustAdRevenue.setAdRevenueNetwork(ad.networkName)
            adjustAdRevenue.setAdRevenueUnit(ad.adUnitId)
            adjustAdRevenue.setAdRevenuePlacement(ad.placement)

            Adjust.trackAdRevenue(adjustAdRevenue)
        }
    }

    @JvmStatic
    fun logPaidAdImpressionValue(revenue: Double, currency: String?) {
        val config = MaxAd.getInstance().adConfig?.adjustConfig
        if (config != null && config.isEnableAdjust) {
            val event = AdjustEvent(config.eventAdImpression)
            event.setRevenue(revenue, currency)
            Adjust.trackEvent(event)
        }
    }
}
