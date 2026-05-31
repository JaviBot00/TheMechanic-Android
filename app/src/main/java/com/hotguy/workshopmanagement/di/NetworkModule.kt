package com.hotguy.workshopmanagement.di

import com.hotguy.workshopmanagement.BuildConfig
import com.hotguy.workshopmanagement.data.remote.api.AuthApiService
import com.hotguy.workshopmanagement.data.remote.authenticator.TokenAuthenticator
import com.hotguy.workshopmanagement.data.remote.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo Hilt que configura y provee toda la infraestructura de red.
 *
 * La cadena de construcción es:
 *   Json (configuración de serialización)
 *     → OkHttpClient (interceptores + authenticator + timeouts)
 *       → Retrofit (URL base + converter factory)
 *         → Servicios API individuales (AuthApiService, ClientApiService, etc.)
 *
 * Todos los bindings son [@Singleton] porque Retrofit y OkHttpClient son
 * thread-safe y costosos de crear; una sola instancia sirve a toda la app.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Configura la instancia de [Json] de kotlinx-serialization.
     *
     * - [ignoreUnknownKeys]: si el backend añade campos nuevos que el cliente
     *   no conoce, los ignora sin lanzar excepción. Hace la app más resiliente
     *   a evoluciones del API.
     * - [isLenient]: acepta JSON ligeramente malformado (strings sin comillas, etc.)
     * - [explicitNulls = false]: los campos nulos no se serializan en las peticiones,
     *   reduciendo el tamaño del body.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient         = true
        explicitNulls     = false
    }

    /**
     * Configura y provee el [OkHttpClient].
     *
     * La cadena de interceptores de OkHttp se ejecuta en el orden en que
     * se añaden. Para nuestra app:
     *
     *   1. [AuthInterceptor] — añade el token Bearer a la petición saliente.
     *   2. [TokenAuthenticator] — refresca el token si la respuesta es 401.
     *      (El Authenticator no es un interceptor; OkHttp lo llama aparte.)
     *   3. [HttpLoggingInterceptor] — registra request y response en debug.
     *      Solo en builds de debug; en release no se añade.
     *
     * Timeouts:
     *   - connectTimeout: tiempo máximo para establecer la conexión TCP.
     *   - readTimeout: tiempo máximo esperando bytes de la respuesta.
     *   - writeTimeout: tiempo máximo enviando el body de la petición.
     *   30 segundos es un valor conservador adecuado para una LAN local.
     *
     * @param authInterceptor     Interceptor que añade el token JWT.
     * @param tokenAuthenticator  Authenticator que refresca el token en 401.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor:    AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .apply {
                // El interceptor de logging solo se añade en builds de debug
                // para evitar que información sensible (tokens, datos de clientes)
                // aparezca en los logs de producción.
                if (BuildConfig.DEBUG) {
                    val loggingInterceptor = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                    addInterceptor(loggingInterceptor)
                }
            }
            .build()
    }

    /**
     * Configura y provee la instancia de [Retrofit].
     *
     * - La URL base se lee de [BuildConfig.API_BASE_URL], que a su vez viene
     *   de [local.properties]. Sin hardcodear nada en el código fuente.
     * - El converter factory conecta Retrofit con kotlinx-serialization:
     *   serializa los body de las peticiones y deserializa las respuestas JSON.
     * - El [OkHttpClient] configurado arriba se inyecta aquí para que Retrofit
     *   use nuestros interceptores y authenticator.
     *
     * @param json       Instancia configurada de kotlinx-serialization Json.
     * @param httpClient El OkHttpClient con interceptores y authenticator.
     */
    @Provides
    @Singleton
    fun provideRetrofit(json: Json, httpClient: OkHttpClient): Retrofit {
        // El converter factory necesita el content type de la respuesta
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    /**
     * Provee el servicio Retrofit para los endpoints de autenticación.
     *
     * Retrofit.create() genera en tiempo de compilación (con KSP en Retrofit 3)
     * la implementación concreta de la interfaz. El resultado es thread-safe
     * y puede usarse como singleton.
     *
     * @param retrofit La instancia configurada de Retrofit.
     */
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    // Los servicios para los demás dominios (Client, Vehicle, Mechanic, Task, Report)
    // se añadirán aquí conforme se implementen sus respectivas interfaces Retrofit.
    // Ejemplo:
    //   @Provides @Singleton
    //   fun provideClientApiService(retrofit: Retrofit): ClientApiService =
    //       retrofit.create(ClientApiService::class.java)
}
