buildscript {
    val kotlinVersion = "1.3.61"

    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath(kotlin("serialization", version = kotlinVersion))
    }
}

project(":module") {
    dependencies {
        project(":starter")
        project(":clash")
    }
}

project(":starter") {
    repositories {
        google()
        jcenter()
    }
}

project(":clash") {
}

task("clean", type = Delete::class) {
    delete = setOf(rootProject.buildDir)
}