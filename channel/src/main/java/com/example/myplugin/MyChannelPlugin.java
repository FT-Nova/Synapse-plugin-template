package com.example.myplugin;

import dev.synapse.plugin.api.Channel;
import dev.synapse.plugin.api.InboundMessage;
import dev.synapse.plugin.api.OutboundMessage;
import dev.synapse.plugin.api.PluginContext;

import java.util.Map;

/**
 * Scaffold for a SYNAPSE Channel plugin.
 *
 * Steps:
 *  1. Rename this class and its file to match your plugin name (PascalCase).
 *  2. Update package, module-info.java, and manifest.yml entry_point to match.
 *  3. Implement all TODO sections. Do not import anything outside synapse-plugin-api.
 */
public class MyChannelPlugin implements Channel {

    private PluginContext ctx;

    // ── Identity ─────────────────────────────────────────────────────────────

    @Override
    public String getId() {
        // TODO: return your plugin id from manifest.yml, e.g. "your-author/telegram-channel"
        return "your-author/your-channel-name";
    }

    @Override
    public String getName() {
        // TODO: return display name, e.g. "Telegram Channel"
        return "My Channel Plugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0"; // TODO: keep in sync with manifest.yml and build file
    }

    @Override
    public String getChannelId() {
        // TODO: return stable platform id from manifest.yml channel_id, e.g. "telegram"
        return "your-channel";
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onLoad(PluginContext context) throws Exception {
        this.ctx = context;

        // TODO: read config, validate credentials, initialise HTTP client, etc.
        // Example:
        //   String token = context.config().getSecret("bot_token");
        //   this.client = new MyPlatformClient(token);

        ctx.logger().info("{} loaded successfully", getName());
    }

    @Override
    public void onInstall() throws Exception {
        // Called once after onLoad when the channel is first installed.
        // TODO: register webhook with the platform, or start a polling loop via ctx.executor()
        // Example (webhook):
        //   client.registerWebhook("https://your-synapse-instance/webhooks/" + getChannelId());
        // Example (polling):
        //   ctx.executor().submit(this::pollLoop);

        ctx.logger().info("{} installed — set up platform integration here", getName());
    }

    @Override
    public void onMessage(InboundMessage message) throws Exception {
        // Called by core when this channel receives a message from the platform.
        // TODO: optionally pre-process the message (parse markdown, resolve user ids, etc.)
        // Then route it to the SYNAPSE agent router:

        ctx.logger().debug("Received message from user={} conversation={}",
                message.getExternalUserId(), message.getConversationId());

        ctx.routeMessage(message);
    }

    @Override
    public void sendMessage(OutboundMessage message) throws Exception {
        // Called by core when an agent produces a reply for this channel.
        // TODO: deliver message.getText() to message.getExternalUserId() via platform API
        // Example:
        //   client.sendMessage(message.getConversationId(), message.getText());

        ctx.logger().debug("Sending reply to user={} conversation={}",
                message.getExternalUserId(), message.getConversationId());

        throw new UnsupportedOperationException("TODO: implement sendMessage");
    }

    @Override
    public void onUninstall() throws Exception {
        // Called before the channel is removed.
        // TODO: deregister webhook, stop polling loop, clean up platform state
        ctx.logger().info("{} uninstalling", getName());
    }

    @Override
    public void onUnload() throws Exception {
        // Called before ClassLoader teardown.
        // TODO: close HTTP clients, cancel futures, flush buffers
        ctx.logger().info("{} unloaded", getName());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Helper: build an InboundMessage from platform-specific data.
     * Adjust fields to match your platform's message model.
     */
    private InboundMessage buildMessage(String userId, String chatId, String text, Map<String, Object> extra) {
        return new InboundMessage(getChannelId(), userId, chatId, text, extra);
    }
}
