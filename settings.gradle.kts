pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.ornithemc.net/releases")
    }
}

include("b1.7.3")
include("1.3.2")
include("1.5.2")
include("common")
include("api")
