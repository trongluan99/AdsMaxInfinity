package com.ads.max.ads.wrapper;

import com.applovin.mediation.ads.MaxInterstitialAd;

public class ApInterstitialAd extends ApAdBase {
    private MaxInterstitialAd maxInterstitialAd;

    public ApInterstitialAd(StatusAd status) {
        super(status);
    }

    public ApInterstitialAd() {
    }

    public ApInterstitialAd(MaxInterstitialAd maxInterstitialAd) {
        this.maxInterstitialAd = maxInterstitialAd;
        status = StatusAd.AD_LOADED;
    }

    public void setMaxInterstitialAd(MaxInterstitialAd maxInterstitialAd) {
        this.maxInterstitialAd = maxInterstitialAd;
        status = StatusAd.AD_LOADED;
    }

    @Override
    public boolean isReady() {
        return maxInterstitialAd != null && maxInterstitialAd.isReady();
    }

    public MaxInterstitialAd getMaxInterstitialAd() {
        return maxInterstitialAd;
    }
}
