package com.hestalang.uswire.ui.home

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.VpnService
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.hestalang.uswire.core.model.VpnConfig
import com.hestalang.uswire.core.model.VpnState
import com.hestalang.uswire.data.local.AndroidKeystoreConfigRepository
import com.hestalang.uswire.domain.usecase.ImportConfigUseCase
import com.hestalang.uswire.vpn.WireGuardController

class MainActivity : Activity() {
    private lateinit var contentFrame: FrameLayout
    private lateinit var bottomNavigation: BottomNavigationView
    private var currentPage = PAGE_HOME
    private var vpnState = VpnState.DISCONNECTED
    private var vpnError: String? = null

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
        window.statusBarColor = COLOR_BACKGROUND
        window.navigationBarColor = COLOR_SURFACE
        window.decorView.systemUiVisibility = 0

        vpnState = wireGuardController.currentState()
        buildApplicationShell()
        renderPage(PAGE_HOME)
    }

    private fun buildApplicationShell() {
        val shell = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(COLOR_BACKGROUND)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(14))
        }

        val logo = TextView(this).apply {
            text = "U"
            textSize = 23f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(COLOR_BLUE, dp(15))
        }
        header.addView(logo, LinearLayout.LayoutParams(dp(44), dp(44)))

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        addText(brand, "USWire", 19f, COLOR_TEXT, true)
        addText(brand, "PRIVATE NETWORK CLIENT", 9f, COLOR_MUTED, true)
        header.addView(brand, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val version = smallPill("ANDROID", COLOR_BLUE_SOFT, COLOR_BLUE_LIGHT)
        header.addView(version, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        shell.addView(header)

        contentFrame = FrameLayout(this)
        shell.addView(
            contentFrame,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )

        bottomNavigation = BottomNavigationView(this).apply {
            setBackgroundColor(COLOR_SURFACE)
            itemIconTintList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(COLOR_BLUE_LIGHT, COLOR_MUTED)
            )
            itemTextColor = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(COLOR_BLUE_LIGHT, COLOR_MUTED)
            )
            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
            menu.add(Menu.NONE, PAGE_HOME, Menu.NONE, "Inicio")
                .setIcon(android.R.drawable.ic_menu_view)
            menu.add(Menu.NONE, PAGE_PROFILES, Menu.NONE, "Perfiles")
                .setIcon(android.R.drawable.ic_menu_agenda)
            menu.add(Menu.NONE, PAGE_SETTINGS, Menu.NONE, "Ajustes")
                .setIcon(android.R.drawable.ic_menu_preferences)
            setOnItemSelectedListener { item ->
                renderPage(item.itemId)
                true
            }
        }
        shell.addView(
            bottomNavigation,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(shell)
    }

    private fun renderPage(page: Int) {
        currentPage = page
        vpnState = wireGuardController.currentState()
        contentFrame.removeAllViews()

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setPadding(0, dp(4), 0, dp(18))
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(10), dp(22), dp(12))
        }

        when (page) {
            PAGE_PROFILES -> renderProfiles(content)
            PAGE_SETTINGS -> renderSettings(content)
            else -> renderHome(content)
        }

        scrollView.addView(content)
        contentFrame.addView(
            scrollView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        if (::bottomNavigation.isInitialized && bottomNavigation.selectedItemId != page) {
            bottomNavigation.selectedItemId = page
        }
    }

    private fun renderHome(content: LinearLayout) {
        val config = configRepository.load()
        addText(content, "TU CONEXIÓN", 11f, COLOR_BLUE_LIGHT, true)
        addText(content, "Tu red.\nBajo tu control.", 31f, COLOR_TEXT, true)
        addText(
            content,
            "Administra tu túnel WireGuard desde un solo lugar.",
            14f,
            COLOR_MUTED
        )
        addSpace(content, 22)

        val statusCard = card(COLOR_CARD)
        val statusBody = verticalCardBody(statusCard)
        val connected = vpnState == VpnState.CONNECTED
        val busy = vpnState == VpnState.CONNECTING || vpnState == VpnState.DISCONNECTING
        val stateLabel = when (vpnState) {
            VpnState.CONNECTED -> "CONEXIÓN ACTIVA"
            VpnState.CONNECTING -> "CONECTANDO"
            VpnState.DISCONNECTING -> "DESCONECTANDO"
            VpnState.ERROR -> "ERROR DE CONEXIÓN"
            VpnState.DISCONNECTED -> "SIN CONEXIÓN"
        }
        val stateColor = when (vpnState) {
            VpnState.CONNECTED -> COLOR_GREEN
            VpnState.ERROR -> COLOR_RED
            VpnState.CONNECTING, VpnState.DISCONNECTING -> COLOR_AMBER
            else -> COLOR_MUTED
        }

        val badgeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val dot = TextView(this).apply {
            text = "●"
            textSize = 12f
            setTextColor(stateColor)
        }
        badgeRow.addView(dot)
        val badge = TextView(this).apply {
            text = stateLabel
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
            setTextColor(stateColor)
            setPadding(dp(8), 0, 0, 0)
        }
        badgeRow.addView(badge)
        statusBody.addView(badgeRow)

        addSpace(statusBody, 17)
        addText(
            statusBody,
            when (vpnState) {
                VpnState.CONNECTED -> "Estás conectado"
                VpnState.CONNECTING -> "Preparando el túnel…"
                VpnState.DISCONNECTING -> "Cerrando el túnel…"
                VpnState.ERROR -> "No se pudo conectar"
                VpnState.DISCONNECTED -> "Listo para conectar"
            },
            24f,
            COLOR_TEXT,
            true
        )
        val description = when (vpnState) {
            VpnState.CONNECTED -> "El motor WireGuard está activo en este dispositivo."
            VpnState.CONNECTING -> "USWire está iniciando el servicio VPN de Android."
            VpnState.DISCONNECTING -> "Se está deteniendo la conexión VPN."
            VpnState.ERROR -> vpnError ?: "Revisa el perfil e inténtalo de nuevo."
            VpnState.DISCONNECTED -> "Importa un perfil y activa tu conexión privada."
        }
        addSpace(statusBody, 5)
        addText(statusBody, description, 13f, COLOR_MUTED)
        addSpace(statusBody, 17)

        val profileLine = TextView(this).apply {
            text = if (config != null) "PERFIL  ·  " + config.displayName else "PERFIL  ·  NINGUNO IMPORTADO"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(COLOR_BLUE_LIGHT)
        }
        statusBody.addView(profileLine)

        addSpace(statusBody, 18)
        val actionLabel = when (vpnState) {
            VpnState.CONNECTED -> "Desconectar"
            VpnState.CONNECTING -> "Conectando…"
            VpnState.DISCONNECTING -> "Desconectando…"
            VpnState.ERROR -> "Reintentar conexión"
            VpnState.DISCONNECTED -> "Conectar"
        }
        val action = actionButton(actionLabel, true) {
            when (vpnState) {
                VpnState.CONNECTED -> disconnectVpn()
                VpnState.CONNECTING, VpnState.DISCONNECTING -> Unit
                else -> requestVpnPermissionAndConnect()
            }
        }
        action.isEnabled = !busy && (config != null || vpnState == VpnState.ERROR)
        statusBody.addView(action, matchWrap())

        addSpace(content, 18)
        addText(content, "VISTA GENERAL", 11f, COLOR_MUTED, true)
        addSpace(content, 9)
        val detailsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        detailsRow.addView(
            miniInfoCard("MOTOR", "WireGuard", "Túnel VPN", COLOR_BLUE_LIGHT),
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        val profileValue = config?.displayName ?: "Sin perfil"
        val secondCard = miniInfoCard("PERFIL", profileValue, "Almacenamiento cifrado", COLOR_GREEN)
        val secondParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        secondParams.marginStart = dp(10)
        detailsRow.addView(secondCard, secondParams)
        content.addView(detailsRow)

        addSpace(content, 18)
        val hint = card(COLOR_SURFACE)
        val hintBody = verticalCardBody(hint)
        addText(hintBody, "¿Necesitas otro servidor?", 16f, COLOR_TEXT, true)
        addSpace(hintBody, 4)
        addText(
            hintBody,
            "Abre Perfiles para importar o reemplazar tu archivo .conf.",
            13f,
            COLOR_MUTED
        )
        addSpace(hintBody, 10)
        hintBody.addView(actionButton("Administrar perfiles", false) {
            bottomNavigation.selectedItemId = PAGE_PROFILES
        }, matchWrap())
        content.addView(hint, matchWrap())
    }

    private fun renderProfiles(content: LinearLayout) {
        val config = configRepository.load()
        addText(content, "BIBLIOTECA", 11f, COLOR_BLUE_LIGHT, true)
        addText(content, "Tus perfiles", 30f, COLOR_TEXT, true)
        addText(content, "Importa y administra tu configuración WireGuard.", 14f, COLOR_MUTED)
        addSpace(content, 22)

        val profileCard = card(COLOR_CARD)
        val body = verticalCardBody(profileCard)
        addText(body, "PERFIL ACTUAL", 10f, COLOR_MUTED, true)
        addSpace(body, 10)
        addText(body, config?.displayName ?: "No hay perfiles", 20f, COLOR_TEXT, true)
        addSpace(body, 6)
        addText(
            body,
            if (config != null) {
                "Guardado localmente y cifrado con Android Keystore."
            } else {
                "Importa un archivo .conf de tu proveedor WireGuard para comenzar."
            },
            13f,
            COLOR_MUTED
        )
        addSpace(body, 18)
        body.addView(actionButton(
            if (config == null) "Importar archivo .conf" else "Reemplazar perfil",
            true
        ) { openConfigPicker() }, matchWrap())

        if (config != null) {
            addSpace(body, 9)
            body.addView(actionButton("Eliminar perfil", false) {
                confirmDeleteProfile()
            }, matchWrap())
        }
        content.addView(profileCard, matchWrap())

        addSpace(content, 18)
        val note = card(COLOR_SURFACE)
        val noteBody = verticalCardBody(note)
        addText(noteBody, "Tu configuración es privada", 16f, COLOR_TEXT, true)
        addSpace(noteBody, 5)
        addText(
            noteBody,
            "El archivo .conf contiene credenciales y claves privadas. USWire lo guarda cifrado en el almacenamiento privado de la aplicación. No lo publiques ni lo compartas.",
            13f,
            COLOR_MUTED
        )
        content.addView(note, matchWrap())
    }

    private fun renderSettings(content: LinearLayout) {
        addText(content, "PREFERENCIAS", 11f, COLOR_BLUE_LIGHT, true)
        addText(content, "Ajustes", 30f, COLOR_TEXT, true)
        addText(content, "Información de la aplicación y privacidad.", 14f, COLOR_MUTED)
        addSpace(content, 22)

        val privacyCard = card(COLOR_CARD)
        val privacyBody = verticalCardBody(privacyCard)
        addText(privacyBody, "Privacidad y seguridad", 19f, COLOR_TEXT, true)
        addSpace(privacyBody, 6)
        addText(
            privacyBody,
            "Conoce qué guarda USWire, cómo funciona el túnel y qué datos puede ver el proveedor VPN.",
            13f,
            COLOR_MUTED
        )
        addSpace(privacyBody, 14)
        privacyBody.addView(actionButton("Leer información de privacidad", true) {
            showPrivacyDialog()
        }, matchWrap())
        content.addView(privacyCard, matchWrap())

        addSpace(content, 14)
        val appCard = card(COLOR_SURFACE)
        val appBody = verticalCardBody(appCard)
        addText(appBody, "USWire para Android", 17f, COLOR_TEXT, true)
        addSpace(appBody, 6)
        val version = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "versión desconocida"
        } catch (_: Exception) {
            "versión desconocida"
        }
        addText(appBody, "Versión " + version, 13f, COLOR_MUTED)
        addText(appBody, "Motor de túnel: WireGuard oficial", 13f, COLOR_MUTED)
        addText(appBody, "Android mínimo: 10", 13f, COLOR_MUTED)
        content.addView(appCard, matchWrap())

        addSpace(content, 14)
        val warningCard = card(COLOR_SURFACE)
        val warningBody = verticalCardBody(warningCard)
        addText(warningBody, "Acerca de Play Protect", 16f, COLOR_TEXT, true)
        addSpace(warningBody, 5)
        addText(
            warningBody,
            "USWire necesita el permiso VPN para crear el túnel. Play Protect analiza las aplicaciones instaladas fuera de Google Play; una alerta de análisis no siempre significa que se haya encontrado malware.",
            13f,
            COLOR_MUTED
        )
        content.addView(warningCard, matchWrap())
    }

    private fun showPrivacyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Privacidad de USWire")
            .setMessage(
                "• USWire no incluye anuncios, analítica ni cuentas propias.\n\n" +
                    "• El perfil .conf se guarda cifrado en el almacenamiento privado del dispositivo, usando Android Keystore.\n\n" +
                    "• USWire no opera el servidor VPN. El tráfico del túnel se envía al endpoint indicado en el perfil que importaste. El proveedor de ese servidor puede procesar los metadatos y el tráfico conforme a sus propias políticas.\n\n" +
                    "• El permiso VPN de Android permite a WireGuard crear el túnel. No desactives Play Protect para usar la aplicación.\n\n" +
                    "No importes perfiles de fuentes desconocidas y no compartas tu archivo .conf."
            )
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun confirmDeleteProfile() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar perfil")
            .setMessage("Se borrará la configuración guardada en este dispositivo. El túnel activo no se detendrá automáticamente.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                configRepository.clear()
                Toast.makeText(this, "Perfil eliminado", Toast.LENGTH_SHORT).show()
                renderPage(PAGE_PROFILES)
            }
            .show()
    }

    private fun miniInfoCard(label: String, value: String, caption: String, accent: Int): MaterialCardView {
        val info = card(COLOR_CARD)
        val body = verticalCardBody(info, 14)
        addText(body, label, 10f, accent, true)
        addSpace(body, 8)
        addText(body, value, 15f, COLOR_TEXT, true)
        addSpace(body, 3)
        addText(body, caption, 11f, COLOR_MUTED)
        return info
    }

    private fun card(backgroundColor: Int): MaterialCardView {
        return MaterialCardView(this).apply {
            radius = dp(22).toFloat()
            cardElevation = 0f
            setCardBackgroundColor(backgroundColor)
            strokeWidth = dp(1)
            setStrokeColor(ColorStateList.valueOf(COLOR_STROKE))
            useCompatPadding = false
        }
    }

    private fun verticalCardBody(card: MaterialCardView, padding: Int = 20): LinearLayout {
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
        }
        card.addView(body, matchWrap())
        return body
    }

    private fun actionButton(label: String, primary: Boolean, onClick: () -> Unit): MaterialButton {
        return MaterialButton(this).apply {
            text = label
            isAllCaps = false
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            cornerRadius = dp(15)
            insetTop = 0
            insetBottom = 0
            minimumHeight = dp(52)
            setTextColor(if (primary) Color.WHITE else COLOR_TEXT)
            backgroundTintList = ColorStateList.valueOf(
                if (primary) COLOR_BLUE else COLOR_BUTTON_SECONDARY
            )
            setOnClickListener { onClick() }
        }
    }

    private fun addText(
        parent: LinearLayout,
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        val view = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = true
        }
        parent.addView(view, matchWrap())
        return view
    }

    private fun addSpace(parent: LinearLayout, heightDp: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(heightDp)))
    }

    private fun smallPill(label: String, backgroundColor: Int, textColor: Int): TextView {
        return TextView(this).apply {
            text = label
            textSize = 9f
            letterSpacing = 0.08f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColor)
            gravity = Gravity.CENTER
            setPadding(dp(11), dp(7), dp(11), dp(7))
            background = rounded(backgroundColor, dp(30))
        }
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun matchWrap(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

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
            Toast.makeText(this, "Primero importa un perfil .conf.", Toast.LENGTH_LONG).show()
            bottomNavigation.selectedItemId = PAGE_PROFILES
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
            Toast.makeText(this, "No hay un perfil guardado.", Toast.LENGTH_LONG).show()
            renderPage(PAGE_PROFILES)
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

    private fun renderVpnState(state: VpnState, message: String?) {
        vpnState = state
        vpnError = message
        if (currentPage == PAGE_HOME && ::contentFrame.isInitialized) {
            renderPage(PAGE_HOME)
        } else if (currentPage == PAGE_PROFILES && ::contentFrame.isInitialized) {
            renderPage(PAGE_PROFILES)
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
                    Toast.makeText(
                        this,
                        "Android no autorizó el permiso VPN.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            REQUEST_CONFIG -> {
                if (resultCode != RESULT_OK) return
                val uri = data?.data ?: return
                val configContent = try {
                    contentResolver.openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use { it.readText() }
                } catch (_: Exception) {
                    null
                }

                if (configContent.isNullOrBlank()) {
                    Toast.makeText(
                        this,
                        "No se pudo leer el archivo seleccionado.",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val displayName = uri.lastPathSegment
                    ?.substringAfterLast('/')
                    ?.takeIf { it.isNotBlank() }
                    ?: "WireGuard.conf"

                val result = importConfig.execute(displayName, configContent)
                result.onSuccess {
                    Toast.makeText(this, "Perfil importado y guardado cifrado.", Toast.LENGTH_LONG).show()
                    renderPage(PAGE_PROFILES)
                }.onFailure {
                    Toast.makeText(
                        this,
                        it.localizedMessage ?: "El perfil WireGuard no es válido.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    companion object {
        private const val PAGE_HOME = 1
        private const val PAGE_PROFILES = 2
        private const val PAGE_SETTINGS = 3
        private const val REQUEST_CONFIG = 4102
        private const val REQUEST_VPN_PERMISSION = 4103

        private val COLOR_BACKGROUND = 0xFF0A1020.toInt()
        private const val COLOR_SURFACE = 0xFF111B2E.toInt()
        private const val COLOR_CARD = 0xFF162238.toInt()
        private const val COLOR_STROKE = 0xFF263650.toInt()
        private const val COLOR_BUTTON_SECONDARY = 0xFF263650.toInt()
        private const val COLOR_TEXT = 0xFFF4F7FC.toInt()
        private const val COLOR_MUTED = 0xFFA1AEC2.toInt()
        private const val COLOR_BLUE = 0xFF367EF5.toInt()
        private const val COLOR_BLUE_SOFT = 0xFF1B3155.toInt()
        private const val COLOR_BLUE_LIGHT = 0xFF80B4FF.toInt()
        private const val COLOR_GREEN = 0xFF42D6A0.toInt()
        private const val COLOR_AMBER = 0xFFFFC36B.toInt()
        private const val COLOR_RED = 0xFFFF7777.toInt()
    }
}
