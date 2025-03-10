/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask

/*
 * Module responsible for aggregating Dokka documentation from subprojects
 */
plugins {
    id("at.released.builder.emscripten.buildlogic.project.doc.base")
}

group = "at.released.builder.emscripten"

private val websiteOutputDirectory = layout.buildDirectory.dir("outputs/website")

dokka {
    dokkaPublications.configureEach {
        moduleName.set("Emscripten Gradle Plugin")
        includes.from("FRONTPAGE.md")
    }
}

val dokkaHtmlOutput = tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationHtml")
    .flatMap(DokkaGeneratePublicationTask::outputDirectory)

tasks.register<Sync>("buildWebsite") {
    description = "Assembles the final website from Dokka output"
    from(dokkaHtmlOutput)
    from(layout.projectDirectory.dir("root"))
    into(websiteOutputDirectory)
}

dependencies {
    dokka(project(":emscripten-plugin"))
}
