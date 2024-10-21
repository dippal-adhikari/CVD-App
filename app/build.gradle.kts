plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}


android {
    namespace = "com.example.cvd_draft_1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cvd_draft_1"
        minSdk = 24
        targetSdk = 34
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

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }


}

dependencies {
    // AndroidX and Firebase dependencies
//    implementation("androidx.appcompat:appcompat:1.3.1")
//    implementation("com.google.android.material:material:1.4.0")
//    implementation("androidx.activity:activity-ktx:1.3.1")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("androidx.cardview:cardview:1.0.0")

    // Firebase dependencies
    implementation("com.google.firebase:firebase-auth:21.0.1")
    implementation("com.google.firebase:firebase-firestore:24.0.1")
    implementation("com.google.firebase:firebase-storage:20.0.1")

    // Retrofit for network requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation(libs.activity)


    //amulya's dependencies

    // Additional dependencies
    implementation("androidx.activity:activity-ktx:1.9.1")  // Activity KTX
    implementation("com.google.android.gms:play-services-auth:20.4.1")  // Google Sign-In
    implementation("com.github.bumptech.glide:glide:4.16.0")



    implementation("androidx.appcompat:appcompat:1.6.1")  // AppCompat library for backward compatibility
    implementation("com.google.android.material:material:1.8.0")  // Material Design components
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")  // ConstraintLayout for flexible UI design

    // CameraX dependencies (using stable versions)
    implementation("androidx.camera:camera-core:1.1.0")  // Core CameraX library
    implementation("androidx.camera:camera-camera2:1.1.0")  // Camera2 implementation for CameraX
    implementation("androidx.camera:camera-lifecycle:1.1.0")  // Lifecycle-aware components for CameraX
    implementation("androidx.camera:camera-view:1.1.0")  // View class for CameraX
    implementation("androidx.camera:camera-video:1.1.0")  // Video recording support for CameraX
    implementation("androidx.camera:camera-extensions:1.1.0")
    implementation(libs.vision.common)
    implementation(libs.segmentation.selfie)  // Extensions for CameraX

    testImplementation("junit:junit:4.13.2")  // JUnit for unit testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")  // AndroidX JUnit extension for Android tests
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")  // Espresso for UI testing

    implementation ("androidx.camera:camera-core:1.0.0")
    implementation ("androidx.camera:camera-camera2:1.0.0")
    implementation ("androidx.camera:camera-lifecycle:1.0.0")
    implementation ("androidx.camera:camera-video:1.0.0")
    implementation ("com.google.android.material:material:1.4.0")

    implementation ("com.arthenica:ffmpeg-kit-full:4.5.LTS") //ffmpeg for video editing

    //

}

// Resolution Strategy to ensure consistent Kotlin version across the project
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("1.9.20")  // Ensure all Kotlin dependencies use version 1.9.20
        }
    }
}
