/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.builder.emscripten.dwarf

import at.released.builder.emscripten.InternalEmscriptenApi
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.process.ExecOperations
import org.gradle.process.internal.ExecException
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Validates that the DWARF debug information of the WebAssembly binary does not contain absolute paths.
 *
 * Requires `llvm-dwarfdump` to be available.
 */
@InternalEmscriptenApi
public open class ValidateDwarfTask @Inject constructor(
    private val execOperations: ExecOperations,
    objects: ObjectFactory,
) : DefaultTask() {

    /**
     * WASM binary file to validate
     */
    @get:InputFile
    public val wasmBinary: RegularFileProperty = objects.fileProperty()

    /**
     * Strings that shouldn't be in paths
     */
    @get:Input
    public val paths: ListProperty<String> = objects.listProperty()

    @TaskAction
    public fun validate() {
        if (paths.get().isEmpty()) {
            return
        }
        val binary = wasmBinary.get().asFile

        val outputStream = ByteArrayOutputStream()

        try {
            execOperations.exec {
                this.commandLine = listOf("llvm-dwarfdump", "--show-sources", binary.canonicalPath)
                this.standardOutput = outputStream
            }.rethrowFailure().assertNormalExitValue()
        } catch (execException: ExecException) {
            logger.error("Failed to execute `llvm-dwarfdump`", execException)
            return
        }

        val sources = outputStream.toString()
        val paths = sources.findStringsStartsWithPath(paths.get())
        if (paths.isNotEmpty()) {
            logger.error("Wasm binary `$binary` contains not mapped paths: ${paths.joinToString(", ")}")
        }
    }

    public companion object
}
