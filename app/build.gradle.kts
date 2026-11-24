plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.vitrine"
    compileSdk {
        version = release(37)
    }

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.vitrine"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Networking and JSON
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

// Lists and images
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")

// Asynchronous operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

// ViewModel and Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

// Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")
}