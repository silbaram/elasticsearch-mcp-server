package com.silbaram.github.mcp.server.elasticsearch.tools;

import com.silbaram.github.infrastructures.elasticsearch.provider.ElasticsearchMappingsProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MappingsToolsService {

    private final ElasticsearchMappingsProvider elasticsearchMappingsProvider;

    public MappingsToolsService(ElasticsearchMappingsProvider elasticsearchMappingsProvider) {
        this.elasticsearchMappingsProvider = elasticsearchMappingsProvider;
    }

    @Tool(
        name = "get_mappings",
        description = "Get field mappings for a specific Elasticsearch index"
    )
    public String getMappings(
        @ToolParam(description = "Name of the Elasticsearch index to get mappings for")
        String index
    ) {
        try {
            return elasticsearchMappingsProvider.getMappings(index);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
