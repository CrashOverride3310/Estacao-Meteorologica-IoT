
package com.example.estacaobackend.repository

// ============================================================
// ESTAÇÃO METEOROLÓGICA IoT — BACKEND
// ============================================================
//
// Arquivo: LocalizacaoEstacaoRepository.kt
// Linguagem: Kotlin
// Framework: Spring Boot / Spring Data JPA
//
// Responsabilidades:
// - Disponibilizar acesso aos registros de localização GPS.
// - Integrar a entidade LocalizacaoEstacao ao Spring Data JPA.
// - Permitir operações de persistência no PostgreSQL.
// - Disponibilizar métodos para consulta de localizações.
//
// Os registros são recebidos pelo backend por meio
// das mensagens MQTT enviadas pelo App2.
//
// A camada de serviço utiliza este repositório
// para executar operações no banco de dados.
//
// ============================================================


// Importa a entidade que representa as informações
// de localização GPS armazenadas no PostgreSQL.
import com.example.estacaobackend.entity.LocalizacaoEstacao

// Importa a interface JpaRepository, responsável
// por disponibilizar operações de persistência
// e consulta por meio do Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository


// ============================================================
// REPOSITÓRIO DE LOCALIZAÇÃO DA ESTAÇÃO
// ============================================================
//
// Esta interface representa a camada de acesso
// aos registros de localização GPS.
//
// JpaRepository recebe dois parâmetros genéricos:
//
// 1. LocalizacaoEstacao:
//    Entidade JPA gerenciada pelo repositório.
//
// 2. Long:
//    Tipo da chave primária da entidade.
//
// Ao herdar JpaRepository, a interface disponibiliza
// automaticamente operações como:
//
// - save(): inserir ou atualizar uma localização.
// - findAll(): consultar todos os registros.
// - findById(): consultar uma localização pelo ID.
// - deleteById(): excluir um registro pelo ID.
// - count(): contar os registros armazenados.
//
// O Spring Data JPA fornece a implementação desses
// métodos, sem necessidade de escrevê-los manualmente.
//
// A interface não declara consultas personalizadas.
// ============================================================

interface LocalizacaoEstacaoRepository :
    JpaRepository<LocalizacaoEstacao, Long>
