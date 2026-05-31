package com.hotguy.workshopmanagement

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application del proyecto.
 *
 * La anotación [@HiltAndroidApp] convierte esta clase en el punto de entrada
 * del grafo de dependencias de Hilt. En tiempo de compilación, KSP genera
 * automáticamente el componente raíz de Hilt y toda la infraestructura de DI.
 *
 * Sin esta anotación, Hilt no puede inyectar dependencias en Activities,
 * ViewModels, Repositories ni ningún otro componente de la app.
 *
 * Esta clase se declara en AndroidManifest.xml con:
 *   android:name=".WorkshopManagementApp"
 */
@HiltAndroidApp
class WorkshopManagementApp : Application()
