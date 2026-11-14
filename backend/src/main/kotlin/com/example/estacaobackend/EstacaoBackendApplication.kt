
package com.example.estacaobackend

// ============================================================
// ESTAÇÃO METEOROLÓGICA IoT — BACKEND
// ============================================================
//
// Arquivo: EstacaoBackendApplication.kt
// Linguagem: Kotlin
// Framework: Spring Boot
//
// Responsabilidades:
// - Definir o ponto de entrada da aplicação.
// - Inicializar o ambiente Spring Boot.
// - Habilitar a configuração automática do framework.
// - Permitir a inicialização dos componentes do backend.
//
// O backend integra a comunicação MQTT com o HiveMQ Cloud,
// o armazenamento das leituras no PostgreSQL e as APIs REST
// consumidas pelo aplicativo Android (App1).
// ============================================================


// Importa a anotação responsável pela configuração
// automática e identificação da aplicação Spring Boot.
import org.springframework.boot.autoconfigure.SpringBootApplication

// Importa a função Kotlin utilizada para inicializar
// a aplicação Spring Boot.
import org.springframework.boot.runApplication


// ============================================================
// CLASSE PRINCIPAL DA APLICAÇÃO
// ============================================================
//
// @SpringBootApplication combina funcionalidades
// essenciais do Spring Boot, incluindo:
//
// - Configuração automática da aplicação.
// - Registro de configurações Spring.
// - Busca de componentes nos pacotes da aplicação.
//
// A classe funciona como referência principal
// para inicialização do contexto Spring.
// ============================================================

@SpringBootApplication
class EstacaoBackendApplication


// ============================================================
// FUNÇÃO MAIN
// ============================================================
//
// Ponto de entrada executado quando o backend é iniciado.
//
// O parâmetro args recebe eventuais argumentos
// informados durante a execução da aplicação.
//
// runApplication inicializa o contexto Spring Boot,
// carregando os componentes e configurações disponíveis.
//
// O operador * encaminha os argumentos recebidos
// para a função runApplication.
// ============================================================

fun main(args: Array<String>) {
	runApplication<EstacaoBackendApplication>(*args)
}
