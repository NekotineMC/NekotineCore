# bots-test-host test-bundle

Server folder that `gradlew integrationTest` stages into `bots-test-host/build/it-server/` and boots in-process (paper-junit-server), with the freshly built BotsTestHost shadow jar added to `plugins/`. Everything here except this README is gitignored: fill it locally.

## What goes in

- `server.properties` with at least `spawn-protection=0` and `online-mode=false` (RCON and query are turned off at staging, the port is chosen per run)
- `eula.txt` (`eula=true`)
- `plugins/ProtocolLib.jar`, the version in `gradle/libs.versions.toml` (`protocollib`). A copy of Vi6Clean's `test-bundle/plugins/ProtocolLib.jar` works if it is the same version.

Paper generates the default world and the other configs on first boot; they need not be kept here.

The Paper version comes from the build's pinned dev bundle (`paper-devbundle`), not from this folder.

## What never goes in

- `plugins/BotsTestHost*.jar`: ignored at staging; the fresh shadow jar is staged instead.
- `plugins/update/`, and any other plugin (Vi6Clean or another Nekotine host included): the host must boot with ProtocolLib alone.
- Server logs, `cache/`, `libraries/`, `versions/`: skipped at staging anyway, and logs hold player names and IPs.

Without ProtocolLib, BotsTestHost doesn't load and the harness fails the run, naming the target plugin as not loaded. Without this folder at all, `preparePaperTestServer` fails with the bundle path.
