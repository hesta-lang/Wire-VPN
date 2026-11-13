package com.hestalang.uswire.data.validation

import com.wireguard.config.Config
import java.io.ByteArrayInputStream

/**
 * Performs structural checks and then delegates syntax and field validation
 * to the same parser used by the WireGuard tunnel backend.
 */
object ConfigValidator {
    fun validate(contents: String): Result<Unit> {
        if (contents.isBlank()) {
            return Result.failure(IllegalArgumentException("El archivo está vacío."))
        }

        val sections = contents.lineSequence()
            .map { it.substringBefore("#").trim() }
            .filter { it.startsWith("[") && it.endsWith("]") }
            .map { it.lowercase() }
            .toSet()

        if ("[interface]" !in sections) {
            return Result.failure(
                IllegalArgumentException("Falta la sección [Interface].")
            )
        }

        if ("[peer]" !in sections) {
            return Result.failure(
                IllegalArgumentException("Falta al menos una sección [Peer].")
            )
        }

        return runCatching {
            Config.parse(ByteArrayInputStream(contents.toByteArray(Charsets.UTF_8)))
            Unit
        }
    }
}
