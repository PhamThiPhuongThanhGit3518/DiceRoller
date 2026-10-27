pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            name = "LibAdsMeta1"
            url = uri("https://maven.pkg.github.com/tokiiapp/Lib-Ads-Meta-1")
            credentials {
                username = "tokiiapp"
                password = "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"
            }
        }
        maven { url = uri("https://artifact.bytedance.com/repository/pangle") }
    }
}

rootProject.name = "Dice Roller"
include(":app")
 