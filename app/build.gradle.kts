// app/build.gradle.kts
//
// Fichero de configuración del módulo principal de la aplicación.
// Define el SDK target, las variantes de build, BuildConfig y todas
// las dependencias necesarias para compilar y testear la app.

import java.util.Properties
import kotlin.jvm.kotlin

// Cargamos local.properties para leer la URL del backend sin hardcodearla
// en el código fuente ni en el repositorio.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Requerido desde Kotlin 2.0+: gestiona el compilador de Compose
    // como un plugin separado en lugar de una dependencia de kapt/ksp.
    alias(libs.plugins.kotlin.compose)
    // Activa @Serializable para las data classes de DTOs y rutas de navegación
    alias(libs.plugins.kotlin.serialization)
    // Hilt requiere procesamiento de anotaciones; usamos KSP (más rápido que kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace  = "com.hotguy.workshopmanagement"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.hotguy.workshopmanagement"
        // minSdk 26 = Android 8.0. Cubre >97 % de dispositivos activos en 2026
        // y permite usar la API de Android Keystore sin restricciones.
        minSdk        = 26
        targetSdk     = 36
        versionCode   = 1
        versionName   = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expone la URL base de la API como constante de compilación.
        // En local se lee de local.properties (fichero ignorado por git).
        // En CI/CD se puede inyectar como variable de entorno.
        // En el código: BuildConfig.API_BASE_URL
        buildConfigField(
            type   = "String",
            name   = "API_BASE_URL",
            value  = "\"${localProperties.getProperty("api.base.url", "http://10.0.2.2:8080/")}\""
        )
    }

    buildTypes {
        debug {
            // En debug: nombre de aplicación con sufijo para distinguirla en el dispositivo
            applicationIdSuffix = ".debug"
            isDebuggable        = true
        }
        release {
            isMinifyEnabled = true
            // Activa la optimización de recursos además de la de código
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose     = true  // Activa el compilador de Jetpack Compose
        buildConfig = true  // Genera la clase BuildConfig con nuestros campos personalizados
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    // ── AndroidX Core ──────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // ── Lifecycle ──────────────────────────────────────────────────────────
    implementation(libs.lifecycle.runtime.ktx)
    // collectAsStateWithLifecycle(): recoge Flow/StateFlow de forma segura
    // parando la colección cuando el Composable no está en pantalla.
    implementation(libs.lifecycle.runtime.compose)
    // viewModel() y hiltViewModel() en Composables
    implementation(libs.lifecycle.viewmodel.compose)

    // ── Jetpack Compose ────────────────────────────────────────────────────
    // El BOM garantiza que todas las librerías de Compose usan versiones
    // compatibles entre sí. platform() le dice a Gradle que es un BOM.
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)  // @Preview en Android Studio
    implementation(libs.compose.material3)
    // Iconos extra: se usa `Icons.Default.X` e `Icons.Outlined.X` en la UI
    implementation(libs.compose.material.icons.extended)

    // ── Navegación ─────────────────────────────────────────────────────────
    implementation(libs.navigation.compose)

    // ── Hilt — Inyección de dependencias ───────────────────────────────────
    implementation(libs.hilt.android)
    // Extensión que conecta Hilt con Navigation Compose para hiltViewModel()
    implementation(libs.hilt.navigation.compose)
    // El compilador KSP genera el código de DI en tiempo de compilación
    ksp(libs.hilt.compiler)

    // ── Networking ─────────────────────────────────────────────────────────
    implementation(libs.retrofit.core)
    // Converter para que Retrofit use kotlinx-serialization en lugar de Gson
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    // El interceptor de logging solo se añade en builds de debug para evitar
    // que información sensible aparezca en los logs de producción.
    debugImplementation(libs.okhttp.logging)

    // ── Serialización ──────────────────────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)

    // ── Coroutinas ─────────────────────────────────────────────────────────
    implementation(libs.coroutines.android)

    // ── Almacenamiento seguro ──────────────────────────────────────────────
    // DataStore Preferences para persistir los tokens cifrados en disco.
    // El cifrado real lo gestiona Android Keystore (sin dependencia adicional,
    // es parte de la plataforma Android desde API 23).
    implementation(libs.datastore.preferences)

    // ── Paginación ───────────────────────────────────────────────────────────────
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    // ── Carga de imágenes ──────────────────────────────────────────────────
    implementation(libs.coil.compose)

    // ── Herramientas de Compose (solo debug) ───────────────────────────────
    debugImplementation(libs.compose.ui.tooling)
    // Manifiesto necesario para los tests instrumentados de Compose
    debugImplementation(libs.test.compose.ui.manifest)

    // ── Tests unitarios (JVM) ──────────────────────────────────────────────
    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)
    // Turbine facilita testear Flow: collected.test { assertEquals(...) }
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.coroutines)

    // ── Tests instrumentados (dispositivo / emulador) ──────────────────────
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.test.junit.ext)
    androidTestImplementation(libs.test.espresso.core)
    androidTestImplementation(libs.test.compose.ui.junit4)
}
