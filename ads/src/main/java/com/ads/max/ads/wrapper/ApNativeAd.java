package com.ads.max.ads.wrapper;

import android.view.View;

public class ApNativeAd extends ApAdBase {
    private int layoutCustomNative;
    private View nativeView;

    public ApNativeAd(StatusAd status) {
        super(status);
    }

    public ApNativeAd(int layoutCustomNative, View nativeView) {
        this.layoutCustomNative = layoutCustomNative;
        this.nativeView = nativeView;
        status = StatusAd.AD_LOADED;
    }

    public ApNativeAd() {
    }

    @Override
    boolean isReady() {
        return nativeView != null;
    }

    public int getLayoutCustomNative() {
        return layoutCustomNative;
    }

    public void setLayoutCustomNative(int layoutCustomNative) {
        this.layoutCustomNative = layoutCustomNative;
    }

    public View getNativeView() {
        return nativeView;
    }

    public void setNativeView(View nativeView) {
        this.nativeView = nativeView;
    }

    @Override
    public String toString() {
        return "Status:" + status + " == nativeView:" + nativeView;
    }
}
