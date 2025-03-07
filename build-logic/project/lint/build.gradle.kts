/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    `kotlin-dsl`
}

group = "at.released.builder.emscripten.buildlogic.project"

dependencies {
    implementation(libs.detekt.plugin)
    implementation(libs.spotless.plugin)
}
