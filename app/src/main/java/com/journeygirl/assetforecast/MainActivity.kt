package com.journeygirl.assetforecast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.core.view.WindowCompat
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.shape.CircleShape
import com.journeygirl.assetforecast.ui.theme.ColorPresets
import com.journeygirl.assetforecast.ui.theme.JapaneseStyleChoices
import com.journeygirl.assetforecast.ui.theme.LocalSeriesColors
import com.journeygirl.assetforecast.ui.theme.SeriesColorChoices
import com.journeygirl.assetforecast.ui.theme.WesternStyleChoices
import com.journeygirl.assetforecast.ui.theme.customChoice
import com.journeygirl.assetforecast.ui.theme.presetChoice
import com.journeygirl.assetforecast.ui.theme.resolveColorChoice
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeygirl.assetforecast.data.db.AssetRecord
import com.journeygirl.assetforecast.data.prefs.ForecastSettings
import com.journeygirl.assetforecast.data.prefs.ExpenseRepeat
import com.journeygirl.assetforecast.data.prefs.ScheduledExpense
import com.journeygirl.assetforecast.data.prefs.netFor
import com.journeygirl.assetforecast.viewmodel.MainViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.utils.MPPointD
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.gms.ads.*
import com.google.android.ump.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.roundToInt
import java.text.NumberFormat
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.zIndex
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.ui.layout.onSizeChanged
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.lazy.rememberLazyListState
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.draggable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.YearMonth
import java.time.*
import java.text.DecimalFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.view.Gravity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import android.graphics.*
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.utils.MPPointF
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.app.DatePickerDialog
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
// ▼ 追加
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange

import androidx.compose.material.icons.Icons
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.unit.sp
import kotlin.math.roundToLong
import java.math.BigDecimal
import java.math.RoundingMode
// ★ Unity / Ads 管理
import android.app.Activity
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import com.journeygirl.assetforecast.ads.AdsManager
import com.journeygirl.assetforecast.ads.AdProvider



class MainActivity : ComponentActivity() {
    private val vm by viewModels<MainViewModel>()

    // 広告SDKの初期化が終わったか（バナー・インタースティシャルの表示許可に使う）
    private var adsReady by mutableStateOf(false)
    // スプラッシュ画面を出し続けるか。アプリ起動広告を待つ間だけ true（最大でも約4秒）
    private var keepSplash = true
    private val adPrefs by lazy { getSharedPreferences("ads", MODE_PRIVATE) }

    // アプリが裏から前面に戻った時のアプリ起動広告。
    // 登録直後に現在の状態（起動済み）が通知されるので、最初の1回は無視する。
    private var firstForegroundEventSkipped = false
    private val appForegroundObserver = object : androidx.lifecycle.DefaultLifecycleObserver {
        override fun onStart(owner: androidx.lifecycle.LifecycleOwner) {
            if (!firstForegroundEventSkipped) { firstForegroundEventSkipped = true; return }
            if (AdsManager.skipNextResumeAppOpen) { AdsManager.skipNextResumeAppOpen = false; return }
            if (!adsReady || vm.settings.value.adFree) return
            AdsManager.showAppOpenIfEligible(
                activity = this@MainActivity,
                isResume = true,
                lastAppOpenWallMs = adPrefs.getLong(KEY_LAST_APP_OPEN, 0L),
                onShown = { adPrefs.edit().putLong(KEY_LAST_APP_OPEN, System.currentTimeMillis()).apply() },
                onDismiss = {}
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // スプラッシュ画面（Android 12未満にも同じ見た目で出る）。super.onCreateより前に呼ぶ必要がある
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { keepSplash }
        // 画面回転などで作り直された時は起動扱いにしない（起動広告はプロセスの最初の1回だけ）
        val isColdStart = !coldStartHandled
        coldStartHandled = true
        if (!isColdStart) keepSplash = false
        lifecycleScope.launch { startAds(isColdStart) }
        androidx.lifecycle.ProcessLifecycleOwner.get().lifecycle.addObserver(appForegroundObserver)

        // ① コンテンツをシステムバーの下まで広げる（Edge-to-Edge）
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ② ステータスバーを透明に
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // 画面を先に描画
        setContent {
            // 設定画面の「テーマ」選択（system/light/dark）に応じてダーク/ライトを決定する。
            // "system"の場合のみ端末設定に追従する。
            val settings by vm.settings.collectAsState()
            val darkTheme = when (settings.themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            com.journeygirl.assetforecast.ui.theme.AssetforecastTheme(
                darkTheme = darkTheme,
                seriesColorChoices = settings.seriesColorChoices()
            ) {
                // ③ ステータスバー/ナビゲーションバーのアイコン色は、ダーク基調テーマでは
                // 濃い背景に馴染むよう白寄りに、ライトモードでは黒寄りに動的に切り替える。
                val view = androidx.compose.ui.platform.LocalView.current
                androidx.compose.runtime.SideEffect {
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = !darkTheme
                    controller.isAppearanceLightNavigationBars = !darkTheme
                }

                AppRoot(vm = vm, showAds = adsReady)
            }
        }
    }

    override fun onDestroy() {
        androidx.lifecycle.ProcessLifecycleOwner.get().lifecycle.removeObserver(appForegroundObserver)
        super.onDestroy()
    }

    // 広告まわりの初期化（同意確認 → AdMob/Unity初期化）と、起動時のアプリ起動広告。
    // 起動広告はスプラッシュ画面の間（最大約4秒）に間に合った時だけ出し、間に合わなければ出さない。
    private suspend fun startAds(isColdStart: Boolean) {
        val splashDeadline = android.os.SystemClock.elapsedRealtime() + APP_OPEN_SPLASH_WAIT_MS
        // 何があってもスプラッシュで止まり続けないための保険
        lifecycleScope.launch { kotlinx.coroutines.delay(APP_OPEN_SPLASH_WAIT_MS + 500); keepSplash = false }

        val launchCount = adPrefs.getInt(KEY_LAUNCH_COUNT, 0) + if (isColdStart) 1 else 0
        if (isColdStart) adPrefs.edit().putInt(KEY_LAUNCH_COUNT, launchCount).apply()

        // UMP（同意管理）。同意フォームは必要な人（未回答の地域のユーザー）にだけ出す。
        // フォームを出した起動では、続けて起動広告まで出すと煩わしいので起動広告は出さない。
        // 同意情報の更新は通信で2秒以上かかることがあり、待つと起動広告が3秒に間に合わないため、
        // Google公式サンプルと同様に裏で並行して進め、前回までの同意状態で広告の準備を先に始める。
        // 前回の時点で「同意が必要（未回答）」だった人だけは、フォームの結果を待ってから進める。
        var consentFormShown = false
        val consentInfo = UserMessagingPlatform.getConsentInformation(this)
        val consentJob = lifecycleScope.launch {
            runCatching {
                suspendCancellableCoroutine<Unit> { cont ->
                    consentInfo.requestConsentInfoUpdate(
                        this@MainActivity, ConsentRequestParameters.Builder().build(),
                        {
                            if (consentInfo.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                                consentFormShown = true
                                keepSplash = false   // フォームはメイン画面の上に出す
                            }
                            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this@MainActivity) { cont.resume(Unit) {} }
                        },
                        { cont.resume(Unit) {} }
                    )
                }
            }
        }
        if (consentInfo.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
            consentFormShown = true
            keepSplash = false
            consentJob.join()
        }

        // AdMob / Unity Ads 初期化。メインスレッドで行うと数秒画面が止まるため、裏のスレッドで行う
        // （Google推奨）。完了を待たずに広告の読み込みを始めてよい（SDK側で待ち合わせる）
        val appContext = applicationContext
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            MobileAds.initialize(this@MainActivity) {
                lifecycleScope.launch { adsReady = true }
            }
            // Unity Ads（AdMob で埋まらなかった時のバナー用）
            runCatching {
                com.unity3d.ads.UnityAds.initialize(appContext, AdsManager.UNITY_GAME_ID, AdsManager.unityTestMode(appContext))
            }
        }

        val adFree = vm.settings.value.adFree
        // 初回起動は出さない（最初の印象を広告にしない）
        val wantAppOpen = isColdStart && launchCount >= 2 && !consentFormShown && !adFree
        if (wantAppOpen) {
            val remaining = splashDeadline - android.os.SystemClock.elapsedRealtime()
            val loaded = remaining > 0 && kotlinx.coroutines.withTimeoutOrNull(remaining) {
                suspendCancellableCoroutine<Boolean> { cont ->
                    AdsManager.loadAppOpen(this@MainActivity) { ok -> if (cont.isActive) cont.resume(ok) {} }
                }
            } == true
            // スプラッシュが既に消えていたら（時間切れ）出さない：メイン画面に突然かぶせない
            if (loaded && keepSplash && !consentFormShown) {
                AdsManager.showAppOpenIfEligible(
                    activity = this,
                    isResume = false,
                    lastAppOpenWallMs = adPrefs.getLong(KEY_LAST_APP_OPEN, 0L),
                    onShown = { adPrefs.edit().putLong(KEY_LAST_APP_OPEN, System.currentTimeMillis()).apply() },
                    onDismiss = {}
                )
            }
        } else if (!adFree) {
            AdsManager.loadAppOpen(this)   // 裏から戻ってきた時用に先読み
        }
        keepSplash = false
    }

    companion object {
        // 起動広告をスプラッシュ画面で待つ最長時間。長くするほど表示機会は増えるが起動が遅く感じられる
        private const val APP_OPEN_SPLASH_WAIT_MS = 4000L
        private const val KEY_LAUNCH_COUNT = "launch_count"
        private const val KEY_LAST_APP_OPEN = "last_app_open_wall_ms"
        // プロセス内で起動時の処理を済ませたか（画面回転などによる作り直しと本当の起動を区別する）
        private var coldStartHandled = false
    }
}

private enum class SelectionSource { LIST, CHART }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(
    vm: MainViewModel = viewModel(),
    showAds: Boolean = false   // ← 追加
) {    var showSettings by remember { mutableStateOf(false) }

    // ▼ ボタンの位置（ドラッグで更新）
    var fabOffsetX by remember { mutableStateOf(0f) }
    var fabOffsetY by remember { mutableStateOf(-130f) }

    var settingsOffsetX by remember { mutableStateOf(0f) }
    var settingsOffsetY by remember { mutableStateOf(-130f) }

    val loadedSettings by vm.loadedSettings.collectAsState()
    val settings = loadedSettings ?: vm.settings.value
    val settingsLoaded = loadedSettings != null
    LaunchedEffect(settings.language) { vm.applyLanguage(settings.language) }
    val records by vm.records.collectAsState()
    var editing by remember { mutableStateOf<AssetRecord?>(null) }

    var showAdd by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<Long?>(null) }   // ← AssetRecord.id が Int の想定
    // 選択中の予測点の日付（予測は記録ではなくidが無いので日付で持つ）。実績の選択(selectedId)とはどちらか一方だけ
    var selectedForecastEpoch by remember { mutableStateOf<Int?>(null) }
    // リストで選んだ点をグラフにも反映するための依頼（同じ点を選び直しても反映されるよう連番を持たせる）
    var chartHighlightRequest by remember { mutableStateOf<ChartHighlightRequest?>(null) }
    var chartHighlightSeq by remember { mutableStateOf(0L) }
    var selectionSource by remember { mutableStateOf<SelectionSource?>(null) }
    val listState = rememberLazyListState()

    // ▼ グラフのミニマップナビゲーター用の表示範囲。アプリを閉じても同じ範囲に戻れるよう永続化する。
    // DisplayRangeのstart/endはそれぞれ独立にnull（無制限）になり得るため、Pair<Int,Int>のような
    // 「両方とも具体値」前提の型にすると、片方だけnullな状態を表現できず不正な範囲になってしまう。
    val savedDisplayRange by vm.displayRange.collectAsState()
    var customRange by remember { mutableStateOf<DisplayRange?>(null) }
    // 起動直後に保存値を1回だけcustomRangeへ復元するためのフラグ。これが立つまでは
    // 下のLaunchedEffect(customRange)による保存処理を走らせない（復元した値をそのまま
    // 書き戻すだけの無駄な保存や、復元前のnull状態での上書きを防ぐため）。
    var displayRangeLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(savedDisplayRange) {
        // savedDisplayRangeがnullの間はDataStoreからまだ読み込めていない状態なので待つ。
        val saved = savedDisplayRange ?: return@LaunchedEffect
        if (!displayRangeLoaded) {
            customRange = if (saved.start == null && saved.end == null) {
                null
            } else {
                DisplayRange(saved.start, saved.end)
            }
            displayRangeLoaded = true
        }
    }

