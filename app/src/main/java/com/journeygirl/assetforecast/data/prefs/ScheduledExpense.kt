package com.journeygirl.assetforecast.data.prefs

import org.json.JSONArray
import org.json.JSONObject

// 予測に反映する「決まった月の収支」の繰り返し方
enum class ExpenseRepeat {
    ONCE,           // 1回だけ（startYear年のmonth月）
    YEARLY_FOREVER, // startYear年から毎年ずっと
    YEARLY_YEARS    // startYear年から毎年、years年間
}

// 決まった月の支出・収入（例：ローンの支払いが毎年12月に5万円を10年間、ボーナスが毎年6月に30万円）。
// amountは正の数で持ち、isIncomeで向きを決める（支出なら予測から差し引き、収入なら足す）。
data class ScheduledExpense(
    val id: Long,
    val name: String,
    val amount: Long,
    val month: Int,          // 1..12
    val startYear: Int,
    val repeat: ExpenseRepeat,
    val years: Int = 1,      // YEARLY_YEARS の時だけ使う
    val enabled: Boolean = true,
    val isIncome: Boolean = false // false=支出（既存データはすべて支出）
) {
    // 予測の資産額に足す値（支出はマイナス）
    val signedAmount: Long get() = if (isIncome) amount else -amount

    // 最後の年（ずっと続く場合はnull）
    val endYear: Int?
        get() = when (repeat) {
            ExpenseRepeat.ONCE -> startYear
            ExpenseRepeat.YEARLY_FOREVER -> null
            ExpenseRepeat.YEARLY_YEARS -> startYear + years.coerceAtLeast(1) - 1
        }

    fun appliesTo(year: Int, month: Int): Boolean {
        if (!enabled || month != this.month || year < startYear) return false
        val end = endYear
        return end == null || year <= end
    }
}

// その年月に予測の資産額へ足す合計（収入はプラス、支出はマイナス）
fun List<ScheduledExpense>.netFor(year: Int, month: Int): Long =
    sumOf { if (it.appliesTo(year, month)) it.signedAmount else 0L }

// DataStoreには1つの文字列（JSON配列）として保存する
internal fun encodeScheduledExpenses(list: List<ScheduledExpense>): String {
    val arr = JSONArray()
    list.forEach { e ->
        arr.put(
            JSONObject()
                .put("id", e.id)
                .put("name", e.name)
                .put("amount", e.amount)
                .put("month", e.month)
                .put("startYear", e.startYear)
                .put("repeat", e.repeat.name)
                .put("years", e.years)
                .put("enabled", e.enabled)
                .put("isIncome", e.isIncome)
        )
    }
    return arr.toString()
}

internal fun decodeScheduledExpenses(json: String?): List<ScheduledExpense> {
    if (json.isNullOrBlank()) return emptyList()
    return runCatching {
        val arr = JSONArray(json)
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            ScheduledExpense(
                id = o.optLong("id"),
                name = o.optString("name"),
                amount = o.optLong("amount"),
                month = o.optInt("month", 1).coerceIn(1, 12),
                startYear = o.optInt("startYear"),
                repeat = runCatching { ExpenseRepeat.valueOf(o.optString("repeat")) }
                    .getOrDefault(ExpenseRepeat.ONCE),
                years = o.optInt("years", 1),
                enabled = o.optBoolean("enabled", true),
                isIncome = o.optBoolean("isIncome", false)
            )
        }
    }.getOrDefault(emptyList())
}
