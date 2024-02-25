import java.io.ByteArrayOutputStream
import java.time.Instant

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.kotlin.kapt)
  alias(libs.plugins.jetbrains.kotlin.android)
  alias(libs.plugins.jetbrains.kotlin.parcelize)
  alias(libs.plugins.anvil)
  alias(libs.plugins.sqldelight)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.evanisnor.flowmeter"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.evanisnor.flowmeter"
    minSdk = 29
    targetSdk = 34
    versionCode =
      countGitCommits().also {
        println("Current Version Code: $it")
      }
    versionName =
      generateVersionNumber().also {
        println("Current Version: $it")
      }

    buildConfigField("Long", "BUILD_TIMESTAMP", "${Instant.now().toEpochMilli()}L")

    buildFeatures {
      buildConfig = true
    }

    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_20
    targetCompatibility = JavaVersion.VERSION_20
  }
  kotlinOptions {
    jvmTarget = "20"
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.9"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }


  sqldelight {
    databases {
      create("Database") {
        packageName.set("com.evanisnor.flowmeter")
      }
    }
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.work)
  implementation(libs.bundles.circuit)
  implementation(libs.sqldelight.driver)
  implementation(libs.sqldelight.adapters)
  implementation(libs.datastore)
  implementation(libs.timber)

  implementation(libs.dagger)
  kapt(libs.dagger.compiler)
  ksp(libs.circuit.codegen)

  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(libs.truth)
  testImplementation(libs.kotlin.coroutines.test)

  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register<Copy>("copySupplementalText") {
  val rawDirectory = "${project.projectDir}/src/main/res/raw"
  from(layout.buildDirectory.file("${project.rootProject.rootDir}/PRIVACY.md"))
  into(layout.buildDirectory.dir(rawDirectory))
  doLast {
    file("$rawDirectory/PRIVACY.md").renameTo(file("$rawDirectory/privacy.md"))
  }
}
afterEvaluate {
  tasks.getByName("preBuild").dependsOn("copySupplementalText")
}

/**
 * Handy function for executing shell commands and getting the output
 */
fun String.execute(vararg args: String = emptyArray()): String {
  val outputStream = ByteArrayOutputStream()
  project.exec {
    workingDir = projectDir
    environment("TZ", "Etc/UTC")
    commandLine(mutableListOf(this@execute).apply { addAll(args) })
    standardOutput = outputStream
  }
  return String(outputStream.toByteArray()).trim()
}

fun generateVersionNumber(): String {
  val year = "date".execute("+\"%Y\"").trim('"')
  val month = "date".execute("+\"%m\"").trim('"')
  val commitsThisMonth = countGitCommits(since = "$year-$month-01 00:00:00")
  return "$year.$month.$commitsThisMonth"
}

fun countGitCommits(since: String? = null) =
  if (!since.isNullOrBlank()) {
    "git".execute("rev-list", "--count", "main", "--since=\"$since\"")
  } else {
    "git".execute("rev-list", "--count", "main")
  }.toInt()
