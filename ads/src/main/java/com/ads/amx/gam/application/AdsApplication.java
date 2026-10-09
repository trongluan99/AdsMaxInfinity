package com.ads.amx.gam.application;

import android.app.Application;

import com.ads.amx.gam.config.AmxGamAdConfig;
import com.ads.amx.gam.util.AppUtil;
import com.ads.amx.gam.util.SharePreferenceUtils;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public abstract class AdsApplication extends Application {

    protected AmxGamAdConfig gamAdConfig;
    protected List<String> listTestDevice ;
    @Override
    public void onCreate() {
        super.onCreate();
        listTestDevice = new ArrayList<String>();
        gamAdConfig = new AmxGamAdConfig(this);
        if (SharePreferenceUtils.getInstallTime(this) == 0) {
            SharePreferenceUtils.setInstallTime(this);
        }
        AppUtil.currentTotalRevenue001Ad = SharePreferenceUtils.getCurrentTotalRevenue001Ad(this);
    }

}
