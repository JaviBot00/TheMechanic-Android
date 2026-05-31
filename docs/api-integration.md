# Integración con el backend

Este documento describe cómo el cliente Android se comunica con la
[Workshop Management API](../README.md) (Spring Boot), qué endpoints usa
y cómo gestiona la autenticación.

---

## Endpoints utilizados

### Autenticación

| Método | Ruta | Descripción | Auth requerida |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Obtener access + refresh token | No |
| POST | `/api/v1/auth/refresh` | Renovar access token | No (lleva el refresh token en el body) |
| POST | `/api/v1/auth/logout` | Revocar refresh tokens | Sí (Bearer) |

### Clientes

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/api/v1/clients` | Lista paginada | ADMIN, MECHANIC |
| GET | `/api/v1/clients/search?surname1=X` | Búsqueda por apellido | ADMIN, MECHANIC |
| GET | `/api/v1/clients/{id}` | Detalle | ADMIN, MECHANIC, CLIENT (solo el suyo) |
| GET | `/api/v1/clients/by-nif/{nif}` | Buscar por NIF | ADMIN, MECHANIC |
| POST | `/api/v1/clients` | Crear | ADMIN, MECHANIC |
| PUT | `/api/v1/clients/{id}` | Actualizar | ADMIN, MECHANIC |
| DELETE | `/api/v1/clients/{id}` | Soft delete | ADMIN |

### Vehículos

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/api/v1/vehicles` | Lista paginada | ADMIN, MECHANIC |
| GET | `/api/v1/vehicles/by-client/{clientId}` | Vehículos de un cliente | ADMIN, MECHANIC, CLIENT |
| GET | `/api/v1/vehicles/by-type?type=CAR` | Filtrar por tipo | ADMIN, MECHANIC |
| GET | `/api/v1/vehicles/{id}` | Detalle | ADMIN, MECHANIC, CLIENT |
| POST | `/api/v1/vehicles` | Registrar | ADMIN, MECHANIC |
| PUT | `/api/v1/vehicles/{id}` | Actualizar | ADMIN, MECHANIC |
| DELETE | `/api/v1/vehicles/{id}` | Soft delete | ADMIN |

### Mecánicos

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/api/v1/mechanics` | Lista paginada | ADMIN, MECHANIC |
| GET | `/api/v1/mechanics/search?specialty=X` | Búsqueda por especialidad | ADMIN, MECHANIC |
| GET | `/api/v1/mechanics/{id}` | Detalle | ADMIN, MECHANIC |
| POST | `/api/v1/mechanics` | Registrar | ADMIN |
| PUT | `/api/v1/mechanics/{id}` | Actualizar | ADMIN |
| DELETE | `/api/v1/mechanics/{id}` | Soft delete | ADMIN |

### Tareas de taller

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/api/v1/tasks` | Lista paginada (todas) | ADMIN, MECHANIC |
| GET | `/api/v1/tasks/pending` | Tareas sin finalizar | ADMIN, MECHANIC |
| GET | `/api/v1/tasks/unpaid` | Finalizadas sin pagar | ADMIN, MECHANIC |
| GET | `/api/v1/tasks/by-client/{id}` | Tareas de un cliente | ADMIN, MECHANIC, CLIENT |
| GET | `/api/v1/tasks/by-vehicle/{id}` | Tareas de un vehículo | ADMIN, MECHANIC, CLIENT |
| GET | `/api/v1/tasks/by-mechanic/{id}` | Tareas de un mecánico | ADMIN, MECHANIC |
| GET | `/api/v1/tasks/{id}` | Detalle | ADMIN, MECHANIC, CLIENT |
| POST | `/api/v1/tasks` | Crear | ADMIN, MECHANIC |
| PUT | `/api/v1/tasks/{id}` | Actualizar diagnóstico/horas | ADMIN, MECHANIC |
| PATCH | `/api/v1/tasks/{id}/hours` | Añadir horas | ADMIN, MECHANIC |
| PATCH | `/api/v1/tasks/{id}/finish` | Finalizar | ADMIN, MECHANIC |
| PATCH | `/api/v1/tasks/{id}/pay` | Marcar como pagada | ADMIN |
| DELETE | `/api/v1/tasks/{id}` | Eliminar | ADMIN |

