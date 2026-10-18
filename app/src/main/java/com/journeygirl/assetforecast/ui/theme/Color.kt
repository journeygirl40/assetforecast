package com.journeygirl.assetforecast.ui.theme

import androidx.compose.ui.graphics.Color

// ── ダーク基調（証券/資産管理アプリ風）のメインパレット ──
val BgDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1C1C1E)
val SurfaceVariantDark = Color(0xFF2A2A2E)
// outline＝入力枠など「ここに触れる」を示す線なので背景からはっきり浮く明るさにする。
// 区切り線など控えめに見せたい線はoutlineVariantを使う。
val OutlineDark = Color(0xFF6A6A72)
val OutlineVariantDark = Color(0xFF38383C)
// ダイアログ・メニュー等はsurfaceContainer系を背景に使う。未指定だとMaterial既定の紫がかった色になる。
val SurfaceContainerLowestDark = Color(0xFF0E0E0E)
val SurfaceContainerLowDark = Color(0xFF18181A)
val SurfaceContainerDark = Color(0xFF1C1C1E)
val SurfaceContainerHighDark = Color(0xFF242428)
val SurfaceContainerHighestDark = Color(0xFF2E2E33)
val OnDark = Color(0xFFECECEC)
val OnDarkMuted = Color(0xFFA0A0A6)

// アクセント：資産の伸び＝グリーン、予測・情報系＝ブルー
val AccentGreen = Color(0xFF00E08C)
val OnAccentGreen = Color(0xFF00341F)
val AccentGreenContainer = Color(0xFF00573A)
val OnAccentGreenContainer = Color(0xFF6DFFC3)
val AccentBlue = Color(0xFF4DA3FF)
val OnAccentBlue = Color(0xFF00264D)
val AccentBlueContainer = Color(0xFF00437A)
val OnAccentBlueContainer = Color(0xFFCDE5FF)
val ErrorContainerDark = Color(0xFF5C1A1A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// ── ライトテーマ（同系統のトーンで統一） ──
val BgLight = Color(0xFFF7F8FA)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFECEEF1)
val OutlineLight = Color(0xFF8E9399)
val OutlineVariantLight = Color(0xFFDADCE0)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF7F8FA)
val SurfaceContainerLight = Color(0xFFF1F3F5)
val SurfaceContainerHighLight = Color(0xFFFFFFFF)
val SurfaceContainerHighestLight = Color(0xFFF1F3F5)
val OnLight = Color(0xFF1A1C1E)
val OnLightMuted = Color(0xFF5F6368)
val AccentGreenLight = Color(0xFF00875A)
val AccentGreenContainerLight = Color(0xFFC6F3DD)
val OnAccentGreenContainerLight = Color(0xFF00361F)
val AccentBlueLight = Color(0xFF2979FF)
val AccentBlueContainerLight = Color(0xFFD6E5FF)
val OnAccentBlueContainerLight = Color(0xFF00205C)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

// グラフ・リストの増減表示色（Material色とは別軸：日本の会計慣習に合わせプラス=赤、マイナス=青で据え置き）
// ダーク背景では明るめ、ライト背景では濃いめにして、どちらでも文字として読める明度差を確保する。
val GainRed = Color(0xFFEF4444)
val LossBlue = Color(0xFF4DA3FF)
val GainRedLight = Color(0xFFD32F2F)
val LossBlueLight = Color(0xFF1565C0)

// グラフの実績線・予測線の初期色。テーマ（ダーク/ライト）に関係なく同じ色を使う。
// どちらの背景（#121212 / #F7F8FA / 白）に対してもコントラスト比3:1以上になる中間の明るさを選び、
// 色相は正反対の青×オレンジにして2本の線を見分けやすくしている（色覚の個人差にも強い組み合わせ）。
val DefaultActualLine = Color(0xFF2F80ED)
val DefaultForecastLine = Color(0xFFD96A00)
