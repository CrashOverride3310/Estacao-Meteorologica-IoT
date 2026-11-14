
# =========================================================
# ESTAÇÃO METEOROLÓGICA IoT — ESP32
# =========================================================
#
# Firmware desenvolvido em MicroPython para a placa ESP32.
#
# Responsável por:
# - Conectar a ESP32 à rede Wi-Fi.
# - Realizar leituras dos sensores meteorológicos.
# - Processar o sinal analógico do MQ-135.
# - Gerar valores simulados de precipitação.
# - Organizar as informações em formato JSON.
# - Publicar os dados no HiveMQ Cloud via MQTT.
#
# Os dados enviados são recebidos pelo backend
# Kotlin/Spring Boot e disponibilizados ao App1.
#
# Intervalo configurado entre publicações: 4 segundos.
# =========================================================


# Biblioteca utilizada para configurar a conexão Wi-Fi.
import network

# Biblioteca de controle de tempo e pausas.
import time

# Biblioteca responsável pela conversão dos dados para JSON.
import json

# Biblioteca utilizada para gerar valores aleatórios
# referentes à simulação de precipitação.
import random

# Biblioteca MicroPython para comunicação com o DHT22.
import dht

# Recursos de hardware da ESP32:
# Pin: controle dos pinos GPIO.
# ADC: conversão analógica para digital.
# I2C: comunicação com o sensor BMP280.
from machine import Pin, ADC, I2C

# Cliente MQTT utilizado para publicar os dados no broker.
from umqtt.simple import MQTTClient

# Biblioteca de comunicação com o sensor BMP280.
import bmp280


# =========================================================
# CONFIGURAÇÕES DO WI-FI
# =========================================================
#
# Define o nome da rede e a senha utilizada pela ESP32.
#
# Na operação original, a ESP32 pode utilizar o hotspot
# disponibilizado pelo smartphone Samsung Galaxy A23.
#
# Os valores reais foram substituídos por marcadores
# para evitar a publicação de credenciais no GitHub.
# =========================================================

WIFI_SSID = "xxxxxx"
WIFI_PASS = "xxxxxx"


# =========================================================
# CONFIGURAÇÕES DO HIVEMQ CLOUD
# =========================================================
#
# O HiveMQ Cloud funciona como broker MQTT.
#
# A ESP32 atua como publicadora das leituras dos sensores.
# O backend recebe as mensagens do tópico configurado.
#
# A conexão utiliza SSL/TLS.
#
# IMPORTANTE:
# Substituir os marcadores pelos valores reais para
# executar o firmware em um ambiente configurado.
# =========================================================

# Endereço do broker MQTT.
MQTT_BROKER = "10947940580e4620936f6b4d7e02055c.s1.eu.hivemq.cloud"

# Porta de comunicação MQTT.
# Substituir pelo número da porta configurada no broker.
MQTT_PORT = 8883

# Credenciais utilizadas para autenticação no broker.
MQTT_USER = "COLOQUE_SEU_USUARIO_MQTT"
MQTT_PASS = "COLOQUE_SUA_SENHA_MQTT"

# Identificador da ESP32 na conexão MQTT.
MQTT_CLIENT_ID = "placa_meteorologica_esp32"

# Tópico MQTT utilizado para publicar as leituras.
#
# O prefixo b indica uma sequência de bytes,
# formato aceito pelo cliente MQTT do MicroPython.
MQTT_TOPIC = b"estacao/meteorologica/dados"


# =========================================================
# PINOS DOS SENSORES
# =========================================================
#
# Define os pinos GPIO utilizados para comunicação
# entre a ESP32 e os sensores físicos do projeto.
# =========================================================


# -------------------------
# DHT22
# -------------------------
#
# Sensor digital responsável pela leitura de:
# - Temperatura ambiente, em graus Celsius.
# - Umidade relativa do ar, em porcentagem.
#
# Utiliza o GPIO4 da ESP32.
# -------------------------

DHT_PIN = 4

# Inicializa o sensor DHT22 conectado ao GPIO4.
sensor_dht = dht.DHT22(
    Pin(DHT_PIN)
)


# -------------------------
# BMP280
# -------------------------
#
# Sensor digital responsável pelas leituras de:
# - Temperatura.
# - Pressão atmosférica.
#
# A comunicação é realizada pelo barramento I2C.
#
# SDA: GPIO21 — linha de dados.
# SCL: GPIO22 — linha de clock.
# -------------------------

