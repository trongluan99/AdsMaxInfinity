package com.ads.max.funtion

import com.ads.max.ads.wrapper.ApAdError

open class AdCallback {
    open fun onNextAction() {}
    open fun onAdClosed() {}
    open fun onAdFailedSplash() {}
    open fun onAdFailedToLoad(error: ApAdError?) {}
    open fun onAdFailedToShow(error: ApAdError?) {}
    open fun onAdLeftApplication() {}
    open fun onAdLoaded() {}
    open fun onAdSplashReady() {}
    open fun onAdClicked() {}
    open fun onAdImpression() {}
    open fun onInterstitialShow() {}
}
