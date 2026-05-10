# Configuration Guide

Complete reference for configuring the Example Plugin.

## Configuration File Format

The plugin uses YAML configuration. The main configuration is defined in `plugin.yaml`:

```yaml
name: example-plugin
version: 1.0.0
description: An example Synapse plugin
author: Your Name <your.email@example.com>
license: MIT

# Plugin configuration
config_schema:
  api_key: ""
  endpoint: "https://api.example.com"
  timeout: 30
  max_retries: 3
  enable_caching: true
  log_level: "INFO"
```

## Configuration Parameters

### Core Settings

#### `api_key`
- **Type**: `string`
- **Required**: No
- **Default**: `""` (empty string)
- **Secret**: Yes (marked as sensitive)
- **Description**: API key for authenticating with external services

**Example**:
```yaml
api_key: "sk-abc123def456ghi789"
```

**Best Practice**: Use environment variables:
```yaml
api_key: ${EXAMPLE_API_KEY}
```

#### `endpoint`
- **Type**: `string`
- **Required**: No
- **Default**: `"https://api.example.com"`
- **Validation**: Must start with `http://` or `https://`
- **Description**: Base URL for API requests

**Example**:
```yaml
endpoint: "https://custom-api.mycompany.com"
```

#### `timeout`
- **Type**: `integer`
- **Required**: No
- **Default**: `30`
- **Range**: 1-300 seconds
- **Description**: Maximum time to wait for API responses

**Example**:
```yaml
timeout: 60  # 60 seconds
```

**Recommendations**:
- Use 30s for normal operations
- Use 60-120s for slow APIs
- Use 5-10s for fast, local services

#### `max_retries`
- **Type**: `integer`
- **Required**: No
- **Default**: `3`
- **Range**: 0-10
- **Description**: Number of retry attempts for failed requests

**Example**:
```yaml
max_retries: 5
```

**Note**: Retry delay uses exponential backoff (2^attempt seconds)

#### `enable_caching`
- **Type**: `boolean`
- **Required**: No
- **Default**: `true`
- **Description**: Enable/disable response caching

**Example**:
```yaml
enable_caching: false  # Disable caching
```

**When to disable**:
- Real-time data requirements
- Limited memory environments
- Testing scenarios

#### `log_level`
- **Type**: `string`
- **Required**: No
- **Default**: `"INFO"`
- **Valid Values**: `DEBUG`, `INFO`, `WARNING`, `ERROR`, `CRITICAL`
- **Description**: Logging verbosity level

**Example**:
```yaml
log_level: "DEBUG"
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

Requires `api_key` for real API calls. Without it, returns mock data.

## Environment-Specific Configurations

### Development

```yaml
config_schema:
  api_key: ${DEV_API_KEY}
  endpoint: "http://localhost:8000"
  timeout: 60
  max_retries: 1
  enable_caching: false  # Fresh data for testing
  log_level: "DEBUG"
```

### Staging

```yaml
config_schema:
  api_key: ${STAGING_API_KEY}
  endpoint: "https://staging-api.example.com"
  timeout: 30
  max_retries: 3
  enable_caching: true
  log_level: "INFO"
```

### Production

```yaml
config_schema:
  api_key: ${PROD_API_KEY}
  endpoint: "https://api.example.com"
  timeout: 30
  max_retries: 5
  enable_caching: true
  log_level: "WARNING"
```

## Programmatic Configuration

### Python Dictionary

```python
from src.example_plugin import ExamplePlugin

config = {
    "api_key": "your-key",
    "endpoint": "https://api.example.com",
    "timeout": 60,
    "max_retries": 3,
    "enable_caching": True,
    "log_level": "DEBUG"
}

plugin = ExamplePlugin(config)
```

### Using Pydantic Model

```python
from src.example_plugin import PluginConfig, ExamplePlugin

config = PluginConfig(
    api_key="your-key",
    timeout=60,
    log_level="DEBUG"
)

plugin = ExamplePlugin(config.dict())
```

### Environment Variables

```python
import os
from src.example_plugin import ExamplePlugin

config = {
    "api_key": os.getenv("EXAMPLE_API_KEY"),
    "endpoint": os.getenv("EXAMPLE_ENDPOINT", "https://api.example.com"),
    "timeout": int(os.getenv("EXAMPLE_TIMEOUT", "30")),
    "log_level": os.getenv("EXAMPLE_LOG_LEVEL", "INFO")
}

plugin = ExamplePlugin(config)
```

## Configuration Validation

The plugin automatically validates configuration on initialization:

```python
from pydantic import ValidationError
from src.example_plugin import ExamplePlugin

try:
    plugin = ExamplePlugin({
        "timeout": 500,  # Invalid: exceeds maximum
        "log_level": "INVALID"  # Invalid: not in enum
    })
except ValidationError as e:
    print(e)
    # Shows detailed validation errors
```

## Advanced Configuration

### Custom Logger Configuration

```python
import logging
from src.example_plugin import ExamplePlugin

# Configure custom logger
logger = logging.getLogger("synapse.plugin.ExamplePlugin")
logger.setLevel(logging.DEBUG)

handler = logging.FileHandler("plugin.log")
handler.setFormatter(logging.Formatter(
    '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
))
logger.addHandler(handler)

# Initialize plugin
plugin = ExamplePlugin({"log_level": "DEBUG"})
```

### Dynamic Configuration Updates

```python
plugin = ExamplePlugin({"timeout": 30})

# Update configuration at runtime
plugin.config.timeout = 60
plugin.config.log_level = "DEBUG"

# Note: Some changes may require plugin restart
```

### Configuration Secrets Management

#### Using AWS Secrets Manager

```python
import boto3
import json
from src.example_plugin import ExamplePlugin

def get_secret(secret_name):
    client = boto3.client('secretsmanager')
    response = client.get_secret_value(SecretId=secret_name)
    return json.loads(response['SecretString'])

secrets = get_secret('example-plugin-secrets')

config = {
    "api_key": secrets['api_key'],
    "endpoint": secrets['endpoint']
}

plugin = ExamplePlugin(config)
```

#### Using HashiCorp Vault

```python
import hvac
from src.example_plugin import ExamplePlugin

client = hvac.Client(url='https://vault.example.com')
client.token = 'your-vault-token'

secrets = client.secrets.kv.v2.read_secret_version(
    path='example-plugin'
)

config = {
    "api_key": secrets['data']['data']['api_key'],
    "endpoint": secrets['data']['data']['endpoint']
}

plugin = ExamplePlugin(config)
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
