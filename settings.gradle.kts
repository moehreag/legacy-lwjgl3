pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.ornithemc.net/releases")
    }
}

include("applet")
include("applet132")
include("common")
