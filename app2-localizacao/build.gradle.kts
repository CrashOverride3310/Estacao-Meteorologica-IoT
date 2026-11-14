
/*
 * build.gradle.kts — Projeto LocalizacaoGPS
 * Estação Meteorológica IoT — App2
 *
 * Arquivo de configuração principal do Gradle.
 *
 * Responsável por declarar os plugins que podem ser
 * utilizados pelos módulos do projeto Android.
 *
 * As versões dos plugins são definidas no arquivo
 * gradle/libs.versions.toml.
 *
 * Este arquivo pertence à raiz do projeto.
 * As configurações específicas do aplicativo estão
 * no arquivo app/build.gradle.kts.
 */

// Declara os plugins disponíveis para os módulos do projeto.
plugins {

    // Plugin Android Application.
    //
    // Responsável por disponibilizar as ferramentas
    // necessárias para compilar aplicativos Android.
    //
    // O identificador e a versão são definidos
    // no catálogo de versões do Gradle.
    //
    // apply false significa que o plugin é declarado
    // aqui, mas não é aplicado diretamente ao projeto raiz.
    alias(libs.plugins.android.application) apply false

    // Plugin Kotlin Compose.
    //
    // Responsável pela integração do compilador Kotlin
    // com a interface desenvolvida em Jetpack Compose.
    //
    // apply false permite que o plugin seja aplicado
    // somente pelos módulos que necessitarem dele.
    alias(libs.plugins.kotlin.compose) apply false
}
