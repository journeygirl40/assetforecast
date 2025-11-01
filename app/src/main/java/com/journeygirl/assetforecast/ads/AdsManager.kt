package com.journeygirl.assetforecast.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

// 追加：広告プロバイダ（AdMob / Unity）
enum class AdProvider { ADMOB, UNITY }

object AdsManager {
    // バナーのみ正式なもの
    // app id :: ca-app-pub-3334691626809528~4696342453
//    const val BANNER_ID = "ca-app-pub-3940256099942544/6300978111" // ←まずテストIDで確認
    const val BANNER_ID = "ca-app-pub-3334691626809528/6097054036"  //本番
    const val INTER_ID  = "ca-app-pub-3334691626809528/6097054036"
    const val REWARD_ID = "ca-app-pub-3334691626809528/6097054036"

    // 追加：Unity Ads 用（値はあなたのダッシュボードのものに）
    const val UNITY_GAME_ID = "5976507"                       // ← あなたのAndroid Game ID
    const val UNITY_BANNER_PLACEMENT_ID = "Banner_Android"    // ← あなたのバナーPlacement名に変更
    const val UNITY_TEST_MODE = true                          // 動作確認なのでON

    // 追加：バナー用 プロバイダ優先順（AdMob → Unity）
    @Volatile
    var enabledBannerProviders: List<AdProvider> = listOf(AdProvider.ADMOB, AdProvider.UNITY)

    @Volatile
    private var bannerProviderIndex: Int = 0

    fun currentBannerProvider(): AdProvider =
        enabledBannerProviders[bannerProviderIndex % enabledBannerProviders.size]

    fun rotateBannerProviderOnFailure() {
        bannerProviderIndex = (bannerProviderIndex + 1) % enabledBannerProviders.size
    }

    fun noteBannerSuccess(@Suppress("UNUSED_PARAMETER") provider: AdProvider) {
        // 必要なら「成功した方を次回も優先」にする処理をここで実装可
    }


    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null

    fun preloadInterstitial(context: Context) {
        InterstitialAd.load(context, INTER_ID, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { interstitial = null }
            })
    }

    fun showInterstitial(activity: Activity, onDismiss: () -> Unit) {
        val ad = interstitial
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitial = null
                    preloadInterstitial(activity)
                    onDismiss()
                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    interstitial = null
                    preloadInterstitial(activity)
                    onDismiss()
                }
            }
            ad.show(activity)
        } else {
            preloadInterstitial(activity)
            onDismiss()
        }
    }

    fun preloadRewarded(context: Context) {
        RewardedAd.load(context, REWARD_ID, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewarded = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { rewarded = null }
            })
    }

    fun showRewarded(activity: Activity, onRewardEarned: (RewardItem) -> Unit, onFinish: () -> Unit) {
        val ad = rewarded
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewarded = null
                    preloadRewarded(activity)
                    onFinish()
                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    rewarded = null
                    preloadRewarded(activity)
                    onFinish()
                }
            }
            ad.show(activity) { reward -> onRewardEarned(reward) }
        } else {
            preloadRewarded(activity)
            onFinish()
        }
    }
}
