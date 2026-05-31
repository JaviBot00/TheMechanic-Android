# Guía de instalación y configuración

## Requisitos previos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| Android Studio | Meerkat 2025.3.1 | Versión estable más reciente |
| JDK | 17 | Incluido en Android Studio |
| Kotlin | 2.2.x | Gestionado por Gradle |
| Backend Workshop Management | Cualquiera | Debe estar corriendo antes de lanzar la app |

---

## 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/workshop-management-android.git
cd workshop-management-android
```

---

## 2. Configurar la URL del backend

Crea o edita el fichero `local.properties` en la raíz del proyecto (ya existe,
generado por Android Studio). Añade al final:

```properties
api.base.url=http://TU_IP_LOCAL:8080/
```

**¿Cómo saber tu IP local?**

En Linux/macOS:
```bash
ip route get 1 | awk '{print $7}'
# o simplemente:
hostname -I | awk '{print $1}'
```

En Windows:
```cmd
ipconfig
# Busca "Dirección IPv4" de tu adaptador de red activo
```

**Casos especiales:**

| Escenario | URL a usar |
|---|---|
| Emulador de Android Studio | `http://10.0.2.2:8080/` |
| Dispositivo físico en la misma red WiFi | `http://192.168.X.X:8080/` |
| Dispositivo con cable USB (port forwarding) | `http://localhost:8080/` tras ejecutar `adb reverse tcp:8080 tcp:8080` |

> ⚠️ **Importante:** `local.properties` está en `.gitignore`. Nunca subas tu IP
> ni credenciales al repositorio.

---

## 3. Sincronizar Gradle

1. Abre el proyecto en Android Studio.
2. Espera a que Android Studio detecte los cambios en `build.gradle.kts`.
3. Si no sincroniza automáticamente: **File → Sync Project with Gradle Files**.
4. La primera sincronización puede tardar varios minutos mientras descarga las dependencias.

---

## 4. Verificar la conexión con el backend

Antes de lanzar la app, comprueba que el backend responde correctamente:

```bash
curl -X POST http://TU_IP:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "tu_contraseña"}'
```

Deberías recibir una respuesta con `accessToken` y `refreshToken`.

---

## 5. Compilar y ejecutar

### En un dispositivo físico

1. Activa el **modo desarrollador** en el dispositivo:
   Ajustes → Acerca del teléfono → pulsa 7 veces en "Número de compilación".
2. Activa **Depuración USB** en Opciones de desarrollador.
3. Conecta el dispositivo con un cable USB.
4. Pulsa el botón **Run** (triángulo verde) en Android Studio.

### Desde la línea de comandos

```bash
# Build debug
./gradlew assembleDebug

# Instalar en dispositivo conectado
./gradlew installDebug

# Build release (requiere keystore configurado)
./gradlew assembleRelease
```

---

## 6. Ejecutar los tests

```bash
# Tests unitarios (JVM, sin dispositivo)
./gradlew test

# Tests con informe de cobertura (JaCoCo no está configurado aún — mejora futura)
./gradlew testDebugUnitTest

# Tests instrumentados (requiere dispositivo o emulador conectado)
./gradlew connectedAndroidTest
```

---

## 7. Estructura del proyecto

```
workshop-management-android/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/workshopmanagement/android/
│       │   │   ├── di/             # Módulos Hilt y SessionManager
│       │   │   ├── data/           # DTOs, servicios Retrofit, repositorios
│       │   │   ├── domain/         # Modelos, repositorios (interfaces), use cases
│       │   │   └── ui/             # Pantallas Compose y ViewModels
│       │   └── res/                # Recursos (strings, drawables, etc.)
│       └── test/                   # Tests unitarios JVM
├── gradle/
│   └── libs.versions.toml          # Catálogo centralizado de versiones
├── docs/
│   ├── setup.md                    # Este fichero
│   └── api-integration.md          # Integración con el backend
├── ARCHITECTURE.md                 # Arquitectura detallada
├── README.md                       # Presentación del proyecto
└── local.properties                # ← NO incluido en git (configuración local)
```

---

## 8. Variables de entorno para CI/CD

En un entorno de integración continua (GitHub Actions, etc.), la URL del backend
se puede inyectar como variable de entorno en lugar de `local.properties`:

```yaml
# .github/workflows/build.yml
- name: Build
  env:
    API_BASE_URL: ${{ secrets.API_BASE_URL }}
  run: ./gradlew assembleDebug
```

Y en `app/build.gradle.kts` el valor ya está configurado para leerlo:

```kotlin
buildConfigField(
    "String", "API_BASE_URL",
    "\"${localProperties.getProperty("api.base.url", "http://10.0.2.2:8080/")}\""
)
```

---

## 9. Problemas comunes

### La app no conecta con el backend

- Comprueba que el backend está corriendo (`curl` como se indica en el paso 4).
- Verifica que la IP en `local.properties` es correcta.
- En Android, las conexiones HTTP planas (no HTTPS) están bloqueadas por defecto desde API 28.
  Añade en `res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">192.168.1.0</domain>
    </domain-config>
</network-security-config>
```

Y referéncialo en el `AndroidManifest.xml`:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

### Error de compilación: "Unresolved reference"

- Ejecuta **Build → Clean Project** y luego **Build → Rebuild Project**.
- Comprueba que el KSP está generando el código de Hilt: busca errores de KSP en el panel Build.

### Los tests fallan con "Main dispatcher not set"

- Asegúrate de que todos los tests de ViewModel tienen `@Before setUp()` con
  `Dispatchers.setMain(testDispatcher)` y `@After tearDown()` con `Dispatchers.resetMain()`.
