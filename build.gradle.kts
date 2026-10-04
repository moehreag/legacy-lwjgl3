import net.fabricmc.loader.impl.game.minecraft.McVersionLookup

plugins {
    `java-library`
    id("io.freefair.lombok") version "9.+"
    id("maven-publish")
    id("com.gradleup.shadow") version "9.+"
    id("me.modmuss50.mod-publish-plugin") version "2.+"
    id("net.fabricmc.fabric-loom-remap") version "1.17.+"
    id("ploceus") version "1.17.+"
}

base.archivesName.set(project.property("archives_base_name") as String)
version = "${project.property("mod_version")}"
group = project.property("maven_group") as String

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { mavenCentral() }
        filter { includeGroup("org.lwjgl") }
    }
}

val lwjglVersion = providers.gradleProperty("lwjgl_version").get()

configurations {
    create("shade")
    create("shadeSources")
}

ploceus {
    setIntermediaryGeneration(2)
}

loom {
    uncompressNestedJars = true
    mods {
        create("legacy-lwjgl3") {
            sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        }
    }
    runs {
        getByName("client") {
            //environmentVars.put("LEGACY_LWJGL3_USE_SDL", "false") // use GLFW
            //programArg("--fullscreen")
        }
        remove(getByName("server"))
    }
}

val targetJava = JavaVersion.VERSION_17

java {
    sourceCompatibility = targetJava
    targetCompatibility = targetJava
    withSourcesJar()
}

dependencies {
    minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
    mappings(ploceus.featherMappings(providers.gradleProperty("mappings_build").get()))
    modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

    ploceus.dependOsl(providers.gradleProperty("osl_version").get())

    listOf("linux", "windows", "macos", "windows-arm64", "macos-arm64", "linux-arm64").forEach { platform ->
        "include"(runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-$platform")!!)
        "include"(runtimeOnly("org.lwjgl:lwjgl-sdl:$lwjglVersion:natives-$platform")!!)
        "include"(runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-$platform")!!)
        "include"(runtimeOnly("org.lwjgl:lwjgl-openal:$lwjglVersion:natives-$platform")!!)
        "include"(runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:natives-$platform")!!)
    }

    "include"(api("org.lwjgl:lwjgl:$lwjglVersion")!!)
    "include"(api("org.lwjgl:lwjgl-sdl:$lwjglVersion")!!)
    "include"(api("org.lwjgl:lwjgl-glfw:$lwjglVersion")!!)
    "include"(api("org.lwjgl:lwjgl-openal:$lwjglVersion")!!)
    "include"(api("org.lwjgl:lwjgl-opengl:$lwjglVersion")!!)

    include(api(project(":api"))!!)
    localRuntime(compileOnly(project(":common"))!!)
    "shade"(project(":common"))
    "shadeSources"(project(":common", configuration = "sourcesElements"))
    for (version in arrayOf("b1.7.3", "1.3.2", "1.5.2")) {
        localRuntime(compileOnly(project(":$version", configuration = "namedElements"))!!)
        "shade"(project(":$version"))
        "shadeSources"(project(":$version", configuration = "sourcesElements"))
    }

    compileOnly("org.jspecify:jspecify:1.0.0")
}

buildscript {
    dependencies {
        classpath("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
    }
}

subprojects {
    apply(plugin = "java")
    dependencies {
        compileOnly("org.lwjgl:lwjgl-sdl:$lwjglVersion")
        compileOnly("org.lwjgl:lwjgl-glfw:${lwjglVersion}")
    }
}

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_18)) {
            options.release.set(17)
        }
    }
}

configurations.configureEach {
    exclude(group = "org.lwjgl.lwjgl")
}

tasks {
    shadowJar {
        enabled = false
    }

    val shadeJar = register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadeJar") {
        description = "Shade all subproject classes"
        this.destinationDirectory = project.layout.buildDirectory.dir("tmp")
        configurations = project.configurations.named("shade").map { listOf(it) }
        archiveClassifier = "shadeOnly"
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}" }
        }
        dependencies {
            exclude {
                it.moduleGroup.startsWith("net.fabricmc") || it.moduleGroup.startsWith("net.ornithemc")
                        || it.moduleGroup.startsWith("org.quiltmc")
                        || it.moduleGroup.startsWith("org.jetbrains")
            }
        }
        actions.addLast {
            outputs.files.forEach {
                zipTree(it).visit {
                    if (path.startsWith("META-INF") || path.startsWith("LICENSE") || file.isDirectory) return@visit
                    remapJar.get().from(file) {
                        if (file.name != path) into(path.substringBeforeLast("/"))
                    }
                }
            }
        }
    }

    remapJar {
        dependsOn(shadeJar)
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}" }
        }
    }

    getByName<Jar>("sourcesJar") {
        dependsOn(classes)
        dependsOn(
            configurations.getByName("shadeSources")
                .incoming.dependencies.buildDependencies
        )
        configurations.named("shadeSources").get().resolve().forEach {
            zipTree(it).visit {
                if (path.startsWith("META-INF") || file.isDirectory) return@visit
                from(file) {
                    into(path.substringBeforeLast("/"))
                }
            }
        }
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}" }
        }
    }

    getByName("remapSourcesJar") {
        outputs.upToDateWhen { !project.tasks.getByName("sourcesJar").didWork }
    }

    register("normalizeVersion") { // utility task to get a normalized version as we've needed that quite a few times now
        actions.add {
            var version = readln()
            println(McVersionLookup.normalizeVersion(version, McVersionLookup.getRelease(version)))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // select the repositories you want to publish to
    repositories {
        val isSnapshot = project.version.toString().contains("beta") || project.version.toString().contains("alpha")
        val repository = if (isSnapshot) "snapshots" else "releases"
        maven("https://maven.axolotlclient.com/$repository") {
            name = "owlMaven"
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

publishMods {
    file.set(tasks.remapJar.flatMap { it.archiveFile })
    additionalFiles.from(tasks.remapSourcesJar.flatMap { it.archiveFile })
    type.set(providers.gradleProperty("mod_version").map { if (it.contains("beta")) BETA else STABLE }.get())
    modLoaders.add("ornithe")
    displayName = version
    changelog = ""

    modrinth {
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        projectId = "lpiIRiAZ"
        requires { slug = "osl" }
        minecraftVersionRange {
            start = "a1.0.4"
            end = "17w43a"
            includeSnapshots = true
        }
    }
}