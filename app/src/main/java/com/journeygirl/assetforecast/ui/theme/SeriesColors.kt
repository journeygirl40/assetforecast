package com.journeygirl.assetforecast.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── 増加/減少/実績線/予測線の、ユーザーが変更できる色 ──
// 設定には「どの色を選んだか」を文字列で保存する:
//   null        … 初期色
//   "p:<id>"    … プリセット（ダーク/ライトそれぞれで読みやすい色を持つので、テーマを切り替えても見やすい）
//   "c:<ARGB16進>" … 自由に選んだ色（ダーク/ライト共通の1色）

// プリセット1色分。ダーク背景では明るめ、ライト背景では濃いめのトーンを使う。
data class PresetColor(val id: String, val name: String, val dark: Color, val light: Color)

val ColorPresets = listOf(
    PresetColor("red", "赤", Color(0xFFEF4444), Color(0xFFD32F2F)),
    PresetColor("orange", "オレンジ", Color(0xFFFB923C), Color(0xFFE65100)),
    PresetColor("yellow", "黄", Color(0xFFFACC15), Color(0xFFB28704)),
    PresetColor("lime", "黄緑", Color(0xFFA3E635), Color(0xFF558B2F)),
    PresetColor("green", "緑", Color(0xFF00E08C), Color(0xFF00875A)),
    PresetColor("teal", "青緑", Color(0xFF2DD4BF), Color(0xFF00796B)),
    PresetColor("sky", "水色", Color(0xFF38BDF8), Color(0xFF0277BD)),
    PresetColor("blue", "青", Color(0xFF4DA3FF), Color(0xFF1565C0)),
    PresetColor("indigo", "藍", Color(0xFF818CF8), Color(0xFF3949AB)),
    PresetColor("purple", "紫", Color(0xFFC084FC), Color(0xFF7B1FA2)),
    PresetColor("pink", "ピンク", Color(0xFFF472B6), Color(0xFFC2185B)),
    PresetColor("gray", "グレー", Color(0xFFA0A0A6), Color(0xFF616161)),
)

fun presetChoice(id: String) = "p:$id"
fun customChoice(color: Color) = "c:%08X".format(colorToArgb(color))

private fun colorToArgb(c: Color): Int {
    fun ch(v: Float) = (v * 255f + 0.5f).toInt().coerceIn(0, 255)
    return (ch(c.alpha) shl 24) or (ch(c.red) shl 16) or (ch(c.green) shl 8) or ch(c.blue)
}

// 保存値 → 実際の色。解釈できない値・null は fallback（初期色）にする。
fun resolveColorChoice(choice: String?, isDark: Boolean, fallback: Color): Color {
    if (choice == null) return fallback
    return when {
        choice.startsWith("p:") -> ColorPresets.firstOrNull { it.id == choice.removePrefix("p:") }
            ?.let { if (isDark) it.dark else it.light } ?: fallback
        choice.startsWith("c:") -> choice.removePrefix("c:").toLongOrNull(16)
            ?.let { Color(it.toInt()) } ?: fallback
        else -> fallback
    }
}

// 画面全体で使う系列色。AssetforecastTheme が設定値から解決して提供する。
data class SeriesColors(
    val gain: Color,
    val loss: Color,
    val actual: Color,
    val forecast: Color
)

// 設定に保存された4色の選択（nullは初期色）
data class SeriesColorChoices(
    val gain: String? = null,
    val loss: String? = null,
    val actual: String? = null,
    val forecast: String? = null
)

// 一括切り替え。増減の2色だけを変える（線の色はそのまま）。
val JapaneseStyleChoices = presetChoice("red") to presetChoice("blue")   // 増=赤 / 減=青
val WesternStyleChoices = presetChoice("green") to presetChoice("red")   // 増=緑 / 減=赤

val LocalSeriesColors = staticCompositionLocalOf {
    SeriesColors(gain = GainRed, loss = LossBlue, actual = DefaultActualLine, forecast = DefaultForecastLine)
}
