
# App2 — Localização GPS

## Estação Meteorológica IoT

Aplicativo Android desenvolvido em Kotlin com Jetpack Compose para obter e transmitir a localização GPS da Estação Meteorológica IoT.

O aplicativo integra o projeto acadêmico desenvolvido na Universidade Paulista (UNIP), em 2026.

---

## 1. Sobre o aplicativo

O App2, denominado LocalizacaoGPS, é responsável por obter a localização geográfica do smartphone utilizado junto à estação meteorológica.

O projeto utiliza um Samsung Galaxy A23, que permanece próximo à ESP32 durante a operação.

O aplicativo obtém as coordenadas GPS e publica os dados por MQTT no HiveMQ Cloud.

O backend Kotlin/Spring Boot recebe essas informações, armazena os registros no PostgreSQL e disponibiliza a localização ao App1.

O Galaxy A23 também atua como hotspot Wi-Fi, fornecendo conectividade à ESP32.

---

## 2. Funcionalidades

O aplicativo permite:

- Solicitar permissões de localização do Android.
- Obter a localização atual do smartphone.
- Consultar latitude e longitude.
- Registrar o timestamp da localização.
- Estabelecer comunicação com o HiveMQ Cloud.
- Publicar os dados GPS utilizando MQTT.
- Integrar as coordenadas ao backend da estação.

### Atualização da localização

Na implementação atual, o usuário solicita uma nova leitura GPS pelo botão do aplicativo.

A localização não é publicada continuamente de maneira automática.

Essa característica foi preservada no projeto testado.

---

## 3. Tecnologias utilizadas

| Tecnologia | Finalidade |
|---|---|
| Kotlin | Desenvolvimento Android |
| Jetpack Compose | Interface do usuário |
| Android SDK | Recursos da plataforma |
| Google Play Services Location | Obtenção da localização |
| Eclipse Paho MQTT | Comunicação MQTT |
| HiveMQ Cloud | Broker MQTT |
| JSON | Formato dos dados |
| Gradle | Compilação e dependências |

---

## 4. Arquitetura

O aplicativo atua como uma fonte de localização para o sistema IoT.

Fluxo de comunicação:

    Samsung Galaxy A23
            |
            v
       App2 Android
            |
            v
    Localização GPS
            |
            v
     JSON + MQTT
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

O backend recebe e armazena as coordenadas por meio de um tópico MQTT específico.

---

## 5. Comunicação MQTT

O aplicativo utiliza a biblioteca Eclipse Paho para estabelecer conexão com o broker HiveMQ Cloud.

### Configurações

| Parâmetro | Valor |
|---|---|
| Broker | HiveMQ Cloud |
| Protocolo | MQTT com TLS |
| Porta | 8883 |
| Tópico | `estacao/localizacao` |
| QoS da publicação | 1 |
| Retain | Desativado |

O cliente MQTT realiza autenticação utilizando usuário e senha.

As credenciais não devem ser publicadas diretamente no repositório.

---

## 6. Formato da mensagem JSON

O aplicativo envia mensagens contendo latitude, longitude e timestamp.

Exemplo ilustrativo, sem representar uma localização coletada:

```json
{
  "latitude": -23.000000,
  "longitude": -46.000000,
  "timestamp": 1760000000000
}
```

### Campos enviados

| Campo | Tipo | Descrição |
|---|---|---|
| `latitude` | Double | Latitude em graus decimais |
| `longitude` | Double | Longitude em graus decimais |
| `timestamp` | Long | Instante da localização em milissegundos desde a época Unix |

A entidade `LocalizacaoEstacao` do backend recebe esses dados e os armazena no PostgreSQL.

---

## 7. Estrutura do código

O código principal está localizado em:

`app/src/main/java/com/example/localizacaogps/`

### Arquivos principais

| Arquivo | Responsabilidade |
|---|---|
| `MainActivity.kt` | Interface, permissões GPS e comunicação MQTT |
| `ui/theme/Color.kt` | Definição das cores |
| `ui/theme/Theme.kt` | Configuração do tema |
| `ui/theme/Type.kt` | Configuração tipográfica |
| `AndroidManifest.xml` | Permissões e configurações Android |
| `app/build.gradle.kts` | Dependências e configurações do aplicativo |

