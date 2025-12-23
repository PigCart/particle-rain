plugins {
    id("fabric-loom")
}

val minecraft = property("deps.minecraft") as String;

tasks.named<ProcessResources>("processResources") {
    fun prop(name: String) = project.property(name) as String

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

        // insert version-specific mixins
        this["RegistrySyncManagerMixin" ] = if (minecraft  > "1.20.1") "\"fabric.RegistrySyncManagerMixin\"," else ""
        this["TextureSheetParticleMixin"] = if (minecraft  < "1.21.9") "\"tint.TextureSheetParticleMixin\"," else ""
        this["DripParticleMixin"        ] = if (minecraft  < "1.21.9") "\"tint.DripParticleMixin\"," else ""
        this["WaterFallProviderMixin"   ] = if (minecraft >= "1.21.9") "\"tint.WaterFallProviderMixin\"," else ""
        this["WaterHangProviderMixin"   ] = if (minecraft >= "1.21.9") "\"tint.WaterHangProviderMixin\"," else ""
    }

    filesMatching(listOf("fabric.mod.json", "${prop("mod.id")}.mixins.json")) {
        expand(props)
    }
}

version = "${property("mod.version")}+${minecraft}-fabric"
base.archivesName = property("mod.id") as String

repositories {
    mavenLocal()
    maven("https://maven.parchmentmc.org")
    maven("https://maven.terraformersmc.com/")
}

loom {
    val accesswidener = rootProject.file("src/main/resources/${property("mod.id")}.accesswidener")
    if (accesswidener.exists()) {
        accessWidenerPath = accesswidener
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("deps.minecraft")}")
    mappings(loom.layered {
        officialMojangMappings()
        if (hasProperty("deps.parchment"))
            parchment("org.parchmentmc.data:parchment-${property("deps.parchment")}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric-loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric-api")}")
    modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
}

tasks {
    processResources {
        exclude("**/neoforge.mods.toml", "**/mods.toml")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

java {
    val javaCompat = if (stonecutter.eval(stonecutter.current.version, ">=1.21")) {
        JavaVersion.VERSION_21
    } else {
        JavaVersion.VERSION_17
    }
    sourceCompatibility = javaCompat
    targetCompatibility = javaCompat
}