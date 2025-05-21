package com.silbaram.github.mcp.server.elasticsearch.tools;

import com.silbaram.github.infrastructures.elasticsearch.provider.ElasticsearchSearchProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DocumentSearchToolsService {

    private final ElasticsearchSearchProvider elasticsearchSearchProvider;

    public DocumentSearchToolsService(ElasticsearchSearchProvider elasticsearchSearchProvider) {
        this.elasticsearchSearchProvider = elasticsearchSearchProvider;
    }

    @Tool(
        name = "get_document_search_by_index",
        description = "Search for documents in your Elasticsearch index using queryDsl"
    )
    public String getDocumentSearchByIndex(
        @ToolParam(description = "The name of the elasticsearch index to search")
        String index,
        @ToolParam(description = "elasticsearch Search queryDSL")
        String queryBody
    ) {
        try {
            return elasticsearchSearchProvider.searchByIndex(index, queryBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
