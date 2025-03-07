# Emscripten Gradle Plugin

A plugin with wrappers for running [Emscripten] tasks from Gradle.

Currently in early development and subject to change.

[Emscripten]: https://emscripten.org/ 

## Requirements

* Gradle `8.0` or newer

## Installation

The latest release is available on Maven Central. Add the following to your plugins block:

```
plugins { 
    id("at.released.builder.emscripten.plugin") version "<not yet released>"
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

TODO

## Contributing

Any type of contributions are welcome. Please see [the contribution guide](CONTRIBUTING.md).

### License

Emscripten Gradle Plugin is distributed under the terms of the Apache License (Version 2.0). See the
[license](https://github.com/illarionov/emscripten-gradle-plugin/blob/main/LICENSE) for more information.
