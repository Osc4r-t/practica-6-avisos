package mx.tec.avisos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import mx.tec.avisos.data.remote.AvisosApi
import mx.tec.avisos.data.remote.Credenciales
import mx.tec.avisos.data.remote.toSesion
import mx.tec.avisos.domain.Sesion

/**
 * La única puerta a la sesión. Hacia arriba habla de `Sesion`; hacia abajo,
 * del servidor.
 *
 * Fíjate en que ninguna función devuelve la sesión: la dejan en `_sesion`, y
 * quien quiera saber si hay una se suscribe a `sesion`. Nadie tiene que
 * "avisar" que entró o salió.
 *
 * Por ahora vive SOLO EN MEMORIA: se va con el proceso. Ese es el bug que el
 * Bloque B arregla.
 */
class SesionRepository(private val api: AvisosApi) {

    private val _sesion = MutableStateFlow<Sesion?>(null)

    val sesion: Flow<Sesion?> = _sesion

    suspend fun entrar(usuario: String, password: String) {
        _sesion.value = api.login(Credenciales(usuario.trim().lowercase(), password)).toSesion()
    }

    suspend fun registrar(usuario: String, password: String, codigoProfesor: String) {
        val credenciales = Credenciales(
            usuario = usuario.trim().lowercase(),
            password = password,
            codigoProfesor = codigoProfesor.trim().ifEmpty { null }
        )
        _sesion.value = api.register(credenciales).toSesion()
    }

    suspend fun salir() {
        _sesion.value = null
    }

    /** Para la capa de red, que corre en su propio hilo y no puede suspender. */
    fun tokenActual(): String? = _sesion.value?.accessToken
}
