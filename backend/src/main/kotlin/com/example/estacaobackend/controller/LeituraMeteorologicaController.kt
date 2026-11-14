
package com.example.estacaobackend.controller

// ============================================================
// ESTAÇÃO METEOROLÓGICA IoT — BACKEND
// ============================================================
//
// Arquivo: LeituraMeteorologicaController.kt
// Linguagem: Kotlin
// Framework: Spring Boot / Spring Web
//
// Responsabilidades:
// - Disponibilizar endpoints HTTP para consulta.
// - Permitir o acesso às leituras meteorológicas.
// - Consultar o histórico de dados armazenados.
// - Retornar a leitura meteorológica mais recente.
// - Integrar a camada REST à camada de serviços.
//
// Fluxo de consulta:
//
// App1 (Android)
//       |
//       v
// API REST (Spring Boot)
//       |
//       v
// LeituraMeteorologicaController
//       |
//       v
// LeituraMeteorologicaService
//       |
//       v
// LeituraMeteorologicaRepository
//       |
//       v
// PostgreSQL
//
// ============================================================


// Importa a entidade que representa os dados
// meteorológicos armazenados no banco de dados.
import com.example.estacaobackend.entity.LeituraMeteorologica

// Importa o serviço responsável pelas operações
// de consulta das leituras meteorológicas.
import com.example.estacaobackend.service.LeituraMeteorologicaService

// Anotação utilizada para mapear requisições HTTP GET.
import org.springframework.web.bind.annotation.GetMapping

// Define o caminho base dos endpoints do controller.
import org.springframework.web.bind.annotation.RequestMapping

// Identifica a classe como um controller REST.
// Os objetos retornados são serializados na resposta HTTP.
import org.springframework.web.bind.annotation.RestController


// ============================================================
// CONTROLLER DE LEITURAS METEOROLÓGICAS
// ============================================================
//
// @RestController informa ao Spring Boot que esta classe
// é responsável por atender requisições HTTP REST.
//
// @RequestMapping("/api/estacao") define o caminho
// base utilizado pelos endpoints da classe.
//
// O serviço é recebido pelo construtor utilizando
// o mecanismo de injeção de dependências do Spring.
//
// A propriedade service é privada e utilizada
// internamente pelos métodos de consulta.
// ============================================================

@RestController
@RequestMapping("/api/estacao")
class LeituraMeteorologicaController(
    private val service: LeituraMeteorologicaService
) {

    // ========================================================
    // ENDPOINT — LISTAR TODAS AS LEITURAS
    // ========================================================
    //
    // Método HTTP: GET
    //
    // Caminho completo:
    // /api/estacao/dados
    //
    // Responsabilidade:
    // Consultar todas as leituras meteorológicas
    // armazenadas no banco de dados PostgreSQL.
    //
    // O método utiliza listarTodas() da camada de serviço,
    // que consulta o repositório Spring Data JPA.
    //
    // Retorno:
    // List<LeituraMeteorologica>
    //
    // O Spring Boot serializa a lista de entidades
    // para a representação da resposta HTTP.
    //
    // Se não existirem registros, o serviço retorna
    // uma lista vazia.
    // ========================================================

    @GetMapping("/dados")
    fun listarDados(): List<LeituraMeteorologica> {
        return service.listarTodas()
    }


    // ========================================================
    // ENDPOINT — CONSULTAR A ÚLTIMA LEITURA
    // ========================================================
    //
    // Método HTTP: GET
    //
    // Caminho completo:
    // /api/estacao/ultima
    //
    // Responsabilidade:
    // Disponibilizar a leitura meteorológica mais recente
    // registrada no banco de dados.
    //
    // O método utiliza buscarUltima() da camada de serviço.
    //
    // Na implementação atual, o serviço seleciona
    // o registro com o maior valor de dataHora.
    //
    // Retorno:
    // LeituraMeteorologica?
    //
    // O retorno pode ser null caso não existam
    // leituras armazenadas no banco de dados.
    //
    // Esse endpoint é utilizado pelo App1 para
    // obter os dados meteorológicos atualizados.
    // ========================================================

    @GetMapping("/ultima")
    fun buscarUltima(): LeituraMeteorologica? {
        return service.buscarUltima()
    }
}
