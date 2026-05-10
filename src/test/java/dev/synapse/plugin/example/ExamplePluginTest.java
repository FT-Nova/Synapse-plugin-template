package dev.synapse.plugin.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ExamplePlugin
 */
@SpringBootTest
@DisplayName("ExamplePlugin Tests")
class ExamplePluginTest {
    
    private ExamplePlugin plugin;
    private PluginConfig config;
    
    @BeforeEach
    void setUp() {
        config = new PluginConfig();
        config.setApiKey("");
        config.setEndpoint("https://api.example.com");
        config.setTimeout(30);
        config.setMaxRetries(3);
        config.setEnableCaching(true);
        config.setLogLevel("INFO");
        
        plugin = new ExamplePlugin(config);
        plugin.onStartup();
    }
    
    @Test
    @DisplayName("Should initialize plugin successfully")
    void testPluginInitialization() {
        assertThat(plugin).isNotNull();
        Map<String, Object> state = plugin.getState();
        assertThat(state).containsKey("config");
        assertThat(state).containsKey("state");
        assertThat(state).containsKey("sessions");
    }
    
    @Test
    @DisplayName("Should process query with example_tool in JSON format")
    void testExampleToolJsonFormat() {
        ExampleToolRequest request = ExampleToolRequest.builder()
            .query("hello world")
            .options(Map.of("format", "json"))
            .build();
        
        Map<String, Object> result = plugin.exampleTool(request);
        
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("success");
        assertThat(result.get("query")).isEqualTo("hello world");
        assertThat(result.get("format")).isEqualTo("json");
        assertThat(result).containsKey("processed");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> processed = (Map<String, Object>) result.get("processed");
        assertThat(processed.get("original")).isEqualTo("hello world");
        assertThat(processed.get("length")).isEqualTo(11);
        assertThat(processed.get("words")).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should process query with example_tool in text format")
    void testExampleToolTextFormat() {
        ExampleToolRequest request = ExampleToolRequest.builder()
            .query("test query")
            .options(Map.of("format", "text"))
            .build();
        
        Map<String, Object> result = plugin.exampleTool(request);
        
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("success");
        assertThat(result.get("format")).isEqualTo("text");
        assertThat(result.get("processed")).isEqualTo("TEST QUERY");
    }
    
    @Test
    @DisplayName("Should include metadata when verbose is true")
    void testExampleToolVerboseMode() {
        ExampleToolRequest request = ExampleToolRequest.builder()
            .query("test")
            .options(Map.of("verbose", true))
            .build();
        
        Map<String, Object> result = plugin.exampleTool(request);
        
        assertThat(result).containsKey("metadata");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) result.get("metadata");
        assertThat(metadata).containsKey("execution_count");
        assertThat(metadata).containsKey("cache_enabled");
        assertThat(metadata).containsKey("plugin_version");
    }
    
    @Test
    @DisplayName("Should use cache for repeated queries")
    void testCaching() {
        ExampleToolRequest request = ExampleToolRequest.builder()
            .query("cached query")
            .options(Map.of("format", "json"))
            .build();
        
        Map<String, Object> result1 = plugin.exampleTool(request);
        Map<String, Object> result2 = plugin.exampleTool(request);
        
        // Should be the same instance from cache
        assertThat(result1).isSameAs(result2);
    }
    
    @Test
    @DisplayName("Should clear cache when requested")
    void testClearCache() {
        ExampleToolRequest request = ExampleToolRequest.builder()
            .query("test")
            .build();
        
        plugin.exampleTool(request);
        Map<String, Object> metrics1 = plugin.getMetrics();
        assertThat(metrics1.get("cache_size")).isEqualTo(1);
        
        plugin.clearCache();
        Map<String, Object> metrics2 = plugin.getMetrics();
        assertThat(metrics2.get("cache_size")).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should return mock data when no API key configured")
    void testFetchDataMockMode() {
        FetchDataRequest request = FetchDataRequest.builder()
            .resourceId("test-123")
            .fields(List.of("name", "email"))
            .build();
        
        Map<String, Object> result = plugin.fetchData(request);
        
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("mock");
        assertThat(result.get("resource_id")).isEqualTo("test-123");
        assertThat(result).containsKey("data");
    }
    
    @Test
    @DisplayName("Should track session lifecycle")
    void testSessionTracking() {
        String sessionId = "test-session-123";
        Map<String, Object> context = new HashMap<>();
        context.put("user", "test_user");
        
        plugin.onSessionStart(sessionId, context);
        
        Map<String, Object> stateBefore = plugin.getState();
        assertThat(stateBefore.get("sessions")).isEqualTo(1);
        
        plugin.onSessionEnd(sessionId);
        
        Map<String, Object> stateAfter = plugin.getState();
        assertThat(stateAfter.get("sessions")).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should track tool execution in session")
    void testToolExecutionTracking() {
        String sessionId = "test-session-456";
        Map<String, Object> context = new HashMap<>();
        
        plugin.onSessionStart(sessionId, context);
        
        Map<String, Object> params = Map.of("query", "test");
        plugin.onToolExecute("example_tool", params, sessionId);
        
        plugin.onSessionEnd(sessionId);
        
        // Verify the session was tracked (check logs or internal state if exposed)
        assertThat(plugin.getState().get("sessions")).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should track errors when they occur")
    void testErrorTracking() {
        Exception testException = new RuntimeException("Test error");
        Map<String, Object> context = Map.of("operation", "test");
        
        plugin.onError(testException, context);
        
        Map<String, Object> metrics = plugin.getMetrics();
        assertThat(metrics.get("error_count")).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should increment execution count")
    void testExecutionCount() {
        Map<String, Object> metricsBefore = plugin.getMetrics();
        long countBefore = (long) metricsBefore.get("execution_count");
        
        ExampleToolRequest request = ExampleToolRequest.builder()
            .query("test")
            .build();
        plugin.exampleTool(request);
        
        Map<String, Object> metricsAfter = plugin.getMetrics();
        long countAfter = (long) metricsAfter.get("execution_count");
        
        assertThat(countAfter).isEqualTo(countBefore + 1);
    }
    
    @Test
    @DisplayName("Should calculate uptime correctly")
    void testUptime() throws InterruptedException {
        Thread.sleep(100); // Sleep for 100ms
        
        Map<String, Object> metrics = plugin.getMetrics();
        long uptime = (long) metrics.get("uptime");
        
        assertThat(uptime).isGreaterThanOrEqualTo(0);
    }
    
    @Test
    @DisplayName("Should handle plugin shutdown gracefully")
    void testPluginShutdown() {
        ExampleToolRequest request = ExampleToolRequest.builder()
            .query("test")
            .build();
        plugin.exampleTool(request);
        
        // Should not throw exception
        assertThatCode(() -> plugin.onShutdown()).doesNotThrowAnyException();
    }
}
