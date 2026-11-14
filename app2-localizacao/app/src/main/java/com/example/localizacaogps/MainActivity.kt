
package com.example.localizacaogps

// Permissões do Android utilizadas para acessar a localização GPS.
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle

// Componentes responsáveis pela Activity e pela interface Jetpack Compose.
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts

// Componentes visuais e gerenciamento de estados do Jetpack Compose.
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Permite verificar se uma permissão já foi concedida.
import androidx.core.content.ContextCompat

// Tema visual personalizado do aplicativo.
import com.example.localizacaogps.ui.theme.LocalizacaoGPSTheme

// Serviços de localização disponibilizados pelo Google Play Services.
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

// Biblioteca Eclipse Paho utilizada para comunicação MQTT.
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

// Biblioteca responsável por fornecer conexões SSL/TLS.
import javax.net.ssl.SSLSocketFactory

/**
 * MainActivity — App2 LocalizacaoGPS
 *
 * Aplicativo Android responsável por obter a localização
 * geográfica do smartphone e publicar as coordenadas no MQTT.
 *
 * No projeto Estação Meteorológica IoT, o aplicativo
 * é utilizado no Samsung Galaxy A23, que permanece
 * próximo à placa ESP32.
 *
 * Funcionamento:
 *
 * 1. O usuário solicita a localização pelo aplicativo.
 * 2. O Android verifica as permissões necessárias.
 * 3. O Google Play Services obtém as coordenadas GPS.
 * 4. O aplicativo estabelece conexão com o HiveMQ Cloud.
 * 5. Latitude, longitude e timestamp são publicados em JSON.
 * 6. O backend recebe os dados e disponibiliza a localização
 *    para o App1 por meio da API REST.
 */
class MainActivity : ComponentActivity() {

    // Estados utilizados para apresentar a latitude,
    // longitude e situação atual do aplicativo.
    //
    // mutableStateOf permite que a interface Compose
    // seja atualizada quando os valores forem modificados.
    private val latitude = mutableStateOf("Aguardando")
    private val longitude = mutableStateOf("Aguardando")
    private val status = mutableStateOf("GPS não iniciado")

    // Endereço do broker MQTT HiveMQ Cloud.
    //
    // O protocolo ssl:// utiliza uma conexão criptografada.
    // A porta 8883 é utilizada para comunicação MQTT com TLS.
    private val brokerUrl =
        "ssl://10947940580e4620936f6b4d7e02055c.s1.eu.hivemq.cloud:8883"

    // Identificador utilizado pelo aplicativo na conexão MQTT.
    private val mqttClientId = "celular_2_gateway"

    // Tópico MQTT no qual as coordenadas GPS são publicadas.
    // O backend utiliza esse tópico para receber a localização.
    private val mqttTopic = "estacao/localizacao"

    // Cliente MQTT responsável pela conexão com o broker.
    // Inicialmente é nulo e será criado quando necessário.
    private var mqttClient: MqttAsyncClient? = null

