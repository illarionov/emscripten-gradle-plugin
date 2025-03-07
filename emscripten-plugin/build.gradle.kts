/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    `kotlin-dsl-base`
    alias(libs.plugins.gradle.maven.publish.plugin.base)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
    id("at.released.builder.emscripten.buildlogic.project.doc.subproject")
}

group = "at.released.builder.emscripten"
version = "0.1-alpha01"

private val internalEmscriptenApiMarker = "at.released.builder.emscripten.InternalEmscriptenApi"

kotlin {
    explicitApi = ExplicitApiMode.Warning
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
        apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
        freeCompilerArgs.addAll("-Xjvm-default=all")
        optIn = listOf(internalEmscriptenApiMarker)
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_11
}

apiValidation {
    nonPublicMarkers.add(internalEmscriptenApiMarker)
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.assertk)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.platform.launcher)
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

gradlePlugin {
    website = "https://github.com/illarionov/emscripten-gradle-plugin"
    vcsUrl = "https://github.com/illarionov/emscripten-gradle-plugin"
}

mavenPublishing {
    configure(GradlePlugin(javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml")))
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    publishing {
        repositories {
            maven {
                name = "PixnewsS3"
                setUrl("s3://maven.pixnews.ru/")
                credentials(AwsCredentials::class) {
                    accessKey = providers.environmentVariable("YANDEX_S3_ACCESS_KEY_ID").getOrElse("")
                    secretKey = providers.environmentVariable("YANDEX_S3_SECRET_ACCESS_KEY").getOrElse("")
                }
            }
        }
    }
    signAllPublications()
    extensions.getByType(SigningExtension::class.java).isRequired = false

    pom {
        name.set(project.name)
        description.set("Helpers for running Emscripten tasks from Gradle.")
        url.set("https://github.com/illarionov/emscripten-gradle-plugin")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("illarionov")
                name.set("Alexey Illarionov")
                email.set("alexey@0xdc.ru")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/illarionov/emscripten-gradle-plugin.git")
            developerConnection.set("scm:git:ssh://github.com:illarionov/emscripten-gradle-plugin.git")
            url.set("https://github.com/illarionov/emscripten-gradle-plugin/tree/main")
        }
    }
}
