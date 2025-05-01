/*
* SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
* SPDX-License-Identifier: Apache-2.0
*/

@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic/project")
    repositories {
        google {
            content {
                includeGroupAndSubgroups("android")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "emscripten-gradle-plugin"
include("emscripten-plugin")
include("doc:aggregate-documentation")
