
package com.example.estacaometeorologica.data

// Biblioteca Retrofit, responsável pela comunicação HTTP
// entre o aplicativo Android e o backend da estação meteorológica.
import retrofit2.Retrofit

// Conversor Gson, utilizado para transformar as respostas JSON
// recebidas do servidor em objetos Kotlin.
import retrofit2.converter.gson.GsonConverterFactory

/**
 * RetrofitClient
 *
 * Objeto responsável por configurar e disponibilizar
 * a conexão HTTP com o backend da Estação Meteorológica IoT.
 *
 * Utiliza o padrão Singleton por meio da declaração "object",
 * garantindo uma única instância compartilhada da configuração.
 */
object RetrofitClient {

    // Endereço público do backend da Estação Meteorológica IoT.
    // O servidor disponibiliza os dados dos sensores e a localização GPS.
    private const val BASE_URL = "https://estacao-iot.duckdns.org/"

    /**
     * Instância da interface EstacaoApi.
     *
     * O modificador "by lazy" permite inicializar o Retrofit
     * somente quando a propriedade for acessada pela primeira vez.
     *
     * Isso evita criar repetidamente a configuração HTTP.
     */
    val api: EstacaoApi by lazy {

        Retrofit.Builder()

            // Define o endereço principal do servidor.
            .baseUrl(BASE_URL)

            // Configura o Gson para interpretar automaticamente
            // os dados JSON retornados pela API REST.
            .addConverterFactory(GsonConverterFactory.create())

            // Constrói a instância configurada do Retrofit.
            .build()

            // Implementa a interface EstacaoApi, responsável
            // pelas requisições HTTP aos endpoints do backend.
            .create(EstacaoApi::class.java)
    }
}
