package com.hestalang.uswire.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.hestalang.uswire.core.model.VpnConfig
import com.hestalang.uswire.data.local.AndroidKeystoreConfigRepository
import com.hestalang.uswire.domain.usecase.ImportConfigUseCase

class MainActivity : Activity() {
    private lateinit var fileLabel: TextView
    private lateinit var statusLabel: TextView

    private val configRepository by lazy {
        AndroidKeystoreConfigRepository(applicationContext)
    }

    private val importConfig by lazy {
        ImportConfigUseCase(configRepository)
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
            text = "Estado: desconectado. El motor VPN aún no está integrado."
        }

        val importButton = Button(this).apply {
            text = "Importar archivo WireGuard (.conf)"
            setOnClickListener { openConfigPicker() }
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
            addView(statusLabel)
        }

        setContentView(layout)
        showSavedConfig()
    }

    private fun openConfigPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, REQUEST_CONFIG)
    }

    @Deprecated("Kept for compatibility with the minimum Android API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CONFIG || resultCode != RESULT_OK) return

        val uri = data?.data ?: return
        val content = try {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        } catch (error: Exception) {
            statusLabel.text = "No se pudo leer el archivo: ${error.message ?: "error"}"
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
                "Configuración validada y guardada cifrada. Conexión VPN pendiente de integración."
        }.onFailure { error ->
            statusLabel.text = error.message ?: "La configuración no es válida."
        }
    }

    private fun showSavedConfig() {
        val config: VpnConfig? = configRepository.load()
        fileLabel.text = config?.let { "Configuración: ${it.displayName}" }
            ?: "No hay ninguna configuración importada."
    }

    companion object {
        private const val REQUEST_CONFIG = 4102
    }
}
