import org.apache.tools.ant.taskdefs.condition.Os
import java.io.ByteArrayOutputStream
import java.io.FileReader
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// Full custom build for clash

object Clang {
    const val COMPILER_PREFIX = "aarch64-linux-android21-"
    const val LD_PREFIX = "aarch64-linux-android-"
}

fun String.exe(): String {
    return if ( Os.isFamily(Os.FAMILY_WINDOWS) )
        "$this.exe"
    else
        this
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

fun ndkHost(): String {
    return when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> "linux-x86_64"
        Os.isFamily(Os.FAMILY_MAC) -> "darwin-x86_64"
        Os.isFamily(Os.FAMILY_UNIX) -> "linux-x86_64"
        else -> throw GradleScriptException("Unsupported Build OS ${System.getenv("os.name")}", IOException())
    }
}

fun clashVersion(): String =
        "git describe --tags || echo \"unknown version\"".execute(file("src/main/golang/clash"))
fun buildTime(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

task("build", type = Exec::class) {
    doFirst {
        try {
            val ndk = Properties().apply {
                FileReader(rootProject.file("local.properties")).use {
                    load(it)
                }
            }.getProperty("ndk.dir") ?: throw IOException("ndk.dir not found in local.properties")

            val compilerDir = file(ndk).resolve("toolchains/llvm/prebuilt/${ndkHost()}/bin")

            environment.put("GOARCH", "arm64")
            environment.put("GOOS", "android")
            environment.put("CGO_ENABLED", "1")
            environment.put("GOPATH", buildDir.resolve("intermediate/gopath").absolutePath)
            environment.put("CXX", compilerDir.resolve(Clang.COMPILER_PREFIX + "clang++".exe()).absolutePath)
            environment.put("CC", compilerDir.resolve(Clang.COMPILER_PREFIX + "clang".exe()).absolutePath)
            environment.put("LD", compilerDir.resolve(Clang.LD_PREFIX + "ld".exe()).absolutePath)
        } catch (e: IOException) {
            throw GradleScriptException("Unable to create build environment", e)
        }
    }

    onlyIf {
        val current = "git rev-parse --short HEAD".execute(file("src/main/golang/clash"))

        val last = buildDir.resolve("intermediate/last_build_commit").takeIf(File::exists)?.readText(Charsets.UTF_8)

        last != current
    }

    doLast {
        val current = "git rev-parse --short HEAD || echo unknown".execute(file("src/main/golang/clash"))

        buildDir.resolve("intermediate").apply(File::mkdirs).resolve("last_build_commit").writeText(current)

        // Workaround for unable to clean gopath
        if (Os.isFamily(Os.FAMILY_UNIX) || Os.isFamily(Os.FAMILY_MAC)) {
            ("chmod -R 700 \"" + buildDir.resolve("intermediate/gopath").absolutePath + "\" || echo \"Change Permission Failure\"").execute(buildDir)
        }
    }

    workingDir = file("src/main/golang/clash")
    commandLine = listOf("go".exe(), "build", "-ldflags", "-X \"github.com/Dreamacro/clash/constant.Version=${clashVersion()}\" -X \"github.com/Dreamacro/clash/constant.BuildTime=${buildTime()}\" -w -s", "-o", file("$buildDir/outputs/clash").absolutePath)
    standardOutput = System.out
    errorOutput = System.err
}

task("clean", type = Delete::class) {
    delete = setOf(buildDir)
}
