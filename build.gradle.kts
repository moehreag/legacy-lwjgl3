plugins {
    id("com.gradleup.shadow") version "8.+"
    id("io.freefair.lombok") version "8.+"
    id("maven-publish")
    id("com.modrinth.minotaur") version "2.+"
    id("xyz.wagyourtail.unimined") version "1.3.13"
}

val targetJava = JavaVersion.VERSION_17

java {
    sourceCompatibility = targetJava
    targetCompatibility = targetJava
    withSourcesJar()
}

base.archivesName.set(project.property("archives_base_name") as String)
version = "${project.property("mod_version")}+${project.property("minecraft_version")}"
group = project.property("maven_group") as String

repositories {
    mavenCentral()
}

val lwjglVersion = properties["lwjgl_version"]

unimined {
    minecraft {
        ornitheMaven()
        fabricMaven()

        version(properties["minecraft_version"].toString())

        legacyFabric {
            loader(properties["loader_version"]!!)
        }

        mappings {
            calamus()
            feather(build = properties["mappings_build"]?.toString()?.toInt()!!)
        }

        runs {
            off = false
            config("client") { javaVersion = targetJava }
            config("server") { enabled = false }
        }

        defaultRemapJar = false
        remap(tasks.shadowJar.get())
    }
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-openal")
    implementation("org.lwjgl:lwjgl-opengl")

    listOf("linux", "windows", "macos", "windows-arm64", "macos-arm64", "linux-arm64").forEach { platform ->
        runtimeOnly("org.lwjgl:lwjgl::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-glfw::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-openal::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-opengl::natives-$platform")
    }
}

configurations.configureEach {
    exclude(group = "org.lwjgl.lwjgl")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_18)) {
            options.release.set(17)
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}" }
        }
    }

    shadowJar {
        minimize {
            exclude(dependency("org.lwjgl:lwjgl:$lwjglVersion"))
            exclude(dependency("org.lwjgl:lwjgl-glfw:$lwjglVersion"))
            exclude(dependency("org.lwjgl:lwjgl-openal:$lwjglVersion"))
            exclude(dependency("org.lwjgl:lwjgl-opengl:$lwjglVersion"))
        }
        dependencies {
            include(dependency("org.lwjgl:lwjgl:$lwjglVersion"))
            include(dependency("org.lwjgl:lwjgl-glfw:$lwjglVersion"))
            include(dependency("org.lwjgl:lwjgl-openal:$lwjglVersion"))
            include(dependency("org.lwjgl:lwjgl-opengl:$lwjglVersion"))
        }
    }

    build {
        dependsOn("remapShadowJar")
    }
}