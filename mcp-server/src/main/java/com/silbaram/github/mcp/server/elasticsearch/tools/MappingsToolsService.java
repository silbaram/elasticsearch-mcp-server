package com.silbaram.github.mcp.server.elasticsearch.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silbaram.github.infrastructures.elasticsearch.provider.ElasticsearchMappingsProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class MappingsToolsService {

    private final ElasticsearchMappingsProvider elasticsearchMappingsProvider;
    private final ObjectMapper objectMapper;

    public MappingsToolsService(ElasticsearchMappingsProvider elasticsearchMappingsProvider, ObjectMapper objectMapper) {
        this.elasticsearchMappingsProvider = elasticsearchMappingsProvider;
        this.objectMapper = objectMapper;
    }

    @Tool(
        name = "get_mappings",
        description = "Get field mappings for a specific Elasticsearch index"
    )
    public List<String> getMappings(
        @ToolParam(description = "Name of the Elasticsearch index to get mappings for")
        String index
    ) {
        try {
            Object mappingObj = elasticsearchMappingsProvider.getMappings(index);

            String prettyJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(mappingObj);

            return List.of("Mappings for index: " + index, prettyJson);
        } catch (IOException e) {
            return List.of("Error: " + e.getMessage());
        }
    }
}
