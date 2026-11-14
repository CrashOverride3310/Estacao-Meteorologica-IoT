
package com.example.estacaometeorologica.data

/**
 * LocalizacaoData
 *
 * Classe de dados responsável por representar a localização
 * geográfica da Estação Meteorológica IoT.
 *
 * As coordenadas são obtidas pelo App2 (LocalizacaoGPS),
 * instalado no smartphone Samsung Galaxy A23,
 * que acompanha a estação meteorológica.
 *
 * O App2 envia as informações de localização ao backend,
 * que disponibiliza os dados por meio de uma API REST.
 *
 * O App1 consulta essas informações utilizando Retrofit
 * e utiliza latitude e longitude para posicionar o
 * marcador da estação no mapa exibido pelo dashboard.
 *
 * Todas as propriedades são opcionais (nullable),
 * permitindo representar informações ausentes com null.
 *
 * Esta classe não realiza a captura do GPS diretamente.
 * Sua função é armazenar os dados recebidos da API.
 */
data class LocalizacaoData(

    // Identificador numérico do registro de localização,
    // conforme disponibilizado pelo backend.
    val id: Long? = null,

    // Coordenada geográfica de latitude, em graus decimais.
    // Valores positivos indicam o hemisfério norte
    // e negativos indicam o hemisfério sul.
    val latitude: Double? = null,

    // Coordenada geográfica de longitude, em graus decimais.
    // Valores positivos indicam o hemisfério leste
    // e negativos indicam o hemisfério oeste.
    val longitude: Double? = null,

    // Registro temporal associado à localização GPS.
    // O tipo Long permite armazenar um valor inteiro.
    // A unidade do timestamp depende do formato enviado
    // pelo backend.
    val timestamp: Long? = null
)