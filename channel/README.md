# Channel Plugin Scaffold

Template for a SYNAPSE Channel plugin. Channels bridge external messaging platforms to SYNAPSE agents.

## Before you start

- [ ] Fill in `manifest.yml` — at minimum: `id`, `name`, `author`, `version`, `channel_id`
- [ ] Rename `MyChannelPlugin.java` to `YourPluginNamePlugin.java`
- [ ] Update `module-info.java` — change `com.example.myplugin` to your module name
- [ ] Choose build system: keep **either** `build.gradle.kts`+`settings.gradle.kts` **or** `pom.xml`, delete the other
- [ ] Implement all `TODO` methods in your plugin class
- [ ] Run `./test.sh` — must exit 0

## Credentials

### Gradle (`~/.gradle/gradle.properties`)
```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.token=YOUR_GITHUB_TOKEN
```

### Maven (`~/.m2/settings.xml`)
```xml
<settings>
  <servers>
    <server>
      <id>github-synapse</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

GitHub token needs `read:packages` scope to download `synapse-plugin-api`.

## Build and test

```bash
# Gradle
./gradlew shadowJar
./test.sh

# Maven
mvn package
./test.sh
```

## Release

Push a version tag to trigger the release workflow:

```bash
git tag v1.0.0
git push --tags
```

The `release.yml` workflow: validates → tests → packages → signs JAR → creates GitHub release.

## Reference

- [synapse-plugin-api Javadoc](https://github.com/FTMahringer/Synapse/tree/main/synapse-plugin-api)
- [Example: discord-channel](https://github.com/FTMahringer/synapse-plugin-examples/tree/main/discord-channel)
