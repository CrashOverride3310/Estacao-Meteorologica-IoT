
package com.example.estacaobackend.entity

// ============================================================
// ESTAÇÃO METEOROLÓGICA IoT — BACKEND
// ============================================================
//
// Arquivo: LocalizacaoEstacao.kt
// Linguagem: Kotlin
// Framework: Spring Boot / Jakarta Persistence (JPA)
//
// Responsabilidades:
// - Representar os dados de localização GPS da estação.
// - Armazenar as coordenadas recebidas do App2.
// - Mapear as informações para uma tabela PostgreSQL.
// - Registrar o timestamp associado à localização.
//
// Fluxo dos dados:
//
// App2 (LocalizacaoGPS)
//        |
//        v
// HiveMQ Cloud (MQTT)
//        |
//        v
// Backend Kotlin / Spring Boot
//        |
//        v
// PostgreSQL
//        |
//        v
// App1 (Dashboard Android)
//
// ============================================================


// Importa as anotações do Jakarta Persistence,
// utilizadas para mapear entidades no banco de dados.
import jakarta.persistence.*


// ============================================================
// ENTIDADE JPA — LOCALIZAÇÃO DA ESTAÇÃO
// ============================================================
//
// @Entity identifica esta classe como uma entidade JPA.
//
// @Table define o nome da tabela utilizada para
// armazenar as informações de localização GPS.
//
// A data class do Kotlin disponibiliza automaticamente
// métodos como equals(), hashCode(), toString() e copy().
//
// A entidade representa uma localização da estação
// meteorológica registrada pelo backend.
// ============================================================

@Entity
@Table(name = "localizacoes_estacao")
data class LocalizacaoEstacao(

    // ========================================================
    // IDENTIFICADOR DA LOCALIZAÇÃO
    // ========================================================
    //
    // @Id identifica a chave primária da entidade.
    //
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // utiliza a geração de identificadores pelo banco.
    //
    // O valor inicial null permite criar uma nova
    // entidade sem informar manualmente seu ID.
    //
    // Tipo: Long?
    // ========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,


    // ========================================================
    // LATITUDE GPS
    // ========================================================
    //
    // Armazena a coordenada geográfica de latitude
    // obtida pelo aplicativo Android App2.
    //
    // A latitude representa a posição do dispositivo
    // em relação à Linha do Equador.
    //
    // Unidade: graus decimais.
    // Faixa geográfica: -90 a +90 graus.
    //
    // Tipo: Double.
    //
    // O valor deve ser informado na criação da entidade.
    // ========================================================

    val latitude: Double,


    // ========================================================
    // LONGITUDE GPS
    // ========================================================
    //
    // Armazena a coordenada geográfica de longitude
    // obtida pelo aplicativo Android App2.
    //
    // A longitude representa a posição do dispositivo
    // em relação ao Meridiano de Greenwich.
    //
    // Unidade: graus decimais.
    // Faixa geográfica: -180 a +180 graus.
    //
    // Tipo: Double.
    //
    // O valor deve ser informado na criação da entidade.
    // ========================================================

    val longitude: Double,


    // ========================================================
    // TIMESTAMP DA LOCALIZAÇÃO
    // ========================================================
    //
    // Armazena a informação temporal recebida do App2.
    //
    // Na implementação do aplicativo Android,
    // o timestamp é enviado em milissegundos
    // desde a época Unix (01/01/1970 UTC).
    //
    // Esse valor permite identificar temporalmente
    // as localizações registradas pela estação.
    //
    // Tipo: Long.
    //
    // O valor é recebido do App2 e não é gerado
    // automaticamente por esta entidade.
    // ========================================================

    val timestamp: Long
)
