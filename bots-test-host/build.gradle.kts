import java.util.zip.ZipFile

// Tiny Paper plugin that hosts the bots library for NekotineCore's own
// integration tests (gradlew integrationTest). No NMS code of its own:
// paperweight userdev is applied because the test harness needs it, on the
// same pinned dev bundle as :bots. Like any final plugin, it shades exactly one
// core and one bots, unrelocated, and relocates CommandAPI itself.
plugins {
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.shadow)
    id("fr.xxgoldenbluexx.paper-junit-server")
}

dependencies {
    paperweight.paperDevBundle(
        libs.versions.paper.devbundle
            .get(),
    )
    // Brings :core (api) and, at runtime, CommandAPI (core's implementation).
    implementation(project(":bots"))
    // Integration tests register ProtocolLib packet listeners; the server provides it.
    compileOnly(libs.protocollib)
}

tasks.shadowJar {
    archiveClassifier = ""
    relocate("dev.jorel.commandapi", "fr.nekotine.core.commandapi")
}

tasks.jar {
    archiveClassifier = "noshadow"
}

// Integration suite (never part of build/check): an in-process Paper server
// staged from test-bundle/ (see test-bundle/README.md).
paperJunitServer {
    targetPlugin = "BotsTestHost"
    pluginJar = tasks.shadowJar.flatMap { it.archiveFile }
    bundle = layout.projectDirectory.dir("test-bundle")
}

// Guards the shading layout: descriptor, Core and bots unrelocated, CommandAPI
// under Core's package, no NMS classes.
val verifyShadowJarLayout by tasks.registering {
    group = "verification"
    description = "Fails if the shadow jar misses its descriptor, Core or bots, has unrelocated CommandAPI or NMS classes."
    val jar = tasks.shadowJar.flatMap { it.archiveFile }
    inputs.file(jar)
    doLast {
        val entries =
            ZipFile(jar.get().asFile).use { zip ->
                zip
                    .entries()
                    .asSequence()
                    .map { it.name }
                    .toList()
            }
        val forbidden =
            entries.filter {
                it.startsWith("dev/jorel/") || it.startsWith("net/minecraft/") || it.startsWith("com/mojang/")
            }
        if (forbidden.isNotEmpty()) {
            throw GradleException("Shadow jar has forbidden entries, e.g. ${forbidden.take(5)}")
        }
        if (entries.none { it.startsWith("fr/nekotine/core/commandapi/") }) {
            throw GradleException("Shadow jar has no entries under fr/nekotine/core/commandapi/")
        }
        val required =
            listOf(
                "paper-plugin.yml",
                "fr/nekotine/botstesthost/BotsTestHost.class",
                "fr/nekotine/core/NekotinePlugin.class",
                "fr/nekotine/core/bot/BotRoster.class",
            )
        val missing = required.filterNot { it in entries }
        if (missing.isNotEmpty()) {
            throw GradleException("Shadow jar is missing $missing")
        }
        val duplicates =
            entries
                .groupingBy { it }
                .eachCount()
                .filterValues { it > 1 }
                .keys
        if (duplicates.isNotEmpty()) {
            throw GradleException("Shadow jar has duplicate entries, e.g. ${duplicates.take(5)}")
        }
    }
}

tasks.check {
    dependsOn(verifyShadowJarLayout)
}
