package mx.tec.avisos.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Firma cada petición con el token de acceso, si hay uno.
 *
 * Recibe una función y no el repositorio: la capa de red no necesita saber
 * dónde vive la sesión, solo cómo pedir el token. Corre en un hilo de OkHttp,
 * nunca en el principal, así que la función puede bloquear.
 */
class AuthInterceptor(private val token: () -> String?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val actual = token() ?: return chain.proceed(chain.request())
        val firmada = chain.request().newBuilder()
            .header("Authorization", "Bearer $actual")
            .build()
        return chain.proceed(firmada)
    }
}