    // ミニマップのドラッグ中は高頻度でcustomRangeが変わるため、都度DataStoreへ書き込まず、
    // 操作が止まってから保存する（デバウンス）。
    LaunchedEffect(customRange, displayRangeLoaded) {
        if (displayRangeLoaded) {
            kotlinx.coroutines.delay(400)
            vm.saveDisplayRange(customRange?.start, customRange?.end)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    // --- エクスポート launcher ---
    // 記録・定期的な収支・設定を1つのCSVに書き出す（形式は data/backup/BackupCsv.kt）
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    OutputStreamWriter(os, Charsets.UTF_8).use { w ->
                        w.write(
                            com.journeygirl.assetforecast.data.backup.buildBackupCsv(
                                records = vm.records.value,
                                scheduled = vm.scheduledExpenses.value,
                                settings = vm.settings.value
                            )
                        )
                    }
                }
            }.isSuccess
            android.widget.Toast.makeText(
                context, if (ok) "書き出しました" else "書き出しに失敗しました", android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    // --- インポート launcher ---
    // 旧形式（記録だけ）のCSVも読める。すでにある記録・収支と同じものは追加しない
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.use { ins ->
                    BufferedReader(InputStreamReader(ins, Charsets.UTF_8)).use { it.readText() }
                }
            }.getOrNull()
            if (text == null) {
                android.widget.Toast.makeText(context, "読み込みに失敗しました", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                val data = com.journeygirl.assetforecast.data.backup.parseBackupCsv(text, vm.settings.value)
                vm.importBackup(data) { r ->
                    val parts = buildList {
                        if (r.addedRecords > 0) add("記録 ${r.addedRecords}件")
                        if (r.addedScheduled > 0) add("定期的な収支 ${r.addedScheduled}件")
                    }
                    val added = if (parts.isEmpty()) "新しい記録・収支はありませんでした（すべて登録済み）"
                    else parts.joinToString("・") + "を追加しました"
                    val msg = added + (if (r.settingsApplied) "\n設定を読み込みました" else "")
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    val scheduledExpenses by vm.scheduledExpenses.collectAsState()
    val forecasts = remember(records, settings, scheduledExpenses) {
        buildForecastPoints(
            records = records,
            months = settings.months,
            sampleN = settings.sampleN,
            mode = settings.mode,
            customRateMonthly = settings.customRatePerMonth,  // ← 追加
            customDeltaPerMonth = settings.customDeltaPerMonth,   // ★ 追加
            drawdownStartMonth = settings.drawdownStartMonth,   // ← 渡す
            withdrawPerMonth  = settings.withdrawPerMonth,      // ← 渡す
            expenses = scheduledExpenses
        )
    }

    LaunchedEffect(selectedId, selectedForecastEpoch, records, selectionSource) {
        if (selectionSource == SelectionSource.CHART && selectedId != null) {
            val sorted = records.sortedBy { it.dateEpochDays }
            val index = sorted.indexOfFirst { it.id == selectedId }

            if (index >= 0) {
                // StickyHeaderが1件あるため +1。scrollOffset=0 でリストの最上部に固定。
                listState.scrollToItem(index + 0, scrollOffset = 0)
            }

            // 一度スクロールしたらフラグをリセット
            selectionSource = null
        } else if (selectionSource == SelectionSource.CHART && selectedForecastEpoch != null) {
            val index = forecasts.indexOfFirst { it.epochDays == selectedForecastEpoch }
            if (index >= 0) {
                // 予測行の位置 = 実績ヘッダー(1) + 実績(records.size) + 予測タイトル(1) + 予測ヘッダー(1) + index。
                // 実績と同じく1つ手前の行を先頭にし、固定ヘッダーの下に選択行が見えるようにする。
                listState.scrollToItem(records.size + 2 + index, scrollOffset = 0)
            }

            // 一度スクロールしたらフラグをリセット
            selectionSource = null
        }
        // LIST の場合はスクロールなし
    }

// 予測用の月数が2ヶ月以上あるかを判定（同じ月内の複数レコードは1ヶ月として数える）
    val monthCount = remember(records) {
        records.groupBy { r ->
            val d = java.time.LocalDate.ofEpochDay(r.dateEpochDays.toLong())
            d.year * 100 + d.monthValue
        }.size
    }

// 予測に使った平均率（小数）と、見出し用の表示文字列
    val avgRateForLabel = remember(records, settings) {
        val customMonthly: Double? = settings.customRatePerMonth  // ★ ローカルに退避
        if (settings.mode == "custom" && customMonthly != null) {
            "(年率指定${formatAnnualFromMonthly(customMonthly)})"
        } else if (monthCount >= 2) {
            "(${formatMonthlyRateLabel(averageMonthlyRate(records, settings.sampleN))})"
        } else null
    }

    Scaffold(
        topBar = {
        },
        floatingActionButton = {
            // --- 新規登録ボタン（右下・ドラッグ可） ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
            ) {
                FloatingActionButton(
                    onClick = { showAdd = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd) // ← 修正ポイント
                        .offset { IntOffset(fabOffsetX.toInt(), fabOffsetY.toInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                fabOffsetX += dragAmount.x
                                fabOffsetY += dragAmount.y
                            }
                        }
                ) {
                    Text("+")
                }
            }

            // --- 設定ボタン（左下・ドラッグ可） ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
            ) {
                SmallFloatingActionButton(
                    onClick = { showSettings = true },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        // ✅ 元の位置と同じオフセット
                        .padding(start = 18.dp)
                        .offset { IntOffset(settingsOffsetX.toInt(), settingsOffsetY.toInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                settingsOffsetX += dragAmount.x
                                settingsOffsetY += dragAmount.y
                            }
                        }
                ) {
                    Icon(imageVector = Icons.Filled.Settings, contentDescription = "設定")
                }
            }
        },
    ) { inner ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(inner)          // ✅ Scaffold から渡される安全領域を反映
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val density = LocalDensity.current
                val totalPx = with(density) { maxHeight.toPx() }         // 有効ドラッグ量の基準
                var split by rememberSaveable { mutableStateOf(0.45f) }   // 上(グラフ)の比率 0.0..1.0
                val handleHeight = 12.dp                                  // つまみの高さ
                val latestEpochDays: Int? = records.maxByOrNull { it.dateEpochDays }?.dateEpochDays
                // グラフ（またはリスト）で選択中の記録の日付。ミニマップ上に選択位置をマーカー表示するために使う。
                val selectedEpochDays: Int? = remember(records, selectedId, selectedForecastEpoch) {
                    selectedId?.let { id -> records.firstOrNull { it.id == id }?.dateEpochDays } ?: selectedForecastEpoch
                }

                // ▼ ミニマップナビゲーターの選択範囲から実際のグラフ表示範囲を算出（null=全期間）
                // 予測期間の変更（例：10年→50年、50年→5年）で全期間が伸び縮みしても、保存済みの範囲が
                // 全期間からはみ出したり、「末尾まで」のつもりが途中止まりになったりしないよう、全期間に合わせて補正する。
                val fullStartEpoch = records.minOfOrNull { it.dateEpochDays }
                val fullEndEpoch = forecasts.lastOrNull()?.epochDays ?: records.maxOfOrNull { it.dateEpochDays }
                val displayRange: DisplayRange = normalizeDisplayRange(
                    range = customRange,
                    fullStart = fullStartEpoch,
                    fullEnd = fullEndEpoch
                )

                // リストで選んだ点をグラフで選択状態にする。表示範囲の外なら、範囲の幅は変えずにその点が中央に来るようずらす。
                fun selectInChart(epochDays: Int?, amount: Long, isForecast: Boolean) {
                    if (epochDays != null && fullStartEpoch != null && fullEndEpoch != null) {
                        val s = displayRange.start ?: fullStartEpoch
                        val e = displayRange.end ?: fullEndEpoch
                        if (epochDays !in s..e) {
                            val width = e - s
                            var ns = epochDays - width / 2
                            var ne = ns + width
                            if (ns < fullStartEpoch) { ns = fullStartEpoch; ne = fullStartEpoch + width }
                            if (ne > fullEndEpoch) { ne = fullEndEpoch; ns = fullEndEpoch - width }
                            customRange = normalizeDisplayRange(DisplayRange(ns, ne), fullStartEpoch, fullEndEpoch)
                                .takeIf { it.start != null || it.end != null }
                        }
                    }
                    chartHighlightRequest = ChartHighlightRequest(epochDays, amount, isForecast, ++chartHighlightSeq)
                }
                // 補正した範囲を保存値にも反映する（反映しないと、予測期間を元に戻した時に古い範囲が復活してしまう）。
                // 記録の読み込み前（空）に補正すると保存済みの範囲が消えるので、記録がある時だけ行う。
                // 設定（予測年数）も初期値のままだと全期間が正しくないので、読み込み後だけ行う。
                LaunchedEffect(displayRange, displayRangeLoaded, settingsLoaded) {
                    if (displayRangeLoaded && settingsLoaded && records.isNotEmpty()) {
                        val normalized = displayRange.takeIf { it.start != null || it.end != null }
                        if (customRange != normalized) customRange = normalized
                    }
                }

                Column(Modifier.fillMaxSize()) {

                    // ── 上：グラフ（高さは split に応じて可変） ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(split)
                    ) {
                        AssetChartSection(
                            records = records,
                            forecastMonths = settings.months,
                            sampleN = settings.sampleN,
                            mode = settings.mode,
                            customRateMonthly = settings.customRatePerMonth,
                            customDeltaPerMonth = settings.customDeltaPerMonth,
                            drawdownStartMonth = settings.drawdownStartMonth,
                            withdrawPerMonth = settings.withdrawPerMonth,
                            scheduledExpenses = scheduledExpenses,
                            range = displayRange,
                            onPointSelected = { epochDaysOrNull: Int?, isForecast: Boolean ->
                                if (isForecast) {
                                    selectedId = null
                                    selectedForecastEpoch = epochDaysOrNull
                                } else {
                                    selectedForecastEpoch = null
                                    selectedId = epochDaysOrNull?.let { picked ->
                                        records.minByOrNull { r -> kotlin.math.abs(r.dateEpochDays - picked) }?.id
                                    }
                                }
                                selectionSource = SelectionSource.CHART
                            },
                            modifier = Modifier
                                .fillMaxSize() // ← Box 全体にグラフを広げる
                                .padding(0.dp),
                            // ▼ 追加：初期ハイライトに最新実績を渡す
                            initialHighlightEpochDays = latestEpochDays,
                            forecastAmounts = remember(forecasts) { forecasts.associate { it.epochDays to it.amount } },
                            highlightRequest = chartHighlightRequest
                        )
                    }

                    // ── グラフとリストの間：全期間（実績＋予測）の簡易推移＋現在の表示範囲ハイライト（ナビゲーター） ──
                    // ミニマップの背景には常に実績・予測全体の推移が描かれているため、専用のリセット
                    // ボタンは置かず、ハンドル／ハイライト枠のドラッグだけで全期間表示へ戻せるようにする。
                    MiniMapNavigator(
                        records = records,
                        forecasts = forecasts,
                        range = displayRange,
                        selectedEpochDays = selectedEpochDays,
                        onRangeChange = { newRange ->
                            customRange = if (newRange.start == null && newRange.end == null) null else newRange
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 4.dp)   // 上はグラフの日付ラベルと詰まらないよう広め
                    )
                    // ミニマップで選んだ期間の差分平均・増減率（グラフの高さを奪わないよう1行の小さな帯にする）。
                    // ミニマップの補足情報なので控えめな文字にし、下のつまみ・リストとの間は少し空けて別物に見せる。
                    RangeStatsStrip(
                        records = records,
                        range = displayRange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, bottom = 6.dp)
                    )

                    // ── 中央：ドラッグつまみ ──
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(handleHeight)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .draggable(
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { dy ->
                                    // dy(px) を全体高さで割って比率に変換し、0.15〜0.85にクランプ
                                    if (totalPx > 0f) {
                                        split = (split + dy / totalPx).coerceIn(0.15f, 0.85f)
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // つまみの見た目（小さなバー）
                        Box(
                            Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }

                    // ── 下：リスト（残りの高さ） ──
                    @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f - split)     // ← 残りの比率
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(bottom = 0.dp)
                    ) {
                        stickyHeader { ListHeader() }

                        val list = records.sortedBy { it.dateEpochDays }

                        itemsIndexed(list) { index, r ->
                            val prevAmount = list.getOrNull(index - 1)?.amount
                            val diff = if (prevAmount == null) 0L else (r.amount - prevAmount)

                            RecordRow(
                                r = r,
                                selected = (r.id == selectedId),
                                diff = diff,
                                onClick = {
                                    if (selectedId == r.id) {
                                        editing = r
                                        selectedId = null
                                        selectionSource = null
                                    } else {
                                        selectedId = r.id
                                        selectedForecastEpoch = null
                                        selectionSource =
                                            SelectionSource.LIST   // ← リスト由来なのでスクロールしない
                                        selectInChart(r.dateEpochDays, r.amount, isForecast = false)
                                    }
                                }
                            )
                        }

// ─── 将来予測ブロック ───
                        if (forecasts.isNotEmpty() || monthCount >= 2) {
                            // タイトル行（非固定）
                            item {
                                val suffix = avgRateForLabel ?: "（-）"
                                Text(
                                    text = "将来予測$suffix",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(
                                        top = 8.dp,
                                        start = 8.dp,
                                        end = 8.dp
                                    )
                                )
                            }
                            // ← ヘッダーを stickyHeader に（実績と同じ動き）
                            stickyHeader { ForecastHeaderRow() }

                            // 予測行（年月日 / 追加金額 / 差分 / 資産額）
                            itemsIndexed(forecasts) { idx, p ->
                                val prevAmount =
                                    if (idx == 0) records.maxByOrNull { it.dateEpochDays }?.amount
                                        ?: p.amount
                                    else forecasts[idx - 1].amount
                                val diff = p.amount - prevAmount
                                ForecastRow(
                                    p = p,
                                    diff = diff,                 // ← 差分
                                    addFixed = p.addFixed ?: 0L, // ← 固定追加（未使用なら 0）
                                    selected = p.epochDays == selectedForecastEpoch,
                                    onClick = {
                                        // 予測は編集できないので、もう一度タップしたら選択解除だけ
                                        if (selectedForecastEpoch == p.epochDays) {
                                            selectedForecastEpoch = null
                                            selectionSource = null
                                            selectInChart(null, 0L, isForecast = true)
                                        } else {
                                            selectedForecastEpoch = p.epochDays
                                            selectedId = null
                                            selectionSource = SelectionSource.LIST
                                            selectInChart(p.epochDays, p.amount, isForecast = true)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            // ===== 広告（overlay：コンテンツを押し上げない）=====
            if (showAds && !settings.adFree) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .zIndex(10f)
                ) {
                    BannerAdView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                }
            }
        }
    }
    // ── インタースティシャル広告 ──
    // 広告SDKの初期化が終わったら1枚先読みしておき、記録の追加・編集を保存した直後（操作の区切り）に
    // 頻度上限付きで表示する（上限や読み込み状況の判定は AdsManager.maybeShowInterstitial 側）。
    val adActivity = remember(context) { context.findActivity() }
    LaunchedEffect(showAds, settings.adFree) {
        if (showAds && !settings.adFree) {
            com.journeygirl.assetforecast.ads.AdsManager.preloadInterstitial(context)
        }
    }
    fun maybeShowInterstitialAfterSave() {
        if (showAds && !settings.adFree && adActivity != null) {
            com.journeygirl.assetforecast.ads.AdsManager.maybeShowInterstitial(adActivity)
        }
    }

    if (showAdd) {
        AddDialog(
            onDismiss = { showAdd = false },
            onSubmit = { date, amount, note ->
                vm.addRecord(date, amount, note)
                showAdd = false
                maybeShowInterstitialAfterSave()
            }
        )
    }
    editing?.let { target ->
        EditDialog(
            initial = target,
            onDismiss = { editing = null; selectedId = null },
            onChange = { date, amount, note ->
                vm.updateRecord(target.id, date, amount, note)
                editing = null
                selectedId = null
                maybeShowInterstitialAfterSave()
            },
            onDelete = {
                vm.deleteRecord(target)
                editing = null
                selectedId = null
            }
        )
    }
    if (showSettings) {
        SettingsDialog(
            current = settings,
            onClose = { showSettings = false },
            onSave = { vm.saveSettings(it) },
            onExportCsv = {
                val name = "assets_${LocalDate.now()}.csv"
                AdsManager.skipNextResumeAppOpen = true
                exportLauncher.launch(name)
            },
            onImportCsv = {
                AdsManager.skipNextResumeAppOpen = true
                // 読み込んだ設定を、開いたままの設定画面の古い入力値で「保存」して上書きしないよう、先に閉じる
                showSettings = false
                importLauncher.launch("text/*") // 端末によっては "text/comma-separated-values" しか出ないケースがあるため広めに
            },
            // テーマだけを即時保存する。保存済みsettingsを基にするので、ダイアログで編集中の
            // 未保存の他項目（予測年数など）はここでは書き込まれない。
            onThemeModeChange = { vm.saveSettings(settings.copy(themeMode = it)) },
            // 色もテーマと同様に即時保存・反映する
            onSeriesColorsChange = { c ->
                vm.saveSettings(
                    settings.copy(
                        colorGain = c.gain, colorLoss = c.loss,
                        colorActual = c.actual, colorForecast = c.forecast
                    )
                )
            },
            scheduledExpenses = scheduledExpenses,
            onScheduledExpensesChange = { vm.saveScheduledExpenses(it) },
            forecastStart = records.maxByOrNull { it.dateEpochDays }
                ?.let { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDays.toLong())).plusMonths(1) }
        )
    }
}

@Composable
fun AddDialog(
    onDismiss: () -> Unit,
    onSubmit: (LocalDate, Long, String?) -> Unit
) {
    var dateText by remember { mutableStateOf(LocalDate.now().format(DATE_FMT)) } // 例: 2025/10/11
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(Modifier.fillMaxWidth()) {
                Text("新規入力", style = MaterialTheme.typography.titleLarge)
                Text(
                    "編集は既存レコードをダブルタップ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        text = {
            val scroll = rememberScrollState()
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)          // 端末差で溢れるのを抑える上限（好みで調整可）
                    .verticalScroll(scroll),         // ← これで“スワイプ”でスクロール可能に
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateField(
                    label = "日付 (yyyy/MM/dd)",
                    value = dateText,
                    onValueChange = { dateText = it }
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("資産額（円）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = appTextFieldColors()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("備考（任意）") },
                    singleLine = false, // ← 単一行モードを解除
                    maxLines = 5,       // ← 最大5行まで自動で拡張（必要なら増やせます）
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 200.dp), // ← 文字量に応じて伸びる枠
                    colors = appTextFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val date = parseDateOrNull(dateText) ?: LocalDate.now()
                val amount = amountText.toLongOrNull() ?: 0L
                onSubmit(date, amount, note.ifBlank { null })
            }) { Text("登録") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}
@Composable
fun EditDialog(
    initial: AssetRecord,
    onDismiss: () -> Unit,
    onChange: (LocalDate, Long, String?) -> Unit,
    onDelete: () -> Unit
) {
    var dateText by remember { mutableStateOf(LocalDate.ofEpochDay(initial.dateEpochDays.toLong()).format(DATE_FMT)) }
    var amountText by remember { mutableStateOf(initial.amount.toString()) }
    var note by remember { mutableStateOf(initial.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("編集") },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField(
                    label = "日付 (yyyy/MM/dd)",
                    value = dateText,
                    onValueChange = { dateText = it }
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("資産額（円）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = appTextFieldColors()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("備考（任意）") },
                    singleLine = false, // ← 単一行モードを解除
                    maxLines = 5,       // ← 最大5行まで自動で拡張（必要なら増やせます）
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 200.dp), // ← 文字量に応じて伸びる枠
                    colors = appTextFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val date = parseDateOrNull(dateText) ?: LocalDate.now()
                val amount = amountText.toLongOrNull() ?: 0L
                onChange(date, amount, note.ifBlank { null })
            }) { Text("変更") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("削除") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismiss) { Text("キャンセル") }
            }
        }
    )
}
private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

