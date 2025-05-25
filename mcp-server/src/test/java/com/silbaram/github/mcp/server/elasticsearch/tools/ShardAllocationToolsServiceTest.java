package com.silbaram.github.mcp.server.elasticsearch.tools;

import com.silbaram.github.infrastructures.elasticsearch.provider.ElasticsearchCatAllocationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ShardAllocationToolsService}.
 */
@ExtendWith(MockitoExtension.class)
public class ShardAllocationToolsServiceTest {

    @Mock
    private ElasticsearchCatAllocationProvider mockElasticsearchCatAllocationProvider;

    @InjectMocks
    private ShardAllocationToolsService shardAllocationToolsService;

    private List<Map<String, Object>> sampleAllocationData;

    /**
     * Sets up common test data before each test.
     */
    @BeforeEach
    void setUp() {
        sampleAllocationData = new ArrayList<>();
        Map<String, Object> node1Data = new HashMap<>();
        node1Data.put("node", "nodeA");
        node1Data.put("shards", "10");
        node1Data.put("disk.indices", "100gb");
        sampleAllocationData.add(node1Data);

        Map<String, Object> node2Data = new HashMap<>();
        node2Data.put("node", "nodeB");
        node2Data.put("shards", "12");
        node2Data.put("disk.indices", "120gb");
        sampleAllocationData.add(node2Data);
    }

    /**
     * Tests {@link ShardAllocationToolsService#getShardAllocation()} for a successful call.
     *
     * @throws IOException if the mocked provider call fails (not expected in this test).
     */
    @Test
    void testGetShardAllocation_Success() throws IOException {
        // Arrange
        when(mockElasticsearchCatAllocationProvider.getCatAllocation()).thenReturn(sampleAllocationData);

        // Act
        List<Map<String, Object>> result = shardAllocationToolsService.getShardAllocation();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("nodeA", result.get(0).get("node"));
        assertEquals("10", result.get(0).get("shards"));
        verify(mockElasticsearchCatAllocationProvider, times(1)).getCatAllocation();
    }

    /**
     * Tests {@link ShardAllocationToolsService#getShardAllocation()} when the provider throws an IOException.
     *
     * @throws IOException if the mocked provider call fails (expected in this test).
     */
    @Test
    void testGetShardAllocation_ProviderThrowsIOException() throws IOException {
        // Arrange
        when(mockElasticsearchCatAllocationProvider.getCatAllocation()).thenThrow(new IOException("Simulated Elasticsearch error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shardAllocationToolsService.getShardAllocation();
        });
        assertTrue(exception.getMessage().contains("Error retrieving shard allocation information"));
        assertTrue(exception.getCause() instanceof IOException);
        verify(mockElasticsearchCatAllocationProvider, times(1)).getCatAllocation();
    }

    /**
     * Tests {@link ShardAllocationToolsService#getShardAllocationForNode(String)} for a successful call.
     *
     * @throws IOException if the mocked provider call fails (not expected in this test).
     */
    @Test
    void testGetShardAllocationForNode_Success() throws IOException {
        // Arrange
        String nodeId = "nodeA";
        List<Map<String, Object>> nodeSpecificData = new ArrayList<>();
        Map<String, Object> nodeData = new HashMap<>();
        nodeData.put("node", nodeId);
        nodeData.put("shards", "10");
        nodeSpecificData.add(nodeData);

        when(mockElasticsearchCatAllocationProvider.getCatAllocation(nodeId)).thenReturn(nodeSpecificData);

        // Act
        List<Map<String, Object>> result = shardAllocationToolsService.getShardAllocationForNode(nodeId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(nodeId, result.get(0).get("node"));
        assertEquals("10", result.get(0).get("shards"));
        verify(mockElasticsearchCatAllocationProvider, times(1)).getCatAllocation(nodeId);
    }

    /**
     * Tests {@link ShardAllocationToolsService#getShardAllocationForNode(String)} when the provider throws an IOException.
     *
     * @throws IOException if the mocked provider call fails (expected in this test).
     */
    @Test
    void testGetShardAllocationForNode_ProviderThrowsIOException() throws IOException {
        // Arrange
        String nodeId = "nodeX";
        when(mockElasticsearchCatAllocationProvider.getCatAllocation(nodeId)).thenThrow(new IOException("Simulated Elasticsearch error for node"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shardAllocationToolsService.getShardAllocationForNode(nodeId);
        });
        assertTrue(exception.getMessage().contains("Error retrieving shard allocation information for node " + nodeId));
        assertTrue(exception.getCause() instanceof IOException);
        verify(mockElasticsearchCatAllocationProvider, times(1)).getCatAllocation(nodeId);
    }
}
