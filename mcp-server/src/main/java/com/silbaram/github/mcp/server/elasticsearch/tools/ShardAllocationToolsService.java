package com.silbaram.github.mcp.server.elasticsearch.tools;

import com.silbaram.github.infrastructures.elasticsearch.provider.ElasticsearchCatAllocationProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service class that provides tools to get shard allocation information from Elasticsearch.
 */
@Service
public class ShardAllocationToolsService {

    private final ElasticsearchCatAllocationProvider elasticsearchCatAllocationProvider;

    /**
     * Constructs a ShardAllocationToolsService with the given ElasticsearchCatAllocationProvider.
     *
     * @param elasticsearchCatAllocationProvider The provider for Elasticsearch cat allocation information.
     */
    public ShardAllocationToolsService(ElasticsearchCatAllocationProvider elasticsearchCatAllocationProvider) {
        this.elasticsearchCatAllocationProvider = elasticsearchCatAllocationProvider;
    }

    /**
     * Retrieves information about shard allocation in the Elasticsearch cluster.
     *
     * @return A list of maps, where each map represents shard allocation information.
     */
    @Tool(
        name = "get_shard_allocation",
        description = "Returns information about shard allocation in the Elasticsearch cluster."
    )
    public List<Map<String, Object>> getShardAllocation() {
        try {
            return elasticsearchCatAllocationProvider.getCatAllocation();
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving shard allocation information: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves shard allocation information for a specific node in the Elasticsearch cluster.
     *
     * @param nodeId The ID of the node.
     * @return A list of maps, where each map represents a shard and its allocation details.
     * @throws RuntimeException if an IOException occurs during the Elasticsearch API call.
     */
    @Tool(
        name = "get_shard_allocation_for_node",
        description = "Returns information about shard allocation for a specific node in the Elasticsearch cluster."
    )
    public List<Map<String, Object>> getShardAllocationForNode(
            @ToolParam(description = "The ID of the node to get shard allocation for.") String nodeId) { // Annotation changed
        try {
            return elasticsearchCatAllocationProvider.getCatAllocation(nodeId);
        } catch (IOException e) {
            // Updated exception throwing:
            throw new RuntimeException("Error retrieving shard allocation information for node " + nodeId + ": " + e.getMessage(), e);
        }
    }
}
