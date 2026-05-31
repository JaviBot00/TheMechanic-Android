package com.hotguy.workshopmanagement.di

import com.hotguy.workshopmanagement.data.remote.api.ClientApiService
import com.hotguy.workshopmanagement.data.remote.api.MechanicApiService
import com.hotguy.workshopmanagement.data.remote.api.ReportApiService
import com.hotguy.workshopmanagement.data.remote.api.VehicleApiService
import com.hotguy.workshopmanagement.data.remote.api.WorkshopTaskApiService
import com.hotguy.workshopmanagement.data.repository.AuthRepositoryImpl
import com.hotguy.workshopmanagement.data.repository.ClientRepositoryImpl
import com.hotguy.workshopmanagement.data.repository.MechanicRepositoryImpl
import com.hotguy.workshopmanagement.data.repository.ReportRepositoryImpl
import com.hotguy.workshopmanagement.data.repository.TaskRepositoryImpl
import com.hotguy.workshopmanagement.data.repository.VehicleRepositoryImpl
import com.hotguy.workshopmanagement.domain.repository.AuthRepository
import com.hotguy.workshopmanagement.domain.repository.ClientRepository
import com.hotguy.workshopmanagement.domain.repository.MechanicRepository
import com.hotguy.workshopmanagement.domain.repository.ReportRepository
import com.hotguy.workshopmanagement.domain.repository.TaskRepository
import com.hotguy.workshopmanagement.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Módulo Hilt que conecta las interfaces de repositorio del dominio con sus
 * implementaciones concretas de la capa de datos.
 *
 * Se divide en dos objetos internos porque Hilt requiere que los métodos
 * [@Binds] estén en una clase abstracta (no generan código, solo registran
 * el binding), mientras que los métodos [@Provides] pueden estar en un
 * object concreto (sí generan código de instanciación).
 *
 * **¿Por qué @Binds en lugar de @Provides?**
 * Con @Provides tendríamos que escribir:
 *   fun provideClientRepository(impl: ClientRepositoryImpl): ClientRepository = impl
 * Con @Binds Hilt lo infiere solo y no genera código de instanciación extra.
 * Es más eficiente y menos verboso.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // ── Bindings interfaz → implementación ────────────────────────────────────
    // Hilt inyecta automáticamente las implementaciones concretas gracias a
    // que están anotadas con @Inject constructor.

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindClientRepository(impl: ClientRepositoryImpl): ClientRepository

    @Binds @Singleton
    abstract fun bindVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository

    @Binds @Singleton
    abstract fun bindMechanicRepository(impl: MechanicRepositoryImpl): MechanicRepository

    @Binds @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    // ── Servicios API Retrofit ─────────────────────────────────────────────────
    // Los @Provides van en un companion object porque @Binds y @Provides
    // no pueden coexistir en el mismo scope de una clase abstracta.

    companion object {

        /**
         * Servicios Retrofit para cada dominio.
         * Retrofit.create() es thread-safe y barato tras la primera llamada,
         * por eso los exponemos como @Singleton.
         */

        @Provides @Singleton
        fun provideClientApiService(retrofit: Retrofit): ClientApiService =
            retrofit.create(ClientApiService::class.java)

        @Provides @Singleton
        fun provideVehicleApiService(retrofit: Retrofit): VehicleApiService =
            retrofit.create(VehicleApiService::class.java)

        @Provides @Singleton
        fun provideMechanicApiService(retrofit: Retrofit): MechanicApiService =
            retrofit.create(MechanicApiService::class.java)

        @Provides @Singleton
        fun provideWorkshopTaskApiService(retrofit: Retrofit): WorkshopTaskApiService =
            retrofit.create(WorkshopTaskApiService::class.java)

        @Provides @Singleton
        fun provideReportApiService(retrofit: Retrofit): ReportApiService =
            retrofit.create(ReportApiService::class.java)
    }
}
