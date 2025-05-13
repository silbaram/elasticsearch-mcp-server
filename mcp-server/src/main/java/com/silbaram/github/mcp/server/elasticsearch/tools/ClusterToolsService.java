package com.silbaram.github.mcp.server.elasticsearch.tools;

import com.silbaram.github.infrastructures.elasticsearch.provider.ElasticsearchClientProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ClusterToolsService {

    private final ElasticsearchClientProvider elasticsearchClientProvider;

    public ClusterToolsService(ElasticsearchClientProvider elasticsearchClientProvider) {
        this.elasticsearchClientProvider = elasticsearchClientProvider;
    }


    @Tool(
        name = "get_cluster_health",
        description = "Returns basic information about the health of the cluster."
    )
    public Map<String, String> getClusterHealth() {
        try {
            return elasticsearchClientProvider.getClusterHealth();
        } catch (IOException e) {
            return Map.of("Error", e.getMessage());
        }
    }

//    @Tool(description = "Returns high-level overview of cluster statistics.")
//    public Map<String, Object> getClusterStats() {
//        return elasticsearchClientProvider.getClusterStats();
//    }
}
