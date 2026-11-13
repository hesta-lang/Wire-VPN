package com.hestalang.uswire.domain.repository

import com.hestalang.uswire.core.model.VpnConfig

interface ConfigRepository {
    fun save(config: VpnConfig)
    fun load(): VpnConfig?
    fun clear()
}
