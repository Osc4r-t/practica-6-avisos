package mx.tec.avisos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import mx.tec.avisos.data.local.SesionStore
import mx.tec.avisos.data.remote.AvisosApi
import mx.tec.avisos.data.remote.Credenciales
import mx.tec.avisos.data.remote.RefreshBody
import mx.tec.avisos.data.remote.toSesion
import mx.tec.avisos.domain.Sesion
import retrofit2.HttpException
import java.io.IOException

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

    /** Primero se borra lo local: aunque no haya red, salir siempre funciona. */
    suspend fun salir() {
        val actual = store.sesion.first()
        store.borrar()
        if (actual != null) {
            try {
                api.logout(RefreshBody(actual.refreshToken))
            } catch (e: IOException) {
                // Sin red no se avisa al servidor; el refresh token expira solo en 7 días.
            } catch (e: HttpException) {
                // Ya estaba revocado, o nunca existió. Salir igual se logró.
            }
        }
    }

    /** Para la capa de red, que corre en su propio hilo y no puede suspender. */
    fun tokenActual(): String? = runBlocking { store.sesion.first() }?.accessToken

    fun refrescarToken(): String? = runBlocking { refrescar() }

    /**
     * Cambia el refresh token por un par nuevo. Si el servidor dice que ese
     * refresh ya no sirve, la sesión se acabó: se borra, y la UI vuelve sola
     * al login porque el Flow emite null.
     */
    private suspend fun refrescar(): String? {
        val actual = store.sesion.first() ?: return null
        return try {
            val nueva = api.refresh(RefreshBody(actual.refreshToken)).toSesion()
            store.guardar(nueva)
            nueva.accessToken
        } catch (e: HttpException) {
            if (e.code() == 401) store.borrar()
            null
        } catch (e: IOException) {
            // Sin red: la sesión sigue guardada, solo falló este intento.
            null
        }
    }
}
