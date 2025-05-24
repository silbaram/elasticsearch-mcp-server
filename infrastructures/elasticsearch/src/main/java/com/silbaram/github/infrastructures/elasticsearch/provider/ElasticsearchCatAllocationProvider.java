package com.silbaram.github.infrastructures.elasticsearch.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Provides methods to retrieve allocation information from Elasticsearch.
 */
@Component
public class ElasticsearchCatAllocationProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs an ElasticsearchCatAllocationProvider with the given RestClient.
     *
     * @param restClient The Elasticsearch RestClient.
     */
    public ElasticsearchCatAllocationProvider(RestClient restClient) {
        this.restClient = restClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Retrieves the cat allocation information from Elasticsearch.
     *
     * @return A list of maps, where each map represents a node's allocation information.
     * @throws IOException If the API call fails.
     */
    public List<Map<String, Object>> getCatAllocation() throws IOException {
        Request request = new Request("GET", "/_cat/allocation?format=json");
        Response response = restClient.performRequest(request);
        try (InputStream inputStream = response.getEntity().getContent()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
        }
    }

    /**
     * Retrieves the cat allocation information for a specific node from Elasticsearch.
     *
     * @param nodeId The ID of the node.
     * @return A list of maps, where each map represents the node's allocation information.
     * @throws IOException If the API call fails or if the nodeId is invalid.
     */
    public List<Map<String, Object>> getCatAllocation(String nodeId) throws IOException {
        Request request = new Request("GET", "/_cat/allocation/" + nodeId + "?format=json");
        Response response = restClient.performRequest(request);
        try (InputStream inputStream = response.getEntity().getContent()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
        }
    }
}
