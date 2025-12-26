plugins {
    id("net.neoforged.moddev.legacyforge")
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
        this["RegistrySyncManagerMixin" ] = ""
        this["TextureSheetParticleMixin"] = "\"tint.TextureSheetParticleMixin\","
        this["DripParticleMixin"        ] = "\"tint.DripParticleMixin\","
        this["WaterFallProviderMixin"   ] = ""
        this["WaterHangProviderMixin"   ] = ""
        this["ParticleLimitMixin"       ] = ""
    }

    filesMatching(listOf("META-INF/mods.toml", "${prop("mod.id")}.mixins.json")) {
        expand(props)
    }
}

version = "${property("mod.version")}+${minecraft}-forge"
base.archivesName = property("mod.id") as String

repositories {
    mavenLocal()
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.parchmentmc.org")
}

legacyForge {
    version = property("deps.forge") as String

    val accessTransformer = rootProject.file("src/main/resources/META-INF/accesstransformer.cfg")
    if (accessTransformer.exists()) {
        accessTransformers.from(accessTransformer)
    }

    if (hasProperty("deps.parchment")) parchment {
        val (mc, ver) = (property("deps.parchment") as String).split(':')
        mappingsVersion = ver
        minecraftVersion = mc
    }

    runs {
        register("client") {
            gameDirectory = file("run/")
            client()
        }
        register("server") {
            gameDirectory = file("run/")
            server()
        }
    }

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
}

mixin {
    add(sourceSets.main.get(), "${property("mod.id")}.refmap.json")
    config("${property("mod.id")}.mixins.json")
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.0")!!)
    implementation(jarJar("io.github.llamalad7:mixinextras-forge:0.5.0")) {}
}

tasks {
    processResources {
        exclude("**/fabric.mod.json", "**/*.accesswidener", "**/neoforge.mods.toml")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }

    jar {
        manifest.attributes["MixinConfigs"] = "${project.property("mod.id")}.mixins.json"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}