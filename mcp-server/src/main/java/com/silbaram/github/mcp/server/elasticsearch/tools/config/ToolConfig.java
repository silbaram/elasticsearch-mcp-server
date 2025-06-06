package com.silbaram.github.mcp.server.elasticsearch.tools.config;

import com.silbaram.github.mcp.server.elasticsearch.tools.*;
import com.silbaram.github.mcp.server.elasticsearch.tools.ShardAllocationToolsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ToolConfig {

    @Bean
    public ToolCallbackProvider elasticSearchTools(
        ClusterHealthToolsService clusterHealthToolsService,
        MappingsToolsService mappingsToolsService,
        ClusterStatisticsToolsService clusterStatisticsToolsService,
        IndicesToolsService indicesToolsService,
        AliasesToolsService aliasesToolsService,
        DocumentSearchToolsService documentSearchToolsService,
        ShardAllocationToolsService shardAllocationToolsService
    ) {

        List<Object> toolList = new ArrayList<>();
        toolList.add(clusterHealthToolsService);
        toolList.add(mappingsToolsService);
        toolList.add(clusterStatisticsToolsService);
        toolList.add(indicesToolsService);
        toolList.add(aliasesToolsService);
        toolList.add(documentSearchToolsService);
        toolList.add(shardAllocationToolsService);

        return  MethodToolCallbackProvider.builder().toolObjects(toolList.toArray()).build();

    }
}
