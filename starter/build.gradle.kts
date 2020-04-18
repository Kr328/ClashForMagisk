plugins {
    id("com.android.application")
    id("kotlin-android")
    kotlin("plugin.serialization")
}

val kotlinVersion: String = "1.3.61"
val jacksonVersion: String = "2.10.1"

android {
    compileSdkVersion(29)
    buildToolsVersion = "29.0.3"

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
    implementation("androidx.core:core-ktx:1.3.0-alpha01")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
    implementation("com.charleskorn.kaml:kaml:0.15.0")
}

repositories {
    mavenCentral()
}

task("createStarterJar", type = Jar::class) {
    from(zipTree(buildDir.resolve("outputs/apk/release/starter-release-unsigned.apk")))
    include("META-INF/", "kotlin/", "classes.dex")

    destinationDirectory.set(buildDir.resolve("outputs"))
    archiveFileName.set("starter.jar")
}

task("extractExecutable", type = Copy::class) {
    from(zipTree(buildDir.resolve("outputs/apk/release/starter-release-unsigned.apk")))
    include("lib/arm64-v8a/")
    eachFile {
        when {
            name.endsWith("libsetuidgid.so") ->  {
                path = "setuidgid"
            }
            name.endsWith("libdaemonize.so") -> {
                path = "daemonize"
            }
        }
    }

    destinationDir = buildDir.resolve("outputs/executable/")
}

afterEvaluate {
    val assembleRelease = tasks.getByName("assembleRelease")

    val createStarterJar = tasks.getByName("createStarterJar").apply {
        dependsOn += assembleRelease
    }
    val extractExecutable = tasks.getByName("extractExecutable").apply {
        dependsOn += assembleRelease
    }
}