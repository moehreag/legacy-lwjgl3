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
    mappings(ploceus.featherMappings(properties["mappings_build"].toString()))
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    ploceus.dependOsl(properties["osl_version"].toString())

    compileOnly(project(":common"))
}