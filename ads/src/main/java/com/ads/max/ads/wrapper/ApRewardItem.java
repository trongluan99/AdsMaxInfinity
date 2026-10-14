package com.ads.max.ads.wrapper;

import com.applovin.mediation.MaxReward;

public class ApRewardItem {

    private MaxReward maxRewardItem;

    public ApRewardItem(MaxReward maxRewardItem) {
        this.maxRewardItem = maxRewardItem;
    }

    public MaxReward getMaxRewardItem() {
        return maxRewardItem;
    }

    public void setMaxRewardItem(MaxReward maxRewardItem) {
        this.maxRewardItem = maxRewardItem;
    }
}
