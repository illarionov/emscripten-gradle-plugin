/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.builder.emscripten

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.process.ExecOperations
import org.gradle.process.internal.ExecException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

/**
 * The main helper object for performing operations using Emscripten.
 *
 * It allows building the command line for running *emcc*, *emconfigure*, *emmake*, or *embuilder* from the
 * Emscripten package, as well as setting up the environment.
 *
 * See [emscripten-plugin](https://illarionov.github.io/emscripten-gradle-plugin) for example usage
 *
 */
@Suppress("TooManyFunctions")
public abstract class EmscriptenSdk @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory,
    private val execOperations: ExecOperations,
) {
    /**
     * Path to Emscripten SDK
     */
    @get:Input
    @Optional
    public val emscriptenRoot: Property<File> = objects.property(File::class.java).convention(
        providers.defaultEmscriptenRoot(),
    )

    /**
     * Version of the Emscripten that should be used.
     */
    @get:Input
    @get:Optional
    public val emccVersion: Property<String> = objects.property(String::class.java)
        .convention(EMSCRIPTEN_VERSION)

    /**
     * The directory that will be used for the cache
     */
    @get:Internal
    public val emscriptenCacheDir: DirectoryProperty = objects.directoryProperty()

    /**
     * Directory with cache prepared in advance using [EmscriptenPrepareCacheTask] task.
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Optional
    public val emscriptenCacheBase: DirectoryProperty = objects.directoryProperty()

    @get:InputFiles
    @Optional
    @PathSensitive(PathSensitivity.NONE)
    public val emscriptenConfigFile: ConfigurableFileCollection = objects.fileCollection()

    @get:Internal
    public val emccExecutablePath: Property<String> = objects.property(String::class.java)
        .convention("upstream/emscripten/emcc")

    @get:Internal
    public val emConfigureExecutablePath: Property<String> = objects.property(String::class.java)
        .convention("upstream/emscripten/emconfigure")

    @get:Internal
    public val embuilderExecutablePath: Property<String> = objects.property(String::class.java)
        .convention("upstream/emscripten/embuilder")

    @get:Internal
    public val emMakeExecutablePath: Property<String> = objects.property(String::class.java)
        .convention("upstream/emscripten/emmake")

    /**
     * Builds [emcc](https://emscripten.org/docs/tools_reference/emcc.html) command line.
     */
    public fun buildEmccCommandLine(
        builderAction: MutableList<String>.() -> Unit,
    ): List<String> = buildList {
        val command = getEmscriptenExecutableOrThrow(emccExecutablePath)
        add(command.toString())
        // Do not depend on ~/.emscripten
        add("--em-config")
        add(getEmscriptenConfigFile().toString())

        if (emscriptenCacheDir.isPresent) {
            val cacheDir = emscriptenCacheDir.get()
            add("--cache")
            add(cacheDir.toString())
        }

        builderAction()
    }

    /**
     * Builds [emconfigure](http://emscripten.org/docs/compiling/Building-Projects.html) command line.
     */
    public fun buildEmconfigureCommandLine(
        builderAction: MutableList<String>.() -> Unit,
    ): List<String> = buildList {
        val command = getEmscriptenExecutableOrThrow(emConfigureExecutablePath)
        add(command.toString())
        // Do not depend on ~/.emscripten
        add("--em-config")
        add(getEmscriptenConfigFile().toString())
        builderAction()
    }

    /**
     * Builds [emmake](http://emscripten.org/docs/compiling/Building-Projects.html) command line.
     */
    public fun buildEmMakeCommandLine(
        builderAction: MutableList<String>.() -> Unit,
    ): List<String> = buildList {
        val command = getEmscriptenExecutableOrThrow(emMakeExecutablePath)
        add(command.toString())
        // Do not depend on ~/.emscripten
        add("--em-config")
        add(getEmscriptenConfigFile().toString())
        builderAction()
    }

    /**
     * Builds [embuilder](https://github.com/emscripten-core/emscripten/blob/main/embuilder.py) command line.
     */
    public fun buildEmBuilderCommandLine(
        builderAction: MutableList<String>.() -> Unit,
    ): List<String> = buildList {
        val command = getEmscriptenExecutableOrThrow(embuilderExecutablePath)
        add(command.toString())
        builderAction()
    }

    /**
     * Validates that the version specified in [emccVersion] is active in the SDK directory [emscriptenRoot]
     */
    public fun checkEmsdkVersion() {
        if (!emccVersion.isPresent) {
            return
        }
        val requiredVersion = emccVersion.get()
        val version = readEmsdkVersion()

        if (requiredVersion != version) {
            throw IllegalStateException(
                "The installed version of Emscripten SDK `$version` differs from the required" +
                        " version `$requiredVersion`",
            )
        }
    }

    /**
     * Returns environment variables that must be set when calling Emscripten executables
     */
    @Internal
    public fun getEmsdkEnvironment(): Map<String, String> = buildMap {
        put("EMSDK", emscriptenRoot.get().toString())
    }

    /**
     * Prepares cache directory [emscriptenCacheDir] from [emscriptenCacheBase]
     */
    public fun prepareEmscriptenCache() {
        if (!emscriptenCacheBase.isPresent) {
            return
        }
        val cacheBase = emscriptenCacheBase.get().asFile
        val cacheDir = emscriptenCacheDir.orNull?.asFile ?: error(
            "emscriptenCacheBase requires emscriptenCacheDir to be set",
        )
        cacheDir.deleteRecursively()
        if (!cacheDir.mkdirs()) {
            error("Can not create $cacheDir")
        }
        cacheBase.copyRecursively(
            target = cacheDir,
            overwrite = false,
        )
    }

    private fun readEmsdkVersion(): String {
        val emcc = getEmscriptenExecutableOrThrow(emccExecutablePath).toString()

        val stdErr = ByteArrayOutputStream()
        try {
            execOperations.exec {
                commandLine = listOf(emcc, "-v")
                errorOutput = stdErr
                environment = getEmsdkEnvironment()
            }.rethrowFailure().assertNormalExitValue()
        } catch (execException: ExecException) {
            throw ExecException(
                "Failed to execute `emcc -v`. Make sure Emscripten SDK is installed correctly",
                execException,
            )
        }

        val firstLine: String = ByteArrayInputStream(stdErr.toByteArray()).bufferedReader().use {
            it.readLine()
        } ?: error("Can not read Emscripten SDK version")

        return EMCC_VERSION_REGEX.matchEntire(firstLine)?.groups?.get(1)?.value
            ?: error("Can not parse EMSDK version from `$firstLine`. ")
    }

    private fun getEmscriptenExecutableOrThrow(
        commandPath: Provider<String>,
    ): File {
        val pathProvider = emscriptenRoot.zip(commandPath, ::File)
        val path = pathProvider.orNull ?: error(
            "Can not find Emscripten SDK installation directory. EMSDK environment variable should be defined",
        )
        check(path.isFile) {
            "Can not find Emscripten executable. `$path` is not a file"
        }
        return path
    }

    private fun getEmscriptenConfigFile(): File {
        val files = emscriptenConfigFile.files
        return if (files.isNotEmpty()) {
            files.first()
        } else {
            emscriptenRoot.get().resolve(".emscripten")
        }
    }

    private companion object {
        internal const val EMSCRIPTEN_VERSION = "4.0.4"

        private val EMCC_VERSION_REGEX = """emcc\s+\(Emscripten.+\)\s+(\S+)\s+.*""".toRegex()

        public fun ProviderFactory.defaultEmscriptenRoot(): Provider<File> = this
            .environmentVariable("EMSDK")
            .orElse(this.gradleProperty("emsdkRoot"))
            .map(::File)
    }
}
