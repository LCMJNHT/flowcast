plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.flowcast.demo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.flowcast.demo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        val flowcastBaseUrl = providers.gradleProperty("FLOWCAST_BASE_URL")
            .orElse("http://10.0.2.2:8000")
            .get()
        val pexelsApiKey = providers.gradleProperty("PEXELS_API_KEY")
            .orElse("SWsNFNttofDIR7Ox3owadTVWKwQ7RHvIfxSt153FgDZKjvSWc0j20bAa")
            .get()
        val pexelsVideoBaseUrl = providers.gradleProperty("PEXELS_VIDEO_BASE_URL")
            .orElse("https://api.pexels.com/v1/videos")
            .get()
        buildConfigField("String", "FLOWCAST_BASE_URL", "\"$flowcastBaseUrl\"")
        buildConfigField("String", "PEXELS_API_KEY", "\"$pexelsApiKey\"")
        buildConfigField("String", "PEXELS_VIDEO_BASE_URL", "\"$pexelsVideoBaseUrl\"")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
