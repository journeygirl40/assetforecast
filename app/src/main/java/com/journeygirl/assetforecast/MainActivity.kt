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
import androidx.compose.ui.draw.clip
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                var showAds by remember { mutableStateOf(false) }   // ← 広告表示トリガー
                AppRoot(vm = vm, showAds = showAds)

                // UI表示後に重い初期化を非同期で実行
                LaunchedEffect(Unit) {
                // UMP（同意管理）を非同期初期化
                runCatching {
                    val params = ConsentRequestParameters.Builder().build()
                    val consentInfo = UserMessagingPlatform.getConsentInformation(this@MainActivity)
                    suspendCancellableCoroutine<Unit> { cont ->
                        consentInfo.requestConsentInfoUpdate(
                            this@MainActivity, params,
                            {
                                if (consentInfo.isConsentFormAvailable) {
                                    UserMessagingPlatform.loadConsentForm(
                                        this@MainActivity,
                                        { form -> form.show(this@MainActivity) { cont.resume(Unit) {} } },
                                        { cont.resume(Unit) {} }
                                    )
                                } else cont.resume(Unit) {}
                            },
                            { cont.resume(Unit) {} }
                        )
                    }
                }

                // AdMob を遅延初期化
                suspendCancellableCoroutine<Unit> { cont ->
                    MobileAds.initialize(this@MainActivity) { cont.resume(Unit) {} }
                }

                // ▼ 追加：Unity Ads 初期化（動作確認なので testMode=ON）
                runCatching {
                    com.unity3d.ads.UnityAds.initialize(
                        this@MainActivity,
                        com.journeygirl.assetforecast.ads.AdsManager.UNITY_GAME_ID,
                        com.journeygirl.assetforecast.ads.AdsManager.UNITY_TEST_MODE
                    )
                }
                showAds = true // ★これを追加（初期化が終わったら広告表示を許可）
                }
            }
        }
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

    val settings by vm.settings.collectAsState()
    LaunchedEffect(settings.language) { vm.applyLanguage(settings.language) }
    val records by vm.records.collectAsState()
    var editing by remember { mutableStateOf<AssetRecord?>(null) }

    var showAdd by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<Long?>(null) }   // ← AssetRecord.id が Int の想定
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

    fun recordsToCsv(rows: List<AssetRecord>): String {
        val sb = StringBuilder()
        sb.appendLine("date,amount,note")
        rows.sortedBy { it.dateEpochDays }.forEach { r ->
            val date = LocalDate.ofEpochDay(r.dateEpochDays.toLong()).format(DATE_FMT)
            val amount = r.amount.toString() // カンマ無し
            val note = r.note?.replace("\"", "\"\"") ?: ""
            // C列はカンマを含む可能性があるのでダブルクォートで囲む
            sb.append(date).append(",")
                .append(amount).append(",")
                .append("\"").append(note).append("\"")
                .appendLine()
        }
        return sb.toString()
    }

    fun parseCsvLine(line: String): List<String> {
        // かんたんRFC4180対応（"で囲まれたカンマ、""のエスケープ対応）
        val out = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (ch == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    cur.append('\"') // エスケープ
                    i += 2
                    continue
                } else {
                    inQuotes = !inQuotes
                }
            } else if (ch == ',' && !inQuotes) {
                out += cur.toString()
                cur = StringBuilder()
            } else {
                cur.append(ch)
            }
            i++
        }
        out += cur.toString()
        return out
    }

    // --- エクスポート launcher ---
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    OutputStreamWriter(os, Charsets.UTF_8).use { w ->
                        w.write(recordsToCsv(vm.records.value))
                    }
                }
            }
        }
    }

    // --- インポート launcher ---
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { ins ->
                    BufferedReader(InputStreamReader(ins, Charsets.UTF_8)).use { br ->
                        var first = true
                        br.lineSequence().forEach { line ->
                            if (line.isBlank()) return@forEach
                            // ヘッダー行は飛ばす
                            if (first && line.lowercase().startsWith("date,amount")) {
                                first = false
                                return@forEach
                            }
                            first = false
                            val cols = parseCsvLine(line)
                            if (cols.size >= 2) {
                                val date = parseDateOrNull(cols[0]) ?: return@forEach
                                val amount = cols[1].trim().toLongOrNull() ?: return@forEach
                                val note = cols.getOrNull(2)?.replace("\"\"", "\"")?.trim('"')?.ifBlank { null }
                                vm.addRecord(date, amount, note)
                            }
                        }
                    }
                }
            }
        }
    }


    LaunchedEffect(selectedId, records, selectionSource) {
        if (selectionSource == SelectionSource.CHART && selectedId != null) {
            val sorted = records.sortedBy { it.dateEpochDays }
            val index = sorted.indexOfFirst { it.id == selectedId }

            if (index >= 0) {
                // StickyHeaderが1件あるため +1。scrollOffset=0 でリストの最上部に固定。
                listState.scrollToItem(index + 0, scrollOffset = 0)
            }

            // 一度スクロールしたらフラグをリセット
            selectionSource = null
        }
        // LIST の場合はスクロールなし
    }

    val forecasts = remember(records, settings) {
        buildForecastPoints(
            records = records,
            months = settings.months,
            sampleN = settings.sampleN,
            mode = settings.mode,
            customRateMonthly = settings.customRatePerMonth,  // ← 追加
            customDeltaPerMonth = settings.customDeltaPerMonth,   // ★ 追加
            drawdownStartMonth = settings.drawdownStartMonth,   // ← 渡す
            withdrawPerMonth  = settings.withdrawPerMonth       // ← 渡す
        )
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
                val selectedEpochDays: Int? = remember(records, selectedId) {
                    selectedId?.let { id -> records.firstOrNull { it.id == id }?.dateEpochDays }
                }

                // ▼ ミニマップナビゲーターの選択範囲から実際のグラフ表示範囲を算出（null=全期間）
                val displayRange: DisplayRange = customRange ?: DisplayRange(null, null)

                Column(Modifier.fillMaxSize()) {

                    // ── 上：全期間（実績＋予測）の簡易推移＋現在の表示範囲ハイライト（ナビゲーター） ──
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
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    // ミニマップで選んだ期間の差分平均・増減率（グラフの高さを奪わないよう1行の小さな帯にする）。
                    // ミニマップの補足情報なので控えめな文字にし、下の「選択中の日付・金額」との間は少し空けて別物に見せる。
                    RangeStatsStrip(
                        records = records,
                        range = displayRange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, bottom = 6.dp)
                    )

                    // ── ミニマップ下：グラフ（高さは split に応じて可変） ──
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
                            range = displayRange,
                            onPointSelected = { epochDaysOrNull: Int? ->
                                selectedId = epochDaysOrNull?.let { picked ->
                                    records.minByOrNull { r -> kotlin.math.abs(r.dateEpochDays - picked) }?.id
                                }
                                selectionSource = SelectionSource.CHART
                            },
                            modifier = Modifier
                                .fillMaxSize() // ← Box 全体にグラフを広げる
                                .padding(0.dp),
                            // ▼ 追加：初期ハイライトに最新実績を渡す
                            initialHighlightEpochDays = latestEpochDays
                        )
                    }

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
                                        selectionSource =
                                            SelectionSource.LIST   // ← リスト由来なのでスクロールしない
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
                                    addFixed = p.addFixed ?: 0L  // ← 固定追加（未使用なら 0）
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
    if (showAdd) {
        AddDialog(
            onDismiss = { showAdd = false },
            onSubmit = { date, amount, note ->
                vm.addRecord(date, amount, note)
                showAdd = false
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
                exportLauncher.launch(name)
            },
            onImportCsv = {
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
            }
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
    onSeriesColorsChange: (SeriesColorChoices) -> Unit = {}
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
                    val uriHandler = LocalUriHandler.current
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
    range: DisplayRange = DisplayRange(null, null), // ← ミニマップナビゲーターで選択された表示範囲
    onPointSelected: (Int?) -> Unit = {},
    initialHighlightEpochDays: Int? = null // ← これを追加
) {
    val lastRangeKey = remember { mutableStateOf<Pair<Int?, Int?>?>(null) }
    // 予測線が実際に描画されたか（データ不足等で描画されない月もあるため、凡例バッジの出し分けに使う）
    var hasForecast by remember { mutableStateOf(false) }
    // グラフで選択中の点。バッジ行の中央に日付・金額を表示するために使う（以前のふきだしMarkerの代わり）
    var selectedPoint by remember { mutableStateOf<SelectedChartPoint?>(null) }

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
                Entry(r.dateEpochDays.toFloat(), r.amount.toFloat())
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
                            amt = (if (rate != 0.0) amt * (1.0 + rate) else amt) + fixedThisMonth
                            list += Entry(d.toEpochDay().toFloat(), amt.toFloat())
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
                    selectedPoint = e?.let { SelectedChartPoint(it.x.toLong(), it.y.toLong(), isForecast) }
                    onPointSelected(epochDays)
                }
                override fun onNothingSelected() {
                    selectedPoint = null
                    onPointSelected(null)
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
// ▼ 初期ハイライト：最新の実績を選択状態にする（表示範囲内にある時だけ）
            initialHighlightEpochDays?.let { epoch ->
                if (displayed.any { it.dateEpochDays == epoch }) {
                    chart.highlightValue(epoch.toFloat(), 0, true)
                }
            }
            // 表示範囲（ミニマップナビゲーターの選択）が変わった時だけビューをリセットする。
            // 無関係な再コンポジションで毎回呼ぶと、ユーザーのピンチズーム/パン操作が意図せずリセットされてしまう。
            val rangeKey = range.start to range.end
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

    val highlightStart = range.start ?: fullStart
    val highlightEnd = range.end ?: fullEnd

    // detectDragGestures のコールバック内から常に最新のハイライト範囲／コールバックを参照するため
    val currentHighlight by rememberUpdatedState(highlightStart to highlightEnd)
    val currentOnRangeChange by rememberUpdatedState(onRangeChange)

    fun emitRange(newStart: Int, newEnd: Int) {
        currentOnRangeChange(
            DisplayRange(
                start = if (newStart <= fullStart) null else newStart,
                end = if (newEnd >= fullEnd) null else newEnd
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
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (widthPx <= 0f) return@detectDragGestures
                            val (hs, he) = currentHighlight
                            val deltaEpoch = (dragAmount.x / widthPx * fullSpan).roundToInt()
                            // he-minRangeDaysがfullStartを下回り得る（heがfullStartに極めて近い場合）ため、
                            // coerceInの上限がfullStart未満にならないようクランプしてから使う
                            val upperBound = (he - minRangeDays).coerceAtLeast(fullStart)
                            val newStart = (hs + deltaEpoch).coerceIn(fullStart, upperBound)
                            emitRange(newStart, he)
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
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (widthPx <= 0f) return@detectDragGestures
                            val (hs, he) = currentHighlight
                            val deltaEpoch = (dragAmount.x / widthPx * fullSpan).roundToInt()
                            // hs+minRangeDaysがfullEndを上回り得る（hsがfullEndに極めて近い場合）ため、
                            // coerceInの下限がfullEndを超えないようクランプしてから使う
                            val lowerBound = (hs + minRangeDays).coerceAtMost(fullEnd)
                            val newEnd = (he + deltaEpoch).coerceIn(lowerBound, fullEnd)
                            emitRange(hs, newEnd)
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
private fun ForecastRow(p: ForecastPoint, diff: Long, addFixed: Long ) {
    val date = LocalDate.ofEpochDay(p.epochDays.toLong())
    val grey = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
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
    HorizontalDivider(thickness = 0.5.dp, color = grey.copy(alpha = 0.4f))
}

// グラフの表示期間プリセット（監視ダッシュボード風のタイムレンジセレクタ用）
// グラフに表示するX軸範囲（epochDays）。start/endがnullなら無制限。
// ミニマップナビゲーターのハイライト枠が全期間の端に達している側はnull（予測線は末尾まで表示）。
// AssetChartSection（public）のパラメータ型として使うため可視性はpublicにする。
data class DisplayRange(val start: Int?, val end: Int?)

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
    val addFixed: Long = 0L        // ★ 追加：その月に加算した固定額
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
    fixedAddPerMonth: Long = 0L      // ★ 追加：毎月の固定加算額（0 なら加算なし）

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

        val nextAmountBD = afterRate.add(BigDecimal.valueOf(fixedThisMonth.toDouble()))

        // 小数点以下を四捨五入してLong化
        val nextAmount = nextAmountBD.setScale(0, RoundingMode.HALF_UP).toLong()

        out += ForecastPoint(
            epochDays = d.toEpochDay().toInt(),
            amount = nextAmount,
            diff = nextAmount - prevAmount.setScale(0, RoundingMode.HALF_UP).toLong(),
            fixedAdd = fixedThisMonth,
            addFixed = fixedAddPerMonth
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
    keyboardType: KeyboardType = KeyboardType.Number
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
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