private fun parseDateOrNull(s: String): LocalDate? =
    try {
        // まずは「yyyy/MM/dd」で解析
        LocalDate.parse(s, DATE_FMT)
    } catch (_: Exception) {
        // 互換のため、旧「yyyy-MM-dd」(ISO) でも試す
        try { LocalDate.parse(s) } catch (_: Exception) { null }
    }
private fun localDateToMillis(d: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
    d.atStartOfDay(zone).toInstant().toEpochMilli()

private fun millisToLocalDate(millis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

// 金額の表示を 10,000 のように3桁区切りにする
private val yenNumberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.JAPAN).apply {
    isGroupingUsed = true
    maximumFractionDigits = 0
}

private fun formatYen(amount: Long): String = yenNumberFormat.format(amount)
private fun formatYenSigned(n: Long): String = when {
    n > 0  -> "+¥%,d".format(n)
    n < 0  -> "-¥%,d".format(-n)
    else   -> "±¥0"
}
/** 年率(小数, 例: 0.04) → 月率(小数) */
private fun monthlyFromAnnualRate(annual: Double): Double =
    (1.0 + annual).pow(1.0 / 12.0) - 1.0

/** 月率(小数) → 年率(%) を丸めて表示用（例: +5%） */
private fun formatAnnualFromMonthly(rateMonthly: Double): String {
    val pct = (rateMonthly * 12.0) * 100.0
    val df = java.text.DecimalFormat("#.#####") // ← 末尾の0は自動で省略、小数5桁まで表示
    return "+${df.format(pct)}%"
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    current: ForecastSettings,
    onClose: () -> Unit,
    onSave: (ForecastSettings) -> Unit,
    onExportCsv: () -> Unit,     // ← 追加
    onImportCsv: () -> Unit,     // ← 追加
    // テーマはチップを押した瞬間に保存・反映する（「保存」ボタンを待たずに見た目を確認できるように）
    onThemeModeChange: (String) -> Unit = {},
    onSeriesColorsChange: (SeriesColorChoices) -> Unit = {},
    // 定期的な収支（追加・編集・削除はその場で保存）と、予測の最初の月（最新の記録の翌月）
    scheduledExpenses: List<ScheduledExpense> = emptyList(),
    onScheduledExpensesChange: (List<ScheduledExpense>) -> Unit = {},
    forecastStart: YearMonth? = null
) {
    var mode by remember { mutableStateOf(current.mode) } // "avg" or "custom"
    var sampleChoice by remember { mutableStateOf(current.sampleN) } // 10/20/50/-1/カスタム値
    var yearsText by remember { mutableStateOf((current.months / 12).toString()) }
    var drawdownStartYearsText by remember {
        mutableStateOf((current.drawdownStartMonth?.div(12))?.toString() ?: "")
    }
    var withdrawPerMonthText by remember {
        mutableStateOf((current.withdrawPerMonth ?: 0L)
            .takeIf { it != 0L }?.toString() ?: "")
    }
    // 現在値から年率入力を初期化（current.customRatePerMonth は月率小数）
    var customAnnualText by remember {
        mutableStateOf(
            current.customRatePerMonth?.let { monthly ->
                // 月利 → 年利（複利）に正しく戻す
                val annualPct = (monthly * 12.0) * 100.0                   // ← APR
                val df = DecimalFormat("#.#####") // 最大5桁、末尾0を表示しない
                df.format(BigDecimal(annualPct).setScale(5, RoundingMode.HALF_UP))
            } ?: ""
        )
    }
    var lang by remember { mutableStateOf(current.language) }
    var themeMode by remember { mutableStateOf(current.themeMode) }
    var adFree by remember { mutableStateOf(current.adFree) }
    var fixedAddText by remember { mutableStateOf((current.customDeltaPerMonth ?: 0L).takeIf { it != 0L }?.toString() ?: "") }

    // 10/20/50/全件/カスタム の簡易UI：ドロップダウン
    var expanded by remember { mutableStateOf(false) }
    val presetItems = listOf(12, 36, 60, -1) // -1=全件, INT_MIN=カスタム
    fun labelOf(n: Int) = when (n) {
        -1 -> "全て"
        Int.MIN_VALUE -> "カスタム"
        else -> "$n 件"
    }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("アプリ設定") },
        text = {
            val scroll = rememberScrollState()
            val density = LocalDensity.current

            // 簡易版：ダイアログの見える高さを 520dp として扱う
            val viewportPx = with(density) { 520.dp.toPx() }
            // 全体をBoxで包む（上にスクロールバーを重ねるため）
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)   // 好みで調整（端末差で溢れない上限）
                        .verticalScroll(scroll),  // ← 追加：スワイプでスクロール可に
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "将来予測数",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    SettingsField(
                        value = yearsText,
                        onValueChange = { yearsText = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = "予測年数",
                        suffix = "年",
                        supportingText = "0〜100 年"
                    )
                    Text(
                        text = "年率の指定",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
//                Row(
//                    Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    FilterChip(
//                        selected = mode == "custom",
//                        onClick = { mode = "custom" },
//                        label = { Text("年率指定") }
//                    )
//                    FilterChip(
//                        selected = mode == "avg",
//                        onClick = { mode = "avg" },
//                        label = { Text("直近N回平均") }
//                    )
//                }

                    if (mode == "avg") {
                        // ✅ ボタンとメニューを同じ Box に入れて、Box をアンカーにする
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text(labelOf(sampleChoice))
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                // 下方向に出したい場合は offset で微調整（必要なければ省略OK）
                                offset = DpOffset(x = 0.dp, y = 0.dp)
                            ) {
                                presetItems.forEach { v ->
                                    DropdownMenuItem(
                                        text = { Text(labelOf(v)) },
                                        onClick = {
                                            sampleChoice = v
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (sampleChoice == Int.MIN_VALUE) {
                            var customNText by remember {
                                mutableStateOf((current.sampleN.takeIf { it > 0 } ?: 10).toString())
                            }
                            SettingsField(
                                value = customNText,
                                onValueChange = {
                                    customNText = it.filter { ch -> ch.isDigit() }.take(4)
                                },
                                label = "カスタムN",
                                suffix = "件"
                            )
                            sampleChoice = customNText.toIntOrNull() ?: sampleChoice
                        }

                    } else {
                        if (mode == "custom") {
                            SettingsField(
                                value = customAnnualText,
                                onValueChange = { raw ->
                                    // 数字・小数点・マイナス符号だけ許可
                                    var t = raw.filter { it.isDigit() || it == '.' || it == '-' }
                                    if (t.count { it == '.' } > 1) {
                                        t = t.replaceFirst(".", "#").replace(".", "")
                                            .replace("#", ".")
                                    }
                                    if (t.count { it == '-' } > 1) t = t.replace("-", "")
                                    if (t.isNotEmpty() && t.first() != '-') t = t.replace("-", "")
                                    customAnnualText = t.take(7)
                                },
                                label = "年率（マイナス可）",
                                suffix = "%/年",
                                placeholder = "例: 4",
                                keyboardType = KeyboardType.Decimal
                            )
                        }
                    }

                    Text(
                        text = "固定追加金額",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    SettingsField(
                        value = fixedAddText,
                        onValueChange = { t ->
                            // 符号は不要にするなら数字だけ許可
                            fixedAddText = t.filter { it.isDigit() }.take(10)
                        },
                        label = "毎月の追加額",
                        suffix = "円/月",
                        placeholder = "未設定（例: 30000）"
                    )
                    Text(
                        text = "切り崩し設定",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    SettingsField(
                        value = drawdownStartYearsText,
                        onValueChange = {
                            drawdownStartYearsText = it.filter { ch -> ch.isDigit() }.take(3)
                        },
                        label = "開始時期",
                        suffix = "年後から",
                        supportingText = "予測年数未満（例：予測 30 年なら 0〜29）"
                    )

                    SettingsField(
                        value = withdrawPerMonthText,
                        onValueChange = {
                            withdrawPerMonthText = it.filter { it.isDigit() }.take(10)
                        },
                        label = "毎月の切り崩し額",
                        suffix = "円/月",
                        placeholder = "未設定（例: 50000）"
                    )

                    Text(
                        text = "定期的な収支",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    // 予測の最後の月は、入力中の予測年数で判定する（保存前でも「期間外」の表示が合うように）
                    val editingMonths = yearsText.toIntOrNull()?.times(12) ?: current.months
                    ScheduledExpenseSection(
                        items = scheduledExpenses,
                        onChange = onScheduledExpensesChange,
                        forecastStart = forecastStart,
                        forecastEnd = forecastStart?.takeIf { editingMonths > 0 }?.plusMonths(editingMonths - 1L)
                    )

//                Divider()
//
//                Row(
//                    Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        text = "広告を非表示（任意）",
//                        style = MaterialTheme.typography.labelMedium.copy(
//                            textDecoration = TextDecoration.Underline,
//                            fontWeight = FontWeight.Bold
//                        ),
//                        color = MaterialTheme.colorScheme.primary,
//                        textAlign = TextAlign.Center
//                    )
//                    Switch(checked = adFree, onCheckedChange = { adFree = it })
//                }
//                     ～～（既存の設定項目の下あたりに追記）～～
                    Divider()
                    Text(
                        text = "テーマ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = themeMode == "system",
                            onClick = { themeMode = "system"; onThemeModeChange("system") },
                            label = { Text("端末設定") }
                        )
                        FilterChip(
                            selected = themeMode == "light",
                            onClick = { themeMode = "light"; onThemeModeChange("light") },
                            label = { Text("ライト") }
                        )
                        FilterChip(
                            selected = themeMode == "dark",
                            onClick = { themeMode = "dark"; onThemeModeChange("dark") },
                            label = { Text("ダーク") }
                        )
                    }
                    Divider()
                    Text(
                        text = "カラー",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    SeriesColorSettings(
                        choices = current.seriesColorChoices(),
                        onChange = onSeriesColorsChange
                    )
                    Divider()
                    Text(
                        text = "CSV インポート / エクスポート",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onImportCsv,
                            modifier = Modifier.weight(1f)
                        ) { Text("import") }

                        OutlinedButton(
                            onClick = onExportCsv,
                            modifier = Modifier.weight(1f)
                        ) { Text("export") }
                    }
                    // リンク先（ブラウザ等）から戻った時にアプリ起動広告が割り込まないよう、開く前に印を付ける
                    val baseUriHandler = LocalUriHandler.current
                    val uriHandler = remember(baseUriHandler) {
                        object : androidx.compose.ui.platform.UriHandler {
                            override fun openUri(uri: String) {
                                AdsManager.skipNextResumeAppOpen = true
                                baseUriHandler.openUri(uri)
                            }
                        }
                    }
                    val privacyPolicyUrl = "https://journeygirl40.github.io/assetforecast/"
                    // 🔹 プライバシーポリシーリンクを追加
                    Divider()
                    Text(
                        text = "プライバシーポリシー/利用規約",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri(privacyPolicyUrl) }
                            .padding(vertical = 10.dp)
                    ) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ページを開く",
                            // 固定の Color.Blue だとダークテーマの暗い背景で読めないため、テーマのリンク色（ブルー）を使う
                            color = MaterialTheme.colorScheme.secondary,
                            textDecoration = TextDecoration.Underline // 下線付き
                        )
                    }
                    val musicurl = "https://www.tunecore.co.jp/artists/JourneyGirl"
                    // 🔹 音楽サイトへのリンク
                    Divider()
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "JourneyGirl 音楽活動",
                            modifier = Modifier.align(Alignment.Start), // ←この1行を追加
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1段目（Spotify・Apple Music）
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { uriHandler.openUri("https://open.spotify.com/intl-ja/artist/3PqlK4aFXeyyU1tWeJsCQw") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF1DB954
                                    )
                                ) // Spotifyグリーン
                            ) {
                                Text("Spotify")
                            }
                            Button(
                                onClick = { uriHandler.openUri("https://music.apple.com/jp/artist/journeygirl/1820600004") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFFA2D48
                                    )
                                ) // Apple Musicピンク
                            ) {
                                Text("Apple Music")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2段目（Amazon Music・LINE Music）
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { uriHandler.openUri("https://www.amazon.co.jp/music/player/browse/tracks/artist/B0FD7MFV84/popular-songs") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFc0c0c0
                                    )
                                ) // Amazonオレンジ
                            ) {
                                Text(
                                    text = "Amazon Music",
                                    color = Color.Black // ← ここで黒文字に指定
                                )
                            }
                            Button(
                                onClick = { uriHandler.openUri("https://music.line.me/webapp/artist/mi00000000280cfdb6") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF00C300
                                    )
                                ) // LINEグリーン
                            ) {
                                Text("LINE Music")
                            }
                        }
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 20.dp) // ← ★ ここを追加（バーを右にズラす）
                        .fillMaxHeight()
                        .width(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                )

                // サム（動く部分）
                val maxScroll = scroll.maxValue.toFloat().coerceAtLeast(1f)
                val vp = viewportPx.coerceAtLeast(1f)
                val thumbHeightPx = (vp * (vp / (vp + maxScroll)))
                    .coerceAtLeast(with(LocalDensity.current) { 24.dp.toPx() })
                val topPx = (scroll.value / maxScroll) * (vp - thumbHeightPx)

                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset {
                            IntOffset(
                                x = with(density) { 20.dp.roundToPx() }, // 8dp → px
                                y = topPx.toInt()
                            )
                        }
                        .width(3.dp)
                        .height(with(density) { thumbHeightPx.toDp() })
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                )
            }
        },

        confirmButton = {
            TextButton(onClick = {
// 年数を 0..100 に丸め、内部保存は「月数 = 年数 * 12」
                val years = yearsText.toIntOrNull()?.coerceIn(0, 100)
                    ?: (current.months / 12)          // 未入力などは現状の年数を維持
                val months = (years * 12)
                val finalSample = when {
                    mode == "avg" && sampleChoice == -1 -> -1
                    mode == "avg" && sampleChoice == Int.MIN_VALUE -> -1
                    mode == "avg" -> sampleChoice.coerceAtLeast(1)
                    else -> current.sampleN
                }

// ★ 年率%を単利で月率に換算
                val monthlyRate: Double? =
                    if (mode == "custom") customAnnualText.toBigDecimalOrNull()?.let {
                        it.divide(BigDecimal(100), 10, RoundingMode.HALF_UP)   // 年率%→小数
                            .divide(BigDecimal(12), 10, RoundingMode.HALF_UP)    // 月率小数（高精度）
                            .toDouble()
                    } else null

                val fixedAdd = fixedAddText.toLongOrNull()
                val ddStartYears = drawdownStartYearsText.toIntOrNull()
                val ddStartMonth: Int? = ddStartYears?.takeIf { it in 0 until years }?.let { it * 12 }
                val withdrawPerMonth: Long? = withdrawPerMonthText.toLongOrNull()?.takeIf { it > 0L }

                onSave(
                    // current.copy にして、このダイアログに入力欄の無い項目（色など即時保存済みの設定）を消さない
                    current.copy(
                        mode = mode,
                        sampleN = finalSample,
                        months = months,
                        customRatePerMonth = monthlyRate,   // ← 単利換算した値を保存
                        customDeltaPerMonth = fixedAdd,    // ★ ここに保存
                        drawdownStartMonth = ddStartMonth,
                        withdrawPerMonth = withdrawPerMonth,
                        language = lang,
                        themeMode = themeMode,
                        adFree = adFree
                    )
                )
                onClose()
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("閉じる") } }
    )
}
@Composable
fun AssetChartSection(
    records: List<AssetRecord>,
    modifier: Modifier = Modifier,
    forecastMonths: Int = 60,
    sampleN: Int = 10,
    mode: String = "custom",
    customRateMonthly: Double? = null,
    customDeltaPerMonth: Long? = null,      // ★ 追加
    drawdownStartMonth: Int? = null,    // ← デフォルトを付ける
    withdrawPerMonth: Long? = null,     // ← デフォルトを付ける
    scheduledExpenses: List<ScheduledExpense> = emptyList(), // 決まった月の収支（予測線に足し引きする）
    range: DisplayRange = DisplayRange(null, null), // ← ミニマップナビゲーターで選択された表示範囲
    onPointSelected: (epochDays: Int?, isForecast: Boolean) -> Unit = { _, _ -> },
    initialHighlightEpochDays: Int? = null, // ← これを追加
    // 予測一覧に表示している金額（日付→金額）。選択中の金額表示を一覧と1円もずらさないために使う
    forecastAmounts: Map<Int, Long> = emptyMap(),
    // リストで選んだ点（グラフでも選択状態にする。epochDaysがnullなら選択解除）
    highlightRequest: ChartHighlightRequest? = null
) {
    val lastRangeKey = remember { mutableStateOf<List<Any?>?>(null) }
    // 起動直後の「最新の実績を選択」を済ませたか（以降はユーザーの選択を優先し、勝手に戻さない）
    val initialHighlightDone = remember { mutableStateOf(false) }
    // 予測線が実際に描画されたか（データ不足等で描画されない月もあるため、凡例バッジの出し分けに使う）
    var hasForecast by remember { mutableStateOf(false) }
    // グラフで選択中の点。バッジ行の中央に日付・金額を表示するために使う（以前のふきだしMarkerの代わり）
    var selectedPoint by remember { mutableStateOf<SelectedChartPoint?>(null) }
    // リストからの選択を読み取り欄に反映する。グラフのハイライトは、selectedPointの変化で走るupdateで当て直される
    LaunchedEffect(highlightRequest) {
        val req = highlightRequest ?: return@LaunchedEffect
        selectedPoint = req.epochDays?.let { SelectedChartPoint(it.toLong(), req.amount, req.isForecast) }
        if (req.epochDays != null) initialHighlightDone.value = true
    }

    // MPAndroidChart(AndroidView)はComposeのMaterialTheme.colorSchemeを直接参照できないため、
    // ここでARGBに変換してfactory/updateへ渡す。ダーク/ライト切替時もupdateで毎回設定し直すことで追従させる。
    val onSurfaceVariantColorArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    // グリッド線は控えめに見せたいので、入力枠用のoutlineではなく区切り線用のoutlineVariantを使う
    val outlineColorArgb = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f).toArgb()
    // 実績・予測の線の色（初期色はブルー/グリーン。設定の「カラー」で変更できる）
    val actualColor = LocalSeriesColors.current.actual
    val forecastColor = LocalSeriesColors.current.forecast
    val actualColorArgb = actualColor.toArgb()
    val forecastColorArgb = forecastColor.toArgb()

    Column(modifier) {
        // ── 凡例の代わりに「実績」「予測」を表のタイトルのようなバッジでグラフ直上に表示する。
        // MPAndroidChart標準のLegendは行の高さを取るため非表示にし、節約した分をグラフ本体に回す。
        // バッジの間の空きスペースには選択中の点の日付・金額を表示する。左右バッジの幅に
        // 左右されず常に画面中央に来るよう、RowではなくBoxで重ねて配置している。
        Box(
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, bottom = 2.dp)
        ) {
            ChartSeriesBadge(
                text = "実績",
                color = actualColor,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            selectedPoint?.let { p ->
                SelectedPointReadout(
                    point = p,
                    color = if (p.isForecast) forecastColor else actualColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            if (hasForecast) {
                ChartSeriesBadge(
                    text = "予測",
                    color = forecastColor,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        factory = { context ->
            LineChart(context).apply {
                // Compose側の背景（テーマのbackground）を透かすため、チャート自体は透明背景にする
                setBackgroundColor(AndroidColor.TRANSPARENT)
                description.isEnabled = false
                setNoDataText("データがありません")
                setNoDataTextColor(onSurfaceVariantColorArgb)
                setPinchZoom(true)
                setScaleEnabled(true)
                setAutoScaleMinMaxEnabled(true)    // データ範囲に合わせて自動スケール
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(true)
                xAxis.setGranularityEnabled(true)
                legend.isEnabled = false
                setDrawMarkers(true)
                // タップの縦位置は無視し、日付（X）が一番近い点を選ぶ
                setHighlighter(XOnlyHighlighter(this))

                // ▼ 追加：縦軸（Y軸）の金額文字サイズを小さく
                axisLeft.textSize = 8f  // ← デフォルトは 10〜12f 程度。8fなら少し小さめ。
                xAxis.textColor = onSurfaceVariantColorArgb
                axisLeft.textColor = onSurfaceVariantColorArgb
                xAxis.gridColor = outlineColorArgb
                axisLeft.gridColor = outlineColorArgb
            }
        },
        update = { chart ->
            // テーマ切替（ダーク/ライト）にも追従させるため、色は毎回設定し直す
            chart.xAxis.textColor = onSurfaceVariantColorArgb
            chart.axisLeft.textColor = onSurfaceVariantColorArgb
            chart.xAxis.gridColor = outlineColorArgb
            chart.axisLeft.gridColor = outlineColorArgb
            chart.setNoDataTextColor(onSurfaceVariantColorArgb)
            // --- 実績データ（青） ---
            // sorted: 率計算・予測の起点は常に全実績が基準（表示期間フィルタとは独立させる）
            val sorted = records.sortedBy { it.dateEpochDays }
            val displayed = sorted.filter {
                (range.start == null || it.dateEpochDays >= range.start) &&
                    (range.end == null || it.dateEpochDays <= range.end)
            }
            val actualEntries = displayed.map { r ->
                // グラフ用のyはFloatで端数が出るため、表示用に正確な金額をdataに持たせる
                Entry(r.dateEpochDays.toFloat(), r.amount.toFloat(), r.amount)
            }
            val actualSet = LineDataSet(actualEntries, "実績").apply {
                color = actualColorArgb
                lineWidth = 2f
                // 表示範囲に実績が1点しか無いと折れ線は描けない（線は2点以上必要）ため、
                // そのままだと何も見えなくなる。1点のときだけ丸を描いて視認できるようにする。
                // デフォルトは円の中心に穴が開く(circle hole)仕様で、小さい半径だと塗りつぶしに
                // 見えず視認しづらいため、穴なしの塗りつぶし丸にする。
                setDrawCircles(actualEntries.size <= 1)
                setDrawCircleHole(false)
                setCircleColor(actualColorArgb)
                circleRadius = 5f
                setDrawValues(false)
                this.mode = LineDataSet.Mode.LINEAR   // ← ここを this.mode に
                applyAreaFill(actualColorArgb)
            }

            // ★ 月のユニーク数を数える（yyyyMM 単位）
            val monthCount = sorted.groupBy { r ->
                val d = LocalDate.ofEpochDay(r.dateEpochDays.toLong())
                d.year * 100 + d.monthValue
            }.size

            // ミニマップで予測区間だけを選ぶと表示範囲内の実績が0件になる。空のDataSetを混ぜると
            // MPAndroidChartのmin/max計算が崩れるため、実績が無い時は予測線だけで描く。
            val dataSets: MutableList<ILineDataSet> =
                if (actualEntries.isEmpty()) mutableListOf() else mutableListOf(actualSet)
            // 予測線の末尾X座標。range.endがnull（プリセット等で末尾まで表示）の時に
            // X軸の表示上限として使う（下のfitScreen前のaxisMaximum設定で参照）。
            var forecastEndX: Float? = null

// 予測線の作成ブロックだけ差し替え
            if (monthCount >= 1 && forecastMonths > 0) {
                val rate = if (mode == "custom" && customRateMonthly != null)
                    customRateMonthly
                else
                    averageMonthlyRate(sorted, sampleN)

                val monthlyAdd = (customDeltaPerMonth ?: 0L).toDouble()

                val last = sorted.lastOrNull()
                val forecastEntriesRaw =
                    if (last != null) {
                        val startDate = LocalDate.ofEpochDay(last.dateEpochDays.toLong())
                        val list = ArrayList<Entry>(forecastMonths)
                        var amt = last.amount.toDouble()
                        for (m in 1..forecastMonths) {
                            val d = startDate.plusMonths(m.toLong())

                            val fixedThisMonth: Long = when {
                                withdrawPerMonth != null && drawdownStartMonth != null && m >= drawdownStartMonth -> -withdrawPerMonth
                                else -> customDeltaPerMonth ?: 0L
                            }

                            // 率 → 固定額（切り崩しならマイナス）を反映
                            amt = (if (rate != 0.0) amt * (1.0 + rate) else amt) + fixedThisMonth +
                                scheduledExpenses.netFor(d.year, d.monthValue)
                            val epochDay = d.toEpochDay().toInt()
                            list += Entry(epochDay.toFloat(), amt.toFloat(), forecastAmounts[epochDay] ?: amt.roundToLong())
                        }
                        list
                    } else emptyList()
                // 予測線も表示範囲で切る（range.end == null なら末尾まで表示）。
                val forecastEntries = forecastEntriesRaw.filter {
                    (range.start == null || it.x >= range.start.toFloat()) &&
                        (range.end == null || it.x <= range.end.toFloat())
                }
                forecastEndX = forecastEntries.lastOrNull()?.x

                if (forecastEntries.isNotEmpty()) {
                    val forecastSet = LineDataSet(forecastEntries, "予測").apply {
                        color = forecastColorArgb
                        setDrawCircles(false)         // 丸を描かない
                        lineWidth = 2f
                        setDrawValues(false)
                        this.mode = LineDataSet.Mode.LINEAR
                        applyAreaFill(forecastColorArgb)
                    }
                    dataSets += forecastSet
                }
                hasForecast = forecastEntries.isNotEmpty()
            } else {
                // 月が1つしかない等で予測線を描けない場合はバッジも出さない
                hasForecast = false
            }

            // 表示範囲（ミニマップのズーム等）に実績も予測も1件も入らない場合、
            // 空のLineDataを渡すとグラフが何も描かれないまま残り「消えた」ように見えてしまう。
            // その場合はdataをnullにしてsetNoDataTextのメッセージを出す。
            chart.data = if (dataSets.isEmpty()) null else LineData(dataSets)
            chart.invalidate()
            // 値選択リスナーを設定（ここがポイント）
            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    // x は epochDay を float にしたもの
                    val epochDays = e?.x?.toLong()?.toInt()
                    // 実績が範囲外だと予測線がindex 0になるため、indexではなくラベルで判定する
                    val isForecast = h?.let { chart.data?.getDataSetByIndex(it.dataSetIndex)?.label } == "予測"
                    selectedPoint = e?.let {
                        SelectedChartPoint(it.x.toLong(), (it.data as? Long) ?: it.y.roundToLong(), isForecast)
                    }
                    onPointSelected(epochDays, isForecast)
                }
                override fun onNothingSelected() {
                    selectedPoint = null
                    onPointSelected(null, false)
                }
            })
            // X軸：日付表示（yy/MM）
            val fmt = DateTimeFormatter.ofPattern("yy/MM")
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val d = LocalDate.ofEpochDay(value.toLong())
                    return d.format(fmt)
                }
            }
            // Y軸：金額
            chart.axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "¥" + formatYen(value.toLong())
                }
            }
