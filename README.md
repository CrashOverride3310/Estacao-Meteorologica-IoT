
# Estação Meteorológica IoT — ESP32

**Projeto de Desenvolvimento II | Universidade Paulista (UNIP) | Ciência da Computação | 2026**

Sistema acadêmico de monitoramento ambiental desenvolvido com ESP32, sensores físicos, comunicação MQTT, backend Kotlin/Spring Boot, PostgreSQL e dois aplicativos Android.

---

## 1. Sobre o projeto

A Estação Meteorológica IoT é um protótipo de monitoramento ambiental que integra hardware, software embarcado, comunicação em nuvem, banco de dados e aplicativos móveis.

O objetivo é demonstrar como tecnologias de Internet das Coisas (IoT) podem contribuir para o acompanhamento de condições ambientais locais, estudos de microclima e aplicações relacionadas ao conceito de cidades inteligentes.

O projeto utiliza uma ESP32 para coletar dados dos sensores, um smartphone para fornecer localização GPS e conectividade Wi-Fi, um backend para processamento e armazenamento das informações e um aplicativo Android para visualização.

O sistema foi desenvolvido e testado como protótipo acadêmico.

---

## 2. Tecnologias utilizadas

| Área | Tecnologias |
|---|---|
| Microcontrolador | ESP32 |
| Sensores | DHT22, BMP280, LDR e MQ-135 |
| Firmware | MicroPython |
| Comunicação | Wi-Fi, MQTT e HiveMQ Cloud |
| Backend | Kotlin, Spring Boot e Java 21 |
| Persistência | Spring Data JPA, Hibernate e PostgreSQL |
| Banco em nuvem | Neon PostgreSQL |
| Infraestrutura | DigitalOcean, Ubuntu Linux |
| Aplicativos | Android, Kotlin e Jetpack Compose |
| Geolocalização | GPS do Samsung Galaxy A23 |
| Ferramentas | Android Studio, VS Code, MQTTX, Git e GitHub |

---

## 3. Arquitetura do sistema

O sistema utiliza duas fontes principais de informações:

1. **ESP32:** coleta os dados dos sensores e publica a telemetria meteorológica.
2. **App2 — LocalizacaoGPS:** obtém a localização GPS do smartphone e publica as coordenadas da estação.

Ambas as fontes utilizam o HiveMQ Cloud, em tópicos MQTT separados.

### Fluxo de comunicação

```text
ESP32 + Sensores
       |
       | Dados meteorológicos
       v
   HiveMQ Cloud <----- App2 (GPS)
       |                   |
       |              Coordenadas
       |              geográficas
       v
Backend Kotlin / Spring Boot
       |
       v
PostgreSQL (Neon)
       |
       v
API REST HTTPS
       |
       v
App1 — Dashboard Android
       |
       v
Sensores + Localização no mapa
```

O backend está implantado em um servidor Ubuntu Linux na DigitalOcean.

O Samsung Galaxy A23, utilizado pelo App2, também fornece conectividade Wi-Fi à ESP32 por meio de hotspot.

A localização GPS é associada à estação porque o smartphone permanece próximo ao dispositivo durante a operação.

---

## 4. Sensores e componentes

| Componente | Função | Conexão ESP32 |
|---|---|---|
| DHT22 | Temperatura e umidade relativa | GPIO4 |
| BMP280 | Temperatura e pressão atmosférica | I2C — GPIO21/22 |
| LDR | Variação da luminosidade | GPIO35 |
| MQ-135 | Leitura analógica experimental | GPIO34 |
| GPS | Latitude e longitude | App2 Android |

### MQ-135

O MQ-135 utiliza sua saída analógica AO conectada ao GPIO34 da ESP32 por meio de um divisor resistivo de 20 kΩ e 10 kΩ.

O firmware realiza a média de 10 amostras do ADC e calcula um índice experimental de 0 a 100.

Esse índice representa o sinal obtido pelo sistema segundo a escala implementada.

**O índice não representa uma concentração calibrada de gases em PPM nem uma medição metrologicamente validada da qualidade do ar.**

### Luminosidade

O LDR fornece um valor analógico obtido pelo ADC da ESP32.

Esse valor não corresponde a uma medição calibrada de iluminância em lux.

### Precipitação

A variável de chuva é simulada por software.

O firmware gera valores aleatórios entre 0 e 100 para demonstrar a integração da informação pluviométrica.

O protótipo não utiliza um pluviômetro físico.

---

## 5. Firmware MicroPython

O firmware executado na ESP32 é responsável por:

1. Inicializar os sensores e a conexão Wi-Fi.
2. Estabelecer comunicação MQTT com o HiveMQ Cloud.
3. Realizar leituras ambientais.
4. Calcular o índice experimental do MQ-135.
5. Gerar o valor simulado de precipitação.
6. Organizar as informações em JSON.
7. Publicar os dados MQTT em ciclos de aproximadamente quatro segundos.
8. Exibir as leituras no terminal serial.

### Arquivos principais

