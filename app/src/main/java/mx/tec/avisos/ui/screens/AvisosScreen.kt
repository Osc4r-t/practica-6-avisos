package mx.tec.avisos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.avisos.domain.Aviso
import mx.tec.avisos.domain.Rol
import mx.tec.avisos.domain.Sesion
import mx.tec.avisos.ui.components.AvisoCard
import mx.tec.avisos.ui.components.CargandoView
import mx.tec.avisos.ui.components.ErrorView
import mx.tec.avisos.ui.components.SesionBanner
import mx.tec.avisos.ui.components.VacioView
import mx.tec.avisos.ui.state.UiState
import mx.tec.avisos.ui.theme.AvisosTheme

/**
 * El tablón. Recibe la sesión para pintar el banner: quién eres y cuánto le
 * queda a tu acceso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvisosScreen(
    sesion: Sesion,
    avisos: UiState<List<Aviso>>,
    onRecargar: () -> Unit,
    onPublicar: () -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Avisos") },
                actions = {
                    IconButton(onClick = onRecargar) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                    IconButton(onClick = onSalir) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        },
        floatingActionButton = {
            if (sesion.puedePublicar) {
                FloatingActionButton(onClick = onPublicar) {
                    Icon(Icons.Default.Add, contentDescription = "Publicar aviso")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SesionBanner(sesion)

            when (avisos) {
                is UiState.Cargando -> CargandoView()

                is UiState.Error -> ErrorView(mensaje = avisos.mensaje, onReintentar = onRecargar)

                is UiState.Exito -> if (avisos.datos.isEmpty()) {
                    VacioView("Todavía no hay avisos.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(avisos.datos, key = { it.id }) { aviso -> AvisoCard(aviso) }
                    }
                }
            }
        }
    }
}

private val demoSesion = Sesion("profe.demo", Rol.PROFESOR, "x", "y", System.currentTimeMillis() / 1000 + 280)

private val demoAvisos = listOf(
    Aviso(2, "Examen parcial", "El parcial es el jueves a las 10:00 en el salón de siempre.", "profe.demo", "2026-09-21 10:00:00"),
    Aviso(1, "Bienvenidos al tablón", "Este aviso lo publicó el servidor al crear la tabla.", "profesor", "2026-09-20 09:00:00")
)

@Preview(showBackground = true)
@Composable
private fun AvisosPreview() {
    AvisosTheme {
        AvisosScreen(
            sesion = demoSesion,
            avisos = UiState.Exito(demoAvisos),
            onRecargar = {}, onPublicar = {}, onSalir = {}
        )
    }
}

@Preview(showBackground = true, name = "Alumno, con error")
@Composable
private fun AvisosErrorPreview() {
    AvisosTheme {
        AvisosScreen(
            sesion = demoSesion.copy(usuario = "a01234567", rol = Rol.ALUMNO),
            avisos = UiState.Error("No hay conexión. Revisa tu internet."),
            onRecargar = {}, onPublicar = {}, onSalir = {}
        )
    }
}
