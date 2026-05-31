package com.hotguy.workshopmanagement.data.remote.api

import com.hotguy.workshopmanagement.data.remote.dto.auth.AuthResponseDto
import com.hotguy.workshopmanagement.data.remote.dto.auth.LoginRequestDto
import com.hotguy.workshopmanagement.data.remote.dto.auth.RefreshRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interfaz Retrofit para los endpoints de autenticación del backend.
 *
 * Retrofit genera en tiempo de compilación la implementación concreta de
 * esta interfaz. Cada función suspend se convierte en una llamada HTTP
 * que se ejecuta en el dispatcher de IO de las coroutinas.
 *
 * Estos endpoints son públicos (no requieren token JWT), por eso el
 * [AuthInterceptor] deja pasar las peticiones sin cabecera Authorization
 * cuando no hay sesión activa.
 *
 * La URL base se configura en [NetworkModule] y se lee de BuildConfig.API_BASE_URL.
 * Las rutas aquí son relativas a esa base: "api/v1/auth/login" →
 * "http://IP:8080/api/v1/auth/login"
 */
interface AuthApiService {

    /**
     * Autentica al usuario y devuelve un par de tokens JWT.
     *
     * POST /api/v1/auth/login
     *
     * @param request Credenciales del usuario (username + password).
     * @return [AuthResponseDto] con access token, refresh token, tipo y rol.
     */
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    /**
     * Genera un nuevo access token a partir de un refresh token válido.
     *
     * POST /api/v1/auth/refresh
     *
     * Este endpoint lo llama [TokenAuthenticator] de forma síncrona cuando
     * el servidor devuelve un 401. No debe llamarse directamente desde
     * los ViewModels o Use Cases.
     *
     * @param request Contiene el refresh token actual.
     * @return [AuthResponseDto] con el nuevo par de tokens.
     */
    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequestDto): AuthResponseDto

    /**
     * Cierra la sesión del usuario autenticado, revocando sus refresh tokens.
     *
     * POST /api/v1/auth/logout
     *
     * Requiere token JWT válido en la cabecera Authorization.
     * El [AuthInterceptor] lo añade automáticamente.
     */
    @POST("api/v1/auth/logout")
    suspend fun logout()
}
