plugins {
    id("com.android.application")
    id("kotlin-android")
    kotlin("plugin.serialization")
}

val kotlinVersion: String by project
val kotlinSerializationVersion: String by project
val kamlVersion: String by project

android {
    compileSdkVersion(30)
    buildToolsVersion = "29.0.3"

    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"

        externalNativeBuild {
            cmake {
                abiFilters.add("arm64-v8a")
            }
        }
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    compileOnly(project(":hideapi"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinSerializationVersion")
    implementation("com.charleskorn.kaml:kaml:$kamlVersion")
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

    tasks["createStarterJar"].dependsOn += assembleRelease
    tasks["extractExecutable"].dependsOn += assembleRelease
}