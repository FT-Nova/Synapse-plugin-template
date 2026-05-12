package com.example.myplugin;

import dev.synapse.plugin.api.AuthMode;
import dev.synapse.plugin.api.InboundMessage;
import dev.synapse.plugin.api.OutboundMessage;
import dev.synapse.plugin.api.PluginConfig;
import dev.synapse.plugin.api.PluginContext;
import dev.synapse.plugin.api.PluginEventBus;
import dev.synapse.plugin.api.PluginLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyChannelPluginTest {

    @Mock PluginContext ctx;
    @Mock PluginConfig config;
    @Mock PluginLogger logger;
    @Mock PluginEventBus eventBus;

    MyChannelPlugin plugin;

    @BeforeEach
    void setUp() throws Exception {
        when(ctx.logger()).thenReturn(logger);
        when(ctx.config()).thenReturn(config);
        when(ctx.eventBus()).thenReturn(eventBus);
        when(ctx.executor()).thenReturn(Executors.newVirtualThreadPerTaskExecutor());
        when(ctx.authMode()).thenReturn(AuthMode.API_KEY);
        when(config.getSecret("bot_token")).thenReturn("test-token");

        plugin = new MyChannelPlugin();
        plugin.onLoad(ctx);
    }

    @Test
    void id_matchesManifest() {
        // TODO: update expected value to match your manifest.yml id
        assertThat(plugin.getId()).isEqualTo("your-author/your-channel-name");
    }

    @Test
    void channelId_isStable() {
        // TODO: update expected value to match your manifest.yml channel_id
        assertThat(plugin.getChannelId()).isEqualTo("your-channel");
    }

    @Test
    void onMessage_routesToCore() throws Exception {
        InboundMessage msg = new InboundMessage(
                plugin.getChannelId(), "user-123", "chat-456", "hello", Map.of());

        plugin.onMessage(msg);

        verify(ctx).routeMessage(msg);
    }

    @Test
    void onLoad_doesNotThrow() {
        // Already verified in setUp — if we get here, onLoad succeeded
        assertThat(plugin).isNotNull();
    }

    @Test
    void onUnload_doesNotThrow() throws Exception {
        plugin.onUnload();
        // Should complete without exception
    }

    // TODO: add tests for sendMessage once implemented
    // TODO: add tests for onInstall / onUninstall
    // TODO: add tests for any helper methods you add
}
