// NMS module: pinned to one Minecraft version through the paperweight dev
// bundle, which must match the host's (Vi6Clean's) locked dev bundle.
plugins {
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    paperweight.paperDevBundle(
        libs.versions.paper.devbundle
            .get(),
    )
    api(project(":core"))
}
