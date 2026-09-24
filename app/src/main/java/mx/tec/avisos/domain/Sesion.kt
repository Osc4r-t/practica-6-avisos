package mx.tec.avisos.domain

/** Lo que el servidor dice que eres. Lo decide él al entrar; la app solo lo lee. */
enum class Rol {
    ALUMNO, PROFESOR;

    companion object {
        fun de(texto: String): Rol = if (texto == "profesor") PROFESOR else ALUMNO
    }
}

/**
 * Una sesión abierta: quién eres y las dos llaves que lo demuestran.
 *
 * Kotlin puro. No sabe de Retrofit, ni de DataStore, ni de Android: si mañana
 * los tokens vinieran de otro servidor o se guardaran en otro lado, esta clase
 * no cambiaría ni una línea.
 *
 *  - `accessToken`  viaja en cada petición. Dura minutos.
 *  - `refreshToken` NO viaja nunca, salvo para pedir otro access. Dura días.
 *  - `expiraEn`     el segundo (epoch) en que el access deja de servir.
 */
data class Sesion(
    val usuario: String,
    val rol: Rol,
    val accessToken: String,
    val refreshToken: String,
    val expiraEn: Long
) {
    fun segundosRestantes(ahora: Long = System.currentTimeMillis() / 1000): Long = expiraEn - ahora

    // EXPERIMENTO C2: la app "decide" que todos pueden publicar. ¿Y el servidor?
    val puedePublicar: Boolean get() = true
}
