package mx.tec.avisos.data.remote

import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object Network {

    private const val BASE_URL = "https://startdroid.com/api/"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /**
     * Arma la API. El interceptor firma cada petición con el token; el
     * authenticator reacciona cuando el servidor responde 401.
     *
     * Los dos son opcionales para que el proyecto compile antes de que existan.
     */
    fun crearApi(interceptor: Interceptor? = null, authenticator: Authenticator? = null): AvisosApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            // Con Level.HEADERS o BODY, sin esta línea el token sale en el Logcat.
            redactHeader("Authorization")
        }

        val client = OkHttpClient.Builder().apply {
            if (interceptor != null) addInterceptor(interceptor)
            if (authenticator != null) authenticator(authenticator)
            // El de log va al final, para que vea la petición ya firmada.
            addInterceptor(logging)
        }.build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AvisosApi::class.java)
    }
}
