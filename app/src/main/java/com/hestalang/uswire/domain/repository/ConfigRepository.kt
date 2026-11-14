package com.hestalang.uswire.domain.repository

import com.hestalang.uswire.core.model.VpnConfig

interface ConfigRepository {
    /** Adds or updates a profile and makes it the active profile. */
    fun save(config: VpnConfig)

    /** Returns the currently selected profile, or null when the library is empty. */
    fun load(): VpnConfig?

    /** Returns every stored profile. */
    fun loadAll(): List<VpnConfig>

    /** Changes the active profile. */
    fun setActive(profileId: String)

    /** Deletes only the profile with the supplied ID. */
    fun delete(profileId: String)

    /** Clears the profile library. */
    fun clear()
}
