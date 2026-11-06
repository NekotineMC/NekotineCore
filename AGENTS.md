<!-- bmad:context -->
<!-- Verified 2026-09-24 against 696d57f. Managed by bmad-project-context; edits inside this block are replaced on refresh. Keep anything you want preserved outside the markers. -->

## NekotineCore

Utility library for PaperMC plugins (French README and Javadoc), consumed as source — Vi6Clean pulls it in as a git submodule plus a Gradle composite build. Java 25, Gradle 9.4.1 wrapper, Kotlin DSL. Multi-module build, coordinates `fr.nekotine:<module>`:

- `core` — all general-purpose code; `paper-api` is compileOnly, no paperweight, so it stays usable on any Paper version. Never depends on `bots`.
- `bots` — NMS module (fake players, package `fr.nekotine.core.bot`): paperweight userdev on the dev bundle pinned in `gradle/libs.versions.toml` (`paper-devbundle`), `api(project(":core"))`. It must match the host's locked dev bundle — bump both together.

Both modules publish plain jars and never shade. The host plugin shades exactly one copy of each and relocates CommandAPI itself (`core` only declares it as `implementation`). Nothing is published; distribution is the repository itself.

## Policy

- Never push to `dev` directly — branch, push, open a PR. Agents may push their own branches and open PRs, but never merge one; a human reviews first.
- Commit messages follow the [Conventional Commits](https://www.conventionalcommits.org/) specification: `type(scope): short subject`, `!` before the colon for breaking changes.

## Where things are

- Own repository: `github.com/NekotineMC/NekotineCore`, default branch `dev`.
- Dependency injection: `fr.nekotine.core.ioc.Ioc.resolve(...)`; `NekotinePlugin.setupModules()` reflects over `fr.nekotine.core` for `IPluginModule` implementations, so adding one auto-registers it — there is no wiring file to edit.
- README.md is the module inventory with status marks; `.github/` holds French issue templates and no workflows — there is no CI, so verify locally.
- Sources live under `core/src/` and `bots/src/`; shared conventions (group, version, repositories, toolchain, Spotless, locking) sit in the root `build.gradle.kts` `subprojects {}` block.
- Vi6Clean (the parent) depends on `fr.nekotine:core` and `fr.nekotine:bots` (substituted by `includeBuild`), shades both unrelocated under `fr.nekotine.core` and relocates CommandAPI to `fr.nekotine.core.commandapi` — package moves break the consumer at compile time.

## Running and verifying

- Build requires JDK 25 — toolchain is fixed at 25 and no toolchain resolver is configured; use the wrapper (`./gradlew`, `gradlew.bat` on Windows). Bare `gradlew` runs `out` = `spotlessApply` + each module's `jar`.
- `gradlew build` runs `check`, which includes `spotlessCheck` on every module — formatting drift and wildcard imports fail it.
- JUnit 5 is configured in `core` but every file under `core/src/test` is fully commented out — `gradlew test` passes while running zero tests; revive a commented file rather than trusting the green run.
- Dependency locking is on, one `gradle.lockfile` per module: after changing dependencies, update only what changed (`gradlew :<module>:dependencies --update-locks <group>:*`) and review the diff — a full `--write-locks` also moves `paper-api` (`26.2.build.+`) and CommandAPI (`11.2.+`).

## Conventions that differ from defaults

- Spotless also runs `removeUnusedImports()` here, unlike the parent repository.
- French and English are used interchangeably — either is fine; don't translate existing text.

<!-- /bmad:context -->
