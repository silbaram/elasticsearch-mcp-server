package com.silbaram.github.mcp.server.elasticsearch.tools;

import com.silbaram.github.infrastructures.elasticsearch.provider.ElasticsearchClusterStatisticsProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ClusterStatisticsToolsService {

    private final ElasticsearchClusterStatisticsProvider elasticsearchClusterStatisticsProvider;

    public ClusterStatisticsToolsService(ElasticsearchClusterStatisticsProvider elasticsearchClusterStatisticsProvider) {
        this.elasticsearchClusterStatisticsProvider = elasticsearchClusterStatisticsProvider;
    }

    @Tool(
            name = "get_cluster_statistics",
            description = "Returns comprehensive cluster statistics including cluster name, UUID, health status, node roles, OS and JVM resource usage, index counts, and shard metrics."
    )
    public Map<String, Object> getClusterStatistics() {
        try {
            return elasticsearchClusterStatisticsProvider.getClusterStatistics();
        } catch (IOException e) {
            return Map.of("Error", e.getMessage());
        }
    }
}
