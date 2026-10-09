package com.ads.amx.gam.ads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.amx.gam.ads.wrapper.ApAdError;
import com.ads.amx.gam.ads.wrapper.ApInterstitialAd;
import com.ads.amx.gam.ads.wrapper.ApNativeAd;
import com.ads.amx.gam.ads.wrapper.ApRewardItem;

public class AmxGamAdCallback {
    public void onNextAction() {
    }

    public void onAdClosed() {
    }

    public void onAdFailedToLoad(@Nullable ApAdError adError) {
    }

    public void onAdFailedToShow(@Nullable ApAdError adError) {
    }

    public void onAdLeftApplication() {
    }

    public void onAdLoaded() {

    }

    // ad splash loaded when showSplashIfReady = false
    public void onAdSplashReady() {

    }

    public void onInterstitialLoad(@Nullable ApInterstitialAd interstitialAd) {

    }

    public void onAdClicked() {
    }

    public void onAdImpression() {
    }


    public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {

    }

    public void onUserEarnedReward(@NonNull ApRewardItem rewardItem) {

    }

    public void onInterstitialShow() {

    }
}
