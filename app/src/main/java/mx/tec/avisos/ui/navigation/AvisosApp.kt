package mx.tec.avisos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.tec.avisos.ui.components.CargandoView
import mx.tec.avisos.ui.screens.LoginScreen
import mx.tec.avisos.ui.state.AppViewModelProvider
import mx.tec.avisos.ui.state.LoginViewModel
import mx.tec.avisos.ui.state.SesionEstado
import mx.tec.avisos.ui.state.SesionViewModel

/**
 * La raíz de la app. No es un NavHost: es un `when` sobre la sesión.
 *
 * Sin sesión, lo único que existe es el login — ni siquiera hay un NavHost al
 * que navegar. Con sesión, existe el tablón. Y cuando la sesión desaparece
 * (salir, o un refresh que el servidor rechazó), el árbol entero del tablón se
 * va y vuelve el login, sin que nadie llame a `navigate`.
 */
@Composable
fun AvisosApp() {
    val sesionViewModel: SesionViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val estado by sesionViewModel.estado.collectAsStateWithLifecycle()

    when (val actual = estado) {
        SesionEstado.Cargando -> CargandoView()

        SesionEstado.Anonimo -> {
            val loginViewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
            LoginScreen(
                uiState = loginViewModel.uiState,
                onUsuarioChange = loginViewModel::onUsuarioChange,
                onPasswordChange = loginViewModel::onPasswordChange,
                onCodigoProfesorChange = loginViewModel::onCodigoProfesorChange,
                onAlternarModo = loginViewModel::alternarModo,
                onEnviar = loginViewModel::enviar
            )
        }

        is SesionEstado.Activa -> AvisosNavHost(
            sesion = actual.sesion,
            onSalir = sesionViewModel::salir
        )
    }
}
