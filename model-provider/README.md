# Model Provider Plugin Scaffold

Template for a SYNAPSE Model Provider plugin. Model providers supply agents with LLM completion and streaming capabilities.

## Before you start

- [ ] Fill in `manifest.yml` — at minimum: `id`, `name`, `author`, `version`, `provider_id`, `auth_modes`
- [ ] Rename `MyModelProviderPlugin.java` to `YourProviderPlugin.java`
- [ ] Update `module-info.java` — change `com.example.myprovider` to your module name
- [ ] Choose build system: keep **either** `build.gradle.kts`+`settings.gradle.kts` **or** `pom.xml`, delete the other
- [ ] Implement all `TODO` methods: `configure`, `complete`, `stream`, `listModels`, `getCapabilities`
- [ ] Run `./test.sh` — must exit 0

## Auth modes

Declare which auth modes your provider supports in `manifest.yml`:

```yaml
auth_modes:
  - api_key          # standard key-based auth
  - acp              # Anthropic Claude Platform subscription
```

Check `context.authMode()` in `configure()` — never branch on raw config keys.

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

## Build and test

```bash
# Gradle
./gradlew shadowJar
./test.sh

# Maven
mvn package
./test.sh
```

## Reference

- [synapse-plugin-api Javadoc](https://github.com/FTMahringer/Synapse/tree/main/synapse-plugin-api)
- [Example: openai-provider](https://github.com/FTMahringer/synapse-plugin-examples/tree/main/openai-provider)
- [Example: ollama-provider](https://github.com/FTMahringer/synapse-plugin-examples/tree/main/ollama-provider)
