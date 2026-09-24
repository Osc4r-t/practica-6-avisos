package mx.tec.avisos

import android.app.Application
import mx.tec.avisos.data.AvisosRepository
import mx.tec.avisos.data.SesionRepository
import mx.tec.avisos.data.remote.AuthInterceptor
import mx.tec.avisos.data.remote.AvisosApi
import mx.tec.avisos.data.remote.Network

/**
 * El contenedor de dependencias: quién construye a quién, en un solo lugar.
 *
 * Hay un ciclo aparente —la API necesita el token, que está en el repositorio
 * de sesión, que necesita la API— y se rompe con dos cosas: `by lazy`, y que
 * el interceptor recibe una FUNCIÓN que pide el token, no el repositorio.
 * Nadie llama a esa función hasta que sale la primera petición.
 */
class AppContainer {

    private val api: AvisosApi by lazy {
        Network.crearApi(interceptor = AuthInterceptor { sesionRepository.tokenActual() })
    }

    val sesionRepository: SesionRepository by lazy { SesionRepository(api) }

    val avisosRepository: AvisosRepository by lazy { AvisosRepository(api) }
}

/** Vive tanto como el proceso. Declarada en el manifiesto con `android:name`. */
class AvisosApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
