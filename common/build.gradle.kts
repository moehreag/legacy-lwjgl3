plugins {
    id("io.freefair.lombok")
    java
}

repositories {
    maven("https://maven.fabricmc.net")
    mavenCentral()
}

version = "${rootProject.property("mod_version")}"
group = "${rootProject.property("maven_group")}.${rootProject.base.archivesName.get()}"
val targetJava = JavaVersion.VERSION_17

java {
    sourceCompatibility = targetJava
    targetCompatibility = targetJava
    withSourcesJar()
}

dependencies {
    compileOnly(project(":api"))
    compileOnly("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("org.jetbrains:annotations:26.1.0")
}