package com.ads.max.ads

import com.ads.max.ads.wrapper.ApAdError
import com.ads.max.ads.wrapper.ApInterstitialAd
import com.ads.max.ads.wrapper.ApNativeAd
import com.ads.max.ads.wrapper.ApRewardItem

open class MaxAdCallback {
    open fun onNextAction() {}
    open fun onAdClosed() {}
    open fun onAdFailedToLoad(adError: ApAdError?) {}
    open fun onAdFailedToShow(adError: ApAdError?) {}
    open fun onAdLeftApplication() {}
    open fun onAdLoaded() {}
    open fun onAdSplashReady() {}
    open fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {}
    open fun onAdClicked() {}
    open fun onAdImpression() {}
    open fun onNativeAdLoaded(nativeAd: ApNativeAd) {}
    open fun onUserEarnedReward(rewardItem: ApRewardItem) {}
    open fun onInterstitialShow() {}
}
