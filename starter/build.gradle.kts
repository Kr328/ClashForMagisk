plugins {
    id("com.android.application")
}

android {
    compileSdkVersion(29)
    buildToolsVersion = "29.0.2"

    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"

        externalNativeBuild {
            cmake {
                abiFilters.add("arm64-v8a")
            }
        }
    }

    buildTypes {
        maybeCreate("release").apply {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    externalNativeBuild {
        cmake {
            setPath(file("src/main/cpp/CMakeLists.txt"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    compileOnly(project(":hideapi"))
    implementation("org.yaml:snakeyaml:1.25-SNAPSHOT")
}

tasks.getByName("clean", type = Delete::class) {
    delete += setOf(file(".cxx"), file(".externalNativeBuild"))
}