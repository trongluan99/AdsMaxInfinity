package com.ads.max.ads.wrapper

import com.applovin.mediation.ads.MaxRewardedAd

class ApRewardAd : ApAdBase {
    var maxReward: MaxRewardedAd? = null
        set(value) {
            field = value
            status = StatusAd.AD_LOADED
        }

    constructor() : super()

    constructor(status: StatusAd) : super(status)

    constructor(maxReward: MaxRewardedAd?) : super() {
        this.maxReward = maxReward
        status = StatusAd.AD_LOADED
    }

    /**
     * Clean reward when shown
     */
    fun clean() {
        maxReward = null
    }

    override fun isReady(): Boolean {
        return maxReward?.isReady == true
    }
}
