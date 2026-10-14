package com.ads.max.application;

import androidx.multidex.MultiDexApplication;

import com.ads.max.config.MaxAdConfig;
import com.ads.max.util.AppUtil;
import com.ads.max.util.SharePreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AdsMultiDexApplication extends MultiDexApplication {

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
