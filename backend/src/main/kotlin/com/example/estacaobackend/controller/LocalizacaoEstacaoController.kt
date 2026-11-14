
package com.example.estacaobackend.controller

// ============================================================
// ESTAÇÃO METEOROLÓGICA IoT — BACKEND
// ============================================================
//
// Arquivo: LocalizacaoEstacaoController.kt
// Linguagem: Kotlin
// Framework: Spring Boot / Spring Web
//
// Responsabilidades:
// - Disponibilizar a localização GPS da estação via API REST.
// - Atender requisições HTTP do aplicativo Android App1.
// - Consultar a última localização armazenada no banco.
// - Integrar o controller ao serviço de localização.
//
// Fluxo da localização GPS:
//
// App2 (Samsung Galaxy A23)
//         |
//         v
// HiveMQ Cloud (MQTT)
//         |
//         v
// MqttListener
//         |
//         v
// LocalizacaoEstacaoService
//         |
//         v
// PostgreSQL
//         |
//         v
// LocalizacaoEstacaoController
//         |
//         v
// App1 — Dashboard Android
//
// ============================================================


// Importa a entidade que representa os dados
// de localização GPS da estação meteorológica.
import com.example.estacaobackend.entity.LocalizacaoEstacao

// Importa o serviço responsável por consultar
// as localizações armazenadas no banco de dados.
import com.example.estacaobackend.service.LocalizacaoEstacaoService

// Permite mapear requisições HTTP GET.
import org.springframework.web.bind.annotation.GetMapping

// Define o endereço base dos endpoints REST.
import org.springframework.web.bind.annotation.RequestMapping

// Identifica a classe como um controller REST
// gerenciado pelo Spring Boot.
import org.springframework.web.bind.annotation.RestController


// ============================================================
// CONTROLLER DE LOCALIZAÇÃO DA ESTAÇÃO
// ============================================================
//
// @RestController identifica a classe como um componente
// responsável por atender requisições HTTP.
//
// @RequestMapping("/api/estacao") define o caminho
// base utilizado pelos endpoints deste controller.
//
// A classe recebe LocalizacaoEstacaoService por meio
// da injeção de dependências do Spring Boot.
//
// A propriedade service é privada e utilizada
// para consultar os registros de localização.
// ============================================================

@RestController
@RequestMapping("/api/estacao")
class LocalizacaoEstacaoController(

    // Serviço responsável pelas operações
    // de consulta da localização GPS.
    private val service: LocalizacaoEstacaoService

) {

    // ========================================================
    // ENDPOINT — ÚLTIMA LOCALIZAÇÃO GPS
    // ========================================================
    //
    // Método HTTP: GET
    //
    // Caminho completo:
    // /api/estacao/localizacao
    //
    // Responsabilidade:
    // Consultar a localização mais recente da estação
    // registrada no banco de dados PostgreSQL.
    //
    // O endpoint é utilizado pelo App1 para obter
    // as coordenadas GPS fornecidas pelo App2.
    //
    // Etapas:
    // 1. Recebe uma requisição HTTP GET.
    // 2. Chama buscarUltima() na camada de serviço.
    // 3. Retorna o objeto LocalizacaoEstacao.
    //
    // Retorno:
    // LocalizacaoEstacao?
    //
    // O resultado pode ser null quando não existe
    // uma localização disponível para consulta.
    //
    // O Spring Boot serializa o objeto retornado
    // para compor o corpo da resposta HTTP.
    // ========================================================

    @GetMapping("/localizacao")
    fun buscarUltimaLocalizacao(): LocalizacaoEstacao? {
        return service.buscarUltima()
    }
}
