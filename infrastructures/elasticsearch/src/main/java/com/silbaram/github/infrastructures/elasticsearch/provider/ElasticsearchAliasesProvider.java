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

@Component
public class ElasticsearchAliasesProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ElasticsearchAliasesProvider(RestClient restClient) {
        this.restClient = restClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Elasticsearch: /_cat/aliases API
     * 모든 별칭 정보를 조회하며, '.'으로 시작하는 시스템 별칭은 제외합니다.
     */
    public List<Map<String, Object>> getCatAliases() throws IOException {
        Request request = new Request("GET", "/_cat/aliases?format=json");
        Response response = restClient.performRequest(request);
        try (InputStream inputStream = response.getEntity().getContent()) {
            List<Map<String, Object>> rawAliases = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
            return processAliases(rawAliases);
        }
    }

    /**
     * Elasticsearch: /_cat/aliases/{name} API
     * 지정된 이름 또는 패턴과 일치하는 별칭 정보를 조회하며, 시스템 별칭은 제외합니다.
     */
    public List<Map<String, Object>> getCatAliasesByName(String aliasName) throws IOException {
        Request request = new Request("GET", "/_cat/aliases/" + aliasName + "?format=json");
        Response response = restClient.performRequest(request);
        try (InputStream inputStream = response.getEntity().getContent()) {
            List<Map<String, Object>> rawAliases = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
            return processAliases(rawAliases);
        }
    }

    /**
     * Elasticsearch로부터 받은 원시 별칭 맵 목록을 처리합니다.
     * 필드를 원하는 이름으로 매핑하고 시스템 별칭을 필터링합니다.
     * @param rawAliases Elasticsearch의 별칭 데이터 목록입니다.
     * @return 처리된 별칭 목록입니다.
     */
    private List<Map<String, Object>> processAliases(List<Map<String, Object>> rawAliases) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> rawAliasRecord : rawAliases) {
            String alias = (String) rawAliasRecord.get("alias");
            // 점(.)으로 시작하면 시스템/숨김 별칭이므로 건너뛴다 (Skip system/hidden aliases starting with a dot)
            if (alias != null && alias.startsWith(".")) {
                continue;
            }

            Map<String, Object> aliasMap = new HashMap<>();
            aliasMap.put("alias", rawAliasRecord.get("alias"));
            aliasMap.put("index", rawAliasRecord.get("index"));
            aliasMap.put("filter", rawAliasRecord.get("filter"));
            aliasMap.put("routingIndex", rawAliasRecord.get("routing.index")); // JSON field is "routing.index"
            aliasMap.put("routingSearch", rawAliasRecord.get("routing.search")); // JSON field is "routing.search"
            aliasMap.put("isWriteIndex", rawAliasRecord.get("is_write_index")); // JSON field is "is_write_index"
            result.add(aliasMap);
        }
        return result;
    }
}
