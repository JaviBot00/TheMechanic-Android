# Arquitectura del cliente Android — Workshop Management

Este documento describe las decisiones de arquitectura del proyecto, la estructura de carpetas, el flujo de datos y las convenciones de código que se siguen en toda la base de código.

---

## Patrón arquitectónico

El proyecto combina **Clean Architecture** (tres capas con dependencias en una sola dirección) con el patrón **MVVM** en la capa de presentación. Es el estándar recomendado por Google para aplicaciones Android de producción en 2026.

```cmd
┌─────────────────────────────────────────┐
│           UI / Presentación             │  Composables + ViewModels
│         (depende de: Dominio)           │
├─────────────────────────────────────────┤
│              Dominio                    │  Use Cases + interfaces de Repositorio
│       (no depende de nadie)             │  ← Kotlin puro, sin Android
├─────────────────────────────────────────┤
│               Datos                     │  Retrofit + DataStore + impl. Repositorios
│         (depende de: Dominio)           │
└─────────────────────────────────────────┘
            ▲
            │
           DI (Hilt conecta las capas en tiempo de ejecución)
```

La regla fundamental es que **las dependencias siempre apuntan hacia dentro**: la capa de datos conoce el dominio, pero el dominio no sabe nada de Retrofit ni de DataStore. La capa UI conoce el dominio (a través de use cases), pero nunca toca la capa de datos directamente.

---

## Estructura de carpetas

```cmd
app/src/main/java/com/workshopmanagement/android/
│
├── WorkshopManagementApp.kt       # Clase Application anotada con @HiltAndroidApp
├── MainActivity.kt                # Única Activity — punto de entrada de Compose
│
├── di/                            # Módulos Hilt (proveen dependencias)
│   ├── NetworkModule.kt           # OkHttpClient, Retrofit, servicios API
│   ├── RepositoryModule.kt        # Binding interface → implementación
│   └── StorageModule.kt           # DataStore, TokenDataStore
│
├── data/                          # CAPA DE DATOS
│   ├── remote/
│   │   ├── api/                   # Interfaces Retrofit (un fichero por dominio)
│   │   │   ├── AuthApiService.kt
│   │   │   ├── ClientApiService.kt
│   │   │   ├── VehicleApiService.kt
│   │   │   ├── MechanicApiService.kt
│   │   │   ├── TaskApiService.kt
│   │   │   └── ReportApiService.kt
│   │   ├── dto/                   # Data Transfer Objects (lo que devuelve la API)
│   │   │   ├── auth/
│   │   │   ├── client/
│   │   │   ├── vehicle/
│   │   │   ├── mechanic/
│   │   │   ├── task/
│   │   │   └── report/
│   │   ├── interceptor/
│   │   │   └── AuthInterceptor.kt # Añade "Authorization: Bearer <token>" a cada request
│   │   └── authenticator/
│   │       └── TokenAuthenticator.kt # Refresca el token ante un 401 y reintenta
│   ├── local/
│   │   └── TokenDataStore.kt      # Almacenamiento cifrado de tokens (Keystore + DataStore)
│   └── repository/                # Implementaciones concretas de los repositorios
│       ├── AuthRepositoryImpl.kt
│       ├── ClientRepositoryImpl.kt
│       ├── VehicleRepositoryImpl.kt
│       ├── MechanicRepositoryImpl.kt
│       ├── TaskRepositoryImpl.kt
│       └── ReportRepositoryImpl.kt
│
├── domain/                        # CAPA DE DOMINIO — Kotlin puro, cero dependencias Android
│   ├── model/                     # Entidades de negocio (distintas de los DTOs de red)
│   │   ├── AuthToken.kt
│   │   ├── Client.kt
│   │   ├── Vehicle.kt
│   │   ├── Mechanic.kt
│   │   ├── WorkshopTask.kt
│   │   ├── SummaryReport.kt
│   │   └── UserRole.kt
│   ├── repository/                # Contratos (interfaces) que la capa de datos implementa
│   │   ├── AuthRepository.kt
│   │   ├── ClientRepository.kt
│   │   ├── VehicleRepository.kt
│   │   ├── MechanicRepository.kt
│   │   ├── TaskRepository.kt
│   │   └── ReportRepository.kt
│   └── usecase/                   # Un use case por acción de negocio
│       ├── auth/
│       │   ├── LoginUseCase.kt
│       │   └── LogoutUseCase.kt
│       ├── client/
│       │   ├── GetClientsUseCase.kt
│       │   ├── GetClientByIdUseCase.kt
│       │   ├── CreateClientUseCase.kt
│       │   ├── UpdateClientUseCase.kt
│       │   └── DeleteClientUseCase.kt
│       ├── vehicle/
│       ├── mechanic/
│       ├── task/
│       │   ├── AddHoursToTaskUseCase.kt
│       │   ├── FinishTaskUseCase.kt
│       │   └── MarkTaskAsPaidUseCase.kt
│       └── report/
│           └── GetSummaryReportUseCase.kt
│
└── ui/                            # CAPA DE PRESENTACIÓN
    ├── navigation/
    │   ├── AppNavGraph.kt         # Grafo de navegación completo
    │   ├── AppRoutes.kt           # Definición de rutas type-safe (sealed class)
    │   └── NavigationExtensions.kt
    ├── theme/
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    ├── components/                # Composables reutilizables (botones, cards, loaders...)
    ├── auth/
    │   ├── LoginScreen.kt
    │   └── LoginViewModel.kt
    ├── dashboard/
    │   ├── DashboardScreen.kt
    │   └── DashboardViewModel.kt
    ├── client/
    │   ├── list/
    │   ├── detail/
    │   └── form/
    ├── vehicle/
    ├── mechanic/
    ├── task/
    └── report/
```

