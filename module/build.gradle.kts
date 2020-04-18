import org.apache.tools.ant.taskdefs.condition.Os
import java.io.ByteArrayOutputStream

task("magisk", type = Zip::class) {
    doFirst {
        val moduleCommitCount = Integer.parseInt("git rev-list --count HEAD || echo -1"
                .execute(rootProject.rootDir))

        val clashCommitId = "git rev-parse --short HEAD || echo unknown"
                .execute(project(":clash").file("src/main/golang/clash/"))
        val clashCommitCount = Integer.parseInt("git rev-list --count HEAD || echo -1"
                .execute(project(":clash").file("src/main/golang/clash/")))

        val version = "$clashCommitId-$moduleCommitCount"
        val versionCode = "$clashCommitCount$moduleCommitCount"

        val content = file("static/module.prop").readText(Charsets.UTF_8)
                .replace("%%VERSION%%", version)
                .replace("%%VERSIONCODE%%", versionCode)

        buildDir.resolve("intermediate/magisk").mkdirs()
        buildDir.resolve("intermediate/magisk/module.prop")
                .writeText(content)

        from(buildDir.resolve("intermediate/magisk/module.prop"))
    }

    from(file("static/")) {
        exclude("module.prop")
    }

    from(project(":starter").buildDir.resolve("outputs")) {
        include("starter.jar")
        eachFile {
            path = "core/$name"
        }
    }
    from(project(":starter").buildDir.resolve("outputs/executable/")) {
        eachFile {
            path = "core/$name"
        }
    }
    from(project(":clash").buildDir.resolve("outputs")) {
        include("clash")
        eachFile {
            path = "core/$name"
        }
    }

    destinationDirectory.set(buildDir.resolve("outputs"))
    archiveFileName.set("clash-for-magisk.zip")
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
        }

        output.toString("utf-8").trim()
    }
}

gradle.projectsEvaluated {
    tasks.getByName("magisk").dependsOn(
            project(":clash").tasks.getByName("build"),
            project(":starter").tasks.getByName("extractExecutable"),
            project(":starter").tasks.getByName("createStarterJar")
    )
}
