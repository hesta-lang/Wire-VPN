# USWire Android — 0.1.0-alpha

Cliente Android experimental para importar y administrar perfiles WireGuard.

## Requisitos
- Android 10 o posterior (minSdk 29).
- JDK 17 y Gradle 8.9 para compilación.
- Un archivo de configuración WireGuard `.conf` y un servidor/proveedor autorizado.

## Funciones incluidas en esta alpha
- Aplicación Android y pantalla inicial.
- Selector para importar archivos de configuración `.conf`.
- Validación estructural básica de las secciones `[Interface]` y `[Peer]`.
- Guardado local cifrado con AES-GCM y una clave administrada por Android Keystore.
- Pruebas unitarias para el validador.
- Workflow de GitHub Actions para ejecutar pruebas y generar un APK debug.

## Estado
La importación y el almacenamiento local están implementados como base experimental. **La conexión real todavía no está implementada**: `WireGuardController` es únicamente el contrato para integrar el backend oficial WireGuard Tunnel/GoBackend. La interfaz no simula una conexión.

Para compilar, abre **Actions → Android CI** y ejecuta el workflow. El APK debug aparecerá como artifact si el trabajo finaliza correctamente.

No uses esta alpha para tráfico sensible. USWire no proporciona servidores, GB gratuitos ni ancho de banda ilimitado; eso depende del servidor o proveedor al que se conecte.
