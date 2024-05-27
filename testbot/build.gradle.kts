import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.7.20"
}

repositories {
    mavenCentral()
    maven(url = "https://m2.dv8tion.net/releases")
}


group = "com.mrkirby153"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    implementation("net.dv8tion:JDA:5.0.0-beta.1")
    implementation("ch.qos.logback:logback-classic:1.4.6")
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}
kotlin {
    jvmToolchain(17)
}
