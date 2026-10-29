# AdsMaxInfinity 🚀

[![JitPack](https://jitpack.io/v/trongluan99/AdsMaxInfinity.svg)](https://jitpack.io/#trongluan99/AdsMaxInfinity)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)](https://developer.android.com/about/dashboards)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blueviolet.svg)](https://developer.android.com/about/dashboards)
[![AppLovin SDK](https://img.shields.io/badge/AppLovin%20MAX-13.6.4-red.svg)](https://dash.applovin.com/)

**AdsMaxInfinity** là thư viện Android (Kotlin) hỗ trợ tích hợp quảng cáo **AppLovin MAX Mediation** nhanh chóng, ổn định và tối ưu doanh thu cho các ứng dụng Android. Thư viện tích hợp sẵn **Google UMP CMP (GDPR / TCF v2.2)**, hệ thống **In-App Purchase (IAP)** & **Subscription** qua Google Play Billing, cùng với giải pháp theo dõi doanh thu & tracking đa kênh (**Firebase, Adjust, AppsFlyer, Facebook App Events**).

---

## 📑 Mục lục
- [Tính năng chính](#-tính-năng-chính)
- [Cài đặt thư viện](#-cài-đặt-thư-viện)
- [Cấu hình AndroidManifest.xml](#-cấu-hình-androidmanifestxml)
- [Khởi tạo trong Application](#-khởi-tạo-trong-application)
- [Hướng dẫn sử dụng chi tiết](#-hướng-dẫn-sử-dụng-chi-tiết)
  - [1. Flow khởi động: CMP Consent & Splash Ad](#1-flow-khởi-động-cmp-consent--splash-ad)
  - [2. App Open Ad Resume](#2-app-open-ad-resume)
  - [3. Interstitial Ad](#3-interstitial-ad)
  - [4. Rewarded Ad](#4-rewarded-ad)
  - [5. Banner Ad (kèm Shimmer Loading)](#5-banner-ad-kèm-shimmer-loading)
  - [6. Native Ad (Medium / Small / Custom / RecyclerView)](#6-native-ad-medium--small--custom--recyclerview)
  - [7. In-App Purchase & Subscription (Google Play Billing)](#7-in-app-purchase--subscription-google-play-billing)
  - [8. Mediation Debugger & Privacy Settings](#8-mediation-debugger--privacy-settings)
- [🛠️ Hướng dẫn Bật Test Ads trên AppLovin MAX & Khắc phục No-Fill (204)](#-hướng-dẫn-bật-test-ads-trên-applovin-max--khắc-phục-no-fill-204)
- [Cấu hình ProGuard](#-cấu-hình-proguard)
- [Hỗ trợ & Đóng góp](#-hỗ-trợ--đóng-góp)

---

## ✨ Tính năng chính

- 🎯 **AppLovin MAX Mediation Đầy đủ**: Hỗ trợ toàn diện App Open Ads (Splash & Resume), Interstitial, Rewarded, Banner, MREC và Native Ads.
- 🛡️ **Tuân thủ chuẩn GDPR / Google UMP CMP (TCF v2.2)**: `MaxCmpManager` giúp xin quyền consent người dùng tại Châu Âu (EEA) và hỗ trợ mở lại màn hình Privacy Options.
- ⏳ **Quản lý Shimmer & Loading Dialog mượt mà**: Tự động hiển thị placeholder Shimmer khi load Native/Banner và hiển thị Dialog chuẩn bị khi show Interstitial/App Open.
- 💳 **Tích hợp Google Play Billing (IAP & Subscriptions)**: `AppPurchase` đơn giản hoá việc query sản phẩm, kiểm tra trạng thái mua hàng, khôi phục giao dịch và thanh toán.
- 📊 **Tracking Doanh thu & Attribution tự động**:
  - Tự động đẩy sự kiện Impression và Revenue theo chuẩn USD sang **Firebase Analytics**.
  - Tích hợp **Adjust SDK** (tự động track attribution, event purchase & ad revenue).
  - Tích hợp **AppsFlyer SDK** & AdRevenue.
  - Tích hợp **Facebook SDK App Events**.
  - Tính toán và bắn event mốc doanh thu (`paid_ad_impression_value_0.01`, doanh thu sau 3 ngày, 7 ngày).
- 🛠️ **Hỗ trợ Mediation Debugger**: Kiểm tra trạng thái tích hợp các Ad Network Adapter (AdMob, Meta Audience Network, Mintegral, Vungle, Moloco, Bytedance Pangle, Unity Ads).

---

## 📦 Cài đặt thư viện

### 1. Thêm kho Maven vào dự án

Trong file `settings.gradle` (hoặc `build.gradle` root):

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url "https://jitpack.io" }
        maven { url 'https://artifact.bytedance.com/repository/pangle' }
        maven { url 'https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea' }
    }
}
```

### 2. Thêm Dependency vào module `app/build.gradle`

```groovy
dependencies {
    implementation 'com.github.trongluan99:AdsMaxInfinity:1.4'
}
```

> **Lưu ý:** Dự án yêu cầu `compileSdk 34+` (khuyến nghị 36) và Java 11 / Java 17.

---

## ⚙️ Cấu hình AndroidManifest.xml

Thêm các quyền cần thiết và meta-data trong `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="com.google.android.gms.permission.AD_ID" />

    <application
        android:name=".MyApplication"
        ...>

        <!-- Google AdMob App ID (bắt buộc nếu bạn dùng AdMob Adapter trong AppLovin MAX) -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713" />

        <!-- Tối ưu hóa tải và khởi tạo Google Ads -->
        <meta-data
            android:name="com.google.android.gms.ads.flag.OPTIMIZE_INITIALIZATION"
            android:value="true" />
        <meta-data
            android:name="com.google.android.gms.ads.flag.OPTIMIZE_AD_LOADING"
            android:value="true" />

        <!-- Facebook App ID (nếu dùng Facebook SDK / Audience Network) -->
        <meta-data
            android:name="com.facebook.sdk.ApplicationId"
            android:value="@string/facebook_app_id" />
        <meta-data
            android:name="com.facebook.sdk.ClientToken"
            android:value="@string/facebook_client_token" />

    </application>
</manifest>
```

---

## 🚀 Khởi tạo trong Application

Kế thừa `AdsApplication` hoặc `AdsMultiDexApplication` trong lớp `Application` của bạn:

```kotlin
package com.ads.max.module

import com.ads.max.ads.MaxAd
import com.ads.max.application.AdsMultiDexApplication
import com.ads.max.applovin.AppLovin
import com.ads.max.applovin.AppOpenMax
import com.ads.max.billing.AppPurchase
import com.ads.max.config.AdjustConfig
import com.ads.max.config.MaxAdConfig

class MyApplication : AdsMultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        
        initBilling()
        initAds()
    }

    private fun initAds() {
        // Thiết lập môi trường Develop hoặc Production
        val environment = if (BuildConfig.DEBUG) MaxAdConfig.ENVIRONMENT_DEVELOP else MaxAdConfig.ENVIRONMENT_PRODUCTION
        maxAdConfig = MaxAdConfig(this, environment)

        // Cấu hình AppLovin SDK Key
        maxAdConfig?.applovinSdkKey = "YOUR_APPLOVIN_SDK_KEY"

        // Cấu hình App Open Resume ID (quảng cáo khi user quay lại app)
        maxAdConfig?.idAdResume = BuildConfig.ad_appopen_resume

        // Cấu hình Adjust (nếu dùng)
        val adjustConfig = AdjustConfig(isEnable = true, adjustToken = "YOUR_ADJUST_TOKEN")
        adjustConfig.eventAdImpression = "YOUR_AD_IMPRESSION_EVENT_TOKEN"
        adjustConfig.eventNamePurchase = "YOUR_PURCHASE_EVENT_TOKEN"
        maxAdConfig?.adjustConfig = adjustConfig

        // Cấu hình danh sách thiết bị test
        listTestDevice.add("TEST_DEVICE_UUID_OR_ID")
        maxAdConfig?.listDeviceTest = listTestDevice

        // Cấu hình hành vi App Open Resume
        AppLovin.getInstance().setDisableAdResumeWhenClickAds(true)
        AppOpenMax.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
    }

    /**
     * Khởi tạo MAX SDK sau khi CMP hoàn tất ở SplashActivity
     */
    fun initMaxAds(onInitSuccess: (() -> Unit)? = null) {
        maxAdConfig?.let { config ->
            MaxAd.getInstance().init(this, config, BuildConfig.DEBUG) {
                onInitSuccess?.invoke()
            }
        } ?: run {
            onInitSuccess?.invoke()
        }
    }

    private fun initBilling() {
        val listINAPId = mutableListOf("android.test.purchased", "vip_lifetime")
        val listSubsId = mutableListOf("vip_monthly", "vip_yearly")
        AppPurchase.getInstance().initBilling(this, listINAPId, listSubsId)
    }
}
```

---

## 📖 Hướng dẫn sử dụng chi tiết

### 1. Flow khởi động: CMP Consent & Splash Ad

Mô hình chuẩn của AppLovin MAX: **SplashActivity** -> **Xin quyền Consent (CMP)** -> **Khởi tạo MAX SDK** -> **Load & Show Splash Ad (hoặc timeout)** -> **Vào MainActivity**.

> **💡 Tại sao khởi tạo sau CMP?**
> AppLovin MAX và các mạng Mediation (AdMob, Meta...) cần đọc chuỗi TCF v2 từ CMP trong `SharedPreferences` lúc khởi tạo. Việc khởi tạo sau khi CMP kết thúc giúp tránh hiện tượng "No-fill ads" và đảm bảo 100% tuân thủ chính sách GDPR tại châu Âu và Google Play.

```kotlin
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initCmpFlow()
    }

    private fun initCmpFlow() {
        val isDebugEEA = BuildConfig.DEBUG
        val resetConsent = BuildConfig.DEBUG // True để test form consent mỗi lần mở trên máy Dev

        // Lấy mã test device hashed id từ Logcat (lọc từ khóa: ConsentDebugSettings)
        val testDeviceHashedId = "YOUR_TEST_DEVICE_HASHED_ID"

        MaxCmpManager.getInstance().requestConsent(
            activity = this,
            enableDebugGeographyEEA = isDebugEEA,
            testDeviceHashedId = testDeviceHashedId,
            resetConsent = resetConsent
        ) { formError ->
            if (formError != null) {
                Log.e("Splash", "CMP Error: ${formError.message}")
            }

            // Sau khi user đồng ý hoặc hoàn thành CMP (hoặc user ở VN ngoài vùng GDPR):
            // Khởi tạo MAX SDK -> Callback này chỉ chạy khi SDK đã khởi tạo xong 100%
            (application as? MyApplication)?.initMaxAds {
                if (SHOW_OPEN_AD_SPLASH && BuildConfig.ads_open_app.isNotEmpty()) {
                    loadSplashAd()
                } else {
                    startMainActivity()
                }
            } ?: run {
                startMainActivity()
            }
        }
    }

    private fun loadSplashAd() {
        AppOpenMax.getInstance().loadOpenMaxSplash(
            activity = this,
            appOpenAdId = BuildConfig.ads_open_app,
            timeDelay = 0,
            timeOut = 3500, // Timeout 3.5 giây để tránh giữ user quá lâu nếu no-fill
            isShowAdIfReady = true,
            adCallback = object : AppLovinCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    startMainActivity()
                }
            }
        )
    }

    private fun startMainActivity() {
        if (!isFinishing) {
            // Đảm bảo SDK được kích hoạt nếu user bỏ qua splash
            (application as? MyApplication)?.initMaxAds()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
```

---

### 2. App Open Ad Resume

Thư viện tự động lắng nghe vòng đời ứng dụng qua `ProcessLifecycleOwner`. Khi user rời app và quay lại, quảng cáo App Open Resume sẽ tự động được hiển thị nếu đã cấu hình `maxAdConfig.idAdResume`.

#### 🛡️ Cơ chế hiển thị thông minh (Chống treo màn hình "Welcome back"):
- **Chỉ hiển thị loading dialog khi ad đã sẵn sàng (`isReady == true`)**: Nếu quảng cáo chưa tải xong hoặc bị No-fill, thư viện sẽ **âm thầm tải ngầm trong nền** và không chặn màn hình của người dùng.
- **Tự động đóng an toàn (Safety Timeout 1.5s)**: Tránh tối đa trường hợp dialog bị kẹt khi hệ thống load ad bị delay.
- **Tự động đóng khi ad hiển thị (`onAdDisplayed`)**: Giúp chuyển cảnh vào quảng cáo mượt mà.

Nếu muốn bỏ qua (không hiện Resume Ad) tại một Activity cụ thể (ví dụ Splash):
```kotlin
AppOpenMax.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
```

Tạm thời vô hiệu hóa / bật lại resume ad trong code:
```kotlin
AppOpenMax.getInstance().isAppResumeEnabled = false // Tắt
AppOpenMax.getInstance().isAppResumeEnabled = true  // Bật lại
```

---

### 3. Interstitial Ad

#### A. Preload và Show Interstitial

```kotlin
class MainActivity : AppCompatActivity() {
    private var maxInterstitialAd: MaxInterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preloadInterstitial()
    }

    private fun preloadInterstitial() {
        maxInterstitialAd = AppLovin.getInstance().getInterstitialAds(this, "YOUR_INTERSTITIAL_ID")
    }

    private fun showInterstitial() {
        val inter = maxInterstitialAd
        if (inter != null && inter.isReady) {
            AppLovin.getInstance().forceShowInterstitial(
                this,
                inter,
                object : AdCallback() {
                    override fun onAdClosed() {
                        // Tải lại ad cho lần hiển thị tiếp theo
                        preloadInterstitial()
                    }
                },
                true // Hiển thị Dialog loading chuẩn bị trước khi show ad
            )
        } else {
            preloadInterstitial()
        }
    }
}
```

#### B. Hiển thị Interstitial theo số lần click (`showInterstitialAdByTimes`)

Cấu hình số lần click để show quảng cáo:
```kotlin
// Ví dụ: Click 3 lần mới hiển thị 1 lần Interstitial
MaxAd.getInstance().setCountClickToShowAds(3)

// Khi người dùng thực hiện một hành động (nhấn nút, chuyển bài, v.v.):
AppLovin.getInstance().showInterstitialAdByTimes(
    activity,
    maxInterstitialAd,
    object : AdCallback() {
        override fun onAdClosed() {
            preloadInterstitial()
        }
    }
)
```

---

### 4. Rewarded Ad

```kotlin
// 1. Preload Rewarded Ad
val maxRewardedAd = AppLovin.getInstance().getRewardAd(this, "YOUR_REWARDED_AD_ID", object : AppLovinCallback() {
    override fun onAdLoaded() {
        Log.d("Ad", "Reward ad loaded")
    }
})

// 2. Show Rewarded Ad
if (maxRewardedAd.isReady) {
    AppLovin.getInstance().showRewardAd(this, maxRewardedAd, object : AppLovinCallback() {
        override fun onUserRewarded(reward: MaxReward?) {
            // Cộng quà / lượt dùng cho user
            val amount = reward?.amount ?: 1
            Toast.makeText(this@MainActivity, "Đã nhận thưởng: $amount", Toast.LENGTH_SHORT).show()
        }

        override fun onAdClosed() {
            // Tải trước lại ad mới
        }
    })
}
```

---

### 5. Banner Ad (kèm Shimmer Loading)

#### Trong file XML Layout:
Nhúng component banner chuẩn của thư viện:
```xml
<FrameLayout
    android:id="@+id/layout_banner"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_alignParentBottom="true">

    <include layout="@layout/layout_banner_control" />
</FrameLayout>
```

#### Trong Kotlin Activity / Fragment:
```kotlin
AppLovin.getInstance().loadBanner(this, "YOUR_BANNER_AD_ID", object : AdCallback() {
    override fun onAdLoaded() {
        Log.d("Ad", "Banner loaded successfully")
    }

    override fun onAdFailedToLoad(error: ApAdError?) {
        Log.e("Ad", "Banner load failed: ${error?.message}")
    }
})
```

---

### 6. Native Ad (Medium / Small / Custom / RecyclerView)

Thư viện hỗ trợ nhiều kích thước Native Ad có tích hợp sẵn khung **Shimmer loading** tự động:

#### A. Native Medium (300x250 dp)
Trong file layout XML:
```xml
<include layout="@layout/layout_native_medium" />
```
Trong code Activity:
```kotlin
AppLovin.getInstance().loadNative(this, "YOUR_NATIVE_AD_ID")
```

#### B. Native Small
Trong file layout XML:
```xml
<include layout="@layout/layout_native_small" />
```
Trong code:
```kotlin
AppLovin.getInstance().loadNativeSmall(this, "YOUR_NATIVE_AD_ID")
```

#### C. Chèn Native Ad tự động vào RecyclerView

```kotlin
val originalAdapter = MyAdapter(dataList)

// Chèn lặp lại ad sau mỗi 5 items (bắt đầu từ vị trí 2)
val adPlacerAdapter = AppLovin.getInstance().getNativeRepeatAdapter(
    activity = this,
    adUnitId = "YOUR_NATIVE_AD_ID",
    originalAdapter = originalAdapter,
    layoutCustom = R.layout.custom_native_max_medium,
    repeatingInterval = 5,
    startPosition = 2
)

recyclerView.adapter = adPlacerAdapter
adPlacerAdapter.loadAds()
```

---

### 7. In-App Purchase & Subscription (Google Play Billing)

`AppPurchase` quản lý mua hàng không tiêu hao (Non-consumable), tiêu hao (Consumable) và gói định kỳ (Subscription):

```kotlin
// Đăng ký Listener nhận kết quả mua hàng
AppPurchase.getInstance().setPurchaseListener(object : PurchaseListener {
    override fun onProductPurchased(productId: String?, transactionDetails: String?) {
        Toast.makeText(this@MainActivity, "Mua thành công: $productId", Toast.LENGTH_SHORT).show()
        // Kích hoạt tính năng VIP, gỡ ads,...
    }

    override fun displayErrorMessage(errorMsg: String?) {
        Toast.makeText(this@MainActivity, "Lỗi: $errorMsg", Toast.LENGTH_SHORT).show()
    }

    override fun onUserCancelBilling() {
        Log.d("IAP", "Người dùng hủy thanh toán")
    }
})

// Thực hiện mua Gói Mua Một Lần (In-App Purchase)
AppPurchase.getInstance().purchase(this, "vip_lifetime")

// Thực hiện mua Gói Thuê Bao (Subscription)
AppPurchase.getInstance().subscribe(this, "vip_monthly")

// Kiểm tra user đã mua gói hay chưa (theo token cache / trạng thái đã mua)
val isVip = AppPurchase.getInstance().isPurchased(this)
```

---

### 8. Mediation Debugger & Privacy Settings

#### Mở AppLovin Mediation Debugger
Rất hữu ích trong quá trình phát triển (Dev) để kiểm tra tình trạng tích hợp các Adapter mediation:

```kotlin
AppLovinSdk.getInstance(this).showMediationDebugger()
```

#### Cho phép người dùng xem lại & thay đổi thiết lập GDPR Consent
Đặt một nút trong màn hình Cài đặt (Settings) của ứng dụng:

```kotlin
btnPrivacySettings.setOnClickListener {
    AppLovin.getInstance().showCmpForExistingUser(this) { error ->
        if (error == null) {
            Toast.makeText(this, "Đã cập nhật cài đặt quyền riêng tư", Toast.LENGTH_SHORT).show()
        }
    }
}
```

---

## 🛠️ Hướng dẫn Bật Test Ads trên AppLovin MAX & Khắc phục No-Fill (204)

Rất nhiều trường hợp bạn đã gắn `device_id` vào code nhưng quảng cáo vẫn **không lên** và báo lỗi `NO_FILL (code: 204)`. Dưới đây là hướng dẫn chi tiết để khắc phục:

### 1. Phân biệt rõ 2 loại Device ID
- **Google UMP Hashed ID (`TEST_DEVICE_HASHED_ID`)**:
  - Dùng để test popup **GDPR Consent Form** của Google UMP.
  - Lấy mã này bằng cách mở Logcat khi vừa bật app, tìm từ khóa `ConsentDebugSettings`.
  - **Mã này KHÔNG CÓ TÁC DỤNG bật Test Ads cho AppLovin MAX.**
- **Google Advertising ID (GAID)**:
  - Đây mới là mã để test quảng cáo MAX.
  - Lấy mã: Vào **Cài đặt điện thoại** > **Google** > **Tất cả dịch vụ** > **Quảng cáo (Ads)** > Copy **Mã nhận dạng cho quảng cáo** (dạng UUID: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`).

### 2. Cách bật Test Ads CHUẨN trên AppLovin Dashboard (Quan trọng nhất)
Hàm `setTestDeviceAdvertisingIds` trong code chỉ gửi thông tin test cho riêng AppLovin, **không thể tự động ép các mạng qua Mediation (Google AdMob, Meta, Mintegral...) trả về test ads**. Nếu mạng trong waterfall không có fill, bạn sẽ nhận mã lỗi `204 (NO_FILL)`.

👉 **Để chắc chắn nhận được quảng cáo Test 100%:**
1. Truy cập [AppLovin Dashboard](https://dash.applovin.com).
2. Vào menu **MAX** > **Mediation** > **Test Mode**.
3. Bấm **Add Test Device**:
   - Nhập tên máy và dán mã **GAID** (Advertising ID) lấy từ điện thoại vào.
   - Tại mục **Select Test Network**: Chọn **AppLovin Test Network** (hoặc *Google AdMob* nếu muốn test AdMob).
4. Bấm **Save**.
5. Đợi 2 - 5 phút rồi mở lại app, quảng cáo Test sẽ hiển thị 100%.

### 3. Kiểm tra mã lỗi bằng Mediation Debugger
Khi gặp lỗi không lên ads, hãy mở Mediation Debugger để xem chi tiết:
```kotlin
AppLovinSdk.getInstance(context).showMediationDebugger()
```
- Vào mục **Ad Units** > Chọn Ad Unit đang gặp lỗi (Splash, Banner, Native...).
- Bấm nút **Load Ad** tại từng mạng trung gian (Network) để xem phản hồi:
  - **`No ad available (code: 204)`**: Do chưa bật Test Mode trên Dashboard hoặc Ad Unit mới tạo chưa kích hoạt (thường mất 30-60 phút).
  - **`Invalid ad unit id (code: -5603)`**: Sai Ad Unit ID hoặc lệch **Package Name** giữa file `build.gradle` và ứng dụng đã đăng ký trên AppLovin Dashboard.
  - **`Missing Adapter / SDK`** (biểu tượng màu đỏ): Chưa thêm thư viện dependency của mạng Mediation tương ứng.

---

## 🔒 Cấu hình ProGuard

Khi kích hoạt `minifyEnabled true`, hãy đảm bảo thêm các rule sau vào `proguard-rules.pro`:

```proguard
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }

# AppLovin MAX
-keep class com.applovin.** { *; }
-dontwarn com.applovin.**

# Adjust
-keep class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult { int SUCCESS; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }

# AppsFlyer
-keep class com.appsflyer.** { *; }

# Pangle & Mintegral
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** { *; }
-keep interface com.mbridge.** { *; }
-dontwarn com.mbridge.**
-keep class **.R$* { public static final int mbridge*; }
```

---

## 🤝 Hỗ trợ & Đóng góp

- **Author**: [trongluan99](https://github.com/trongluan99)
- **Repository**: [https://github.com/trongluan99/AdsMaxInfinity](https://github.com/trongluan99/AdsMaxInfinity)
- Báo lỗi hoặc yêu cầu tính năng: Vui lòng mở [Issue trên GitHub](https://github.com/trongluan99/AdsMaxInfinity/issues).