// 上下左右に少し余白を足す（右を多めにして最後の点が切れないように）
            chart.setExtraOffsets(0f, 0f, 0f, 0f)

// Y軸: 上側に余白（%）を追加して上端の点が切れないように
            chart.axisLeft.spaceTop = 25f   // 10〜20 の範囲でお好み
// ▼ 選択中の点（グラフのタップ・リストのタップどちらでも）をハイライトし直す。
            // dataを作り直すとハイライトが消えるため、updateのたびに現在の選択を当て直す（リスナーは呼ばない）。
            // まだ何も選ばれていない起動直後だけ、最新の実績を選択状態にする（表示範囲内にある時だけ）。
            val current = selectedPoint
            if (current != null) {
                if (!highlightChartPoint(chart, current)) chart.highlightValue(null, false)
            } else if (!initialHighlightDone.value) {
                initialHighlightEpochDays?.let { epoch ->
                    if (displayed.any { it.dateEpochDays == epoch }) {
                        chart.highlightValue(epoch.toFloat(), 0, true)
                        initialHighlightDone.value = true
                    }
                }
            } else {
                chart.highlightValue(null, false)
            }
            // 表示範囲（ミニマップナビゲーターの選択）が変わった時だけビューをリセットする。
            // 無関係な再コンポジションで毎回呼ぶと、ユーザーのピンチズーム/パン操作が意図せずリセットされてしまう。
            // 範囲がnull（端まで）の時は、予測期間の変更などでデータの端が動いても軸を合わせ直す必要があるため、
            // 実際に使う軸の端もキーに含める
            val rangeKey = listOf(range.start, range.end, sorted.firstOrNull()?.dateEpochDays, forecastEndX)
            if (lastRangeKey.value != rangeKey) {
                // fitScreen()はxAxisのaxisMinimum/axisMaximumに対してズーム・パンの変換行列を
                // リセットするだけだが、axisMinimum/axisMaximumを明示設定していないとMPAndroidChartは
                // 「現在のdataSetの実際のデータ範囲」から自動計算してしまう。これだと、range指定で
                // 広い期間を要求していても、その範囲内の実績が疎（例:1点だけ）だとグラフがその1点
                // 付近だけに縮んで表示され、「データが消えた」ように見えるバグになる。
                // range（ユーザーが選んだ表示期間）をX軸の範囲として明示的に設定してからリセットする。
                val xMin = range.start?.toFloat() ?: sorted.firstOrNull()?.dateEpochDays?.toFloat()
                val xMax = range.end?.toFloat() ?: forecastEndX ?: sorted.lastOrNull()?.dateEpochDays?.toFloat()
                if (xMin != null && xMax != null && xMax > xMin) {
                    chart.xAxis.axisMinimum = xMin
                    chart.xAxis.axisMaximum = xMax
                } else {
                    chart.xAxis.resetAxisMinimum()
                    chart.xAxis.resetAxisMaximum()
                }
                chart.fitScreen()
                lastRangeKey.value = rangeKey
            }
