package com.ads.max.ads.wrapper

import android.view.View

class ApNativeAd : ApAdBase {
    var layoutCustomNative: Int = 0
    var nativeView: View? = null

    constructor(status: StatusAd) : super(status)

    constructor(layoutCustomNative: Int, nativeView: View?) : super() {
        this.layoutCustomNative = layoutCustomNative
        this.nativeView = nativeView
        status = StatusAd.AD_LOADED
    }

    constructor() : super()

    override fun isReady(): Boolean {
        return nativeView != null
    }

    override fun toString(): String {
        return "Status:$status == nativeView:$nativeView"
    }
}
