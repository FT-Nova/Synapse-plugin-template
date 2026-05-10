# Configuration Guide

Complete reference for configuring the Example Plugin using Spring Boot.

## Configuration File Format

The plugin uses Spring Boot's YAML configuration format. Configuration is defined in `application.yml`:

```yaml
synapse:
  plugin:
    example:
      api-key: ""
      endpoint: "https://api.example.com"
      timeout: 30
      max-retries: 3
      enable-caching: true
      log-level: "INFO"
```

## Configuration Parameters

### Core Settings

#### `api-key`
- **Type**: `String`
- **Required**: No
- **Default**: `""` (empty string)
- **Secret**: Yes (marked as sensitive)
- **Description**: API key for authenticating with external services
- **Validation**: Via `@ConfigurationProperties`

**Example**:
```yaml
synapse:
  plugin:
    example:
      api-key: "sk-abc123def456ghi789"
```

**Best Practice**: Use environment variables:
```yaml
synapse:
  plugin:
    example:
      api-key: ${EXAMPLE_API_KEY}
```

Or set via environment:
```bash
export EXAMPLE_API_KEY=sk-abc123def456ghi789
```

#### `endpoint`
- **Type**: `String`
- **Required**: No
- **Default**: `"https://api.example.com"`
- **Validation**: Must start with `http://` or `https://` (via `@Pattern`)
- **Description**: Base URL for API requests

**Example**:
```yaml
synapse:
  plugin:
    example:
      endpoint: "https://custom-api.mycompany.com"
```

#### `timeout`
- **Type**: `int`
- **Required**: No
- **Default**: `30`
- **Range**: 1-300 seconds (validated via `@Min` and `@Max`)
- **Description**: Maximum time to wait for API responses

**Example**:
```yaml
synapse:
  plugin:
    example:
      timeout: 60  # 60 seconds
```

**Recommendations**:
- Use 30s for normal operations
- Use 60-120s for slow APIs
- Use 5-10s for fast, local services

#### `max-retries`
- **Type**: `int`
- **Required**: No
- **Default**: `3`
- **Range**: 0-10 (validated via `@Min` and `@Max`)
- **Description**: Number of retry attempts for failed requests

**Example**:
```yaml
synapse:
  plugin:
    example:
      max-retries: 5
```

**Note**: Retry delay uses exponential backoff (2^attempt seconds)

#### `enable-caching`
- **Type**: `boolean`
- **Required**: No
- **Default**: `true`
- **Description**: Enable/disable response caching

**Example**:
```yaml
synapse:
  plugin:
    example:
      enable-caching: false  # Disable caching
```

**When to disable**:
- Real-time data requirements
- Limited memory environments
- Testing scenarios

#### `log-level`
- **Type**: `String`
- **Required**: No
- **Default**: `"INFO"`
- **Valid Values**: `DEBUG`, `INFO`, `WARNING`, `ERROR`, `CRITICAL`
- **Description**: Logging verbosity level

**Example**:
```yaml
synapse:
  plugin:
    example:
      log-level: "DEBUG"
```

**Level Guidelines**:
- `DEBUG`: Development and troubleshooting
- `INFO`: Normal operation (recommended)
- `WARNING`: Only warnings and errors
- `ERROR`: Only errors
- `CRITICAL`: Only critical failures

## Tool-Specific Configuration

### example_tool

No additional configuration required. Behavior controlled by runtime parameters.

### fetch_data

Requires `api-key` for real API calls. Without it, returns mock data.

## Environment-Specific Configurations

### Development

```yaml
spring:
  profiles:
    active: dev

synapse:
  plugin:
    example:
      api-key: ${DEV_API_KEY}
      endpoint: "http://localhost:8000"
      timeout: 60
      max-retries: 1
      enable-caching: false  # Fresh data for testing
      log-level: "DEBUG"
```

### Staging

```yaml
spring:
  profiles:
    active: staging

synapse:
  plugin:
    example:
      api-key: ${STAGING_API_KEY}
      endpoint: "https://staging-api.example.com"
      timeout: 30
      max-retries: 3
      enable-caching: true
      log-level: "INFO"
```

### Production

```yaml
spring:
  profiles:
    active: prod

synapse:
  plugin:
    example:
      api-key: ${PROD_API_KEY}
      endpoint: "https://api.example.com"
      timeout: 30
      max-retries: 5
      enable-caching: true
      log-level: "WARNING"
```

## Programmatic Configuration

### Using PluginConfig Object

```java
import dev.synapse.plugin.example.*;

PluginConfig config = new PluginConfig();
config.setApiKey("your-key");
config.setEndpoint("https://api.example.com");
config.setTimeout(60);
config.setMaxRetries(3);
config.setEnableCaching(true);
config.setLogLevel("DEBUG");

ExamplePlugin plugin = new ExamplePlugin(config);
```

### Using Spring Boot Auto-Configuration

```java
@Configuration
public class PluginConfiguration {
    
    @Bean
    public ExamplePlugin examplePlugin(PluginConfig config) {
        return new ExamplePlugin(config);
    }
}
```

The configuration will be automatically injected from `application.yml`.

### Environment Variables

