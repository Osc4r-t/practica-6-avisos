package mx.tec.avisos.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.tec.avisos.data.SesionRepository
import mx.tec.avisos.domain.Sesion

/**
 * Lo que la app sabe de la sesión en este instante. Son tres casos y no dos:
 * `Cargando` es "todavía no leí el archivo", que no es lo mismo que "nadie ha
 * entrado". Sin él, la app enseñaría el login medio segundo a quien ya tenía
 * sesión, y luego la lista.
 */
sealed interface SesionEstado {
    data object Cargando : SesionEstado
    data object Anonimo : SesionEstado
    data class Activa(val sesion: Sesion) : SesionEstado
}

/**
 * El ViewModel de más arriba: decide qué árbol de pantallas existe. No tiene
 * ninguna función de "entrar": la sesión la escribe el repositorio y este
 * ViewModel solo la observa.
 */
class SesionViewModel(private val repository: SesionRepository) : ViewModel() {

    val estado: StateFlow<SesionEstado> = repository.sesion
        .map { sesion -> if (sesion == null) SesionEstado.Anonimo else SesionEstado.Activa(sesion) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(ESPERA_MS),
            initialValue = SesionEstado.Cargando
        )

    fun salir() {
        viewModelScope.launch { repository.salir() }
    }

    private companion object {
        const val ESPERA_MS = 5_000L
    }
}
