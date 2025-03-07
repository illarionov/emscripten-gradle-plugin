/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.base.dwarf

import assertk.assertThat
import assertk.assertions.containsExactly
import at.released.builder.emscripten.dwarf.findStringsStartsWithPath
import org.junit.jupiter.api.Test

class StringsExt {
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
        val incorrectPaths = testContent.findStringsStartsWithPath(shouldNotContainPaths)

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
