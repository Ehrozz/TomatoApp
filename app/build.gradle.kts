plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

/**
 * Versioning
 * - versionName: semantic version (MAJOR.MINOR.PATCH) + optional pre-release suffix (alpha/beta)
 * - versionCode: monotonically increasing integer (baseSemVerCode * 1000 + buildNumber)
 *
 * Provide an optional CI build number via:
 * - environment variable: BUILD_NUMBER
 * - Gradle property: -PbuildNumber=123
 */
val versionMajor = 1
val versionMinor = 0
val versionPatch = 0

fun Project.buildNumberOrNull(): Int? {
    val fromEnv = System.getenv("BUILD_NUMBER")?.trim()?.takeIf { it.isNotEmpty() }
    val fromProp = (findProperty("buildNumber") as String?)?.trim()?.takeIf { it.isNotEmpty() }
    return (fromProp ?: fromEnv)?.toIntOrNull()
}

android {
    namespace = "com.android.tomatoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.android.tomatoapp"
        minSdk = 24
        targetSdk = 36

        val baseSemVerCode = (versionMajor * 10_000) + (versionMinor * 100) + versionPatch
        val buildNumber = project.buildNumberOrNull() ?: 0 // 0 keeps stable releases clean
        versionCode = (baseSemVerCode * 1000) + buildNumber

        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Stable / production releases: keep versionName clean (e.g., 1.2.0)
            versionNameSuffix = ""
        }
        debug {
            isMinifyEnabled = false
            // Alpha / internal builds: clearly marked
            versionNameSuffix = "-alpha"
        }
        // Beta builds: release-like but debuggable and clearly labeled
        create("beta") {
            initWith(getByName("release"))
            isDebuggable = true
            // Minification is disabled automatically for debuggable builds; make intent explicit.
            isMinifyEnabled = false
            isShrinkResources = false
            // Use debug signing so local/CI builds work without release keystore
            signingConfig = signingConfigs.getByName("debug")

            val buildNumber = project.buildNumberOrNull()
            versionNameSuffix = if (buildNumber != null) "-beta.$buildNumber" else "-beta"
            matchingFallbacks += listOf("release")
        }
    }
    
    // Configure APK naming with version information
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val buildType = variant.buildType.name
            val versionName = variant.versionName
            val versionCode = variant.versionCode
            
            // Example:
            // TomatoApp_v1.2.0_(Build_45000)_release.apk
            // TomatoApp_v1.2.0-beta.45_(Build_1200045)_beta.apk
            output.outputFileName = "TomatoApp_v${versionName}_(Build_${versionCode})_${buildType}.apk"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    
    // Explicitly configure source sets to ensure IDE classpath recognition
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.firebase.database)
    implementation(libs.media3.common)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.prolificinteractive:material-calendarview:1.4.3")

    val camerax_version = "1.3.3"

    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")

    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")

    implementation("org.tensorflow:tensorflow-lite:2.12.0")

    // Location services for fetching device location (weather)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Room (for local WorkProgram storage)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // MPAndroidChart for analytics charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Jetpack Compose - Core
    implementation("androidx.compose.ui:ui:1.6.4")
    implementation("androidx.compose.ui:ui-graphics:1.6.4")
    implementation("androidx.compose.ui:ui-text:1.6.4")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.4")
    
    // Compose Activity
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Compose Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Compose Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.4")

}