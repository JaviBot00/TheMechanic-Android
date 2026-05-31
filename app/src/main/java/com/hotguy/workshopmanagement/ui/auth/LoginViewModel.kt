package com.hotguy.workshopmanagement.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// UiState
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Estado completo de la pantalla de login.
 *
 * Se modela como una data class porque la pantalla siempre se muestra —
 * no hay estados "vacíos". Los campos representan la condición del formulario
 * y de la petición de red en cada momento.
 *
 * @param username        Texto actual del campo de usuario.
 * @param password        Texto actual del campo de contraseña.
 * @param passwordVisible Si true, la contraseña se muestra en texto plano.
 * @param isLoading       Si true, hay una petición de login en curso.
 * @param errorMessage    Mensaje de error bajo el formulario (null = sin error).
 */
data class LoginUiState(
    val username:        String  = "",
    val password:        String  = "",
    val passwordVisible: Boolean = false,
    val isLoading:       Boolean = false,
    val errorMessage:    String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// Events
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Eventos de un solo disparo emitidos por el ViewModel a la pantalla.
 *
 * Se usan para acciones que no tienen sentido persistir en el estado, como
 * la navegación tras un login exitoso. Se exponen como [SharedFlow] — sin
 * valor inicial, emitidos exactamente una vez y no re-emitidos al recomponer.
 */
sealed interface LoginEvent {
    /** El login fue exitoso. La pantalla navega al destino correcto. */
    data class LoginSuccess(val role: UserRole) : LoginEvent
}

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel de la pantalla de login.
 *
 * Gestiona el estado del formulario (texto, visibilidad de contraseña, errores)
 * y coordina la llamada al [LoginUseCase] cuando el usuario pulsa el botón.
 *
 * @param loginUseCase Use Case de autenticación inyectado por Hilt.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    /** Estado observable de la pantalla. */
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    /** Eventos de un solo disparo (navegación). */
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    // ── Acciones del usuario ──────────────────────────────────────────────────

    /** Actualiza el campo de usuario y limpia el error previo. */
    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    /** Actualiza el campo de contraseña y limpia el error previo. */
    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    /** Alterna la visibilidad de la contraseña (texto plano / puntos). */
    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    /**
     * Ejecuta el flujo de autenticación al pulsar el botón de login.
     *
     * Pasos:
     * 1. Mostrar indicador de carga y limpiar errores previos.
     * 2. Llamar al [LoginUseCase] con las credenciales del formulario.
     * 3a. Éxito → emitir [LoginEvent.LoginSuccess] con el rol del usuario.
     * 3b. Error → mostrar el mensaje de error en el formulario.
     * 4. Ocultar el indicador de carga (siempre, sea cual sea el resultado).
     */
    fun onLoginClick() {
        val state = _uiState.value
        if (state.isLoading) return   // evitar llamadas duplicadas

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            loginUseCase(state.username, state.password)
                .onSuccess { authToken ->
                    _events.emit(LoginEvent.LoginSuccess(authToken.role))
                }
                .onFailure { exception ->
                    val message = when {
                        exception.message?.contains("401") == true ||
                        exception.message?.contains("Unauthorized") == true ->
                            "Credenciales incorrectas. Comprueba el usuario y la contraseña."
                        exception.message?.contains("Unable to resolve host") == true ||
                        exception.message?.contains("timeout") == true ->
                            "No se puede conectar con el servidor. Comprueba tu conexión de red."
                        exception is IllegalArgumentException ->
                            exception.message ?: "Datos del formulario incorrectos."
                        else ->
                            "Error inesperado. Inténtalo de nuevo."
                    }
                    _uiState.update { it.copy(errorMessage = message) }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