// （任意）凡例が近い場合の回避
            chart.invalidate()
        }
    )
    }
}

// 「実績」「予測」の凡例バッジ。表のタイトルのような、色付き枠線＋同色文字の小さなラベル。
@Composable
private fun ChartSeriesBadge(text: String, color: ComposeColor, modifier: Modifier = Modifier) {
    Box(
        modifier
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

// 差分（増減額）の文字色。初期色は日本の慣習どおりプラス＝赤、マイナス＝青で、設定の「カラー」で変更できる。
// ダーク/ライトの出し分けはAssetforecastTheme側（LocalSeriesColors）で済んでいる。±0はグレー。
@Composable
private fun diffColor(diff: Long): ComposeColor {
    val colors = LocalSeriesColors.current
    return when {
        diff > 0 -> colors.gain
        diff < 0 -> colors.loss
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }
}

// 表示期間内の実績から「差分平均（円/回）」と「期間増減率」を求めた結果
private data class RangeStats(val fromEpochDays: Int, val toEpochDays: Int, val avgDiff: Long, val changeRate: Double)

// 一覧の「差分」列（前回記録との差）と同じ基準にするため、期間直前の記録を起点（base）とする。
// こうすると avgDiff＝期間内の差分列の平均、changeRate＝base→期間末の変化率 と一致する。
// 期間の先頭が全記録の先頭なら直前の記録が無いので、期間内の最初の記録を起点にする。
private fun computeRangeStats(records: List<AssetRecord>, range: DisplayRange): RangeStats? {
    val sorted = records.sortedBy { it.dateEpochDays }
    val inRange = sorted.filter {
        (range.start == null || it.dateEpochDays >= range.start) &&
            (range.end == null || it.dateEpochDays <= range.end)
    }
    val first = inRange.firstOrNull() ?: return null
    val last = inRange.last()
    val prev = sorted.lastOrNull { it.dateEpochDays < first.dateEpochDays }
    val base = prev ?: first
    val steps = if (prev != null) inRange.size else inRange.size - 1
    if (steps <= 0 || base.amount == 0L) return null
    return RangeStats(
        fromEpochDays = first.dateEpochDays,
        toEpochDays = last.dateEpochDays,
        avgDiff = (last.amount - base.amount) / steps,
        changeRate = (last.amount - base.amount).toDouble() / base.amount
    )
}

@Composable
private fun RangeStatsStrip(records: List<AssetRecord>, range: DisplayRange, modifier: Modifier = Modifier) {
    val stats = remember(records, range) { computeRangeStats(records, range) } ?: return
    val ym = DateTimeFormatter.ofPattern("yyyy/MM")
    val numberFont = com.journeygirl.assetforecast.ui.theme.NumberFontFamily
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall
    Row(
        modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = LocalDate.ofEpochDay(stats.fromEpochDays.toLong()).format(ym) + "〜" +
                LocalDate.ofEpochDay(stats.toEpochDays.toLong()).format(ym),
            style = labelStyle, color = muted, fontFamily = numberFont, maxLines = 1
        )
        Spacer(Modifier.width(10.dp))
        Text("差分平均", style = labelStyle, color = muted, maxLines = 1)
        Spacer(Modifier.width(4.dp))
        Text(
            text = formatYenSigned(stats.avgDiff),
            style = labelStyle, color = diffColor(stats.avgDiff), fontFamily = numberFont, maxLines = 1
        )
        Spacer(Modifier.width(10.dp))
        Text("増減率", style = labelStyle, color = muted, maxLines = 1)
        Spacer(Modifier.width(4.dp))
        val pct = stats.changeRate * 100.0
        Text(
            text = (if (pct > 0) "+" else "") + String.format(java.util.Locale.US, "%.1f%%", pct),
            style = labelStyle,
            // 色は符号で決める（小数1桁で0.0%に丸まる微小な増減でも向きが分かるように）
            color = diffColor(if (pct > 0) 1 else if (pct < 0) -1 else 0),
            fontFamily = numberFont, maxLines = 1
        )
    }
}

// グラフで選択中の点（x=epochDays, y=金額）。予測線上の点かどうかで表示色を変える。
// MPAndroidChart標準の選択は、実績線・予測線それぞれの最寄り点のうち「タップの縦位置に近い方」を選ぶため、
// 線から離れた所を押すと隣の線（例：予測区間なのに最新の実績）が選ばれてしまう。X（日付）の近さだけで選ぶ。
private class XOnlyHighlighter(chart: LineChart) :
    com.github.mikephil.charting.highlight.ChartHighlighter<LineChart>(chart) {
    override fun getHighlight(x: Float, y: Float): Highlight? {
        val pos = getValsForTouch(x, y)
        val xVal = pos.x.toFloat()
        MPPointD.recycleInstance(pos)
        return getHighlightsAtXValue(xVal, x, y).minByOrNull { kotlin.math.abs(it.x - xVal) }
    }
}

// リストで選んだ点をグラフに伝えるための依頼。seqは同じ点を選び直した時も反映させるための連番
data class ChartHighlightRequest(val epochDays: Int?, val amount: Long, val isForecast: Boolean, val seq: Long)

// 選択中の点をグラフ上でハイライトする（実績/予測はデータセットのラベルで探す）。その点が今のデータに無ければfalse
private fun highlightChartPoint(chart: LineChart, p: SelectedChartPoint): Boolean {
    val data = chart.data ?: return false
    val label = if (p.isForecast) "予測" else "実績"
    val index = (0 until data.dataSetCount).firstOrNull { data.getDataSetByIndex(it).label == label } ?: return false
    val x = p.epochDays.toFloat()
    if (data.getDataSetByIndex(index).getEntriesForXValue(x).isEmpty()) return false
    chart.highlightValue(x, index, false)
    return true
}

private data class SelectedChartPoint(val epochDays: Long, val amount: Long, val isForecast: Boolean)

// バッジ行の中央に出す選択中の点の読み取り表示。証券アプリの価格表示風に、
// 日付は控えめな小さい文字、金額は系列色の太い等幅数字にして桁が揃って見えるようにする。
@Composable
private fun SelectedPointReadout(point: SelectedChartPoint, color: ComposeColor, modifier: Modifier = Modifier) {
    val date = LocalDate.ofEpochDay(point.epochDays).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    // すぐ上のミニマップの集計行（期間・差分平均・増減率）と混ざって見えないよう、
    // 系列色の淡い塗り＋枠のカプセルで囲み、金額も一回り大きくして「今選んでいる値」だと分かるようにする。
    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier
            .background(color.copy(alpha = 0.12f), shape)
            .border(1.dp, color.copy(alpha = 0.35f), shape)
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = date,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = com.journeygirl.assetforecast.ui.theme.NumberFontFamily,
            modifier = Modifier.alignByBaseline()
        )
        Text(
            text = "¥" + formatYen(point.amount),
            color = color,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
            fontFamily = com.journeygirl.assetforecast.ui.theme.NumberFontFamily,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.alignByBaseline()
        )
    }
}

