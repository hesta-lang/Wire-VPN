# Distribución de USWire y Google Play Protect

USWire es un cliente VPN Android que importa perfiles WireGuard y crea un túnel mediante el backend oficial de WireGuard para Android. La aplicación declara el permiso de Internet y utiliza el permiso VPN del sistema cuando el usuario inicia una conexión. No desactiva Play Protect, no solicita exclusiones y no intenta ocultar su comportamiento.

## Comprender el aviso de Play Protect

El aviso que dice **«Play Protect no vio una app de este desarrollador antes. Podría no ser segura»** corresponde a una aplicación que Google no ha visto antes de ese desarrollador. Por sí solo no afirma que se haya encontrado malware. Es distinto de una alerta que identifica explícitamente la aplicación como dañina o potencialmente dañina.

No se debe intentar ocultar el paquete ni alterar el comportamiento de la aplicación para esquivar el análisis. Si aparece una detección explícita de aplicación dañina, detén la distribución de ese APK, revisa el paquete y sus dependencias y sigue el proceso oficial de revisión de Google.

## Canales de distribución

USWire utiliza un modelo de distribución independiente, similar al de proyectos Android que publican sus instaladores en GitHub:

- **Android CI:** ejecuta pruebas y produce un APK debug marcado para pruebas. No es el artefacto de distribución.
- **GitHub Releases:** publica APKs release firmados después de compilar, verificar la firma y calcular el SHA-256.
- **Pruebas alpha:** las etiquetas con sufijo, como `v0.3.2-alpha`, se publican como prereleases.
- **Versiones estables:** las etiquetas sin sufijo, como `v1.0.0`, se publican como releases normales.

La distribución independiente no garantiza que Play Protect retire el aviso de desarrollador desconocido. La reputación y las decisiones de detección corresponden a Google.

## Firma persistente

El workflow **Publish Android Release** requiere estos GitHub Actions Repository secrets:

- `USWIRE_KEYSTORE_BASE64`: keystore JKS codificado en Base64.
- `USWIRE_STORE_PASSWORD`: contraseña del keystore.
- `USWIRE_KEY_ALIAS`: alias de la clave de firma.
- `USWIRE_KEY_PASSWORD`: contraseña de la clave.

La misma clave debe utilizarse en cada actualización. Guarda una copia cifrada de respaldo fuera del repositorio y limita quién puede acceder a ella. No guardes el keystore ni las contraseñas en Git, artefactos, issues o mensajes. Si se pierde la clave, no se podrán producir actualizaciones firmadas con la identidad de la instalación anterior.

El workflow verifica la firma del APK mediante Android SDK Build Tools y adjunta al release un archivo de verificación con el SHA-256 y los datos del certificado de firma. Comprueba esos datos al descargar el instalador.

## Proceso de publicación

1. Configura los cuatro secretos de firma en **Settings → Secrets and variables → Actions**.
2. Ejecuta las pruebas con **Android CI** y revisa el resultado.
3. Crea una etiqueta siguiendo el formato `vMAJOR.MINOR.PATCH` o `vMAJOR.MINOR.PATCH-suffix`, por ejemplo `v0.3.2-alpha`.
4. Publica la etiqueta en el repositorio. GitHub Actions compilará el APK release y creará el GitHub Release si las pruebas, la firma y la verificación terminan correctamente.
5. Descarga el APK desde la página Releases del repositorio y comprueba su SHA-256 contra el archivo adjunto.

## Comprobaciones de seguridad

Antes de distribuir una versión:

1. Instala únicamente APKs publicados en el repositorio oficial.
2. Revisa los permisos del manifiesto fusionado y conserva solo los requeridos por las funciones de USWire y WireGuard.
3. Mantén al día la biblioteca WireGuard y sus dependencias.
4. Explica que el tráfico se enruta al servidor indicado por el perfil importado.
5. Si Google muestra una clasificación explícita de aplicación dañina, no ignores el bloqueo. Revisa el APK exacto y presenta una apelación si se confirma que es un falso positivo.

## Documentación oficial

- [Orientación para desarrolladores sobre advertencias de Play Protect](https://developers.google.com/android/play-protect/warning-dev-guidance)
- [Ayuda de Google Play Protect](https://support.google.com/googleplay/answer/2812853)
- [GitHub Releases](https://docs.github.com/repositories/releasing-projects-on-github/about-releases)