BMP_SDA = 21
BMP_SCL = 22

# Inicializa o barramento I2C de número 0.
#
# A frequência configurada é de 400000 Hz,
# equivalente a 400 kHz.
i2c = I2C(
    0,
    scl=Pin(BMP_SCL),
    sda=Pin(BMP_SDA),
    freq=400000
)

# Cria a instância do BMP280 utilizando
# o barramento I2C configurado.
sensor_bmp = bmp280.BMP280(i2c)


# -------------------------
# MQ-135
# -------------------------
#
# Sensor utilizado para obter um sinal analógico
# experimental relacionado à resposta do MQ-135.
#
# A saída AO do módulo é conectada ao GPIO34
# por meio do circuito divisor de tensão.
#
# O valor é lido pelo conversor ADC da ESP32.
#
# O índice calculado neste firmware não representa
# uma concentração de gases calibrada em PPM.
# -------------------------

MQ135_PIN = 34

# Inicializa a entrada analógica do MQ-135.
mq135 = ADC(
    Pin(MQ135_PIN)
)

# Configura a atenuação do ADC em 0 dB.
#
# Mantida conforme os testes realizados
# com o sensor e o circuito do projeto.
mq135.atten(
    ADC.ATTN_0DB
)


# -------------------------
# LDR
# -------------------------
#
# Sensor resistivo sensível à luminosidade.
#
# A leitura é realizada pela entrada analógica
# GPIO35 da ESP32.
#
# O resultado corresponde ao valor digital
# produzido pelo ADC, não a uma medida calibrada
# de iluminância em lux.
# -------------------------

LDR_PIN = 35

# Inicializa a leitura analógica do LDR.
ldr = ADC(
    Pin(LDR_PIN)
)

# Configura a atenuação de 11 dB.
#
# Permite trabalhar com uma faixa maior
# de tensão na entrada analógica.
ldr.atten(
    ADC.ATTN_11DB
)


# =========================================================
# LEITURA DO MQ-135
# =========================================================
#
# Função responsável por:
#
# 1. Realizar 10 leituras consecutivas do ADC.
# 2. Calcular a média inteira das amostras.
# 3. Converter a média em um índice experimental.
# 4. Limitar o índice ao valor máximo de 100.
#
# Retorna:
# adc_bruto: média das leituras analógicas.
# indice: índice experimental de 0 a 100.
#
# IMPORTANTE:
# O índice não representa PPM nem concentração
# de gases validada por calibração laboratorial.
# =========================================================

def ler_mq135():

    # Acumulador utilizado para somar as leituras.
    total = 0

    # Quantidade de amostras utilizadas na média.
    quantidade_leituras = 10

    # Realiza as dez leituras analógicas.
    for i in range(quantidade_leituras):

        # Lê o ADC e acumula o valor encontrado.
        total += mq135.read()

        # Aguarda 10 milissegundos entre amostras.
        time.sleep_ms(10)

    # Média das leituras do ADC.
    #
    # A divisão inteira descarta a parte decimal.
    adc_bruto = total // quantidade_leituras

    # Escala experimental de 0 a 100.
    #
    # Aplica uma relação matemática entre o valor
    # médio do ADC e a referência experimental 500.
    indice = (adc_bruto * 100) // 500

    # Limita o resultado ao intervalo máximo de 100.
    indice = min(indice, 100)

    # Retorna o valor médio do ADC e o índice calculado.
    return adc_bruto, indice


# =========================================================
# CONEXÃO WI-FI
# =========================================================
#
# Configura a ESP32 como estação Wi-Fi.
#
# Caso a placa não esteja conectada, utiliza
# as credenciais configuradas anteriormente.
#
# A função aguarda até a conexão ser estabelecida
# e apresenta o endereço IP recebido.
# =========================================================

