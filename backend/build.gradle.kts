
/*
 * ============================================================
 * ESTAÇÃO METEOROLÓGICA IoT — BACKEND
 * ============================================================
 *
 * Arquivo: build.gradle.kts
 * Linguagem de configuração: Kotlin DSL
 * Sistema de build: Gradle
 *
 * Responsabilidades:
 * - Configurar Kotlin e Spring Boot.
 * - Definir a versão Java utilizada pelo backend.
 * - Gerenciar as dependências do projeto.
 * - Configurar integração com PostgreSQL.
 * - Configurar comunicação MQTT pelo Eclipse Paho.
 * - Configurar bibliotecas de serialização JSON.
 * - Preparar o ambiente de testes automatizados.
 *
 * Nenhuma versão, dependência ou configuração
 * funcional foi alterada.
 * ============================================================
 */


// ============================================================
// PLUGINS
// ============================================================
//
// Plugins utilizados para configurar o ambiente
// de desenvolvimento e compilação do backend.
//
// Kotlin JVM:
// Permite compilar o código Kotlin para a JVM.
//
// Kotlin Spring:
// Adapta classes Kotlin ao funcionamento do Spring.
//
// Spring Boot:
// Disponibiliza as tarefas e configurações do framework.
//
// Dependency Management:
// Gerencia versões de dependências integradas ao Spring.
//
// Kotlin JPA:
// Auxilia na compatibilidade entre Kotlin e JPA.
// ============================================================

plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.spring") version "2.3.21"
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "2.3.21"
}


// ============================================================
// IDENTIFICAÇÃO DO PROJETO
// ============================================================
//
// group:
// Identifica o grupo ao qual o projeto pertence.
//
// version:
// Define a versão atual do backend.
//
// SNAPSHOT indica uma versão de desenvolvimento.
// ============================================================

group = "com.example"
version = "0.0.1-SNAPSHOT"


// ============================================================
// VERSÃO DO JAVA
// ============================================================
//
// Configura a ferramenta de compilação para Java 21.
//
// O Gradle utiliza uma toolchain compatível
// com a versão especificada.
//
// Essa configuração é importante para manter
// a compatibilidade com o ambiente de execução.
// ============================================================

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}


// ============================================================
// REPOSITÓRIOS
// ============================================================
//
// Define os repositórios utilizados pelo Gradle
// para localizar e baixar as dependências.
//
// Maven Central:
// Repositório público de bibliotecas Java/Kotlin.
// ============================================================

repositories {
	mavenCentral()
}


// ============================================================
// DEPENDÊNCIAS DO BACKEND
// ============================================================
//
// As dependências disponibilizam os componentes
// utilizados pelo Spring Boot e pelas integrações.
//
// implementation:
// Dependências utilizadas na implementação.
//
// runtimeOnly:
// Dependências necessárias durante a execução.
//
// testImplementation:
// Dependências utilizadas durante os testes.
//
// testRuntimeOnly:
// Componentes necessários para executar os testes.
// ============================================================

