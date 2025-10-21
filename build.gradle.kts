plugins {
    id("dev.isxander.modstitch.base") version "0.7.1-unstable"
    id("dev.kikugie.stonecutter")
}

fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)
        ?.let(consumer)
}

val minecraft = property("deps.minecraft") as String;

modstitch {
    minecraftVersion = minecraft

    parchment {
        prop("deps.parchment") { mappingsVersion = it }
    }

    metadata {
        modId = "particlerain"
        modName = "Particle Rain"
        modVersion = "4.0.0-beta.3+$name"
        modGroup = "pigcart"
        modAuthor = "PigCart"
        modLicense = "MIT"

        fun <K, V> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
            block()
        }

        replacementProperties.populate {
            // insert version-specific mixins
            put("RegistrySyncManagerMixin", if (isLoom && minecraft != "1.20.1") "\"fabric.RegistrySyncManagerMixin\"," else "")
            put("TextureSheetParticleMixin", if (minecraft < "1.21.9") "\"tint.TextureSheetParticleMixin\"," else "")
            put("DripParticleMixin", if (minecraft < "1.21.9") "\"tint.DripParticleMixin\"," else "")
            put("WaterFallProviderMixin", if (minecraft >= "1.21.9") "\"tint.WaterFallProviderMixin\"," else "")
            put("WaterHangProviderMixin", if (minecraft >= "1.21.9") "\"tint.WaterHangProviderMixin\"," else "")

            // workaround for modstitch including both mods.toml files screwing up mc-publish
            put("forge_or_neoforge", if (isModDevGradleLegacy) "forge" else "neoforge")

            // mod metadata
            put("mod_issue_tracker", "https://github.com/pigcart/particle-rain/issues")
            put("mod_icon", "assets/particlerain/icon.png")
            put("version_range", property("version_range") as String)
        }
    }

    loom {
        fabricLoaderVersion = "0.17.2"
    }

    moddevgradle {
        prop("deps.forge") { forgeVersion = it }
        prop("deps.neoform") { neoFormVersion = it }
        prop("deps.neoforge") { neoForgeVersion = it }
        prop("deps.mcp") { mcpVersion = it }

        // Configures client and server runs for MDG, it is not done by default
        defaultRuns()

        // This block configures the `neoforge` extension that MDG exposes by default,
        // you can configure MDG like normal from here
        configureNeoForge {
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
    // fabric
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
    // all platforms.
    modstitchModImplementation("dev.isxander:yet-another-config-lib:${property("yacl")}")
}