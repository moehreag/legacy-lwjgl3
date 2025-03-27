import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI

plugins {
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

    "include"("org.lwjgl:lwjgl:$lwjglVersion")
    "include"("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    "include"("org.lwjgl:lwjgl-openal:$lwjglVersion")
    "include"("org.lwjgl:lwjgl-opengl:$lwjglVersion")
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

    this.modrinth {
        dependsOn("publish")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.getByName("remapJar"))
        }
    }

    // select the repositories you want to publish to
    repositories {
        val isSnapshot = project.version.toString().contains("beta") || project.version.toString().contains("alpha")
        val repository = if (isSnapshot) "snapshots" else "releases"
        maven("https://moehreag.duckdns.org/maven/$repository") {
            name = "owlMaven"
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "lpiIRiAZ"
    versionType = "release"
    uploadFile = "remapJar"
    additionalFiles = listOf("sourcesJar")
    loaders = listOf("fabric", "quilt")

    gameVersions = run {
        URI("https://meta.ornithemc.net/v3/versions/game")
            .toURL()
            .openStream()
            .use { JsonParser.parseReader(it.bufferedReader()).asJsonArray }
            .mapNotNull {
                it as JsonObject
                if (it["stable"].asBoolean) it["version"].asString else null
            }
    }

    dependencies {
        optional.project("osl")
    }
}