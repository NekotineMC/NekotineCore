group = "fr.nekotine"
version = "0.0.1-SNAPSHOT"
description = "NekotineCore"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://repo.codemc.org/repository/maven-public/") {
        name = "commandapi"
    }
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.protocollib)
    compileOnly(libs.fawe.core)
    compileOnly(libs.fawe.bukkit)
    implementation(libs.commandapi)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.paper.api)
}

dependencyLocking {
    lockAllConfigurations()
}

plugins {
    java
    alias(libs.plugins.shadow)
    id("com.diffplug.spotless") version "8.4.0"
}

java {
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

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    relocate("dev.jorel.commandapi", "fr.nekotine.core.commandapi")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// make shadowJar the default output and remove relocated dependencies
configurations {
    named("apiElements") {
        outgoing.artifacts.clear()
        outgoing.variants.clear()
        outgoing.artifact(tasks.shadowJar)
        exclude(group = "dev.jorel", module = "commandapi-paper-shade")
    }
    named("runtimeElements") {
        outgoing.artifacts.clear()
        outgoing.variants.clear()
        outgoing.artifact(tasks.shadowJar)
        exclude(group = "dev.jorel", module = "commandapi-paper-shade")
    }
}

tasks.register("out") {
    group = "dev"
    description = "Apply Spotless and ShadowJar"
    dependsOn(tasks.shadowJar)
    dependsOn(tasks.spotlessApply)
}

defaultTasks("out")
