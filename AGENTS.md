<!-- bmad:context -->
<!-- Verified 2026-09-24 against 696d57f. Managed by bmad-project-context; edits inside this block are replaced on refresh. Keep anything you want preserved outside the markers. -->

## NekotineCore

Utility library for PaperMC plugins (French README and Javadoc), consumed as source — Vi6Clean pulls it in as a git submodule plus a Gradle composite build. Java 25, Gradle 9.4.1 wrapper, `paper-api` is compileOnly and there is no paperweight. Nothing is published; distribution is the repository itself.

## Policy

- Never push to `dev` directly — branch, push, open a PR. Agents may push their own branches and open PRs, but never merge one; a human reviews first.
- Commit messages follow the [Conventional Commits](https://www.conventionalcommits.org/) specification: `type(scope): short subject`, `!` before the colon for breaking changes.

## Where things are

- Own repository: `github.com/NekotineMC/NekotineCore`, default branch `dev`.
- Dependency injection: `fr.nekotine.core.ioc.Ioc.resolve(...)`; `NekotinePlugin.setupModules()` reflects over `fr.nekotine.core` for `IPluginModule` implementations, so adding one auto-registers it — there is no wiring file to edit.
- README.md is the module inventory with status marks; `.github/` holds French issue templates and no workflows — there is no CI, so verify locally.
- Vi6Clean (the parent) shades this library and relocates `fr.nekotine.core` to `fr.nekotine.vi6clean.nekotinecore` — package moves break the consumer at compile time.

## Running and verifying

- Build requires JDK 25 — toolchain is fixed at 25 and no toolchain resolver is configured; use the wrapper (`./gradlew`, `gradlew.bat` on Windows). Bare `gradlew` runs `out` = `spotlessApply` + `shadowJar`.
- `gradlew build` runs `check`, which includes `spotlessCheck` — formatting drift and wildcard imports fail it.
- JUnit 5 is configured but every file under `src/test` is fully commented out — `gradlew test` passes while running zero tests; revive a commented file rather than trusting the green run.
- Dependency locking is on: run `gradlew --write-locks` after changing dependencies, plain resolution fails otherwise.

## Conventions that differ from defaults

- Spotless also runs `removeUnusedImports()` here, unlike the parent repository.
- French and English are used interchangeably — either is fine; don't translate existing text.

<!-- /bmad:context -->
