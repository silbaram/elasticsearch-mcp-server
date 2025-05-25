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
     * Elasticsearch의 cat allocation 정보를 조회합니다.
     *
     * @return 각 노드의 할당 정보를 나타내는 Map의 List입니다.
     * @throws IOException API 호출에 실패한 경우 발생합니다.
     */
    public List<Map<String, Object>> getCatAllocation() throws IOException {
        Request request = new Request("GET", "/_cat/allocation?format=json");
        Response response = restClient.performRequest(request);
        try (InputStream inputStream = response.getEntity().getContent()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
        }
    }

    /**
     * Elasticsearch에서 특정 노드의 cat allocation 정보를 조회합니다.
     *
     * @param nodeId 노드 ID입니다.
     * @return 해당 노드의 할당 정보를 나타내는 Map의 List입니다.
     * @throws IOException API 호출에 실패하거나 nodeId가 유효하지 않은 경우 발생합니다.
     */
    public List<Map<String, Object>> getCatAllocation(String nodeId) throws IOException {
        Request request = new Request("GET", "/_cat/allocation/" + nodeId + "?format=json");
        Response response = restClient.performRequest(request);
        try (InputStream inputStream = response.getEntity().getContent()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
        }
    }
}
