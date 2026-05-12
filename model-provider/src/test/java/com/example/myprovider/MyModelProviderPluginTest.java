package com.example.myprovider;

import dev.synapse.plugin.api.AuthMode;
import dev.synapse.plugin.api.CompletionRequest;
import dev.synapse.plugin.api.ModelCapabilities;
import dev.synapse.plugin.api.ModelInfo;
import dev.synapse.plugin.api.PluginConfig;
import dev.synapse.plugin.api.PluginContext;
import dev.synapse.plugin.api.PluginEventBus;
import dev.synapse.plugin.api.PluginLogger;
import dev.synapse.plugin.api.StreamHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyModelProviderPluginTest {

    @Mock PluginContext ctx;
    @Mock PluginConfig config;
    @Mock PluginLogger logger;
    @Mock PluginEventBus eventBus;
    @Mock StreamHandler streamHandler;

    MyModelProviderPlugin plugin;

    @BeforeEach
    void setUp() throws Exception {
        when(ctx.logger()).thenReturn(logger);
        when(ctx.config()).thenReturn(config);
        when(ctx.eventBus()).thenReturn(eventBus);
        when(ctx.executor()).thenReturn(Executors.newVirtualThreadPerTaskExecutor());
        when(ctx.authMode()).thenReturn(AuthMode.API_KEY);
        when(config.getSecret("api_key")).thenReturn("test-api-key");

        plugin = new MyModelProviderPlugin();
        plugin.onLoad(ctx);
    }

    @Test
    void id_matchesManifest() {
        // TODO: update expected value to match your manifest.yml id
        assertThat(plugin.getId()).isEqualTo("your-author/your-provider-name");
    }

    @Test
    void providerId_isStable() {
        // TODO: update expected value to match your manifest.yml provider_id
        assertThat(plugin.getProviderId()).isEqualTo("your-provider");
    }

    @Test
    void capabilities_notNull() {
        ModelCapabilities caps = plugin.getCapabilities();
        assertThat(caps).isNotNull();
        // Capabilities must declare a realistic context window
        assertThat(caps.getMaxContextTokens()).isGreaterThan(0);
    }

    @Test
    void listModels_returnsAtLeastOne() throws Exception {
        List<ModelInfo> models = plugin.listModels();
        assertThat(models).isNotEmpty();
        models.forEach(m -> {
            assertThat(m.getId()).isNotBlank();
            assertThat(m.getDisplayName()).isNotBlank();
            assertThat(m.getContextWindow()).isGreaterThan(0);
        });
    }

    @Test
    void onLoad_doesNotThrow() {
        assertThat(plugin).isNotNull();
    }

    @Test
    void onUnload_doesNotThrow() throws Exception {
        plugin.onUnload();
    }

    // TODO: once complete() is implemented, add:
    // @Test void complete_returnsNonEmptyContent() throws Exception { ... }

    // TODO: once stream() is implemented, add:
    // @Test void stream_callsOnCompleteOrOnError() throws Exception { ... }

    // TODO: add test for AuthMode.NONE (Ollama-style) if your provider supports it
}
