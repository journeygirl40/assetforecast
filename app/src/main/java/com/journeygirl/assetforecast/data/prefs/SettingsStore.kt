package com.journeygirl.assetforecast.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore("app_settings")

private const val DEFAULT_ANNUAL_RATE = 0.04          // 年率4%
private const val DEFAULT_MONTHLY_RATE = DEFAULT_ANNUAL_RATE / 12.0

data class ForecastSettings(
    val mode: String = "custom",            // 既定：年率指定
    val sampleN: Int = -1,                  // 直近Nヵ月平均を使う場合の既定：全件
    val months: Int = 60,                   // 12..600
    val customDeltaPerMonth: Long? = null,  // 固定の増減額（円/月）
    val customRatePerMonth: Double? = DEFAULT_MONTHLY_RATE, // 既定：年率4% → 月率
    val language: String = "system",        // "system" / "ja" / "en"
    val drawdownStartMonth: Int? = null,
    val withdrawPerMonth: Long? = null,
    val adFree: Boolean = false
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val MODE = stringPreferencesKey("forecast_mode")
        val SAMPLE_N = intPreferencesKey("forecast_sample_n")
        val MONTHS = intPreferencesKey("forecast_months")
        val CUSTOM_DELTA = longPreferencesKey("forecast_custom_delta")
        val CUSTOM_RATE = doublePreferencesKey("forecast_custom_rate_monthly") // ← 重要
        val LANG = stringPreferencesKey("app_language")
        val AD_FREE = booleanPreferencesKey("ad_free")
        val DRAWDOWN_START_MONTH = intPreferencesKey("drawdown_start_month")
        val WITHDRAW_PER_MONTH   = longPreferencesKey("withdraw_per_month")
    }

    val settingsFlow: Flow<ForecastSettings> =
        context.dataStore.data.map { p ->
            ForecastSettings(
                mode = p[Keys.MODE] ?: "custom",
                sampleN = p[Keys.SAMPLE_N] ?: -1,
                months = p[Keys.MONTHS] ?: 60,
                customDeltaPerMonth = p[Keys.CUSTOM_DELTA],
                customRatePerMonth = p[Keys.CUSTOM_RATE] ?: DEFAULT_MONTHLY_RATE,
                drawdownStartMonth = p[Keys.DRAWDOWN_START_MONTH],      // ← 追加
                withdrawPerMonth   = p[Keys.WITHDRAW_PER_MONTH],         // ← 追加
                language = p[Keys.LANG] ?: "system",
                adFree = p[Keys.AD_FREE] ?: false
            )
        }

    suspend fun save(s: ForecastSettings) {
        context.dataStore.edit { p ->
            p[Keys.MODE] = s.mode
            p[Keys.SAMPLE_N] = s.sampleN
            p[Keys.MONTHS] = s.months

            // 固定額
            if (s.customDeltaPerMonth == null) {
                p.remove(Keys.CUSTOM_DELTA)
            } else {
                p[Keys.CUSTOM_DELTA] = s.customDeltaPerMonth
            }

            // 月率(Double)
            if (s.customRatePerMonth == null) {
                p.remove(Keys.CUSTOM_RATE)
            } else {
                p[Keys.CUSTOM_RATE] = s.customRatePerMonth
            }
            // 切り崩し開始「月」: null のときはキー削除、値ありなら保存
            if (s.drawdownStartMonth == null) {
                p.remove(Keys.DRAWDOWN_START_MONTH)
            } else {
                p[Keys.DRAWDOWN_START_MONTH] = s.drawdownStartMonth
            }

            // 毎月の切り崩し金額: null のときはキー削除、値ありなら保存
            if (s.withdrawPerMonth == null) {
                p.remove(Keys.WITHDRAW_PER_MONTH)
            } else {
                p[Keys.WITHDRAW_PER_MONTH] = s.withdrawPerMonth
            }
            p[Keys.LANG] = s.language
            p[Keys.AD_FREE] = s.adFree
        }
    }
}
