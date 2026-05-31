package com.hotguy.workshopmanagement.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Esquema de colores para el modo oscuro.
 *
 * Usa los colores definidos en Color.kt. Se aplica automáticamente
 * cuando el dispositivo tiene el modo oscuro activado.
 */
private val DarkColorScheme = darkColorScheme(
    primary         = WorkshopBlue80,
    onPrimary       = WorkshopBlue20,
    primaryContainer= WorkshopBlue30,
    secondary       = WorkshopOrange80,
    onSecondary     = WorkshopOrange20,
    background      = WorkshopNeutral10,
    surface         = WorkshopNeutral15,
    onBackground    = WorkshopNeutral90,
    onSurface       = WorkshopNeutral90,
)

/**
 * Esquema de colores para el modo claro.
 */
private val LightColorScheme = lightColorScheme(
    primary         = WorkshopBlue40,
    onPrimary       = WorkshopWhite,
    primaryContainer= WorkshopBlue90,
    secondary       = WorkshopOrange40,
    onSecondary     = WorkshopWhite,
    background      = WorkshopNeutral99,
    surface         = WorkshopNeutral95,
    onBackground    = WorkshopNeutral10,
    onSurface       = WorkshopNeutral10,
)

/**
 * Tema principal de la aplicación Workshop Management.
 *
 * Este Composable envuelve toda la UI y provee:
 * - Esquema de colores (claro / oscuro / dinámico en Android 12+)
 * - Tipografía definida en Type.kt
 * - Formas definidas por Material 3
 *
 * @param darkTheme Si es true, aplica el esquema oscuro. Por defecto sigue
 *                  la preferencia del sistema.
 * @param dynamicColor Si es true y el dispositivo es Android 12+, usa los
 *                     colores dinámicos generados a partir del fondo de pantalla.
 *                     Desactívalo si prefieres mantener siempre la paleta de marca.
 * @param content El árbol de Composables hijo que heredará el tema.
 */
@Composable
fun WorkshopManagementTheme(
    darkTheme: Boolean    = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Material You (Android 12+): los colores se extraen del fondo de pantalla
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = WorkshopTypography,
        content     = content
    )
}
