import org.apache.tools.ant.taskdefs.condition.Os
import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("rirutools.magisk")
}

magisk {
    output = "$buildDir/outputs/clash-for-magisk.zip"
    
    zip.map(buildDir.resolve("intermediate/magisk/"), "/")
    zip.map(buildDir.resolve("intermediate/starter/classes.dex"), "/core/starter.dex")
    zip.map(buildDir.resolve("intermediate/starter/lib/arm64-v8a/libsetuidgid.so"), "/core/setuidgid")
    zip.map(buildDir.resolve("intermediate/starter/lib/arm64-v8a/libdaemonize.so"), "/core/daemonize")
    zip.map(project(":clash").buildDir.resolve("outputs/clash"), "/core/clash")
}

repositories {
    mavenCentral()
}

dependencies {
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

fun String.execute(pwd: File): String {
    return ByteArrayOutputStream().use { output ->
        exec {
            if ( Os.isFamily(Os.FAMILY_WINDOWS) )
                commandLine("cmd.exe", "/c", this@execute)
            else
                commandLine("bash", "-c", this@execute)

            workingDir = pwd
            standardOutput = output
            errorOutput = output
        }.assertNormalExitValue()

        output.toString("utf-8")
    }
}

task("extractStarter", type = Copy::class) {
    from(zipTree(project(":starter").buildDir.resolve("outputs/apk/release/starter-release-unsigned.apk")))
    into(buildDir.resolve("intermediate/starter/"))
}

task("setupMagiskFiles", type = Copy::class) {
    from("src/main/raw/magisk")
    into("$buildDir/intermediate/magisk/")

    doLast {
        val moduleCommitCount = Integer.parseInt("git rev-list --count HEAD || echo -1".execute(rootProject.rootDir).trim())

        val clashCommitId = "git rev-parse --short HEAD || echo unknown".execute(project(":clash").file("src/main/golang/clash/")).trim()
        val clashCommitCount = Integer.parseInt("git rev-list --count HEAD || echo -1".execute(project(":clash").file("src/main/golang/clash/")).trim())

        val version = "$clashCommitId-$moduleCommitCount"
        val versionCode = "$clashCommitCount$moduleCommitCount"

        val file = File(buildDir, "intermediate/magisk/module.prop")
        val content = file.readText(Charsets.UTF_8).replace("%%VERSION%%", version).replace("%%VERSIONCODE%%", versionCode)

        file.writeText(content)
    }
}

project(":starter").tasks.whenTaskAdded {
    if ( name.contains("assembleRelease") )
        tasks.getByName("extractStarter").dependsOn(this)
}

afterEvaluate {
    tasks.getByName("magiskModule").dependsOn(
            tasks.getByName("setupMagiskFiles"),
            tasks.getByName("extractStarter"),
            project(":clash").tasks.getByName("build"))
}