import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `maven-publish`
    id("fabric-loom")
    //id("dev.kikugie.j52j")
    id("me.modmuss50.mod-publish-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

class ModData {
    val id = property("mod.id").toString()
    val name = property("mod.name").toString()
    val version = property("mod.version").toString()
    val title = property("mod.mc_title").toString()
    val group = property("mod.group").toString()
}

class ModDependencies {
    operator fun get(name: String) = property("deps.$name").toString()
}

val mod = ModData()
val deps = ModDependencies()
val mcVersion = stonecutter.current.version
val mcDep = property("mod.mc_dep").toString()
val publish = property("mod.publish")

version = "${mod.version}+${mod.title}"
group = mod.group
base { archivesName.set(mod.id) }

loom {
    serverOnlyMinecraftJar()

    mods {
        create("template") {
            sourceSet(sourceSets["main"])
        }
    }
}

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    strictMaven("https://maven.maxhenkel.de/repository/public", "MaxHenkel")
}

val shadowLibrary: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    fun fapi(vararg modules: String) = modules.forEach {
        modImplementation(fabricApi.module(it, deps["fabric_api"]))
    }

    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:$mcVersion+build.${deps["yarn_build"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${deps["fabric_loader"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${deps["fabric_api"]}")

    fapi(
        // Add modules from https://github.com/FabricMC/fabric
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-lifecycle-events-v1",
    )

    implementation("de.maxhenkel.configbuilder:configbuilder:${deps["maxhenkel_configbuilder"]}")
    include("de.maxhenkel.configbuilder:configbuilder:${deps["maxhenkel_configbuilder"]}")
    modImplementation("me.lucko:fabric-permissions-api:${deps["fabric_permission_api"]}")
    modImplementation("maven.modrinth:luckperms:${deps["luckperms"]}")
}

loom {
    decompilers {
        get("vineflower").apply { // Adds names to lambdas - useful for mixins
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    runConfigs.all {
        ideConfigGenerated(false)
        vmArgs("-Dmixin.debug.export=true")
        runDir = "../../run"
    }
}

java {
    withSourcesJar()
    val java = if (stonecutter.eval(mcVersion, ">=1.20.6")) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shadowLibrary)
    archiveClassifier = "dev-shadow"
    relocate("de.maxhenkel.admiral", "com.scubakay.autorelog.admiral")
}

tasks {
    remapJar {
        inputFile = shadowJar.get().archiveFile
    }
}

tasks.processResources {
    inputs.property("id", mod.id)
    inputs.property("name", mod.name)
    inputs.property("version", mod.version)
    inputs.property("mcdep", mcDep)

    val map = mapOf(
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "mcdep" to mcDep
    )

    filesMatching("fabric.mod.json") { expand(map) }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
    dependsOn("build")
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("Run Active Server") {
        group = "stonecutter"
        dependsOn(tasks.named("runServer"))
    }

    loom {
        runs {
            create("Run Active Server") {
                server()
                ideConfigGenerated(true)
                runDir = "../../run"
                name = "Run Active Server"
                vmArgs("-Dmixin.debug.export=true")
                property("fabric.mcVersion", mcVersion)
            }
        }
    }
}

if (publish == true || publish == "true") {
    publishMods {
        fun versionList(prop: String) = findProperty(prop)?.toString()
            ?.split("\\s+".toRegex())
            ?.map { it.trim() }
            ?: emptyList()

        val versions = versionList("mod.mc_targets")

        file = tasks.remapJar.get().archiveFile
        additionalFiles.from(tasks.remapSourcesJar.get().archiveFile)
        displayName = "${mod.name} ${mod.version} for ${mod.title}"
        version = mod.version
        changelog = rootProject.file("CHANGELOG.md").readText()
        type = ALPHA
        modLoaders.add("fabric")

        dryRun = providers.environmentVariable("MODRINTH_TOKEN")
            .getOrNull() == null
        //|| providers.environmentVariable("CURSEFORGE_TOKEN").getOrNull() == null

        modrinth {
            projectId = property("publish.modrinth").toString()
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            minecraftVersions.addAll(versions)
            requires {
                slug = "fabric-api"
            }
        }

//    curseforge {
//        projectId = property("publish.curseforge").toString()
//        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
//        minecraftVersions.add(mcVersion)
//        requires {
//            slug = "fabric-api"
//        }
//    }
    }

    publishing {
        repositories {
            maven("...") {
                name = "..."
                credentials(PasswordCredentials::class.java)
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }

        publications {
            create<MavenPublication>("mavenJava") {
                groupId = "${property("mod.group")}.${mod.id}"
                artifactId = mod.version
                version = mcVersion

                from(components["java"])
            }
        }
    }
} else {
    logger.lifecycle("Skipping publishing ${projectDir.name}: 'mod.publish' property is false.")
}

// Good job. You just added a comment