plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.enggo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.enggo"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val storeFilePath = project.findProperty("ENGGO_STORE_FILE") as String
            storeFile = file(storeFilePath)
            storePassword = project.findProperty("ENGGO_STORE_PASSWORD") as String
            keyAlias = project.findProperty("ENGGO_KEY_ALIAS") as String
            keyPassword = project.findProperty("ENGGO_KEY_PASSWORD") as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.kizitonwose.calendar:view:2.9.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
}
