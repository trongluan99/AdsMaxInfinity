package com.ads.max.config

class AppsflyerConfig {
    var isEnableAppsflyer: Boolean = false
    var appsflyerToken: String = ""
    var eventAdImpression: String = ""

    constructor(enableAppsflyer: Boolean) {
        this.isEnableAppsflyer = enableAppsflyer
    }

    constructor(enableAppsflyer: Boolean, appsflyerToken: String) {
        this.isEnableAppsflyer = enableAppsflyer
        this.appsflyerToken = appsflyerToken
    }
}
