import kotlin.apply
import kotlin.jvm.kotlin

// build.gradle.kts (raíz del proyecto)
//
// Este fichero solo declara los plugins que usan los submódulos del proyecto,
// pero NO los aplica aquí (apply false). Cada módulo los aplica en su propio
// build.gradle.kts con `alias(libs.plugins.X)`.
//
// Centralizar las declaraciones aquí permite que Gradle resuelva y descargue
// los plugins una sola vez, independientemente de cuántos módulos los usen.

plugins {
    alias(libs.plugins.android.application)  apply false
    alias(libs.plugins.kotlin.android)       apply false
    alias(libs.plugins.kotlin.compose)       apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android)         apply false
    alias(libs.plugins.ksp)                  apply false
}
