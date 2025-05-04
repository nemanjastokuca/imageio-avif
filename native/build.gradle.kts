plugins {}

tasks {
    // build targets
    val buildTargets = listOf(
        "Linux" to "Amd64",
        "MacOS" to "Amd64", "MacOS" to "Arm64",
        "Windows" to "Amd64", "Windows" to "Arm64",
    )
    // task for different targets
    val buildTasks = buildTargets.map {
        val toolchain = "${it.first.lowercase()}-${it.second.lowercase()}"
        val clean = register<Delete>("clean${it.first}${it.second}") {
            delete(layout.buildDirectory.dir(toolchain))
        }
        val setup = register<Exec>("setup${it.first}${it.second}") {
            dependsOn(clean)
            commandLine(
                "cmake",
                "-S", layout.projectDirectory,
                "-B", layout.buildDirectory.dir(toolchain).get(),
                "-DBUILD_SHARED_LIBS=OFF",
                "-DCMAKE_TOOLCHAIN_FILE=${layout.projectDirectory.file("toolchains/$toolchain.cmake")}"
            )
        }
        val build = register<Exec>("build${it.first}${it.second}") {
            dependsOn(setup)
            environment("VERBOSE", "1")
            workingDir(layout.buildDirectory.dir(toolchain))
            commandLine("cmake", "--build", ".", "--config", "MinSizeRel", "--parallel")
        }
        Pair(clean, build)
    }
    // total
    register<Task>("clean") {
        dependsOn(*buildTasks.map { it.first }.toTypedArray())
    }
    register<Copy>("build") {
        mustRunAfter(*buildTasks.map { it.second }.toTypedArray())
        into(layout.projectDirectory.dir("libraries/META-INF/native"))
        buildTargets.forEach {
            val source = layout.buildDirectory.dir("${it.first.lowercase()}-${it.second.lowercase()}")
            from(source) { include("*.so", "*.dylib", "*.dll").into(it.second.lowercase()) }
        }
    }
}
