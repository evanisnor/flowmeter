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
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("io.nlopez.compose.rules:ktlint:0.3.11")
  }
}

rootProject.name = "flowmeter"
include(":app")
