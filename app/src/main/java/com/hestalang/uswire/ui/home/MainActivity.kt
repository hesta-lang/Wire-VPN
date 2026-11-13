package com.hestalang.uswire.ui.home

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.hestalang.uswire.core.model.VpnConfig
import com.hestalang.uswire.core.model.VpnState
import com.hestalang.uswire.data.local.AndroidKeystoreConfigRepository
import com.hestalang.uswire.domain.usecase.ImportConfigUseCase
import com.hestalang.uswire.vpn.WireGuardController

class MainActivity : Activity() {
    private lateinit var fileLabel: TextView
    private lateinit var statusLabel: TextView
    private lateinit var connectButton: Button
    private lateinit var disconnectButton: Button

    private val configRepository by lazy {
        AndroidKeystoreConfigRepository(applicationContext)
    }

    private val importConfig by lazy {
        ImportConfigUseCase(configRepository)
    }

    private val wireGuardController by lazy {
        WireGuardController.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileLabel = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
        }

        statusLabel = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            text = "Estado: desconectado."
        }

        val importButton = Button(this).apply {
            text = "Importar configuración (.conf)"
            setOnClickListener { openConfigPicker() }
        }

        connectButton = Button(this).apply {
            text = "Conectar"
            setOnClickListener { requestVpnPermissionAndConnect() }
        }

        disconnectButton = Button(this).apply {
            text = "Desconectar"
            isEnabled = false
            setOnClickListener { disconnectVpn() }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)

            addView(TextView(this@MainActivity).apply {
                text = getString(com.hestalang.uswire.R.string.app_name)
                textSize = 28f
                gravity = Gravity.CENTER
            })
            addView(fileLabel)
            addView(importButton)
            addView(connectButton)
            addView(disconnectButton)
            addView(statusLabel)
        }

        setContentView(layout)
        showSavedConfig()
        renderVpnState(wireGuardController.currentState(), null)
    }

    private fun openConfigPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, REQUEST_CONFIG)
    }

    private fun requestVpnPermissionAndConnect() {
        val config = configRepository.load()
        if (config == null) {
            statusLabel.text = "Primero importa un archivo WireGuard .conf válido."
            return
        }

        val permissionIntent = VpnService.prepare(this)
        if (permissionIntent != null) {
            startActivityForResult(permissionIntent, REQUEST_VPN_PERMISSION)
        } else {
            connectSavedConfig(config)
        }
    }

    private fun connectSavedConfig(config: VpnConfig? = configRepository.load()) {
        if (config == null) {
            statusLabel.text = "No hay ninguna configuración WireGuard guardada."
            return
        }

        wireGuardController.connect(config) { state, message ->
            runOnUiThread { renderVpnState(state, message) }
        }
    }

    private fun disconnectVpn() {
        wireGuardController.disconnect { state, message ->
            runOnUiThread { renderVpnState(state, message) }
        }
    }

    @Deprecated("Kept compatible with the app's minimum Android version")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_VPN_PERMISSION -> {
                if (resultCode == RESULT_OK) {
                    connectSavedConfig()
                } else {
                    statusLabel.text =
                        "Permiso VPN denegado. Android no permitió iniciar el túnel."
                }
            }

            REQUEST_CONFIG -> {
                if (resultCode != RESULT_OK) return
                val uri = data?.data ?: return
                val content = try {
                    contentResolver.openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use { it.readText() }
                } catch (error: Exception) {
                    statusLabel.text =
                        "No se pudo leer el archivo: ${error.localizedMessage ?: "error"}"
                    return
                }

                if (content.isNullOrBlank()) {
                    statusLabel.text = "El archivo está vacío o no se pudo leer."
                    return
                }

                val displayName = uri.lastPathSegment
                    ?.substringAfterLast('/')
                    ?.takeIf { it.isNotBlank() }
                    ?: "WireGuard.conf"

                val result = importConfig.execute(displayName, content)
                result.onSuccess { config ->
                    fileLabel.text = "Configuración: ${config.displayName}"
                    statusLabel.text =
                        "Configuración validada y guardada cifrada. Ya puedes conectarte."
                }.onFailure { error ->
                    statusLabel.text =
                        error.localizedMessage ?: "La configuración WireGuard no es válida."
                }
            }
        }
    }

    private fun renderVpnState(state: VpnState, message: String?) {
        statusLabel.text = when (state) {
            VpnState.DISCONNECTED -> "Estado: desconectado."
            VpnState.CONNECTING -> "Estado: conectando con WireGuard…"
            VpnState.CONNECTED -> "Estado: conectado mediante WireGuard."
            VpnState.DISCONNECTING -> "Estado: desconectando…"
            VpnState.ERROR -> "Error VPN: ${message ?: "No se pudo completar la operación."}"
        }

        val busy = state == VpnState.CONNECTING || state == VpnState.DISCONNECTING
        connectButton.isEnabled = !busy
        disconnectButton.isEnabled = state == VpnState.CONNECTED
    }

    private fun showSavedConfig() {
        val config: VpnConfig? = configRepository.load()
        fileLabel.text = config?.let { "Configuración: ${it.displayName}" }
            ?: "No hay ninguna configuración importada."
    }

    companion object {
        private const val REQUEST_CONFIG = 4102
        private const val REQUEST_VPN_PERMISSION = 4103
    }
}
