package com.silbaram.github.mcp.server.elasticsearch.tools.config;

import com.silbaram.github.mcp.server.elasticsearch.tools.ClusterStatisticsToolsService;
import com.silbaram.github.mcp.server.elasticsearch.tools.ClusterToolsService;
import com.silbaram.github.mcp.server.elasticsearch.tools.MappingsToolsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    private final ClusterToolsService clusterToolsService;
    private final MappingsToolsService mappingsToolsService;
    private final ClusterStatisticsToolsService clusterStatisticsToolsService;

    public ToolConfig(ClusterToolsService clusterToolsService, MappingsToolsService mappingsToolsService, ClusterStatisticsToolsService clusterStatisticsToolsService) {
        this.clusterToolsService = clusterToolsService;
        this.mappingsToolsService = mappingsToolsService;
        this.clusterStatisticsToolsService = clusterStatisticsToolsService;
    }

    @Bean
    public ToolCallbackProvider elasticSearchTools() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(clusterToolsService, mappingsToolsService, clusterStatisticsToolsService)
                .build();
    }
}
