
package com.example.estacaometeorologica.data

/**
 * SensorData
 *
 * Classe de dados responsável por representar as informações
 * meteorológicas recebidas do backend da Estação Meteorológica IoT.
 *
 * Os dados são coletados pelo firmware MicroPython da ESP32,
 * enviados ao broker MQTT e processados pelo backend,
 * que disponibiliza as informações por meio de uma API REST.
 *
 * O Retrofit, juntamente com o Gson, utiliza esta estrutura
 * para representar os dados recebidos em formato JSON.
 *
 * As propriedades são opcionais (nullable), permitindo
 * representar valores ausentes com null.
 *
 * Esta classe apenas armazena os dados recebidos.
 * Ela não realiza leituras diretamente dos sensores.
 */
data class SensorData(

    // Temperatura ambiente coletada pelo sensor DHT22.
    // Unidade: graus Celsius (°C).
    val temperatura: Double? = null,

    // Temperatura medida pelo sensor BMP280.
    // Unidade: graus Celsius (°C).
    val temperaturaBmp: Double? = null,

    // Umidade relativa do ar coletada pelo sensor DHT22.
    // Unidade: porcentagem (%).
    val umidade: Double? = null,

    // Pressão atmosférica coletada pelo sensor BMP280.
    val pressao: Double? = null,

    // Identificação da unidade utilizada para a pressão.
    // Exemplo: hPa (hectopascal).
    val unidadePressao: String? = null,

    // Índice experimental calculado a partir da leitura
    // analógica do sensor MQ-135.
    //
    // Representa um valor na escala experimental de 0 a 100.
    // Não corresponde a uma concentração calibrada em PPM.
    val qualidadeAr: Int? = null,

    // Leitura de luminosidade obtida pelo LDR conectado
    // à entrada analógica da ESP32.
    val luminosidade: Int? = null,

    // Valor de chuva utilizado pelo sistema meteorológico.
    // Atualmente, a pluviometria é simulada pelo firmware,
    // sem utilização de um sensor físico de chuva.
    val chuva: Int? = null
)
