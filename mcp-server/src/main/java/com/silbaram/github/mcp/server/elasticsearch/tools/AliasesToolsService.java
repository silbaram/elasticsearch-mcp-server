package com.silbaram.github.mcp.server.elasticsearch.tools;

import com.silbaram.github.infrastructures.elasticsearch.provider.ElasticsearchAliasesProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class AliasesToolsService {

    private final ElasticsearchAliasesProvider elasticsearchAliasesProvider;

    public AliasesToolsService(ElasticsearchAliasesProvider elasticsearchAliasesProvider) {
        this.elasticsearchAliasesProvider = elasticsearchAliasesProvider;
    }

    @Tool(
        name = "get_cat_aliases",
        description = "Get a list of all aliases Elasticsearch."
    )
    public List<Map<String, Object>> getCatAliases() {
        try {
            return elasticsearchAliasesProvider.getCatAliases();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Tool(
        name = "get_cat_aliases_by_name",
        description = "Get only the aliases matching the specified alias name or wildcard pattern."
    )
    public List<Map<String, Object>> getCatAliasesByName(
        @ToolParam(description = "Alias name or wildcard pattern to filter")
        String aliasName
    ) {
        try {
            return elasticsearchAliasesProvider.getCatAliasesByName(aliasName);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
