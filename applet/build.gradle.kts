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
    minecraft("com.mojang:minecraft:b1.7.3")
    mappings(ploceus.featherMappings(properties["mappings_build"].toString()))
    compileOnly("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    compileOnly("net.fabricmc:sponge-mixin:0.17.1+mixin.0.8.7")

    compileOnly(project(":common"))
}