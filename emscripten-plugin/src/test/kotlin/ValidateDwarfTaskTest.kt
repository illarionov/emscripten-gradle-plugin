/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.base

import assertk.assertThat
import assertk.assertions.containsExactly
import at.released.builder.emscripten.ValidateDwarfTask.Companion.findStringsStartsWithPath
import org.junit.jupiter.api.Test

class ValidateDwarfTaskTest {
    @Test
    fun `getPatternStringStartsWithAnyOf() should return correct substrings`() {
        val testContent = listOf(
            "/home",
            "/home/work/test1",
            "/sqlite",
            "/emsdk/path1",
            "/home/work/path3",
            "/emsdk/path2",
            "/home/work/path4",
            "/home/user",
            "/emsdk/home/user",
            "/home/user/subpath",
        ).joinToString("\n")

        val shouldNotContainPaths = listOf(
            "/home/work",
            "/home/user",
        )
        val incorrectPaths = findStringsStartsWithPath(testContent, shouldNotContainPaths)

        assertThat(incorrectPaths.toList())
            .containsExactly(
                "/home/user",
                "/home/user/subpath",
                "/home/work/path3",
                "/home/work/path4",
                "/home/work/test1",
            )
    }
}