def conectar_wifi():

    # Cria a interface Wi-Fi em modo estação.
    wifi = network.WLAN(
        network.STA_IF
    )

    # Ativa a interface de rede sem fio.
    wifi.active(True)

    # Verifica se já existe uma conexão ativa.
    if not wifi.isconnected():

        print("Conectando ao Wi-Fi...")

        # Solicita conexão à rede configurada.
        wifi.connect(
            WIFI_SSID,
            WIFI_PASS
        )

        # Aguarda até que a conexão seja estabelecida.
        while not wifi.isconnected():

            # Exibe pontos no terminal enquanto conecta.
            print(
                ".",
                end=""
            )

            # Aguarda um segundo antes de consultar novamente.
            time.sleep(1)

    print()
    print("Wi-Fi conectado!")

    # Exibe o endereço IP atribuído à ESP32.
    print(
        "IP:",
        wifi.ifconfig()[0]
    )


# =========================================================
# CONEXÃO MQTT
# =========================================================
#
# Estabelece comunicação com o broker HiveMQ Cloud.
#
# Utiliza:
# - Identificador do cliente.
# - Endereço do broker.
# - Porta MQTT.
# - Usuário e senha.
# - Conexão SSL/TLS.
#
# Retorna o cliente MQTT conectado para que
# as mensagens sejam publicadas posteriormente.
# =========================================================

def conectar_mqtt():

    print(
        "Conectando ao HiveMQ Cloud..."
    )

    # Inicializa o cliente MQTT com as
    # configurações definidas no firmware.
    cliente = MQTTClient(
        client_id=MQTT_CLIENT_ID,
        server=MQTT_BROKER,
        port=MQTT_PORT,
        user=MQTT_USER,
        password=MQTT_PASS,
        ssl=True
    )

    # Estabelece a conexão com o broker.
    cliente.connect()

    print(
        "MQTT conectado!"
    )

    # Retorna o cliente conectado.
    return cliente


# =========================================================
# INICIALIZAÇÃO
# =========================================================
#
# Apresenta a identificação do sistema no terminal
# serial ou no console MicroPython.
# =========================================================

print()
print(
    "=========================================="
)

print(
    "       ESTAÇÃO METEOROLÓGICA IoT"
)

print(
    "                  ESP32"
)

print(
    "=========================================="
)

print()


# =========================================================
# CONECTA AO WI-FI
# =========================================================
#
# Inicializa a comunicação de rede necessária
# para acessar o broker MQTT pela internet.
# =========================================================

conectar_wifi()


# =========================================================
# CONECTA AO MQTT
# =========================================================
#
# Estabelece a conexão com o HiveMQ Cloud.
# O cliente retornado será utilizado no loop principal.
# =========================================================

mqtt = conectar_mqtt()


print()

print(
    "Sistema iniciado!"
)

print(
    "Publicando dados a cada 4 segundos..."
)

print()


# =========================================================
# LOOP PRINCIPAL
# =========================================================
#
# Executa continuamente as seguintes operações:
#
# 1. Lê temperatura e umidade do DHT22.
# 2. Lê temperatura e pressão do BMP280.
# 3. Obtém a média e o índice experimental do MQ-135.
# 4. Lê o valor analógico do LDR.
# 5. Gera um valor simulado de chuva.
# 6. Organiza os resultados em JSON.
# 7. Publica a mensagem no broker MQTT.
# 8. Exibe os resultados no terminal.
# 9. Aguarda quatro segundos.
#
# As operações são protegidas por try/except,
# permitindo registrar erros durante a execução.
# =========================================================

