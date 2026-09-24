package mx.tec.avisos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import mx.tec.avisos.data.local.SesionStore
import mx.tec.avisos.data.remote.AvisosApi
import mx.tec.avisos.data.remote.Credenciales
import mx.tec.avisos.data.remote.toSesion
import mx.tec.avisos.domain.Sesion

/**
 * La única puerta a la sesión. Hacia arriba habla de `Sesion`; hacia abajo,
 * del servidor y del archivo donde se guarda.
 *
 * Fíjate en que ninguna función devuelve la sesión: la escriben en el store, y
 * el store la emite por `sesion`. Quien quiera saber si hay sesión se suscribe;
 * nadie tiene que "avisar" que entró o salió.
 */
class SesionRepository(private val api: AvisosApi, private val store: SesionStore) {

    val sesion: Flow<Sesion?> = store.sesion

    suspend fun entrar(usuario: String, password: String) {
        store.guardar(api.login(Credenciales(usuario.trim().lowercase(), password)).toSesion())
    }

    suspend fun registrar(usuario: String, password: String, codigoProfesor: String) {
        val credenciales = Credenciales(
            usuario = usuario.trim().lowercase(),
            password = password,
            codigoProfesor = codigoProfesor.trim().ifEmpty { null }
        )
        store.guardar(api.register(credenciales).toSesion())
    }

    suspend fun salir() {
        store.borrar()
    }

    /** Para la capa de red, que corre en su propio hilo y no puede suspender. */
    fun tokenActual(): String? = runBlocking { store.sesion.first() }?.accessToken
}
