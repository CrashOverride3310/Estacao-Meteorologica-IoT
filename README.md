Estação Meteorológica IoT — ESP32
Projeto de Desenvolvimento II | Universidade Paulista (UNIP) | Ciência da Computação | 2026



- Sobre o projeto

O projeto Estação Meteorológica IoT consiste no desenvolvimento de um sistema de monitoramento ambiental utilizando ESP32, sensores físicos, comunicação MQTT, backend em Kotlin e aplicativos Android.

O objetivo é demonstrar como tecnologias de Internet das Coisas (IoT) podem ser aplicadas ao monitoramento de condições ambientais locais, contribuindo para estudos de microclima e conceitos de cidades inteligentes.

O sistema foi desenvolvido e testado, integrando hardware, comunicação em nuvem, persistência de dados, geolocalização e interface mobile.


- Tecnologias utilizadas

| Topicos | Tecnologias |

| Hardware | ESP32, DHT22, BMP280, LDR e MQ-135 |

| Firmware | MicroPython |

| Comunicação | Wi-Fi, MQTT e HiveMQ Cloud |

| Backend | Kotlin, Spring Boot, Java 21 |

| Banco de dados | PostgreSQL, Neon |

| Infraestrutura | DigitalOcean, Ubuntu Linux |

| Aplicativos | Android, Kotlin, Jetpack Compose |

| Localização | GPS do Samsung Galaxy A23 |

| Ferramentas | VS Code, Android Studio, MQTTX, Git e GitHub |



- Arquitetura do sistema

O sistema utiliza duas fontes de dados:

1. ESP32: realiza a leitura dos sensores e transmite a telemetria meteorológica.
2. App2 — LocalizacaoGPS: obtém as coordenadas GPS do celular e transmite a localização da estação.

Ambos utilizam o broker HiveMQ Cloud, com tópicos MQTT separados.



→ ESP32
→ Sensores
→ App2 GPS → HiveMQ Cloud →

→ Backend Kotlin/Spring Boot
→ PostgreSQL (Neon)
→ API REST
→ App1 Dashboard Android

O backend está hospedado em um servidor Linux na DigitalOcean. A ESP32 utiliza a conexão Wi-Fi disponibilizada pelo hotspot do Samsung Galaxy A23.



- Sensores e componentes

| Componente | Função | Conexão |

| DHT22 | Temperatura e umidade | GPIO4 |

| BMP280 | Temperatura e pressão atmosférica | I²C — GPIO21/22 |

| LDR | Variação da luminosidade | GPIO35 |

| MQ-135 | Sinal analógico experimental relacionado à qualidade do ar | GPIO34 |

| GPS do celular | Latitude e longitude | App2 Android |

O MQ-135 utiliza um divisor resistivo de 20 kΩ e 10 kΩ antes da entrada analógica da ESP32.

Observações técnicas: o índice do MQ-135 é experimental, calculado em escala de 0 a 100 a partir de leituras ADC. Ele não corresponde a uma concentração calibrada em PPM. A luminosidade não é calibrada em lux. A precipitação é simulada por software, sem pluviômetro físico.



- Firmware MicroPython

O firmware executado na ESP32 realiza:

1. Inicialização dos sensores e conexão Wi-Fi.
2. Aquisição periódica de dados ambientais.
3. Média de 10 amostras analógicas do MQ-135.
4. Organização das leituras em formato JSON.
5. Publicação por MQTT aproximadamente a cada 4 segundos.
6. Inicialização automática quando a placa recebe alimentação elétrica.

O principal arquivo do firmware é main.py, acompanhado dos módulos de suporte ao BMP280 e ao cliente MQTT.



- Comunicação MQTT

Broker: HiveMQ Cloud.

| Tópico | Origem | Finalidade |

| estacao/meteorologica/dados | ESP32 | Telemetria dos sensores |

| estacao/localizacao | App2 | Coordenadas GPS |


