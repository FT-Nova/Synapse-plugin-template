package dev.synapse.plugin.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Request object for the example_tool
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleToolRequest {
    
    /**
     * Input query to process
     */
    @NotBlank(message = "Query is required")
    private String query;
    
    /**
     * Optional processing options
     * - format: Output format (json, text, xml)
     * - verbose: Enable verbose output
     */
    private Map<String, Object> options;
}
