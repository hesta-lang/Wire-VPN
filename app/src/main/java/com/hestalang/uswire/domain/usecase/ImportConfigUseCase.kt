package com.hestalang.uswire.domain.usecase

import com.hestalang.uswire.core.model.VpnConfig
import com.hestalang.uswire.data.validation.ConfigValidator
import com.hestalang.uswire.domain.repository.ConfigRepository

class ImportConfigUseCase(
    private val repository: ConfigRepository
) {
    fun execute(name: String, contents: String): Result<VpnConfig> {
        val validation = ConfigValidator.validate(contents)
        if (validation.isFailure) {
            return Result.failure(
                validation.exceptionOrNull()
                    ?: IllegalArgumentException("Configuración no válida.")
            )
        }

        val config = VpnConfig(
            displayName = name,
            rawConfig = contents,
            importedAtMillis = System.currentTimeMillis()
        )

        return runCatching {
            repository.save(config)
            config
        }
    }
}
