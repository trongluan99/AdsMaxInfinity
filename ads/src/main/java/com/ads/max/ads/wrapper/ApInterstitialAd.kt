package com.ads.max.ads.wrapper

import com.applovin.mediation.ads.MaxInterstitialAd

class ApInterstitialAd : ApAdBase {
    var maxInterstitialAd: MaxInterstitialAd? = null
        set(value) {
            field = value
            status = StatusAd.AD_LOADED
        }

    constructor(status: StatusAd) : super(status)

    constructor() : super()

    constructor(maxInterstitialAd: MaxInterstitialAd?) : super() {
        this.maxInterstitialAd = maxInterstitialAd
        status = StatusAd.AD_LOADED
    }

    override fun isReady(): Boolean {
        return maxInterstitialAd != null && maxInterstitialAd!!.isReady
    }
}
