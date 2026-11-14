
/*
 * settings.gradle.kts — App2 LocalizacaoGPS
 * Projeto: Estação Meteorológica IoT
 *
 * Arquivo responsável pelas configurações gerais
 * da estrutura do projeto Android.
 *
 * Define:
 * - Os repositórios utilizados para localizar plugins.
 * - O plugin de resolução de ferramentas Java.
 * - Os repositórios utilizados pelas dependências.
 * - O nome principal do projeto.
 * - Os módulos incluídos na compilação.
 */

// Configura onde o Gradle deve procurar os plugins
// utilizados durante a compilação do aplicativo.
pluginManagement {

    repositories {

        // Repositório oficial do Google.
        // Os filtros restringem os grupos pesquisados
        // neste repositório.
        google {
            content {

                // Plugins pertencentes ao grupo Android.
                includeGroupByRegex("com\\.android.*")

                // Plugins pertencentes aos grupos Google.
                includeGroupByRegex("com\\.google.*")

                // Bibliotecas e plugins do grupo AndroidX.
                includeGroupByRegex("androidx.*")
            }
        }

        // Repositório Maven Central, utilizado por
        // bibliotecas do ecossistema Java e Kotlin.
        mavenCentral()

        // Portal oficial de plugins do Gradle.
        gradlePluginPortal()
    }
}

// Plugins aplicados à configuração global do projeto.
plugins {

    // Foojay Resolver Convention.
    //
    // Auxilia o Gradle a localizar ou provisionar
    // instalações Java compatíveis com as toolchains.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Configura como as dependências dos módulos serão resolvidas.
dependencyResolutionManagement {

    // Impede que os módulos declarem repositórios próprios.
    // Os repositórios são centralizados neste arquivo.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {

        // Repositório oficial do Google para bibliotecas Android.
        google()

        // Repositório Maven Central para bibliotecas Java/Kotlin.
        mavenCentral()
    }
}

// Define o nome do projeto exibido pelo Gradle e Android Studio.
rootProject.name = "LocalizacaoGPS"

// Inclui o módulo principal do aplicativo Android.
include(":app")
