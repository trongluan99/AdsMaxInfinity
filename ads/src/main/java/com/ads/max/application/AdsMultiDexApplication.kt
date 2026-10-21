package com.ads.max.application

import androidx.multidex.MultiDexApplication
import com.ads.max.config.MaxAdConfig
import com.ads.max.util.AppUtil
import com.ads.max.util.SharePreferenceUtils
import java.util.ArrayList

abstract class AdsMultiDexApplication : MultiDexApplication() {

    @JvmField
    protected var maxAdConfig: MaxAdConfig? = null

    @JvmField
    protected var listTestDevice: MutableList<String> = ArrayList()

    override fun onCreate() {
        super.onCreate()
        listTestDevice = ArrayList()
        maxAdConfig = MaxAdConfig(this)
        if (SharePreferenceUtils.getInstallTime(this) == 0L) {
            SharePreferenceUtils.setInstallTime(this)
        }
        AppUtil.currentTotalRevenue001Ad = SharePreferenceUtils.getCurrentTotalRevenue001Ad(this)
    }
}
