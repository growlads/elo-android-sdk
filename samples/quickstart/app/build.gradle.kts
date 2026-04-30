plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.withgrowl.growlquickstart"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.withgrowl.growlquickstart"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    // GrowlAdView extends MaterialCardView from the Material Components library.
    // 2.2.3's POM declares it as a runtime dep; we pull it onto the compile
    // classpath here so AndroidView's factory can return GrowlAdView directly.
    implementation("com.google.android.material:material:1.12.0")

    // The SDK — the version is bumped by the update-dist-repo job after each Maven publish.
    implementation("com.withgrowl:growl-android-sdk:2.2.3")
}
