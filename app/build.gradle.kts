plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

// Default DummyJSON demo API. Override for another server with:
// ./gradlew ... -PEVENTS_BASE_URL=https://your-api.example/
val eventsBaseUrl = (providers.gradleProperty("EVENTS_BASE_URL").orNull
    ?: "https://dummyjson.com/")
    .let { if (it.endsWith("/")) it else "$it/" }

android {
    namespace = "com.bekaryab.qadamevents"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.bekaryab.qadamevents"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "EVENTS_BASE_URL", "\"$eventsBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(
        platform("com.google.firebase:firebase-bom:35.0.0")
    )

    // Firebase Authentication
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-auth")

// Firebase Task-тарды coroutine арқылы күту
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.11.0")

// ViewModel және viewModelScope
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("com.google.firebase:firebase-auth")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
// AndroidX Media3 ExoPlayer for local Reels video playback
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
// Retrofit + OkHttp for REST API integration
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}