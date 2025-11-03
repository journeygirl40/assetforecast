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
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color as ComposeColor
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

        // ③ ステータスバーのアイコンを「ダーク（黒寄り）」に
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true   // ← これが肝
        // （必要ならナビゲーションバーも）
        controller.isAppearanceLightNavigationBars = true


        // 画面を先に描画
        setContent {
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
    var fabOffsetY by remember { mutableStateOf(0f) }

    var settingsOffsetX by remember { mutableStateOf(0f) }
    var settingsOffsetY by remember { mutableStateOf(0f) }

    val settings by vm.settings.collectAsState()
    LaunchedEffect(settings.language) { vm.applyLanguage(settings.language) }
    val records by vm.records.collectAsState()
    var editing by remember { mutableStateOf<AssetRecord?>(null) }

    var showAdd by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<Long?>(null) }   // ← AssetRecord.id が Int の想定
    var selectionSource by remember { mutableStateOf<SelectionSource?>(null) }
    val listState = rememberLazyListState()

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
        bottomBar = {
            if (!settings.adFree) { // ← 条件は残す！
                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()     // ← 高さは中身に合わせる
                        .navigationBarsPadding()   // ← これで最下部に固定
                ) {
                    BannerAdView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }
    ) { inner ->
        // 画面全体の高さが必要なので BoxWithConstraints を使う
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(inner)          // ✅ Scaffold から渡される安全領域を反映
        ) {
            val density = LocalDensity.current
            val totalPx = with(density) { maxHeight.toPx() }         // 有効ドラッグ量の基準
            var split by rememberSaveable { mutableStateOf(0.45f) }   // 上(グラフ)の比率 0.0..1.0
            val handleHeight = 12.dp                                  // つまみの高さ
            val latestEpochDays: Int? = records.maxByOrNull { it.dateEpochDays }?.dateEpochDays

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
                        withdrawPerMonth  = settings.withdrawPerMonth,
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
                    contentPadding = PaddingValues(bottom = 72.dp)
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
                                    selectionSource = SelectionSource.LIST   // ← リスト由来なのでスクロールしない
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
                                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
                            )
                        }
                        // ← ヘッダーを stickyHeader に（実績と同じ動き）
                        stickyHeader { ForecastHeaderRow() }

                        // 予測行（年月日 / 追加金額 / 差分 / 資産額）
                        itemsIndexed(forecasts) { idx, p ->
                            val prevAmount =
                                if (idx == 0) records.maxByOrNull { it.dateEpochDays }?.amount ?: p.amount
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
    onImportCsv: () -> Unit      // ← 追加
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
                    OutlinedTextField(
                        value = yearsText,
                        onValueChange = { yearsText = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("予測年数（0〜100）") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                            OutlinedTextField(
                                value = customNText,
                                onValueChange = {
                                    customNText = it.filter { ch -> ch.isDigit() }.take(4)
                                },
                                label = { Text("カスタムN（件数）") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            sampleChoice = customNText.toIntOrNull() ?: sampleChoice
                        }

                    } else {
                        if (mode == "custom") {
                            OutlinedTextField(
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
                                label = { Text("固定の増減率") },
                                placeholder = { Text("例: 4") },
                                trailingIcon = { Text("%/年") },   // 単位明示
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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
                    OutlinedTextField(
                        value = fixedAddText,
                        onValueChange = { t ->
                            // 符号は不要にするなら数字だけ許可
                            fixedAddText = t.filter { it.isDigit() }.take(10)
                        },
                        label = {
                            Text(
                                "固定追加金額（円/月）",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        placeholder = { Text("例: 30000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Text(
                        text = "切り崩し設定",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = drawdownStartYearsText,
                        onValueChange = {
                            drawdownStartYearsText = it.filter { ch -> ch.isDigit() }.take(3)
                        },
                        label = {
                            Text(
                                "〇年後から（予測年数未満）",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        supportingText = { Text("例：予測 30 年なら 0〜29 のいずれか") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = withdrawPerMonthText,
                        onValueChange = {
                            withdrawPerMonthText = it.filter { it.isDigit() }.take(10)
                        },
                        label = {
                            Text(
                                "固定切り崩し金額（円/月）",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        placeholder = { Text("例: 50000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                    // ～～（既存の設定項目の下あたりに追記）～～
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
                            color = Color.Blue, // 青文字
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
                    ForecastSettings(
                        mode = mode,
                        sampleN = finalSample,
                        months = months,
                        customRatePerMonth = monthlyRate,   // ← 単利換算した値を保存
                        customDeltaPerMonth = fixedAdd,    // ★ ここに保存
                        drawdownStartMonth = ddStartMonth,
                        withdrawPerMonth = withdrawPerMonth,
                        language = lang,
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
    onPointSelected: (Int?) -> Unit = {},
    initialHighlightEpochDays: Int? = null // ← これを追加
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setNoDataText("データがありません")
                setPinchZoom(true)
                setScaleEnabled(true)
                setAutoScaleMinMaxEnabled(true)    // データ範囲に合わせて自動スケール
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(true)
                xAxis.setGranularityEnabled(true)
                legend.isEnabled = true
                setDrawMarkers(true)

                // ▼ 追加：縦軸（Y軸）の金額文字サイズを小さく
                axisLeft.textSize = 8f  // ← デフォルトは 10〜12f 程度。8fなら少し小さめ。

                marker = TextMarker(
                    context = context,
                    xFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                    yFormatter = { v -> "¥" + formatYen(v.toLong()) },
                    // “2mm 上” に出したいので mm→px 変換でオフセット指定
                    offsetLeftPx = mmToPx(context, 2f),
                    offsetDownPx = -mmToPx(context, 3f)
                )
            }
        },
        update = { chart ->
            // --- 実績データ（青） ---
            val sorted = records.sortedBy { it.dateEpochDays }
            val actualEntries = sorted.map { r ->
                Entry(r.dateEpochDays.toFloat(), r.amount.toFloat())
            }
            val actualSet = LineDataSet(actualEntries, "実績").apply {
                color = AndroidColor.BLUE
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                this.mode = LineDataSet.Mode.LINEAR   // ← ここを this.mode に
            }

            // ★ 月のユニーク数を数える（yyyyMM 単位）
            val monthCount = sorted.groupBy { r ->
                val d = LocalDate.ofEpochDay(r.dateEpochDays.toLong())
                d.year * 100 + d.monthValue
            }.size

            val dataSets: MutableList<ILineDataSet> = mutableListOf(actualSet)

// 予測線の作成ブロックだけ差し替え
            if (monthCount >= 1 && forecastMonths > 0) {
                val rate = if (mode == "custom" && customRateMonthly != null)
                    customRateMonthly
                else
                    averageMonthlyRate(sorted, sampleN)

                val monthlyAdd = (customDeltaPerMonth ?: 0L).toDouble()

                val last = sorted.lastOrNull()
                val forecastEntries =
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

                if (forecastEntries.isNotEmpty()) {
                    val forecastSet = LineDataSet(forecastEntries, "予測").apply {
                        val orange = AndroidColor.rgb(255, 165, 0)
                        color = orange                      // ▼ 線を緑に
                        setDrawCircles(false)         // 丸を描かない
                        lineWidth = 2f
                        setDrawValues(false)
                        this.mode = LineDataSet.Mode.LINEAR
                    }
                    dataSets += forecastSet
                }
                chart.legend.resetCustom()
            } else {
                // ★ 月が1つしかない → 予測線は描かず、凡例だけメッセージ表示
                val blue = AndroidColor.BLUE
                val orange = AndroidColor.rgb(255, 165, 0)


                val legend = chart.legend
                val entries = mutableListOf<com.github.mikephil.charting.components.LegendEntry>()

                // 実績（青）
                entries += com.github.mikephil.charting.components.LegendEntry().apply {
                    label = "実績"
                    formColor = blue
                    form = com.github.mikephil.charting.components.Legend.LegendForm.LINE
                }
                // メッセージ（オレンジ）
                entries += com.github.mikephil.charting.components.LegendEntry().apply {
                    label = "-"
                    formColor = orange
                    form = com.github.mikephil.charting.components.Legend.LegendForm.LINE
                }
                legend.setCustom(entries)
            }

            chart.data = LineData(dataSets)
            // 値選択リスナーを設定（ここがポイント）
            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    // x は epochDay を float にしたもの
                    val epochDays = e?.x?.toLong()?.toInt()
                    onPointSelected(epochDays)
                }
                override fun onNothingSelected() {
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
// ▼ 初期ハイライト：最新の実績を選択状態にする
            initialHighlightEpochDays?.let { epoch ->
                chart.highlightValue(epoch.toFloat(), 0, true)
            }
// （任意）凡例が近い場合の回避
            chart.invalidate()
        }
    )
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

    // 選択時の水色ハイライト
    val targetColor = if (selected) ComposeColor(0xFFE3F2FD) else ComposeColor.Transparent
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
            color = when {
                diff > 0 -> Color.Red          // プラス → 赤
                diff < 0 -> Color.Blue         // マイナス → 青
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // ±0 → グレー
            },
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
            color = when {
                diff > 0 -> Color.Red          // プラス → 赤
                diff < 0 -> Color.Blue         // マイナス → 青
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // ±0 → グレー
            },
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


// mm → px 変換
private fun mmToPx(context: Context, mm: Float): Float =
    mm * context.resources.displayMetrics.xdpi / 25.4f

// テキストふきだし用の Marker
private class TextMarker(
    private val context: Context,
    private val xFormatter: java.time.format.DateTimeFormatter,
    private val yFormatter: (Float) -> String,
    private val offsetLeftPx: Float = 0f,   // ← 追加：左方向へずらす量
    private val offsetDownPx: Float = 0f    // ← 追加：下方向へずらす量
) : com.github.mikephil.charting.components.IMarker {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.BLACK
        textSize = context.resources.displayMetrics.scaledDensity * 12f
    }
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = context.resources.displayMetrics.density
    }
    private val padH = context.resources.displayMetrics.density * 6f
    private val padV = context.resources.displayMetrics.density * 4f
    private var label: String = ""

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null) return
        val d = java.time.LocalDate.ofEpochDay(e.x.toLong())
        label = "${d.format(xFormatter)}  ${yFormatter(e.y)}"
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        if (label.isEmpty()) return

        // テキストサイズを測ってバッジの矩形を決定
        val textW = textPaint.measureText(label)
        val textH = textPaint.fontMetrics.let { it.descent - it.ascent }
        val boxW = textW + padH * 2
        val boxH = textH + padV * 2

        // 画面外に出ないよう X をクランプ
        val x = (posX - boxW / 2f).coerceAtLeast(0f)
        val maxX = canvas.width - boxW
        val boxX = x.coerceAtMost(maxX)

        // “点の 2mm 左” に移動（xは左へオフセット）
        val rawX = posX - boxW / 2f - offsetLeftPx

        // “点の 2mm 下” に移動（yは下方向が正なのでオフセットを足す）
        val rawY = posY + offsetDownPx
        val boxY = rawY.coerceIn(0f, canvas.height - boxH)

        val rect = android.graphics.RectF(boxX, boxY, boxX + boxW, boxY + boxH)
        canvas.drawRoundRect(rect, 12f, 12f, bgPaint)
        canvas.drawRoundRect(rect, 12f, 12f, stroke)

        val textX = rect.left + padH
        val textY = rect.top + padV - textPaint.fontMetrics.ascent
        canvas.drawText(label, textX, textY, textPaint)
    }

    // 既定のオフセットは使わない（描画時に自前でずらしている）
    override fun getOffset(): com.github.mikephil.charting.utils.MPPointF =
        com.github.mikephil.charting.utils.MPPointF(0f, 0f)
    override fun getOffsetForDrawingAtPoint(
        posX: Float, posY: Float
    ): com.github.mikephil.charting.utils.MPPointF = getOffset()
}


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
