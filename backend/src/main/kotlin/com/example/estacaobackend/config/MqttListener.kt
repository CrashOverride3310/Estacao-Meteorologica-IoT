
package com.example.estacaobackend.config

// ============================================================
// ESTAÇÃO METEOROLÓGICA IoT — BACKEND
// ============================================================
//
// Arquivo: MqttListener.kt
// Linguagem: Kotlin
// Framework: Spring Boot
// Biblioteca MQTT: Eclipse Paho
//
// Responsabilidades:
// - Inscrever o backend nos tópicos MQTT.
// - Receber as mensagens meteorológicas da ESP32.
// - Receber as mensagens de localização GPS do App2.
// - Converter os dados JSON em entidades Kotlin.
// - Encaminhar as entidades para a camada de serviços.
// - Persistir os registros no PostgreSQL.
// - Encerrar a conexão MQTT ao finalizar a aplicação.
//
// Fluxo dos dados meteorológicos:
//
// ESP32 → HiveMQ Cloud → MqttListener
//       → LeituraMeteorologicaService → PostgreSQL
//
// Fluxo dos dados de localização:
//
// App2 → HiveMQ Cloud → MqttListener
//      → LocalizacaoEstacaoService → PostgreSQL
//
// ============================================================


// Importa a entidade que representa os dados
// meteorológicos recebidos da ESP32.
import com.example.estacaobackend.entity.LeituraMeteorologica

// Importa a entidade responsável por representar
// as coordenadas GPS da estação meteorológica.
import com.example.estacaobackend.entity.LocalizacaoEstacao

// Importa o serviço responsável pela persistência
// das leituras meteorológicas.
import com.example.estacaobackend.service.LeituraMeteorologicaService

// Importa o serviço responsável por salvar
// os dados de localização GPS.
import com.example.estacaobackend.service.LocalizacaoEstacaoService

// Importa o cliente MQTT utilizado para estabelecer
// inscrições e receber mensagens do broker.
import org.eclipse.paho.client.mqttv3.MqttClient

// Representa uma mensagem recebida pelo protocolo MQTT.
import org.eclipse.paho.client.mqttv3.MqttMessage

// Permite obter propriedades da configuração Spring.
import org.springframework.beans.factory.annotation.Value

// Identifica a classe como um componente
// gerenciado pelo contexto Spring Boot.
import org.springframework.stereotype.Component

// Executa um método após a inicialização
// do componente pelo Spring.
import jakarta.annotation.PostConstruct

// Executa um método durante o encerramento
// do ciclo de vida do componente.
import jakarta.annotation.PreDestroy

// Responsável pela conversão entre JSON
// e objetos Kotlin/Java.
import tools.jackson.databind.ObjectMapper


// ============================================================
// COMPONENTE MQTT LISTENER
// ============================================================
//
// @Component permite que o Spring Boot identifique
// e gerencie automaticamente esta classe.
//
// As dependências são recebidas pelo construtor,
// utilizando a injeção de dependências do Spring.
//
// Dependências:
//
// mqttClient:
// Cliente MQTT configurado em MqttConfig.kt.
//
// service:
// Serviço de persistência meteorológica.
//
// localizacaoService:
// Serviço de persistência de localização GPS.
//
// objectMapper:
// Conversor de mensagens JSON para objetos Kotlin.
//
// topic:
// Tópico MQTT das leituras meteorológicas.
//
// locationTopic:
// Tópico MQTT das mensagens GPS.
//
// A propriedade mqtt.location-topic possui um
// valor padrão definido na anotação @Value.
// ============================================================

