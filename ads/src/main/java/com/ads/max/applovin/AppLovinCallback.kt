package com.ads.max.applovin

import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.nativeAds.MaxNativeAdView

open class AppLovinCallback {
    open fun initAppLovinSuccess() {}

    open fun onAdClosed() {}

    open fun onAdFailedToLoad(error: MaxError?) {}

    open fun onAdFailedToShow(adError: MaxError?) {}

    open fun onAdLeftApplication() {}

    open fun onAdLoaded() {}

    open fun onInterstitialLoad(interstitialAd: MaxInterstitialAd?) {}

    open fun onAdClicked() {}

    open fun onUserRewarded(reward: MaxReward?) {}

    open fun onAdImpression() {}

    open fun onAdSplashReady() {}

    open fun onUnifiedNativeAdLoaded(unifiedNativeAd: MaxNativeAdView?) {}

    open fun onNativeAdLoaded(unifiedNativeAd: MaxNativeAdView?, nativeAd: MaxAd?) {
        onUnifiedNativeAdLoaded(unifiedNativeAd)
    }

    open fun onNativeAdExpired(nativeAd: MaxAd?) {}

    open fun onNextAction() {}

    open fun onAdDisplayed() {}

    open fun onAdLoadFailed() {}

    open fun onAdDisplayFailed() {}
}
