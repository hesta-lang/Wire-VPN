package com.hestalang.uswire.vpn

import android.content.Context
import android.net.VpnService
import com.hestalang.uswire.core.model.VpnConfig
import com.hestalang.uswire.core.model.VpnState
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.io.ByteArrayInputStream
import java.util.concurrent.Executors

/**
 * Runs the official WireGuard userspace backend on a dedicated worker thread.
 *
 * The singleton keeps the GoBackend and Tunnel instances stable for the lifetime
 * of the app process, which is required by GoBackend's service ownership model.
 */
class WireGuardController private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val backend = GoBackend(appContext)
    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var state: VpnState = VpnState.DISCONNECTED

    @Volatile
    private var stateListener: ((VpnState, String?) -> Unit)? = null

    private val tunnel = object : Tunnel {
        override fun getName(): String = TUNNEL_NAME

        override fun onStateChange(newState: Tunnel.State) {
            publishState(
                if (newState == Tunnel.State.UP) {
                    VpnState.CONNECTED
                } else {
                    VpnState.DISCONNECTED
                },
                null
            )
        }
    }

    fun currentState(): VpnState = state

    /**
     * Call only after Android's VpnService.prepare() permission flow has completed.
     * The configuration is parsed again by WireGuard's own parser before use.
     */
    fun connect(
        vpnConfig: VpnConfig,
        onStateChanged: (VpnState, String?) -> Unit
    ) {
        stateListener = onStateChanged
        executor.execute {
            publishState(VpnState.CONNECTING, null)

            try {
                check(VpnService.prepare(appContext) == null) {
                    "Android todavía no ha autorizado a USWire para crear la conexión VPN."
                }

                val parsedConfig = Config.parse(
                    ByteArrayInputStream(vpnConfig.rawConfig.toByteArray(Charsets.UTF_8))
                )

                backend.setState(tunnel, Tunnel.State.UP, parsedConfig)

                check(backend.getState(tunnel) == Tunnel.State.UP) {
                    "WireGuard no confirmó que el túnel quedara activo."
                }

                publishState(VpnState.CONNECTED, null)
            } catch (error: Exception) {
                publishState(
                    VpnState.ERROR,
                    error.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: "No se pudo iniciar el túnel WireGuard."
                )
            }
        }
    }

    fun disconnect(onStateChanged: (VpnState, String?) -> Unit) {
        stateListener = onStateChanged
        executor.execute {
            publishState(VpnState.DISCONNECTING, null)

            try {
                backend.setState(tunnel, Tunnel.State.DOWN, null)
                publishState(VpnState.DISCONNECTED, null)
            } catch (error: Exception) {
                publishState(
                    VpnState.ERROR,
                    error.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: "No se pudo detener el túnel WireGuard."
                )
            }
        }
    }

    private fun publishState(newState: VpnState, message: String?) {
        state = newState
        stateListener?.invoke(newState, message)
    }

    companion object {
        private const val TUNNEL_NAME = "uswire"

        @Volatile
        private var instance: WireGuardController? = null

        fun getInstance(context: Context): WireGuardController {
            return instance ?: synchronized(this) {
                instance ?: WireGuardController(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
