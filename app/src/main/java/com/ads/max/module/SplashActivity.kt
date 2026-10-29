package com.ads.max.module

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ads.max.applovin.AppLovinCallback
import com.ads.max.applovin.AppOpenMax
import com.ads.max.cmp.MaxCmpManager
import com.applovin.sdk.AppLovinSdk

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"
        private const val TEST_DEVICE_HASHED_ID = "0CFFB360968837ECF982C1E38F8D975F"

        // Set false nếu bạn muốn sau khi nhận consent thì vào thẳng Main (bỏ qua Splash Ads)
        private const val SHOW_OPEN_AD_SPLASH = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initDebugViews()
        initCmpFlow()
    }

    /**
     * Bật các tiện ích debug khi chạy ở môi trường DEV
     */
    private fun initDebugViews() {
        val layoutDebug = findViewById<View>(R.id.layoutDebug)
        if (BuildConfig.env_dev) {
            layoutDebug.visibility = View.VISIBLE
            val txtDebugInfo = findViewById<TextView>(R.id.txtDebugInfo)
            txtDebugInfo.text = "DEBUG MODE | AppOpen ID: " + BuildConfig.ads_open_app

            // Nút test ép hiển thị lại form CMP
            findViewById<View>(R.id.btn_test_cmp_splash).setOnClickListener {
                Toast.makeText(this@SplashActivity, "Requesting CMP Form...", Toast.LENGTH_SHORT).show()
                MaxCmpManager.getInstance().requestConsent(
                    this@SplashActivity,
                    true,
                    TEST_DEVICE_HASHED_ID,
                    true
                ) { formError ->
                    if (formError != null) {
                        Toast.makeText(this@SplashActivity, "CMP Error: " + formError.message, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(
                            this@SplashActivity,
                            "CMP Completed! Can request ads: " + MaxCmpManager.getInstance().canRequestAds(this@SplashActivity),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            // Mở nhanh Mediation Debugger tại Splash (khi cần)
            findViewById<View>(R.id.btn_mediation_debugger_splash).setOnClickListener {
                AppLovinSdk.getInstance(this@SplashActivity).showMediationDebugger()
            }

            // Bỏ qua splash để vào thẳng Main
            findViewById<View>(R.id.btn_skip_splash).setOnClickListener {
                startMainActivity()
            }
        } else {
            layoutDebug.visibility = View.GONE
        }
    }

    /**
     * Flow chuẩn: Splash -> Show CMP -> User nhấn Consent -> Vào Main
     */
    private fun initCmpFlow() {
        val isDebugEEA = BuildConfig.env_dev
        // Ở bản DEV, resetConsent = true để mỗi lần mở app đều luôn hiển thị form CMP test
        val resetConsent = BuildConfig.env_dev

        Log.d(TAG, "initCmpFlow: requesting consent...")
        MaxCmpManager.getInstance().requestConsent(
            this@SplashActivity,
            isDebugEEA,
            TEST_DEVICE_HASHED_ID,
            resetConsent
        ) { formError ->
            if (formError != null) {
                Log.e(TAG, "CMP Consent error: " + formError.message + " code: " + formError.errorCode)
            } else {
                Log.d(TAG, "CMP Consent finished. Can request ads: " + MaxCmpManager.getInstance().canRequestAds(this@SplashActivity))
            }

            // Sau khi nhận consent: Khởi tạo MAX SDK để đọc chuỗi TCF v2 mới nhất
            (application as? MyApplication)?.initMaxAds {
                if (SHOW_OPEN_AD_SPLASH && BuildConfig.ads_open_app.isNotEmpty()) {
                    loadSplashAd()
                } else {
                    startMainActivity()
                }
            } ?: run {
                if (SHOW_OPEN_AD_SPLASH && BuildConfig.ads_open_app.isNotEmpty()) {
                    loadSplashAd()
                } else {
                    startMainActivity()
                }
            }
        }
    }

    private fun loadSplashAd() {
        Log.d(TAG, "loadSplashAd: id=" + BuildConfig.ads_open_app)
        AppOpenMax.getInstance().loadOpenMaxSplash(
            this@SplashActivity,
            BuildConfig.ads_open_app,
            0,
            30000,
            true,
            object : AppLovinCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    startMainActivity()
                }
            }
        )
    }

    private fun startMainActivity() {
        if (!isFinishing) {
            (application as? MyApplication)?.initMaxAds()
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
