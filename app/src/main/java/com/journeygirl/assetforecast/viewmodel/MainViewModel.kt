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

data class ImportResult(val addedRecords: Int, val addedScheduled: Int, val settingsApplied: Boolean)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AssetRepository(AppDatabase.get(app).assetDao())

    // ★ 追加：設定ストア
    private val settingsStore = SettingsStore(app)

    val records = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ★ 追加：設定の State
    val settings = settingsStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ForecastSettings())
    // 読み込み前はnull。「読み込み済みか」と「その値」を同じStateFlowから取るために使う
    // （settingsとは別に収集すると、どちらが先に更新されるか保証されず、初期値で処理してしまうことがある）
    val loadedSettings: kotlinx.coroutines.flow.StateFlow<ForecastSettings?> = settingsStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

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
    // 予測に反映する定期的な支出（ローンの支払いなど）
    val scheduledExpenses = settingsStore.scheduledExpensesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun saveScheduledExpenses(list: List<com.journeygirl.assetforecast.data.prefs.ScheduledExpense>) {
        viewModelScope.launch { settingsStore.saveScheduledExpenses(list) }
    }

    // CSVの読み込み結果を取り込む。記録・定期的な収支は、すでに同じ内容のものがあれば追加しない
    // （同じファイルを2回読んでも重複しないように）。設定はファイルにあったものだけ上書きする。
    fun importBackup(
        data: com.journeygirl.assetforecast.data.backup.BackupData,
        onDone: (ImportResult) -> Unit
    ) {
        viewModelScope.launch {
            val known = records.value
                .map { Triple(it.dateEpochDays, it.amount, it.note?.ifBlank { null }) }
                .toMutableSet()
            var addedRecords = 0
            data.records.forEach { r ->
                val epochDays = r.date.toEpochDay().toInt()
                if (known.add(Triple(epochDays, r.amount, r.note))) {
                    repo.insert(AssetRecord(dateEpochDays = epochDays, amount = r.amount, note = r.note))
                    addedRecords++
                }
            }

            // idとON/OFF以外が同じなら同じ項目とみなす
            fun key(e: com.journeygirl.assetforecast.data.prefs.ScheduledExpense) = e.copy(id = 0L, enabled = true)
            val current = scheduledExpenses.value
            val knownScheduled = current.map(::key).toMutableSet()
            var nextId = System.currentTimeMillis()
            val added = data.scheduled.filter { knownScheduled.add(key(it)) }.map { it.copy(id = nextId++) }
            if (added.isNotEmpty()) settingsStore.saveScheduledExpenses(current + added)

            // 広告非表示はファイルから変えさせない（今の値を保つ）
            data.settings?.let { settingsStore.save(it.copy(adFree = settings.value.adFree)) }

            onDone(ImportResult(addedRecords, added.size, data.settings != null))
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
