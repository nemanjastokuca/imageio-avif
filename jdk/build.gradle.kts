import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.34.0"
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
    withSourcesJar()
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
    named("sourcesJar").configure {
        dependsOn(project(":native").tasks.build)
    }
}

mavenPublishing {
    coordinates("io.github.nemanjastokuca", "avif-imageio-native-reader", "0.0.1")
    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = true
    ))
    pom {
        name.set("AVIF ImageIO Native Reader")
        url.set("https://github.com/nemanjastokuca/imageio-avif")
        description.set("A native JNI binding for avif image format, which supports Java imageio service.")
        licenses {
            license {
                name.set("GNU Lesser General Public License, Version 3.0")
                url.set("https://www.gnu.org/licenses/lgpl-3.0.txt")
            }
        }
        developers {
            developer {
                id.set("nemanjastokuca")
                name.set("Nemanja Stokuca")
                email.set("nemanjastokuca95@gmail.com")
            }
        }
        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/nemanjastokuca/imageio-avif/issues")
        }
        scm {
            url.set("https://github.com/nemanjastokuca/imageio-avif")
            connection.set("scm:git:git://github.com/nemanjastokuca/avif-imageio-native-reader.git")
            developerConnection.set("scm:git:ssh://github.com/nemanjastokuca/avif-imageio-native-reader.git")
        }
    }
}