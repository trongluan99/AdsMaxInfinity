package com.ads.max.application;

import android.app.Application;

import com.ads.max.config.MaxAdConfig;
import com.ads.max.util.AppUtil;
import com.ads.max.util.SharePreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AdsApplication extends Application {

    protected MaxAdConfig maxAdConfig;
    protected List<String> listTestDevice ;
    @Override
    public void onCreate() {
        super.onCreate();
        listTestDevice = new ArrayList<String>();
        maxAdConfig = new MaxAdConfig(this);
        if (SharePreferenceUtils.getInstallTime(this) == 0) {
            SharePreferenceUtils.setInstallTime(this);
        }
        AppUtil.currentTotalRevenue001Ad = SharePreferenceUtils.getCurrentTotalRevenue001Ad(this);
    }

}
