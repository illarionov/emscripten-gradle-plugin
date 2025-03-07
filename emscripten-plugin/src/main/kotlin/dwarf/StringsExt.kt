/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the emscripten-gradle-plugin project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.builder.emscripten.dwarf

import kotlin.text.RegexOption.IGNORE_CASE
import kotlin.text.RegexOption.MULTILINE

internal fun String.findStringsStartsWithPath(
    paths: List<String>,
): Set<String> {
    val pattern = getPatternStringStartsWithAnyOf(paths)
    val matches = pattern.findAll(this)
        .map { it.value }
        .distinct()
        .toSortedSet()
    return matches
}

internal fun getPatternStringStartsWithAnyOf(paths: List<String>): Regex {
    if (paths.isEmpty()) {
        return Regex("""^$""")
    }
    return paths
        .joinToString(
            separator = "|",
            prefix = """^(?:""",
            postfix = """).*$""",
            transform = Regex::escape,
        )
        .toRegex(setOf(MULTILINE, IGNORE_CASE))
}
