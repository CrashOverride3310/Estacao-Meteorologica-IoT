
package com.example.estacaobackend.service

// ============================================================
// ESTAÇÃO METEOROLÓGICA IoT — BACKEND
// ============================================================
//
// Arquivo: LeituraMeteorologicaService.kt
// Linguagem: Kotlin
// Framework: Spring Boot / Spring Data JPA
//
// Responsabilidades:
// - Receber os dados meteorológicos para persistência.
// - Salvar registros no banco de dados PostgreSQL.
// - Consultar todas as leituras armazenadas.
// - Identificar a leitura meteorológica mais recente.
//
// Esta classe representa a camada de serviço da aplicação.
//
// A camada de serviço conecta as funcionalidades
// do backend à camada de acesso ao banco de dados.
//
// ============================================================


// Importa a entidade que representa uma leitura
// meteorológica armazenada no PostgreSQL.
import com.example.estacaobackend.entity.LeituraMeteorologica

// Importa o repositório Spring Data JPA responsável
// pelas operações de persistência e consulta.
import com.example.estacaobackend.repository.LeituraMeteorologicaRepository

// Importa a anotação utilizada para identificar
// uma classe de serviço gerenciada pelo Spring.
import org.springframework.stereotype.Service


// ============================================================
// SERVIÇO DE LEITURAS METEOROLÓGICAS
// ============================================================
//
// @Service identifica a classe como um componente
// da camada de serviços da aplicação Spring Boot.
//
// O Spring gerencia sua criação e disponibilização
// para outros componentes que precisem utilizá-la.
//
// O repositório é recebido pelo construtor,
// utilizando o mecanismo de injeção de dependências.
//
// A propriedade repository é privada, restringindo
// seu acesso à própria classe.
// ============================================================

@Service
class LeituraMeteorologicaService(
    private val repository: LeituraMeteorologicaRepository
) {

    // ========================================================
    // SALVAR LEITURA METEOROLÓGICA
    // ========================================================
    //
    // Recebe um objeto LeituraMeteorologica e utiliza
    // o repositório JPA para persistir seus dados.
    //
    // O método save() permite inserir ou atualizar
    // uma entidade, conforme seu estado e identificador.
    //
    // Parâmetro:
    // leitura — objeto contendo os dados meteorológicos.
    //
    // Retorno:
    // Entidade retornada após a operação de persistência.
    //
    // Esta função é utilizada para registrar os dados
    // recebidos pela aplicação.
    // ========================================================

    fun salvar(leitura: LeituraMeteorologica): LeituraMeteorologica {
        return repository.save(leitura)
    }


    // ========================================================
    // LISTAR TODAS AS LEITURAS
    // ========================================================
    //
    // Consulta todos os registros meteorológicos
    // disponíveis no repositório.
    //
    // O método findAll() é disponibilizado
    // automaticamente pelo Spring Data JPA.
    //
    // Retorno:
    // Lista de objetos LeituraMeteorologica.
    //
    // Não é aplicada nenhuma ordenação ou filtragem
    // adicional nesta implementação.
    // ========================================================

    fun listarTodas(): List<LeituraMeteorologica> {
        return repository.findAll()
    }


    // ========================================================
    // BUSCAR A LEITURA MAIS RECENTE
    // ========================================================
    //
    // Consulta todas as leituras armazenadas e seleciona
    // aquela que possui o maior valor no campo dataHora.
    //
    // Etapas:
    // 1. Obtém todos os registros com findAll().
    // 2. Compara os valores de dataHora.
    // 3. Retorna o registro com a data/hora mais recente.
    //
    // maxByOrNull retorna null caso a lista esteja vazia.
    //
    // Retorno:
    // LeituraMeteorologica? — leitura mais recente
    // ou null quando não existem registros.
    //
    // IMPORTANTE:
    // A implementação atual carrega todos os registros
    // antes de identificar o mais recente.
    //
    // ========================================================

    fun buscarUltima(): LeituraMeteorologica? {
        return repository.findAll()
            .maxByOrNull { it.dataHora }
    }
}
