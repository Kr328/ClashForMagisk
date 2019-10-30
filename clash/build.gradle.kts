import org.apache.tools.ant.taskdefs.condition.Os
import java.io.ByteArrayOutputStream
import java.io.FileReader
import java.io.IOException
import java.util.*

// Full custom build for clash

object Clang {
    const val COMPILER_PREFIX = "aarch64-linux-android23-"
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
        }.assertNormalExitValue()

        output.toString("utf-8")
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

task("build", type = Exec::class) {
    doFirst {
        try {
            val ndk = Properties().apply {
                FileReader(rootProject.file("local.properties")).use {
                    load(it)
                }
            }.getProperty("ndk.dir") ?: throw IOException("ndk.dir not found in local.properties")

            val compilerDir = file(ndk).resolve("toolchains/llvm/prebuilt/linux-x86_64/bin/")

            environment.put("GOARCH", "arm64")
            environment.put("GOOS", "android")
            environment.put("CGO_ENABLED", "1")
            environment.put("GOPATH", file("$buildDir/intermediate/gopath/").absolutePath)
            environment.put("CXX", compilerDir.resolve(Clang.COMPILER_PREFIX + "clang++".exe()))
            environment.put("CC", compilerDir.resolve(Clang.COMPILER_PREFIX + "clang".exe()))
            environment.put("LD", compilerDir.resolve(Clang.LD_PREFIX + "ld".exe()))
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
    }

    workingDir = file("src/main/golang/clash")
    commandLine = listOf("go".exe(), "build", "-o", file("$buildDir/outputs/clash").absolutePath)
    standardOutput = System.out
    errorOutput = System.err
}
