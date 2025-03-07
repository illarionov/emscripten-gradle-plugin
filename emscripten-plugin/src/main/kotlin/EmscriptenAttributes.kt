/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.builder.emscripten

import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.LibraryElements.DYNAMIC_LIB
import org.gradle.api.attributes.Usage
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.named
import org.gradle.nativeplatform.MachineArchitecture
import org.gradle.nativeplatform.OperatingSystemFamily

public object EmscriptenAttributes {
    /**
     * Attribute to mark code compiled with multithreading support using pthread
     */
    public val EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE: Attribute<Boolean> = Attribute.of(
        "at.released.builder.emscripten.pthreads",
        Boolean::class.javaObjectType,
    )


    public val ObjectFactory.wasm32Architecture: MachineArchitecture get() = named("wasm32")

    public val ObjectFactory.emscriptenOperatingSystem: OperatingSystemFamily get() = named("emscripten")

    public val ObjectFactory.wasmApiUsage: Usage get() = named("wasm-api")

    public val ObjectFactory.wasmRuntimeUsage: Usage get() = named("wasm-runtime")

    public val ObjectFactory.wasmBinaryLibraryElements: LibraryElements get() = named(DYNAMIC_LIB)
}


