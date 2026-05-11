import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Pulls publisher / ad-unit IDs from `samples/quickstart/local.properties`
// (gitignored). Falls back to public placeholders so the build is green
// for fresh clones — replace the placeholders with real IDs locally to
// see live ads, without committing them.
val sampleProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun localOr(key: String, default: String): String =
    sampleProps.getProperty(key)?.takeIf { it.isNotBlank() } ?: default

// Escape values bound for `buildConfigField "String", ..., "\"$x\""` so a
// stray backslash, quote, or newline in local.properties doesn't generate
// invalid BuildConfig source.
fun escapeForBuildConfig(raw: String): String =
    raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")

val growlPublisherId = localOr("elo.publisherId", "YOUR_PUBLISHER_ID")
val growlAdUnitId    = localOr("elo.adUnitId",    "YOUR_AD_UNIT_ID")
// Defaults are Google's official AdMob test IDs (always-fill, safe to commit).
// See https://developers.google.com/admob/android/test-ads — the native ad unit
// is what the SDK requests. Override in local.properties for production.
val admobAppId       = localOr("admob.appId",    "ca-app-pub-3940256099942544~3347511713")
val admobAdUnitId    = localOr("admob.adUnitId", "ca-app-pub-3940256099942544/2247696110")

android {
    namespace = "ad.elo.quickstart"
    compileSdk = 36

    defaultConfig {
        applicationId = "ad.elo.quickstart"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "GROWL_PUBLISHER_ID", "\"${escapeForBuildConfig(growlPublisherId)}\"")
        buildConfigField("String", "GROWL_AD_UNIT_ID",   "\"${escapeForBuildConfig(growlAdUnitId)}\"")
        buildConfigField("String", "ADMOB_APP_ID",       "\"${escapeForBuildConfig(admobAppId)}\"")
        buildConfigField("String", "ADMOB_AD_UNIT_ID",   "\"${escapeForBuildConfig(admobAdUnitId)}\"")
        manifestPlaceholders["admobAppId"] = admobAppId
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")

    // The Elo SDK — the version is bumped by the update-dist-repo job after each Maven publish.
    implementation("ad.elo:elo-android-sdk:2.4.0")

    // AdMob mediation adapter — first-party adapter that participates in
    // Elo's parallel auction. Ships from the same SDK release pipeline.
    implementation("ad.elo:elo-android-mediation-admob:0.0.3")
}
