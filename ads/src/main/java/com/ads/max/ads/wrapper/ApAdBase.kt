package com.ads.max.ads.wrapper

abstract class ApAdBase {
    var status: StatusAd = StatusAd.AD_INIT

    constructor(status: StatusAd) {
        this.status = status
    }

    constructor()

    abstract fun isReady(): Boolean

    fun isNotReady(): Boolean {
        return !isReady()
    }

    fun isLoading(): Boolean {
        return status == StatusAd.AD_LOADING
    }

    fun isLoadFail(): Boolean {
        return status == StatusAd.AD_LOAD_FAIL
    }
}
