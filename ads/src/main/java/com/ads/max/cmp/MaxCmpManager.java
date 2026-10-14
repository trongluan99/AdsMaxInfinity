package com.ads.max.cmp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

public class MaxCmpManager {
    private static final String TAG = "MaxCmpManager";
    private static MaxCmpManager instance;

    public interface CmpCallback {
        void onCompleted(@Nullable FormError formError);
    }

    public static synchronized MaxCmpManager getInstance() {
        if (instance == null) {
            instance = new MaxCmpManager();
        }
        return instance;
    }

    private MaxCmpManager() {
    }

    /**
     * Khởi tạo và hiển thị CMP (Google UMP) tại màn hình Splash.
     *
     * @param activity
     * @param enableDebugGeographyEEA Nếu true, ép UMP coi thiết bị đang ở EEA (Châu Âu) để luôn hiển thị form khi debug
     * @param testDeviceHashedId      Hashed ID của thiết bị test (lấy từ Logcat: Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("..."))
     * @param resetConsent            Nếu true, reset trạng thái consent cũ để test lại từ đầu
     * @param callback                Callback khi hoàn thành (hoặc lỗi)
     */
    public void requestConsent(Activity activity,
                               boolean enableDebugGeographyEEA,
                               @Nullable String testDeviceHashedId,
                               boolean resetConsent,
                               @NonNull CmpCallback callback) {
        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(activity);

        if (resetConsent && enableDebugGeographyEEA) {
            Log.d(TAG, "resetConsent for debugging");
            consentInformation.reset();
        }

        ConsentRequestParameters.Builder paramsBuilder = new ConsentRequestParameters.Builder();

        if (enableDebugGeographyEEA) {
            ConsentDebugSettings.Builder debugBuilder = new ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA);
            if (testDeviceHashedId != null && !testDeviceHashedId.trim().isEmpty()) {
                debugBuilder.addTestDeviceHashedId(testDeviceHashedId.trim());
            }
            paramsBuilder.setConsentDebugSettings(debugBuilder.build());
        }

        ConsentRequestParameters params = paramsBuilder.build();

        consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                () -> {
                    Log.d(TAG, "Consent info update success. Can request ads: " + consentInformation.canRequestAds());
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            activity,
                            formError -> {
                                if (formError != null) {
                                    Log.e(TAG, "loadAndShowConsentFormIfRequired error: " + formError.getMessage() + " code: " + formError.getErrorCode());
                                } else {
                                    Log.d(TAG, "loadAndShowConsentFormIfRequired completed successfully");
                                }
                                callback.onCompleted(formError);
                            }
                    );
                },
                requestConsentError -> {
                    Log.e(TAG, "requestConsentInfoUpdate failed: " + requestConsentError.getMessage() + " code: " + requestConsentError.getErrorCode());
                    callback.onCompleted(requestConsentError);
                }
        );
    }

    /**
     * Mở form chỉnh sửa Privacy / Consent cho user (thường đặt trong menu Settings của app)
     */
    public void showPrivacyOptionsForm(Activity activity, @NonNull CmpCallback callback) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, formError -> {
            if (formError != null) {
                Log.e(TAG, "showPrivacyOptionsForm error: " + formError.getMessage());
            }
            callback.onCompleted(formError);
        });
    }

    public boolean canRequestAds(Context context) {
        return UserMessagingPlatform.getConsentInformation(context).canRequestAds();
    }
}
