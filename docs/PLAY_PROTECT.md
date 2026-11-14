# Google Play Protect y distribución de USWire

USWire es un cliente VPN que necesita el permiso de VPN de Android para iniciar el backend WireGuard. La aplicación declara el permiso de Internet y utiliza la biblioteca oficial WireGuard para establecer el túnel. No desactiva Play Protect, no solicita exclusiones y no intenta ocultar su comportamiento.

## Dos avisos distintos

- **“Play Protect no ha visto esta app antes” / “Se recomienda analizar la app”:** puede aparecer al instalar una aplicación distribuida fuera de Google Play que todavía no tiene reputación de distribución. No equivale por sí solo a una confirmación de malware.
- **“App dañina bloqueada” / “Aplicación potencialmente dañina”:** es una clasificación de seguridad. No debe ignorarse ni sortearse. Hay que revisar el código, los permisos y el paquete exacto, y apelar si se trata de un falso positivo.

## Qué debe comprobarse antes de distribuir

1. Instalar solo APKs generados por el workflow público del repositorio.
2. Mantener una clave de firma de release privada y constante para todas las actualizaciones.
3. Revisar los permisos del manifiesto fusionado; conservar únicamente los necesarios para WireGuard.
4. Publicar esta información de privacidad y explicar que el tráfico se enruta al servidor indicado por el perfil importado.
5. Si Google confirma una clasificación incorrecta, presentar una apelación oficial con el APK exacto, el package name `com.hestalang.uswire` y la versión afectada.

## Firma de release en GitHub Actions

El workflow puede generar un APK release firmado cuando se configuren estos GitHub Actions Secrets:

- `USWIRE_KEYSTORE_BASE64`: archivo JKS codificado en Base64.
- `USWIRE_STORE_PASSWORD`: contraseña del keystore.
- `USWIRE_KEY_ALIAS`: alias de la clave.
- `USWIRE_KEY_PASSWORD`: contraseña de la clave.

No guardes el JKS ni las contraseñas en el repositorio, en el APK, en issues o en el chat. Si los secretos no están configurados, el workflow solo genera el APK debug para pruebas; ese APK no es la distribución final.

La firma estable ayuda a verificar que las actualizaciones vienen del mismo desarrollador, pero **no garantiza** que Play Protect retire una clasificación. La reputación del paquete y la decisión de seguridad pertenecen a Google.
