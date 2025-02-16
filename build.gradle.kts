import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("java")
    id("maven-publish")
    id("org.jetbrains.dokka") version "2.0.0"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://m2.dv8tion.net/releases")
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    implementation("org.json:json:20220924")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
    implementation("com.ibm.icu:icu4j:72.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    compileOnly("net.dv8tion:JDA:5.3.0")

    compileOnly("org.springframework:spring-context:6.0.0")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.7.5")
    compileOnly("org.springframework.data:spring-data-jpa:2.7.5")
    compileOnly("jakarta.persistence:jakarta.persistence-api:2.2.3")
}

group = "com.mrkirby153"

java {
    withSourcesJar()

}

kotlin {
    jvmToolchain(17)
}


fun publishUrl() = if (project.version.toString().endsWith("-SNAPSHOT")) {
    "https://repo.mrkirby153.com/repository/maven-snapshots/"
} else {
    "https://repo.mrkirby153.com/repository/maven-releases"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "mrkirby153"
            url = uri(publishUrl())
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }
}

tasks {
    withType<DokkaTask>().configureEach {
        suppressInheritedMembers.set(true)
    }
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    withType<KotlinCompile> {
        kotlinOptions {
            javaParameters = true
            // Experimental context receiver support
            freeCompilerArgs += "-Xcontext-receivers"
        }
    }
}