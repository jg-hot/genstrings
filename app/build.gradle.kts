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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

genstrings {
    languages {
        create("es") {
            description = "Spanish"
        }
        create("b+zh+Hans") {
            description = "Chinese (Simplified Han)"
        }
        create("b+zh+Hant") {
            description = "Chinese (Traditional Han)"
        }
    }
}
