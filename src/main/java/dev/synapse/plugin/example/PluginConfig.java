package dev.synapse.plugin.example;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

/**
 * Plugin configuration with validation
 * 
 * This class defines all configurable parameters for the plugin
 * with Spring Boot's configuration properties support.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "synapse.plugin.example")
@Validated
public class PluginConfig {
    
    /**
     * API key for external service
     */
    private String apiKey = "";
    
    /**
     * API endpoint URL
     */
    @Pattern(regexp = "^https?://.*", message = "Endpoint must start with http:// or https://")
    private String endpoint = "https://api.example.com";
    
    /**
     * Request timeout in seconds
     */
    @Min(value = 1, message = "Timeout must be at least 1 second")
    @Max(value = 300, message = "Timeout must not exceed 300 seconds")
    private int timeout = 30;
    
    /**
     * Maximum number of retry attempts
     */
    @Min(value = 0, message = "Max retries must be non-negative")
    @Max(value = 10, message = "Max retries must not exceed 10")
    private int maxRetries = 3;
    
    /**
     * Enable response caching
     */
    private boolean enableCaching = true;
    
    /**
     * Logging level
     */
    @Pattern(regexp = "^(DEBUG|INFO|WARNING|ERROR|CRITICAL)$", 
             message = "Log level must be one of: DEBUG, INFO, WARNING, ERROR, CRITICAL")
    private String logLevel = "INFO";
}
