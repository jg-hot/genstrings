plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

group = "io.genstrings"
version = "0.1.2"

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
    implementation(libs.ktor)
    implementation(libs.openai)
    testImplementation(kotlin("test"))
}

publishing {
    val githubPackagesUrl = "https://maven.pkg.github.com/jg-hot/genstrings"

    repositories {
        maven {
            name = "GithubPackages"
            url = uri(githubPackagesUrl)
            credentials {
                username = properties["gpr.user"]?.toString()
                password = properties["gpr.key"]?.toString()
            }
        }
    }
}

// currently you will need to run :plugin:test --rerun and check the HTML report manually
tasks.test {
    useJUnitPlatform()
}
