
# =========================================================
# ESTAÇÃO METEOROLÓGICA IoT — ESP32
# =========================================================
#
# Biblioteca de comunicação com o sensor BMP280.
#
# Linguagem: MicroPython
# Comunicação: I2C
#
# Responsabilidades:
# - Identificar o BMP280 no barramento I2C.
# - Verificar o identificador do sensor.
# - Configurar os registradores de operação.
# - Ler os coeficientes internos de calibração.
# - Obter os valores brutos de temperatura e pressão.
# - Aplicar os cálculos de compensação.
# - Disponibilizar temperatura em graus Celsius.
# - Disponibilizar pressão atmosférica em hPa.
#
# Esta biblioteca é utilizada pelo arquivo main.py.
#
# IMPORTANTE:
# Foram preservados os cálculos e as configurações
# da implementação original do projeto.
# =========================================================


# Biblioteca utilizada para aguardar a inicialização
# do sensor após configurar os registradores.
import time


# =========================================================
# CLASSE BMP280
# =========================================================
#
# Representa o sensor BMP280 conectado à ESP32.
#
# Encapsula a comunicação I2C, a configuração do
# dispositivo e os cálculos de compensação das leituras.
# =========================================================

class BMP280:

    # =====================================================
    # CONVERSÃO PARA INTEIRO COM SINAL DE 16 BITS
    # =====================================================
    #
    # Alguns coeficientes de calibração são armazenados
    # pelo sensor como inteiros de 16 bits com sinal.
    #
    # Esta função interpreta dois bytes em little-endian
    # e converte valores superiores ou iguais a 32768
    # para a representação negativa correspondente.
    #
    # O método é estático porque não depende de
    # atributos específicos da instância do sensor.
    # =====================================================

    @staticmethod
    def _signed16(data):

        # Converte os bytes em um inteiro sem sinal.
        value = int.from_bytes(data, "little")

        # Ajusta a representação para inteiro com sinal.
        if value >= 32768:
            value -= 65536

        return value


    # =====================================================
    # INICIALIZAÇÃO DO SENSOR
    # =====================================================
    #
    # Recebe:
    # i2c: barramento I2C configurado no main.py.
    # address: endereço I2C inicialmente esperado.
    #
    # O endereço padrão utilizado é 0x76.
    #
    # Se o dispositivo não for encontrado em 0x76,
    # a biblioteca verifica a presença em 0x77.
    #
    # Depois verifica o identificador do chip,
    # lê os coeficientes de calibração e configura
    # os registradores de operação do sensor.
    # =====================================================

    def __init__(self, i2c, address=0x76):

        # Armazena o barramento I2C recebido.
        self.i2c = i2c

        # Armazena o endereço inicial do dispositivo.
        self.address = address

        # Procura os dispositivos presentes no I2C.
        devices = self.i2c.scan()

        # Verifica se o endereço esperado foi encontrado.
        if self.address not in devices:

            # Tenta utilizar o endereço alternativo 0x77.
            if 0x77 in devices:
                self.address = 0x77

            # Interrompe a inicialização caso o sensor
            # não seja encontrado nos endereços previstos.
            else:
                raise OSError("BMP280 não encontrado no I2C")


        # Lê o registrador de identificação do sensor.
        # O registrador de CHIP ID está em 0xD0.
        chip_id = self._read_byte(0xD0)

        # Verifica os identificadores aceitos
        # pela implementação original.
        if chip_id not in (0x58, 0x60):
            raise OSError("Sensor não reconhecido")


        # Lê os coeficientes internos de calibração
        # necessários para compensar as medições.
        self._read_calibration()


        # Modo normal.
        #
        # Configura o registrador ctrl_meas (0xF4)
        # utilizando o valor original 0x27.
        #
        # Define a operação contínua do sensor
        # e as configurações de sobreamostragem.
        self._write_byte(
            0xF4,
            0x27
        )


        # Filtro.
        #
        # Configura o registrador config (0xF5)
        # com o valor original 0xA0.
        #
        # Preserva os parâmetros de configuração
        # utilizados no projeto.
        self._write_byte(
            0xF5,
            0xA0
        )


        # Aguarda 100 milissegundos após a configuração.
        time.sleep_ms(100)


    # =====================================================
    # COMUNICAÇÃO I2C
    # =====================================================
    #
    # Métodos auxiliares responsáveis pela leitura
    # e escrita dos registradores internos do BMP280.
    #
    # Esses métodos são utilizados pelas demais
    # funções da biblioteca.
    # =====================================================


    # -----------------------------------------------------
    # LEITURA DE UM BYTE
    # -----------------------------------------------------
    #
    # Lê um byte de um registrador específico.
    #
    # Parâmetro:
    # register: endereço do registrador desejado.
    #
    # Retorno:
    # Valor inteiro correspondente ao byte recebido.
    # -----------------------------------------------------

    def _read_byte(self, register):

        return self.i2c.readfrom_mem(
            self.address,
            register,
            1
        )[0]


    # -----------------------------------------------------
    # LEITURA DE MÚLTIPLOS BYTES
    # -----------------------------------------------------
    #
    # Lê uma sequência de bytes a partir de um registrador.
    #
    # Parâmetros:
    # register: endereço inicial.
    # length: quantidade de bytes solicitados.
    # -----------------------------------------------------

    def _read_bytes(self, register, length):

        return self.i2c.readfrom_mem(
            self.address,
            register,
            length
        )


    # -----------------------------------------------------
    # ESCRITA DE UM BYTE
    # -----------------------------------------------------
    #
    # Envia um valor de um byte para um registrador
    # interno do sensor por meio do barramento I2C.
    #
    # Utilizado para configurar o funcionamento do BMP280.
    # -----------------------------------------------------

    def _write_byte(self, register, value):

        self.i2c.writeto_mem(
            self.address,
            register,
            bytes([value])
        )


    # =====================================================
    # CALIBRAÇÃO
    # =====================================================
    #
    # O BMP280 possui coeficientes individuais
    # armazenados em seus registradores internos.
    #
    # Esses coeficientes são utilizados para compensar
    # os valores brutos de temperatura e pressão.
    #
    # A rotina lê 24 bytes a partir do endereço 0x88.
    #
    # Coeficientes de temperatura:
    # dig_T1, dig_T2 e dig_T3.
    #
    # Coeficientes de pressão:
    # dig_P1 até dig_P9.
    #
    # A interpretação de cada coeficiente respeita
    # o formato numérico previsto na implementação.
    # =====================================================

    def _read_calibration(self):

        # Obtém os 24 bytes dos registradores
        # de calibração do sensor.
        data = self._read_bytes(
            0x88,
            24
        )


        # Coeficiente de temperatura T1:
        # inteiro de 16 bits sem sinal.
        self.dig_T1 = int.from_bytes(
            data[0:2],
            "little"
        )

        # Coeficientes de temperatura com sinal.
        self.dig_T2 = self._signed16(data[2:4])
        self.dig_T3 = self._signed16(data[4:6])


        # Coeficiente de pressão P1:
        # inteiro de 16 bits sem sinal.
        self.dig_P1 = int.from_bytes(
            data[6:8],
            "little"
        )

        # Coeficientes de pressão com sinal.
        self.dig_P2 = self._signed16(data[8:10])
        self.dig_P3 = self._signed16(data[10:12])
        self.dig_P4 = self._signed16(data[12:14])
        self.dig_P5 = self._signed16(data[14:16])
        self.dig_P6 = self._signed16(data[16:18])
        self.dig_P7 = self._signed16(data[18:20])
        self.dig_P8 = self._signed16(data[20:22])
        self.dig_P9 = self._signed16(data[22:24])


    # =====================================================
    # LEITURAS BRUTAS DOS REGISTRADORES
    # =====================================================
    #
    # O BMP280 disponibiliza as leituras brutas
    # de temperatura e pressão em registradores.
    #
    # Os valores são reconstruídos por operações
    # de deslocamento de bits e OR bit a bit.
    #
    # As medições brutas ainda não estão compensadas.
    # =====================================================


    # -----------------------------------------------------
    # TEMPERATURA BRUTA
    # -----------------------------------------------------
    #
    # Lê três bytes a partir do endereço 0xFA.
    #
    # Reconstrói o valor ADC de temperatura
    # utilizando 20 bits de informação.
    # -----------------------------------------------------

    def _read_raw_temperature(self):

        data = self._read_bytes(
            0xFA,
            3
        )

        return (
            (data[0] << 12)
            |
            (data[1] << 4)
            |
            (data[2] >> 4)
        )


    # -----------------------------------------------------
    # PRESSÃO BRUTA
    # -----------------------------------------------------
    #
    # Lê três bytes a partir do endereço 0xF7.
    #
    # Reconstrói o valor ADC de pressão
    # utilizando 20 bits de informação.
    # -----------------------------------------------------

    def _read_raw_pressure(self):

        data = self._read_bytes(
            0xF7,
            3
        )

        return (
            (data[0] << 12)
            |
            (data[1] << 4)
            |
            (data[2] >> 4)
        )


    # =====================================================
    # TEMPERATURA
    # =====================================================
    #
    # Propriedade responsável por calcular a temperatura
    # compensada a partir do valor bruto do BMP280.
    #
    # Utiliza:
    # - Leitura bruta de temperatura.
    # - Coeficientes de calibração dig_T1 a dig_T3.
    #
    # A variável t_fine é armazenada na instância
    # porque também é necessária para compensar
    # a leitura de pressão atmosférica.
    #
    # Retorna a temperatura em graus Celsius,
    # com arredondamento para duas casas decimais.
    # =====================================================

    @property
    def temperature(self):

        # Obtém a leitura bruta da temperatura.
        adc_T = self._read_raw_temperature()


        # Primeira etapa da compensação.
        var1 = (
            (adc_T / 16384.0)
            -
            (self.dig_T1 / 1024.0)
        ) * self.dig_T2


        # Segunda etapa da compensação.
        var2 = (
            (
                (adc_T / 131072.0)
                -
                (self.dig_T1 / 8192.0)
            )
            ** 2
            *
            self.dig_T3
        )


        # Armazena a compensação intermediária.
        #
        # Esse valor também é utilizado no
        # cálculo da pressão atmosférica.
        self.t_fine = var1 + var2


        # Converte o resultado compensado
        # para temperatura em graus Celsius.
        temperature = self.t_fine / 5120.0


        # Retorna a temperatura com duas casas decimais.
        return round(
            temperature,
            2
        )


    # =====================================================
    # PRESSÃO ATMOSFÉRICA
    # =====================================================
    #
    # Propriedade responsável pelo cálculo da pressão
    # atmosférica compensada do BMP280.
    #
    # Utiliza:
    # - Leitura bruta de pressão.
    # - Coeficientes dig_P1 a dig_P9.
    # - Valor intermediário t_fine.
    #
    # As operações abaixo fazem parte da fórmula
    # de compensação empregada pelo firmware original.
    #
    # O resultado é convertido para hPa
    # e arredondado para duas casas decimais.
    # =====================================================

    @property
    def pressure(self):

        # Obtém a leitura bruta de pressão.
        adc_P = self._read_raw_pressure()

        # Atualiza t_fine realizando o cálculo
        # de temperatura antes da compensação.
        self.temperature

        # Primeira etapa da compensação da pressão.
        var1 = (
            (self.t_fine / 2.0)
            - 64000.0
        )

        # Calcula a contribuição do coeficiente dig_P6.
        var2 = (
            var1
            * var1
            * self.dig_P6
            / 32768.0
        )

        # Incorpora o coeficiente dig_P5.
        var2 = (
            var2
            + var1
            * self.dig_P5
            * 2.0
        )

        # Incorpora o coeficiente dig_P4.
        var2 = (
            var2 / 4.0
            + self.dig_P4
            * 65536.0
        )

        # Calcula uma etapa intermediária
        # utilizando dig_P3 e dig_P2.
        var1 = (
            self.dig_P3 * var1 * var1 / 524288.0
            + self.dig_P2 * var1
        ) / 524288.0

        # Aplica o coeficiente dig_P1.
        var1 = (
            1.0
            + var1 / 32768.0
        ) * self.dig_P1

        # Evita a divisão por zero
        # no cálculo subsequente da pressão.
        if var1 == 0:
            return 0

        # Prepara o valor bruto para a compensação.
        p = 1048576.0 - adc_P

        # Calcula a primeira pressão compensada.
        p = (
            (p - var2 / 4096.0)
            * 6250.0
            / var1
        )

        # Calcula a contribuição do coeficiente dig_P9.
        var1 = (
            self.dig_P9
            * p
            * p
            / 2147483648.0
        )

        # Calcula a contribuição do coeficiente dig_P8.
        var2 = (
            p
            * self.dig_P8
            / 32768.0
        )

        # Aplica o ajuste final com os coeficientes
        # dig_P7, dig_P8 e dig_P9.
        p = (
            p
            + (var1 + var2 + self.dig_P7)
            / 16.0
        )

        # Converte a pressão para hectopascais (hPa)
        # e retorna o resultado com duas casas decimais.
        return round(
            p / 100.0,
            2
        )
