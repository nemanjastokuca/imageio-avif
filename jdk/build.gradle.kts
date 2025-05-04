plugins {
    id("java-library")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("com.google.guava:guava:32.1.2-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.+")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

base {
    archivesName = rootProject.name
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

sourceSets {
    main { resources.srcDir(project(":native").layout.projectDirectory.dir("libraries")) }
}

tasks {
    test { useJUnitPlatform() }
    processResources {
        dependsOn(project(":native").tasks.build)
        from(rootProject.file("LICENSE")) { into("META-INF") }
        from(rootProject.file("LICENSE.libavif")) { into("META-INF") }
        from(rootProject.file("LICENSE.libdav1d")) { into("META-INF") }
    }
}

publishing {
    publications.create<MavenPublication>("jitPack") {
        version = System.getenv("VERSION") ?: version
        groupId = System.getenv("GROUP") ?: "com.github.ustc-zzzz"
        artifactId = System.getenv("ARTIFACT") ?: "avif-imageio-native-reader"
        pom {
            name.set("AVIF ImageIO Native Reader")
            url.set("https://github.com/ustc-zzzz/avif-imageio-native-reader")
            description.set("A native JNI binding for avif image format, which supports Java imageio service.")
            licenses {
                license {
                    name.set("GNU Lesser General Public License, Version 3.0")
                    url.set("https://www.gnu.org/licenses/lgpl-3.0.txt")
                }
            }
            developers {
                developer {
                    id.set("ustc-zzzz")
                    name.set("Yanbing Zhao")
                    email.set("zzzz.mail.ustc@gmail.com")
                }
            }
            issueManagement {
                system.set("GitHub Issues")
                url.set("https://github.com/ustc-zzzz/avif-imageio-native-reader/issues")
            }
            scm {
                url.set("https://github.com/ustc-zzzz/avif-imageio-native-reader")
                connection.set("scm:git:git://github.com/ustc-zzzz/avif-imageio-native-reader.git")
                developerConnection.set("scm:git:ssh://github.com/ustc-zzzz/avif-imageio-native-reader.git")
            }
        }
        artifact(tasks.jar)
    }
}
