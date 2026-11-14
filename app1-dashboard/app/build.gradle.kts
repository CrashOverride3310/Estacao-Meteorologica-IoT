
/*
 * build.gradle.kts — Módulo app
 *
 * Arquivo responsável pela configuração de compilação
 * do App1 — Dashboard da Estação Meteorológica IoT.
 *
 * Define plugins, versões do Android, compatibilidade
 * com Java e dependências utilizadas pelo aplicativo.
 */

// Plugins utilizados para construir o aplicativo Android
// e habilitar a interface desenvolvida com Jetpack Compose.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Configurações gerais do aplicativo Android.
android {

    // Identifica o namespace do projeto.
    namespace = "com.example.estacaometeorologica"

    // Versão do Android SDK utilizada na compilação.
    compileSdk {
        version = release(37)
    }

    // Configurações padrão do aplicativo.
    defaultConfig {

        // Identificador exclusivo do aplicativo Android.
        applicationId = "com.example.estacaometeorologica"

        // Versão mínima do Android necessária para executar o App1.
        minSdk = 26

        // Versão do Android SDK utilizada como referência
        // para o comportamento do aplicativo.
        targetSdk = 37

        // Código interno de versionamento do aplicativo.
        versionCode = 1

        // Versão apresentada ao usuário.
        versionName = "1.0"

        // Classe responsável pela execução de testes instrumentados.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Configurações dos tipos de compilação.
    buildTypes {

        // Configuração utilizada para gerar a versão de distribuição.
        release {

            // Mantém a otimização de código desativada,
            // conforme a configuração original do projeto.
            optimization {
                enable = false
            }
        }
    }

    // Define a compatibilidade do código Java utilizado
    // durante a compilação do aplicativo.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Habilita a utilização do Jetpack Compose,
    // biblioteca responsável pela construção da interface Android.
    buildFeatures {
        compose = true
    }
}

// Bibliotecas e dependências utilizadas pelo App1.
dependencies {

    // BOM (Bill of Materials) do Jetpack Compose.
    // Ajuda a manter versões compatíveis entre suas bibliotecas.
    implementation(platform(libs.androidx.compose.bom))

    // Integra o Jetpack Compose ao ciclo de vida da Activity.
    implementation(libs.androidx.activity.compose)

    // Componentes visuais baseados no Material Design 3.
    implementation(libs.androidx.compose.material3)

    // Biblioteca principal da interface Jetpack Compose.
    implementation(libs.androidx.compose.ui)

    // Recursos gráficos utilizados pela interface Compose.
    implementation(libs.androidx.compose.ui.graphics)

    // Permite visualizar componentes Compose no editor.
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Extensões Kotlin para funcionalidades do Android.
    implementation(libs.androidx.core.ktx)

    // Integração do ciclo de vida Android com recursos Kotlin.
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Biblioteca utilizada para testes unitários.
    testImplementation(libs.junit)

    // Dependências de testes instrumentados do Jetpack Compose.
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Biblioteca Espresso para testes de interface Android.
    androidTestImplementation(libs.androidx.espresso.core)

    // Integração do JUnit com testes instrumentados Android.
    androidTestImplementation(libs.androidx.junit)

    // Recursos auxiliares de testes Compose em modo debug.
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Ferramentas de inspeção e depuração da interface Compose.
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Retrofit: biblioteca para realizar requisições HTTP
    // e consumir os endpoints REST do backend.
    implementation(libs.retrofit)

    // Conversor Gson utilizado pelo Retrofit para transformar
    // respostas JSON em objetos Kotlin.
    implementation(libs.converter.gson)

    // osmdroid: biblioteca para exibir mapas OpenStreetMap
    // e marcadores da localização GPS da estação.
    implementation("org.osmdroid:osmdroid-android:6.1.20")
}
