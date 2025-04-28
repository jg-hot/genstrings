plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

tasks.wrapper {
    gradleVersion = libs.versions.gradle.get()
    distributionType = Wrapper.DistributionType.ALL
}
