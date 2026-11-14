
package com.example.estacaobackend.repository

// ============================================================
// ESTAÇÃO METEOROLÓGICA IoT — BACKEND
// ============================================================
//
// Arquivo: LeituraMeteorologicaRepository.kt
// Linguagem: Kotlin
// Framework: Spring Boot / Spring Data JPA
//
// Responsabilidades:
// - Fornecer acesso aos registros meteorológicos.
// - Integrar a entidade LeituraMeteorologica ao JPA.
// - Permitir operações de persistência no PostgreSQL.
// - Disponibilizar consultas e manipulação de registros.
//
// A interface utiliza os métodos oferecidos
// automaticamente pelo Spring Data JPA.
//
// ============================================================


// Importa a entidade que representa os registros
// meteorológicos armazenados no banco de dados.
import com.example.estacaobackend.entity.LeituraMeteorologica

// Importa a interface JpaRepository do Spring Data,
// responsável por disponibilizar operações de banco.
import org.springframework.data.jpa.repository.JpaRepository


// ============================================================
// REPOSITÓRIO DE LEITURAS METEOROLÓGICAS
// ============================================================
//
// Esta interface representa a camada de acesso a dados.
//
// JpaRepository recebe dois parâmetros genéricos:
//
// 1. LeituraMeteorologica:
//    Entidade JPA gerenciada pelo repositório.
//
// 2. Long:
//    Tipo do identificador (chave primária) da entidade.
//
// Por meio da herança, o Spring Data JPA disponibiliza
// automaticamente operações como:
//
// - save(): inserir ou atualizar registros.
// - findById(): localizar um registro pelo identificador.
// - findAll(): consultar todos os registros.
// - deleteById(): excluir um registro pelo identificador.
// - count(): contar registros armazenados.
//
// Não é necessário implementar manualmente essas
// operações, pois o Spring Data JPA gera a implementação.
// ============================================================

interface LeituraMeteorologicaRepository :
    JpaRepository<LeituraMeteorologica, Long>
