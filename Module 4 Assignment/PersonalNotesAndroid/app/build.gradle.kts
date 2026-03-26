// Only android-application is applied. AGP 9.0 registers the Kotlin
// extension and handles .kt compilation internally - adding kotlin-android
// would cause a duplicate 'kotlin' extension error at sync time.
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace   = "com.frcc.personalnotes"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId   = "com.frcc.personalnotes"
        minSdk          = 24
        targetSdk       = 36
        versionCode     = 1
        versionName     = "1.0"

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

    // AGP 9.0 automatically syncs the Kotlin JVM target to match
    // compileOptions, so a separate kotlinOptions block is not needed.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.coordinatorlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
