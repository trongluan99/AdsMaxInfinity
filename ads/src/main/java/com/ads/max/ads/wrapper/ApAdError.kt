package com.ads.max.ads.wrapper

import com.applovin.mediation.MaxError

class ApAdError {
    var maxError: MaxError? = null
        private set
    private var _message: String = ""

    constructor(maxError: MaxError?) {
        this.maxError = maxError
    }

    constructor(message: String?) {
        this._message = message ?: ""
    }

    var message: String
        get() {
            if (maxError != null) return maxError!!.message
            if (_message.isNotEmpty()) return _message
            return "unknown error"
        }
        set(value) {
            this._message = value
        }
}
