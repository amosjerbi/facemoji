# Project Setup

## build.gradle.kts (app level)

```kotlin
plugins {
    id("com.android.application") version "8.2.0"
    id("org.jetbrains.kotlin.android") version "1.9.21"
}

android {
    namespace = "com.facemoji.app"  // Package name
    compileSdk = 35

    defaultConfig {
        applicationId = "com.facemoji.app"
        minSdk = 24        // Android 7.0+
        targetSdk = 35     // Android 15
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Must match Kotlin version
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Compose BOM - manages all Compose versions
    val composeBom = platform("androidx.compose:compose-bom:2024.08.00")
    implementation(composeBom)

    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")

    // ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ML Kit Face Detection
    implementation("com.google.mlkit:face-detection:16.1.6")

    // Image Loading (optional, for URI loading)
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

## settings.gradle.kts

```kotlin
pluginManagement {
    repositories {
        google()
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

rootProject.name = "facemoji"
include(":app")
```

## AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Photo access - Android 13+ -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

    <!-- Photo access - Android 12 and below -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.FaceMoji">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- FileProvider for sharing -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

    </application>
</manifest>
```

## res/xml/file_paths.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="shared_images" path="shared/" />
</paths>
```

## Version Compatibility Matrix

| Component | Version | Notes |
|-----------|---------|-------|
| AGP | 8.2.0 | Android Studio Hedgehog+ |
| Kotlin | 1.9.21 | Must match Compose compiler |
| Compose Compiler | 1.5.7 | For Kotlin 1.9.21 |
| Compose BOM | 2024.08.00 | Manages all Compose deps |
| ML Kit | 16.1.6 | On-device face detection |
| minSdk | 24 | Android 7.0 Nougat |
| targetSdk | 35 | Android 15 |

## Common Compatibility Issues

```
ERROR: AGP version incompatible
FIX: Downgrade to 8.2.0 or update Android Studio

ERROR: Compose compiler version mismatch
FIX: Match kotlinCompilerExtensionVersion to Kotlin version
     Kotlin 1.9.21 → Compose Compiler 1.5.7

ERROR: ML Kit not found
FIX: Ensure google() in repositories
```
