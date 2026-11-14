
# =========================================================
# ESTAÇÃO METEOROLÓGICA IoT — ESP32
# =========================================================
#
# Biblioteca de comunicação MQTT para MicroPython.
#
# Arquivo: umqtt/simple.py
#
# Responsabilidades:
# - Estabelecer uma conexão TCP com o broker MQTT.
# - Habilitar comunicação TLS quando solicitada.
# - Montar e enviar o pacote MQTT CONNECT.
# - Autenticar o cliente com usuário e senha.
# - Interpretar a resposta CONNACK do broker.
# - Publicar mensagens em tópicos MQTT.
# - Encerrar a conexão MQTT.
#
# Esta implementação é utilizada pelo main.py
# para enviar as leituras da estação meteorológica
# ao broker HiveMQ Cloud.
#
# Protocolo utilizado: MQTT 3.1.1.
#
# =========================================================


# Biblioteca de comunicação por sockets.
# Permite estabelecer conexões TCP com o broker.
import usocket as socket

# Biblioteca utilizada para empacotar valores
# binários no formato exigido pelo protocolo MQTT.
import ustruct as struct

# Biblioteca de controle de tempo do MicroPython.
import utime as time


# =========================================================
# EXCEÇÃO MQTT
# =========================================================
#
# Exceção personalizada utilizada para indicar
# problemas durante a comunicação MQTT.
#
# Permite identificar erros como:
# - Ausência de resposta do broker.
# - Resposta MQTT inválida.
# - Falha de autenticação ou conexão.
# =========================================================

class MQTTException(Exception):
    pass


# =========================================================
# CLASSE MQTTClient
# =========================================================
#
# Representa um cliente MQTT.
#
# Contém os métodos necessários para estabelecer
# uma conexão, publicar mensagens e desconectar.
#
# A classe é utilizada pelo firmware principal
# da estação meteorológica.
# =========================================================

