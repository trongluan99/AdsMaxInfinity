package com.ads.max.util

import android.content.Context
import android.content.SharedPreferences

object SharePreferenceUtils {
    private const val PREF_NAME = "gam_ad_pref"
    private const val KEY_INSTALL_TIME = "KEY_INSTALL_TIME"
    private const val KEY_CURRENT_TOTAL_REVENUE_AD = "KEY_CURRENT_TOTAL_REVENUE_AD"
    private const val KEY_CURRENT_TOTAL_REVENUE_001_AD = "KEY_CURRENT_TOTAL_REVENUE_001_AD"
    private const val KEY_PUSH_EVENT_REVENUE_3_DAY = "KEY_PUSH_EVENT_REVENUE_3_DAY"
    private const val KEY_PUSH_EVENT_REVENUE_7_DAY = "KEY_PUSH_EVENT_REVENUE_7_DAY"
    private const val KEY_LAST_IMPRESSION_INTERSTITIAL_TIME = "KEY_LAST_IMPRESSION_INTERSTITIAL_TIME"

    @JvmStatic
    fun getInstallTime(context: Context): Long {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getLong(KEY_INSTALL_TIME, 0)
    }

    @JvmStatic
    fun setInstallTime(context: Context) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pre.edit().putLong(KEY_INSTALL_TIME, System.currentTimeMillis()).apply()
    }

    @JvmStatic
    fun getCurrentTotalRevenueAd(context: Context): Float {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getFloat(KEY_CURRENT_TOTAL_REVENUE_AD, 0f)
    }

    @JvmStatic
    fun updateCurrentTotalRevenueAd(context: Context, revenue: Float) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var currentTotalRevenue = pre.getFloat(KEY_CURRENT_TOTAL_REVENUE_AD, 0f)
        currentTotalRevenue += (revenue / 1000000.0).toFloat()
        pre.edit().putFloat(KEY_CURRENT_TOTAL_REVENUE_AD, currentTotalRevenue).apply()
    }

    @JvmStatic
    fun getCurrentTotalRevenue001Ad(context: Context): Float {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getFloat(KEY_CURRENT_TOTAL_REVENUE_001_AD, 0f)
    }

    @JvmStatic
    fun updateCurrentTotalRevenue001Ad(context: Context, revenue: Float) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pre.edit().putFloat(KEY_CURRENT_TOTAL_REVENUE_001_AD, revenue).apply()
    }

    @JvmStatic
    fun isPushRevenue3Day(context: Context): Boolean {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getBoolean(KEY_PUSH_EVENT_REVENUE_3_DAY, false)
    }

    @JvmStatic
    fun setPushedRevenue3Day(context: Context) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pre.edit().putBoolean(KEY_PUSH_EVENT_REVENUE_3_DAY, true).apply()
    }

    @JvmStatic
    fun isPushRevenue7Day(context: Context): Boolean {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getBoolean(KEY_PUSH_EVENT_REVENUE_7_DAY, false)
    }

    @JvmStatic
    fun setPushedRevenue7Day(context: Context) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pre.edit().putBoolean(KEY_PUSH_EVENT_REVENUE_7_DAY, true).apply()
    }

    @JvmStatic
    fun getLastImpressionInterstitialTime(context: Context): Long {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getLong(KEY_LAST_IMPRESSION_INTERSTITIAL_TIME, 0)
    }

    @JvmStatic
    fun setLastImpressionInterstitialTime(context: Context) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pre.edit().putLong(KEY_LAST_IMPRESSION_INTERSTITIAL_TIME, System.currentTimeMillis()).apply()
    }
}
