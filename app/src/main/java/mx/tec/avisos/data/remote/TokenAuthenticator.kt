package mx.tec.avisos.data.remote

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * OkHttp lo llama SOLO cuando una respuesta trae 401. Su trabajo: conseguir
 * un token nuevo y devolver la misma petición firmada otra vez — o `null`,
 * que significa "ríndete y entrega el 401 tal cual".
 *
 * Tres guardas, y las tres evitan un ciclo infinito:
 *   1. Si la petición no llevaba token, el 401 no es por el token (un login
 *      con la contraseña mal, por ejemplo). No hay nada que refrescar.
 *   2. Si la petición era el propio refresh, ya no hay a quién pedirle.
 *   3. Si ya se reintentó una vez, no se insiste.
 */
class TokenAuthenticator(
    private val tokenActual: () -> String?,
    private val refrescar: () -> String?
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val enviado = response.request.header("Authorization")?.removePrefix("Bearer ") ?: return null
        if (response.request.url.encodedPath.endsWith("/auth/refresh")) return null
        if (response.priorResponse != null) return null

        // Una sola renovación a la vez: si dos peticiones fallan juntas, la
        // segunda espera aquí y al salir encuentra el token que trajo la primera.
        val nuevo = synchronized(this) {
            val vigente = tokenActual()
            if (vigente != null && vigente != enviado) vigente else refrescar()
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $nuevo")
            .build()
    }
}
