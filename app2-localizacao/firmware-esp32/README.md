# Firmware — ESP32 (MicroPython)

Firmware desenvolvido em MicroPython para a Estação Meteorológica IoT.

## Funcionalidades

- Leitura de temperatura e umidade pelo DHT22 (GPIO4).
- Leitura de temperatura e pressão atmosférica pelo BMP280 (I²C).
- Leitura analógica do LDR (GPIO35).
- Leitura do MQ-135 (GPIO34), utilizando média de 10 amostras e índice experimental de 0 a 100.
- Simulação de precipitação por software.
- Conexão Wi-Fi e envio de mensagens JSON ao HiveMQ Cloud.
- Publicação MQTT aproximadamente a cada 4 segundos.
- Inicialização automática ao energizar a ESP32.

## Comunicação MQTT

Tópico: `estacao/meteorologica/dados`

## Arquivos do firmware

- `main.py` — programa principal.
- `bmp280.py` — driver do sensor BMP280.
- `umqtt/simple.py` — cliente MQTT.

**Status:** firmware desenvolvido e testado, com integração funcional ao backend e App1.

O código-fonte será publicado após a revisão e remoção das credenciais de acesso.