dependencies {

	// ========================================================
	// SPRING DATA JPA
	// ========================================================
	//
	// Permite o mapeamento de entidades Kotlin
	// e a persistência de dados em bancos relacionais.
	//
	// Utilizado pelas entidades e repositórios
	// da estação meteorológica.
	// ========================================================

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")


	// ========================================================
	// SPRING VALIDATION
	// ========================================================
	//
	// Disponibiliza recursos de validação de dados
	// integrados ao Spring Boot.
	// ========================================================

	implementation("org.springframework.boot:spring-boot-starter-validation")


	// ========================================================
	// SPRING WEB MVC
	// ========================================================
	//
	// Disponibiliza a infraestrutura HTTP utilizada
	// pelos controllers REST do backend.
	//
	// Permite expor os endpoints:
	//
	// GET /api/estacao/dados
	// GET /api/estacao/ultima
	// GET /api/estacao/localizacao
	// ========================================================

	implementation("org.springframework.boot:spring-boot-starter-webmvc")


	// ========================================================
	// KOTLIN REFLECT
	// ========================================================
	//
	// Disponibiliza recursos de reflexão para Kotlin,
	// utilizados por bibliotecas e frameworks.
	// ========================================================

	implementation("org.jetbrains.kotlin:kotlin-reflect")


	// ========================================================
	// JACKSON KOTLIN
	// ========================================================
	//
	// Integra a serialização e desserialização JSON
	// com os recursos da linguagem Kotlin.
	//
	// Utilizado no processamento das mensagens MQTT
	// recebidas pelo MqttListener.
	// ========================================================

	implementation("tools.jackson.module:jackson-module-kotlin")


	// ========================================================
	// DRIVER POSTGRESQL
	// ========================================================
	//
	// Driver JDBC que permite ao Spring Boot
	// estabelecer comunicação com o PostgreSQL.
	//
	// No projeto implantado, o banco é hospedado
	// no serviço Neon PostgreSQL.
	// ========================================================

	runtimeOnly("org.postgresql:postgresql")


	// ========================================================
	// TESTES — SPRING DATA JPA
	// ========================================================
	//
	// Recursos para testar componentes da camada
	// de persistência e repositórios JPA.
	// ========================================================

	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")


	// ========================================================
	// TESTES — SPRING VALIDATION
	// ========================================================
	//
	// Recursos para testar as validações
	// utilizadas na aplicação.
	// ========================================================

	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")


	// ========================================================
	// TESTES — SPRING WEB MVC
	// ========================================================
	//
	// Recursos para testar endpoints e controllers
	// desenvolvidos com Spring Web MVC.
	// ========================================================

	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")


	// ========================================================
	// TESTES — KOTLIN E JUNIT 5
	// ========================================================
	//
	// Integra a linguagem Kotlin ao JUnit 5
	// para a execução de testes automatizados.
	// ========================================================

	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")


	// ========================================================
	// JUNIT PLATFORM LAUNCHER
	// ========================================================
	//
	// Disponibiliza componentes necessários
	// para inicializar a plataforma de testes JUnit.
	// ========================================================

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")


	// ========================================================
	// ECLIPSE PAHO MQTT
	// ========================================================
	//
	// Biblioteca utilizada para comunicação MQTT.
	//
	// Permite:
	// - Conectar o backend ao HiveMQ Cloud.
	// - Autenticar o cliente MQTT.
	// - Inscrever-se nos tópicos meteorológicos e GPS.
	// - Receber mensagens publicadas pelos dispositivos.
	//
	// Versão original: 1.2.5.
	// ========================================================

	implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")


	// ========================================================
	// GOOGLE GSON
	// ========================================================
	//
	// Biblioteca utilizada para trabalhar com dados JSON.
	//
	// Mantida como dependência da implementação original.
	//
	// Versão original: 2.13.1.
	// ========================================================

	implementation("com.google.code.gson:gson:2.13.1")
}


// ============================================================
// CONFIGURAÇÕES DO COMPILADOR KOTLIN
// ============================================================
//
// Configura argumentos adicionais do compilador.
//
// -Xjsr305=strict:
// Aplica um tratamento rigoroso às anotações
// relacionadas à nulidade provenientes do Java.
//
// -Xannotation-default-target=param-property:
// Configura o tratamento dos alvos padrão de
// anotações aplicadas a propriedades Kotlin.
// ============================================================

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll(
			"-Xjsr305=strict",
			"-Xannotation-default-target=param-property"
		)
	}
}


// ============================================================
// CONFIGURAÇÃO ALL-OPEN PARA JPA
// ============================================================
//
// Permite que classes Kotlin com determinadas
// anotações JPA sejam tratadas como abertas.
//
// Esse comportamento facilita a integração
// com mecanismos utilizados por frameworks JPA.
//
// Anotações configuradas:
// - Entity
// - MappedSuperclass
// - Embeddable
// ============================================================

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}


// ============================================================
// CONFIGURAÇÃO DOS TESTES AUTOMATIZADOS
// ============================================================
//
// Configura as tarefas de teste do Gradle
// para utilizar a plataforma JUnit.
//
// Não executa os testes automaticamente;
// apenas determina a plataforma utilizada
// quando a tarefa de testes é iniciada.
// ============================================================

tasks.withType<Test> {
	useJUnitPlatform()
}
