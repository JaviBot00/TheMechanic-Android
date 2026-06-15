# Workshop Management — Cliente Android

Cliente Android nativo para la [Workshop Management REST API](https://github.com/tu-usuario/workshop-management-api), un sistema integral de gestión para talleres mecánicos.

## ¿Qué hace esta aplicación?

Permite gestionar el ciclo completo de un taller: clientes, vehículos, mecánicos y órdenes de trabajo, con control de acceso por roles y sincronización en tiempo real con el backend Spring Boot.

## Características principales

- **Autenticación JWT** con refresco automático de token transparente al usuario
- **Control de acceso por rol** — la interfaz se adapta automáticamente según el rol (ADMIN / MECHANIC / CLIENT)
- **Gestión de clientes** — listado, búsqueda, alta, edición y baja lógica
- **Gestión de vehículos** — vinculados a clientes, con historial de reparaciones
- **Gestión de mecánicos** — especialidades, asignación de tareas
- **Órdenes de trabajo** — ciclo completo: diagnóstico → horas trabajadas → finalización → cobro
- **Panel de resumen** — estadísticas globales del taller (ADMIN y MECHANIC)

## Tech Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | Clean Architecture + MVVM |
| DI | Hilt |
| Red | Retrofit 3 + OkHttp 4 |
| Async | Kotlin Coroutines + Flow |
| Almacenamiento seguro | DataStore + Android Keystore |

## Documentación

- [`ARCHITECTURE.md`](./ARCHITECTURE.md) — arquitectura detallada, estructura de carpetas y convenciones
- [`docs/setup.md`](./docs/setup.md) — guía de instalación y configuración del entorno
- [`docs/api-integration.md`](./docs/api-integration.md) — integración con el backend

## Requisitos

- Android Studio Meerkat (2025.3.x) o superior
- JDK 17
- Backend Workshop Management API corriendo localmente o en red accesible

## Configuración rápida

1. Clona el repositorio
2. Crea el fichero `local.properties` en la raíz del proyecto:

   ```properties
   api.base.url=http://TU_IP_LOCAL:8080/
   ```

3. Sincroniza Gradle y ejecuta la app

> Si usas el emulador de Android Studio, la IP del host es `10.0.2.2`.
> Si usas un dispositivo físico en la misma red, usa la IP de tu máquina.

## Licencia

MIT
