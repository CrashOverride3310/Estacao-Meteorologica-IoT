
package com.example.estacaobackend.config

// ============================================================
// ESTAÇÃO METEOROLÓGICA IoT — BACKEND
// ============================================================
//
// Arquivo: MqttConfig.kt
// Linguagem: Kotlin
// Framework: Spring Boot
// Biblioteca MQTT: Eclipse Paho
//
// Responsabilidades:
// - Configurar a comunicação MQTT do backend.
// - Obter as configurações do broker.
// - Configurar a autenticação do cliente.
// - Estabelecer uma conexão com o HiveMQ Cloud.
// - Habilitar a reconexão automática.
// - Disponibilizar o cliente MQTT aos componentes Spring.
//
// O backend utiliza essa conexão para receber
// mensagens meteorológicas publicadas pela ESP32.
//
// IMPORTANTE:
// As credenciais são obtidas pelas propriedades
// de configuração do Spring Boot.
//
// Nenhuma credencial real está escrita neste arquivo.
// ============================================================


// Importa o cliente MQTT da biblioteca Eclipse Paho.
// Responsável pela comunicação com o broker.
import org.eclipse.paho.client.mqttv3.MqttClient

// Importa a classe utilizada para configurar
// os parâmetros de conexão MQTT.
import org.eclipse.paho.client.mqttv3.MqttConnectOptions

// Permite injetar valores definidos nas
// propriedades de configuração do Spring Boot.
import org.springframework.beans.factory.annotation.Value

// Identifica métodos que disponibilizam
// objetos gerenciados pelo Spring.
import org.springframework.context.annotation.Bean

// Identifica a classe como uma configuração
// utilizada pelo contexto Spring.
import org.springframework.context.annotation.Configuration


// ============================================================
// CONFIGURAÇÃO DA CONEXÃO MQTT
// ============================================================
//
// @Configuration informa ao Spring Boot que esta
// classe contém configurações da aplicação.
//
// Os parâmetros recebidos pelo construtor são
// preenchidos a partir das propriedades:
//
// mqtt.broker-url
// mqtt.client-id
// mqtt.username
// mqtt.password
//
// A anotação @Value realiza a injeção desses valores.
//
// As propriedades são privadas e utilizadas
// internamente para estabelecer a conexão.
// ============================================================

@Configuration
class MqttConfig(

    // Endereço do broker MQTT.
    //
    // Na infraestrutura do projeto, corresponde
    // ao servidor HiveMQ Cloud utilizado pelo backend.
    @Value("\${mqtt.broker-url}")
    private val brokerUrl: String,

    // Identificador do cliente MQTT do backend.
    //
    // Permite identificar esta conexão no broker.
    @Value("\${mqtt.client-id}")
    private val clientId: String,

    // Nome de usuário utilizado na autenticação MQTT.
    //
    // O valor é fornecido pela configuração Spring.
    @Value("\${mqtt.username}")
    private val username: String,

    // Senha utilizada na autenticação MQTT.
    //
    // O valor é fornecido pela configuração Spring.
    // Não existe senha fixa escrita no código.
    @Value("\${mqtt.password}")
    private val password: String
) {

    // ========================================================
    // CRIAÇÃO DO CLIENTE MQTT
    // ========================================================
    //
    // @Bean indica que o objeto retornado por este método
    // será gerenciado pelo contexto do Spring Boot.
    //
    // O cliente MQTT poderá ser utilizado por outros
    // componentes da aplicação por meio da injeção
    // de dependências.
    //
    // Etapas:
    // 1. Cria uma instância do cliente Eclipse Paho.
    // 2. Configura autenticação e opções de conexão.
    // 3. Estabelece a conexão com o broker.
    // 4. Retorna o cliente MQTT conectado.
    // ========================================================

    @Bean
    fun mqttClient(): MqttClient {

        // Cria o cliente MQTT utilizando o endereço
        // do broker e o identificador configurado.
        val client = MqttClient(brokerUrl, clientId)

        // ====================================================
        // OPÇÕES DE CONEXÃO MQTT
        // ====================================================
        //
        // MqttConnectOptions permite configurar
        // o comportamento da conexão com o broker.
        //
        // O bloco apply inicializa as propriedades
        // do objeto e retorna a própria instância.
        // ====================================================

        val options = MqttConnectOptions().apply {

            // Define o usuário utilizado na autenticação.
            userName = username

            // Converte a senha para um array de caracteres,
            // conforme o formato esperado pelo Eclipse Paho.
            //
            // this@MqttConfig identifica a propriedade
            // password pertencente à classe MqttConfig.
            password = this@MqttConfig.password.toCharArray()

            // Habilita a tentativa automática de reconexão
            // quando a comunicação com o broker é perdida.
            isAutomaticReconnect = true

            // Habilita uma sessão MQTT limpa na conexão.
            //
            // A sessão anterior não é reutilizada
            // como uma sessão persistente.
            isCleanSession = true

            // Define o tempo máximo de espera para
            // estabelecer uma conexão MQTT.
            //
            // Unidade: segundos.
            connectionTimeout = 30

            // Define o intervalo de keepalive.
            //
            // O protocolo MQTT utiliza esse intervalo
            // para monitorar a atividade da conexão.
            //
            // Unidade: segundos.
            keepAliveInterval = 60
        }

        // ====================================================
        // ESTABELECIMENTO DA CONEXÃO
        // ====================================================
        //
        // Realiza a conexão com o HiveMQ Cloud
        // utilizando as configurações definidas.
        //
        // Caso a conexão falhe, a biblioteca poderá
        // lançar uma exceção durante esta operação.
        // ====================================================

        client.connect(options)

        // Retorna o cliente MQTT conectado.
        //
        // O Spring poderá disponibilizar este objeto
        // aos componentes que dependem de MqttClient.
        return client
    }
}
