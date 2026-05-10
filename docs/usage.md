# Usage Guide

This guide covers how to use the Example Plugin in your Synapse environment.

## Quick Start

### 1. Installation

Build and install the plugin using Gradle:

```bash
# Build the plugin JAR
./gradlew build

# The JAR will be in build/libs/synapse-example-plugin-1.0.0.jar
```

Or install from Maven/GitHub Packages:

```bash
# Add to your Synapse installation
synapse plugin install dev.synapse.plugin:example-plugin:1.0.0
```

### 2. Basic Configuration

Create an `application.yml` configuration file:

```yaml
synapse:
  plugin:
    example:
      api-key: "your-api-key-here"
      endpoint: "https://api.example.com"
      timeout: 30
      enable-caching: true
```

### 3. Load the Plugin

```java
import dev.synapse.plugin.example.*;

// Initialize with configuration
PluginConfig config = new PluginConfig();
config.setApiKey("your-api-key");
config.setTimeout(60);
config.setLogLevel("DEBUG");

ExamplePlugin plugin = new ExamplePlugin(config);

// Start the plugin
plugin.onStartup();
```

## Using Plugin Tools

### Example Tool

The `example_tool` processes queries with various output formats:

```java
// Basic usage
ExampleToolRequest request = ExampleToolRequest.builder()
    .query("hello world")
    .build();

Map<String, Object> result = plugin.exampleTool(request);
System.out.println(result);
// Output: {
//   status=success,
//   query=hello world,
//   format=json,
//   processed={original=hello world, length=11, words=2}
// }

// Text format
ExampleToolRequest textRequest = ExampleToolRequest.builder()
    .query("hello world")
    .options(Map.of("format", "text"))
    .build();

Map<String, Object> textResult = plugin.exampleTool(textRequest);
// Output: {processed=HELLO WORLD, ...}

// Verbose output
ExampleToolRequest verboseRequest = ExampleToolRequest.builder()
    .query("test")
    .options(Map.of("verbose", true, "format", "json"))
    .build();

Map<String, Object> verboseResult = plugin.exampleTool(verboseRequest);
// Includes metadata about execution
```

### Fetch Data Tool

The `fetch_data` tool retrieves data from external APIs:

```java
// Basic fetch
FetchDataRequest request = FetchDataRequest.builder()
    .resourceId("resource-123")
    .build();

Map<String, Object> result = plugin.fetchData(request);

// Fetch specific fields
FetchDataRequest fieldRequest = FetchDataRequest.builder()
    .resourceId("resource-123")
    .fields(List.of("name", "email", "status"))
    .build();

Map<String, Object> fieldResult = plugin.fetchData(fieldRequest);
```

**Note**: Without an API key, the tool returns mock data. Configure `apiKey` in your config for real API calls.

## Working with Sessions

The plugin supports session tracking:

```java
String sessionId = "my-session-001";

// Start a session
Map<String, Object> context = new HashMap<>();
context.put("user", "john_doe");
context.put("workspace", "/home/john/project");

plugin.onSessionStart(sessionId, context);

// Use tools within the session
Map<String, Object> params = Map.of("query", "test");
plugin.onToolExecute("example_tool", params, sessionId);

ExampleToolRequest request = ExampleToolRequest.builder()
    .query("test query")
    .build();
Map<String, Object> result = plugin.exampleTool(request);

// End the session
plugin.onSessionEnd(sessionId);
```

## Lifecycle Management

### Startup and Shutdown

```java
// Startup - initialize resources
plugin.onStartup();

// Your application logic here
// ...

// Shutdown - cleanup resources
plugin.onShutdown();
```

### Error Handling

The plugin provides error tracking:

```java
try {
    FetchDataRequest request = FetchDataRequest.builder()
        .resourceId("invalid-resource")
        .build();
    Map<String, Object> result = plugin.fetchData(request);
} catch (Exception e) {
    // Plugin automatically logs the error
    plugin.onError(e, Map.of("operation", "fetch_data"));
    
    // Check error history
    Map<String, Object> metrics = plugin.getMetrics();
    System.out.println("Total errors: " + metrics.get("error_count"));
}
```

## Cache Management

The plugin includes built-in caching:

