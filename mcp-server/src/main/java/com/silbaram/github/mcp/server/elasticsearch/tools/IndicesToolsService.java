package com.silbaram.github.mcp.server.elasticsearch.tools;

import com.silbaram.github.infrastructures.elasticsearch.provider.ElasticsearchIndicesProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class IndicesToolsService {

    private final ElasticsearchIndicesProvider elasticsearchIndicesProvider;

    public IndicesToolsService(ElasticsearchIndicesProvider elasticsearchIndicesProvider) {
        this.elasticsearchIndicesProvider = elasticsearchIndicesProvider;
    }

    @Tool(
        name = "get_cat_indices",
        description = "Get a list of all indices in Elasticsearch."
    )
    public List<Map<String, Object>> getCatIndices() {
        try {
            return elasticsearchIndicesProvider.getCatIndices();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Tool(
        name = "get_cat_indices_by_name",
        description = "Get a list of indices matching the specified index name or pattern."
    )
    public List<Map<String, Object>> getCatIndicesByName(
        @ToolParam(description = "Index name or pattern to filter indices by")
        String indexName
    ) {
        try {
            return elasticsearchIndicesProvider.getCatIndicesByName(indexName);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
