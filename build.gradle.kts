buildscript {
    repositories {
        google()
        jcenter()
        maven { url = java.net.URI("https://dl.bintray.com/kr328/riru-gradle-tools") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.1")
        classpath("com.github.kr328:riru-gradle-tools:1.0-alpha08")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        
        maven { url = java.net.URI("https://oss.sonatype.org/content/groups/public/") }
    }
}

task("clean", type = Delete::class) {
    delete = setOf(rootProject.buildDir)
}
