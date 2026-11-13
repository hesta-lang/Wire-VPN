package com.hestalang.uswire.core.model

data class VpnConfig(
    val displayName: String,
    val rawConfig: String,
    val importedAtMillis: Long
)
