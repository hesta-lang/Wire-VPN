# USWire Android — 0.1.0-alpha

Cliente Android experimental para administrar túneles WireGuard. Esta entrega es un **esqueleto de proyecto**, no un APK terminado: todavía requiere integrar y probar el ciclo real de `GoBackend` de WireGuard en un dispositivo Android 10+.

## Requisitos
- Android Studio / JDK 17 para compilar localmente, o GitHub Actions.
- Android 10+ (minSdk 29).
- Un archivo `.conf` WireGuard válido y un servidor/proveedor autorizado. La aplicación no incluye servidores ni proporciona datos ilimitados.

## Compilar
Abre el repositorio en GitHub y ejecuta el workflow **Android CI** desde Actions. El workflow compila el proyecto y publica el APK como artifact si la compilación termina correctamente.

## Estado
- [x] Estructura Android y workflow CI.
- [x] Pantalla inicial y selector de archivo `.conf`.
- [ ] Integración completa con WireGuard Tunnel/GoBackend.
- [ ] Pruebas de conexión, reconexión y ciclo de vida en Android 10.
- [ ] Almacenamiento cifrado de configuraciones.

No uses esta versión para tráfico sensible. No contiene una VPN funcional hasta completar los puntos pendientes.