- `firmware-esp32/main.py` — inicialização, leitura dos sensores e publicação MQTT.
- `firmware-esp32/bmp280.py` — driver MicroPython para o sensor BMP280.
- `firmware-esp32/umqtt/simple.py` — cliente de comunicação MQTT.

O firmware foi testado com a ESP32 e os sensores do protótipo.

Para utilizar o código em outra placa, é necessário configurar as credenciais de Wi-Fi e MQTT antes da execução.

**Documentação:** [Firmware ESP32](firmware-esp32/README.md)

---

## 6. Comunicação MQTT

O HiveMQ Cloud atua como broker para a comunicação entre os dispositivos e o backend.

| Tópico MQTT | Publicador | Conteúdo |
|---|---|---|
| `estacao/meteorologica/dados` | ESP32 | Leituras meteorológicas |
| `estacao/localizacao` | App2 | Coordenadas GPS |

O firmware da ESP32 utiliza conexão MQTT com TLS.

O backend utiliza a biblioteca Eclipse Paho para receber e processar as mensagens.

A comunicação foi validada durante os testes com auxílio do MQTTX.

---

## 7. Backend e banco de dados

O backend foi desenvolvido em Kotlin com Spring Boot.

Suas responsabilidades incluem:

- Estabelecer conexão com o broker HiveMQ Cloud.
- Receber as mensagens meteorológicas da ESP32.
- Receber a localização GPS enviada pelo App2.
- Converter mensagens JSON em entidades Kotlin.
- Persistir os registros no PostgreSQL.
- Disponibilizar endpoints HTTP para consulta dos dados.

### API REST

| Método | Endpoint | Finalidade |
|---|---|---|
| GET | `/api/estacao/dados` | Consultar todas as leituras meteorológicas |
| GET | `/api/estacao/ultima` | Consultar a leitura meteorológica mais recente |
| GET | `/api/estacao/localizacao` | Consultar a localização GPS mais recente |

O backend está implantado em um servidor DigitalOcean com Ubuntu Linux, integrado ao PostgreSQL hospedado no Neon.

### Entidades principais

**LeituraMeteorologica**

Representa os registros meteorológicos recebidos pelo backend, incluindo temperatura, umidade, pressão atmosférica, índice experimental do MQ-135, luminosidade, chuva simulada e data/hora.

**LocalizacaoEstacao**

Representa os registros de GPS contendo latitude, longitude e timestamp.

**Documentação:** [Backend Kotlin/Spring Boot](backend/README.md)

---

## 8. Aplicativos Android

### App1 — EstacaoMeteorologica

Aplicativo Android desenvolvido em Kotlin com Jetpack Compose.

Responsável por consultar e apresentar as informações disponibilizadas pelo backend.

Principais funcionalidades:

- Visualização das leituras meteorológicas.
- Consulta periódica ao backend.
- Exibição das informações dos sensores.
- Consulta da localização GPS da estação.
- Apresentação da posição geográfica em mapa.

O App1 consulta os dados meteorológicos e de localização por meio da API REST.

**Documentação:** [App1 — Dashboard Android](app1-dashboard/README.md)

### App2 — LocalizacaoGPS

Aplicativo Android desenvolvido em Kotlin e executado no Samsung Galaxy A23.

Responsabilidades:

- Solicitar permissão de localização ao usuário.
- Obter coordenadas GPS do smartphone.
- Preparar os dados de latitude, longitude e timestamp.
- Conectar ao HiveMQ Cloud.
- Publicar a localização no tópico `estacao/localizacao`.

Na implementação atual, a atualização da localização é solicitada pelo usuário no aplicativo.

O Galaxy A23 também atua como hotspot Wi-Fi para a ESP32.

**Documentação:** [App2 — Localização GPS](app2-localizacao/README.md)

---

## 9. Organização do repositório

```text
Estacao-Meteorologica-IoT/
|
|-- app1-dashboard/
|   |-- app/
|   |-- gradle/
|   |-- README.md
|
|-- app2-localizacao/
|   |-- app/
|   |-- gradle/
|   |-- README.md
|
|-- firmware-esp32/
|   |-- main.py
|   |-- bmp280.py
|   |-- umqtt/
|   |   |-- simple.py
|   |-- README.md
|
|-- backend/
|   |-- src/
|   |   |-- main/
|   |   |   |-- kotlin/
|   |   |   |-- resources/
|   |   |       |-- application.properties.example
|   |   |-- test/
|   |-- gradle/
|   |-- build.gradle.kts
|   |-- README.md
|
|-- README.md
```

Cada componente possui seu próprio código-fonte e arquivo README.

As configurações de desenvolvimento local e os arquivos de credenciais são protegidos pelas regras de versionamento.

---

## 10. Configuração do ambiente

Este repositório disponibiliza o código-fonte dos componentes do protótipo.

Para executar o sistema em outro ambiente, é necessário instalar as ferramentas, preparar as credenciais e configurar os serviços correspondentes.

### ESP32

Requisitos:

- Placa ESP32 com MicroPython.
- Sensores e conexões físicas correspondentes.
- Rede Wi-Fi com acesso à internet.
- Broker MQTT compatível.

