package dev.synapse.plugin.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request object for the fetch_data tool
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchDataRequest {
    
    /**
     * Resource identifier to fetch
     */
    @NotBlank(message = "Resource ID is required")
    private String resourceId;
    
    /**
     * Specific fields to retrieve
     */
    private List<String> fields;
}
