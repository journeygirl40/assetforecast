package com.journeygirl.assetforecast.data.backup

import com.journeygirl.assetforecast.data.db.AssetRecord
import com.journeygirl.assetforecast.data.prefs.ExpenseRepeat
import com.journeygirl.assetforecast.data.prefs.ForecastSettings
import com.journeygirl.assetforecast.data.prefs.ScheduledExpense
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// CSVのインポート/エクスポート（記録・定期的な収支・設定を1ファイルにまとめる）。
//
// 形式：先頭は従来どおりの記録の表（date,amount,note）。続けて「#scheduled」「#settings」の見出し行で始まる
// セクションを置く。見出し行や設定の行は日付として読めないため、旧バージョンのアプリで読み込んでも
// 記録以外は読み飛ばされる（記録だけは従来どおり取り込める）。逆に旧形式のCSV（記録だけ）もそのまま読める。

private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
private const val SECTION_SCHEDULED = "#scheduled"
private const val SECTION_SETTINGS = "#settings"

data class ImportedRecord(val date: LocalDate, val amount: Long, val note: String?)

data class BackupData(
    val records: List<ImportedRecord>,
    val scheduled: List<ScheduledExpense>,
    // ファイルに設定のセクションがあった時だけ、現在の設定にファイルの値を上書きしたもの
    val settings: ForecastSettings?
)

fun buildBackupCsv(
    records: List<AssetRecord>,
    scheduled: List<ScheduledExpense>,
    settings: ForecastSettings
): String {
    val sb = StringBuilder()
    sb.appendLine("date,amount,note")
    records.sortedBy { it.dateEpochDays }.forEach { r ->
        val date = LocalDate.ofEpochDay(r.dateEpochDays.toLong()).format(DATE_FMT)
        sb.appendLine("$date,${r.amount},${quote(r.note ?: "")}")
    }

    sb.appendLine()
    sb.appendLine(SECTION_SCHEDULED)
    sb.appendLine("name,type,amount,month,repeat,start_year,years,enabled")
    scheduled.forEach { e ->
        sb.appendLine(
            listOf(
                quote(e.name),
                if (e.isIncome) "income" else "expense",
                e.amount.toString(),
                e.month.toString(),
                e.repeat.name.lowercase(),
                e.startYear.toString(),
                e.years.toString(),
                e.enabled.toString()
            ).joinToString(",")
        )
    }

    // 設定は「キー,値」で並べる（値が空＝未設定）。広告非表示はファイルの書き換えで有効にできないよう含めない
    sb.appendLine()
    sb.appendLine(SECTION_SETTINGS)
    sb.appendLine("key,value")
    settingsToPairs(settings).forEach { (k, v) -> sb.appendLine("$k,${quote(v ?: "")}") }
    return sb.toString()
}

fun parseBackupCsv(text: String, current: ForecastSettings): BackupData {
    val records = mutableListOf<ImportedRecord>()
    val scheduled = mutableListOf<ScheduledExpense>()
    val settingValues = mutableMapOf<String, String>()
    var section = "records"

    text.lineSequence().forEach { raw ->
        val line = raw.trim().removePrefix("﻿")   // Excel等で付くBOMを除く
        if (line.isBlank()) return@forEach
        when (line.lowercase().substringBefore(',')) {
            SECTION_SCHEDULED -> { section = "scheduled"; return@forEach }
            SECTION_SETTINGS -> { section = "settings"; return@forEach }
        }
        val cols = parseCsvLine(line)
        when (section) {
            "records" -> {
                val date = parseDate(cols.getOrNull(0)) ?: return@forEach   // ヘッダー行もここで飛ぶ
                val amount = cols.getOrNull(1)?.trim()?.toLongOrNull() ?: return@forEach
                records += ImportedRecord(date, amount, cols.getOrNull(2)?.trim()?.ifBlank { null })
            }
            "scheduled" -> parseScheduled(cols)?.let { scheduled += it }
            "settings" -> {
                val key = cols.getOrNull(0)?.trim() ?: return@forEach
                if (key == "key") return@forEach
                settingValues[key] = cols.getOrNull(1)?.trim() ?: ""
            }
        }
    }
    return BackupData(
        records = records,
        scheduled = scheduled,
        settings = if (settingValues.isEmpty()) null else applySettings(current, settingValues)
    )
}