---

## 8. Como configurar

### Requisitos

- Android Studio.
- Android SDK compatível.
- Smartphone ou emulador com serviços de localização.
- Conexão à internet.
- Credenciais válidas de um broker MQTT.

### Abrir o aplicativo

1. Clone o repositório principal.
2. Abra o Android Studio.
3. Selecione a pasta `app2-localizacao`.
4. Aguarde a sincronização do Gradle.
5. Configure as credenciais MQTT.
6. Selecione um dispositivo Android compatível.
7. Compile e execute o aplicativo.

No Windows, também é possível compilar pela pasta do App2:

    .\gradlew.bat assembleDebug

### Credenciais MQTT

O arquivo `app/build.gradle.kts` utiliza duas variáveis de ambiente:

- `MQTT_USERNAME`
- `MQTT_PASSWORD`

Essas variáveis são utilizadas para gerar os campos correspondentes na classe `BuildConfig` durante a compilação.

Configure as variáveis no ambiente utilizado pelo Gradle antes de compilar o aplicativo.

Não é necessário adicionar usuário e senha diretamente ao `MainActivity.kt`.

**Atenção:** as credenciais incorporadas ao `BuildConfig` também fazem parte do aplicativo compilado. Portanto, esse mecanismo evita publicar os valores no código-fonte, mas não impede sua extração do APK. Utilize credenciais com permissões restritas e apropriadas ao ambiente.

---

## 9. Permissões Android

O aplicativo utiliza permissões relacionadas à localização e à conectividade.

Principais permissões:

- `INTERNET`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`

As permissões de localização são solicitadas ao usuário durante a utilização do aplicativo.

O acesso ao GPS depende das permissões concedidas e das condições de recepção de localização do dispositivo.

---

## 10. Integração com o backend

O backend recebe as mensagens GPS publicadas no tópico:

`estacao/localizacao`

O componente `MqttListener.kt` processa os dados recebidos.

Em seguida, o serviço de localização salva os registros no PostgreSQL.

O endpoint REST utilizado para consultar a localização mais recente é:

`GET /api/estacao/localizacao`

O App1 utiliza esse endpoint para apresentar a posição geográfica da estação.

---

## 11. Testes realizados

O App2 foi instalado e testado no Samsung Galaxy A23.

Durante os testes:

- O aplicativo obteve coordenadas GPS.
- As informações foram publicadas via MQTT.
- O backend recebeu as mensagens.
- Os registros foram armazenados no PostgreSQL.
- O App1 apresentou a localização da estação.

Também foi realizado um teste de deslocamento de aproximadamente 500 metros, com atualizações de localização solicitadas pelo usuário no App2.

O funcionamento integrado foi validado no protótipo acadêmico.

---

## 12. Limitações

- A atualização da posição depende da solicitação do usuário.
- O aplicativo necessita de conexão à internet para publicar os dados.
- A precisão da localização depende das condições do dispositivo e dos serviços de posicionamento.
- O sistema depende da disponibilidade do broker MQTT e do backend.
- A posição publicada corresponde ao smartphone utilizado junto à estação.

---

## 13. Segurança

As credenciais MQTT não devem ser armazenadas diretamente no código-fonte público.

A versão publicada utiliza variáveis de ambiente para configurar os dados de autenticação durante a compilação.

Arquivos locais de configuração e credenciais devem permanecer protegidos e fora do repositório.

Para uma implantação de produção, recomenda-se adotar mecanismos de autenticação e autorização adequados ao dispositivo e limitar as permissões das credenciais MQTT.

---

## 14. Projeto principal

O App2 integra o projeto:

[Estação Meteorológica IoT — ESP32](../README.md)

Componentes relacionados:

- [App1 — Dashboard Android](../app1-dashboard/README.md)
- [Firmware ESP32](../firmware-esp32/README.md)
- [Backend Kotlin/Spring Boot](../backend/README.md)

---

Projeto acadêmico — Ciência da Computação, Universidade Paulista (UNIP), 2026.