A comunicação foi testada utilizando MQTTX, com mensagens reais recebidas no tópico meteorológico e mensagens de localização no tópico GPS.



- Backend e banco de dados

O backend utiliza Kotlin com Spring Boot para integrar a comunicação MQTT, o armazenamento relacional e a disponibilização de dados por API REST.

A aplicação recebe mensagens meteorológicas por meio de um subscriber MQTT e utiliza JPA/Hibernate para persistência no PostgreSQL hospedado no Neon.

Endpoints utilizados pelo dashboard:

GET /api/estacao/dados
GET /api/estacao/ultima

A entidade LeituraMeteorologica representa os registros dos sensores e seus respectivos horários.



- Aplicativos Android


App1 — EstacaoMeteorologica

Aplicativo Android desenvolvido com Kotlin e Jetpack Compose.

Permite consultar e visualizar as informações meteorológicas disponibilizadas pelo backend, incluindo a localização geográfica da estação.


App2 — LocalizacaoGPS

Aplicativo Android executado no Samsung Galaxy A23.

Obtém coordenadas de latitude e longitude por GPS e as publica por MQTT, permitindo associar a posição geográfica às informações meteorológicas.

O Galaxy A23 também funciona como hotspot Wi-Fi para fornecer internet à ESP32.



Testes realizados

| Teste | Resultado |

| CT01 — DHT22 | Leituras de temperatura e umidade observadas |

| CT02 — BMP280 | Comunicação I²C e pressão verificadas |

| CT03 — LDR | Resposta analógica à iluminação validada |

| CT04 — MQ-135 | Aquisição ADC e índice experimental funcionando |

| Comunicação MQTT | Recebimento de mensagens no HiveMQ/MQTTX |

| Integração backend | Recebimento e persistência dos dados |

| App1 | Exibição dos dados meteorológicos |

| App2 | Envio e integração da localização GPS |

| Hotspot Galaxy A23 | Conexão Wi-Fi da ESP32 confirmada |

| Inicialização automática | Firmware executado sem depender do PowerShell |

Os testes demonstraram a integração funcional do protótipo, mas não representam calibração metrológica dos sensores.



- Limitações e melhorias futuras

O protótipo utiliza montagem em protoboard, adequada para desenvolvimento e experimentação, mas não ideal para exposição permanente ao ambiente externo.


- Melhorias previstas:

Desenvolvimento de uma placa de circuito impresso (PCB).
Instalação em gabinete com proteção ambiental, como IP65.
Calibração dos sensores analógicos.
Inclusão de um pluviômetro físico.
Ampliação dos recursos de análise e visualização de dados.
Aprimoramento dos testes de estabilidade e precisão.



- Organização do repositório

Estrutura da publicação:

Estacao-Meteorologica-IoT/

→ app1-dashboard/

→ app2-localizacao/

→ firmware-esp32/

→ backend/

→ docs/

→ README.md


- Segurança

Os códigos-fonte e demais materiais serão adicionados após remoção de informações sensíveis.

Senhas de Wi-Fi, credenciais MQTT, tokens e dados privados de conexão com o banco de dados não devem ser publicados neste repositório.

As configurações sensíveis devem ser fornecidas por mecanismos seguros, como variáveis de ambiente e arquivos locais não versionados.



- Referências técnicas

- ESPRESSIF SYSTEMS. Documentação técnica do ESP32. https://docs.espressif.com/
- MICROPYTHON. Documentação oficial. https://docs.micropython.org/
- OASIS. MQTT Version 5.0. https://docs.oasis-open.org/mqtt/mqtt/v5.0/
- SPRING. Spring Boot. https://spring.io/projects/spring-boot
- POSTGRESQL. Documentação oficial. https://www.postgresql.org/docs/
- ANDROID DEVELOPERS. Documentação Android. https://developer.android.com/

---

Projeto acadêmico desenvolvido para a Universidade Paulista (UNIP), em 2026.
*Este sistema possui finalidade acadêmica e experimental e não substitui uma estação meteorológica profissional calibrada.
