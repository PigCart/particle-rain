plugins {
    id("net.fabricmc.fabric-loom")
}

fun prop(name: String) = sc.properties[name]
val minecraft = prop("deps.minecraft");

tasks.named<ProcessResources>("processResources") {

    val props = HashMap<String, String>().apply {
        this["mod_id"] =        prop("mod.id")
        this["mod_name"] =      prop("mod.name")
        this["mod_version"] =   prop("mod.version")
        this["mod_description"]=prop("mod.description")
        this["mod_author"] =    prop("mod.author")
        this["mod_sources"] =   prop("mod.sources")
        this["mod_issues"] =    prop("mod.issues")
        this["mod_homepage"] =  prop("mod.homepage")
        this["mod_license"] =   prop("mod.license")
        this["mod_icon"] =      prop("mod.icon")
        this["version_range"] = prop("version_range")

        this["access_widener"] = "${prop("mod.id")}.unobf.accesswidener"

        // insert version-specific mixins
        this["RegistrySyncManagerMixin" ] = if (sc.current.parsed  > "1.20.1") "\"fabric.RegistrySyncManagerMixin\"," else ""
        this["TextureSheetParticleMixin"] = if (sc.current.parsed  < "1.21.9") "\"tint.TextureSheetParticleMixin\"," else ""
    }

    filesMatching(listOf("fabric.mod.json", "${prop("mod.id")}.mixins.json")) {
        expand(props)
    }
}

version = "${prop("mod.version")}+${minecraft}-fabric"
base.archivesName = prop("mod.id")

repositories {
    mavenLocal()
    maven("https://maven.terraformersmc.com/")
    maven("https://api.modrinth.com/maven")
}

loom {
    val accesswidener = rootProject.file("src/main/resources/${prop("mod.id")}.unobf.accesswidener")
    if (accesswidener.exists()) {
        accessWidenerPath = accesswidener
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")

    implementation("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
    compileOnly("com.terraformersmc:modmenu:${prop("deps.modmenu")}")
    compileOnly("maven.modrinth:iris:${prop("deps.iris")}")
    //implementation("maven.modrinth:sodium:mc26.3-0.9.3-beta.1-fabric")
}

tasks {
    processResources {
        exclude("**/neoforge.mods.toml", "**/mods.toml")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${prop("mod.version")}"))
        dependsOn("build")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}