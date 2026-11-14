
# Backend — Estação Meteorológica IoT

**Kotlin | Spring Boot | Java 21 | PostgreSQL | MQTT | API REST**

Backend da Estação Meteorológica IoT, projeto acadêmico desenvolvido no curso de Ciência da Computação da Universidade Paulista (UNIP), em 2026.

O sistema integra os dados ambientais coletados por uma ESP32, a localização GPS enviada por um aplicativo Android, a comunicação MQTT e a persistência em PostgreSQL.

---

## 1. Sobre o backend

O backend é responsável pela comunicação entre os dispositivos IoT, o banco de dados e o aplicativo Android utilizado para visualizar as informações da estação.

Foi desenvolvido em Kotlin com Spring Boot e utiliza o padrão de organização em camadas, com controllers, services, repositories e entities.

Suas principais responsabilidades são:

- Estabelecer conexão MQTT com o HiveMQ Cloud.
- Receber as leituras meteorológicas enviadas pela ESP32.
- Receber as coordenadas GPS publicadas pelo App2.
- Desserializar mensagens JSON.
- Persistir os registros em PostgreSQL.
- Disponibilizar endpoints REST para consulta.
- Integrar os dados meteorológicos e de localização ao App1.

A aplicação foi implantada em um servidor Ubuntu Linux na DigitalOcean, utilizando PostgreSQL hospedado no Neon.

---

## 2. Tecnologias utilizadas

| Tecnologia | Finalidade |
|---|---|
| Kotlin 2.3.21 | Linguagem principal |
| Spring Boot 4.1.2 | Framework backend |
| Java 21 | Plataforma JVM |
| Gradle 9.8.2 | Compilação e gerenciamento de dependências |
| Spring Web MVC | API REST |
| Spring Data JPA | Acesso aos dados |
| Hibernate | Mapeamento objeto-relacional |
| PostgreSQL | Banco de dados relacional |
| Neon | Hospedagem PostgreSQL |
| Eclipse Paho MQTT 1.2.5 | Comunicação MQTT |
| Jackson | Serialização e desserialização JSON |
| Gson | Dependência JSON adicional |
| HiveMQ Cloud | Broker MQTT |
| DigitalOcean | Infraestrutura em nuvem |

---

## 3. Arquitetura

O backend recebe informações de duas fontes:

1. A ESP32 publica leituras meteorológicas.
2. O App2 publica coordenadas GPS.

As mensagens são recebidas pelo HiveMQ Cloud e encaminhadas ao backend por meio das inscrições MQTT.

### Fluxo de comunicação

    ESP32 + Sensores             App2 Android (GPS)
           |                            |
           v                            v
    Dados meteorológicos        Dados de localização
           |                            |
           +------------+---------------+
                        |
                        v
                   HiveMQ Cloud
                        |
                        v
                   MqttListener
                        |
              +---------+---------+
              |                   |
              v                   v
    LeituraMeteorologica   LocalizacaoEstacao
           Service              Service
              |                   |
              v                   v
          Repositories Spring Data JPA
                        |
                        v
                 PostgreSQL Neon
                        |
                        v
                   Controllers
                        |
                        v
                     API REST
                        |
                        v
                App1 Dashboard

O backend utiliza a injeção de dependências do Spring Boot para integrar seus componentes.

---

## 4. Estrutura do projeto

O código-fonte está organizado em:

    backend/
    |
    |-- src/
    |   |-- main/
    |   |   |-- kotlin/
    |   |   |   |-- com/example/estacaobackend/
    |   |   |       |-- EstacaoBackendApplication.kt
    |   |   |       |
    |   |   |       |-- config/
    |   |   |       |   |-- MqttConfig.kt
    |   |   |       |   |-- MqttListener.kt
    |   |   |       |
    |   |   |       |-- controller/
    |   |   |       |   |-- LeituraMeteorologicaController.kt
    |   |   |       |   |-- LocalizacaoEstacaoController.kt
    |   |   |       |
    |   |   |       |-- entity/
    |   |   |       |   |-- LeituraMeteorologica.kt
    |   |   |       |   |-- LocalizacaoEstacao.kt
    |   |   |       |
    |   |   |       |-- repository/
    |   |   |       |   |-- LeituraMeteorologicaRepository.kt
    |   |   |       |   |-- LocalizacaoEstacaoRepository.kt
    |   |   |       |
    |   |   |       |-- service/
    |   |   |           |-- LeituraMeteorologicaService.kt
    |   |   |           |-- LocalizacaoEstacaoService.kt
    |   |   |
    |   |   |-- resources/
    |   |       |-- application.properties.example
    |   |
    |   |-- test/
    |
    |-- gradle/
    |-- build.gradle.kts
    |-- settings.gradle.kts
    |-- gradle.properties
    |-- gradlew
    |-- gradlew.bat
    |-- .gitignore
    |-- README.md

O arquivo `application.properties` operacional não é versionado.

---

## 5. Organização em camadas

### Config

Contém os componentes responsáveis pela comunicação MQTT.

**MqttConfig.kt**

Configura o cliente Eclipse Paho, obtendo os parâmetros de conexão das propriedades Spring Boot.

