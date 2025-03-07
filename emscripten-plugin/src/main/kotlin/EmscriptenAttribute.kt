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

public object EmscriptenAttribute {
    public const val MACHINE_ARCHITECTURE_WASM32: String = "wasm32"

    public const val OPERATING_SYSTEM_FAMILY_EMSCRIPTEN: String = "emscripten"

    public const val USAGE_WASM_API: String = "wasm-api"

    public const val USAGE_WASM_RUNTIME: String = "wasm-runtime"

    /**
     * Attribute to mark code compiled with multithreading support using pthread
     */
    public val EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE: Attribute<Boolean> = Attribute.of(
        "at.released.builder.emscripten.pthreads",
        Boolean::class.javaObjectType,
    )

    public val ObjectFactory.wasm32Architecture: MachineArchitecture
        get() = named(MACHINE_ARCHITECTURE_WASM32)

    public val ObjectFactory.emscriptenOperatingSystem: OperatingSystemFamily
        get() = named(OPERATING_SYSTEM_FAMILY_EMSCRIPTEN)

    public val ObjectFactory.wasmApiUsage: Usage
        get() = named(USAGE_WASM_API)

    public val ObjectFactory.wasmRuntimeUsage: Usage
        get() = named(USAGE_WASM_RUNTIME)

    public val ObjectFactory.wasmBinaryLibraryElements: LibraryElements
        get() = named(DYNAMIC_LIB)
}