```java
// Configuration is automatically loaded from environment variables
// via Spring Boot's property resolution

// Set environment variables:
// SYNAPSE_PLUGIN_EXAMPLE_APIKEY=your-key
// SYNAPSE_PLUGIN_EXAMPLE_ENDPOINT=https://api.example.com
// SYNAPSE_PLUGIN_EXAMPLE_TIMEOUT=30

// No code changes needed - Spring Boot handles it automatically
```

Or programmatically:

```java
import org.springframework.core.env.Environment;

@Autowired
private Environment env;

String apiKey = env.getProperty("synapse.plugin.example.api-key");
```

## Configuration Validation

The plugin automatically validates configuration on initialization using Bean Validation:

```java
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

try {
    PluginConfig config = new PluginConfig();
    config.setTimeout(500);  // Invalid: exceeds maximum
    config.setLogLevel("INVALID");  // Invalid: not in enum
    
    // Validation happens automatically via @Validated
    ExamplePlugin plugin = new ExamplePlugin(config);
} catch (Exception e) {
    // Validation errors will be thrown
    System.err.println(e.getMessage());
}
```

## Advanced Configuration

### Custom Logger Configuration

```java
// Via application.yml
logging:
  level:
    dev.synapse.plugin.example: DEBUG
  file:
    name: plugin.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

Or programmatically:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
ch.qos.logback.classic.Logger logger = 
    loggerContext.getLogger("dev.synapse.plugin.example");
logger.setLevel(Level.DEBUG);
```

### Configuration Profiles

Use Spring profiles for environment-specific configuration:

```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
# Development profile
spring:
  config:
    activate:
      on-profile: dev

synapse:
  plugin:
    example:
      endpoint: "http://localhost:8000"
      enable-caching: false

---
# Production profile
spring:
  config:
    activate:
      on-profile: prod

synapse:
  plugin:
    example:
      endpoint: "https://api.example.com"
      enable-caching: true
```

### Configuration Secrets Management

#### Using Environment Variables (Recommended)

```yaml
synapse:
  plugin:
    example:
      api-key: ${EXAMPLE_API_KEY}
```

```bash
export EXAMPLE_API_KEY=your-secret-key
```

#### Using Spring Cloud Config Server

```yaml
spring:
  cloud:
    config:
      uri: https://config-server.example.com
      
synapse:
  plugin:
    example:
      api-key: ${example.api.key}  # Fetched from config server
```

#### Using Encrypted Properties

```yaml
synapse:
  plugin:
    example:
      api-key: '{cipher}AQBvXZ...'  # Encrypted value
```

## Configuration Best Practices

### 1. Never Hardcode Secrets

❌ **Bad**:
```yaml
api_key: "sk-abc123def456"
```

✅ **Good**:
```yaml
api_key: ${EXAMPLE_API_KEY}
```

### 2. Use Environment-Specific Configs

```
config/
├── base.yaml         # Common settings
├── development.yaml  # Dev overrides
├── staging.yaml      # Staging overrides
└── production.yaml   # Prod overrides
```

### 3. Validate Early

```python
from src.example_plugin import PluginConfig

# Validate config before creating plugin
try:
    config = PluginConfig(**user_config)
except Exception as e:
    print(f"Invalid configuration: {e}")
    exit(1)
```

### 4. Document Custom Settings

If extending the plugin, document new configuration options:

```yaml
# Custom configuration
custom_setting:
  type: string
  description: Description of what this does
  required: false
  default: "value"
```

## Configuration Schema Reference

Complete JSON Schema for configuration:

```json
{
  "type": "object",
  "properties": {
    "api_key": {
      "type": "string",
      "description": "API key for external service",
      "default": "",
      "secret": true
    },
    "endpoint": {
      "type": "string",
      "description": "API endpoint URL",
      "default": "https://api.example.com",
      "pattern": "^https?://"
    },
    "timeout": {
      "type": "integer",
      "description": "Request timeout in seconds",
      "default": 30,
      "minimum": 1,
      "maximum": 300
    },
    "max_retries": {
      "type": "integer",
      "description": "Maximum number of retry attempts",
      "default": 3,
      "minimum": 0,
      "maximum": 10
    },
    "enable_caching": {
      "type": "boolean",
      "description": "Enable response caching",
      "default": true
    },
    "log_level": {
      "type": "string",
      "description": "Logging level",
      "default": "INFO",
      "enum": ["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"]
    }
  }
}
```

## Troubleshooting Configuration Issues

### Issue: Configuration not loading

**Check**:
1. YAML syntax is valid
2. File exists and is readable
3. Environment variables are set

```bash
# Validate YAML
python -c "import yaml; yaml.safe_load(open('plugin.yaml'))"

# Check environment variables
echo $EXAMPLE_API_KEY
```

### Issue: Validation errors

**Check**:
1. All required fields are present
2. Values are within valid ranges
3. Types match expected types

```python
from src.example_plugin import PluginConfig

config = PluginConfig(**your_config)
print(config.dict())  # See actual values
```

### Issue: Secrets not resolving

**Check**:
1. Environment variables are exported
2. Secret manager is accessible
3. Permissions are correct

```bash
# Test environment variable
python -c "import os; print(os.getenv('EXAMPLE_API_KEY'))"
```

## Next Steps

- See [Usage Guide](usage.md) for how to use the configured plugin
- Check [examples/](../examples/) for configuration examples
- Review [Security Guide](security.md) for secure configuration practices
