package com.silbaram.github.mcp.server.elasticsearch.tools.config;

import com.silbaram.github.mcp.server.elasticsearch.tools.MappingsToolService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ToolConfig {

    private final MappingsToolService mappingsToolService;

    public ToolConfig(MappingsToolService mappingsToolService) {
        this.mappingsToolService = mappingsToolService;
    }

    @Bean
    public ToolCallbackProvider elasticSearchTools() {
        return MethodToolCallbackProvider.builder().toolObjects(mappingsToolService).build();
    }
}
