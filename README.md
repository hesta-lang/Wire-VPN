# USWire Android — 0.2.0-alpha

Cliente Android experimental para importar perfiles WireGuard y establecer túneles VPN mediante el motor oficial WireGuard para Android.

## Requisitos
- Android 10 o posterior (minSdk 29).
- JDK 17 y Gradle 8.9 para compilación.
- Un archivo de configuración WireGuard .conf y un servidor/proveedor autorizado.

## Funciones de esta alpha
- Selector de archivos Android para importar perfiles .conf.
- Validación sintáctica con el parser oficial de WireGuard.
- Almacenamiento local cifrado con AES-GCM y una clave administrada por Android Keystore.
- Motor oficial WireGuard Tunnel/GoBackend integrado como dependencia de Maven Central.
- Solicitud del permiso VPN del sistema Android.
- Controles para conectar y desconectar desde USWire.
- Workflow de GitHub Actions para ejecutar pruebas y generar un APK debug.

## Estado y limitaciones
Esta versión incorpora el backend real, pero todavía requiere pruebas en dispositivos Android físicos y con perfiles de proveedores autorizados. La compilación del APK no demuestra por sí sola que un servidor concreto acepte la conexión.

USWire es un cliente: no proporciona servidores, cuentas, GB gratuitos ni ancho de banda. Los límites y ubicaciones dependen del proveedor del perfil .conf.

## Seguridad
- No subas perfiles .conf a GitHub ni los compartas públicamente: contienen claves privadas.
- Los perfiles importados se guardan cifrados en el almacenamiento privado de la aplicación.
- No uses esta alpha para tráfico sensible hasta completar las pruebas de conexión, reconexión, DNS e IPv6.

## Compilar
Abre **Actions → Android CI** y ejecuta el workflow. El APK debug aparecerá como artifact si el trabajo finaliza correctamente.