Entre as configurações estão:

- Endereço do broker.
- Identificador do cliente.
- Usuário e senha.
- Reconexão automática.
- Clean Session.
- Timeout de conexão de 30 segundos.
- Keepalive de 60 segundos.

**MqttListener.kt**

Realiza as inscrições nos tópicos MQTT e processa as mensagens recebidas.

Os dados são convertidos de JSON para entidades Kotlin e encaminhados aos serviços correspondentes.

### Controller

Contém os endpoints REST utilizados para consultar os dados armazenados.

Os controllers recebem os serviços por injeção de dependências.

### Service

Contém as operações de persistência e consulta utilizadas pelos controllers e pelo listener MQTT.

Os serviços utilizam os repositórios JPA.

### Repository

Contém interfaces que estendem `JpaRepository`, disponibilizando operações de acesso ao PostgreSQL.

### Entity

Contém as entidades Kotlin mapeadas para as tabelas do banco de dados por meio do Jakarta Persistence.

---

## 6. Comunicação MQTT

O backend utiliza a biblioteca Eclipse Paho MQTT.

### Tópicos

| Tópico | Origem | Destino |
|---|---|---|
| `estacao/meteorologica/dados` | ESP32 | LeituraMeteorologicaService |
| `estacao/localizacao` | App2 | LocalizacaoEstacaoService |

O `MqttListener` realiza as inscrições durante a inicialização do componente Spring.

Quando uma mensagem chega, seu conteúdo é convertido de bytes para texto UTF-8.

Em seguida, o Jackson realiza a desserialização JSON para a entidade correspondente.

O serviço recebe a entidade e executa a persistência por meio do repositório JPA.

### Tratamento de erros

O listener utiliza blocos `try/catch` para registrar problemas de processamento das mensagens MQTT.

As exceções são exibidas no terminal para auxiliar o diagnóstico.

A conexão MQTT possui reconexão automática configurada, mas a implementação atual não inclui uma rotina explícita de renovação das inscrições após uma reconexão.

Esse comportamento poderá ser aprimorado futuramente.

---

## 7. Banco de dados PostgreSQL

O backend utiliza PostgreSQL, com persistência por Spring Data JPA e Hibernate.

Na implantação acadêmica, o banco está hospedado no Neon.

### Tabela leituras_meteorologicas

Representa as leituras recebidas da estação.

Principais campos:

| Campo | Tipo na entidade | Descrição |
|---|---|---|
| id | Long | Identificador do registro |
| temperatura | Double | Temperatura do DHT22 |
| umidade | Double | Umidade relativa |
| temperaturaBmp | Double | Temperatura do BMP280 |
| pressao | Double | Pressão atmosférica |
| unidadePressao | String | Unidade da pressão |
| qualidadeAr | Int | Índice experimental do MQ-135 |
| luminosidade | Int | Leitura analógica do LDR |
| chuva | Int | Valor de chuva simulado |
| dataHora | LocalDateTime | Data e hora do registro |

Os campos Kotlin são mapeados pelo JPA para a tabela relacional.

O índice `qualidadeAr` corresponde à escala experimental gerada pelo firmware, sem calibração em PPM.

A entidade não contém uma propriedade específica para armazenar `mq135_adc`, embora esse campo seja publicado no JSON do firmware.

### Tabela localizacoes_estacao

Armazena as coordenadas enviadas pelo App2.

| Campo | Tipo na entidade | Descrição |
|---|---|---|
| id | Long | Identificador |
| latitude | Double | Latitude em graus decimais |
| longitude | Double | Longitude em graus decimais |
| timestamp | Long | Instante enviado pelo aplicativo |

O timestamp é recebido em milissegundos desde a época Unix.

---

## 8. API REST

O backend disponibiliza endpoints HTTP para a consulta dos dados.

### Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/estacao/dados` | Lista as leituras meteorológicas |
| GET | `/api/estacao/ultima` | Retorna a leitura meteorológica mais recente |
| GET | `/api/estacao/localizacao` | Retorna a localização GPS mais recente |

### GET /api/estacao/dados

Utiliza `LeituraMeteorologicaService.listarTodas()` para consultar os registros meteorológicos.

Retorna uma lista de entidades.

### GET /api/estacao/ultima

Utiliza `LeituraMeteorologicaService.buscarUltima()`.

O serviço consulta os registros e seleciona aquele com o maior valor de `dataHora`.

Retorna `null` quando não existem leituras disponíveis.

### GET /api/estacao/localizacao

Utiliza `LocalizacaoEstacaoService.buscarUltima()`.

O serviço consulta os registros e seleciona aquele com o maior `timestamp`.

Retorna `null` quando não existem localizações disponíveis.

### Endereço da implantação acadêmica

O projeto utiliza o endereço:

`https://estacao-iot.duckdns.org`

A disponibilidade depende do servidor e dos serviços de infraestrutura.

---

## 9. Configuração do ambiente

### Requisitos

- Java 21.
- Gradle Wrapper incluído no projeto.
- PostgreSQL acessível.
- Broker MQTT acessível.
- Credenciais válidas para os serviços utilizados.

