
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.jrdev.systemmanager"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.jrdev.systemmanager"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures { //hecho del video
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    //ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    //LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    //Codigo de barras ZXING
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") //o 4.3.0
    //Libreria HoloGraph
    implementation("org.quanqi:android-holo-graph:0.1.0")
    //ITextPDF5
    implementation("com.itextpdf:itextg:5.5.10")//o5.5.10
    //Toasty
    implementation("com.github.GrenderG:Toasty:1.5.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.core.ktx)//1.5.2

    //db visual studio ia
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation(libs.cardview)
    //implementation(libs.cardview.v7)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}