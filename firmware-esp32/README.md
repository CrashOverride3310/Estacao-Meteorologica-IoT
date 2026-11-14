
# Firmware ESP32 — Estação Meteorológica IoT

**MicroPython | ESP32 | Sensores ambientais | MQTT**

Firmware embarcado da Estação Meteorológica IoT, projeto acadêmico desenvolvido no curso de Ciência da Computação da Universidade Paulista (UNIP), em 2026.

O firmware permite coletar informações ambientais, processar leituras analógicas e transmitir os dados por MQTT para um backend Kotlin/Spring Boot.

---

## 1. Sobre o firmware

O firmware é executado em uma placa ESP32 com MicroPython.

Suas principais responsabilidades são:

- Inicializar os sensores conectados à ESP32.
- Estabelecer conexão Wi-Fi.
- Conectar ao broker MQTT HiveMQ Cloud.
- Realizar leituras ambientais.
- Processar o sinal analógico do MQ-135.
- Gerar valores simulados de precipitação.
- Organizar os dados em JSON.
- Publicar mensagens MQTT periodicamente.
- Exibir informações de diagnóstico no terminal.

O sistema utiliza ciclos de aquisição e publicação separados por uma pausa de quatro segundos.

---

## 2. Tecnologias utilizadas

| Tecnologia | Finalidade |
|---|---|
| ESP32 | Microcontrolador |
| MicroPython | Linguagem do firmware |
| DHT22 | Temperatura e umidade |
| BMP280 | Temperatura e pressão atmosférica |
| LDR | Leitura analógica de luminosidade |
| MQ-135 | Sinal analógico experimental |
| ADC | Conversão analógica para digital |
| I2C | Comunicação com o BMP280 |
| Wi-Fi | Conectividade à internet |
| MQTT | Publicação das leituras |
| HiveMQ Cloud | Broker MQTT |
| JSON | Estrutura das mensagens |

O firmware foi desenvolvido e testado com MicroPython 1.23.0 na ESP32.

---

## 3. Estrutura dos arquivos

```text
firmware-esp32/
|
|-- main.py
|-- bmp280.py
|-- umqtt/
|   |-- simple.py
|
|-- README.md
```

### main.py

Programa principal do firmware.

Responsável por configurar os sensores, estabelecer as conexões Wi-Fi e MQTT, adquirir as leituras, organizar os dados e publicá-los.

### bmp280.py

Driver MicroPython responsável pela comunicação I2C com o BMP280.

Implementa leitura de registradores, interpretação dos coeficientes de calibração e cálculos de compensação para temperatura e pressão.

### umqtt/simple.py

Cliente MQTT utilizado pelo firmware.

Implementa conexão TCP, comunicação TLS, autenticação MQTT, publicação de mensagens e desconexão.

A implementação utilizada pelo projeto publica a telemetria com QoS 0 e sem retenção.

---

## 4. Sensores e pinagem

| Sensor | Sinal | ESP32 |
|---|---|---|
| DHT22 | DATA | GPIO4 |
| BMP280 | SDA | GPIO21 |
| BMP280 | SCL | GPIO22 |
| MQ-135 | AO, via divisor resistivo | GPIO34 |
| LDR | AO | GPIO35 |

### Alimentação

| Componente | Alimentação utilizada |
|---|---|
| DHT22 | 3,3 V |
| BMP280 | 3,3 V |
| LDR | 3,3 V |
| MQ-135 | 5 V |

Todos os componentes devem utilizar a referência de GND apropriada ao circuito.

A ESP32 não deve receber diretamente sinais analógicos acima dos limites elétricos de suas entradas.

### Circuito do MQ-135

O MQ-135 utiliza um módulo com saída analógica AO.

Para conectar essa saída ao GPIO34, foi implementado um divisor resistivo:

```text
MQ-135 AO
    |
    |
  20 kOhm
    |
    +---------- GPIO34 (ESP32)
    |
  10 kOhm
    |
   GND
```

O resistor de 20 kΩ conecta a saída AO à junção de medição.

O resistor de 10 kΩ conecta a junção ao GND.

A junção é conectada ao GPIO34 da ESP32.

O divisor reduz a tensão aplicada à entrada analógica, respeitando o projeto elétrico utilizado.

Antes de reproduzir a montagem, recomenda-se conferir as tensões com um multímetro e verificar as especificações dos componentes.

---

## 5. Aquisição dos sensores

### DHT22

O DHT22 fornece:

- Temperatura ambiente em graus Celsius.
- Umidade relativa do ar em porcentagem.

