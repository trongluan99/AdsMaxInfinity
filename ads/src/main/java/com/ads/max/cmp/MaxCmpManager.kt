package com.ads.max.cmp

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

class MaxCmpManager private constructor() {

    fun interface CmpCallback {
        fun onCompleted(formError: FormError?)
    }

    companion object {
        private const val TAG = "MaxCmpManager"

        @Volatile
        private var instance: MaxCmpManager? = null

        @JvmStatic
        @Synchronized
        fun getInstance(): MaxCmpManager {
            if (instance == null) {
                instance = MaxCmpManager()
            }
            return instance!!
        }
    }

    fun requestConsent(
        activity: Activity,
        enableDebugGeographyEEA: Boolean,
        testDeviceHashedId: String?,
        resetConsent: Boolean,
        callback: CmpCallback
    ) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        if (resetConsent && enableDebugGeographyEEA) {
            Log.d(TAG, "resetConsent for debugging")
            consentInformation.reset()
        }

        val paramsBuilder = ConsentRequestParameters.Builder()

        if (enableDebugGeographyEEA) {
            val debugBuilder = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            if (!testDeviceHashedId.isNullOrBlank()) {
                debugBuilder.addTestDeviceHashedId(testDeviceHashedId.trim())
            }
            paramsBuilder.setConsentDebugSettings(debugBuilder.build())
        }

        val params = paramsBuilder.build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                Log.d(TAG, "Consent info update success. Can request ads: " + consentInformation.canRequestAds())
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    activity
                ) { formError ->
                    if (formError != null) {
                        Log.e(TAG, "loadAndShowConsentFormIfRequired error: " + formError.message + " code: " + formError.errorCode)
                    } else {
                        Log.d(TAG, "loadAndShowConsentFormIfRequired completed successfully")
                    }
                    callback.onCompleted(formError)
                }
            },
            { requestConsentError ->
                Log.e(TAG, "requestConsentInfoUpdate failed: " + requestConsentError.message + " code: " + requestConsentError.errorCode)
                callback.onCompleted(requestConsentError)
            }
        )
    }

    fun showPrivacyOptionsForm(activity: Activity, callback: CmpCallback) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.e(TAG, "showPrivacyOptionsForm error: " + formError.message)
            }
            callback.onCompleted(formError)
        }
    }

    fun canRequestAds(context: Context): Boolean {
        return UserMessagingPlatform.getConsentInformation(context).canRequestAds()
    }
}
