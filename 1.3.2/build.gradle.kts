plugins {
    id("net.fabricmc.fabric-loom-remap")
    id("io.freefair.lombok")
    id("ploceus")
}

ploceus {
    setIntermediaryGeneration(2)
}

version = "${rootProject.property("mod_version")}"
group = "${rootProject.property("maven_group")}.${rootProject.base.archivesName.get()}"
val targetJava = JavaVersion.VERSION_17

java {
    sourceCompatibility = targetJava
    targetCompatibility = targetJava
    withSourcesJar()
}

loom {
    runs {
        removeAll { true }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:1.3.2")
    mappings(ploceus.featherMappings(providers.gradleProperty("mappings_build").get()))
    modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
    ploceus.dependOsl(providers.gradleProperty("osl_version").get())

    compileOnly(project(":common"))
}