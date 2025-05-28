package com.octopus.ai.compoent;

import io.modelcontextprotocol.client.McpClient;
import org.springframework.ai.mcp.customizer.McpAsyncClientCustomizer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CustomMcpAsyncClientCustomizer implements McpAsyncClientCustomizer {
    @Override
    public void customize(String serverConfigurationName, McpClient.AsyncSpec spec) {
        spec.requestTimeout(Duration.ofSeconds(30));
    }
}
