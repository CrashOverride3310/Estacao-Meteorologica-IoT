
package com.example.estacaobackend.service

// ============================================================
// ESTAÇÃO METEOROLÓGICA IoT — BACKEND
// ============================================================
//
// Arquivo: LocalizacaoEstacaoService.kt
// Linguagem: Kotlin
// Framework: Spring Boot / Spring Data JPA
//
// Responsabilidades:
// - Receber os registros de localização GPS.
// - Salvar as coordenadas no banco PostgreSQL.
// - Consultar os registros de localização armazenados.
// - Identificar a localização com o timestamp mais recente.
//
// Fluxo de gravação:
//
// App2 (LocalizacaoGPS)
//        |
//        v
// HiveMQ Cloud
//        |
//        v
// MqttListener
//        |
//        v
// LocalizacaoEstacaoService
//        |
//        v
// LocalizacaoEstacaoRepository
//        |
//        v
// PostgreSQL
//
// ============================================================


// Importa a entidade que representa as coordenadas
// GPS recebidas do aplicativo Android App2.
import com.example.estacaobackend.entity.LocalizacaoEstacao

// Importa o repositório responsável pelas operações
// de persistência dos registros de localização.
import com.example.estacaobackend.repository.LocalizacaoEstacaoRepository

// Importa a anotação utilizada para identificar
// componentes da camada de serviço do Spring Boot.
import org.springframework.stereotype.Service


// ============================================================
// SERVIÇO DE LOCALIZAÇÃO DA ESTAÇÃO
// ============================================================
//
// @Service identifica esta classe como um componente
// da camada de serviços gerenciado pelo Spring Boot.
//
// O repositório é recebido por meio do construtor,
// utilizando o mecanismo de injeção de dependências.
//
// A propriedade repository é privada e utilizada
// internamente para acessar os dados de localização.
// ============================================================

@Service
class LocalizacaoEstacaoService(

    // Repositório JPA responsável pelo acesso
    // à tabela de localizações do PostgreSQL.
    private val repository: LocalizacaoEstacaoRepository

) {

    // ========================================================
    // SALVAR LOCALIZAÇÃO GPS
    // ========================================================
    //
    // Recebe uma entidade LocalizacaoEstacao contendo:
    //
    // - Identificador do registro, quando disponível.
    // - Latitude em graus decimais.
    // - Longitude em graus decimais.
    // - Timestamp da localização.
    //
    // Utiliza o método save() do Spring Data JPA
    // para inserir ou atualizar o registro.
    //
    // Parâmetro:
    // localizacao — entidade a ser persistida.
    //
    // Retorno:
    // LocalizacaoEstacao — entidade retornada pelo
    // repositório após a operação de persistência.
    // ========================================================

    fun salvar(localizacao: LocalizacaoEstacao): LocalizacaoEstacao {
        return repository.save(localizacao)
    }


    // ========================================================
    // CONSULTAR A ÚLTIMA LOCALIZAÇÃO GPS
    // ========================================================
    //
    // Identifica o registro com o maior timestamp
    // entre as localizações armazenadas.
    //
    // Etapas:
    //
    // 1. Consulta todos os registros utilizando findAll().
    // 2. Compara o timestamp de cada localização.
    // 3. Seleciona a localização mais recente.
    //
    // maxByOrNull retorna null quando a lista está vazia.
    //
    // Retorno:
    // LocalizacaoEstacao? — registro mais recente
    // ou null caso não existam localizações.
    //
    // IMPORTANTE:
    // A consulta carrega todos os registros antes de selecionar o mais recente.
    //
    //
    // O resultado é utilizado pelo controller GPS
    // para responder ao endpoint:
    //
    // GET /api/estacao/localizacao
    // ========================================================

    fun buscarUltima(): LocalizacaoEstacao? {
        return repository.findAll()
            .maxByOrNull { it.timestamp }
    }
}
