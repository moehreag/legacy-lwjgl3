plugins {
    id("io.freefair.lombok")
    java
    `maven-publish`
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
    compileOnly("org.jetbrains:annotations:26.1.0")
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