import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
    application
    id("com.gradleup.shadow")
    id("com.google.devtools.ksp")
    id("org.graalvm.buildtools.native")
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
    }
}

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

configurations {
    all {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
}

dependencies {
    implementation(platform(Http4k.bom))
    implementation(Http4k.core)
    implementation("org.http4k:http4k-config:_")
    implementation(Http4k.format.moshi)
    implementation("org.http4k:http4k-server-helidon:_")
    implementation("io.helidon.webserver:helidon-webserver-http2:_")
    implementation(Kotlin.stdlib)
    implementation("se.ansman.kotshi:api:_")
    ksp("se.ansman.kotshi:compiler:_")
    testImplementation(Http4k.testing.approval)
    testImplementation(Http4k.testing.hamkrest)
    testImplementation(Testing.junit.jupiter.api)
    testImplementation(Testing.junit.jupiter.engine)
}