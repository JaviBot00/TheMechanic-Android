package com.hotguy.workshopmanagement.di

import com.hotguy.workshopmanagement.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de sesión del usuario autenticado.
 *
 * Mantiene en memoria el estado de autenticación actual: si hay un usuario
 * conectado y cuál es su rol. Los Composables y ViewModels observan el
 * [sessionState] como un Flow para reaccionar automáticamente a cambios
 * (login / logout).
 *
 * Es un [@Singleton] de Hilt, por lo que existe una única instancia durante
 * toda la vida del proceso de la app. Al matar la app, la sesión desaparece
 * de memoria; los tokens cifrados persisten en DataStore y se usan para
 * restaurar la sesión al relanzar la app.
 *
 * El rol se extrae de las claims del JWT en el momento del login y se descarta
 * al hacer logout. Nunca se persiste en disco — solo los tokens van al
 * DataStore cifrado.
 */
@Singleton
class SessionManager @Inject constructor() {

    /**
     * Estado interno mutable de la sesión.
     * Solo [SessionManager] puede modificarlo desde dentro.
     */
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Unauthenticated)

    /**
     * Estado de sesión expuesto como Flow de solo lectura.
     * Los ViewModels y el NavGraph observan este Flow para decidir
     * qué pantallas mostrar y qué acciones habilitar.
     */
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    /**
     * Rol del usuario actualmente autenticado, o null si no hay sesión.
     * Propiedad de conveniencia para acceso síncrono sin colectar el Flow.
     */
    val currentRole: UserRole?
        get() = (_sessionState.value as? SessionState.Authenticated)?.role

    /**
     * Indica si hay un usuario con sesión activa.
     */
    val isAuthenticated: Boolean
        get() = _sessionState.value is SessionState.Authenticated

    /**
     * Registra una sesión exitosa tras el login o el refresco del token.
     *
     * @param role El rol del usuario extraído del JWT.
     */
    fun onLoginSuccess(role: UserRole) {
        _sessionState.value = SessionState.Authenticated(role = role)
    }

    /**
     * Cierra la sesión activa.
     * Se llama tanto desde el logout explícito del usuario como desde el
     * [TokenAuthenticator] cuando el refresh token ha expirado.
     */
    fun onLogout() {
        _sessionState.value = SessionState.Unauthenticated
    }
}

/**
 * Representa el estado de autenticación de la sesión actual.
 *
 * Se usa como sealed class para poder usar when() exhaustivo en los
 * Composables y ViewModels que consumen el estado de sesión.
 */
sealed class SessionState {

    /** No hay ningún usuario autenticado. La app debe mostrar la pantalla de login. */
    data object Unauthenticated : SessionState()

    /**
     * Hay un usuario autenticado con su rol correspondiente.
     * @param role El rol determina qué pantallas y acciones están disponibles.
     */
    data class Authenticated(val role: UserRole) : SessionState()
}