// グラフ下に全期間（実績＋予測）の簡易推移を描き、現在の表示範囲（DisplayRange）をハイライト枠で示すナビゲーター。
// ハイライト枠の中央をドラッグすると範囲全体をシフト、左右端のハンドルをドラッグすると個別にリサイズできる。
// 枠が全期間の端に達した側はDisplayRangeのstart/endをnullにして、AssetChartSection側の「予測線は末尾まで表示」挙動を保つ。
@Composable
private fun MiniMapNavigator(
    records: List<AssetRecord>,
    forecasts: List<ForecastPoint>,
    range: DisplayRange,
    onRangeChange: (DisplayRange) -> Unit,
    selectedEpochDays: Int? = null,
    modifier: Modifier = Modifier
) {
    val sorted = remember(records) { records.sortedBy { it.dateEpochDays } }
    val fullStart = sorted.firstOrNull()?.dateEpochDays
    // 右端は予測の末尾まで広げる（予測が無い時は実績の末尾）
    val fullEnd = forecasts.lastOrNull()?.epochDays ?: sorted.lastOrNull()?.dateEpochDays

    if (fullStart == null || fullEnd == null || fullEnd <= fullStart) {
        return
    }

    val density = LocalDensity.current
    // 背景の簡易推移線はメイングラフの実績線・予測線と同じ色で統一する
    val backgroundLineColor = LocalSeriesColors.current.actual
    val forecastLineColor = LocalSeriesColors.current.forecast
    var widthPx by remember { mutableStateOf(0f) }
    val fullSpan = (fullEnd - fullStart).toFloat()
    // ミニマップは全期間（数年）を狭い横幅に凝縮しているため、わずかな指の移動でも
    // 大きな日数変化になる。記録は月次が基本なので、下限を短くしすぎると実績データが
    // 1件も入らない範囲までズームでき、グラフが実質空になって「消えた」ように見えてしまう。
    // 必ず数ヶ月分のデータが入る粒度に制限する。
    val minRangeDays = 90

    val highlightStart = (range.start ?: fullStart).coerceIn(fullStart, fullEnd)
    val highlightEnd = (range.end ?: fullEnd).coerceIn(highlightStart, fullEnd)

    // detectDragGestures のコールバック内から常に最新のハイライト範囲／コールバックを参照するため
    val currentHighlight by rememberUpdatedState(highlightStart to highlightEnd)
    val currentOnRangeChange by rememberUpdatedState(onRangeChange)

    // 期間が長い（予測50年など）と1pxが数十日になり、指でぴったり端まで寄せるのが難しい。
    // ハンドル操作では端から少し手前まで来たら端に吸着させる。
    fun emitRange(newStart: Int, newEnd: Int, snapToEdges: Boolean = false) {
        val snapDays = if (snapToEdges && widthPx > 0f) (with(density) { 12.dp.toPx() } / widthPx * fullSpan).roundToInt() else 0
        currentOnRangeChange(
            DisplayRange(
                start = if (newStart <= fullStart + snapDays) null else newStart,
                end = if (newEnd >= fullEnd - snapDays) null else newEnd
            )
        )
    }

    // 縦軸は実績・予測を合わせた最小〜最大で正規化する（切り崩しで予測が実績を下回る場合もあるため両方見る）
    val minAmount = minOf(sorted.minOf { it.amount }, forecasts.minOfOrNull { it.amount } ?: Long.MAX_VALUE)
    val maxAmount = maxOf(sorted.maxOf { it.amount }, forecasts.maxOfOrNull { it.amount } ?: Long.MIN_VALUE)
    val amountSpan = (maxAmount - minAmount).coerceAtLeast(1)

    val handleWidthDp = 40.dp
    val handleWidthPx = with(density) { handleWidthDp.toPx() }

    // records（＝全期間の範囲）が変わった時だけジェスチャー検出コルーチンを作り直す。
    // widthPxをキーに含めると、ドラッグ中のレイアウト再計算で値がわずかに変動するたびに
    // コルーチンが再構築されてしまい、ドラッグが実質的に効かなくなる（widthPx自体は
    // varなのでブロック内で都度読み直せば最新値が得られ、キーにする必要はない）。
    val gestureKey = fullStart to fullEnd

    Box(
        modifier
            .height(56.dp)
            .onSizeChanged { widthPx = it.width.toFloat() }
    ) {
        // 背景：全期間の簡易推移（薄い色の折れ線。実績＝ブルー、予測＝グリーン）
        Canvas(Modifier.fillMaxSize()) {
            if (size.width > 0f) {
                fun xOf(epochDays: Int) = size.width * (epochDays - fullStart) / fullSpan
                fun yOf(amount: Long) = size.height * (1f - (amount - minAmount).toFloat() / amountSpan)
                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                if (sorted.size >= 2) {
                    val path = androidx.compose.ui.graphics.Path()
                    sorted.forEachIndexed { i, r ->
                        if (i == 0) path.moveTo(xOf(r.dateEpochDays), yOf(r.amount))
                        else path.lineTo(xOf(r.dateEpochDays), yOf(r.amount))
                    }
                    drawPath(path = path, color = backgroundLineColor.copy(alpha = 0.5f), style = stroke)
                }
                if (forecasts.isNotEmpty()) {
                    // 実績の最終点から繋げて描き、実績線と予測線の間に切れ目が出ないようにする
                    val last = sorted.last()
                    val path = androidx.compose.ui.graphics.Path()
                    path.moveTo(xOf(last.dateEpochDays), yOf(last.amount))
                    forecasts.forEach { f -> path.lineTo(xOf(f.epochDays), yOf(f.amount)) }
                    drawPath(path = path, color = forecastLineColor.copy(alpha = 0.5f), style = stroke)
                }
            }
        }

        if (widthPx > 0f) {
            val startPx = widthPx * (highlightStart - fullStart) / fullSpan
            val endPx = widthPx * (highlightEnd - fullStart) / fullSpan

            // ハイライト枠（中央をドラッグすると範囲全体をシフト）
            Box(
                Modifier
                    .offset { IntOffset(startPx.roundToInt(), 0) }
                    .width(with(density) { (endPx - startPx).coerceAtLeast(0f).toDp() })
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .border(1.dp, MaterialTheme.colorScheme.primary)
                    .pointerInput(gestureKey) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (widthPx <= 0f) return@detectDragGestures
                            val (hs, he) = currentHighlight
                            val deltaEpoch = (dragAmount.x / widthPx * fullSpan).roundToInt()
                            val width = he - hs
                            var newStart = hs + deltaEpoch
                            var newEnd = he + deltaEpoch
                            if (newStart < fullStart) {
                                newStart = fullStart
                                newEnd = fullStart + width
                            }
                            if (newEnd > fullEnd) {
                                newEnd = fullEnd
                                newStart = fullEnd - width
                            }
                            emitRange(newStart, newEnd)
                        }
                    }
            )

            // 左ハンドル（開始位置のリサイズ）
            Box(
                Modifier
                    .offset { IntOffset((startPx - handleWidthPx / 2).roundToInt(), 0) }
                    .width(handleWidthDp)
                    .fillMaxHeight()
                    .pointerInput(gestureKey) {
                        // 端への吸着があるため、1回ごとの移動量ではなくドラッグ開始位置からの累計で位置を決める
                        // （毎回の小さな移動量だと吸着範囲から抜け出せなくなる）
                        var anchorStart = 0
                        var totalDx = 0f
                        detectDragGestures(
                            onDragStart = { anchorStart = currentHighlight.first; totalDx = 0f }
                        ) { change, dragAmount ->
                            change.consume()
                            if (widthPx <= 0f) return@detectDragGestures
                            val he = currentHighlight.second
                            totalDx += dragAmount.x
                            val deltaEpoch = (totalDx / widthPx * fullSpan).roundToInt()
                            // he-minRangeDaysがfullStartを下回り得る（heがfullStartに極めて近い場合）ため、
                            // coerceInの上限がfullStart未満にならないようクランプしてから使う
                            val upperBound = (he - minRangeDays).coerceAtLeast(fullStart)
                            val newStart = (anchorStart + deltaEpoch).coerceIn(fullStart, upperBound)
                            emitRange(newStart, he, snapToEdges = true)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight(0.6f)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
            }

            // 右ハンドル（終了位置のリサイズ）
            Box(
                Modifier
                    .offset { IntOffset((endPx - handleWidthPx / 2).roundToInt(), 0) }
                    .width(handleWidthDp)
                    .fillMaxHeight()
                    .pointerInput(gestureKey) {
                        var anchorEnd = 0
                        var totalDx = 0f
                        detectDragGestures(
                            onDragStart = { anchorEnd = currentHighlight.second; totalDx = 0f }
                        ) { change, dragAmount ->
                            change.consume()
                            if (widthPx <= 0f) return@detectDragGestures
                            val hs = currentHighlight.first
                            totalDx += dragAmount.x
                            val deltaEpoch = (totalDx / widthPx * fullSpan).roundToInt()
                            // hs+minRangeDaysがfullEndを上回り得る（hsがfullEndに極めて近い場合）ため、
                            // coerceInの下限がfullEndを超えないようクランプしてから使う
                            val lowerBound = (hs + minRangeDays).coerceAtMost(fullEnd)
                            val newEnd = (anchorEnd + deltaEpoch).coerceIn(lowerBound, fullEnd)
                            emitRange(hs, newEnd, snapToEdges = true)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight(0.6f)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
            }

            // グラフ（またはリスト）で選択中の記録の位置を、ミニマップ上に縦線で示す。
            // ハイライト枠・ハンドルの後に描画することで最前面に重ね、ドラッグ操作とは独立した
            // ただの目印（pointerInputを持たないのでタッチは奪わない）として扱う。
            if (selectedEpochDays != null && selectedEpochDays in fullStart..fullEnd) {
                val selectedPx = widthPx * (selectedEpochDays - fullStart) / fullSpan
                val markerWidth = 2.dp
                Box(
                    Modifier
                        .offset { IntOffset((selectedPx - with(density) { markerWidth.toPx() } / 2).roundToInt(), 0) }
                        .width(markerWidth)
                        .fillMaxHeight()
                        .background(ComposeColor.Red.copy(alpha = 0.8f))
                )
            }
        }
    }
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    // いまどのプロバイダを表示しようとしているか
    var provider by remember {
        mutableStateOf(com.journeygirl.assetforecast.ads.AdsManager.currentBannerProvider())
    }

    when (provider) {
        // ① AdMob を先に試す
        com.journeygirl.assetforecast.ads.AdProvider.ADMOB -> {
            AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    AdView(ctx).apply {
                        // 画面幅からアダプティブサイズを一度だけ設定
                        val density = ctx.resources.displayMetrics.density
                        val adWidthDp = (ctx.resources.displayMetrics.widthPixels / density).toInt()
                        val size = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidthDp)

                        adUnitId = com.journeygirl.assetforecast.ads.AdsManager.BANNER_ID
                        setAdSize(size)

                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                com.journeygirl.assetforecast.ads.AdsManager
                                    .noteBannerSuccess(com.journeygirl.assetforecast.ads.AdProvider.ADMOB)
                            }
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                // 失敗 → Unity へ切替
                                com.journeygirl.assetforecast.ads.AdsManager.rotateBannerProviderOnFailure()
                                provider = com.journeygirl.assetforecast.ads.AdsManager.currentBannerProvider()
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                update = { /* no-op（サイズ再設定や無限リロードはしない） */ }
            )
        }

        // ② Unity Ads を試す（AdMobが失敗した場合に来る）
        com.journeygirl.assetforecast.ads.AdProvider.UNITY -> {
// UNITY 分岐（修正版：旧 BannerView API、IDはAdsManagerから参照）
            AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    val banner = com.unity3d.services.banners.BannerView(
                        ctx as android.app.Activity,
                        com.journeygirl.assetforecast.ads.AdsManager.UNITY_BANNER_PLACEMENT_ID, // ← 集約された定数を参照
                        com.unity3d.services.banners.UnityBannerSize(320, 50) // Unityは固定サイズのみ
                    )
                    banner.listener = object : com.unity3d.services.banners.BannerView.IListener {
                        override fun onBannerLoaded(view: com.unity3d.services.banners.BannerView) {
                            com.journeygirl.assetforecast.ads.AdsManager
                                .noteBannerSuccess(com.journeygirl.assetforecast.ads.AdProvider.UNITY)
                        }
                        override fun onBannerShown(view: com.unity3d.services.banners.BannerView) { }
                        override fun onBannerFailedToLoad(
                            view: com.unity3d.services.banners.BannerView,
                            errorInfo: com.unity3d.services.banners.BannerErrorInfo
                        ) {
                            // 失敗時のフォールバック（AdMobに戻したいときだけ回す）
                            com.journeygirl.assetforecast.ads.AdsManager.rotateBannerProviderOnFailure()
                        }
                        override fun onBannerClick(view: com.unity3d.services.banners.BannerView) { }
                        override fun onBannerLeftApplication(view: com.unity3d.services.banners.BannerView) { }
                    }
                    banner.load()
                    banner
                },
                update = { /* no-op */ }
            )

        }
    }
}



