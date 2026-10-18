package com.journeygirl.assetforecast.ads

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.SystemClock
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
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
    // インタースティシャル（全画面）広告・アプリ起動広告の本番ユニット。
    // デバッグビルドでは自分の広告を誤クリックしないよう、常にGoogle公式のテスト用IDを使う。
    const val INTER_ID  = "ca-app-pub-3334691626809528/7666972025"
    const val APP_OPEN_ID = "ca-app-pub-3334691626809528/6645534394"
    private const val TEST_INTER_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    const val REWARD_ID = "ca-app-pub-3334691626809528/6097054036"

    // 追加：Unity Ads 用（値はあなたのダッシュボードのものに）
    const val UNITY_GAME_ID = "5976507"                       // ← あなたのAndroid Game ID
    const val UNITY_BANNER_PLACEMENT_ID = "Banner_Android"    // ← あなたのバナーPlacement名に変更
    // テストモードの広告は収益にならないので、本番（リリースビルド）では必ずOFF。デバッグビルドだけON。
    fun unityTestMode(context: Context) = isDebuggable(context)

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
    private var interstitialLoading = false
    private var rewarded: RewardedAd? = null

    // ── 全画面広告（インタースティシャル／アプリ起動）の表示頻度の上限 ──
    // どちらかを表示したら、その後一定時間はどちらも出さない（広告が続けて出るのを防ぐ）。
    // インタースティシャルは、起動直後の最初の保存でいきなり出ないよう、起動から同じ時間が経つまでも出さない。
    private const val FULLSCREEN_MIN_INTERVAL_MS = 3 * 60 * 1000L
    private val processStartAt = SystemClock.elapsedRealtime()
    private var lastFullScreenAdAt: Long? = null
    @Volatile var isShowingFullScreenAd = false
        private set

    private fun fullScreenCooldownPassed(now: Long) =
        lastFullScreenAdAt?.let { now - it >= FULLSCREEN_MIN_INTERVAL_MS } ?: true

    private fun isDebuggable(context: Context) =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private fun interstitialUnitId(context: Context): String =
        if (isDebuggable(context)) TEST_INTER_ID else INTER_ID

    // 頻度の上限を満たしていて、読み込み済みの広告がある時だけ表示する。表示しなかった場合は何もしない。
    fun maybeShowInterstitial(activity: Activity) {
        val now = SystemClock.elapsedRealtime()
        if (now - processStartAt < FULLSCREEN_MIN_INTERVAL_MS) return
        if (!fullScreenCooldownPassed(now) || isShowingFullScreenAd) return
        if (interstitial == null) {
            preloadInterstitial(activity)
            return
        }
        lastFullScreenAdAt = now
        isShowingFullScreenAd = true
        showInterstitial(activity) { isShowingFullScreenAd = false }
    }

    fun preloadInterstitial(context: Context) {
        val unitId = interstitialUnitId(context)
        if (interstitial != null || interstitialLoading) return
        interstitialLoading = true
        InterstitialAd.load(context, unitId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad; interstitialLoading = false }
                override fun onAdFailedToLoad(e: LoadAdError) { interstitial = null; interstitialLoading = false }
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

    // ── アプリ起動広告 ──
    // 読み込んだ広告は4時間で期限切れになる（Googleの仕様）ため、古いものは捨てて読み直す。
    private const val APP_OPEN_EXPIRY_MS = 4 * 60 * 60 * 1000L
    // アプリを裏から戻した時は、前回のアプリ起動広告からこの時間が経っている場合だけ出す
    private const val APP_OPEN_RESUME_INTERVAL_MS = 4 * 60 * 60 * 1000L
    private var appOpenAd: AppOpenAd? = null
    private var appOpenLoadedAt = 0L
    private var appOpenLoading = false
    private val appOpenWaiters = mutableListOf<(Boolean) -> Unit>()

    private fun appOpenUnitId(context: Context) =
        if (isDebuggable(context)) TEST_APP_OPEN_ID else APP_OPEN_ID

    private fun hasFreshAppOpenAd() =
        appOpenAd != null && SystemClock.elapsedRealtime() - appOpenLoadedAt < APP_OPEN_EXPIRY_MS

    // 読み込み済み（期限内）なら即 true。読み込み中・未読み込みなら読み込みを始め、結果を onResult で返す。
    fun loadAppOpen(context: Context, onResult: (Boolean) -> Unit = {}) {
        if (hasFreshAppOpenAd()) { onResult(true); return }
        appOpenAd = null
        appOpenWaiters += onResult
        if (appOpenLoading) return
        appOpenLoading = true
        AppOpenAd.load(context, appOpenUnitId(context), AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    appOpenLoadedAt = SystemClock.elapsedRealtime()
                    finishAppOpenLoad(true)
                }
                override fun onAdFailedToLoad(e: LoadAdError) { finishAppOpenLoad(false) }
            })
    }

    private fun finishAppOpenLoad(loaded: Boolean) {
        appOpenLoading = false
        val waiters = appOpenWaiters.toList()
        appOpenWaiters.clear()
        waiters.forEach { it(loaded) }
    }

    // 起動時：前回の全画面広告から一定時間経っていれば表示。
    // 裏から戻った時（isResume=true）：さらに前回のアプリ起動広告から4時間以上経っている場合だけ表示。
    // 表示した場合 true（onDismiss は広告を閉じた時に呼ばれる）。
    fun showAppOpenIfEligible(
        activity: Activity,
        isResume: Boolean,
        lastAppOpenWallMs: Long,
        onShown: () -> Unit,
        onDismiss: () -> Unit
    ): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (isShowingFullScreenAd || !fullScreenCooldownPassed(now)) return false
        if (isResume && System.currentTimeMillis() - lastAppOpenWallMs < APP_OPEN_RESUME_INTERVAL_MS) return false
        val ad = appOpenAd?.takeIf { hasFreshAppOpenAd() } ?: return false
        appOpenAd = null
        isShowingFullScreenAd = true
        lastFullScreenAdAt = now
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isShowingFullScreenAd = false
                loadAppOpen(activity)   // 次に戻ってきた時用に先読み
                onDismiss()
            }
            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                isShowingFullScreenAd = false
                loadAppOpen(activity)
                onDismiss()
            }
        }
        onShown()
        ad.show(activity)
        return true
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