O firmware realiza a leitura utilizando a biblioteca `dht` do MicroPython.

### BMP280

O BMP280 fornece:

- Temperatura em graus Celsius.
- Pressão atmosférica em hectopascais (hPa).

A comunicação utiliza I2C, com SDA no GPIO21 e SCL no GPIO22.

O driver verifica os endereços I2C 0x76 e 0x77 e utiliza os coeficientes internos do sensor para compensar as leituras.

### LDR

O LDR permite acompanhar variações de luminosidade por meio do ADC conectado ao GPIO35.

O firmware utiliza atenuação ADC de 11 dB.

Os valores representam leituras analógicas digitais, sem calibração para lux.

### MQ-135

O MQ-135 é conectado ao GPIO34 por meio do divisor resistivo.

O firmware utiliza a configuração ADC de 0 dB, adotada após os testes do circuito.

O processamento realiza dez leituras consecutivas, com pausas de 10 milissegundos entre amostras.

Em seguida, calcula a média inteira:

```python
adc_bruto = total // quantidade_leituras
```

O índice experimental é calculado pela expressão:

```python
indice = (adc_bruto * 100) // 500
indice = min(indice, 100)
```

São publicados dois campos:

- `mq135_adc`: média digital das leituras analógicas.
- `qualidade_ar`: índice experimental calculado na escala de 0 a 100.

**Importante:** os valores são obtidos de leituras reais do sistema, mas o índice experimental não corresponde a uma concentração de gases calibrada em PPM.

Não foi realizada calibração metrológica para determinar concentrações específicas de gases.

### Precipitação simulada

O protótipo não utiliza pluviômetro físico.

A variável `chuva` é gerada por software:

```python
chuva = random.randint(0, 100)
```

O valor é utilizado para demonstrar a integração de uma variável pluviométrica no sistema.

Não representa precipitação atmosférica realmente medida.

---

## 6. Conectividade Wi-Fi

A ESP32 utiliza Wi-Fi para acessar a internet.

Na montagem testada, a conectividade foi fornecida pelo hotspot do smartphone Samsung Galaxy A23.

O firmware utiliza o modo estação do MicroPython:

```python
wifi = network.WLAN(network.STA_IF)
```

Após estabelecer a conexão, apresenta no terminal o endereço IP atribuído à ESP32.

As credenciais devem ser configuradas antes da execução.

---

## 7. Comunicação MQTT

O firmware utiliza o HiveMQ Cloud para transmitir as leituras ao backend.

| Parâmetro | Configuração |
|---|---|
| Broker | HiveMQ Cloud |
| Porta | 8883 |
| Transporte | MQTT sobre TLS |
| Tópico | `estacao/meteorologica/dados` |
| QoS | 0 |
| Retain | Desativado |

A biblioteca `umqtt/simple.py` implementa a comunicação MQTT utilizada pela ESP32.

O backend Kotlin/Spring Boot recebe as mensagens e as armazena no PostgreSQL.

---

## 8. Formato das mensagens JSON

As mensagens publicadas contêm os valores ambientais coletados ou calculados pelo firmware.

Exemplo ilustrativo da estrutura, sem representar um registro específico de teste:

```json
{
  "temperatura": 27.3,
  "umidade": 82.9,
  "temperatura_bmp": 27.8,
  "pressao": 927.2,
  "unidade_pressao": "hPa",
  "qualidade_ar": 14,
  "mq135_adc": 74,
  "luminosidade": 2723,
  "chuva": 69
}
```

### Descrição dos campos

| Campo | Descrição |
|---|---|
| `temperatura` | Temperatura do DHT22 em °C |
| `umidade` | Umidade relativa em % |
| `temperatura_bmp` | Temperatura do BMP280 em °C |
| `pressao` | Pressão atmosférica |
| `unidade_pressao` | Unidade da pressão, hPa |
| `qualidade_ar` | Índice experimental do MQ-135 |
| `mq135_adc` | Média das amostras ADC do MQ-135 |
| `luminosidade` | Valor analógico do LDR |
| `chuva` | Valor simulado de precipitação |

O JSON é preparado utilizando `json.dumps()` e enviado ao tópico MQTT configurado.

---

## 9. Como configurar o firmware

### Requisitos

- ESP32 compatível com MicroPython.
- Firmware MicroPython instalado na placa.
- Sensores conectados conforme o circuito.
- Rede Wi-Fi com acesso à internet.
- Broker MQTT compatível.
- Ferramenta para transferir arquivos para a ESP32.

### Preparação

