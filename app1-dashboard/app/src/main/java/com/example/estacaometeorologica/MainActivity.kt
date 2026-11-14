
package com.example.estacaometeorologica

// Bibliotecas Android responsáveis pela criação da Activity
// e pela inicialização da interface com Jetpack Compose.
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

// Componentes utilizados para organizar a interface do dashboard.
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

// Permitem a rolagem vertical da interface.
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// Componentes visuais do Material Design 3.
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

// Recursos do Compose para gerenciar estados e efeitos.
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Permite utilizar componentes Android tradicionais dentro do Compose.
import androidx.compose.ui.viewinterop.AndroidView

// Modelos de dados e cliente Retrofit utilizados na comunicação
// entre o aplicativo Android e o backend da estação.
import com.example.estacaometeorologica.data.LocalizacaoData
import com.example.estacaometeorologica.data.RetrofitClient
import com.example.estacaometeorologica.data.SensorData

// Tema visual personalizado do aplicativo.
import com.example.estacaometeorologica.ui.theme.EstacaoMeteorologicaTheme

// Biblioteca de corrotinas utilizada para controlar o intervalo
// entre as consultas realizadas ao backend.
import kotlinx.coroutines.delay

// Bibliotecas osmdroid utilizadas para carregar mapas,
// trabalhar com coordenadas e posicionar marcadores.
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * MainActivity
 *
 * Activity principal do App1 — Estação Meteorológica IoT.
 *
 * Responsável por inicializar a interface Android,
 * configurar a biblioteca de mapas e apresentar
 * o dashboard desenvolvido com Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    // Método executado durante a criação da Activity.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite que a interface utilize a área disponível
        // da tela, incluindo as regiões próximas às barras do sistema.
        enableEdgeToEdge()

        // Define a identificação do aplicativo utilizada
        // nas requisições realizadas pela biblioteca osmdroid.
        Configuration.getInstance().userAgentValue =
            "EstacaoMeteorologica/1.0"

        // Inicializa a interface utilizando Jetpack Compose.
        setContent {

            // Aplica o tema visual personalizado do aplicativo.
            EstacaoMeteorologicaTheme {

                // Scaffold organiza a estrutura principal da tela
                // e disponibiliza os espaçamentos necessários.
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    // Exibe o dashboard respeitando os espaçamentos
                    // fornecidos pelo Scaffold.
                    Dashboard(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Dashboard
 *
 * Componente principal da interface meteorológica.
 *
 * Responsável por:
 * - Consultar os dados dos sensores pelo backend.
 * - Atualizar as informações automaticamente.
 * - Consultar a localização GPS enviada pelo App2.
 * - Apresentar os valores meteorológicos.
 * - Exibir a posição da estação no mapa.
 *
 * Os dados chegam ao aplicativo por meio da API REST.
 * A leitura física dos sensores é realizada pela ESP32.
 */
@Composable
fun Dashboard(modifier: Modifier = Modifier) {

    // Armazena a leitura meteorológica mais recente.
    // O Compose atualiza a interface quando esse estado muda.
    var dados by remember {
        mutableStateOf<SensorData?>(null)
    }

    // Armazena a localização GPS mais recente
    // recebida do backend.
    var localizacao by remember {
        mutableStateOf<LocalizacaoData?>(null)
    }

    // Armazena a mensagem que informa ao usuário
    // a situação da comunicação com o servidor.
    var status by remember {
        mutableStateOf("Conectando...")
    }

    // Atualização dos sensores.
    // LaunchedEffect executa uma corrotina enquanto
    // este componente permanece na composição.
    LaunchedEffect(Unit) {
        while (true) {
            try {

                // Consulta a última leitura meteorológica
                // disponibilizada pela API REST do backend.
                val resposta = RetrofitClient.api.obterDados()

                // Verifica se a requisição HTTP foi concluída
                // com sucesso e se existe um corpo de resposta.
                if (resposta.isSuccessful && resposta.body() != null) {

                    // Atualiza os dados utilizados pelo dashboard.
                    dados = resposta.body()

                    // Informa que a consulta foi realizada com sucesso.
                    status = "● Sistema online"
                } else {

                    // Mensagem exibida quando a consulta não retorna
                    // uma resposta HTTP válida com os dados esperados.
                    status = "● Erro ao consultar o servidor"
                }

            } catch (e: Exception) {

                // Exibe informações da exceção caso ocorra
                // uma falha durante a comunicação.
                status = "Erro: ${e.javaClass.simpleName}: ${e.message}"
            }

            // Aguarda 4 segundos antes da próxima consulta.
            // delay suspende a corrotina sem bloquear a interface.
            delay(4000)
        }
    }

    // Atualização da localização GPS.
    // Esta rotina é independente da consulta dos sensores.
    LaunchedEffect(Unit) {
        while (true) {
            try {

                // Consulta a última localização disponibilizada
                // pelo backend, originalmente enviada pelo App2.
                val resposta =
                    RetrofitClient.api.obterLocalizacao()

                // Verifica se a requisição foi bem-sucedida.
                if (resposta.isSuccessful) {

                    // Atualiza o estado somente quando
                    // o servidor retorna dados de localização.
                    resposta.body()?.let {
                        localizacao = it
                    }
                }

            } catch (e: Exception) {

                // Preserva a última localização válida.
                // Uma falha temporária não apaga o marcador anterior.
            }

            // Realiza uma nova consulta GPS a cada 4 segundos.
            delay(4000)
        }
    }

    // Organiza os elementos do dashboard verticalmente.
    // A rolagem permite acessar todas as informações
    // mesmo em dispositivos com telas menores.
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {

        // Título principal do aplicativo.
        Text(
            text = "🌤 Estação Meteorológica",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Exibe a situação atual da comunicação com o backend.
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Temperatura ambiente medida pelo sensor DHT22.
        // O valor é apresentado em graus Celsius.
        SensorCard(
            titulo = "🌡 Temperatura",
            valor = dados?.temperatura?.let {
                "%.1f °C".format(it)
            } ?: "--"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Organiza umidade e luminosidade lado a lado.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Umidade relativa do ar obtida pelo DHT22.
            SensorCard(
                titulo = "💧 Umidade",
                valor = dados?.umidade?.let {
                    "%.1f %%".format(it)
                } ?: "--",
                modifier = Modifier.weight(1f)
            )

            // Leitura de luminosidade obtida pelo sensor LDR
            // conectado à entrada analógica da ESP32.
            SensorCard(
                titulo = "☀ Luminosidade",
                valor = dados?.luminosidade?.toString() ?: "--",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Organiza pressão e qualidade do ar lado a lado.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Pressão atmosférica medida pelo BMP280.
            // A unidade é recebida do backend, com hPa
            // utilizado como valor padrão de apresentação.
            SensorCard(
                titulo = "🌬 Pressão",
                valor = dados?.pressao?.let {
                    "%.2f %s".format(
                        it,
                        dados?.unidadePressao ?: "hPa"
                    )
                } ?: "--",
                modifier = Modifier.weight(1f)
            )

            // Índice experimental calculado a partir das
            // leituras analógicas do sensor MQ-135.
            //
            // O valor não representa uma concentração
            // calibrada de gases em PPM.
            SensorCard(
                titulo = "🫁 Qualidade do ar",
                valor = dados?.qualidadeAr?.toString() ?: "--",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Exibe o valor de chuva fornecido pelo sistema.
        // Neste projeto, a pluviometria é simulada
        // pelo firmware da estação meteorológica.
        SensorCard(
            titulo = "🌧 Chuva",
            valor = dados?.chuva?.toString() ?: "--"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Informa se o aplicativo já recebeu alguma
        // leitura meteorológica do backend.
        Text(
            text = if (dados != null) {
                "Dados atualizados automaticamente"
            } else {
                "Aguardando dados da estação..."
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Identifica a seção de localização geográfica.
        Text(
            text = "📍 Localização da estação",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Obtém as coordenadas da última localização recebida.
        val latitude = localizacao?.latitude
        val longitude = localizacao?.longitude

        // O mapa somente é apresentado quando existem
        // coordenadas válidas de latitude e longitude.
        if (latitude != null && longitude != null) {

            // Contêiner visual responsável por apresentar o mapa.
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {

                // Exibe o mapa centralizado na posição
                // informada pelo App2 através do backend.
                MapaEstacao(
                    latitude = latitude,
                    longitude = longitude
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Créditos dos fornecedores dos dados cartográficos.
            Text(
                text = "© MapTiler | © OpenStreetMap contributors",
                style = MaterialTheme.typography.bodySmall
            )

        } else {

            // Caso não existam coordenadas disponíveis,
            // apresenta uma mensagem em vez do mapa.
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Aguardando localização GPS...")
                }
            }
        }
    }
}

/**
 * MapaEstacao
 *
 * Componente responsável por exibir o mapa da estação.
 *
 * Utiliza a biblioteca osmdroid integrada ao Jetpack Compose
 * por meio do componente AndroidView.
 *
 * As coordenadas recebidas são utilizadas para centralizar
 * o mapa e posicionar um marcador geográfico.
 */
@Composable
fun MapaEstacao(
    latitude: Double,
    longitude: Double
) {

    // Obtém o contexto Android necessário para criar o mapa.
    val context = LocalContext.current

    // remember mantém a instância do MapView durante
    // as recomposições do componente.
    val mapView = remember {
        MapView(context).apply {

            // Habilita gestos de interação, como zoom por toque.
            setMultiTouchControls(true)

            // Permite carregar imagens do mapa pela internet.
            setUseDataConnection(true)

            // Insira aqui sua chave de API do MapTiler.
            // Obtenha gratuitamente em: https://www.maptiler.com/
            val chaveMapTiler = "COLOQUE_SUA_CHAVE_MAPTILER_AQUI"

            // Configura a origem das imagens cartográficas.
            // Cada imagem corresponde a uma parte do mapa (tile).
            val fonteMapTiler = object : XYTileSource(
                "MapTiler",
                0,
                22,
                256,
                ".png",
                arrayOf(
                    "https://api.maptiler.com/maps/streets-v2/256/"
                )
            ) {

                // Acrescenta a chave MapTiler ao endereço
                // utilizado para solicitar cada imagem do mapa.
                override fun getTileURLString(
                    pMapTileIndex: Long
                ): String {
                    return super.getTileURLString(pMapTileIndex) +
                            "?key=$chaveMapTiler"
                }
            }

            // Define o MapTiler como fonte de imagens do mapa.
            setTileSource(fonteMapTiler)

            // Configura o nível inicial de aproximação.
            controller.setZoom(16.0)
        }
    }

    // Guarda a última latitude utilizada para posicionar o mapa.
    var ultimaLatitude by remember {
        mutableStateOf<Double?>(null)
    }

    // Guarda a última longitude utilizada para posicionar o mapa.
    var ultimaLongitude by remember {
        mutableStateOf<Double?>(null)
    }

    // Integra o MapView tradicional à interface Jetpack Compose.
    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize(),
        update = { map ->

            // Verifica se a posição GPS mudou em relação
            // às coordenadas utilizadas anteriormente.
            val coordenadaMudou =
                ultimaLatitude != latitude ||
                        ultimaLongitude != longitude

            if (coordenadaMudou) {

                // Cria um ponto geográfico com as coordenadas atuais.
                val ponto = GeoPoint(latitude, longitude)

                // Centraliza o mapa na posição recebida.
                map.controller.setCenter(ponto)

                // Remove os marcadores antigos para evitar
                // múltiplos indicadores da mesma estação.
                map.overlays.removeAll {
                    it is Marker
                }

                // Cria o marcador que representa a estação
                // na posição geográfica informada pelo App2.
                val marcador = Marker(map).apply {
                    position = ponto
                    title = "Estação Meteorológica"
                    snippet = "Localização GPS"
                }

                // Adiciona o novo marcador ao mapa.
                map.overlays.add(marcador)

                // Armazena as coordenadas utilizadas.
                ultimaLatitude = latitude
                ultimaLongitude = longitude

                // Solicita a atualização visual do mapa.
                map.invalidate()
            }
        }
    )

    // Executa a liberação dos recursos associados ao mapa
    // quando o componente deixa a composição.
    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }
}

/**
 * SensorCard
 *
 * Componente visual reutilizável para apresentar
 * o nome e o valor de uma informação meteorológica.
 *
 * Recebe:
 * - titulo: identificação do sensor ou informação.
 * - valor: dado formatado para apresentação.
 * - modifier: configurações de tamanho e posicionamento.
 *
 * A reutilização desse componente mantém o padrão
 * visual dos cartões apresentados no dashboard.
 */
@Composable
fun SensorCard(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {

    // Cria um cartão visual utilizando Material Design 3.
    Card(
        modifier = modifier.fillMaxWidth()
    ) {

        // Organiza o título e o valor verticalmente.
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Identificação da informação apresentada.
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Exibe o valor recebido do backend.
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
