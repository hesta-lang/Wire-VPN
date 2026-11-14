package com.hestalang.uswire.core.model

import java.util.UUID

data class VpnConfig(
    val displayName: String,
    val rawConfig: String,
    val importedAtMillis: Long,
    val id: String = UUID.randomUUID().toString()
)
