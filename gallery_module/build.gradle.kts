import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.volpis.gallery_module"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/ASL2.0")
            excludes.add("META-INF/INDEX.LIST")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.glide)
    kapt(libs.lifecycle.compiler)

    implementation (libs.play.services.auth)

    implementation(libs.google.api.client.android) {
        exclude(group = "org.apache.httpcomponents")
    }

    implementation(libs.google.api.services.drive) {
        exclude(group = "org.apache.httpcomponents")
    }

    implementation(libs.google.http.client.gson) {
        exclude(group = "org.apache.httpcomponents")
    }

    implementation(libs.google.auth.library.oauth2.http) {
        exclude(group = "org.apache.httpcomponents")
    }

    implementation(libs.firebase.bom)

    implementation(libs.okhttp)

}