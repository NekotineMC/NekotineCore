plugins {
	java
	kotlin("jvm").version("2.0.+")
}

val group = "fr.nekotine"
val version = "0.0.1-SNAPSHOT"
val description = "NekotineCore"

repositories {
	mavenLocal()
	mavenCentral()
	// PAPERMC
	maven("https://repo.papermc.io/repository/maven-public/") {
		name = "papermc-repo"
	}
	// PROTOCOLIB
	maven("https://repo.dmulloy2.net/repository/public/") {
		name = "protocolib-repo"
	}
	// NBT-API for CommandAPI
	maven("https://repo.codemc.org/repository/maven-public/") {
		name = "commandapi-repo"
	}
    // World Edit
    maven ("https://maven.enginehub.org/repo/") { 
    	name = "worldedit-repo"
    }
}

dependencies {
	compileOnly(group = "io.papermc.paper", name = "paper-api", version = "1.21.4-R0.1-SNAPSHOT")
	compileOnly(group = "com.comphenix.protocol", name = "ProtocolLib", version = "+")
	compileOnly(group = "dev.jorel", name = "commandapi-bukkit-core", version = "+")
	compileOnly(group = "com.sk89q.worldedit", name = "FastAsyncWorldEdit-Core", version = "+")
	compileOnly(group = "com.sk89q.worldedit", name = "FastAsyncWorldEdit-Bukkit", version = "+")
	testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.+")
	testImplementation(group = "io.papermc.paper", name = "paper-api", version = "1.21.3-R0.1-SNAPSHOT")
}

