package com.ads.max.event;

import android.content.Context;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAdRevenue;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustPlayStoreSubscription;
import com.ads.max.ads.MaxAd;

public class MaxAdjust {


    public static boolean enableAdjust = false;
    private static String eventNamePurchase = "";

    public static void setEventNamePurchase(String eventNamePurchase) {
        MaxAdjust.eventNamePurchase = eventNamePurchase;
    }

    public static void trackAdRevenue(String id) {
        AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue(id);
        Adjust.trackAdRevenue(adjustAdRevenue);
    }

    public static void onTrackEvent(String eventName) {
        AdjustEvent event = new AdjustEvent(eventName);
        Adjust.trackEvent(event);
    }

    public static void onTrackEvent(String eventName, String id) {
        AdjustEvent event = new AdjustEvent(eventName);
        // Assign custom identifier to event which will be reported in success/failure callbacks.
        event.setCallbackId(id);
        Adjust.trackEvent(event);
    }

    public static void onTrackRevenue(String eventName, float revenue, String currency) {
        AdjustEvent event = new AdjustEvent(eventName);
        // Add revenue 1 cent of an euro.
        event.setRevenue(revenue / 1000000.0, currency);
        Adjust.trackEvent(event);
    }

    public static void onTrackRevenuePurchase(float revenue, String currency) {
        if (MaxAdjust.enableAdjust) {
            onTrackRevenue(eventNamePurchase, revenue, currency);
        }
    }

    /**
     * Track a Play Store subscription purchase for server-side verification.
     * Adjust re-validates the token with Google (Developer API) and tracks
     * renewals/cancellations/refunds automatically via RTDN, without further
     * client-side calls on renew.
     *
     * @param priceMicros        subscription price in micros (ProductDetails.PricingPhase#getPriceAmountMicros)
     * @param currency           ISO 4217 currency code
     * @param sku                subscription product ID
     * @param orderId            Purchase#getOrderId()
     * @param signature          Purchase#getSignature()
     * @param purchaseToken      Purchase#getPurchaseToken()
     * @param purchaseTimeMillis Purchase#getPurchaseTime()
     */
    public static void trackPlayStoreSubscription(long priceMicros, String currency, String sku,
                                                  String orderId, String signature, String purchaseToken,
                                                  long purchaseTimeMillis) {
        if (!MaxAdjust.enableAdjust) {
            return;
        }
        AdjustPlayStoreSubscription subscription = new AdjustPlayStoreSubscription(
                priceMicros, currency, sku, orderId, signature, purchaseToken);
        subscription.setPurchaseTime(purchaseTimeMillis);
        Adjust.trackPlayStoreSubscription(subscription);
    }

    public static void pushTrackEventApplovin(com.applovin.mediation.MaxAd ad, Context context) {
        if (MaxAdjust.enableAdjust) {
            AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue("applovin_max_sdk");
            adjustAdRevenue.setRevenue(ad.getRevenue(), "USD");
            adjustAdRevenue.setAdRevenueNetwork(ad.getNetworkName());
            adjustAdRevenue.setAdRevenueUnit(ad.getAdUnitId());
            adjustAdRevenue.setAdRevenuePlacement(ad.getPlacement());

            Adjust.trackAdRevenue(adjustAdRevenue);

        }
    }

    static void logPaidAdImpressionValue(double revenue, String currency) {
        if (MaxAd.getInstance().getAdConfig().getAdjustConfig() != null && MaxAd.getInstance().getAdConfig().getAdjustConfig().isEnableAdjust()) {
            AdjustEvent event = new AdjustEvent(MaxAd.getInstance().getAdConfig().getAdjustConfig().getEventAdImpression());
            event.setRevenue(revenue, currency);
            Adjust.trackEvent(event);
        }
    }

}