private fun parseScheduled(cols: List<String>): ScheduledExpense? {
    if (cols.size < 7) return null
    val amount = cols[2].trim().toLongOrNull()?.takeIf { it > 0 } ?: return null   // ヘッダー行もここで飛ぶ
    val month = cols[3].trim().toIntOrNull()?.takeIf { it in 1..12 } ?: return null
    val repeat = runCatching { ExpenseRepeat.valueOf(cols[4].trim().uppercase()) }.getOrNull() ?: return null
    val startYear = cols[5].trim().toIntOrNull() ?: return null
    return ScheduledExpense(
        id = 0L,
        name = cols[0].trim(),
        amount = amount,
        month = month,
        startYear = startYear,
        repeat = repeat,
        years = cols[6].trim().toIntOrNull()?.coerceAtLeast(1) ?: 1,
        enabled = cols.getOrNull(7)?.trim()?.lowercase() != "false",
        isIncome = cols[1].trim().lowercase() == "income"
    )
}

// 年率は画面と同じ「%/年」で書き出す（保存値は月率なので×1200。画面と同じく小数5桁まで）
private fun settingsToPairs(s: ForecastSettings): List<Pair<String, String?>> = listOf(
    "forecast_months" to s.months.toString(),
    "annual_rate_percent" to s.customRatePerMonth?.let {
        BigDecimal(it * 1200.0).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    },
    "monthly_add" to s.customDeltaPerMonth?.toString(),
    "drawdown_start_month" to s.drawdownStartMonth?.toString(),
    "withdraw_per_month" to s.withdrawPerMonth?.toString(),
    "mode" to s.mode,
    "sample_n" to s.sampleN.toString(),
    "theme_mode" to s.themeMode,
    "language" to s.language,
    "color_gain" to s.colorGain,
    "color_loss" to s.colorLoss,
    "color_actual" to s.colorActual,
    "color_forecast" to s.colorForecast
)

// ファイルにあるキーだけを現在の設定に上書きする（無いキーや読めない値は今の設定のまま）
private fun applySettings(current: ForecastSettings, v: Map<String, String>): ForecastSettings {
    var s = current
    fun has(k: String) = v.containsKey(k)
    fun str(k: String) = v[k]?.ifBlank { null }
    if (has("forecast_months")) str("forecast_months")?.toIntOrNull()?.takeIf { it in 0..1200 }?.let { s = s.copy(months = it) }
    if (has("annual_rate_percent")) s = s.copy(
        customRatePerMonth = str("annual_rate_percent")?.toBigDecimalOrNull()?.let {
            it.divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal(12), 10, RoundingMode.HALF_UP).toDouble()
        }
    )
    if (has("monthly_add")) s = s.copy(customDeltaPerMonth = str("monthly_add")?.toLongOrNull())
    if (has("drawdown_start_month")) s = s.copy(drawdownStartMonth = str("drawdown_start_month")?.toIntOrNull())
    if (has("withdraw_per_month")) s = s.copy(withdrawPerMonth = str("withdraw_per_month")?.toLongOrNull())
    str("mode")?.let { s = s.copy(mode = it) }
    str("sample_n")?.toIntOrNull()?.let { s = s.copy(sampleN = it) }
    str("theme_mode")?.takeIf { it in listOf("system", "light", "dark") }?.let { s = s.copy(themeMode = it) }
    str("language")?.let { s = s.copy(language = it) }
    if (has("color_gain")) s = s.copy(colorGain = str("color_gain"))
    if (has("color_loss")) s = s.copy(colorLoss = str("color_loss"))
    if (has("color_actual")) s = s.copy(colorActual = str("color_actual"))
    if (has("color_forecast")) s = s.copy(colorForecast = str("color_forecast"))
    return s
}

private fun parseDate(s: String?): LocalDate? {
    val t = s?.trim() ?: return null
    return runCatching { LocalDate.parse(t, DATE_FMT) }.getOrNull()
        ?: runCatching { LocalDate.parse(t) }.getOrNull()   // 旧形式「yyyy-MM-dd」
}

// カンマや"を含んでも壊れないよう、文字列は常に"で囲み、中の"は""にする
private fun quote(s: String): String = "\"" + s.replace("\"", "\"\"") + "\""

// かんたんRFC4180対応（"で囲まれたカンマ、""のエスケープ対応）
private fun parseCsvLine(line: String): List<String> {
    val out = mutableListOf<String>()
    var cur = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val ch = line[i]
        if (ch == '"') {
            if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                cur.append('"'); i += 2; continue
            }
            inQuotes = !inQuotes
        } else if (ch == ',' && !inQuotes) {
            out += cur.toString(); cur = StringBuilder()
        } else {
            cur.append(ch)
        }
        i++
    }
    out += cur.toString()
    return out
}
