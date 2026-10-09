package com.ads.amx.gam.application;

import androidx.multidex.MultiDexApplication;

import com.ads.amx.gam.config.AmxGamAdConfig;
import com.ads.amx.gam.util.AppUtil;
import com.ads.amx.gam.util.SharePreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AdsMultiDexApplication extends MultiDexApplication {

    protected AmxGamAdConfig amxGamAdConfig;
    protected List<String> listTestDevice ;
    @Override
    public void onCreate() {
        super.onCreate();
        listTestDevice = new ArrayList<String>();
        amxGamAdConfig = new AmxGamAdConfig(this);
        if (SharePreferenceUtils.getInstallTime(this) == 0) {
            SharePreferenceUtils.setInstallTime(this);
        }
        AppUtil.currentTotalRevenue001Ad = SharePreferenceUtils.getCurrentTotalRevenue001Ad(this);
    }


}
