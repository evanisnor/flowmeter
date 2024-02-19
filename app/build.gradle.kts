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
    versionCode = 1
    versionName = "1.0"

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
  implementation(libs.bundles.circuit)
  implementation(libs.sqldelight.driver)
  implementation(libs.sqldelight.adapters)

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
