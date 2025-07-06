plugins {
    id("dev.isxander.modstitch.base") version "0.5.12"
    id("dev.kikugie.stonecutter")
}

fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)
        ?.let(consumer)
}

val minecraft = property("deps.minecraft") as String;

modstitch {
    minecraftVersion = minecraft

    // Alternatively use stonecutter.eval if you have a lot of versions to target.
    // https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions
    javaTarget = when (minecraft) {
        "1.20.1" -> 17
        "1.21.1" -> 21
        "1.21.4" -> 21
        "1.21.5" -> 21
        "1.21.6" -> 21
        else -> throw IllegalArgumentException("Please store the java version for ${property("deps.minecraft")} in build.gradle.kts!")
    }

    // If parchment doesnt exist for a version yet you can safely
    // omit the "deps.parchment" property from your versioned gradle.properties
    parchment {
        prop("deps.parchment") { mappingsVersion = it }
    }

    // This metadata is used to fill out the information inside
    // the metadata files found in the templates folder.
    metadata {
        modId = "particlerain"
        modName = "Particle Rain"
        modVersion = "4.0.0-alpha-2+$name"
        modGroup = "pigcart"
        modAuthor = "PigCart"
        modLicense = "MIT"

        fun <K, V> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
            block()
        }

        replacementProperties.populate {
            if (isModDevGradleLegacy) {
                put("refmap", ",\"refmap\": \"particlerain.refmap.json\"")
            } else {
                // uses a dash: "particlerain-refmap", and is added automatically anyway
                put("refmap", "")
            }
            put("mod_issue_tracker", "https://github.com/pigcart/particlerain/issues")
            put("mod_icon", "assets/particlerain/icon.png")
            put("version_range", property("version_range") as String)
        }
    }

    loom {
        fabricLoaderVersion = "0.16.10"
    }

    moddevgradle {
        enable {
            prop("deps.forge") { forgeVersion = it }
            prop("deps.neoform") { neoFormVersion = it }
            prop("deps.neoforge") { neoForgeVersion = it }
            prop("deps.mcp") { mcpVersion = it }
        }

        // Configures client and server runs for MDG, it is not done by default
        defaultRuns()

        // This block configures the `neoforge` extension that MDG exposes by default,
        // you can configure MDG like normal from here
        configureNeoforge {
            runs.all {
                disableIdeRun()
            }
        }
    }

    mixin {
        addMixinsToModManifest = true

        configs.register("particlerain")

    }
}

tasks.named("generateModMetadata") {
    dependsOn("stonecutterGenerate")
}
modstitch.moddevgradle {
    tasks.named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }
}

// Stonecutter constants for mod loaders.
// See https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants
var constraint: String = name.split("-")[1]
stonecutter {
    consts(
        "fabric" to constraint.equals("fabric"),
        "neoforge" to constraint.equals("neoforge"),
        "forge" to constraint.equals("forge")
    )
}

dependencies {
    modstitch.loom {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabricapi")}")
        modstitchModImplementation("com.terraformersmc:modmenu:${property("modmenu")}")
    }
    // forge
    if (modstitch.isModDevGradleLegacy) {
        compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)
        "io.github.llamalad7:mixinextras-forge:0.4.1".let {
            modstitchModImplementation(it)
            modstitchJiJ(it)
        }
    }

    // Anything else in the dependencies block will be used for all platforms.
    modstitchModImplementation("dev.isxander:yet-another-config-lib:${property("yacl")}")
}