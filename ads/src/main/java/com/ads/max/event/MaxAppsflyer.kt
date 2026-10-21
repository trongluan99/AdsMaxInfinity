package com.ads.max.event

import android.app.Application
import android.content.Context
import android.util.Log
import com.ads.max.billing.AppPurchase
import com.ads.max.funtion.AdType
import com.applovin.mediation.MaxAd
import com.appsflyer.AppsFlyerLib
import com.appsflyer.adrevenue.AppsFlyerAdRevenue
import com.appsflyer.adrevenue.adnetworks.generic.MediationNetwork
import com.appsflyer.adrevenue.adnetworks.generic.Scheme
import com.appsflyer.share.AFInAppEventParameterName
import com.appsflyer.share.AFInAppEventType
import com.appsflyer.share.attribution.AppsFlyerRequestListener
import java.util.Currency
import java.util.HashMap
import java.util.Locale

class MaxAppsflyer {
    private var context: Context? = null

    companion object {
        private const val TAG = "AmxAppsflyer"

        @Volatile
        private var maxAppsflyer: MaxAppsflyer? = null

        @JvmField
        var enableAppsflyer: Boolean = false

        @JvmStatic
        fun getInstance(): MaxAppsflyer {
            if (maxAppsflyer == null) {
                maxAppsflyer = MaxAppsflyer()
            }
            return maxAppsflyer!!
        }
    }

    @JvmOverloads
    fun init(context: Application, devKey: String, enableDebugLog: Boolean = false) {
        this.context = context
        AppsFlyerLib.getInstance().init(devKey, null, context)
        AppsFlyerLib.getInstance().start()

        val afRevenueBuilder = AppsFlyerAdRevenue.Builder(context)
        AppsFlyerAdRevenue.initialize(afRevenueBuilder.build())

        AppsFlyerLib.getInstance().setDebugLog(enableDebugLog)
    }

    fun onTrackEventAddToCard(contentId: String) {
        val ctx = context ?: return
        val eventValues: MutableMap<String, Any> = HashMap()
        eventValues[AFInAppEventParameterName.CONTENT_ID] = contentId
        AppsFlyerLib.getInstance().logEvent(ctx,
            AFInAppEventType.ADD_TO_CART, eventValues, object : AppsFlyerRequestListener {
                override fun onSuccess() {
                    Log.i(TAG, "onTrackEventAddToCard contentId:$contentId success ")
                }

                override fun onError(i: Int, s: String) {
                    Log.i(TAG, "onTrackEventAddToCard contentId:$contentId error: $s")
                }
            })
    }

    fun onTrackRevenuePurchase(price: Float, currency: String, contentId: String, typeIAP: Int) {
        val ctx = context ?: return
        val type = if (typeIAP == AppPurchase.TYPE_IAP.PURCHASE) "inapp" else "subs"
        val eventValues: MutableMap<String, Any> = HashMap()
        eventValues[AFInAppEventParameterName.REVENUE] = price
        eventValues[AFInAppEventParameterName.CONTENT_ID] = contentId
        eventValues[AFInAppEventParameterName.CURRENCY] = currency
        eventValues[AFInAppEventParameterName.CONTENT_TYPE] = type

        AppsFlyerLib.getInstance().logEvent(ctx,
            AFInAppEventType.PURCHASE, eventValues, object : AppsFlyerRequestListener {
                override fun onSuccess() {
                    Log.i(TAG, "onTrackRevenuePurchase contentId:$contentId success ")
                }

                override fun onError(i: Int, s: String) {
                    Log.i(TAG, "onTrackRevenuePurchase contentId:$contentId error: $s")
                }
            })
    }

    fun pushTrackEventApplovin(ad: MaxAd, adType: AdType) {
        Log.i(TAG, "pushTrackEventApplovin  enableAppsflyer:$enableAppsflyer --- value: ${ad.revenue} -- adType: $adType")
        if (enableAppsflyer) {
            val customParams: MutableMap<String, String> = HashMap()
            customParams[Scheme.AD_UNIT] = ad.adUnitId
            customParams[Scheme.AD_TYPE] = adType.toString()

            AppsFlyerAdRevenue.logAdRevenue(
                "Max",
                MediationNetwork.applovinmax,
                Currency.getInstance(Locale.US),
                ad.revenue,
                customParams
            )
        }
    }

    fun updateServerUninstallToken(context: Application, token: String) {
        AppsFlyerLib.getInstance().updateServerUninstallToken(context, token)
    }

    fun onTrackRevenue(context: Context?, eventName: String?, revenue: Float, currency: String?) {}

    fun onTrackRevenuePurchase(revenue: Float, currency: String?) {}
}
