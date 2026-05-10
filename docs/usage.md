# Usage Guide

This guide covers how to use the Example Plugin in your Synapse environment.

## Quick Start

### 1. Installation

Install the plugin using pip:

```bash
pip install synapse-example-plugin
```

Or install from source:

```bash
git clone https://github.com/yourusername/synapse-example-plugin
cd synapse-example-plugin
pip install -r requirements.txt
```

### 2. Basic Configuration

Create a `plugin.yaml` configuration file:

```yaml
name: example-plugin
version: 1.0.0

config_schema:
  api_key: "your-api-key-here"
  endpoint: "https://api.example.com"
  timeout: 30
  enable_caching: true
```

### 3. Load the Plugin

```python
from src.example_plugin import ExamplePlugin

# Initialize with configuration
config = {
    "api_key": "your-api-key",
    "timeout": 60,
    "log_level": "DEBUG"
}

plugin = ExamplePlugin(config)

# Start the plugin
await plugin.on_startup()
```

## Using Plugin Tools

### Example Tool

The `example_tool` processes queries with various output formats:

```python
# Basic usage
result = await plugin.example_tool("hello world")
print(result)
# Output: {
#   "status": "success",
#   "query": "hello world",
#   "format": "json",
#   "processed": {"original": "hello world", "length": 11, "words": 2}
# }

# Text format
result = await plugin.example_tool(
    "hello world",
    options={"format": "text"}
)
# Output: {"processed": "HELLO WORLD", ...}

# Verbose output
result = await plugin.example_tool(
    "test",
    options={"verbose": True, "format": "json"}
)
# Includes metadata about execution
```

### Fetch Data Tool

The `fetch_data` tool retrieves data from external APIs:

```python
# Basic fetch
result = await plugin.fetch_data("resource-123")

# Fetch specific fields
result = await plugin.fetch_data(
    "resource-123",
    fields=["name", "email", "status"]
)
```

**Note**: Without an API key, the tool returns mock data. Configure `api_key` in your config for real API calls.

## Working with Sessions

The plugin supports session tracking:

```python
session_id = "my-session-001"

# Start a session
await plugin.on_session_start(session_id, {
    "user": "john_doe",
    "workspace": "/home/john/project"
})

# Use tools within the session
await plugin.on_tool_execute("example_tool", {"query": "test"}, session_id)
result = await plugin.example_tool("test query")

# End the session
await plugin.on_session_end(session_id)
```

## Lifecycle Management

### Startup and Shutdown

```python
# Startup - initialize resources
await plugin.on_startup()

# Your application logic here
# ...

# Shutdown - cleanup resources
await plugin.on_shutdown()
```

### Error Handling

The plugin provides error tracking:

```python
try:
    result = await plugin.fetch_data("invalid-resource")
except Exception as e:
    # Plugin automatically logs the error
    await plugin.on_error(e, {"operation": "fetch_data"})
    
    # Check error history
    errors = plugin.state["errors"]
    print(f"Total errors: {len(errors)}")
```

## Cache Management

The plugin includes built-in caching:

```python
# Enable caching (default)
plugin = ExamplePlugin({"enable_caching": True})

# First call - fetches and caches
result1 = await plugin.example_tool("test query")

# Second call - returns cached result
result2 = await plugin.example_tool("test query")

# Clear cache manually
plugin.clear_cache()

# Disable caching
plugin = ExamplePlugin({"enable_caching": False})
```

## Monitoring and Metrics

### Get Plugin State

```python
state = plugin.get_state()
print(state)
# Output:
# {
#   "config": {...},
#   "state": {
#     "execution_count": 10,
#     "cache": "5 entries",
#     "errors": []
#   },
#   "sessions": 2
# }
```

### Get Metrics

```python
metrics = plugin.get_metrics()
print(metrics)
# Output:
# {
#   "execution_count": 10,
#   "error_count": 0,
#   "cache_size": 5,
#   "active_sessions": 2,
#   "uptime": 3600.5
# }
```

## Advanced Usage

### Async Operations

All plugin tools support async execution:

```python
import asyncio

# Run multiple tools concurrently
results = await asyncio.gather(
    plugin.example_tool("query 1"),
    plugin.example_tool("query 2"),
    plugin.fetch_data("resource-1"),
    plugin.fetch_data("resource-2")
)
```

### Custom Logging

Configure logging level:

```python
plugin = ExamplePlugin({
    "log_level": "DEBUG"  # DEBUG, INFO, WARNING, ERROR, CRITICAL
})
```

### Retry Configuration

Configure API retry behavior:

```python
plugin = ExamplePlugin({
    "max_retries": 5,  # Number of retry attempts
    "timeout": 60      # Request timeout in seconds
})
```

## Integration with Synapse

When integrated with Synapse, tools are automatically registered:

```yaml
# In your Synapse workspace configuration
plugins:
  - name: example-plugin
    enabled: true
    config:
      api_key: ${EXAMPLE_API_KEY}  # Use environment variables
      timeout: 30
      enable_caching: true
```

Then use tools through Synapse:

```
User: Use example_tool to process "hello world"

Agent: [Executes plugin.example_tool("hello world")]
```

## Best Practices

1. **Configuration Management**: Use environment variables for sensitive data:
   ```python
   import os
   
   config = {
       "api_key": os.getenv("EXAMPLE_API_KEY"),
       "endpoint": os.getenv("EXAMPLE_ENDPOINT", "https://api.example.com")
   }
   ```

2. **Error Handling**: Always wrap tool calls in try-except:
   ```python
   try:
       result = await plugin.fetch_data("resource-id")
   except Exception as e:
       await plugin.on_error(e, {"context": "additional info"})
       # Handle error appropriately
   ```

3. **Resource Cleanup**: Always call shutdown:
   ```python
   try:
       await plugin.on_startup()
       # Use plugin
   finally:
       await plugin.on_shutdown()
   ```

4. **Session Tracking**: Use sessions for multi-turn interactions:
   ```python
   session_id = generate_session_id()
   await plugin.on_session_start(session_id, context)
   # Multiple tool calls
   await plugin.on_session_end(session_id)
   ```

## Troubleshooting

### Common Issues

**Issue**: "requests library not available"
- **Solution**: Install dependencies: `pip install -r requirements.txt`

**Issue**: API calls return mock data
- **Solution**: Configure `api_key` in plugin configuration

**Issue**: Cache not working
- **Solution**: Verify `enable_caching: true` in configuration

**Issue**: High memory usage
- **Solution**: Disable caching or clear cache periodically:
  ```python
  plugin.clear_cache()
  ```

### Debug Mode

Enable debug logging for troubleshooting:

```python
plugin = ExamplePlugin({"log_level": "DEBUG"})
```

This will output detailed information about:
- Tool execution
- Cache hits/misses
- API requests
- Session events
- Errors

## Next Steps

- Read [Configuration Guide](configuration.md) for detailed configuration options
- Check [examples/](../examples/) for more usage examples
- See [API Documentation](api.md) for complete API reference
