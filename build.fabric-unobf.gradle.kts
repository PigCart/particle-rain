plugins {
    id("net.fabricmc.fabric-loom")
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

        this["access_widener"] = "${prop("mod.id")}.unobf.accesswidener"

        // insert version-specific mixins
        this["RegistrySyncManagerMixin" ] = if (sc.current.parsed  > "1.20.1") "\"fabric.RegistrySyncManagerMixin\"," else ""
        this["TextureSheetParticleMixin"] = if (sc.current.parsed  < "1.21.9") "\"tint.TextureSheetParticleMixin\"," else ""
        this["DripParticleMixin"        ] = if (sc.current.parsed  < "1.21.9") "\"tint.DripParticleMixin\"," else ""
        this["WaterFallProviderMixin"   ] = if (sc.current.parsed >= "1.21.9") "\"tint.WaterFallProviderMixin\"," else ""
        this["WaterHangProviderMixin"   ] = if (sc.current.parsed >= "1.21.9") "\"tint.WaterHangProviderMixin\"," else ""
    }

    filesMatching(listOf("fabric.mod.json", "${prop("mod.id")}.mixins.json")) {
        expand(props)
    }
}

version = "${property("mod.version")}+${minecraft}-fabric"
base.archivesName = property("mod.id") as String

repositories {
    mavenLocal()
    maven("https://maven.terraformersmc.com/")
    maven("https://api.modrinth.com/maven")
}

loom {
    val accesswidener = rootProject.file("src/main/resources/${property("mod.id")}.unobf.accesswidener")
    if (accesswidener.exists()) {
        accessWidenerPath = accesswidener
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("deps.minecraft")}")

    implementation("net.fabricmc:fabric-loader:${property("deps.fabric-loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric-api")}")
    compileOnly("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    compileOnly("maven.modrinth:iris:${property("deps.iris")}")
}

tasks {
    processResources {
        exclude("**/neoforge.mods.toml", "**/mods.toml")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}