// Full custom build for clash

fun String.exe(): String {
    return if ( System.getProperty("os.name")?.toLowerCase()?.contains("win") == true )
        "$this.exe"
    else
        this
}

task("build", type = Exec::class) {
    workingDir = file("src/main/golang/clash")
    commandLine = listOf("go".exe(), "build", "-o", file("$buildDir/outputs/clash").absolutePath)

    environment("GOPATH" to file("$buildDir/intermediate/gopath/").absolutePath)
}