@Component
class MqttListener(

    // Cliente MQTT compartilhado com o backend.
    private val mqttClient: MqttClient,

    // Serviço utilizado para salvar leituras meteorológicas.
    private val service: LeituraMeteorologicaService,

    // Serviço responsável pela persistência da localização.
    private val localizacaoService: LocalizacaoEstacaoService,

    // Conversor JSON utilizado na desserialização.
    private val objectMapper: ObjectMapper,

    // Tópico de mensagens meteorológicas.
    @Value("\${mqtt.topic}")
    private val topic: String,

    // Tópico de localização GPS.
    //
    // Caso mqtt.location-topic não esteja configurado,
    // utiliza o valor padrão "estacao/localizacao".
    @Value("\${mqtt.location-topic:estacao/localizacao}")
    private val locationTopic: String

) {

    // ========================================================
    // INICIALIZAÇÃO DAS INSCRIÇÕES MQTT
    // ========================================================
    //
    // @PostConstruct indica que este método deve ser
    // executado após a inicialização do componente.
    //
    // O método realiza duas inscrições no broker:
    //
    // 1. Tópico de leituras meteorológicas.
    // 2. Tópico de localização GPS.
    //
    // Cada inscrição utiliza um callback próprio
    // para processar o tipo de mensagem recebido.
    //
    // O processo ocorre quando a aplicação inicializa
    // o componente MqttListener.
    // ========================================================

    @PostConstruct
    fun iniciar() {

        // ----------------------------------------------------
        // INSCRIÇÃO NO TÓPICO METEOROLÓGICO
        // ----------------------------------------------------
        //
        // Inscreve o cliente MQTT no tópico utilizado
        // pela ESP32 para publicar os dados dos sensores.
        //
        // Quando uma mensagem chega, o callback
        // encaminha seu conteúdo para processarMensagem().
        // ----------------------------------------------------

        // Inscrição no tópico de dados meteorológicos
        mqttClient.subscribe(topic) { _, message ->
            processarMensagem(message)
        }

        // Confirma no terminal o tópico registrado.
        println("MQTT inscrito no tópico: $topic")


        // ----------------------------------------------------
        // INSCRIÇÃO NO TÓPICO DE LOCALIZAÇÃO GPS
        // ----------------------------------------------------
        //
        // Inscreve o backend no tópico utilizado pelo App2.
        //
        // As mensagens contêm as informações de localização
        // publicadas pelo smartphone Samsung Galaxy A23.
        //
        // Quando uma mensagem chega, o callback
        // executa processarLocalizacao().
        // ----------------------------------------------------

        // Novo tópico de localização GPS
        mqttClient.subscribe(locationTopic) { _, message ->
            processarLocalizacao(message)
        }

        // Confirma no terminal o tópico GPS registrado.
        println("MQTT inscrito no tópico GPS: $locationTopic")
    }


    // ========================================================
    // PROCESSAMENTO DAS LEITURAS METEOROLÓGICAS
    // ========================================================
    //
    // Recebe uma mensagem MQTT contendo os dados
    // publicados pelo firmware MicroPython da ESP32.
    //
    // Etapas:
    //
    // 1. Obtém os bytes da mensagem MQTT.
    // 2. Converte os bytes para texto UTF-8.
    // 3. Desserializa o JSON para LeituraMeteorologica.
    // 4. Encaminha a entidade para persistência.
    // 5. Registra o resultado no terminal.
    //
    // O tratamento de exceções permite registrar
    // falhas sem propagá-las diretamente ao callback.
    // ========================================================

    private fun processarMensagem(message: MqttMessage) {

        try {

            // Obtém o conteúdo da mensagem MQTT
            // e converte os bytes para texto UTF-8.
            val json = String(message.payload, Charsets.UTF_8)

            // Converte o JSON recebido em uma instância
            // da entidade LeituraMeteorologica.
            //
            // O ObjectMapper utiliza o mapeamento
            // definido na classe da entidade.
            val leitura = objectMapper.readValue(
                json,
                LeituraMeteorologica::class.java
            )

            // Encaminha a entidade para o serviço
            // responsável pela gravação no PostgreSQL.
            service.salvar(leitura)

            // Registra a mensagem processada no terminal.
            println("Leitura meteorológica salva: $json")

        } catch (e: Exception) {

            // Apresenta uma descrição do erro ocorrido.
            println("Erro ao processar mensagem MQTT: ${e.message}")

            // Exibe o rastreamento da exceção,
            // facilitando o diagnóstico técnico.
            e.printStackTrace()
        }
    }


    // ========================================================
    // PROCESSAMENTO DA LOCALIZAÇÃO GPS
    // ========================================================
    //
    // Recebe mensagens MQTT enviadas pelo App2,
    // responsável pela localização da estação.
    //
    // Etapas:
    //
    // 1. Recebe a mensagem MQTT.
    // 2. Converte o conteúdo para texto UTF-8.
    // 3. Desserializa o JSON para LocalizacaoEstacao.
    // 4. Encaminha o objeto para persistência.
    // 5. Registra o resultado no terminal.
    //
    // Os dados são posteriormente disponibilizados
    // ao App1 pelos endpoints REST do backend.
    // ========================================================

    private fun processarLocalizacao(message: MqttMessage) {

        try {

            // Obtém o conteúdo JSON recebido pelo MQTT.
            val json = String(message.payload, Charsets.UTF_8)

            // Converte o JSON em um objeto
            // da entidade LocalizacaoEstacao.
            val localizacao = objectMapper.readValue(
                json,
                LocalizacaoEstacao::class.java
            )

            // Encaminha a localização para o serviço
            // responsável pela persistência dos dados.
            localizacaoService.salvar(localizacao)

            // Registra a localização recebida no terminal.
            println("Localização GPS salva: $json")

        } catch (e: Exception) {

            // Exibe a descrição do erro de processamento.
            println("Erro ao processar localização GPS: ${e.message}")

            // Apresenta o rastreamento da exceção.
            e.printStackTrace()
        }
    }


    // ========================================================
    // ENCERRAMENTO DA CONEXÃO MQTT
    // ========================================================
    //
    // @PreDestroy indica que o método será executado
    // durante a destruição do componente Spring.
    //
    // Caso o cliente MQTT esteja conectado:
    //
    // 1. Remove a inscrição meteorológica.
    // 2. Remove a inscrição de localização GPS.
    // 3. Desconecta o cliente do broker.
    // 4. Fecha os recursos do cliente MQTT.
    //
    // Esse procedimento ajuda a liberar os recursos
    // utilizados pela comunicação MQTT.
    //
    // A desconexão ocorre somente quando:
    // as operações são executadas apenas quando
    // mqttClient.isConnected retorna true.
    // ========================================================

    @PreDestroy
    fun encerrar() {

        // Verifica se o cliente MQTT está conectado.
        if (mqttClient.isConnected) {

            // Cancela a inscrição no tópico meteorológico.
            mqttClient.unsubscribe(topic)

            // Cancela a inscrição no tópico GPS.
            mqttClient.unsubscribe(locationTopic)

            // Encerra a conexão MQTT com o broker.
            mqttClient.disconnect()

            // Fecha os recursos internos do cliente.
            mqttClient.close()
        }
    }
}