---

## Flujo de datos (unidireccional)

Cada pantalla sigue exactamente el mismo patrón:

```cmd
Usuario toca botón
       │
       ▼
  Composable llama a ViewModel.onAction(...)
       │
       ▼
  ViewModel invoca UseCase (suspend fun en viewModelScope)
       │
       ▼
  UseCase llama a Repository (interfaz de dominio)
       │
       ▼
  RepositoryImpl hace la llamada Retrofit / DataStore
       │
       ▼
  Devuelve Result<T>  ──→  RepositoryImpl mapea DTO → modelo de dominio
       │
       ▼
  ViewModel actualiza _uiState (MutableStateFlow<UiState>)
       │
       ▼
  Composable recoge el estado con collectAsStateWithLifecycle()
       │
       ▼
  Recomposición automática de la UI
```

---

## Gestión de autenticación JWT

El backend implementa **JWT de corta duración** (access token, 1 h) más **refresh token de larga duración** (7 días) con rotación: cada vez que se usa el refresh token, el servidor lo invalida y emite uno nuevo.

### AuthInterceptor

Se ejecuta en **cada petición saliente**. Lee el access token del `TokenDataStore` y añade la cabecera `Authorization: Bearer <token>`. No hace ninguna lógica de refresco.

### TokenAuthenticator

Se ejecuta **solo cuando el servidor devuelve un 401**. OkHttp llama a este componente automáticamente antes de propagar el error a Retrofit. El authenticator:

1. Comprueba si ya hay un refresco en curso (para evitar llamadas duplicadas con múltiples peticiones concurrentes fallidas).
2. Llama al endpoint `/auth/refresh` de forma síncrona.
3. Si tiene éxito → guarda los nuevos tokens en `TokenDataStore` y reintenta la petición original con el nuevo access token.
4. Si falla (refresh token expirado o revocado) → limpia los tokens almacenados y emite una señal de logout para que la app navegue a la pantalla de login.

### Almacenamiento de tokens

Los tokens son datos sensibles. El proceso de almacenamiento usa dos capas:

```
Texto del token
      │  cifrar con AES-GCM
      ▼
Bytes cifrados  ──→  guardados en DataStore (fichero en disco privado de la app)
      ▲
      │  La clave AES nunca sale de aquí
Android Keystore (respaldado por hardware en dispositivos modernos)
```

`EncryptedSharedPreferences` **no se usa** porque está deprecado desde la librería `security-crypto` 1.1.x. La alternativa es cifrado manual con la API de Keystore, que da más control y no tiene los problemas de compatibilidad entre dispositivos del enfoque anterior.

---

## Gestión de roles

El `SessionManager` (singleton `@Singleton` de Hilt, en memoria) almacena el rol del usuario autenticado. El rol se extrae de las claims del JWT al hacer login y se descarta al hacer logout.

La UI usa el rol de tres formas:

| Uso | Cómo |
|---|---|
| Destino inicial tras login | El NavGraph lee `SessionManager.role` para decidir la primera pantalla |
| Visibilidad de botones de acción | Los Composables reciben el rol del ViewModel y renderizan condicionalmente |
| Protección de rutas | El NavGraph impide navegar a rutas no permitidas para el rol actual |

### Mapa de permisos por pantalla