1. Clone o repositório principal.
2. Acesse a pasta `firmware-esp32`.
3. Verifique os arquivos `main.py`, `bmp280.py` e `umqtt/simple.py`.
4. Configure as credenciais Wi-Fi e MQTT.
5. Confira a alimentação e as conexões dos sensores.
6. Transfira os arquivos necessários para o sistema de arquivos da ESP32.
7. Execute o firmware e acompanhe o terminal serial.

### Configuração das credenciais

No arquivo `main.py`, configure os seguintes campos:

```python
WIFI_SSID = "NOME_DA_REDE"
WIFI_PASS = "SENHA_DO_WIFI"

MQTT_BROKER = "SEU_BROKER_MQTT"
MQTT_PORT = 8883

MQTT_USER = "USUARIO_MQTT"
MQTT_PASS = "SENHA_MQTT"

MQTT_CLIENT_ID = "placa_meteorologica_esp32"
MQTT_TOPIC = b"estacao/meteorologica/dados"
```

Os valores apresentados são exemplos de configuração.

A versão publicada não contém as senhas utilizadas na operação do protótipo.

Não publique credenciais reais em commits ou arquivos públicos.

### Transferência dos arquivos

Uma opção para transferir os arquivos é utilizar o `mpremote`.

Exemplo para Windows, supondo a placa conectada à porta COM5:

```powershell
mpremote connect COM5 fs cp firmware-esp32/main.py :main.py
mpremote connect COM5 fs cp firmware-esp32/bmp280.py :bmp280.py
```

O módulo MQTT deve ser transferido preservando sua estrutura de pacote `umqtt/simple.py`.

O arquivo `main.py` utiliza o mecanismo normal de inicialização do MicroPython, quando armazenado na raiz do sistema de arquivos da placa.

Também é possível executar o programa diretamente durante os testes, sem depender da inicialização automática.

**Atenção:** antes de transferir a versão pública para uma placa em operação, configure as credenciais corretas. Não substitua o firmware operacional por uma versão contendo marcadores.

---

## 10. Testes realizados

O firmware foi utilizado em testes físicos da Estação Meteorológica IoT.

| Componente | Verificação |
|---|---|
| DHT22 | Leituras de temperatura e umidade |
| BMP280 | Identificação I2C e leituras compensadas |
| LDR | Variação do sinal analógico |
| MQ-135 | Leitura ADC e cálculo do índice experimental |
| Wi-Fi | Conexão com a rede |
| MQTT | Publicação de mensagens no HiveMQ Cloud |
| JSON | Organização das variáveis ambientais |
| Backend | Recebimento e armazenamento das mensagens |
| App1 | Exibição das leituras publicadas |

O firmware também foi executado durante testes integrados com o backend e os aplicativos Android.

Esses testes validaram a integração funcional do protótipo, mas não representam certificação ou calibração metrológica dos sensores.

---

## 11. Tratamento de erros

O loop principal utiliza um bloco `try/except`.

Caso ocorra uma exceção durante o ciclo, o firmware apresenta uma mensagem no terminal e aguarda quatro segundos antes da próxima tentativa.

A implementação atual não possui uma rotina completa de recuperação automática de todas as falhas de rede ou de reconexão MQTT.

Essa é uma possível melhoria futura.

---

## 12. Limitações

- Montagem eletrônica em protoboard.
- Ausência de gabinete com proteção ambiental.
- MQ-135 sem calibração em PPM.
- Luminosidade sem calibração em lux.
- Precipitação simulada por software.
- Dependência de Wi-Fi e do broker MQTT.
- Necessidade de conferência das tensões e da alimentação dos sensores.

O protótipo possui finalidade acadêmica e experimental.

---

## 13. Melhorias futuras

Possíveis evoluções incluem:

- Desenvolvimento de placa PCB.
- Utilização de gabinete com proteção ambiental.
- Implementação de pluviômetro físico.
- Calibração dos sensores analógicos.
- Monitoramento e tratamento de desconexões.
- Estratégias de reconexão MQTT.
- Armazenamento temporário em caso de falha de rede.
- Ampliação dos testes de estabilidade e precisão.

---

## 14. Projeto principal

Este firmware integra o projeto:

[Estação Meteorológica IoT — ESP32](../README.md)

Componentes relacionados:

- [App1 — Dashboard Android](../app1-dashboard/README.md)
- [App2 — Localização GPS](../app2-localizacao/README.md)
- [Backend Kotlin/Spring Boot](../backend/README.md)

---

Projeto acadêmico — Ciência da Computação, Universidade Paulista (UNIP), 2026.
