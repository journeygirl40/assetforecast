package com.journeygirl.assetforecast.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.journeygirl.assetforecast.data.db.AppDatabase
import com.journeygirl.assetforecast.data.db.AssetRecord
import com.journeygirl.assetforecast.data.repo.AssetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.journeygirl.assetforecast.data.prefs.SettingsStore
import com.journeygirl.assetforecast.data.prefs.ForecastSettings
import com.journeygirl.assetforecast.data.prefs.DisplayRangePrefs
import kotlinx.coroutines.flow.map
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AssetRepository(AppDatabase.get(app).assetDao())

    // ★ 追加：設定ストア
    private val settingsStore = SettingsStore(app)

    val records = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ★ 追加：設定の State
    val settings = settingsStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ForecastSettings())

    // グラフのミニマップナビゲーターで選択中の表示範囲（アプリ再起動後も復元する）。
    // 初期値はnull＝「DataStoreからまだ読み込んでいない」を表す。これをDisplayRangePrefs(null,null)
    // （＝保存済みだが値が無い＝全期間）にしてしまうと、復元側が「初期値で確定」と誤判定して
    // 実際の保存値のemitを無視してしまう（起動のたびに保存範囲が復元されないバグになる）。
    val displayRange: kotlinx.coroutines.flow.StateFlow<DisplayRangePrefs?> = settingsStore.displayRangeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun addRecord(date: LocalDate, amount: Long, note: String?) {
        viewModelScope.launch {
            repo.insert(
                AssetRecord(
                    dateEpochDays = date.toEpochDay().toInt(),
                    amount = amount,
                    note = note
                )
            )
        }
    }
    fun updateRecord(id: Long, date: LocalDate, amount: Long, note: String?) {
        viewModelScope.launch {
            // 既存レコードのIDを保持したまま更新
            val updated = com.journeygirl.assetforecast.data.db.AssetRecord(
                id = id,
                dateEpochDays = date.toEpochDay().toInt(),
                amount = amount,
                note = note
            )
            repo.update(updated)
        }
    }

    fun deleteRecord(record: com.journeygirl.assetforecast.data.db.AssetRecord) {
        viewModelScope.launch { repo.delete(record) }
    }
    fun saveSettings(newSettings: ForecastSettings) {
        viewModelScope.launch {
            settingsStore.save(newSettings)
        }
    }
    fun saveDisplayRange(start: Int?, end: Int?) {
        viewModelScope.launch {
            settingsStore.saveDisplayRange(start, end)
        }
    }
    fun applyLanguage(tag: String) {
        val locales = if (tag == "system") {
            LocaleListCompat.getEmptyLocaleList() // 端末設定に従う
        } else {
            LocaleListCompat.forLanguageTags(tag) // "ja" or "en"
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
