package com.example.myprovider;

import dev.synapse.plugin.api.AuthMode;
import dev.synapse.plugin.api.CompletionRequest;
import dev.synapse.plugin.api.CompletionResponse;
import dev.synapse.plugin.api.ModelCapabilities;
import dev.synapse.plugin.api.ModelInfo;
import dev.synapse.plugin.api.ModelProvider;
import dev.synapse.plugin.api.PluginContext;
import dev.synapse.plugin.api.StreamHandler;

import java.util.List;

/**
 * Scaffold for a SYNAPSE Model Provider plugin.
 *
 * Steps:
 *  1. Rename this class and its file to match your provider name (PascalCase).
 *  2. Update package, module-info.java, and manifest.yml entry_point to match.
 *  3. Implement all TODO sections. Do not import anything outside synapse-plugin-api.
 */
public class MyModelProviderPlugin implements ModelProvider {

    private PluginContext ctx;

    // ── Identity ─────────────────────────────────────────────────────────────

    @Override
    public String getId() {
        // TODO: return your plugin id from manifest.yml, e.g. "your-author/openai-provider"
        return "your-author/your-provider-name";
    }

    @Override
    public String getName() {
        // TODO: return display name, e.g. "OpenAI Provider"
        return "My Model Provider";
    }

    @Override
    public String getVersion() {
        return "1.0.0"; // TODO: keep in sync with manifest.yml and build file
    }

    @Override
    public String getProviderId() {
        // TODO: return stable provider id from manifest.yml provider_id, e.g. "openai"
        return "your-provider";
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onLoad(PluginContext context) throws Exception {
        this.ctx = context;
        configure(context);
        ctx.logger().info("{} loaded successfully", getName());
    }

    @Override
    public void configure(PluginContext context) throws Exception {
        // Called by core to inject credentials. Check authMode() — never branch on raw config keys.
        if (context.authMode() == AuthMode.API_KEY) {
            String apiKey = context.config().getSecret("api_key");
            // TODO: initialise your HTTP client with apiKey
            // this.client = new MyProviderClient(apiKey);
            ctx.logger().info("Configured with API key auth");

        } else if (context.authMode() == AuthMode.ACP) {
            String subscriptionId = context.config().getSecret("acp_subscription_id");
            // TODO: initialise with ACP subscription
            ctx.logger().info("Configured with ACP subscription auth");

        } else {
            // AuthMode.NONE — e.g. local Ollama, no credentials needed
            // TODO: initialise HTTP client with base_url only
            String baseUrl = context.config().getString("base_url", "http://localhost:11434");
            // this.client = new MyProviderClient(baseUrl);
            ctx.logger().info("Configured with no-auth mode, base_url={}", baseUrl);
        }
    }

    // ── Completion ────────────────────────────────────────────────────────────

    @Override
    public CompletionResponse complete(CompletionRequest request) throws Exception {
        // TODO: make blocking HTTP call to provider API.
        // Use ctx.executor() to avoid tying up the calling thread for long-running requests.
        ctx.logger().debug("complete() model={} messages={}", request.getModel(), request.getMessages().size());

        // Example structure (replace with real HTTP call):
        //   MyProviderResponse response = client.complete(toProviderRequest(request));
        //   return new CompletionResponse(
        //       response.getContent(),
        //       response.getModel(),
        //       response.getUsage().promptTokens(),
        //       response.getUsage().completionTokens(),
        //       response.getFinishReason()
        //   );

        throw new UnsupportedOperationException("TODO: implement complete()");
    }

    @Override
    public void stream(CompletionRequest request, StreamHandler handler) throws Exception {
        // TODO: open a streaming HTTP connection. Call handler.onChunk() for each token.
        // Always call handler.onComplete() or handler.onError() when done — never leave hanging.
        ctx.logger().debug("stream() model={}", request.getModel());

        // Example structure:
        //   try (var stream = client.streamComplete(toProviderRequest(request))) {
        //       stream.forEach(chunk -> handler.onChunk(chunk.text()));
        //       handler.onComplete("stop", stream.totalTokens());
        //   } catch (Exception e) {
        //       handler.onError(e);
        //   }

        handler.onError(new UnsupportedOperationException("TODO: implement stream()"));
    }

    // ── Capabilities & Models ─────────────────────────────────────────────────

    @Override
    public ModelCapabilities getCapabilities() {
        // TODO: declare your provider's actual capabilities
        return ModelCapabilities.builder()
                .streaming(true)
                .toolCalling(false)     // TODO: set true if provider supports tool/function calling
                .vision(false)          // TODO: set true if provider supports image input
                .embeddings(false)      // TODO: set true if provider supports embeddings
                .maxContextTokens(128_000)  // TODO: set to your provider's actual context window
                .build();
    }

    @Override
    public List<ModelInfo> listModels() throws Exception {
        // TODO: fetch available models from the provider API.
        // This may make a network call — handle timeouts gracefully.
        ctx.logger().debug("listModels()");

        // Example:
        //   return client.listModels().stream()
        //       .map(m -> new ModelInfo(m.id(), m.displayName(), m.contextWindow(), m.deprecated()))
        //       .toList();

        return List.of(
                new ModelInfo("your-model-id", "Your Model Name", 128_000, false)  // TODO
        );
    }

    // ── Teardown ──────────────────────────────────────────────────────────────

    @Override
    public void onUnload() throws Exception {
        // TODO: close HTTP clients, cancel in-flight requests, release resources
        ctx.logger().info("{} unloaded", getName());
    }
}