    /**
     * permissionLauncher
     *
     * Gerencia o resultado da solicitação de permissões
     * de localização realizada ao sistema Android.
     *
     * O aplicativo aceita a permissão de localização
     * precisa ou aproximada.
     */
    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            // Verifica se pelo menos uma das permissões
            // de localização foi autorizada pelo usuário.
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {

                // Inicia a consulta GPS após a autorização.
                getLocation()

            } else {

                // Informa quando o usuário não autoriza
                // o acesso à localização do dispositivo.
                status.value = "Permissão de localização negada"
            }
        }

    // Cliente de localização fornecido pelo Google Play Services.
    //
    // by lazy cria a instância apenas quando ela
    // é utilizada pela primeira vez.
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * onCreate
     *
     * Método executado quando a Activity é criada.
     *
     * Inicializa a interface gráfica do aplicativo,
     * incluindo os campos de coordenadas e o botão
     * responsável pela solicitação da localização.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite a utilização da área da tela próxima
        // às barras do sistema Android.
        enableEdgeToEdge()

        // Inicializa a interface desenvolvida com Jetpack Compose.
        setContent {

            // Aplica o tema visual personalizado do App2.
            LocalizacaoGPSTheme {

                // Scaffold organiza a estrutura principal da tela.
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    // Organiza os componentes verticalmente
                    // e centraliza o conteúdo na tela.
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        // Título principal do aplicativo.
                        Text(
                            text = "Localização GPS",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Exibe o estado atual da operação GPS/MQTT.
                        Text("Status: ${status.value}")

                        Spacer(modifier = Modifier.height(24.dp))

                        // Identifica o campo de latitude.
                        Text("Latitude")

                        // Apresenta a última latitude obtida.
                        Text(
                            text = latitude.value,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Identifica o campo de longitude.
                        Text("Longitude")

                        // Apresenta a última longitude obtida.
                        Text(
                            text = longitude.value,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botão que inicia a solicitação de localização.
                        //
                        // Quando pressionado, verifica as permissões
                        // e inicia uma nova consulta GPS.
                        Button(
                            onClick = { requestLocationPermission() }
                        ) {
                            Text("Obter localização")
                        }
                    }
                }
            }
        }
    }

    /**
     * requestLocationPermission
     *
     * Verifica se o aplicativo possui autorização
     * para acessar a localização do smartphone.
     *
     * Se alguma permissão já estiver concedida,
     * inicia a leitura da localização.
     *
     * Caso contrário, solicita as permissões
     * necessárias ao sistema Android.
     */
    private fun requestLocationPermission() {

        // Verifica a permissão para localização precisa.
        // ACCESS_FINE_LOCATION permite maior precisão.
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Verifica a permissão para localização aproximada.
        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {

            // Caso exista autorização, consulta a localização.
            getLocation()

        } else {

            // Solicita as permissões ao usuário por meio
            // da interface de permissões do Android.
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     * getLocation
     *
     * Obtém a localização atual do smartphone utilizando
     * o Fused Location Provider do Google Play Services.
     *
     * PRIORITY_HIGH_ACCURACY solicita uma localização
     * com alta precisão, quando disponível.
     *
     * Após obter as coordenadas, chama publishLocation
     * para enviá-las ao broker MQTT.
     *
     * A anotação SuppressLint evita o aviso de permissão,
     * pois a verificação já é realizada antes da chamada.
     */
    @SuppressLint("MissingPermission")
    private fun getLocation() {

        // Atualiza a mensagem apresentada ao usuário.
        status.value = "Obtendo localização..."

        // Cria um token que pode ser utilizado
        // para controlar o cancelamento da solicitação.
        val cancellationToken = CancellationTokenSource()

        // Solicita a localização atual ao Google Play Services.
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).addOnSuccessListener { location ->

            // Verifica se foi possível obter coordenadas.
            if (location != null) {

                // Extrai latitude e longitude da localização.
                val lat = location.latitude
                val lon = location.longitude

                // Atualiza os valores exibidos na interface.
                latitude.value = lat.toString()
                longitude.value = lon.toString()

                // Publica as coordenadas no broker MQTT.
                publishLocation(lat, lon)

            } else {

                // Mensagem exibida quando a localização
                // não está disponível no momento da consulta.
                status.value =
                    "Localização indisponível. Verifique o GPS do celular."
            }

        }.addOnFailureListener { exception ->

            // Apresenta a mensagem de erro retornada
            // pelo serviço de localização.
            status.value = "Erro GPS: ${exception.message}"
        }
    }

    /**
     * publishLocation
     *
     * Responsável por estabelecer a conexão MQTT
     * e solicitar a publicação das coordenadas GPS.
     *
     * Se o cliente já estiver conectado, publica
     * diretamente a localização recebida.
     *
     * Caso contrário, configura uma nova conexão
     * segura com o HiveMQ Cloud.
     */
    private fun publishLocation(lat: Double, lon: Double) {
        try {

            // Obtém ou cria a instância do cliente MQTT.
            val client = getMqttClient()

            if (client.isConnected) {

                // Reutiliza a conexão MQTT existente.
                sendMqttMessage(client, lat, lon)

            } else {

                // Informa que a conexão com o broker será iniciada.
                status.value = "Conectando ao MQTT..."

                // Configura as opções de conexão MQTT.
                val options = MqttConnectOptions().apply {

                    // Obtém usuário e senha das configurações
                    // BuildConfig geradas durante a compilação.
                    //
                    // As credenciais não são declaradas
                    // diretamente neste arquivo Kotlin.
                    userName = BuildConfig.MQTT_USERNAME
                    password = BuildConfig.MQTT_PASSWORD.toCharArray()

                    // Solicita uma sessão MQTT limpa.
                    isCleanSession = true

                    // Tempo máximo para estabelecer a conexão.
                    // Unidade: segundos.
                    connectionTimeout = 10

                    // Intervalo utilizado para manter
                    // a conexão MQTT ativa.
                    // Unidade: segundos.
                    keepAliveInterval = 30

                    // Configura uma conexão SSL/TLS.
                    socketFactory = SSLSocketFactory.getDefault()
                }

                // Inicia a conexão MQTT de forma assíncrona.
                //
                // IMqttActionListener recebe o resultado
                // da tentativa de conexão com o broker.
                client.connect(
                    options,
                    null,
                    object : IMqttActionListener {

                        // Executado quando a conexão é estabelecida.
                        override fun onSuccess(asyncActionToken: IMqttToken?) {

                            // Retorna à thread principal do Android
                            // para executar a próxima operação.
                            runOnUiThread {

                                // Publica as coordenadas após conectar.
                                sendMqttMessage(client, lat, lon)
                            }
                        }

                        // Executado quando a conexão MQTT falha.
                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {

                            // Atualiza a interface com a falha recebida.
                            runOnUiThread {
                                status.value =
                                    "Falha na conexão MQTT: ${exception?.message}"
                            }
                        }
                    }
                )
            }

        } catch (exception: Exception) {

            // Trata falhas síncronas durante a configuração
            // ou utilização do cliente MQTT.
            status.value = "Erro MQTT: ${exception.message}"
        }
    }

    /**
     * getMqttClient
     *
     * Retorna a instância do cliente MQTT.
     *
     * Se ainda não existir um cliente,
     * cria uma nova instância do MqttAsyncClient.
     *
     * MemoryPersistence utiliza armazenamento
     * em memória para o estado MQTT mantido pelo cliente.
     */
    private fun getMqttClient(): MqttAsyncClient {

        // Cria o cliente somente quando necessário.
        if (mqttClient == null) {
            mqttClient = MqttAsyncClient(
                brokerUrl,
                mqttClientId,
                MemoryPersistence()
            )
        }

        // Retorna a instância existente do cliente.
        return mqttClient!!
    }

    /**
     * sendMqttMessage
     *
     * Constrói e publica uma mensagem JSON com:
     *
     * - latitude: coordenada geográfica em graus decimais.
     * - longitude: coordenada geográfica em graus decimais.
     * - timestamp: momento da publicação em milissegundos
     *   desde a época Unix.
     *
     * A mensagem é enviada ao tópico estacao/localizacao.
     *
     * O backend recebe essas informações e disponibiliza
     * a última localização ao dashboard do App1.
     */
    private fun sendMqttMessage(
        client: MqttAsyncClient,
        lat: Double,
        lon: Double
    ) {

        // Constrói o conteúdo JSON enviado ao broker MQTT.
        val payload = """
            {
                "latitude": $lat,
                "longitude": $lon,
                "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()

        // Cria a mensagem MQTT utilizando o JSON
        // convertido para uma sequência de bytes.
        val message = MqttMessage(payload.toByteArray()).apply {

            // QoS 1: o protocolo confirma a entrega
            // ao menos uma vez entre cliente e broker.
            qos = 1

            // A mensagem não fica armazenada como
            // mensagem retida no tópico MQTT.
            isRetained = false
        }

        // Publica a mensagem no tópico configurado.
        // A publicação é assíncrona.
        client.publish(
            mqttTopic,
            message,
            null,
            object : IMqttActionListener {

                // Executado quando o cliente recebe a confirmação
                // de sucesso da operação de publicação.
                override fun onSuccess(asyncActionToken: IMqttToken?) {

                    // Atualiza o status apresentado na interface.
                    runOnUiThread {
                        status.value = "Localização publicada no MQTT!"
                    }
                }

                // Executado quando a publicação apresenta falha.
                override fun onFailure(
                    asyncActionToken: IMqttToken?,
                    exception: Throwable?
                ) {

                    // Informa o erro retornado pela biblioteca MQTT.
                    runOnUiThread {
                        status.value =
                            "Falha ao publicar: ${exception?.message}"
                    }
                }
            }
        )
    }

    /**
     * onDestroy
     *
     * Método executado quando a Activity é destruída.
     *
     * Responsável por encerrar a conexão MQTT
     * e liberar os recursos associados ao cliente.
     */
    override fun onDestroy() {

        // Verifica se existe uma instância do cliente MQTT.
        mqttClient?.let { client ->

            try {

                // Solicita desconexão caso o cliente
                // ainda esteja conectado ao broker.
                if (client.isConnected) {
                    client.disconnect()
                }

                // Fecha o cliente MQTT e libera recursos.
                client.close()

            } catch (_: Exception) {

                // Mantém o tratamento original de encerramento,
                // sem executar ações adicionais em caso de falha.
            }
        }

        // Executa o encerramento padrão da Activity Android.
        super.onDestroy()
    }
}
