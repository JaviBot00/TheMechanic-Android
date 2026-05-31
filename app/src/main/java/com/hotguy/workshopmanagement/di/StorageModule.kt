package com.hotguy.workshopmanagement.di

import android.content.Context
import com.hotguy.workshopmanagement.data.local.TokenDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para dependencias de almacenamiento.
 *
 * [@Module] indica a Hilt que esta clase provee dependencias.
 * [@InstallIn(SingletonComponent)] significa que los bindings declarados aquí
 * viven mientras vive el proceso de la aplicación — exactamente lo que
 * necesitamos para el DataStore, que debe ser una única instancia global.
 *
 * En este módulo se declara [TokenDataStore]. Al estar anotada con [@Singleton]
 * y tener [@Inject constructor], Hilt podría instanciarla automáticamente sin
 * necesidad de este módulo. Sin embargo, lo declaramos explícitamente para:
 * - Tener un punto central donde ver todas las dependencias de almacenamiento.
 * - Facilitar la sustitución por un fake en tests de integración.
 */
@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    /**
     * Provee la instancia singleton de [TokenDataStore].
     *
     * Hilt inyecta el [ApplicationContext] automáticamente porque está
     * cualificado con [@ApplicationContext]. Nunca uses Activity context
     * en un singleton — el singleton vive más que la Activity y provocaría
     * una fuga de memoria.
     *
     * @param context El contexto de la aplicación.
     * @return La única instancia de [TokenDataStore] en toda la app.
     */
    @Provides
    @Singleton
    fun provideTokenDataStore(
        @ApplicationContext context: Context
    ): TokenDataStore = TokenDataStore(context)
}
