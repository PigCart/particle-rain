plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.1-fabric"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()

        // yacl moment. this must be ordered before xander's maven
        maven("https://thedarkcolour.github.io/KotlinForForge/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")

        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.terraformersmc.com/")
        maven("https://maven.parchmentmc.org")
        maven("https://maven.isxander.dev/releases")
        maven("https://api.modrinth.com/maven")
    }
}