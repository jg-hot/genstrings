plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "io.genstrings"
version = "1.0-SNAPSHOT"

gradlePlugin {
    plugins {
        create("genstrings") {
            id = "io.genstrings.plugin"
            implementationClass = "io.genstrings.GenstringsPlugin"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.agp.api)
    implementation(libs.jackson)
    implementation(libs.kaml)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
