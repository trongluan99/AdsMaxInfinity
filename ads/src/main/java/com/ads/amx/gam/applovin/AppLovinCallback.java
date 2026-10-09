package com.ads.amx.gam.applovin;

import androidx.annotation.Nullable;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.nativeAds.MaxNativeAdView;

public class AppLovinCallback {
    public void initAppLovinSuccess() {
    }

    public void onAdClosed() {
    }

    public void onAdFailedToLoad(@Nullable MaxError i) {
    }

    public void onAdFailedToShow(@Nullable MaxError adError) {
    }

    public void onAdLeftApplication() {
    }

    public void onAdLoaded() {
    }

    public void onInterstitialLoad(MaxInterstitialAd interstitialAd) {
    }

    public void onAdClicked() {
    }
    public void onUserRewarded( MaxReward reward) {
    }

    public void onAdImpression() {
    }

    public void onAdSplashReady() {
    }

    public void onUnifiedNativeAdLoaded(MaxNativeAdView unifiedNativeAd) {
    }

    public void onNativeAdLoaded(MaxNativeAdView unifiedNativeAd, MaxAd nativeAd) {
        onUnifiedNativeAdLoaded(unifiedNativeAd);
    }

    public void onNativeAdExpired(MaxAd nativeAd) {
    }

    public void onNextAction() {
    }

    public void onAdDisplayed() {
    }

    public void onAdLoadFailed() {
    }

    public void onAdDisplayFailed() {
    }
}
