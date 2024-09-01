import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.0.0"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
    }
}

val http4kVersion: String by project
//val http4kConnectVersion: String by project
val junitVersion: String by project
val kotlinVersion: String by project

application {
    mainClass = "org.aaron.TestServerKt"
}

tasks {
    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier = null
        archiveVersion = null
        mergeServiceFiles()
        dependsOn(distTar, distZip)
        isZip64 = true
    }
}

repositories {
    mavenCentral()
}

apply(plugin = "kotlin")

tasks {
    withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors = false
            jvmTarget.set(JVM_21)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    java {
        sourceCompatibility = VERSION_21
        targetCompatibility = VERSION_21
    }
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-core:${http4kVersion}")
    implementation("org.http4k:http4k-config:${http4kVersion}")
    implementation("org.http4k:http4k-format-jackson:${http4kVersion}")
    implementation("org.http4k:http4k-server-helidon:${http4kVersion}")
    // https://mvnrepository.com/artifact/io.helidon.webserver/helidon-webserver-http2
    implementation("io.helidon.webserver:helidon-webserver-http2:4.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    testImplementation("org.http4k:http4k-testing-approval:${http4kVersion}")
    testImplementation("org.http4k:http4k-testing-hamkrest:${http4kVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

