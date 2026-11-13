package com.hestalang.uswire.data.validation

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

        return Result.success(Unit)
    }
}
