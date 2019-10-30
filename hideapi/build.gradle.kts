plugins {
    id("java")
    id("rirutools.dex")
}

dex {
    output = "api.jar"

    platform = "android-29"
    buildTools = "29.0.1"
}

repositories {
    mavenCentral()
}

dependencies {
}