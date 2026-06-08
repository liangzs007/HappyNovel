plugins {
    id("com.android.application")
}

android {
    namespace = "com.happynovel"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.happynovel"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "HAPPYNOVEL_API_BASE_URL", "\"\"")
        buildConfigField("String", "ADMOB_READER_BANNER_AD_UNIT_ID", "\"\"")
        manifestPlaceholders["ADMOB_APP_ID"] = "ca-app-pub-3940256099942544~3347511713"
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-ads:24.9.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
