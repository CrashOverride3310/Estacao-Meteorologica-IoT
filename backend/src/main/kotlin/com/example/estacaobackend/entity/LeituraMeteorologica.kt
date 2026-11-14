
package com.example.estacaobackend.entity

// ============================================================
// ESTAÇÃO METEOROLÓGICA IoT — BACKEND
// ============================================================
//
// Arquivo: LeituraMeteorologica.kt
// Linguagem: Kotlin
// Framework: Spring Boot / Jakarta Persistence (JPA)
//
// Responsabilidades:
// - Representar uma leitura meteorológica.
// - Mapear os dados para uma tabela PostgreSQL.
// - Identificar cada registro por um ID.
// - Armazenar os valores recebidos dos sensores.
// - Registrar a data e a hora da leitura.
//
// A entidade participa da camada de persistência
// utilizada pelo backend da estação meteorológica.
//
// IMPORTANTE:
// ============================================================


// Importa as anotações da Jakarta Persistence,
// utilizadas no mapeamento objeto-relacional.
import jakarta.persistence.*

// Importa a classe utilizada para armazenar
// a data e a hora associadas ao registro.
import java.time.LocalDateTime

// Importa a anotação Jackson utilizada para
// reconhecer nomes alternativos de propriedades JSON.
import com.fasterxml.jackson.annotation.JsonAlias


// ============================================================
// ENTIDADE JPA — LEITURA METEOROLÓGICA
// ============================================================
//
// @Entity indica que esta classe representa
// uma entidade gerenciada pelo JPA.
//
// @Table define o nome da tabela correspondente
// no banco de dados PostgreSQL.
//
// A data class do Kotlin disponibiliza recursos
// como equals(), hashCode(), toString() e copy().
// ============================================================

@Entity
@Table(name = "leituras_meteorologicas")
data class LeituraMeteorologica(

    // ========================================================
    // IDENTIFICADOR DO REGISTRO
    // ========================================================
    //
    // @Id identifica a chave primária da entidade.
    //
    // @GeneratedValue com GenerationType.IDENTITY
    // utiliza a geração de identificadores pelo banco.
    //
    // O valor inicial é null para permitir que o ID
    // seja atribuído durante a persistência.
    // ========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,


    // ========================================================
    // TEMPERATURA AMBIENTE — DHT22
    // ========================================================
    //
    // Armazena a temperatura ambiente obtida
    // pelo sensor DHT22 conectado à ESP32.
    //
    // Unidade utilizada pelo firmware: °C.
    // Tipo: Double, permitindo valores decimais.
    // ========================================================

    val temperatura: Double? = null,


    // ========================================================
    // UMIDADE RELATIVA — DHT22
    // ========================================================
    //
    // Armazena a umidade relativa do ar obtida
    // pelo sensor DHT22.
    //
    // Unidade utilizada pelo firmware: porcentagem.
    // ========================================================

    val umidade: Double? = null,


    // ========================================================
    // TEMPERATURA — BMP280
    // ========================================================
    //
    // Armazena a temperatura calculada pelo BMP280.
    //
    // O firmware transmite a propriedade JSON
    // utilizando o nome "temperatura_bmp".
    //
    // @param:JsonAlias permite reconhecer esse nome
    // durante a desserialização do objeto JSON.
    //
    // A propriedade Kotlin mantém o nome temperaturaBmp.
    // ========================================================

    @param:JsonAlias("temperatura_bmp")
    val temperaturaBmp: Double? = null,


    // ========================================================
    // PRESSÃO ATMOSFÉRICA — BMP280
    // ========================================================
    //
    // Armazena a pressão atmosférica calculada
    // pelo sensor BMP280.
    //
    // O firmware envia os valores em hPa.
    // ========================================================

    val pressao: Double? = null,


    // ========================================================
    // UNIDADE DA PRESSÃO ATMOSFÉRICA
    // ========================================================
    //
    // Armazena a unidade utilizada para representar
    // a pressão atmosférica.
    //
    // No firmware da ESP32, o valor enviado é "hPa".
    //
    // A anotação reconhece o nome "unidade_pressao"
    // durante a desserialização do JSON.
    // ========================================================

    @param:JsonAlias("unidade_pressao")
    val unidadePressao: String? = null,


    // ========================================================
    // ÍNDICE EXPERIMENTAL — MQ-135
    // ========================================================
    //
    // Armazena o índice experimental calculado
    // a partir das leituras analógicas do MQ-135.
    //
    // O firmware calcula a média de dez amostras
    // do ADC e converte o resultado para uma escala
    // experimental de 0 a 100.
    //
    // IMPORTANTE:
    // O índice não representa uma concentração
    // de gases calibrada em PPM.
    //
    // A anotação reconhece o nome "qualidade_ar"
    // recebido no JSON publicado pela ESP32.
    // ========================================================

    @param:JsonAlias("qualidade_ar")
    val qualidadeAr: Int? = null,


    // ========================================================
    // LUMINOSIDADE — LDR
    // ========================================================
    //
    // Armazena o valor da leitura analógica
    // realizada pelo LDR conectado ao GPIO35.
    //
    // O resultado representa uma leitura do ADC
    // e não uma medida calibrada em lux.
    // ========================================================

    val luminosidade: Int? = null,


    // ========================================================
    // PRECIPITAÇÃO SIMULADA
    // ========================================================
    //
    // Armazena o valor gerado pela simulação
    // de chuva implementada no firmware.
    //
    // O projeto não utiliza um pluviômetro físico.
    //
    // O firmware gera um valor aleatório
    // no intervalo de 0 a 100.
    // ========================================================

    val chuva: Int? = null,


    // ========================================================
    // DATA E HORA DO REGISTRO
    // ========================================================
    //
    // Armazena a data e a hora associadas
    // à criação da entidade no backend.
    //
    // LocalDateTime.now() utiliza a data e a hora
    // do ambiente em que o backend está executando,
    // quando o valor padrão é utilizado.
    //
    // Esse campo permite organizar historicamente
    // as leituras registradas no PostgreSQL.
    // ========================================================

    val dataHora: LocalDateTime = LocalDateTime.now()
)
