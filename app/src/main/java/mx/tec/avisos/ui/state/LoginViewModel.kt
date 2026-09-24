package mx.tec.avisos.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.tec.avisos.data.SesionRepository
import mx.tec.avisos.domain.CredencialesValidator
import retrofit2.HttpException
import java.io.IOException

/**
 * Lo que el usuario lleva tecleado en el login. La contraseña vive aquí solo
 * mientras se escribe: al entrar se manda y se olvida. Nunca se guarda.
 */
data class LoginUiState(
    val usuario: String = "",
    val password: String = "",
    val codigoProfesor: String = "",
    val modoRegistro: Boolean = false,
    val enviando: Boolean = false,
    val error: String? = null
) {
    val puedeEnviar: Boolean =
        CredencialesValidator.sonValidas(usuario, password) && !enviando
}

class LoginViewModel(private val repository: SesionRepository) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onUsuarioChange(texto: String) {
        uiState = uiState.copy(usuario = texto, error = null)
    }

    fun onPasswordChange(texto: String) {
        uiState = uiState.copy(password = texto, error = null)
    }

    fun onCodigoProfesorChange(texto: String) {
        uiState = uiState.copy(codigoProfesor = texto, error = null)
    }

    fun alternarModo() {
        uiState = uiState.copy(modoRegistro = !uiState.modoRegistro, error = null)
    }

    /**
     * No recibe un `alTerminar`: cuando el repositorio guarda la sesión, el
     * `SesionViewModel` la ve y la app cambia de pantalla sola.
     */
    fun enviar() {
        if (!uiState.puedeEnviar) return
        viewModelScope.launch {
            uiState = uiState.copy(enviando = true, error = null)
            try {
                if (uiState.modoRegistro) {
                    repository.registrar(uiState.usuario, uiState.password, uiState.codigoProfesor)
                } else {
                    repository.entrar(uiState.usuario, uiState.password)
                }
                // Se limpia todo: la contraseña no se queda en memoria más de lo necesario.
                uiState = LoginUiState()
            } catch (e: IOException) {
                uiState = uiState.copy(enviando = false, error = "No hay conexión. Revisa tu internet.")
            } catch (e: HttpException) {
                uiState = uiState.copy(enviando = false, error = mensajeDe(e))
            }
        }
    }
}
