package dev.synapse.plugin.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Example Synapse Plugin Implementation
 * 
 * This class demonstrates a complete plugin implementation with:
 * - Tool registration and execution
 * - Configuration management
 * - Lifecycle hooks
 * - State management
 * - Error handling
 * - Async operations
 */
@Slf4j
@Component
public class ExamplePlugin {
    
    private final PluginConfig config;
    private final PluginState state;
    private final Map<String, SessionData> sessionData;
    
    public ExamplePlugin(PluginConfig config) {
        this.config = config;
        this.state = new PluginState();
        this.sessionData = new ConcurrentHashMap<>();
        
        log.info("ExamplePlugin initialized with config: {}", config);
    }
    
    // -------------------------------------------------------------------------
    // Lifecycle Hooks
    // -------------------------------------------------------------------------
    
    /**
     * Called when the plugin is loaded
     */
    public void onStartup() {
        log.info("Plugin starting up...");
        
        // Perform startup tasks
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            log.info("API key configured");
        }
        
        log.info("Plugin startup complete");
    }
    
    /**
     * Called when the plugin is being unloaded
     */
    public void onShutdown() {
        log.info("Plugin shutting down...");
        
        // Cleanup resources
        if (config.isEnableCaching() && state.getCache() != null) {
            int cacheSize = state.getCache().size();
            log.info("Clearing cache with {} entries", cacheSize);
            state.getCache().clear();
        }
        
        // Log statistics
        log.info("Total executions: {}, Errors: {}", 
            state.getExecutionCount().get(), 
            state.getErrors().size());
        
        log.info("Plugin shutdown complete");
    }
    
    /**
     * Called when a new session starts
     * 
     * @param sessionId Unique session identifier
     * @param context Session context data
     */
    public void onSessionStart(String sessionId, Map<String, Object> context) {
        log.info("Session started: {}", sessionId);
        SessionData data = new SessionData(sessionId, context);
        sessionData.put(sessionId, data);
    }
    
    /**
     * Called when a session ends
     * 
     * @param sessionId Session identifier
     */
    public void onSessionEnd(String sessionId) {
        SessionData data = sessionData.get(sessionId);
        if (data != null) {
            int toolCalls = data.getToolCalls().size();
            log.info("Session ended: {}, Tool calls: {}", sessionId, toolCalls);
            sessionData.remove(sessionId);
        }
    }
    
    /**
     * Called before a tool is executed
     * 
     * @param toolName Name of the tool being executed
     * @param parameters Tool parameters
     * @param sessionId Associated session ID
     */
    public void onToolExecute(String toolName, Map<String, Object> parameters, String sessionId) {
        log.debug("Executing tool: {} with params: {}", toolName, parameters);
        
        if (sessionId != null) {
            SessionData data = sessionData.get(sessionId);
            if (data != null) {
                data.addToolCall(toolName, parameters);
            }
        }
    }
    
    /**
     * Called when an error occurs
     * 
     * @param error The exception that occurred
     * @param context Error context information
     */
    public void onError(Exception error, Map<String, Object> context) {
        ErrorInfo errorInfo = new ErrorInfo(error, context);
        state.getErrors().add(errorInfo);
        log.error("Error occurred: {}", error.getMessage(), error);
    }
    
    // -------------------------------------------------------------------------
    // Tool Implementations
    // -------------------------------------------------------------------------
    
    /**
     * Example tool that processes a query
     * 
     * @param request Tool request containing query and options
     * @return Processing results
     */
    public Map<String, Object> exampleTool(ExampleToolRequest request) {
        log.info("example_tool called with query: {}", request.getQuery());
        state.getExecutionCount().incrementAndGet();
        
        String outputFormat = request.getOptions() != null ? 
            request.getOptions().getOrDefault("format", "json").toString() : "json";
        boolean verbose = request.getOptions() != null && 
            Boolean.TRUE.equals(request.getOptions().get("verbose"));
        
        // Check cache
        String cacheKey = String.format("%s:%s:%s", request.getQuery(), outputFormat, verbose);
        if (config.isEnableCaching() && state.getCache().containsKey(cacheKey)) {
            log.debug("Cache hit for: {}", cacheKey);
            return state.getCache().get(cacheKey);
        }
        
        // Process query
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("query", request.getQuery());
        result.put("format", outputFormat);
        result.put("timestamp", Instant.now().toString());
        
        if ("text".equals(outputFormat)) {
            result.put("processed", request.getQuery().toUpperCase());
        } else {
            Map<String, Object> processed = new HashMap<>();
            processed.put("original", request.getQuery());
            processed.put("length", request.getQuery().length());
            processed.put("words", request.getQuery().split("\\s+").length);
            result.put("processed", processed);
        }
        
        if (verbose) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("execution_count", state.getExecutionCount().get());
            metadata.put("cache_enabled", config.isEnableCaching());
            metadata.put("plugin_version", "1.0.0");
            result.put("metadata", metadata);
        }
        
        // Update cache
        if (config.isEnableCaching()) {
            state.getCache().put(cacheKey, result);
        }
        
        return result;
    }
    
    /**
     * Fetch data from external API
     * 
     * @param request Fetch data request
     * @return Fetched data
     */
    public Map<String, Object> fetchData(FetchDataRequest request) {
        log.info("fetch_data called for resource: {}", request.getResourceId());
        state.getExecutionCount().incrementAndGet();
        
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            log.warn("No API key configured, returning mock data");
            Map<String, Object> result = new HashMap<>();
            result.put("status", "mock");
            result.put("resource_id", request.getResourceId());
            result.put("fields", request.getFields() != null ? request.getFields() : Collections.emptyList());
            result.put("data", Map.of("message", "Mock data - configure api_key for real data"));
            return result;
        }
        
        // In a real implementation, use WebClient or RestTemplate to make API calls
        // This is a placeholder showing the structure
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("resource_id", request.getResourceId());
        result.put("data", Map.of("message", "Real API call would go here"));
        
        return result;
    }
    
    // -------------------------------------------------------------------------
    // Utility Methods
    // -------------------------------------------------------------------------
    
    /**
     * Get current plugin state
     * 
     * @return Plugin state information
     */
    public Map<String, Object> getState() {
        Map<String, Object> stateInfo = new HashMap<>();
        stateInfo.put("config", config);
        
        Map<String, Object> stateData = new HashMap<>();
        stateData.put("initialized_at", state.getInitializedAt());
        stateData.put("execution_count", state.getExecutionCount().get());
        stateData.put("cache", config.isEnableCaching() ? 
            state.getCache().size() + " entries" : "disabled");
        stateData.put("errors", state.getErrors().size());
        stateInfo.put("state", stateData);
        
        stateInfo.put("sessions", sessionData.size());
        
        return stateInfo;
    }
    
    /**
     * Clear the plugin cache
     */
    public void clearCache() {
        if (state.getCache() != null) {
            state.getCache().clear();
            log.info("Cache cleared");
        }
    }
    
    /**
     * Get plugin metrics
     * 
     * @return Metrics information
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("execution_count", state.getExecutionCount().get());
        metrics.put("error_count", state.getErrors().size());
        metrics.put("cache_size", state.getCache().size());
        metrics.put("active_sessions", sessionData.size());
        
        long uptime = Instant.now().getEpochSecond() - state.getInitializedAt().getEpochSecond();
        metrics.put("uptime", uptime);
        
        return metrics;
    }
    
    // -------------------------------------------------------------------------
    // Inner Classes
    // -------------------------------------------------------------------------
    
    /**
     * Plugin state management
     */
    private static class PluginState {
        private final Instant initializedAt = Instant.now();
        private final AtomicLong executionCount = new AtomicLong(0);
        private final Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();
        private final List<ErrorInfo> errors = Collections.synchronizedList(new ArrayList<>());
        
        public Instant getInitializedAt() { return initializedAt; }
        public AtomicLong getExecutionCount() { return executionCount; }
        public Map<String, Map<String, Object>> getCache() { return cache; }
        public List<ErrorInfo> getErrors() { return errors; }
    }
    
    /**
     * Session data tracking
     */
    private static class SessionData {
        private final String sessionId;
        private final Instant startedAt = Instant.now();
        private final Map<String, Object> context;
        private final List<Map<String, Object>> toolCalls = Collections.synchronizedList(new ArrayList<>());
        
        public SessionData(String sessionId, Map<String, Object> context) {
            this.sessionId = sessionId;
            this.context = context;
        }
        
        public void addToolCall(String toolName, Map<String, Object> parameters) {
            Map<String, Object> call = new HashMap<>();
            call.put("tool", toolName);
            call.put("timestamp", Instant.now().toString());
            call.put("parameters", parameters);
            toolCalls.add(call);
        }
        
        public List<Map<String, Object>> getToolCalls() { return toolCalls; }
    }
    
    /**
     * Error information tracking
     */
    private static class ErrorInfo {
        private final Instant timestamp = Instant.now();
        private final String error;
        private final String type;
        private final Map<String, Object> context;
        
        public ErrorInfo(Exception exception, Map<String, Object> context) {
            this.error = exception.getMessage();
            this.type = exception.getClass().getSimpleName();
            this.context = context;
        }
    }
}
