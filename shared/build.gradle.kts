plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    android {
        namespace = "com.example.liquidglasslab.shared"
        compileSdk = 36
        minSdk = 24
    }

    sourceSets {
        commonMain.dependencies {
        }
    }
}
