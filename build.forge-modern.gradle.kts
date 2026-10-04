plugins {
    id("net.minecraftforge.gradle")
    id("net.minecraftforge.jarjar")
}

fun prop(name: String) = sc.properties[name] as String
val mcVersion = prop("deps.minecraft")

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "mod_id" to prop("mod.id"),
        "mod_name" to prop("mod.name"),
        "mod_version" to prop("mod.version"),
        "mod_description" to prop("mod.description"),
        "mod_author" to prop("mod.author"),
        "mod_sources" to prop("mod.sources"),
        "mod_issues" to prop("mod.issues"),
        "mod_homepage" to prop("mod.homepage"),
        "mod_license" to prop("mod.license"),
        "mod_icon" to prop("mod.icon"),
        "version_range" to prop("version_range"),
        "loader_version_range" to prop("loader_version_range"),
        "RegistrySyncManagerMixin" to "",
        "TextureSheetParticleMixin" to ""
    )

    filesMatching(listOf("META-INF/mods.toml", "${prop("mod.id")}.mixins.json")) {
        expand(props)
    }

    exclude("META-INF/accesstransformer.cfg")
    from(rootProject.file("gradle/accesstransformer-26.3.cfg")) {
        into("META-INF")
        rename { "accesstransformer.cfg" }
    }
}

version = "${prop("mod.version")}+$mcVersion-forge"
base.archivesName = prop("mod.id")

minecraft {
    accessTransformers = rootProject.files("gradle/accesstransformer-26.3.cfg")

    runs {
        configureEach {
            workingDir.convention(layout.projectDirectory.dir("run"))
        }
        register("client")
        register("server")
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    maven("https://api.modrinth.com/maven")
    mavenCentral()
}

jarJar.register {
    archiveClassifier = null
}

tasks.named<Jar>("jar") {
    archiveClassifier = "slim"
    manifest.attributes["MixinConfigs"] = "${prop("mod.id")}.mixins.json"
}

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${prop("deps.forge")}"))
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.0")
    "jarJar"("io.github.llamalad7:mixinextras-forge:0.5.0")
    compileOnly("maven.modrinth:iris:${prop("deps.iris")}")
}

tasks {
    processResources {
        exclude("**/fabric.mod.json", "**/*.accesswidener", "**/neoforge.mods.toml")
    }

    named("compileJava") {
        dependsOn("stonecutterGenerate")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(named<Jar>("jarJar").map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${prop("mod.version")}"))
        dependsOn("build")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
