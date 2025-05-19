package com.silbaram.github.mcp.server.elasticsearch.tools.config;

import com.silbaram.github.mcp.server.elasticsearch.tools.ClusterStatisticsToolsService;
import com.silbaram.github.mcp.server.elasticsearch.tools.ClusterHealthToolsService;
import com.silbaram.github.mcp.server.elasticsearch.tools.IndicesToolsService;
import com.silbaram.github.mcp.server.elasticsearch.tools.MappingsToolsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    private final ClusterHealthToolsService clusterHealthToolsService;
    private final MappingsToolsService mappingsToolsService;
    private final ClusterStatisticsToolsService clusterStatisticsToolsService;
    private final IndicesToolsService indicesToolsService;

    public ToolConfig(ClusterHealthToolsService clusterHealthToolsService, MappingsToolsService mappingsToolsService, ClusterStatisticsToolsService clusterStatisticsToolsService, IndicesToolsService indicesToolsService) {
        this.clusterHealthToolsService = clusterHealthToolsService;
        this.mappingsToolsService = mappingsToolsService;
        this.clusterStatisticsToolsService = clusterStatisticsToolsService;
        this.indicesToolsService = indicesToolsService;
    }

    @Bean
    public ToolCallbackProvider elasticSearchTools() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(
                    clusterHealthToolsService,
                    mappingsToolsService,
                    clusterStatisticsToolsService,
                    indicesToolsService
                )
                .build();
    }
}
