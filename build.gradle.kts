// Shared conventions for every module. Modules publish plain jars and never
// shade: the final plugin (e.g. Vi6Clean) shades core and bots and relocates
// CommandAPI itself.
plugins {
    alias(libs.plugins.paperweight.userdev) apply false
    id("com.diffplug.spotless") version "8.4.0"
}

description = "NekotineCore"

// Spotless resolves ktlint for the root build scripts from here.
repositories {
    mavenCentral()
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

subprojects {
    group = "fr.nekotine"
    version = "0.0.1-SNAPSHOT"

    apply(plugin = "java-library")
    apply(plugin = "com.diffplug.spotless")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc"
        }
        maven("https://repo.codemc.org/repository/maven-public/") {
            name = "commandapi"
        }
        // ProtocolLib snapshots (servers run 5.5.0-SNAPSHOT); only net.dmulloy2 resolves here.
        maven("https://repo.dmulloy2.net/repository/public/") {
            name = "protocollib"
            content { includeGroup("net.dmulloy2") }
        }
    }

    dependencyLocking {
        lockAllConfigurations()
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        format("misc") {
            target("src/**/*.java", "*.gradle.kts")
            trimTrailingWhitespace()
            endWithNewline()
        }
        java {
            eclipse("4.39")
            removeUnusedImports()
            formatAnnotations()
            forbidWildcardImports()
        }
        kotlinGradle {
            ktlint()
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<ProcessResources> {
        filteringCharset = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.register("out") {
    group = "dev"
    description = "Apply Spotless and build every module's jar"
    dependsOn(subprojects.map { "${it.path}:spotlessApply" })
    dependsOn(subprojects.map { "${it.path}:jar" })
    dependsOn(tasks.spotlessApply)
}

defaultTasks("out")
