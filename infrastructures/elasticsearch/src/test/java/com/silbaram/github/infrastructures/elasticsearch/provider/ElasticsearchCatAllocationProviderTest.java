package com.silbaram.github.infrastructures.elasticsearch.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ElasticsearchCatAllocationProvider}.
 */
@ExtendWith(MockitoExtension.class)
public class ElasticsearchCatAllocationProviderTest {

    @Mock
    private RestClient mockRestClient;

    @Mock
    private Response mockResponse;

    @InjectMocks
    private ElasticsearchCatAllocationProvider provider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Sets up mocks before each test.
     * Note: MockitoAnnotations.openMocks(this) is not strictly needed with MockitoExtension,
     * but can be useful for more complex setups or if not using the extension.
     */
    @BeforeEach
    void setUp() {
        // Initialization is handled by @ExtendWith(MockitoExtension.class)
        // and @Mock / @InjectMocks annotations.
    }

    /**
     * Tests {@link ElasticsearchCatAllocationProvider#getCatAllocation()} for a successful API call.
     *
     * @throws IOException if the simulated API call fails, which is part of the test.
     */
    @Test
    void testGetCatAllocation_Success() throws IOException {
        String jsonResponse = "[{\"node\": \"node1\", \"shards\": \"10\"}, {\"node\": \"node2\", \"shards\": \"5\"}]";
        HttpEntity entity = new NStringEntity(jsonResponse, ContentType.APPLICATION_JSON);

        when(mockResponse.getEntity()).thenReturn(entity);
        when(mockRestClient.performRequest(ArgumentMatchers.any(Request.class))).thenReturn(mockResponse);

        List<Map<String, Object>> result = provider.getCatAllocation();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("node1", result.get(0).get("node"));
        assertEquals("10", result.get(0).get("shards"));
        assertEquals("node2", result.get(1).get("node"));
        assertEquals("5", result.get(1).get("shards"));
    }

    /**
     * Tests {@link ElasticsearchCatAllocationProvider#getCatAllocation()} when an IOException occurs.
     *
     * @throws IOException if the simulated API call fails, which is part of the test.
     */
    @Test
    void testGetCatAllocation_IOException() throws IOException {
        when(mockRestClient.performRequest(ArgumentMatchers.any(Request.class))).thenThrow(new IOException("Simulated API error"));

        assertThrows(IOException.class, () -> provider.getCatAllocation());
    }

    /**
     * Tests {@link ElasticsearchCatAllocationProvider#getCatAllocation(String)} for a successful API call with a valid nodeId.
     *
     * @throws IOException if the simulated API call fails, which is part of the test.
     */
    @Test
    void testGetCatAllocation_WithNodeId_Success() throws IOException {
        String nodeId = "node1";
        String jsonResponse = "[{\"node\": \"node1\", \"shards\": \"10\"}]";
        HttpEntity entity = new NStringEntity(jsonResponse, ContentType.APPLICATION_JSON);

        // Mock the response from the REST client
        Request expectedRequest = new Request("GET", "/_cat/allocation/" + nodeId + "?format=json");

        when(mockResponse.getEntity()).thenReturn(entity);
        // We need to be more specific with request matching if the path changes
        when(mockRestClient.performRequest(ArgumentMatchers.argThat(request ->
                request.getMethod().equals(expectedRequest.getMethod()) &&
                request.getEndpoint().equals(expectedRequest.getEndpoint())
        ))).thenReturn(mockResponse);


        List<Map<String, Object>> result = provider.getCatAllocation(nodeId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("node1", result.get(0).get("node"));
        assertEquals("10", result.get(0).get("shards"));
    }

    /**
     * Tests {@link ElasticsearchCatAllocationProvider#getCatAllocation(String)} when an IOException occurs.
     *
     * @throws IOException if the simulated API call fails, which is part of the test.
     */
    @Test
    void testGetCatAllocation_WithNodeId_IOException() throws IOException {
        String nodeId = "node1_error";
        Request expectedRequest = new Request("GET", "/_cat/allocation/" + nodeId + "?format=json");

        when(mockRestClient.performRequest(ArgumentMatchers.argThat(request ->
                request.getMethod().equals(expectedRequest.getMethod()) &&
                request.getEndpoint().equals(expectedRequest.getEndpoint())
        ))).thenThrow(new IOException("Simulated API error for node"));

        assertThrows(IOException.class, () -> provider.getCatAllocation(nodeId));
    }
}
