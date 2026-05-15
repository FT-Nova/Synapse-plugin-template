# synapse-plugin-template

Scaffold templates for [SYNAPSE](https://github.com/FTMahringer/Synapse) plugins.

The fastest way to start: run `synapse plugin scaffold` — it creates a GitHub repo from the right subdirectory automatically.

---

## Plugin types

| Subdirectory | Type | When to use |
|---|---|---|
| [`channel/`](./channel/) | Channel | Connect an external messaging platform (Telegram, Discord, WhatsApp, …) |
| [`model-provider/`](./model-provider/) | Model Provider | Add an LLM backend (OpenAI-compatible, local, custom) |

MCP Server and Skills Bundle scaffolds are coming in v2.7.0.

---

## Quick start (manual)

1. Pick a type subdirectory.
2. Copy it to your new repo.
3. Choose Gradle or Maven — delete the other build file.
4. Fill in `manifest.yml` — at minimum: `id`, `name`, `author`, `version`.
5. Rename the scaffold class to match your plugin name.
6. Implement the interface methods (all `TODO` comments).
7. Run `./test.sh` — must exit 0 before pushing.
8. Push a version tag: `git tag v1.0.0 && git push --tags` — triggers the release workflow.

---

## Requirements

- Java 25+
- Docker (for integration tests)
- [SYNAPSE CLI](https://github.com/FTMahringer/Synapse) (`synapse plugin validate`, `synapse plugin package`)
- GitHub account (for scaffold + publish workflows)

---

## Dependency

Both scaffolds declare `synapse-plugin-api` as their only compile dependency:

```
dev.synapse:synapse-plugin-api:1.0.0
```

Served from GitHub Packages. Add your credentials to `~/.gradle/gradle.properties` or `~/.m2/settings.xml` — see the scaffold README for details.

**Do not add any other dependencies** unless they are purely utility libraries with no Spring, JPA, or Redis classes. The bytecode scanner will reject forbidden references at install time.

---

## Publishing

Community plugins: open a PR against [synapse-plugins-community](https://github.com/FTMahringer/synapse-plugins-community).  
Official plugins: contact the SYNAPSE team.

`synapse plugin publish` (stub in v2.6.0) prints the submission instructions.

---

## Reference implementations

See [synapse-plugin-examples](https://github.com/FTMahringer/synapse-plugin-examples) for real, working plugins against this API.

<!-- FT-Nova contributor -->

