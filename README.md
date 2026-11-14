# USWire Android — 0.3.0-alpha

Cliente Android independiente para importar perfiles WireGuard y controlar túneles VPN mediante el motor oficial WireGuard para Android.

## Interfaz 0.3.0-alpha
- Panel principal oscuro con estado de conexión y acción principal.
- Navegación inferior entre Inicio, Perfiles y Ajustes.
- Biblioteca de perfil importado, reemplazo y eliminación.
- Resumen de privacidad integrado en la aplicación.
- Icono de aplicación propio.

## Funciones VPN
- Importación de perfiles `.conf` desde el selector de documentos de Android.
- Validación sintáctica mediante el parser oficial de WireGuard.
- Almacenamiento local cifrado con AES-GCM y Android Keystore.
- Backend oficial WireGuard Tunnel/GoBackend.
- Solicitud de permiso VPN del sistema Android.
- Conectar y desconectar el túnel.

## Compatibilidad
- Android 10 o posterior (minSdk 29).
- JDK 17 y Gradle 8.9 para compilación.

## Privacidad y seguridad
- Consulta [la política de privacidad](docs/PRIVACY.md).
- Consulta [la guía de Play Protect y distribución](docs/PLAY_PROTECT.md).
- No subas archivos `.conf`: contienen claves privadas.
- USWire no opera servidores ni proporciona ancho de banda. El proveedor del perfil controla el endpoint y su política de privacidad.
- No desactives Play Protect para instalar la aplicación.

## Compilación
Abre **Actions → Android CI**. El workflow ejecuta pruebas y genera un APK debug para pruebas. Cuando configures los secretos de firma, también genera un APK release firmado.

### Firma release opcional
Configura los secretos del repositorio indicados en [docs/PLAY_PROTECT.md](docs/PLAY_PROTECT.md). Conserva siempre la misma clave para las actualizaciones. No descargues ni compartas el keystore desde el chat.