while True:

    try:

        # =========================================
        # DHT22
        # =========================================
        #
        # Inicia a medição do DHT22 e obtém
        # temperatura e umidade relativa do ar.
        # =========================================

        sensor_dht.measure()

        # Temperatura ambiente, em graus Celsius.
        temperatura = (
            sensor_dht.temperature()
        )

        # Umidade relativa do ar, em porcentagem.
        umidade = (
            sensor_dht.humidity()
        )


        # =========================================
        # BMP280
        # =========================================
        #
        # Obtém as medições realizadas pelo BMP280.
        # A biblioteca utilizada disponibiliza
        # os valores por meio de propriedades.
        # =========================================

        # Temperatura obtida pelo BMP280.
        temperatura_bmp = (
            sensor_bmp.temperature
        )

        # Pressão atmosférica obtida pelo BMP280.
        pressao = (
            sensor_bmp.pressure
        )


        # =========================================
        # MQ-135
        # =========================================
        #
        # Obtém a média das leituras do ADC
        # e o índice experimental calculado.
        # =========================================

        mq135_adc, qualidade_ar = ler_mq135()


        # =========================================
        # LDR
        # =========================================
        #
        # Realiza a leitura analógica da luminosidade
        # por meio do ADC conectado ao GPIO35.
        # =========================================

        luminosidade = (
            ldr.read()
        )


        # =========================================
        # PLUVIÔMETRO SIMULADO
        # =========================================
        #
        # A estação não utiliza um sensor físico
        # de precipitação nesta implementação.
        #
        # O valor de chuva é gerado artificialmente
        # para demonstrar a integração da variável
        # pluviométrica com os demais componentes.
        #
        # O número gerado fica entre 0 e 100.
        # =========================================

        chuva = random.randint(
            0,
            100
        )


        # =========================================
        # MONTA O JSON
        # =========================================
        #
        # Organiza as informações em um dicionário.
        #
        # As chaves identificam cada variável
        # meteorológica enviada ao backend.
        #
        # O campo mq135_adc contém a média real
        # das leituras analógicas do MQ-135.
        #
        # O campo qualidade_ar contém o índice
        # experimental calculado a partir dessa média.
        # =========================================

        dados = {

            "temperatura": temperatura,

            "umidade": umidade,

            "temperatura_bmp": temperatura_bmp,

            "pressao": pressao,

            "unidade_pressao": "hPa",

            "qualidade_ar": qualidade_ar,

            "mq135_adc": mq135_adc,

            "luminosidade": luminosidade,

            "chuva": chuva

        }


        # =========================================
        # CONVERTE PARA JSON
        # =========================================
        #
        # Transforma o dicionário Python em uma
        # mensagem JSON, formato utilizado na
        # comunicação entre os componentes IoT.
        # =========================================

        mensagem = json.dumps(
            dados
        )


        # =========================================
        # PUBLICA NO MQTT
        # =========================================
        #
        # Envia o JSON ao tópico MQTT configurado.
        #
        # O backend inscrito no tópico poderá
        # receber e processar os dados publicados.
        # =========================================

        mqtt.publish(

            MQTT_TOPIC,

            mensagem

        )


        # =========================================
        # MOSTRA NO TERMINAL
        # =========================================
        #
        # Apresenta os valores coletados e calculados.
        #
        # Facilita o acompanhamento das leituras
        # e o diagnóstico durante os testes físicos.
        # =========================================

        print(
            "------------------------------------------"
        )

        print(
            "Dados dos sensores:"
        )

        # Exibe a temperatura ambiente do DHT22.
        print(
            "Temperatura:",
            temperatura,
            "°C"
        )

        # Exibe a umidade relativa do DHT22.
        print(
            "Umidade:",
            umidade,
            "%"
        )

        # Exibe a temperatura obtida pelo BMP280.
        print(
            "Temperatura BMP280:",
            temperatura_bmp,
            "°C"
        )

        # Exibe a pressão atmosférica em hPa.
        print(
            "Pressão:",
            pressao,
            "hPa"
        )

        # Exibe a média das leituras do ADC do MQ-135.
        print(
            "MQ-135 ADC:",
            mq135_adc
        )

        # Exibe o índice experimental do MQ-135.
        print(
            "Sinal MQ-135 (escala experimental 0-100):",
            qualidade_ar
        )

        # Exibe o valor analógico obtido pelo LDR.
        print(
            "LDR:",
            luminosidade
        )

        # Exibe o valor gerado pela simulação de chuva.
        print(
            "Chuva:",
            chuva
        )

        print()

        print(
            "JSON enviado:"
        )

        # Exibe exatamente o JSON publicado no MQTT.
        print(
            mensagem
        )

        print(
            "------------------------------------------"
        )


        # =========================================
        # AGUARDA 4 SEGUNDOS
        # =========================================
        #
        # Controla o intervalo entre os ciclos
        # de leitura e publicação das informações.
        # =========================================

        time.sleep(4)


    except Exception as erro:

        # =========================================
        # TRATAMENTO DE ERROS
        # =========================================
        #
        # Captura exceções durante o ciclo de leitura
        # ou durante a publicação MQTT.
        #
        # Apresenta o erro no terminal e aguarda
        # quatro segundos antes de tentar novamente.
        # =========================================

        print()

        print(
            "ERRO:",
            erro
        )

        print()

        time.sleep(4)
