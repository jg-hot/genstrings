plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("io.genstrings.plugin")
}

android {
    namespace = "io.genstrings.sample"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        targetSdk = 35
    }
}
