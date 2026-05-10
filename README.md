# SYNAPSE Plugin Template

🚀 **Use this template** to create your own SYNAPSE plugin in Java!

This repository provides a complete, production-ready starting point for developing SYNAPSE plugins using Java 21, Spring Boot 3.x, and Gradle 8.x.

[![Use this template](https://img.shields.io/badge/use%20this-template-blue?logo=github)](https://github.com/FTMahringer/Synapse-Plugin-Template/generate)

---

## 📋 What's Included

- ✅ **Complete Java plugin structure** with example implementation
- ✅ **Spring Boot 3.x integration** with dependency injection
- ✅ **Gradle 8.x build system** with wrapper
- ✅ **Configuration examples** using Spring Boot properties
- ✅ **JUnit 5 testing setup** with example tests
- ✅ **CI/CD workflows** for validation and testing
- ✅ **Documentation templates** for your plugin
- ✅ **Development tools** and utilities

---

## 🎯 Quick Start

### Prerequisites

- **Java 21** or later
- **Gradle 8.5** or later (included via wrapper)
- **Git**

### 1. Use This Template

Click the **"Use this template"** button above to create your own repository.

### 2. Clone Your Repository

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_PLUGIN_NAME.git
cd YOUR_PLUGIN_NAME
```

### 3. Customize Your Plugin

Update the following files:

- `plugin.yaml` - Plugin metadata and configuration
- `build.gradle` - Build configuration, dependencies, version
- `settings.gradle` - Project name
- `src/main/java/dev/synapse/plugin/example/` - Your plugin implementation
- `README.md` - This file! Make it yours.
- `LICENSE` - Choose your license

### 4. Build Your Plugin

```bash
# Build the plugin
./gradlew build

# Run tests
./gradlew test

# Create JAR
./gradlew jar
```

### 5. Test Locally

```bash
# Link plugin for local development
synapse plugin link .

# Test with SYNAPSE
synapse dev --watch-plugins .
```

---

## 📦 Plugin Structure

```
synapse-plugin-template/
├── README.md                                    # This file
├── LICENSE                                      # Plugin license
├── plugin.yaml                                  # Plugin manifest
├── build.gradle                                 # Gradle build configuration
├── settings.gradle                              # Gradle settings
├── gradle.properties                            # Gradle properties
├── gradlew                                      # Gradle wrapper (Unix)
├── gradlew.bat                                  # Gradle wrapper (Windows)
├── .gitignore
│
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── src/
│   ├── main/
│   │   ├── java/dev/synapse/plugin/example/
│   │   │   ├── ExamplePlugin.java              # Main plugin class
│   │   │   ├── PluginConfig.java               # Configuration
│   │   │   ├── ExampleToolRequest.java         # Tool request objects
│   │   │   └── FetchDataRequest.java
│   │   └── resources/
│   │       ├── plugin.yml                       # Plugin descriptor
│   │       └── application.yml                  # Spring Boot config
│   │
│   └── test/java/dev/synapse/plugin/example/
│       └── ExamplePluginTest.java               # Unit tests
│
├── docs/                                        # Plugin documentation
│   ├── usage.md
│   └── configuration.md
│
├── examples/                                    # Usage examples
│   └── basic_example.yaml
│
└── .github/
    └── workflows/
        ├── test.yml                             # CI testing
        └── validate.yml                         # Plugin validation
```

---

## 🔧 Plugin Development

### Plugin Manifest (plugin.yaml)

The `plugin.yaml` file defines your plugin's metadata:

```yaml
name: my-plugin
version: 1.0.0
description: Short description of what your plugin does
language: java
build_system: gradle

author: Your Name
synapse_version: ">=2.0.0"

entry_point: dev.synapse.plugin.mypackage.MyPlugin
```

### Implementing Tools

Example plugin implementation (Java):

```java
@Slf4j
@Component
public class MyPlugin {
    
    private final PluginConfig config;
    
    public MyPlugin(PluginConfig config) {
        this.config = config;
    }
    
    /**
     * Example tool implementation
     */
    public Map<String, Object> exampleTool(ExampleToolRequest request) {
        log.info("Processing query: {}", request.getQuery());
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("result", "Processed: " + request.getQuery());
        
        return result;
    }
}
```

### Plugin Lifecycle

Your plugin can implement lifecycle hooks:

```java
public class MyPlugin {
    
    public void onStartup() {
        log.info("Plugin loaded!");
    }
    
    public void onShutdown() {
        log.info("Plugin shutting down");
        // Cleanup resources
    }
    
    public void onSessionStart(String sessionId, Map<String, Object> context) {
        log.info("Session started: {}", sessionId);
    }
}
```

---

## 🧪 Testing Your Plugin

### Unit Tests

Write tests using JUnit 5:

```java
@SpringBootTest
@DisplayName("MyPlugin Tests")
class MyPluginTest {
    
    @Test
    @DisplayName("Should process tool request successfully")
    void testExampleTool() {
        ExampleToolRequest request = ExampleToolRequest.builder()
            .query("test input")
            .build();
        
        Map<String, Object> result = plugin.exampleTool(request);
        
        assertThat(result.get("status")).isEqualTo("success");
        assertThat(result.get("result")).asString()
            .contains("test input");
    }
}
```

### Run Tests

```bash
# Run all tests
./gradlew test

# Run with coverage report
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests ExamplePluginTest
```

---

## 📝 Configuration

### Plugin Configuration

Define configuration using Spring Boot properties:

```java
@Data
@Configuration
@ConfigurationProperties(prefix = "synapse.plugin.myplugin")
@Validated
public class PluginConfig {
    
    private String apiKey;
    
    @Min(1)
    @Max(300)
    private int timeout = 30;
    
    private boolean enableCaching = true;
}
```

### Application Configuration

Users configure your plugin via `application.yml`:

```yaml
synapse:
  plugin:
    myplugin:
      api-key: ${MY_PLUGIN_API_KEY}
      timeout: 30
      enable-caching: true
```

### Environment Variables

Use environment variables for sensitive data:

```bash
export MY_PLUGIN_API_KEY=your_api_key_here
```

---

## 🚀 Publishing Your Plugin

### 1. Test Thoroughly

```bash
# Run all tests
./gradlew test

# Build JAR
./gradlew build

# Validate plugin
synapse plugin validate .
```

### 2. Update Documentation

- Complete README.md
- Add usage examples
- Document all tools and parameters
- Add troubleshooting section

### 3. Choose Publishing Method

#### Option A: Submit to Community Repository

Submit your plugin to the official community repository:

1. Fork [Synapse-Plugins-Community](https://github.com/FTMahringer/Synapse-Plugins-Community)
2. Add your plugin to the `plugins/` directory
3. Submit a Pull Request
4. Wait for review

See the [Community Contribution Guide](https://github.com/FTMahringer/Synapse-Plugins-Community/blob/main/CONTRIBUTING.md) for details.

#### Option B: Distribute Independently

Keep your plugin in your own repository:

```bash
# Users install directly from your repo
synapse plugin install https://github.com/YOUR_USERNAME/YOUR_PLUGIN_NAME
```

---

## 🔒 Security Best Practices

### Input Validation

Always validate user input using Bean Validation:

```java
@Data
@Builder
public class ToolRequest {
    @NotBlank(message = "Query is required")
    @Size(max = 1000, message = "Query too long")
    private String query;
}
```

### Configuration Security

Never hardcode secrets:

```java
// ❌ Bad
private String apiKey = "sk-abc123";

// ✅ Good  
@Value("${synapse.plugin.api-key}")
private String apiKey;
```

---

## 📚 Resources

### Documentation

- [SYNAPSE Documentation](https://ftmahringer.github.io/Synapse/)
- [Plugin Development Guide](https://ftmahringer.github.io/Synapse/plugins/development/getting-started)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Gradle User Manual](https://docs.gradle.org/current/userguide/userguide.html)

### Examples

- [Official Plugins](https://github.com/FTMahringer/Synapse-Plugins)
- [Community Plugins](https://github.com/FTMahringer/Synapse-Plugins-Community)

### Support

- [GitHub Discussions](https://github.com/FTMahringer/Synapse-Plugin-Template/discussions)
- [Report Issues](https://github.com/FTMahringer/Synapse-Plugin-Template/issues)

---

## 🤝 Contributing

Contributions to improve this template are welcome!

1. Fork this repository
2. Create a feature branch
3. Make your changes
4. Submit a Pull Request

---

## 📄 License

This template is licensed under the MIT License - see [LICENSE](LICENSE) for details.

Your plugin can use any license you choose. Common choices:

- **MIT** - Permissive, allows commercial use
- **Apache 2.0** - Permissive with patent grant
- **GPL-3.0** - Copyleft, requires derivatives to be open source

---

## 🎉 Next Steps

Now that you've created your plugin repository:

1. ✅ Customize `plugin.yaml` with your plugin details
2. ✅ Implement your plugin tools in Java
3. ✅ Write tests using JUnit 5
4. ✅ Update this README with your plugin documentation
5. ✅ Add usage examples in `examples/`
6. ✅ Test locally with SYNAPSE
7. ✅ Submit to Community Repository or distribute independently

**Happy plugin development!** 🚀
