
/*
 * settings.gradle.kts — Estação Meteorológica IoT
 *
 * Arquivo de configuração global do projeto Android.
 *
 * Responsável por definir:
 * - Os repositórios utilizados para localizar plugins.
 * - As configurações de resolução de dependências.
 * - O nome do projeto.
 * - Os módulos incluídos na compilação.
 */

// Configura os repositórios utilizados pelo Gradle
// para localizar e baixar os plugins necessários ao projeto.
pluginManagement {

    repositories {

        // Repositório oficial do Google.
        // O filtro restringe os grupos de plugins pesquisados aqui.
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        // Repositório Maven Central, utilizado por diversas
        // bibliotecas e ferramentas do ecossistema Java/Kotlin.
        mavenCentral()

        // Portal oficial de plugins do Gradle.
        gradlePluginPortal()
    }
}

// Plugins aplicados ao processo de configuração do Gradle.
plugins {

    // Plugin Foojay Resolver Convention.
    // Auxilia o Gradle a localizar instalações do Java
    // compatíveis com as toolchains configuradas no projeto.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Configura o gerenciamento centralizado das dependências.
dependencyResolutionManagement {

    // Impede que os módulos adicionem repositórios próprios.
    // Os repositórios devem ser definidos neste arquivo.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {

        // Repositório do Google para bibliotecas Android.
        google()

        // Repositório Maven Central para bibliotecas Java/Kotlin.
        mavenCentral()
    }
}

// Nome principal do projeto exibido pelo Gradle.
rootProject.name = "EstacaoMeteorologica"

// Inclui o módulo responsável pelo aplicativo Android.
include(":app")
