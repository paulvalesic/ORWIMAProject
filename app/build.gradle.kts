plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.mediaappprojectfororwim"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mediaappprojectfororwim"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)

        // Firebase ovisnosti (koristeći libs)
        implementation(libs.firebase.auth.ktx)  // Firebase Authentication
        implementation(libs.firebase.database.ktx)  // Firebase Database

        // Coil za slike
        implementation("io.coil-kt:coil-compose:2.2.2")

        // Ostale ovisnosti

        implementation("com.google.accompanist:accompanist-systemuicontroller:0.26.2-beta")
        implementation("androidx.compose.ui:ui:1.5.0")
        implementation("androidx.compose.material3:material3:1.1.0")
        implementation("androidx.compose.foundation:foundation:1.5.0")
        implementation("androidx.compose.runtime:runtime:1.5.0")
        implementation("androidx.compose.material:material:1.5.0")
        implementation("androidx.navigation:navigation-compose:2.6.0")
        implementation("androidx.activity:activity-compose:1.7.2")
    }