Configure no `main.py` o nome e a senha da rede Wi-Fi, as credenciais MQTT e os parâmetros de conexão.

A versão pública utiliza marcadores nos campos sensíveis.

### Backend

Requisitos:

- Java 21.
- Gradle Wrapper incluído no projeto.
- Banco PostgreSQL acessível.
- Broker MQTT acessível.

O arquivo:

`backend/src/main/resources/application.properties.example`

contém um exemplo das configurações necessárias.

Copie o exemplo para `application.properties` e configure as variáveis de ambiente utilizadas pelo Spring Boot:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MQTT_BROKER_URL`
- `MQTT_USERNAME`
- `MQTT_PASSWORD`

O arquivo `application.properties` operacional não deve ser publicado no repositório.

Para compilar o backend no Windows, dentro da pasta `backend`, utilize:

```powershell
.\gradlew.bat compileKotlin compileTestKotlin
```

A compilação verifica o código Kotlin principal e os arquivos de testes, mas não executa os testes nem inicia o servidor.

### Aplicativos Android

Requisitos:

- Android Studio.
- Android SDK compatível com cada aplicativo.
- Gradle Wrapper incluído nos projetos.

Abra individualmente as pastas `app1-dashboard` e `app2-localizacao` no Android Studio.

O App1 necessita de acesso ao backend e da configuração de sua chave de mapas para exibir corretamente os recursos cartográficos.

O App2 necessita de permissões de localização e credenciais MQTT válidas para publicar coordenadas.

As instruções específicas de configuração de cada aplicativo devem ser consultadas em seus respectivos READMEs.

---

## 11. Testes realizados

| Teste | Resultado |
|---|---|
| DHT22 | Leituras de temperatura e umidade observadas |
| BMP280 | Comunicação I2C e leituras de pressão verificadas |
| LDR | Resposta analógica à variação de luminosidade |
| MQ-135 | Aquisição ADC e índice experimental funcionando |
| MQTT | Publicação e recebimento de mensagens |
| Backend | Recebimento e persistência das leituras |
| PostgreSQL | Armazenamento de dados meteorológicos |
| App1 | Visualização das informações meteorológicas |
| App2 | Envio e integração das coordenadas GPS |
| Hotspot Galaxy A23 | Conectividade Wi-Fi da ESP32 |
| Inicialização ESP32 | Execução do firmware após energização |
| Compilação backend | Código principal e testes compilados com sucesso |

Os testes confirmaram a integração funcional do protótipo.

Esses resultados não equivalem à calibração metrológica dos sensores ou à certificação do sistema para operação profissional.

---

## 12. Segurança das configurações

O repositório foi organizado para evitar a publicação de informações privadas.

Entre as informações que não devem ser versionadas estão:

- Senhas de Wi-Fi.
- Senhas e credenciais privadas do MQTT.
- Credenciais do PostgreSQL.
- Chaves privadas e arquivos de configuração sensíveis.
- Tokens e chaves de API que exijam proteção.

O backend disponibiliza um arquivo de exemplo com variáveis de ambiente.

Arquivos locais contendo credenciais devem permanecer fora do controle de versão.

Antes de executar uma cópia pública, cada desenvolvedor deve configurar suas próprias credenciais e os serviços necessários.

---

## 13. Limitações do protótipo

O sistema foi desenvolvido com finalidade acadêmica e experimental.

Entre as limitações atuais estão:

- Montagem eletrônica em protoboard.
- Ausência de gabinete de proteção ambiental.
- Ausência de pluviômetro físico.
- Índice do MQ-135 sem calibração em PPM.
- Leitura do LDR sem conversão calibrada para lux.
- Dependência de conectividade Wi-Fi e serviços em nuvem.
- Dependência do smartphone para fornecer a localização GPS da estação.

O protótipo não substitui uma estação meteorológica profissional calibrada.

---

## 14. Melhorias futuras

Entre as possíveis evoluções do projeto estão:

- Desenvolvimento de placa de circuito impresso (PCB).
- Instalação em gabinete com proteção ambiental.
- Implementação de pluviômetro físico.
- Calibração dos sensores analógicos.
- Ampliação dos testes de estabilidade.
- Aprimoramento do tratamento de falhas de comunicação.
- Expansão dos recursos de análise histórica.
- Melhorias na visualização das informações ambientais.

---

## 15. Referências técnicas

- [Espressif Systems — ESP32](https://docs.espressif.com/)
- [MicroPython — Documentação oficial](https://docs.micropython.org/)
- [OASIS — MQTT 3.1.1](https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [PostgreSQL](https://www.postgresql.org/docs/)
- [Android Developers](https://developer.android.com/)

---

## 16. Contexto acadêmico

Projeto desenvolvido para a disciplina de Projeto de Desenvolvimento II do curso de Ciência da Computação da Universidade Paulista (UNIP), em 2026.

A Estação Meteorológica IoT demonstra a integração entre sistemas embarcados, protocolos de comunicação, aplicações móveis, desenvolvimento backend, persistência de dados e serviços em nuvem.

**Finalidade:** acadêmica, experimental e de demonstração tecnológica.
