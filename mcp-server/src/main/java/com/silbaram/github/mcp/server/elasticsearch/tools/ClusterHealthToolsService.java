package com.silbaram.github.mcp.server.elasticsearch.tools;

import com.silbaram.github.infrastructures.elasticsearch.provider.ElasticsearchHealthProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ClusterHealthToolsService {

    private final ElasticsearchHealthProvider elasticsearchHealthProvider;

    public ClusterHealthToolsService(ElasticsearchHealthProvider elasticsearchHealthProvider) {
        this.elasticsearchHealthProvider = elasticsearchHealthProvider;
    }


    @Tool(
        name = "get_cluster_health",
        description = "Returns basic information about the health of the cluster."
    )
    public Map<String, String> getClusterHealth() {
        try {
            return elasticsearchHealthProvider.getClusterHealth();
        } catch (IOException e) {
            return Map.of("Error", e.getMessage());
        }
    }
}
