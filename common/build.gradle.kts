plugins {
    id("io.freefair.lombok")
    java
}

repositories {
    maven("https://maven.fabricmc.net")
    mavenCentral()
}

val targetJava = JavaVersion.VERSION_17

java {
    sourceCompatibility = targetJava
    targetCompatibility = targetJava
    withSourcesJar()
}

dependencies {
    compileOnly("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("net.fabricmc:sponge-mixin:0.17.1+mixin.0.8.7")
}