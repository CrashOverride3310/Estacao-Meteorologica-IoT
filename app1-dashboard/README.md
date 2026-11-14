
# App1 — Dashboard Android

## Estação Meteorológica IoT

Aplicativo Android desenvolvido em Kotlin com Jetpack Compose para visualizar os dados ambientais coletados pela estação meteorológica ESP32.

Este aplicativo é a interface principal do projeto acadêmico Estação Meteorológica IoT, desenvolvido na Universidade Paulista (UNIP), em 2026.

## 1. Funcionalidades

O aplicativo permite:

- Visualizar temperatura e umidade relativa do ar.
- Consultar temperatura e pressão atmosférica do BMP280.
- Acompanhar o índice experimental do sensor MQ-135.
- Visualizar os valores analógicos de luminosidade.
- Apresentar informações de precipitação simulada.
- Consultar a localização GPS da estação.
- Exibir a posição geográfica em um mapa.
- Atualizar periodicamente os dados consultados no backend.

As consultas são realizadas aproximadamente a cada quatro segundos.

## 2. Tecnologias utilizadas

- Kotlin
- Android SDK
- Jetpack Compose
- Retrofit
- Comunicação HTTP/HTTPS
- JSON
- osmdroid
- MapTiler
- Gradle

## 3. Arquitetura

O App1 consulta as informações disponibilizadas pelo backend Kotlin/Spring Boot.

Fluxo de comunicação:

    Sensores ESP32
          |
          v
      HiveMQ Cloud
          |
          v
    Backend Spring Boot
          |
          v
    PostgreSQL (Neon)
          |
          v
       API REST
          |
          v
    App1 Dashboard Android

A localização GPS é obtida pelo App2, enviada ao backend e consultada pelo App1.

## 4. API REST

O aplicativo utiliza os seguintes endpoints:

| Método | Endpoint | Finalidade |
|---|---|---|
| GET | `/api/estacao/ultima` | Consultar a última leitura meteorológica |
| GET | `/api/estacao/localizacao` | Consultar a localização GPS mais recente |

Endereço do backend utilizado no projeto:

`https://estacao-iot.duckdns.org/`

O funcionamento das consultas depende da disponibilidade do serviço.

## 5. Estrutura do código

Os principais arquivos estão localizados em:

`app/src/main/java/com/example/estacaometeorologica/`

| Arquivo | Responsabilidade |
|---|---|
| `MainActivity.kt` | Interface e apresentação dos dados |
| `data/EstacaoApi.kt` | Definição das chamadas REST |
| `data/RetrofitClient.kt` | Configuração do cliente HTTP |
| `data/SensorData.kt` | Modelo dos dados meteorológicos |
| `data/LocalizacaoData.kt` | Modelo dos dados de localização |

A pasta `ui/theme/` contém os recursos de apresentação visual do aplicativo.

## 6. Como configurar

### Requisitos

- Android Studio
- Android SDK compatível com o projeto
- Acesso à internet
- Backend disponível

### Abrir o projeto

1. Clone o repositório principal.
2. Abra o Android Studio.
3. Selecione a opção para abrir um projeto existente.
4. Escolha a pasta `app1-dashboard`.
5. Aguarde a sincronização do Gradle.
6. Selecione um emulador ou dispositivo Android compatível.
7. Compile e execute o aplicativo.

No Windows, também é possível iniciar uma verificação de compilação pela pasta do App1:

    .\gradlew.bat assembleDebug

### Configuração do backend

O cliente HTTP está configurado para acessar o endereço do backend utilizado no projeto.

Caso o aplicativo seja executado com outro backend, será necessário ajustar o endereço em `RetrofitClient.kt`.

### Configuração do mapa

A visualização cartográfica utiliza osmdroid e MapTiler.

A versão pública do código utiliza um marcador no lugar da chave MapTiler.

Para utilizar o mapa, configure uma chave válida no local indicado em `MainActivity.kt`.

Não publique uma chave privada sem verificar suas permissões e restrições de uso.

## 7. Dados apresentados

O dashboard trabalha com informações recebidas do sistema de monitoramento.

### MQ-135

O indicador do MQ-135 utiliza uma escala experimental de 0 a 100 calculada pelo firmware a partir de leituras analógicas reais.

Esse valor não representa concentração calibrada de gases em PPM.

### Luminosidade

A luminosidade corresponde ao valor analógico recebido pelo ADC da ESP32, sem calibração em lux.

### Precipitação

A variável de chuva é simulada por software no firmware da estação.

Não existe um pluviômetro físico nesta implementação.

## 8. Testes realizados

O aplicativo foi integrado ao backend e utilizado para visualizar as leituras meteorológicas recebidas da ESP32.

Também foi validada a apresentação da localização GPS fornecida pelo App2.

Os testes demonstraram o funcionamento integrado do protótipo acadêmico.

## 9. Limitações

- Depende da disponibilidade do backend e de conexão à internet.
- A visualização do mapa exige configuração válida do serviço cartográfico.
- Algumas variáveis ambientais são experimentais ou simuladas.
- O sistema não substitui equipamentos meteorológicos profissionais calibrados.

## 10. Projeto principal

Este aplicativo faz parte do projeto:

[Estação Meteorológica IoT — ESP32](../README.md)

Componentes relacionados:

- [App2 — Localização GPS](../app2-localizacao/README.md)
- [Firmware ESP32](../firmware-esp32/README.md)
- [Backend Kotlin/Spring Boot](../backend/README.md)

---

Projeto acadêmico — Ciência da Computação, Universidade Paulista (UNIP), 2026.
