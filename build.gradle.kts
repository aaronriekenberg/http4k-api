import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
    application
    id("com.gradleup.shadow")
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

dependencies {
    implementation(platform(Http4k.bom))
    implementation(Http4k.core)
    implementation("org.http4k:http4k-config:_")
    implementation(Http4k.format.moshi)
    implementation(Http4k.server.jetty)
    implementation("org.eclipse.jetty.http2:jetty-http2-server:_")
    implementation("org.eclipse.jetty:jetty-unixdomain-server:_")
    implementation(Kotlin.stdlib)
    testImplementation(Http4k.testing.approval)
    testImplementation(Http4k.testing.hamkrest)
    testImplementation(Testing.junit.jupiter.api)
    testImplementation(Testing.junit.jupiter.engine)
}

