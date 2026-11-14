# Privacidad de USWire

**Última actualización: 13 de noviembre de 2026**

USWire es un cliente Android experimental para importar una configuración WireGuard y crear un túnel VPN mediante el backend oficial de WireGuard para Android.

## Datos que procesa la aplicación

- **Perfiles WireGuard:** al importar un archivo `.conf`, USWire lee su contenido para validarlo y guardarlo cifrado en el almacenamiento privado de la aplicación.
- **Claves y credenciales:** los datos del perfil se cifran localmente con AES-GCM y una clave administrada por Android Keystore. No deben compartirse ni subirse a repositorios.
- **Tráfico VPN:** cuando el usuario conecta, el backend WireGuard envía el tráfico al endpoint definido por el propio perfil. USWire no opera ese servidor y no puede establecer las prácticas de registro, retención o privacidad de su proveedor.
- **Permiso VPN:** Android solicita autorización explícita para crear el túnel.

## Datos que USWire no recopila

La versión actual no integra cuentas propias, anuncios, SDK de analítica, rastreadores ni un servicio remoto de telemetría. No se transmite el archivo de configuración a servidores operados por USWire.

## Servicios de terceros

USWire utiliza el motor oficial WireGuard para Android. El servidor VPN indicado en el perfil es un servicio de terceros y tiene sus propias políticas. Consulta esas políticas antes de enviar tráfico por él.

## Controles del usuario

Puedes eliminar el perfil guardado desde la sección **Perfiles**. También puedes detener el túnel desde la pantalla principal o desde los controles VPN de Android.

## Seguridad

Instala USWire únicamente desde una fuente que puedas verificar. No desactives Google Play Protect para instalarla. Si Play Protect indica que la app es dañina, no ignores la advertencia: revisa la clasificación y comunícate con el desarrollador o consulta el proceso oficial de apelación.

## Contacto

Para informar un problema de privacidad o seguridad, abre un issue en el repositorio oficial:
https://github.com/hesta-lang/Wire-VPN/issues
