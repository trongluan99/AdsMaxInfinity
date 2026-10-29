package com.ads.max.module

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ads.max.applovin.AppLovin
import com.ads.max.applovin.AppLovinCallback
import com.ads.max.billing.AppPurchase
import com.ads.max.funtion.AdCallback
import com.ads.max.funtion.PurchaseListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.sdk.AppLovinSdk

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var maxInterstitialAd: MaxInterstitialAd? = null
    private var maxRewardedAd: MaxRewardedAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadBannerAd()
        loadNativeAd()
        preloadInterstitialAd()
        preloadRewardedAd()
    }

    private fun initViews() {
        findViewById<View>(R.id.btn_show_inter).setOnClickListener { showInterstitialAd() }
        findViewById<View>(R.id.btn_show_reward).setOnClickListener { showRewardedAd() }
        findViewById<View>(R.id.btn_mediation_debugger).setOnClickListener {
            AppLovinSdk.getInstance(this@MainActivity).showMediationDebugger()
        }
        findViewById<View>(R.id.btn_open_viewpager_demo).setOnClickListener {
            startActivity(Intent(this@MainActivity, SecondActivity::class.java))
        }
        findViewById<View>(R.id.btn_test_iap).setOnClickListener {
            AppPurchase.getInstance().setPurchaseListener(object : PurchaseListener {
                override fun onProductPurchased(productId: String?, transactionDetails: String?) {
                    Toast.makeText(this@MainActivity, "Purchase success: $productId", Toast.LENGTH_SHORT).show()
                }

                override fun displayErrorMessage(errorMsg: String?) {
                    Toast.makeText(this@MainActivity, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                }

                override fun onUserCancelBilling() {
                    Toast.makeText(this@MainActivity, "User cancelled billing", Toast.LENGTH_SHORT).show()
                }
            })
            AppPurchase.getInstance().subscribe(this@MainActivity, "android.test.purchased")
        }
        findViewById<View>(R.id.btn_cmp_privacy).setOnClickListener {
            AppLovin.getInstance().showCmpForExistingUser(this@MainActivity) { error ->
                if (error == null) {
                    Toast.makeText(this@MainActivity, "Privacy settings updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "CMP: " + error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 1. Load Banner Ad (ở dưới đáy màn hình)
     */
    private fun loadBannerAd() {
        AppLovin.getInstance().loadBanner(this, BuildConfig.ad_banner, object : AdCallback() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner onAdLoaded")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Banner onAdClicked")
            }
        })
    }

    /**
     * 2. Load Native Ad (ở giữa màn hình)
     */
    private fun loadNativeAd() {
        AppLovin.getInstance().loadNative(this, BuildConfig.ad_native)
    }

    /**
     * 3. Preload Interstitial Ad
     */
    private fun preloadInterstitialAd() {
        maxInterstitialAd = AppLovin.getInstance().getInterstitialAds(this, BuildConfig.ad_interstitial_splash)
    }

    private fun showInterstitialAd() {
        val inter = maxInterstitialAd
        if (inter != null && inter.isReady) {
            AppLovin.getInstance().forceShowInterstitial(this, inter, object : AdCallback() {
                override fun onAdClosed() {
                    Log.d(TAG, "Interstitial onAdClosed")
                    Toast.makeText(this@MainActivity, "Interstitial Closed", Toast.LENGTH_SHORT).show()
                    preloadInterstitialAd()
                }
            }, true)
        } else {
            Toast.makeText(this, "Interstitial not ready yet, reloading...", Toast.LENGTH_SHORT).show()
            preloadInterstitialAd()
        }
    }

    /**
     * 4. Preload & Show Rewarded Ad
     */
    private fun preloadRewardedAd() {
        maxRewardedAd = AppLovin.getInstance().getRewardAd(this, BuildConfig.ad_reward, object : AppLovinCallback() {
            override fun onAdLoaded() {
                Log.d(TAG, "Reward onAdLoaded")
            }

            override fun onAdFailedToLoad(error: MaxError?) {
                Log.e(TAG, "Reward onAdFailedToLoad: " + (error?.message ?: ""))
            }
        })
    }

    private fun showRewardedAd() {
        val reward = maxRewardedAd
        if (reward != null && reward.isReady) {
            AppLovin.getInstance().showRewardAd(this, reward, object : AppLovinCallback() {
                override fun onUserRewarded(reward: MaxReward?) {
                    Toast.makeText(this@MainActivity, "User rewarded: " + (reward?.amount ?: 0) + " " + (reward?.label ?: ""), Toast.LENGTH_LONG).show()
                }

                override fun onAdClosed() {
                    Log.d(TAG, "Reward onAdClosed")
                    preloadRewardedAd()
                }
            })
        } else {
            Toast.makeText(this, "Rewarded ad not ready yet, reloading...", Toast.LENGTH_SHORT).show()
            preloadRewardedAd()
        }
    }
}
