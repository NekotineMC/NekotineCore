plugins {
    java
    id("com.gradleup.shadow") version "+"
}

group = "fr.nekotine"
version = "0.0.1-SNAPSHOT"
description = "NekotineCore"

repositories {
    mavenLocal()
    mavenCentral()
    // PAPERMC
    maven("https://repo.papermc.io/repository/maven-public/"){
    	name = "papermc"
    }
    // CommandAPI
    maven ("https://repo.codemc.org/repository/maven-public/"){
    	name = "commandapi"
    }
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.21.11+")
	compileOnly("net.dmulloy2:ProtocolLib:+")
	compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:+")
	compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:+")
	implementation("dev.jorel:commandapi-paper-shade:11.1+")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.+")
	testImplementation("io.papermc.paper:paper-api:1.21.11+")
}


tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    relocate("dev.jorel.commandapi", "fr.nekotine.core.commandapi")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// make Shadowjar the default output and remove relocated dependencies
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

// CONFIGURATION
defaultTasks("shadowJar")