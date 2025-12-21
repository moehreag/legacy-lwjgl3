import com.google.common.jimfs.Jimfs
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.kamranzafar.jtar.TarEntry
import org.kamranzafar.jtar.TarHeader
import org.kamranzafar.jtar.TarOutputStream
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZ
import org.tukaani.xz.XZOutputStream
import java.io.BufferedOutputStream
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.*

plugins {
    `java-library`
    id("io.freefair.lombok") version "9.+"
    id("maven-publish")
    id("com.modrinth.minotaur") version "2.+"
    id("fabric-loom") version "1.14.+"
    id("ploceus") version "1.14.+"
    id("io.github.p03w.machete") version "2.+"
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
    maven("https://central.sonatype.com/repository/maven-snapshots")
}

val lwjglVersion = properties["lwjgl_version"]

configurations {
    create("embedCompressed")
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings(ploceus.featherMappings(properties["mappings_build"].toString()))
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"].toString()}")

    listOf("linux", "windows", "macos", "windows-arm64", "macos-arm64").forEach { platform ->
        "embedCompressed"(runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-$platform")!!)
        "embedCompressed"(runtimeOnly("org.lwjgl:lwjgl-sdl:$lwjglVersion:natives-$platform")!!)
        "embedCompressed"(runtimeOnly("org.lwjgl:lwjgl-openal:$lwjglVersion:natives-$platform")!!)
        "embedCompressed"(runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:natives-$platform")!!)
    }

    "include"(api("org.lwjgl:lwjgl:$lwjglVersion")!!)
    "embedCompressed"(api("org.lwjgl:lwjgl-sdl:$lwjglVersion")!!)
    "include"(api("org.lwjgl:lwjgl-openal:$lwjglVersion")!!)
    "include"(api("org.lwjgl:lwjgl-opengl:$lwjglVersion")!!)

    include(implementation("org.kamranzafar:jtar:2.3")!!)
    include(implementation("org.tukaani:xz:1.10")!!)
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.kamranzafar:jtar:2.3")
        classpath("org.tukaani:xz:1.10")
        classpath("com.google.jimfs:jimfs:1.3.1")
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

        val out = layout.buildDirectory.dir("resources").get().asFile.toPath().resolve("main")
            .resolve("libraries.tar.xz")
        actions.addLast {
            val jijs = configurations.getByName("include").dependencies.toSet()
            val includes = configurations.getByName("embedCompressed").resolvedConfiguration
                .resolvedArtifacts
                .filter { jijs.none { jij -> it.moduleVersion.toString() + (if (it.classifier != null) ":" + it.classifier else "") == jij.toString() } }
                .map { it.file.toPath() }


            out.parent.createDirectories()
            out.deleteIfExists()
            Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix()).use { memFs ->

                val memPaths = ConcurrentLinkedDeque<java.nio.file.Path>()
                val futs = mutableListOf<CompletableFuture<*>>()
                includes.forEach { jar ->
                    futs.add(CompletableFuture.runAsync {
                        val memPath = memFs.getPath(jar.fileName.toString())
                        FileSystems.newFileSystem(
                            memPath,
                            mapOf("create" to "true", "compressionMethod" to "stored")
                        ).use { fs ->
                            FileSystems.newFileSystem(jar).use { file ->
                                Files.walkFileTree(
                                    file.getPath("/"),
                                    object : SimpleFileVisitor<java.nio.file.Path>() {
                                        override fun visitFile(
                                            file: java.nio.file.Path,
                                            attrs: BasicFileAttributes
                                        ): FileVisitResult {
                                            val f = fs.getPath(file.toString())
                                            f.parent.createDirectories()
                                            file.copyTo(f)
                                            return super.visitFile(file, attrs)
                                        }
                                    })
                            }
                        }
                        memPaths.add(memPath)
                    })
                }
                CompletableFuture.allOf(*futs.toTypedArray()).join()
                out.outputStream().use { outputStream ->
                    BufferedOutputStream(outputStream).use { buf ->
                        XZOutputStream(buf, LZMA2Options(6), XZ.CHECK_SHA256).use { xz ->
                            TarOutputStream(xz).use { tar ->
                                memPaths.forEach { memPath ->
                                    val e = TarEntry(
                                        TarHeader.createHeader(
                                            memPath.fileName.toString(),
                                            memPath.fileSize(),
                                            0,
                                            false,
                                            444
                                        )
                                    )
                                    tar.putNextEntry(e)
                                    Files.copy(memPath, tar)
                                    tar.flush()
                                }
                            }
                        }
                    }
                }
            }
        }
        outputs.file(out)
    }

    processIncludeJars.configure {
        fun recompressNestedJar(jar: File) {
            val out = jar.resolveSibling(jar.name+".stored")
            out.toPath().deleteIfExists()
            jar.inputStream().use { stream ->
                ZipInputStream(stream).use { zipIn ->
                    out.outputStream().use { outStream ->
                        ZipOutputStream(outStream).use { zipOut ->
                            zipOut.setMethod(ZipOutputStream.STORED)
                            var entry: ZipEntry? = zipIn.nextEntry
                            while (entry != null) {
                                val entryBytes = zipIn.readAllBytes()
                                if (entry.method == ZipEntry.DEFLATED) {
                                    entry.method = ZipEntry.STORED
                                    entry.size = entryBytes.size.toLong()
                                    entry.compressedSize = entry.size
                                }
                                zipOut.putNextEntry(entry)
                                zipOut.write(entryBytes)
                                entry = zipIn.nextEntry
                            }
                            zipOut.closeEntry()
                        }
                    }
                }
            }
            out.toPath().moveTo(jar.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        outputs.upToDateWhen { false }
        actions.addLast {
            outputs.files.asFileTree.files.forEach { recompressNestedJar(it) }
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
            //artifact(tasks["remapJar"])
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
    uploadFile = tasks["remapJar"]
    additionalFiles = listOf(tasks["sourcesJar"])
    loaders = listOf("ornithe")

    gameVersions = run {
        URI("https://meta.ornithemc.net/v3/versions/game")
            .toURL()
            .openStream()
            .use { JsonParser.parseReader(it.bufferedReader()).asJsonArray }
            .mapNotNull {
                it as JsonObject
                if (it["stable"].asBoolean) {
                    val minor = Integer.parseInt(it["version"].asString.split(".")[1])
                    if (minor in 8..12) it["version"].asString else null
                } else null
            }
    }

    dependencies {
        optional.project("osl")
    }
}