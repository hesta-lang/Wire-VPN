# USWire Android

Cliente Android independiente para importar perfiles WireGuard y controlar túneles VPN mediante el motor oficial WireGuard para Android.

## Estado actual

- Versión de desarrollo del repositorio: `0.4.0-alpha`.
- Android 10 o posterior (API 29+).
- Interfaz con Inicio, Perfiles y Ajustes.
- Biblioteca de múltiples perfiles WireGuard con selección del perfil activo.
- Importación, validación y almacenamiento cifrado de perfiles WireGuard.
- Migración automática del perfil único de versiones anteriores sin eliminar el archivo cifrado antiguo.
- Conexión y desconexión mediante el backend oficial WireGuard para Android.

## Distribución

USWire se distribuye como una aplicación Android independiente. El canal de distribución previsto es **GitHub Releases**, con APK release firmado. No se requiere instalar Geode ni otra aplicación.

El workflow **Android CI** ejecuta pruebas y genera un APK debug únicamente para pruebas. Ese archivo no es el paquete recomendado para distribuir a usuarios.

El workflow **Publish Android Release** se ejecuta al publicar una etiqueta Git que siga el formato `vMAJOR.MINOR.PATCH` o `vMAJOR.MINOR.PATCH-suffix`. Por ejemplo, `v0.3.2-alpha`. Compila el APK release firmado, verifica su firma, comprueba que el identificador de aplicación siga siendo `com.hestalang.uswire`, valida el `versionCode` incremental, calcula su SHA-256 y crea un GitHub Release. El `versionCode` aumenta con cada ejecución de ese workflow. Las etiquetas que contienen un guion (por ejemplo, `-alpha`) se publican como prerelease.

### Configurar la firma (una sola vez)

En **Settings → Secrets and variables → Actions**, crea estos cuatro Repository secrets:

- `USWIRE_KEYSTORE_BASE64`: keystore JKS codificado en Base64.
- `USWIRE_STORE_PASSWORD`: contraseña del keystore.
- `USWIRE_KEY_ALIAS`: alias de la clave de firma.
- `USWIRE_KEY_PASSWORD`: contraseña de la clave.

Conserva una copia de seguridad segura del keystore y sus contraseñas. **No los subas al repositorio, no los incluyas en un issue y no los compartas en el chat.** Perder la clave impedirá firmar actualizaciones compatibles con las instalaciones existentes. No generes una clave nueva para cada versión.

Después de configurar los secretos, crea y publica una etiqueta como `v0.3.2-alpha`. Consulta [la guía de distribución y Play Protect](docs/PLAY_PROTECT.md) para los pasos completos.

### Actualizar sin desinstalar

Para instalar una versión nueva encima de la anterior, descarga el APK firmado desde **Assets** del GitHub Release y ábrelo en Android. No desinstales USWire ni uses el APK debug de Android CI para actualizar una instalación release.

Las actualizaciones conservan los datos privados de la aplicación cuando se mantienen el mismo `applicationId` (`com.hestalang.uswire`) y la misma clave JKS de firma, y el nuevo APK tiene un `versionCode` mayor. El workflow release reutiliza los cuatro secretos de firma y aumenta el código de versión en cada publicación. No reemplaces ni regeneres el keystore.

Al primer inicio tras actualizar desde una versión que guardaba un único perfil, USWire lo migra a la nueva biblioteca cifrada. El archivo cifrado original se conserva como copia de recuperación. Los temas y demás preferencias también permanecen en el almacenamiento de la aplicación.

## Seguridad y privacidad

- Consulta [la política de privacidad](docs/PRIVACY.md).
- Consulta [la guía de distribución y Play Protect](docs/PLAY_PROTECT.md).
- No subas archivos `.conf`: contienen claves privadas.
- USWire no opera servidores ni proporciona ancho de banda. El proveedor del perfil controla el endpoint y su política de privacidad.
- No desactives Google Play Protect para instalar la aplicación.

## Desarrollo

Abre **Actions → Android CI** para ejecutar pruebas y descargar un APK debug de prueba. La publicación de APKs release se realiza únicamente mediante etiquetas Git y el workflow de distribución.
