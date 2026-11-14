
/*
 * build.gradle.kts — Projeto EstacaoMeteorologica
 *
 * Arquivo principal de configuração do Gradle.
 *
 * Define os plugins compartilhados entre os módulos
 * do projeto Android.
 *
 * As versões dos plugins são gerenciadas pelo catálogo
 * gradle/libs.versions.toml.
 *
 * Este arquivo pertence à raiz do projeto, enquanto
 * app/build.gradle.kts contém as configurações
 * específicas do aplicativo Android.
 */

// Declara os plugins disponíveis para os módulos do projeto.
plugins {

    // Plugin responsável pela compilação de aplicativos Android.
    //
    // O identificador e a versão são definidos no
    // catálogo de versões do Gradle.
    //
    // apply false: disponibiliza o plugin sem aplicá-lo
    // diretamente ao projeto raiz.
    alias(libs.plugins.android.application) apply false

    // Plugin do Kotlin para integração com Jetpack Compose.
    //
    // É utilizado na compilação dos componentes de interface
    // desenvolvidos com Compose.
    //
    // apply false: o plugin será aplicado somente
    // pelos módulos que o declararem.
    alias(libs.plugins.kotlin.compose) apply false
}