```java
// Enable caching (default)
PluginConfig config = new PluginConfig();
config.setEnableCaching(true);
ExamplePlugin plugin = new ExamplePlugin(config);

// First call - fetches and caches
ExampleToolRequest request = ExampleToolRequest.builder()
    .query("test query")
    .build();
Map<String, Object> result1 = plugin.exampleTool(request);

// Second call - returns cached result
Map<String, Object> result2 = plugin.exampleTool(request);

// Clear cache manually
plugin.clearCache();

// Disable caching
config.setEnableCaching(false);
plugin = new ExamplePlugin(config);
```

## Monitoring and Metrics

### Get Plugin State

```java
Map<String, Object> state = plugin.getState();
System.out.println(state);
// Output:
// {
//   config=PluginConfig(...),
//   state={
//     execution_count=10,
//     cache=5 entries,
//     errors=0
//   },
//   sessions=2
// }
```

### Get Metrics

```java
Map<String, Object> metrics = plugin.getMetrics();
System.out.println(metrics);
// Output:
// {
//   execution_count=10,
//   error_count=0,
//   cache_size=5,
//   active_sessions=2,
//   uptime=3600
// }
```

## Advanced Usage

### Concurrent Operations

Execute multiple tools concurrently using Java's CompletableFuture:

```java
import java.util.concurrent.CompletableFuture;

// Run multiple tools concurrently
CompletableFuture<Map<String, Object>> future1 = 
    CompletableFuture.supplyAsync(() -> plugin.exampleTool(
        ExampleToolRequest.builder().query("query 1").build()
    ));

CompletableFuture<Map<String, Object>> future2 = 
    CompletableFuture.supplyAsync(() -> plugin.fetchData(
        FetchDataRequest.builder().resourceId("resource-1").build()
    ));

// Wait for all to complete
CompletableFuture.allOf(future1, future2).join();

Map<String, Object> result1 = future1.get();
Map<String, Object> result2 = future2.get();
```

### Custom Logging

Configure logging level:

```java
PluginConfig config = new PluginConfig();
config.setLogLevel("DEBUG"); // DEBUG, INFO, WARNING, ERROR, CRITICAL
ExamplePlugin plugin = new ExamplePlugin(config);
```

Or via application.yml:

```yaml
logging:
  level:
    dev.synapse.plugin.example: DEBUG
```

### Retry Configuration

Configure API retry behavior:

```java
PluginConfig config = new PluginConfig();
config.setMaxRetries(5);  // Number of retry attempts
config.setTimeout(60);     // Request timeout in seconds

ExamplePlugin plugin = new ExamplePlugin(config);
```

## Integration with Synapse

When integrated with Synapse, tools are automatically registered:

```yaml
# In your Synapse workspace configuration
synapse:
  plugin:
    example:
      api-key: ${EXAMPLE_API_KEY}  # Use environment variables
      timeout: 30
      enable-caching: true
```

Then use tools through Synapse:

```
User: Use example_tool to process "hello world"

Agent: [Executes plugin.exampleTool("hello world")]
```

## Best Practices

1. **Configuration Management**: Use environment variables for sensitive data:
   ```java
   // In application.yml
   synapse:
     plugin:
       example:
         api-key: ${EXAMPLE_API_KEY}
         endpoint: ${EXAMPLE_ENDPOINT:https://api.example.com}
   ```

2. **Error Handling**: Always wrap tool calls in try-catch:
   ```java
   try {
       Map<String, Object> result = plugin.fetchData(request);
   } catch (Exception e) {
       plugin.onError(e, Map.of("context", "additional info"));
       // Handle error appropriately
   }
   ```

3. **Resource Cleanup**: Always call shutdown:
   ```java
   try {
       plugin.onStartup();
       // Use plugin
   } finally {
       plugin.onShutdown();
   }
   ```

4. **Session Tracking**: Use sessions for multi-turn interactions:
   ```java
   String sessionId = generateSessionId();
   plugin.onSessionStart(sessionId, context);
   // Multiple tool calls
   plugin.onSessionEnd(sessionId);
   ```

## Troubleshooting

### Common Issues

**Issue**: "No class def found error"
- **Solution**: Ensure all dependencies are included: `./gradlew dependencies`

**Issue**: API calls return mock data
- **Solution**: Configure `api-key` in plugin configuration

**Issue**: Cache not working
- **Solution**: Verify `enable-caching: true` in configuration

**Issue**: High memory usage
- **Solution**: Disable caching or clear cache periodically:
  ```java
  plugin.clearCache();
  ```

### Debug Mode

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    dev.synapse.plugin.example: DEBUG
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
- Build and test: `./gradlew test`
