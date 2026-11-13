package com.hestalang.uswire.vpn

import com.hestalang.uswire.core.model.VpnConfig
import com.hestalang.uswire.core.model.VpnState

/**
 * Adapter boundary for the official WireGuard Tunnel backend.
 *
 * No implementation is provided in this alpha. The UI must not report CONNECTED
 * until a real GoBackend/VpnService implementation confirms the tunnel state.
 */
interface WireGuardController {
    fun currentState(): VpnState

    fun connect(
        config: VpnConfig,
        onStateChanged: (VpnState) -> Unit
    )

    fun disconnect(
        onStateChanged: (VpnState) -> Unit
    )
}
