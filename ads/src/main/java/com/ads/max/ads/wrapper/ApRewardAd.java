package com.ads.max.ads.wrapper;

import com.applovin.mediation.ads.MaxRewardedAd;

public class ApRewardAd extends ApAdBase {
    private MaxRewardedAd maxReward;

    public ApRewardAd() {
    }

    public ApRewardAd(StatusAd status) {
        super(status);
    }

    public void setMaxReward(MaxRewardedAd maxReward) {
        this.maxReward = maxReward;
        status = StatusAd.AD_LOADED;
    }

    public ApRewardAd(MaxRewardedAd maxReward) {
        this.maxReward = maxReward;
        status = StatusAd.AD_LOADED;
    }

    public MaxRewardedAd getMaxReward() {
        return maxReward;
    }

    /**
     * Clean reward when shown
     */
    public void clean() {
        maxReward = null;
    }

    @Override
    public boolean isReady() {
        return maxReward != null && maxReward.isReady();
    }
}
