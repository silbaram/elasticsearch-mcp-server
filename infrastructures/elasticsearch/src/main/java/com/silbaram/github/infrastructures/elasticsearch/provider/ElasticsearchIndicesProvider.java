package com.silbaram.github.infrastructures.elasticsearch.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ElasticsearchIndicesProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private static final String CAT_INDICES_HEADERS = "health,status,index,docs.count,docs.deleted,pri.store.size,store.size";

    public ElasticsearchIndicesProvider(RestClient restClient) {
        this.restClient = restClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Elasticsearch: /_cat/indices API
     * 모든 인덱스 정보를 조회합니다.
     */
    public List<Map<String, Object>> getCatIndices() throws IOException {
        Request request = new Request("GET", "/_cat/indices?format=json&h=" + CAT_INDICES_HEADERS);
        Response response = restClient.performRequest(request);
        try (InputStream inputStream = response.getEntity().getContent()) {
            List<Map<String, Object>> rawIndices = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
            return rawIndices.stream()
                             .map(this::transformIndexData)
                             .collect(Collectors.toList());
        }
    }

    /**
     * Elasticsearch: /_cat/indices/{index} API
     * 지정된 인덱스 이름 또는 패턴과 일치하는 인덱스 정보를 조회합니다.
     */
    public List<Map<String, Object>> getCatIndicesByName(String indexName) throws IOException {
        Request request = new Request("GET", "/_cat/indices/" + indexName + "?format=json&h=" + CAT_INDICES_HEADERS);
        Response response = restClient.performRequest(request);
        try (InputStream inputStream = response.getEntity().getContent()) {
            List<Map<String, Object>> rawIndices = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
            return rawIndices.stream()
                             .map(this::transformIndexData)
                             .collect(Collectors.toList());
        }
    }

    /**
     * Elasticsearch _cat/indices API 응답의 원시 데이터를 클라이언트가 사용하기 쉬운 형태로 변환합니다.
     * cat API 요청의 `h` 매개변수는 특정 필드를 반환하는 것을 목표로 합니다.
     * 이 메서드는 해당 필드를 원하는 키에 매핑합니다.
     *
     * @param rawIndexData _cat/indices API로부터 받은 단일 인덱스에 대한 원시 Map 데이터
     * @return 변환된 인덱스 정보 Map
     */
    private Map<String, Object> transformIndexData(Map<String, Object> rawIndexData) {
        Map<String, Object> index = new HashMap<>();
        // 'h' 매개변수는 이러한 키가 존재하는지 확인해야 합니다.
        // 'h'에도 불구하고 짧은 이름(예: 'docs.count'의 'dc')이 반환되는 경우,
        // 이 매핑을 조정하거나 잠재적 키에 따라 우선 순위를 지정해야 할 수 있습니다.
        index.put("index", rawIndexData.get("index"));
        index.put("health", rawIndexData.get("health"));
        index.put("status", rawIndexData.get("status"));
        index.put("docsCount", rawIndexData.get("docs.count")); // or "dc"
        index.put("docsDeleted", rawIndexData.get("docs.deleted")); // or "dd"
        index.put("priStoreSize", rawIndexData.get("pri.store.size")); // or "pri.ss"
        index.put("storeSize", rawIndexData.get("store.size")); // or "ss"
        return index;
    }
}
