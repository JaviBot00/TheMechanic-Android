package com.hotguy.workshopmanagement.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// LoadingBox
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Indicador de carga centrado que ocupa todo el espacio disponible.
 * Se usa como estado intermedio mientras se cargan datos de la red.
 */
@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(
        modifier        = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ErrorBox
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Panel de error centrado con un mensaje descriptivo y un botón de reintento.
 *
 * @param message   Mensaje de error a mostrar al usuario.
 * @param onRetry   Acción a ejecutar al pulsar "Reintentar". Si es null, el
 *                  botón no se muestra (útil para errores irrecuperables).
 * @param modifier  Modificador opcional para personalizar el layout.
 */
@Composable
fun ErrorBox(
    message:  String,
    onRetry:  (() -> Unit)? = null,
    modifier: Modifier      = Modifier
) {
    Column(
        modifier            = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector        = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier           = Modifier.size(48.dp),
            tint               = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurface
        )
        if (onRetry != null) {
            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WorkshopTextField
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Campo de texto estilizado para los formularios de la aplicación.
 *
 * Envuelve [OutlinedTextField] de Material 3 con valores por defecto
 * coherentes con el diseño de la app: ancho completo, mensaje de error
 * integrado, soporte para transformaciones visuales (contraseñas).
 *
 * @param value               Valor actual del campo.
 * @param onValueChange       Callback cuando el texto cambia.
 * @param label               Etiqueta flotante del campo.
 * @param modifier            Modificador para personalizar el layout.
 * @param isError             Si es true, el campo se muestra en estado error.
 * @param errorMessage        Mensaje a mostrar bajo el campo cuando [isError] es true.
 * @param visualTransformation Transformación visual (ej. [PasswordVisualTransformation]).
 * @param trailingIcon        Icono opcional al final del campo (ej. mostrar contraseña).
 * @param singleLine          Si true, el campo no acepta saltos de línea.
 */
@Composable
fun WorkshopTextField(
    value:                String,
    onValueChange:        (String) -> Unit,
    label:                String,
    modifier:             Modifier             = Modifier,
    isError:              Boolean              = false,
    errorMessage:         String?              = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon:         @Composable (() -> Unit)? = null,
    singleLine:           Boolean              = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value                = value,
            onValueChange        = onValueChange,
            label                = { Text(label) },
            modifier             = Modifier.fillMaxWidth(),
            isError              = isError,
            visualTransformation = visualTransformation,
            trailingIcon         = trailingIcon,
            singleLine           = singleLine,
            supportingText       = if (isError && errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PrimaryButton
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Botón primario de ancho completo con estado de carga integrado.
 *
 * Cuando [isLoading] es true, muestra un [CircularProgressIndicator] pequeño
 * en lugar del texto y deshabilita el botón para evitar pulsaciones repetidas.
 *
 * @param text      Texto del botón en estado normal.
 * @param onClick   Acción al pulsar el botón.
 * @param modifier  Modificador opcional.
 * @param isLoading Si true, muestra el indicador de carga y desactiva el botón.
 * @param enabled   Si false, el botón aparece desactivado (además de [isLoading]).
 */
@Composable
fun PrimaryButton(
    text:      String,
    onClick:   () -> Unit,
    modifier:  Modifier = Modifier,
    isLoading: Boolean  = false,
    enabled:   Boolean  = true
) {
    Button(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled  = enabled && !isLoading,
        colors   = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier  = Modifier.size(20.dp),
                color     = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text  = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
