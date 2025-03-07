/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    // `kotlin-dsl-base`
    `kotlin-dsl`
    alias(libs.plugins.gradle.maven.publish.plugin.base)
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
}

group = "at.released.builder.emscripten"
version = "0.1-SNAPSHOT"

kotlin {
    explicitApi = ExplicitApiMode.Warning
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
        apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
        freeCompilerArgs.addAll("-Xjvm-default=all")
        optIn = listOf("at.released.builder.emscripten.InternalEmscriptenApi")
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.assertk)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
    configureTestTaskDefaults()
}

private fun Test.configureTestTaskDefaults() {
    maxHeapSize = "1512M"
    jvmArgs = listOf("-XX:MaxMetaspaceSize=768M")
    testLogging {
        if (providers.gradleProperty("verboseTest").map(String::toBoolean).getOrElse(false)) {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.STANDARD_ERROR, TestLogEvent.STANDARD_OUT)
        } else {
            events = setOf(TestLogEvent.FAILED)
        }
    }
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = providers.environmentVariable("TEST_JDK_VERSION")
            .map { JavaLanguageVersion.of(it.toInt()) }
            .orElse(JavaLanguageVersion.of(21))
    }
}
