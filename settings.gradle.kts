// The in-process Paper test harness (git submodule), used by bots-test-host's
// integrationTest suite: once for its Gradle plugin, once for its libraries.
pluginManagement {
    includeBuild("paper-junit-server")
}

rootProject.name = "NekotineCore"

include("core", "bots", "bots-test-host")
includeBuild("paper-junit-server")
