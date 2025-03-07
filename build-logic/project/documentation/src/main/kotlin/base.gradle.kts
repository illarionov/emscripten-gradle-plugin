/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.builder.emscripten.buildlogic.project.doc

/*
 * Base configuration of Dokka
 */
plugins {
    id("org.jetbrains.dokka")
}

@Suppress("UnstableApiUsage")
private val htmlResourcesRoot = layout.settingsDirectory.dir("doc/aggregate-documentation")

dokka {
    dokkaPublications.configureEach {
        suppressObviousFunctions.set(true)
        suppressInheritedMembers.set(true)
    }

    dokkaSourceSets.configureEach {
        includes.from("MODULE.md")
        sourceLink {
            localDirectory.set(project.layout.projectDirectory)
            val remoteUrlSubpath = project.path.replace(':', '/')
            remoteUrl("https://github.com/illarionov/emscripten-gradle-plugin/tree/main$remoteUrlSubpath")
        }
    }

    pluginsConfiguration.html {
        homepageLink.set("https://github.com/illarionov/emscripten-gradle-plugin")
        footerMessage.set("(C) emscripten-gradle-plugin project authors and contributors")
        customStyleSheets.from(
            htmlResourcesRoot.file("styles/font-jb-sans-auto.css"),
            htmlResourcesRoot.file("styles/emstyle.css"),
        )
    }
}
