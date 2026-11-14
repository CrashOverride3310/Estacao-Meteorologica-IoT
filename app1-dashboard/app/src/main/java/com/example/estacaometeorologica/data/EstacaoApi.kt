
package com.example.estacaometeorologica.data

// Classe do Retrofit utilizada para representar uma resposta HTTP.
// Permite consultar o status da requisição e os dados retornados.
import retrofit2.Response

// Anotação do Retrofit que identifica requisições HTTP GET.
import retrofit2.http.GET

/**
 * EstacaoApi
 *
 * Interface que define os endpoints REST utilizados pelo App1.
 *
 * O Retrofit implementa automaticamente esta interface,
 * permitindo que o aplicativo consulte o backend sem
 * precisar implementar manualmente as requisições HTTP.
 *
 * A URL principal do servidor é configurada em RetrofitClient.kt.
 */
interface EstacaoApi {

    /**
     * Consulta a leitura meteorológica mais recente.
     *
     * Endpoint: GET /api/estacao/ultima
     *
     * Retorna os dados dos sensores recebidos pelo backend,
     * incluindo temperatura, umidade, pressão atmosférica,
     * luminosidade, índice experimental do MQ-135 e chuva.
     *
     * A palavra "suspend" permite executar esta operação
     * dentro de uma coroutine Kotlin, sem bloquear a
     * thread principal da interface Android.
     *
     * O resultado é convertido para o modelo SensorData.
     */
    @GET("api/estacao/ultima")
    suspend fun obterDados(): Response<SensorData>

    /**
     * Consulta a localização GPS mais recente da estação.
     *
     * Endpoint: GET /api/estacao/localizacao
     *
     * O backend disponibiliza as coordenadas enviadas pelo
     * App2, instalado no smartphone próximo à ESP32.
     *
     * As coordenadas são utilizadas pelo App1 para atualizar
     * o marcador exibido no mapa.
     *
     * O resultado é convertido para o modelo LocalizacaoData.
     */
    @GET("api/estacao/localizacao")
    suspend fun obterLocalizacao(): Response<LocalizacaoData>

}