| Pantalla | ADMIN | MECHANIC | CLIENT |
|---|---|---|---|
| Dashboard / Resumen | ✅ | ✅ | ❌ |
| Lista de clientes | ✅ | ✅ | ❌ |
| Detalle de cliente | ✅ | ✅ | Solo el suyo |
| Crear / Editar cliente | ✅ | ✅ | ❌ |
| Eliminar cliente | ✅ | ❌ | ❌ |
| Lista de vehículos | ✅ | ✅ | ❌ |
| Mis vehículos | ✅ | ✅ | ✅ |
| Lista de mecánicos | ✅ | ✅ | ❌ |
| Crear / Editar mecánico | ✅ | ❌ | ❌ |
| Lista de tareas | ✅ | ✅ | Solo las suyas |
| Añadir horas a tarea | ✅ | ✅ | ❌ |
| Finalizar tarea | ✅ | ✅ | ❌ |
| Marcar tarea como pagada | ✅ | ❌ | ❌ |
| Eliminar tarea | ✅ | ❌ | ❌ |
| Reporte de resumen | ✅ | ✅ | ❌ |

---

## Convenciones de código

### UiState

Cada ViewModel expone exactamente un `StateFlow<UiState>`. El `UiState` es una sealed class con tres estados mínimos:

```kotlin
sealed class ClientListUiState {
    data object Loading : ClientListUiState()
    data class Success(val clients: List<Client>) : ClientListUiState()
    data class Error(val message: String) : ClientListUiState()
}
```

### Result<T>

Todos los métodos de repositorio devuelven `Result<T>` de la stdlib de Kotlin. La capa de datos envuelve las llamadas de red en `runCatching { }`. El ViewModel desenvuelve el resultado:

```kotlin
viewModelScope.launch {
    _uiState.value = ClientListUiState.Loading
    getClientsUseCase()
        .onSuccess { clients -> _uiState.value = ClientListUiState.Success(clients) }
        .onFailure { e -> _uiState.value = ClientListUiState.Error(e.message ?: "Error desconocido") }
}
```

### Use Cases

- Nombre: verbo + sustantivo + `UseCase` → `GetClientByIdUseCase`, `MarkTaskAsPaidUseCase`
- Un único método público: `operator fun invoke(...)` (permite llamarlos como `getClientByIdUseCase(id)`)
- Son `suspend fun` cuando hacen operaciones de red o disco

### Mappers

Los DTOs de Retrofit nunca salen de la capa de datos. La conversión se hace con funciones de extensión en el mismo paquete:

```kotlin
// En data/remote/dto/client/ClientDto.kt
fun ClientDto.toDomain(): Client = Client(id = id, name = name, ...)
```

### Nomenclatura de ficheros

| Tipo | Ejemplo |
|---|---|
| Screen (Composable) | `ClientListScreen.kt` |
| ViewModel | `ClientListViewModel.kt` |
| UiState | Declarado dentro del ViewModel o en un fichero `ClientListUiState.kt` |
| DTO | `ClientDto.kt`, `ClientRequestDto.kt` |
| Modelo de dominio | `Client.kt` |
| Use Case | `GetClientsUseCase.kt` |
| Repositorio (interfaz) | `ClientRepository.kt` |
| Repositorio (impl) | `ClientRepositoryImpl.kt` |

---

## Stack tecnológico

| Componente | Librería | Versión |
|---|---|---|
| Lenguaje | Kotlin | 2.2.x |
| Build system | Android Gradle Plugin | 8.10.x |
| UI | Jetpack Compose BOM | 2026.05.00 |
| Diseño | Material 3 | (vía BOM) |
| Inyección de dependencias | Hilt | 2.57.x |
| Navegación | Navigation Compose | 2.9.x |
| Red | Retrofit | 3.0.0 |
| Cliente HTTP | OkHttp | 4.12.x |
| Serialización JSON | kotlinx-serialization | 1.8.x |
| Async / reactivo | Coroutines + Flow | 1.10.x |
| Almacenamiento seguro | DataStore + Android Keystore | 1.1.x |
| Carga de imágenes | Coil 3 | 3.1.x |
| Tests unitarios | JUnit 4 + MockK + Turbine | — |
| Tests de UI | Compose Testing + Espresso | — |

---

## Orden de implementación

El proyecto se construye capa por capa, de dentro hacia afuera, para que cada parte sea testeable antes de que la siguiente dependa de ella:

1. **Esqueleto** — `WorkshopManagementApp`, `MainActivity`, tema Compose base
2. **Almacenamiento de tokens** — `TokenDataStore` con cifrado Keystore
3. **Capa de red** — `AuthInterceptor`, `TokenAuthenticator`, instancia Retrofit, servicio de autenticación
4. **Modelos de dominio e interfaces de repositorio** — un fichero por agregado
5. **Implementaciones de repositorio** — mapeo DTO → dominio, envuelto en `Result<T>`
6. **Use Cases** — uno por operación de negocio
7. **Grafo de navegación** — rutas type-safe, destino inicial según rol
8. **Pantallas** — Login → Dashboard → CRUD por dominio
9. **Visibilidad por roles** — `SessionManager`, renderizado condicional en Composables
10. **Tests** — unitarios e instrumentados junto a cada capa