@Composable
private fun RecordRow(
    r: AssetRecord,
    selected: Boolean = false,
    diff: Long,                          // ★ 必須
    onClick: () -> Unit
) {
    val date = LocalDate.ofEpochDay(r.dateEpochDays.toLong())
    val basePaddingV = 2.dp  // ▼ 予測側と同じ 2.dp に統一

    // 選択時のハイライト（ダーク/ライトどちらでも浮かないようテーマのprimaryをベースにする）
    val targetColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else ComposeColor.Transparent
    val bg by animateColorAsState(targetValue = targetColor, label = "rowBg")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = basePaddingV)
            .heightIn(min = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1列目：年月日
        Text(
            text = date.format(DATE_FMT),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        // 2列目：備考を表示
        Text(
            text = r.note?.takeIf { it.isNotBlank() } ?: "—",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        // 3列目：差分
        Text(
            text = formatYenSigned(diff),                 // 例: +¥10,000 / -¥5,000 / ±¥0
            style = MaterialTheme.typography.bodySmall,
            color = diffColor(diff),
            maxLines = 1,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        // 4列目：金額
        Text(
            text = "¥" + formatYen(r.amount),                   // 3桁区切り
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
    )
}


// === ここは RecordRow のすぐ下に置く ===
@Composable
private fun ForecastRow(
    p: ForecastPoint,
    diff: Long,
    addFixed: Long,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val date = LocalDate.ofEpochDay(p.epochDays.toLong())
    val grey = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    // 選択時のハイライトは実績行と同じ色にそろえる
    val targetColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else ComposeColor.Transparent
    val bg by animateColorAsState(targetValue = targetColor, label = "forecastRowBg")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date.format(DATE_FMT),
            style = MaterialTheme.typography.bodyMedium,
            color = grey,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        // 追加金額（中央寄せ）
        Text(
            text = "¥" + formatYen(p.fixedAdd),
            style = MaterialTheme.typography.bodySmall,
            color = grey,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        // 差分（中央寄せ）
        Text(
            text = formatYenSigned(diff),
            style = MaterialTheme.typography.bodySmall,
            color = diffColor(diff),
            maxLines = 1,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        // 資産額（右寄せ）
        Text(
            text = "¥" + formatYen(p.amount),
            style = MaterialTheme.typography.bodyMedium,
            color = grey,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )

    }
    // 定期的な収支がある月だけ、その内訳を行の下に小さく出す（差分が急に変わった理由が分かるように）。
    if (p.scheduledItems.isNotEmpty()) {
        val seriesColors = LocalSeriesColors.current
        // 1件ずつ小さなラベルにして横に並べ、入りきらなければ折り返す（件数が増えても縦に伸びすぎないように）。
        // ラベルはそれぞれの符号に合わせた設定の増減色でうっすら塗り、金額の文字も同じ色にする。
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            p.scheduledItems.forEach { (name, signed) ->
                val c = if (signed >= 0) seriesColors.gain else seriesColors.loss
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(c.copy(alpha = 0.12f))
                        .border(0.5.dp, c.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 96.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = formatScheduledYen(signed),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = c,
                        maxLines = 1
                    )
                }
            }
        }
    }
    }
    HorizontalDivider(thickness = 0.5.dp, color = grey.copy(alpha = 0.4f))
}

// グラフの表示期間プリセット（監視ダッシュボード風のタイムレンジセレクタ用）
// グラフに表示するX軸範囲（epochDays）。start/endがnullなら無制限。
// ミニマップナビゲーターのハイライト枠が全期間の端に達している側はnull（予測線は末尾まで表示）。
// AssetChartSection（public）のパラメータ型として使うため可視性はpublicにする。
data class DisplayRange(val start: Int?, val end: Int?)

// 表示範囲を全期間[fullStart, fullEnd]に収める。端に達した側はnull（＝端まで）にし、
// 範囲が全期間の外に出てしまった時は全期間表示に戻す。
private fun normalizeDisplayRange(range: DisplayRange?, fullStart: Int?, fullEnd: Int?): DisplayRange {
    if (range == null || fullStart == null || fullEnd == null || fullEnd <= fullStart) return DisplayRange(null, null)
    val start = range.start?.takeIf { it > fullStart }
    val end = range.end?.takeIf { it < fullEnd }
    if ((start ?: fullStart) >= (end ?: fullEnd)) return DisplayRange(null, null)
    return DisplayRange(start, end)
}

// 月ごとの平均資産額 → 月次増減率の平均を返す（直近 sampleMonths 分）
// sampleMonths <= 0 なら全期間
private fun averageMonthlyRate(records: List<AssetRecord>, sampleMonths: Int): Double {
    if (records.isEmpty()) return 0.0

    // 年月キー（例: 2025-10 → 202510）でグルーピング
    val monthlyAvgs: List<Pair<Int, Double>> =
        records.groupBy { r ->
            val d = LocalDate.ofEpochDay(r.dateEpochDays.toLong())
            d.year * 100 + d.monthValue
        }.map { (ym, list) ->
            ym to list.map { it.amount.toDouble() }.average()
        }.sortedBy { it.first }

    if (monthlyAvgs.size < 2) return 0.0 // 前月比が作れない

    // 月次の前月比（率）
    val rates = monthlyAvgs.zipWithNext().map { (prev, cur) ->
        val prevYm = prev.first  // 例: 202510
        val curYm  = cur.first
        val prevAvg = prev.second
        val curAvg  = cur.second

        // 年月差 → Δmonths を計算
        val prevYear = prevYm / 100
        val prevMonth = prevYm % 100
        val curYear = curYm / 100
        val curMonth = curYm % 100
        val deltaMonths = (curYear - prevYear) * 12 + (curMonth - prevMonth)
        if (prevAvg > 0.0 && deltaMonths > 0) {
            // （cur/prev）をΔmonthsで月次複利に分解
            (curAvg / prevAvg).pow(1.0 / deltaMonths) - 1.0
        } else 0.0
    }
    if (rates.isEmpty()) return 0.0

    val use = if (sampleMonths <= 0) rates.size else minOf(sampleMonths, rates.size)
    return rates.takeLast(use).average()
}
// 予測1行分
private data class ForecastPoint(
    val epochDays: Int,
    val amount: Long,
    val diff: Long,       // 前月との差分（予測）
    val fixedAdd: Long,    // その月に足した固定額
    val addFixed: Long = 0L,        // ★ 追加：その月に加算した固定額
    val scheduled: Long = 0L,        // その月に足し引きした定期的な収支の合計（支出はマイナス）
    val scheduledItems: List<Pair<String, Long>> = emptyList() // その内訳（名前と符号付きの金額。一覧に小さく表示する）
)
// 直近Nか月の平均“率”を使って、将来monthsか月ぶんの予測点を作る
private fun buildForecastPoints(
    records: List<AssetRecord>,
    months: Int,
    sampleN: Int,
    mode: String,
    customRateMonthly: Double?,
    customDeltaPerMonth: Long?,      // ★ 追加
    drawdownStartMonth: Int?,          // 予測開始からの経過“月”ではなく“年単位→月換算済み”
    withdrawPerMonth: Long?,           // 切り崩し（円/月）
    fixedAddPerMonth: Long = 0L,     // ★ 追加：毎月の固定加算額（0 なら加算なし）
    expenses: List<ScheduledExpense> = emptyList() // 決まった月の収支（ローン、ボーナスなど）

): List<ForecastPoint> {
    if (records.isEmpty() || months <= 0) return emptyList()

    // ユニーク月数が1なら予測なし
    val monthCount = records.groupBy { r ->
        val d = LocalDate.ofEpochDay(r.dateEpochDays.toLong())
        d.year * 100 + d.monthValue
    }.size
    if (monthCount < 2 && !(mode == "custom" && customRateMonthly != null)) return emptyList()

    // ← “custom” なら指定の月率、そうでなければ平均月率
    val rate = if (mode == "custom" && customRateMonthly != null)
        customRateMonthly
    else
        averageMonthlyRate(records, sampleN)
    val monthlyAdd = (customDeltaPerMonth ?: 0L)

    val last = records.maxByOrNull { it.dateEpochDays } ?: return emptyList()
    val startDate = LocalDate.ofEpochDay(last.dateEpochDays.toLong())

    var prevAmount = BigDecimal.valueOf(last.amount.toDouble())
// APR 前提：保存されている月利 (customRateMonthly) は「年率/12」由来
// → 年率(%) に一度戻してから、APRの式で高精度に月利へ復元する
    val annualAprPct = BigDecimal(customRateMonthly?.let { it * 1200.0 }?.toString() ?: "0")
    val annualAprPctRounded = annualAprPct.setScale(5, RoundingMode.HALF_UP) // ← 5桁精度
    val rateBD = annualAprPctRounded
        .divide(BigDecimal("1200"), 30, RoundingMode.HALF_UP)  // 月利 = 年率(%) / 1200 を高精度で生成
    val out = ArrayList<ForecastPoint>(months)

    for (m in 1..months) {
        val d = startDate.plusMonths(m.toLong())

        val fixedThisMonth: Long = when {
            withdrawPerMonth != null && drawdownStartMonth != null && m >= drawdownStartMonth -> -withdrawPerMonth
            else -> customDeltaPerMonth ?: 0L
        }

        // 複利計算（BigDecimal）
        val afterRate = if (rate != 0.0)
            prevAmount.multiply(BigDecimal.ONE.add(rateBD))
        else
            prevAmount

        // 定期的な収支（その年月に該当するものの合計。支出はマイナス）を足し引きする
        val applied = expenses.filter { it.appliesTo(d.year, d.monthValue) }
        val scheduledThisMonth = applied.sumOf { it.signedAmount }

        val nextAmountBD = afterRate.add(BigDecimal.valueOf(fixedThisMonth.toDouble()))
            .add(BigDecimal.valueOf(scheduledThisMonth))

        // 小数点以下を四捨五入してLong化
        val nextAmount = nextAmountBD.setScale(0, RoundingMode.HALF_UP).toLong()

        out += ForecastPoint(
            epochDays = d.toEpochDay().toInt(),
            amount = nextAmount,
            diff = nextAmount - prevAmount.setScale(0, RoundingMode.HALF_UP).toLong(),
            fixedAdd = fixedThisMonth,
            addFixed = fixedAddPerMonth,
            scheduled = scheduledThisMonth,
            scheduledItems = applied.map { it.displayName() to it.signedAmount }
        )

        prevAmount = nextAmountBD
    }
    return out
}


private fun formatMonthlyRateLabel(rate: Double): String {
    // rateは 0.0123 = 1.23%/月 のような小数
    val pct = (rate * 100.0).roundToInt()
    val sign = if (pct > 0) "+" else ""
    return "月平均$sign$pct%"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        trailingIcon = { TextButton(onClick = { showPicker = true }) { Text("📅") } },
        modifier = Modifier.fillMaxWidth(),
        colors = appTextFieldColors()
    )

    if (showPicker) {
        val initial = parseDateOrNull(value) ?: LocalDate.now()
        // Compose からプラットフォームダイアログを一度だけ表示
        DisposableEffect(Unit) {
            val dlg = android.app.DatePickerDialog(
                // スピナーを強制したい場合は “theme” を後述のスタイルに差し替える
                context,
                { _, y, m, d ->
                    val picked = LocalDate.of(y, m + 1, d) // DatePicker は 0 始まりの月
                    onValueChange(picked.format(DATE_FMT))
                },
                initial.year,
                initial.monthValue - 1,
                initial.dayOfMonth
            )
            // 既存の“減光しっぱなし”対策フックを流用（あなたの拡張）
            dlg.setOnShowListener { dlg.installAntiStuckDim() }
            dlg.setOnDismissListener { showPicker = false }
            dlg.show()

            onDispose { dlg.dismiss() }
        }
    }
}


@Composable
private fun ListHeader() {
    Surface(tonalElevation = 2.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "年月日",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text("備考",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "差分",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "資産額",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
    HorizontalDivider()
}
@Composable
private fun ForecastHeaderRow() {
    Surface(tonalElevation = 1.dp, shadowElevation = 1.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp) // 行と同じくらいの薄さに
        ) {
            Text(
                text = "年月日",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "追加金額",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "差分",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "資産額",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }
    }
    HorizontalDivider()
}


private fun ForecastSettings.seriesColorChoices() =
    SeriesColorChoices(gain = colorGain, loss = colorLoss, actual = colorActual, forecast = colorForecast)

// ── 設定ダイアログの「カラー」セクション ──
// 分かりやすさ優先の構成：
//   1) 一括切り替え（日本式／欧米式）… 多くの人はこれだけで済む
//   2) 4項目の行（項目名＋現在の色）… タップした1行だけがその場で開き、12色のプリセットを選べる
//   3) プリセットに無い色は、開いた中の「自由に選ぶ」から色相・明るさの2本のスライダーで作る
// 色はタップ（スライダーは指を離した時）の瞬間に保存・反映するので「保存」ボタンは不要。
@Composable
private fun SeriesColorSettings(
    choices: SeriesColorChoices,
    onChange: (SeriesColorChoices) -> Unit
) {
    val colors = LocalSeriesColors.current
    var expandedSlot by remember { mutableStateOf<Int?>(null) }

    fun isStyle(style: Pair<String, String>, defaultIsThis: Boolean): Boolean {
        val gainOk = choices.gain == style.first || (defaultIsThis && choices.gain == null)
        val lossOk = choices.loss == style.second || (defaultIsThis && choices.loss == null)
        return gainOk && lossOk
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 1) 一括切り替え。▲▼をそれぞれの色で見せ、押すと何色になるかが文字を読まずに分かるようにする
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("日本式", JapaneseStyleChoices, true),
                Triple("欧米式", WesternStyleChoices, false)
            ).forEach { (name, style, isDefault) ->
                val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                val up = resolveColorChoice(style.first, isDark, colors.gain)
                val down = resolveColorChoice(style.second, isDark, colors.loss)
                FilterChip(
                    selected = isStyle(style, isDefault),
                    onClick = { onChange(choices.copy(gain = style.first, loss = style.second)) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name)
                            Spacer(Modifier.width(6.dp))
                            Text("▲", color = up)
                            Text("▼", color = down)
                        }
                    }
                )
            }
        }

        // 2) 4項目。開けるのは常に1行だけにして、画面がごちゃつかないようにする
        val slots = listOf(
            Triple("増えた時", colors.gain, choices.gain),
            Triple("減った時", colors.loss, choices.loss),
            Triple("実績の線", colors.actual, choices.actual),
            Triple("予測の線", colors.forecast, choices.forecast)
        )
        slots.forEachIndexed { index, (label, color, choice) ->
            val expanded = expandedSlot == index
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expandedSlot = if (expanded) null else index }
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                ColorSwatch(color = color, size = 22.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                ColorPalettePanel(
                    choice = choice,
                    currentColor = color,
                    onPick = { picked ->
                        onChange(
                            when (index) {
                                0 -> choices.copy(gain = picked)
                                1 -> choices.copy(loss = picked)
                                2 -> choices.copy(actual = picked)
                                else -> choices.copy(forecast = picked)
                            }
                        )
                    }
                )
            }
        }
    }
}