### Clonar o repositório

    git clone https://github.com/CrashOverride3310/Estacao-Meteorologica-IoT.git

Depois acesse a pasta:

    cd Estacao-Meteorologica-IoT/backend

### Configurar application.properties

A versão pública contém:

`src/main/resources/application.properties.example`

Esse arquivo apresenta um modelo das propriedades necessárias.

Copie-o para:

`src/main/resources/application.properties`

O Spring Boot utiliza `application.properties` para carregar as configurações.

O arquivo `.example` não é carregado automaticamente.

### Variáveis de ambiente

As propriedades do exemplo utilizam:

| Variável | Finalidade |
|---|---|
| DB_URL | URL JDBC do PostgreSQL |
| DB_USERNAME | Usuário do banco |
| DB_PASSWORD | Senha do banco |
| MQTT_BROKER_URL | Endereço de conexão MQTT |
| MQTT_USERNAME | Usuário MQTT |
| MQTT_PASSWORD | Senha MQTT |

Exemplo de formato de endereço JDBC:

    jdbc:postgresql://servidor:5432/banco_de_dados

Exemplo de endereço MQTT com TLS:

    ssl://seu-broker.hivemq.cloud:8883

Esses endereços são exemplos de formato, não credenciais de produção.

Configure as variáveis no ambiente antes de inicializar a aplicação.

O arquivo de configuração privada deve permanecer fora do Git.

---

## 10. Compilação

O backend utiliza Gradle Wrapper.

No Windows, a partir da pasta `backend`, execute:

    .\gradlew.bat compileKotlin compileTestKotlin --no-daemon

Esse comando compila o código Kotlin principal e os arquivos Kotlin de teste.

Não inicia o servidor Spring Boot nem executa os testes.

A compilação foi validada durante a preparação do código para publicação no GitHub, com resultado `BUILD SUCCESSFUL`.

### Executar testes

Para executar a tarefa de testes configurada pelo Gradle:

    .\gradlew.bat test

Essa operação poderá exigir configurações adicionais conforme os testes existentes.

### Executar o backend

Após configurar as credenciais e os serviços necessários, no Windows:

    .\gradlew.bat bootRun

Esse comando inicializa a aplicação Spring Boot.

Durante a inicialização, o backend tentará estabelecer as conexões configuradas, incluindo o broker MQTT e o PostgreSQL.

Não execute a aplicação utilizando credenciais ou serviços de produção sem compreender os efeitos da conexão e das operações de persistência.

---

## 11. Segurança

O projeto utiliza arquivos de configuração e variáveis de ambiente para gerenciar informações sensíveis.

O arquivo operacional:

`src/main/resources/application.properties`

está incluído nas regras do `.gitignore`.

A versão pública disponibiliza apenas:

`src/main/resources/application.properties.example`

Não devem ser publicados:

- Senhas PostgreSQL.
- Senhas MQTT.
- Tokens privados.
- Chaves de API sensíveis.
- Certificados ou chaves privadas.
- Arquivos locais contendo credenciais.

O `.gitignore` não remove automaticamente informações sensíveis que já estejam presentes no histórico do Git.

Antes de publicar novas alterações, recomenda-se verificar os arquivos adicionados e os valores de configuração.

---

## 12. Testes e validações

Durante o desenvolvimento do protótipo, foram realizadas verificações de:

- Conexão MQTT com o HiveMQ Cloud.
- Recebimento dos dados publicados pela ESP32.
- Conversão de mensagens JSON.
- Persistência de leituras meteorológicas.
- Armazenamento da localização GPS.
- Consulta aos endpoints REST.
- Integração com o App1.
- Integração com o App2.
- Compilação do backend Kotlin.

Os testes demonstraram a integração funcional do backend com os componentes do projeto acadêmico.

A compilação do código não substitui testes funcionais e de integração realizados em ambiente configurado.

---

## 13. Limitações e melhorias futuras

A implementação atual possui possibilidades de melhoria, como:

- Otimização das consultas de registros mais recentes.
- Paginação do histórico meteorológico.
- Melhorias no tratamento de falhas MQTT.
- Renovação de inscrições após reconexão.
- Ampliação dos testes automatizados.
- Aprimoramento do monitoramento e dos logs.
- Validação adicional dos dados recebidos.
- Maior controle de acesso aos endpoints.
- Evolução das configurações para diferentes ambientes.

Atualmente, os métodos que procuram a leitura e a localização mais recentes utilizam `findAll()` e selecionam o maior valor de data/hora ou timestamp em memória.

Uma evolução possível seria utilizar consultas otimizadas diretamente no banco de dados.

---

## 14. Projeto principal

Este backend integra o projeto:

[Estação Meteorológica IoT — ESP32](../README.md)

Componentes relacionados:

- [App1 — Dashboard Android](../app1-dashboard/README.md)
- [App2 — Localização GPS](../app2-localizacao/README.md)
- [Firmware ESP32](../firmware-esp32/README.md)

---

Projeto acadêmico — Ciência da Computação, Universidade Paulista (UNIP), 2026.
