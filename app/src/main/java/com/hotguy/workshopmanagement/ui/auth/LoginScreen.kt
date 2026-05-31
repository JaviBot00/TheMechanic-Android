package com.hotguy.workshopmanagement.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.ui.components.PrimaryButton
import com.hotguy.workshopmanagement.ui.components.WorkshopTextField

/**
 * Pantalla de inicio de sesión.
 *
 * Es la primera pantalla que ve el usuario si no tiene sesión activa.
 * Muestra un formulario con usuario y contraseña y gestiona los estados
 * de carga y error de forma visual.
 *
 * Sigue el patrón Composable sin estado (stateless): todo el estado viene
 * del [LoginViewModel] y las interacciones del usuario se delegan a él.
 * Esto hace la pantalla fácil de testear y de previsualizar con @Preview.
 *
 * @param onLoginSuccess Callback que recibe el rol del usuario tras un login
 *                       exitoso. El NavGraph lo usa para navegar al destino
 *                       correcto según el rol.
 * @param viewModel      ViewModel inyectado automáticamente por Hilt. Se puede
 *                       sustituir en tests por un fake.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    viewModel:      LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // LaunchedEffect recoge los eventos de un solo disparo (LoginSuccess) sin
    // re-ejecutarse en cada recomposición. La clave Unit garantiza que el bloque
    // solo se lanza una vez durante la vida de este Composable.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> onLoginSuccess(event.role)
            }
        }
    }

    Scaffold { innerPadding ->
        LoginContent(
            uiState   = uiState,
            onUsernameChange         = viewModel::onUsernameChange,
            onPasswordChange         = viewModel::onPasswordChange,
            onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
            onLoginClick             = viewModel::onLoginClick,
            modifier  = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Contenido stateless de la pantalla de login.
 *
 * Separar el contenido del Composable con estado ([LoginScreen]) permite:
 * - Previsualizar la UI con @Preview sin necesidad de un ViewModel real.
 * - Testear el layout de forma aislada.
 *
 * @param uiState                   Estado actual del formulario.
 * @param onUsernameChange          Callback al escribir en el campo de usuario.
 * @param onPasswordChange          Callback al escribir en el campo de contraseña.
 * @param onTogglePasswordVisibility Callback al pulsar el icono de visibilidad.
 * @param onLoginClick              Callback al pulsar el botón de login.
 * @param modifier                  Modificador del layout raíz.
 */
@Composable
private fun LoginContent(
    uiState:                    LoginUiState,
    onUsernameChange:           (String) -> Unit,
    onPasswordChange:           (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick:               () -> Unit,
    modifier:                   Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // imePadding mueve el contenido hacia arriba cuando el teclado
            // virtual aparece, evitando que tape el botón de login.
            .imePadding()
            // verticalScroll permite desplazamiento si la pantalla es pequeña
            // o el teclado ocupa demasiado espacio.
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Cabecera ──────────────────────────────────────────────────────────

        Icon(
            imageVector        = Icons.Outlined.Build,
            contentDescription = null,
            modifier           = Modifier.size(64.dp),
            tint               = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = "Workshop Management",
            style     = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text      = "Gestión integral del taller",
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(48.dp))

        // ── Formulario ────────────────────────────────────────────────────────

        WorkshopTextField(
            value         = uiState.username,
            onValueChange = onUsernameChange,
            label         = "Usuario",
            isError       = uiState.errorMessage != null
        )

        Spacer(Modifier.height(12.dp))

        WorkshopTextField(
            value                = uiState.password,
            onValueChange        = onPasswordChange,
            label                = "Contraseña",
            isError              = uiState.errorMessage != null,
            visualTransformation = if (uiState.passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.passwordVisible)
                            Icons.Outlined.Visibility
                        else
                            Icons.Outlined.VisibilityOff,
                        contentDescription = if (uiState.passwordVisible)
                            "Ocultar contraseña"
                        else
                            "Mostrar contraseña"
                    )
                }
            }
        )

        // Mensaje de error bajo el formulario
        if (uiState.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text  = uiState.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Botón de login ────────────────────────────────────────────────────

        PrimaryButton(
            text      = "Iniciar sesión",
            onClick   = onLoginClick,
            isLoading = uiState.isLoading,
            // Deshabilitar el botón si algún campo está vacío para dar feedback
            // inmediato sin necesidad de tocar el servidor
            enabled   = uiState.username.isNotBlank() && uiState.password.isNotBlank()
        )
    }
}
