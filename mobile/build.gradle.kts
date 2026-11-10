plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.hrb116.waterlogged"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hrb116.waterlogged"
        minSdk = 30
        targetSdk = 36
        versionCode = 23
        versionName = "3.1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation("androidx.compose.material3:material3")
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    
    // Play Services Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Fix Lint Error for ActivityResult APIs
    implementation("androidx.fragment:fragment-ktx:1.8.1")

    // Play Services Wearable for syncing
    implementation(libs.play.services.wearable)
    
    // Health Connect
    implementation(libs.health.connect.client)
    
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}

