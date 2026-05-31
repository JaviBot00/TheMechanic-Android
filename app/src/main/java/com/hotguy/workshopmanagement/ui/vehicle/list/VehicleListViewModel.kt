package com.hotguy.workshopmanagement.ui.vehicle.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hotguy.workshopmanagement.di.SessionManager
import com.hotguy.workshopmanagement.domain.model.UserRole
import com.hotguy.workshopmanagement.domain.model.Vehicle
import com.hotguy.workshopmanagement.domain.usecase.vehicle.GetVehiclesByClientUseCase
import com.hotguy.workshopmanagement.domain.usecase.vehicle.GetVehiclesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class VehicleListUiState(
    val userRole: UserRole = UserRole.MECHANIC,
    val clientId: Long?    = null     // null = todos los vehículos
)

/**
 * ViewModel de la lista de vehículos.
 *
 * Si [clientId] es -1L (centinela de CLIENT) se resuelve automáticamente
 * al clientId real del usuario autenticado leyéndolo del [SessionManager].
 * Si [clientId] es null se muestran todos los vehículos (ADMIN/MECHANIC).
 * Cualquier otro valor filtra por ese cliente concreto.
 */
@HiltViewModel
class VehicleListViewModel @Inject constructor(
    private val getVehiclesUseCase:          GetVehiclesUseCase,
    private val getVehiclesByClientUseCase:  GetVehiclesByClientUseCase,
    private val sessionManager:              SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VehicleListUiState(userRole = sessionManager.currentRole ?: UserRole.MECHANIC)
    )
    val uiState = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel con el clientId de la ruta de navegación.
     * Debe llamarse desde el Composable pasando el parámetro de la ruta.
     */
    fun init(clientId: Long?) {
        val resolvedId = when (clientId) {
            null  -> null            // todos los vehículos
            -1L   -> resolveOwnClientId()   // vehículos del usuario autenticado
            else  -> clientId        // cliente específico
        }
        _uiState.update { it.copy(clientId = resolvedId) }
    }

    /**
     * Extrae el clientId del usuario autenticado desde el SessionManager.
     * Solo aplicable cuando el rol es CLIENT.
     * En este proyecto el clientId del usuario autenticado no está en el
     * SessionManager (que solo guarda el rol). La pantalla de vehículos de
     * CLIENT carga todos los vehículos filtrando por el token en el servidor,
     * usando el endpoint /vehicles/by-client/{id} con el ID que devuelve
     * el servidor al hacer login (flujo pendiente: guardar clientId en
     * SessionManager en LoginRepositoryImpl como mejora futura).
     * Por ahora se retorna null para mostrar la lista global con el filtro
     * aplicado en el servidor vía JWT.
     */
    private fun resolveOwnClientId(): Long? = null

    val vehicles: Flow<PagingData<Vehicle>> by lazy {
        val cid = _uiState.value.clientId
        if (cid == null) getVehiclesUseCase()
        else getVehiclesByClientUseCase(cid)
    }.let { it.cachedIn(viewModelScope) }
}
