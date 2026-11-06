// Paper API only, no paperweight: stays usable on any Paper version.
// CommandAPI is an implementation dependency, so it reaches the host's
// runtime classpath, where the host shades and relocates it.
dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.protocollib)
    compileOnly(libs.fawe.core)
    compileOnly(libs.fawe.bukkit)
    implementation(libs.commandapi)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.paper.api)
}
