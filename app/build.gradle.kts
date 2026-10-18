import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"   // ★ 追加
    // id("org.jetbrains.kotlin.kapt")                      // ★ 使わないなら削除
}


// リリース署名の情報はプロジェクト直下の keystore.properties（git管理外）から読む。
// パスワードをビルドスクリプトやgitに残さないため。ファイルが無い/未記入なら署名なしでビルドされる。
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.reader(Charsets.UTF_8).use { load(it) }  // パスやパスワードに日本語が含まれても読めるようUTF-8で読む
}
val hasReleaseKeystore = keystoreProps.getProperty("storeFile")?.let { file(it).exists() } == true

android {
    namespace = "com.journeygirl.assetforecast"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.journeygirl.assetforecast"
        minSdk = 24
        targetSdk = 36  // Google Play: 2026/8/31以降のアップデートはAPI 36以上が必須
        versionCode = 23
        versionName = "4.0"
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (hasReleaseKeystore) signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures { compose = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true   // ★ これ！
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Unity Ads SDK を追加
    implementation("com.unity3d.ads:unity-ads:4.10.0")
// （将来的に広告メディエーションを使うならアダプタも追加可）
    implementation("com.google.ads.mediation:unity:4.16.3.0")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    // Compose core & Material3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")   // ←これ大事！

    // Lifecycle / ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-process:2.8.6")   // アプリ起動広告：アプリが前面に戻ったことの検知
    implementation("androidx.core:core-splashscreen:1.0.1")        // アプリ起動広告：起動中のスプラッシュ画面

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")                // ★ kapt→ksp に置換

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // その他
    implementation("androidx.core:core-ktx:1.13.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4") // ★ 追加（例）
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")  // ← 追加
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1") // 既にあればOK
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0") // ← これが Theme.Material3 を提供
    implementation("com.google.android.gms:play-services-ads:23.3.0")   // AdMob
    implementation("com.google.android.ump:user-messaging-platform:2.2.0") // 同意(必要地域)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3") // >= 1.2.0
    implementation("androidx.activity:activity-compose:1.9.0")


}
