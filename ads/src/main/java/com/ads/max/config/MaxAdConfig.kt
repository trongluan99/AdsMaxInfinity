package com.ads.max.config

import android.app.Application
import java.util.ArrayList

class MaxAdConfig {

    companion object {
        const val PROVIDER_ADMOB = 0
        const val PROVIDER_MAX = 1

        const val ENVIRONMENT_DEVELOP = "develop"
        const val ENVIRONMENT_PRODUCTION = "production"

        const val DEFAULT_TOKEN_FACEBOOK_SDK = "client_token"
        @JvmField
        var ADJUST_TOKEN_TIKTOK = "client_token_adjust_tiktok"
    }

    private var _isVariantDev: Boolean = false

    var adjustConfig: AdjustConfig? = null
    var appsflyerConfig: AppsflyerConfig? = null
    var eventNamePurchase: String = ""
    var idAdResume: String? = null
        set(value) {
            field = value
            isEnableAdResume = true
        }
    var listDeviceTest: List<String> = ArrayList()
    var application: Application? = null
        private set
    var isEnableAdResume: Boolean = false
        private set
    var facebookClientToken: String = DEFAULT_TOKEN_FACEBOOK_SDK
    var adjustTokenTiktok: String? = null
        set(value) {
            field = value
            if (value != null) {
                ADJUST_TOKEN_TIKTOK = value
            }
        }
    var applovinSdkKey: String? = null
    var intervalInterstitialAd: Int = 0

    constructor(application: Application?) {
        this.application = application
    }

    constructor(application: Application?, environment: String) {
        this.application = application
        this._isVariantDev = environment == ENVIRONMENT_DEVELOP
    }

    @Deprecated("Use setEnvironment instead")
    fun setVariant(isVariantDev: Boolean?) {
        this._isVariantDev = isVariantDev ?: false
    }

    fun setEnvironment(environment: String) {
        this._isVariantDev = environment == ENVIRONMENT_DEVELOP
    }

    fun isVariantDev(): Boolean {
        return _isVariantDev
    }

    fun isEnableAdjust(): Boolean {
        return adjustConfig?.isEnableAdjust ?: false
    }

    fun isEnableAppsflyer(): Boolean {
        return appsflyerConfig?.isEnableAppsflyer ?: false
    }
}
