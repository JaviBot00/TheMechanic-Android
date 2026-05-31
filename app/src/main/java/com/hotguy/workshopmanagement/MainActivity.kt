package com.hotguy.workshopmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.hotguy.workshopmanagement.domain.usecase.auth.SessionRestoreUseCase
import com.hotguy.workshopmanagement.ui.navigation.AppNavGraph
import com.hotguy.workshopmanagement.ui.theme.WorkshopManagementTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Única Activity de la aplicación.
 *
 * En una arquitectura Compose moderna toda la UI vive en el árbol de
 * Composables. La Activity actúa únicamente como contenedor del sistema:
 * gestiona el ciclo de vida del proceso y aloja el [setContent] que
 * arranca el árbol Compose.
 *
 * Responsabilidad adicional en este proyecto: restaurar la sesión del
 * usuario al arrancar la app invocando [SessionRestoreUseCase] antes de
 * que el NavGraph decida la pantalla inicial. Esto permite que usuarios
 * que ya hicieron login no tengan que volver a introducir credenciales.
 *
 * [@AndroidEntryPoint] es obligatorio en cualquier Activity que use Hilt
 * o aloje Composables con [hiltViewModel()].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Inyectado por Hilt. Restaura el [SessionManager] a partir de los
     * tokens cifrados en [TokenDataStore] al arrancar la app.
     */
    @Inject
    lateinit var sessionRestoreUseCase: SessionRestoreUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restaurar la sesión de forma asíncrona ANTES de dibujar el NavGraph.
        // lifecycleScope.launch ejecuta la coroutina en el hilo principal pero
        // sin bloquear el hilo de UI — setContent se llama después de que la
        // coroutina termine gracias a que ambos se ejecutan secuencialmente
        // dentro del mismo bloque launch.
        lifecycleScope.launch {
            // Si hay tokens válidos en disco, SessionManager queda en estado
            // Authenticated. Si no, queda en Unauthenticated → NavGraph → Login.
            sessionRestoreUseCase()

            // enableEdgeToEdge permite que el contenido se dibuje bajo las barras
            // del sistema (status bar, navigation bar) para un aspecto más moderno.
            enableEdgeToEdge()

            setContent {
                WorkshopManagementTheme {
                    // AppNavGraph lee el SessionState ya inicializado y elige
                    // la pantalla de inicio correcta en el primer frame.
                    AppNavGraph()
                }
            }
        }
    }
}
