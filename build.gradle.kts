/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("at.released.builder.emscripten.buildlogic.project.lint.detekt")
    id("at.released.builder.emscripten.buildlogic.project.lint.spotless")
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.gradle.maven.publish.plugin.base) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

tasks.register("styleCheck") {
    group = "Verification"
    description = "Runs code style checking tools (excluding tests)"
    dependsOn("detektCheck", "spotlessCheck")
}