class MQTTClient:

    # =====================================================
    # INICIALIZAÇÃO DO CLIENTE MQTT
    # =====================================================
    #
    # Parâmetros:
    #
    # client_id:
    # Identificador do dispositivo conectado ao broker.
    #
    # server:
    # Endereço do servidor MQTT.
    #
    # port:
    # Porta utilizada na comunicação.
    # O padrão MQTT sem TLS é 1883.
    #
    # No projeto, o main.py informa explicitamente
    # a porta 8883 para conexão com TLS.
    #
    # user:
    # Nome de usuário para autenticação.
    #
    # password:
    # Senha utilizada na autenticação.
    #
    # keepalive:
    # Intervalo de manutenção da conexão MQTT.
    #
    # ssl:
    # Indica se a conexão utilizará TLS.
    #
    # ssl_params:
    # Parâmetros adicionais de configuração TLS.
    # =====================================================

    def __init__(
        self,
        client_id,
        server,
        port=1883,
        user=None,
        password=None,
        keepalive=0,
        ssl=False,
        ssl_params={}
    ):

        # Armazena o identificador do cliente.
        self.client_id = client_id

        # Armazena o endereço do broker.
        self.server = server

        # Armazena a porta configurada.
        self.port = port

        # Armazena as credenciais de autenticação.
        self.user = user
        self.password = password

        # Define o intervalo keepalive.
        self.keepalive = keepalive

        # Armazena as configurações TLS.
        self.ssl = ssl
        self.ssl_params = ssl_params

        # Inicialmente não existe um socket conectado.
        self.sock = None


    # =====================================================
    # ENVIO DE STRINGS MQTT
    # =====================================================
    #
    # O protocolo MQTT representa determinadas strings
    # utilizando:
    #
    # - Dois bytes com o comprimento da string.
    # - Os bytes correspondentes ao seu conteúdo.
    #
    # O formato !H representa um inteiro de
    # 16 bits sem sinal em ordem de rede.
    #
    # Este método é utilizado durante o envio
    # do identificador e das credenciais.
    # =====================================================

    def _send_str(self, s):

        # Envia o comprimento da string.
        self.sock.write(
            struct.pack(
                "!H",
                len(s)
            )
        )

        # Envia o conteúdo da string.
        self.sock.write(s)


    # =====================================================
    # CODIFICAÇÃO DE COMPRIMENTO VARIÁVEL
    # =====================================================
    #
    # O protocolo MQTT utiliza uma representação
    # de comprimento variável para seus pacotes.
    #
    # Cada byte utiliza sete bits para representar
    # parte do tamanho.
    #
    # O oitavo bit indica se existem mais bytes
    # compondo o comprimento.
    #
    # O processo continua até que todo o valor
    # tenha sido codificado.
    # =====================================================

    def _send_variable_length(self, value):

        while True:

            # Obtém os sete bits inferiores do valor.
            encoded = value % 128

            # Avança para a próxima parte do valor.
            value //= 128

            # Sinaliza a existência de outros bytes.
            if value > 0:
                encoded |= 128

            # Envia o byte codificado pelo socket.
            self.sock.write(
                bytes([encoded])
            )

            # Encerra quando o valor foi processado.
            if value == 0:
                break


    # =====================================================
    # CONEXÃO COM O BROKER MQTT
    # =====================================================
    #
    # Etapas:
    #
    # 1. Resolve o endereço do servidor.
    # 2. Cria um socket TCP.
    # 3. Estabelece a conexão.
    # 4. Ativa TLS quando configurado.
    # 5. Monta o pacote MQTT CONNECT.
    # 6. Envia os dados de autenticação.
    # 7. Aguarda a resposta CONNACK.
    #
    # O método retorna True quando o broker
    # confirma a conexão com código de sucesso.
    # =====================================================

    def connect(self):

        # Consulta o endereço de rede do broker
        # utilizando servidor e porta configurados.
        addr = socket.getaddrinfo(
            self.server,
            self.port
        )[0][-1]

        # Cria um socket para comunicação TCP.
        self.sock = socket.socket()

        # Estabelece a conexão TCP com o broker.
        self.sock.connect(addr)


        # -----------------------------------------
        # TLS
        # -----------------------------------------
        #
        # Caso o parâmetro ssl esteja habilitado,
        # o socket TCP passa a utilizar TLS.
        #
        # Isso protege o tráfego de comunicação
        # entre a ESP32 e o broker MQTT.
        #
        # No projeto, ssl=True é informado
        # pelo arquivo main.py.
        # -----------------------------------------

        if self.ssl:

            # Importa o módulo TLS disponível
            # no ambiente MicroPython.
            import ssl

            # Encapsula a conexão TCP com TLS.
            self.sock = ssl.wrap_socket(
                self.sock,
                server_hostname=self.server,
                **self.ssl_params
            )


        # -----------------------------------------
        # MQTT CONNECT
        # -----------------------------------------
        #
        # Monta o pacote de conexão MQTT.
        #
        # O pacote informa ao broker:
        # - Nome e versão do protocolo.
        # - Flags de conexão.
        # - Intervalo keepalive.
        # - Identificador do cliente.
        # - Credenciais, quando configuradas.
        # -----------------------------------------

        # Cria o conjunto de bytes do pacote.
        packet = bytearray()

        # Identificação do protocolo MQTT.
        packet.extend(
            b"\x00\x04MQTT"
        )

        # Nível 4 corresponde ao MQTT 3.1.1.
        packet.append(4)

        # Inicializa as flags de conexão.
        # O valor 2 habilita Clean Session.
        flags = 2

        # Indica a presença de nome de usuário.
        if self.user is not None:
            flags |= 0x80

        # Indica a presença de senha.
        if self.password is not None:
            flags |= 0x40

        # Insere as flags no pacote.
        packet.append(flags)

        # Insere o intervalo keepalive
        # utilizando dois bytes.
        packet.extend(
            struct.pack(
                "!H",
                self.keepalive
            )
        )

        # Envia o byte inicial do pacote CONNECT.
        self.sock.write(
            bytes([0x10])
        )

        # Calcula e envia o tamanho variável
        # do conteúdo restante do pacote.
        #
        # Considera os bytes do cabeçalho variável,
        # identificador do cliente e credenciais.
        self._send_variable_length(
            len(packet)
            + 2
            + len(self.client_id)
            + (
                2 + len(self.user)
                if self.user
                else 0
            )
            + (
                2 + len(self.password)
                if self.password
                else 0
            )
        )

        # Envia os dados fixos do pacote CONNECT.
        self.sock.write(packet)

        # Envia o identificador MQTT da ESP32.
        self._send_str(
            self.client_id
        )

        # Envia o usuário quando configurado.
        if self.user is not None:
            self._send_str(
                self.user
            )

        # Envia a senha quando configurada.
        if self.password is not None:
            self._send_str(
                self.password
            )


        # -----------------------------------------
        # CONNACK
        # -----------------------------------------
        #
        # O broker responde ao pacote CONNECT
        # utilizando uma mensagem CONNACK.
        #
        # A resposta contém quatro bytes.
        #
        # O primeiro byte identifica o pacote.
        # O último informa o resultado da conexão.
        #
        # Código 0 indica conexão aceita.
        # -----------------------------------------

        # Aguarda os quatro bytes da resposta.
        response = self.sock.read(4)

        # Verifica se a resposta foi recebida
        # com o comprimento esperado.
        if response is None or len(response) != 4:

            raise MQTTException(
                "Sem resposta do broker"
            )

        # Verifica o identificador do CONNACK.
        if response[0] != 0x20:

            raise MQTTException(
                "Resposta MQTT inválida"
            )

        # Verifica se o broker aceitou a conexão.
        if response[3] != 0:

            raise MQTTException(
                "Falha na conexão MQTT: "
                + str(response[3])
            )

        # Retorna sucesso após a confirmação.
        return True


    # =====================================================
    # PUBLICAÇÃO MQTT
    # =====================================================
    #
    # Responsável por enviar mensagens para
    # um tópico do broker MQTT.
    #
    # Parâmetros:
    #
    # topic:
    # Tópico de destino da publicação.
    #
    # msg:
    # Conteúdo enviado ao broker.
    #
    # retain:
    # Parâmetro recebido pela interface de publicação.
    #
    # qos:
    # Parâmetro recebido pela interface de publicação.
    #
    # O método transmite o pacote
    # com o byte fixo 0x30, correspondente a PUBLISH
    # com QoS 0 e retain desativado.
    #
    # A variável packet calcula flags, mas não é
    # utilizada na escrita final do pacote.
    #
    # O main.py utiliza os valores padrão,
    # portanto não depende de QoS 1 ou retain=True.
    # =====================================================

    def publish(
        self,
        topic,
        msg,
        retain=False,
        qos=0
    ):

        # Converte o tópico para bytes
        # caso seja recebido como string.
        if isinstance(topic, str):
            topic = topic.encode()

        # Converte a mensagem para bytes
        # caso seja recebida como string.
        if isinstance(msg, str):
            msg = msg.encode()

        # Cria um buffer para o cabeçalho.
        packet = bytearray()

        # Calcula o byte de controle MQTT
        # considerando os parâmetros recebidos.
        #
        packet.append(
            0x30
            | (qos << 1)
            | (1 if retain else 0)
        )

        # Calcula o comprimento do conteúdo:
        #
        # 2 bytes para o tamanho do tópico.
        # Bytes do nome do tópico.
        # Bytes da mensagem.
        remaining_length = (
            2
            + len(topic)
            + len(msg)
        )

        # Buffer utilizado para codificar
        # o tamanho restante do pacote.
        encoded_length = bytearray()

        value = remaining_length

        # Codifica o tamanho usando o formato
        # de comprimento variável do MQTT.
        while True:

            encoded = value % 128

            value //= 128

            # Ativa o bit de continuação
            # quando ainda existem dados.
            if value > 0:
                encoded |= 128

            # Armazena o byte codificado.
            encoded_length.append(
                encoded
            )

            # Encerra ao finalizar a codificação.
            if value == 0:
                break

        # Envia o byte fixo do pacote PUBLISH.
        #
        # O valor 0x30 corresponde à publicação
        # com QoS 0 e retain desativado.
        self.sock.write(
            bytes([0x30])
        )

        # Envia o comprimento restante do pacote.
        self.sock.write(
            encoded_length
        )

        # Envia o comprimento do tópico em dois bytes.
        self.sock.write(
            struct.pack(
                "!H",
                len(topic)
            )
        )

        # Envia o nome do tópico.
        self.sock.write(topic)

        # Envia o conteúdo da mensagem MQTT.
        self.sock.write(msg)


    # =====================================================
    # DESCONEXÃO MQTT
    # =====================================================
    #
    # Encerra a comunicação com o broker.
    #
    # Se existir um socket ativo:
    #
    # 1. Envia o pacote MQTT DISCONNECT.
    # 2. Fecha o socket.
    # 3. Limpa a referência da conexão.
    #
    # O pacote DISCONNECT utiliza os bytes
    # 0xE0 e 0x00.
    # =====================================================

    def disconnect(self):

        # Verifica se existe uma conexão associada.
        if self.sock:

            # Envia o pacote MQTT DISCONNECT.
            self.sock.write(
                b"\xE0\x00"
            )

            # Fecha a conexão de rede.
            self.sock.close()

            # Remove a referência ao socket.
            self.sock = None
