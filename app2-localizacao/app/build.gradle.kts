
/*
 * build.gradle.kts — Módulo app
 *
 * App2 — LocalizacaoGPS
 * Projeto Estação Meteorológica IoT
 *
 * Responsável por configurar a compilação do aplicativo Android,
 * as bibliotecas de localização GPS e a comunicação MQTT.
 *
 * As credenciais MQTT são obtidas de variáveis de ambiente,
 * evitando que seus valores sejam escritos diretamente
 * neste arquivo de configuração.
 */

// Obtém o nome de usuário MQTT da variável de ambiente MQTT_USERNAME.
// Caso a variável não esteja configurada, utiliza uma string vazia.
val mqttUsername = providers.environmentVariable("MQTT_USERNAME").orElse("").get()

// Obtém a senha MQTT da variável de ambiente MQTT_PASSWORD.
// Caso a variável não esteja configurada, utiliza uma string vazia.
val mqttPassword = providers.environmentVariable("MQTT_PASSWORD").orElse("").get()

// Plugins utilizados para compilar o aplicativo Android
// e habilitar a interface desenvolvida com Jetpack Compose.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Configurações gerais do aplicativo Android.
android {

    // Namespace utilizado pelas classes do aplicativo.
    namespace = "com.example.localizacaogps"

    // Versão do Android SDK utilizada para compilar o projeto.
    compileSdk {
        version = release(37)
    }

    // Configurações padrão do aplicativo.
    defaultConfig {

        // Identificador exclusivo do aplicativo Android.
        applicationId = "com.example.localizacaogps"

        // Versão mínima do Android necessária para executar o App2.
        minSdk = 26

        // Versão do Android SDK usada como referência
        // para o comportamento do aplicativo.
        targetSdk = 37

        // Código interno de versionamento do aplicativo.
        versionCode = 1

        // Versão apresentada ao usuário.
        versionName = "1.0"

        // Classe utilizada na execução de testes instrumentados.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Gera a constante MQTT_USERNAME na classe BuildConfig.
        // O valor é obtido da variável de ambiente durante a compilação.
        buildConfigField("String", "MQTT_USERNAME", "\"$mqttUsername\"")

        // Gera a constante MQTT_PASSWORD na classe BuildConfig.
        // O valor é obtido da variável de ambiente durante a compilação.
        buildConfigField("String", "MQTT_PASSWORD", "\"$mqttPassword\"")
    }

    // Define as configurações dos tipos de compilação.
    buildTypes {

        // Configurações utilizadas na versão de distribuição.
        release {

            // Mantém a otimização de código desativada,
            // conforme a configuração original do projeto.
            optimization {
                enable = false
            }
        }
    }

    // Define a compatibilidade com a versão 11 da linguagem Java.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Habilita os recursos necessários para o aplicativo.
    buildFeatures {

        // Ativa a interface desenvolvida com Jetpack Compose.
        compose = true

        // Permite gerar a classe BuildConfig,
        // utilizada para disponibilizar as configurações MQTT.
        buildConfig = true
    }
}

// Bibliotecas e dependências utilizadas pelo App2.
dependencies {

    // BOM (Bill of Materials) do Jetpack Compose.
    // Gerencia versões compatíveis das bibliotecas Compose.
    implementation(platform(libs.androidx.compose.bom))

    // Integra o Jetpack Compose às Activities Android.
    implementation(libs.androidx.activity.compose)

    // Componentes visuais do Material Design 3.
    implementation(libs.androidx.compose.material3)

    // Biblioteca principal do Jetpack Compose.
    implementation(libs.androidx.compose.ui)

    // Recursos gráficos utilizados pela interface Compose.
    implementation(libs.androidx.compose.ui.graphics)

    // Permite visualizar componentes Compose no Android Studio.
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Extensões Kotlin para funcionalidades do Android.
    implementation(libs.androidx.core.ktx)

    // Integra recursos Kotlin ao ciclo de vida Android.
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Biblioteca utilizada para testes unitários.
    testImplementation(libs.junit)

    // BOM do Compose utilizado durante testes instrumentados.
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Integração do Jetpack Compose com JUnit4.
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Espresso: biblioteca para testes de interface Android.
    androidTestImplementation(libs.androidx.espresso.core)

    // JUnit para testes executados em dispositivos Android.
    androidTestImplementation(libs.androidx.junit)

    // Recursos de suporte aos testes Compose no modo debug.
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Ferramentas para inspeção e depuração de interfaces Compose.
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Google Play Services Location.
    //
    // Disponibiliza o Fused Location Provider, utilizado
    // para obter latitude e longitude do smartphone.
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Comunicação MQTT.
    //
    // Eclipse Paho MQTT v3 é responsável por estabelecer
    // a conexão com o HiveMQ Cloud e publicar mensagens
    // contendo as coordenadas GPS da estação.
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
}