### Reportes

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/api/v1/reports/summary` | Resumen estadístico | ADMIN, MECHANIC |

---

## Paginación

El backend usa Spring Data Pageable. Todos los endpoints de listado aceptan
los siguientes query params:

| Parámetro | Tipo | Por defecto | Descripción |
|---|---|---|---|
| `page` | int | 0 | Número de página (0-indexed) |
| `size` | int | 20 | Elementos por página |
| `sort` | string | varies | Campo y dirección (ej. `surname1,asc`) |

La respuesta tiene la siguiente estructura:

```json
{
  "content": [...],
  "totalElements": 42,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "last": false
}
```

En el cliente, `GenericPagingSource` maneja este formato automáticamente.
Los `PagingSource` incrementan el parámetro `page` en cada carga y detectan
la última página cuando `content` viene vacío.

---

## Autenticación JWT

### Flujo de login

```
Cliente                           Servidor
  │                                  │
  ├── POST /auth/login ──────────────▶
  │   { username, password }         │
  │                                  │ Verifica credenciales
  │                                  │ Genera access token (1h)
  │                                  │ Genera refresh token (7d)
  │                                  │ Guarda refresh token en BD
  │                                 ◀── 200 OK
  │   { accessToken, refreshToken }  │
  │                                  │
  ├── [Guarda tokens cifrados en DataStore con Keystore]
  ├── [Actualiza SessionManager con el rol del JWT]
  └── [Navega al destino según rol]
```

### Petición autenticada

Cada petición de red pasa por `AuthInterceptor`, que añade automáticamente:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xl...
```

### Refresco automático de token (401)

Cuando el servidor devuelve un 401, OkHttp llama a `TokenAuthenticator`:

```
Cliente                           Servidor
  │                                  │
  ├── GET /api/v1/clients ──────────▶
  │   Authorization: Bearer <token>  │
  │                                  │ Token expirado
  │                                 ◀── 401 Unauthorized
  │                                  │
  │ [OkHttp llama a TokenAuthenticator]
  │                                  │
  ├── POST /auth/refresh ───────────▶
  │   { refreshToken: "uuid" }       │
  │                                  │ Valida refresh token
  │                                  │ Revoca el refresh token usado
  │                                  │ Genera nuevo par de tokens
  │                                 ◀── 200 OK
  │   { accessToken, refreshToken }  │
  │                                  │
  ├── [Guarda nuevos tokens en DataStore]
  │                                  │
  ├── GET /api/v1/clients ──────────▶  (reintento automático)
  │   Authorization: Bearer <nuevo>  │
  │                                 ◀── 200 OK
  └── [El ViewModel recibe los datos sin saber que hubo un refresco]
```

Si el refresh también falla (token expirado o revocado):
- `TokenAuthenticator` limpia los tokens del DataStore.
- Llama a `SessionManager.onLogout()`.
- `NavViewModel` detecta el cambio de `SessionState` → `Unauthenticated`.
- El NavGraph navega automáticamente a la pantalla de Login.

---

## Formato de fechas

El backend (Spring Boot + Jackson) serializa las fechas así:

| Tipo Java (backend) | Formato JSON | Tipo Kotlin (cliente) |
|---|---|---|
| `Instant` | `"2026-01-15T10:30:00Z"` (ISO-8601 UTC) | `Instant` via `Instant.parse()` |
| `LocalDate` | `"2026-01-15"` | `LocalDate` via `LocalDate.parse()` |

La conversión ocurre en los mappers (`XxxDto.toDomain()`), nunca en los modelos
de dominio ni en los ViewModels.

---

## Manejo de errores HTTP

El backend devuelve errores en formato RFC 9457 (ProblemDetail):

```json
{
  "type": "https://workshopmanagement.com/errors/not-found",
  "title": "Recurso no encontrado",
  "status": 404,
  "detail": "Cliente no encontrado: 42",
  "timestamp": "2026-01-15T10:30:00Z"
}
```

En el cliente, los repositorios envuelven las llamadas Retrofit en `runCatching`:
- Si Retrofit lanza una excepción (de red, timeout, etc.) → `Result.failure(exception)`.
- Si el servidor devuelve un código de error HTTP → Retrofit lanza `HttpException`.
- Los ViewModels muestran el `message` de la excepción al usuario.

Para mayor detalle en los errores del servidor (extraer el campo `detail` del ProblemDetail),
se puede añadir un `ErrorInterceptor` de OkHttp que parsee el body de los errores —
mejora planificada en futuras versiones.
