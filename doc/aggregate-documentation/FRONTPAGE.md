# Emscripten Gradle Plugin

Helpers for running [Emscripten] tasks from Gradle.

## Installation

The latest release is available on Maven Central. Add the following to your plugins block:

```
plugins { 
    id("at.released.builder.emscripten.plugin") version "0.1-alpha01"
}
```

Snapshot versions of the library may be published to a self-hosted public repository.

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pixnews.ru")
            mavenContent {
                includeGroupAndSubgroups("at.released.builder.emscripten")
            }
        }
    }
}
```

## Usage

[EmscriptenSdk] - the main utility class helper object for performing operations using Emscripten.

It allows building the command line for running [emcc], [emconfigure], [emmake], or [embuilder] commands from the 
Emscripten package, as well as setting up the environment.

Example usage from a custom task:

```kotlin
public abstract class SdkSampleTask @Inject constructor(
    private val execOperations: ExecOperations,
    objects: ObjectFactory,
) {
    @get:Nested
    public val emscriptenSdk: EmscriptenSdk = objects.newInstance()

    @get:Input
    public val outputFileName: Property<String> = objects.property(String::class.java)

    @get:OutputDirectory
    public val outputDirectory: DirectoryProperty = objects.directoryProperty()

    @get:Input
    public val workingDir: Property<File> = objects.property(File::class.java)

    @get:OutputFile
    public val outputFile: RegularFileProperty = objects.fileProperty().convention(
        outputDirectory.zip(outputFileName, Directory::file),
    )

    @TaskAction
    public fun build() {
        emscriptenSdk.checkEmsdkVersion()
        emscriptenSdk.prepareEmscriptenCache()

        val cmdLine = emscriptenSdk.buildEmccCommandLine {
            add("-o")
            add(outputFile.get().toString())
            add("-I<includes>")
            addAll(listOf("other", "command", "line", "args"))
            add("source.c")
        }

        try {
            execOperations.exec {
                this.commandLine = cmdLine
                this.workingDir = workingDir
                this.environment = emscriptenSdk.getEmsdkEnvironment()
            }.rethrowFailure().assertNormalExitValue()
        } catch (execException: ExecException) {
            throw ExecException(
                "Failed to execute `$cmdLine`",
                execException,
            )
        }
    }
}
```

##  Preparing system libraries and ports using embuilder

Emscripten provides a tool called [embuilder] that can be used to precompile system libraries and ports.

This step is optional, but it helps make builds more reproducible because
this compilation uses the debug-prefix-map parameter to get deterministic paths in the DWARF debug info.

To automate this process, a dedicated task called [EmscriptenPrepareCacheTask] has been added,
which handles precompiling the system libraries into a cache.

The location of this prepared cache, used for all subsequent commands, can be set via the 
`EmscriptenSdk.emscriptenCacheBase` parameter.

TODO

## Emscripten SDK Location

At this time, automatic downloading of the Emscripten is not implemented.
Users can specify the location of their installed SDK using environment variables or Gradle properties.

`EMSDK` environment variable must point to the root of the installed SDK.
Version of the SDK used in the project must be activated.

Alternatively, user can specify the Emscripten SDK root by setting the `emsdkRoot` project property.
You can do this for example in `~/.gradle/gradle.properties`:

```properties
emsdkRoot=/opt/emsdk
```

Install and activate the required SDK version:

```shell
./emsdk install 4.0.4
./emsdk activate 4.0.4
source ./emsdk_env.sh
```

## Contributing

Any type of contributions are welcome. Please see the [contribution guide].

## License

These services are licensed under Apache 2.0 License. Authors and contributors are listed in the
[Authors] file.

```
Copyright 2024-2025 emscripten-gradle-plugin project authors and contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[Emscripten]: https://emscripten.org/
[EmscriptenSdk]: https://illarionov.github.io/emscripten-gradle-plugin/emscripten-plugin/at.released.builder.emscripten/-emscripten-sdk/index.html
[EmscriptenPrepareCacheTask]: https://illarionov.github.io/emscripten-gradle-plugin/emscripten-plugin/emscripten-plugin/at.released.builder.emscripten/-emscripten-prepare-cache-task/index.html
[emcc]: https://emscripten.org/docs/tools_reference/emcc.html
[emconfigure]: http://emscripten.org/docs/compiling/Building-Projects.html
[emmake]: http://emscripten.org/docs/compiling/Building-Projects.html
[embuilder]: https://github.com/emscripten-core/emscripten/blob/main/embuilder.py
