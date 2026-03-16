
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)

    kotlin("plugin.serialization") version "2.1.0" // Match your Kotlin version
}

group = "org.kobjects"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.cio.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    maven(
        url = "https://central.sonatype.com/repository/maven-snapshots/",
    )
    maven(
        url = "https://oss.sonatype.org/service/local/staging/deploy/maven2",
    )
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.kobjects.parsek:core:0.10.0")
    implementation("com.pi4j:pi4j-core:3.0.2")
    implementation("com.pi4j:pi4j-drivers:0.0.1-SNAPSHOT")
    implementation("com.pi4j:pi4j-plugin-raspberrypi:3.0.2")
    implementation("com.pi4j:pi4j-plugin-gpiod:3.0.2")
    implementation("com.pi4j:pi4j-plugin-linuxfs:3.0.2")
    implementation("io.github.davidepianca98:kmqtt-common-jvm:1.0.0")
    implementation("io.github.davidepianca98:kmqtt-client-jvm:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("io.ktor:ktor-client-websockets")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.kotlinx.html)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.cio)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