// 色見本の丸。背景と同化しないよう細い枠を付ける。
@Composable
private fun ColorSwatch(
    color: ComposeColor,
    size: androidx.compose.ui.unit.Dp,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .size(size)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .padding(if (selected) 3.dp else 1.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            // 選択中の印。明るい色の上でも暗い色の上でも読めるよう、色の明るさで白/黒を切り替える
            Text(
                "✓",
                color = if (color.luminance() > 0.5f) ComposeColor.Black else ComposeColor.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 1項目分の色選択パネル：12色のプリセット＋「自由に選ぶ」＋「初期色に戻す」
@Composable
private fun ColorPalettePanel(
    choice: String?,
    currentColor: ComposeColor,
    onPick: (String?) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var showCustom by remember { mutableStateOf(choice?.startsWith("c:") == true) }

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ColorPresets.chunked(6).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { preset ->
                    val id = presetChoice(preset.id)
                    ColorSwatch(
                        color = if (isDark) preset.dark else preset.light,
                        size = 32.dp,
                        selected = choice == id,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { showCustom = false; onPick(id) }
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // 「自由に選ぶ」：虹色の丸で、プリセット以外の色を作れることが一目で分かるようにする
            Row(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showCustom = !showCustom }
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(22.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.sweepGradient(rainbowColors()),
                            CircleShape
                        )
                        .border(
                            if (choice?.startsWith("c:") == true) 2.dp else 0.dp,
                            MaterialTheme.colorScheme.onSurface,
                            CircleShape
                        )
                )
                Spacer(Modifier.width(6.dp))
                Text("自由に選ぶ", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { showCustom = false; onPick(null) }, enabled = choice != null) {
                Text("初期色に戻す")
            }
        }
        AnimatedVisibility(visible = showCustom) {
            CustomColorPicker(initial = currentColor, onPicked = { onPick(customChoice(it)) })
        }
    }
}

private fun rainbowColors() = listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map {
    ComposeColor.hsv(it, 0.8f, 0.95f)
}

// 自由に色を作る：「色合い」と「明るさ」の2本だけ。彩度は見やすさ優先で固定する。
// ドラッグ中はプレビューだけ更新し、指を離した時に保存（ドラッグ中に毎回保存しない）。
@Composable
private fun CustomColorPicker(initial: ComposeColor, onPicked: (ComposeColor) -> Unit) {
    val hsv = remember(initial) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initial.toArgb(), it) }
    }
    var hue by remember(initial) { mutableStateOf(hsv[0]) }
    var value by remember(initial) { mutableStateOf(hsv[2].coerceIn(0.35f, 1f)) }
    val saturation = 0.8f
    val picked = ComposeColor.hsv(hue, saturation, value)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("色合い", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        GradientSlider(
            value = hue / 360f,
            onValueChange = { hue = it * 360f },
            onValueChangeFinished = { onPicked(ComposeColor.hsv(hue, saturation, value)) },
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(rainbowColors())
        )
        Text("明るさ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        GradientSlider(
            value = (value - 0.35f) / 0.65f,
            onValueChange = { value = 0.35f + it * 0.65f },
            onValueChangeFinished = { onPicked(ComposeColor.hsv(hue, saturation, value)) },
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                listOf(ComposeColor.hsv(hue, saturation, 0.35f), ComposeColor.hsv(hue, saturation, 1f))
            )
        )
        // 自由色はダーク/ライト共通の1色になるため、両方の背景での見え方を並べて確認できるようにする
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                com.journeygirl.assetforecast.ui.theme.BgDark,
                com.journeygirl.assetforecast.ui.theme.BgLight
            ).forEach { bg ->
                Box(
                    Modifier
                        .weight(1f)
                        .background(bg, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "+¥12,345",
                        color = picked,
                        fontFamily = com.journeygirl.assetforecast.ui.theme.NumberFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// グラデーションの帯の上にスライダーを重ねたもの（帯で「どちらに動かすと何色になるか」を見せる）
@Composable
private fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    brush: androidx.compose.ui.graphics.Brush
) {
    Box(Modifier.fillMaxWidth().height(32.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .height(10.dp)
                .background(brush, RoundedCornerShape(5.dp))
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            colors = SliderDefaults.colors(
                thumbColor = ComposeColor.White,
                activeTrackColor = ComposeColor.Transparent,
                inactiveTrackColor = ComposeColor.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 設定ダイアログの数値入力欄。ダイアログ背景から浮くよう枠線（outline）と薄い塗り（surfaceContainerHighest）を付け、
// 単位はラベルに混ぜずsuffixで入力値の右に出す（ラベルは短く、単位は常に見える位置に）。
// ラベルは枠の外（上）に常時表示する。OutlinedTextFieldのlabelを使うと、空欄の時だけラベルが
// 枠内に大きく表示されsuffixも隠れるため、入力済みの欄と見た目が揃わず分かりにくくなる。
@Composable
private fun SettingsField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suffix: String? = null,
    placeholder: String = "未設定",
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Number,
    labelTrailing: (@Composable () -> Unit)? = null   // ラベル行の右端に置く小さな部品（切り替えなど）
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .weight(1f)
            )
            labelTrailing?.invoke()
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            suffix = suffix?.let { { Text(it) } },
            placeholder = { Text(placeholder) },
            supportingText = supportingText?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = com.journeygirl.assetforecast.ui.theme.NumberFontFamily
            ),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                focusedSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── 定期的な収支（決まった月に予測へ足し引きする） ──

// 名前が空の時は「支出」「収入」と表示する
private fun ScheduledExpense.displayName(): String = name.ifBlank { if (isIncome) "収入" else "支出" }

// 符号付きの金額表示（例「−¥50,000」「+¥300,000」）
private fun formatScheduledYen(signed: Long): String =
    (if (signed < 0) "−¥" else "+¥") + formatYen(kotlin.math.abs(signed))

// 一覧や編集画面に出す「いつ・何回」の説明文
private fun ScheduledExpense.scheduleText(): String = when (repeat) {
    ExpenseRepeat.ONCE -> "${startYear}年${month}月の1回"
    ExpenseRepeat.YEARLY_FOREVER -> "毎年${month}月・${startYear}年からずっと"
    ExpenseRepeat.YEARLY_YEARS -> "毎年${month}月・${startYear}〜${endYear}年（${years}回）"
}

// 編集画面の確認用の文（例：「2025〜2034年の毎年12月に ¥50,000（10回）」）
private fun ScheduledExpense.previewText(): String = formatScheduledYen(signedAmount).let { yen ->
    when (repeat) {
        ExpenseRepeat.ONCE -> "${startYear}年${month}月に 1回だけ $yen"
        ExpenseRepeat.YEARLY_FOREVER -> "${startYear}年から毎年${month}月に $yen（ずっと）"
        ExpenseRepeat.YEARLY_YEARS -> "${startYear}〜${endYear}年の毎年${month}月に $yen（${years}回）"
    }
}

// 予測期間 [forecastStart, forecastEnd] の中で実際に差し引かれる回数
private fun ScheduledExpense.countWithin(forecastStart: YearMonth?, forecastEnd: YearMonth?): Int? {
    if (forecastStart == null || forecastEnd == null || forecastEnd < forecastStart) return null
    var count = 0
    var y = maxOf(startYear, forecastStart.year)
    while (y <= forecastEnd.year && (endYear == null || y <= endYear!!)) {
        val ym = YearMonth.of(y, month)
        if (ym >= forecastStart && ym <= forecastEnd && y >= startYear) count++
        y++
    }
    return count
}

// 設定ダイアログ内の「定期的な収支」セクション。項目のタップで編集、スイッチで一時的に外す、＋で追加。
// 変更はその場で保存する（設定ダイアログの「保存」を押し忘れても消えないように）。
@Composable
private fun ScheduledExpenseSection(
    items: List<ScheduledExpense>,
    onChange: (List<ScheduledExpense>) -> Unit,
    forecastStart: YearMonth?,
    forecastEnd: YearMonth?
) {
    // null=閉じている。id=0Lの新規項目なら「追加」、それ以外は「編集」
    var editing by remember { mutableStateOf<ScheduledExpense?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "ローンやボーナスなど、決まった月の支出・収入を予測に反映します",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        items.forEach { e ->
            ScheduledExpenseItem(
                item = e,
                outOfRange = e.countWithin(forecastStart, forecastEnd) == 0,
                onClick = { editing = e },
                onEnabledChange = { on ->
                    onChange(items.map { if (it.id == e.id) it.copy(enabled = on) else it })
                }
            )
        }
        OutlinedButton(
            onClick = {
                val base = forecastStart ?: YearMonth.now().plusMonths(1)
                editing = ScheduledExpense(
                    id = 0L, name = "", amount = 0L, month = 12,
                    startYear = if (12 >= base.monthValue) base.year else base.year + 1,
                    repeat = ExpenseRepeat.YEARLY_YEARS, years = 10
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("＋ 追加")
        }
    }

    editing?.let { target ->
        ScheduledExpenseEditor(
            initial = target,
            isNew = target.id == 0L,
            forecastStart = forecastStart,
            forecastEnd = forecastEnd,
            onDismiss = { editing = null },
            onSave = { saved ->
                onChange(
                    if (target.id == 0L) items + saved.copy(id = System.currentTimeMillis())
                    else items.map { if (it.id == saved.id) saved else it }
                )
                editing = null
            },
            onDelete = {
                onChange(items.filterNot { it.id == target.id })
                editing = null
            }
        )
    }
}

@Composable
private fun ScheduledExpenseItem(
    item: ScheduledExpense,
    outOfRange: Boolean,
    onClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit
) {
    // OFFの項目は薄く表示して「今は計算に入っていない」ことを分かるようにする
    val contentAlpha = if (item.enabled) 1f else 0.45f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(1f)
                .alpha(contentAlpha)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.displayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatScheduledYen(item.signedAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = com.journeygirl.assetforecast.ui.theme.NumberFontFamily,
                    color = if (item.isIncome) LocalSeriesColors.current.gain else LocalSeriesColors.current.loss,
                    maxLines = 1
                )
            }
            Text(
                text = item.scheduleText(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            if (outOfRange && item.enabled) {
                Text(
                    text = "予測期間の外なので反映されません",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Switch(
            checked = item.enabled,
            onCheckedChange = onEnabledChange,
            modifier = Modifier.scale(0.8f)
        )
    }
}

// 「支出｜収入」の2択。選んだ側を増減の色で塗る（支出＝減った時の色、収入＝増えた時の色）
@Composable
private fun IncomeExpenseToggle(isIncome: Boolean, onChange: (Boolean) -> Unit) {
    val colors = LocalSeriesColors.current
    Row(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        listOf(false to "支出", true to "収入").forEach { (income, label) ->
            val selected = isIncome == income
            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) (if (income) colors.gain else colors.loss) else ComposeColor.Transparent)
                    .clickable { onChange(income) }
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) ComposeColor.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ScheduledExpenseEditor(
    initial: ScheduledExpense,
    isNew: Boolean,
    forecastStart: YearMonth?,
    forecastEnd: YearMonth?,
    onDismiss: () -> Unit,
    onSave: (ScheduledExpense) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var amountText by remember { mutableStateOf(initial.amount.takeIf { it > 0 }?.toString() ?: "") }
    var isIncome by remember { mutableStateOf(initial.isIncome) }
    var month by remember { mutableStateOf(initial.month) }
    var repeat by remember { mutableStateOf(initial.repeat) }
    var yearText by remember { mutableStateOf(initial.startYear.toString()) }
    var yearsText by remember { mutableStateOf(initial.years.toString()) }
    // 新規追加で年を自分で入力していない間は、月を変えたら「予測の中で最初に来るその月」の年に合わせる
    var yearTouched by remember { mutableStateOf(!isNew) }

    val amount = amountText.toLongOrNull() ?: 0L
    val year = yearText.toIntOrNull()?.takeIf { it in 1900..2300 }
    val years = yearsText.toIntOrNull()?.takeIf { it in 1..100 }
    val draft = if (year != null && (repeat != ExpenseRepeat.YEARLY_YEARS || years != null)) {
        initial.copy(
            name = name.trim(), amount = amount, month = month, startYear = year,
            repeat = repeat, years = years ?: 1, enabled = initial.enabled, isIncome = isIncome
        )
    } else null
    val canSave = draft != null && amount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "収支を追加" else "収支を編集") },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingsField(
                    value = name,
                    onValueChange = { name = it.take(20) },
                    label = "名前",
                    placeholder = "例: ローン、ボーナス",
                    keyboardType = KeyboardType.Text
                )
                SettingsField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() }.take(10) },
                    label = "金額（1回あたり）",
                    suffix = "円",
                    placeholder = "例: 50000",
                    // 支出か収入かは、項目を増やさず金額ラベルの横の切り替えだけで選ぶ
                    labelTrailing = { IncomeExpenseToggle(isIncome = isIncome, onChange = { isIncome = it }) }
                )

                // 月：12個のボタンから1つ選ぶ（数字入力より押し間違いが少なく、一目で分かる）
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "月",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    (1..12).chunked(6).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { m ->
                                val selected = m == month
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceContainerHighest
                                        )
                                        .clickable {
                                            month = m
                                            if (!yearTouched) {
                                                val base = forecastStart ?: YearMonth.now().plusMonths(1)
                                                yearText = (if (m >= base.monthValue) base.year else base.year + 1).toString()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${m}月",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // 繰り返し：3択をラジオボタンで縦に並べる（選択肢の意味が文章で読めるように）
                Column {
                    Text(
                        text = "繰り返し",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    listOf(
                        ExpenseRepeat.ONCE to "1回だけ",
                        ExpenseRepeat.YEARLY_YEARS to "毎年（期間を指定）",
                        ExpenseRepeat.YEARLY_FOREVER to "毎年（ずっと）"
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { repeat = value }
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = repeat == value, onClick = { repeat = value })
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        SettingsField(
                            value = yearText,
                            onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4); yearTouched = true },
                            label = if (repeat == ExpenseRepeat.ONCE) "年" else "開始する年",
                            suffix = "年"
                        )
                    }
                    if (repeat == ExpenseRepeat.YEARLY_YEARS) {
                        Box(Modifier.weight(1f)) {
                            SettingsField(
                                value = yearsText,
                                onValueChange = { yearsText = it.filter { ch -> ch.isDigit() }.take(3) },
                                label = "期間",
                                suffix = "年間"
                            )
                        }
                    }
                }

                // 入力内容を文章で確認できるようにする（「何年の何月から何回」を取り違えないように）
                if (draft != null && amount > 0) {
                    val count = draft.countWithin(forecastStart, forecastEnd)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = draft.previewText(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (count != null) {
                            Text(
                                text = if (count > 0) "予測期間の中では ${count} 回、合計 ¥${formatYen(amount * count)} を" +
                                    (if (isIncome) "足します" else "差し引きます")
                                else "予測期間（〜${forecastEnd!!.year}年${forecastEnd.monthValue}月）の外なので反映されません",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (count > 0) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { draft?.let(onSave) }, enabled = canSave) { Text("保存") }
        },
        dismissButton = {
            Row {
                if (!isNew) {
                    TextButton(onClick = onDelete) {
                        Text("削除", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("キャンセル") }
            }
        }
    )
}

@Composable
private fun appTextFieldColors(): TextFieldColors =
    TextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface
    )



/** DatePickerDialog に “減光しっぱなし” 回避フックを仕込む */
fun DatePickerDialog.installAntiStuckDim() {
    // dialog.show() 済みであること（View が作られている必要あり）
    val root = window?.decorView ?: return
    val pickers = root.findAllNumberPickers()
    pickers.forEach { picker ->
        // 1) スクロールが止まったら確実に明るさを戻す
        picker.setOnScrollListener { _, state ->
            if (state == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                picker.isPressed = false
                picker.invalidate()
                picker.post { picker.refreshDrawableState() }
            }
        }
        // 2) タップで止めた直後に pressed が残らないようにする
        picker.setOnTouchListener { v, ev ->
            if (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_CANCEL) {
                v.isPressed = false
                v.parent?.requestDisallowInterceptTouchEvent(false)
                v.invalidate()
                v.post { v.refreshDrawableState() }
            }
            false // 既存の挙動は維持
        }
        // 念のためフォーカスでのハイライトも抑える
        picker.isFocusable = false
        picker.isFocusableInTouchMode = false
    }
}

// View 階層から NumberPicker をすべて集める（リフレクション無し）
private fun View.findAllNumberPickers(): List<NumberPicker> {
    val out = mutableListOf<NumberPicker>()
    fun dfs(v: View) {
        if (v is NumberPicker) out += v
        if (v is ViewGroup) for (i in 0 until v.childCount) dfs(v.getChildAt(i))
    }
    dfs(this)
    return out
}

// Compose の LocalContext は ContextWrapper で包まれていることがあるため、Activity まで辿って取り出す
private tailrec fun android.content.Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

// 折れ線の下を同系色で塗る（上が薄い色、下に向かって透明になるグラデーション）。
// 線の色から作るので、設定の「カラー」で色を変えても塗りが追従する。
private fun LineDataSet.applyAreaFill(lineColorArgb: Int) {
    setDrawFilled(true)
    fillDrawable = android.graphics.drawable.GradientDrawable(
        android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(
            androidx.core.graphics.ColorUtils.setAlphaComponent(lineColorArgb, 0x55),
            androidx.core.graphics.ColorUtils.setAlphaComponent(lineColorArgb, 0x00)
        )
    )
}
