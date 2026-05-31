package com.hotguy.workshopmanagement.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Paleta de colores de la aplicación Workshop Management
//
// La nomenclatura sigue el sistema de tonos de Material 3:
//   NombreColor + Tono (0 = negro, 100 = blanco)
//
// Los colores aquí definidos se usan en Theme.kt para construir los esquemas
// de color claro y oscuro. No se usan directamente en los Composables;
// en su lugar se usa MaterialTheme.colorScheme.primary, etc.
// ─────────────────────────────────────────────────────────────────────────────

// Azul industrial — color primario de la marca
val WorkshopBlue20  = Color(0xFF003060)
val WorkshopBlue30  = Color(0xFF004585)
val WorkshopBlue40  = Color(0xFF1565C0)  // Primario en modo claro
val WorkshopBlue80  = Color(0xFF9ABFFF)  // Primario en modo oscuro
val WorkshopBlue90  = Color(0xFFD6E4FF)  // Contenedor primario en modo claro

// Naranja herramienta — color secundario / de acento
val WorkshopOrange20 = Color(0xFF4A1800)
val WorkshopOrange40 = Color(0xFFBF4E00)  // Secundario en modo claro
val WorkshopOrange80 = Color(0xFFFFB68A)  // Secundario en modo oscuro

// Neutros — fondos y superficies
val WorkshopNeutral10  = Color(0xFF1A1C1E)  // Fondo oscuro / texto sobre claro
val WorkshopNeutral15  = Color(0xFF252729)  // Superficie oscura
val WorkshopNeutral90  = Color(0xFFE2E2E6)  // Texto sobre oscuro
val WorkshopNeutral95  = Color(0xFFF0F0F4)  // Superficie clara
val WorkshopNeutral99  = Color(0xFFFCFCFF)  // Fondo claro

val WorkshopWhite = Color(0xFFFFFFFF)

// Colores semánticos de estado de tarea — se usan directamente en los
// Composables de la lista de tareas para mostrar el chip de estado.
val StatusPendingColor  = Color(0xFFFFF9C4)  // Amarillo suave
val StatusInProgressColor = Color(0xFFBBDEFB) // Azul suave
val StatusFinishedColor = Color(0xFFC8E6C9)  // Verde suave
val StatusPaidColor     = Color(0xFFD1C4E9)  // Púrpura suave

// Versiones oscuras de los colores de estado (para modo oscuro)
val StatusPendingColorDark    = Color(0xFF5D4E00)
val StatusInProgressColorDark = Color(0xFF0D3F6B)
val StatusFinishedColorDark   = Color(0xFF1B4D2B)
val StatusPaidColorDark       = Color(0xFF3D2A6B)
