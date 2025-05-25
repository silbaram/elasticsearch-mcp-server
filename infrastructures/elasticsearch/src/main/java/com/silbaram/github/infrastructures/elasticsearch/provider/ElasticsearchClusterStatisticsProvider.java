package com.silbaram.github.infrastructures.elasticsearch.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class ElasticsearchClusterStatisticsProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ElasticsearchClusterStatisticsProvider(RestClient restClient) {
        this.restClient = restClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Elasticsearch: /_cluster/stats API
     * 클러스터 통계 정보를 조회합니다.
     * @return 클러스터 통계 정보를 담은 Map
     * @throws IOException API 호출 실패 시
     */
    @SuppressWarnings("unchecked") // JSON 파싱 시 Map 캐스팅에 대한 경고를 무시합니다.
    public Map<String, Object> getClusterStatistics() throws IOException {
        Request request = new Request("GET", "/_cluster/stats");
        Response response = restClient.performRequest(request);
        Map<String, Object> rootJsonMap;
        try (InputStream inputStream = response.getEntity().getContent()) {
            rootJsonMap = objectMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
        }

        Map<String, Object> result = new HashMap<>();

        // 클러스터 정보 (Cluster Info)
        Map<String, Object> clusterInfo = new HashMap<>();
        clusterInfo.put("name", rootJsonMap.get("cluster_name"));
        clusterInfo.put("uuid", rootJsonMap.get("cluster_uuid"));
        clusterInfo.put("status", rootJsonMap.get("status")); // JSON 응답의 status 필드 (예: "green", "yellow", "red")
        clusterInfo.put("timestamp", rootJsonMap.get("timestamp"));
        result.put("cluster", clusterInfo);

        // 노드 정보 (Nodes Info)
        Map<String, Object> nodesData = (Map<String, Object>) rootJsonMap.get("nodes");
        Map<String, Object> nodesInfo = new HashMap<>();
        if (nodesData != null) {
            Map<String, Object> countData = (Map<String, Object>) nodesData.get("count");
            if (countData != null) {
                nodesInfo.put("total", countData.get("total"));
                nodesInfo.put("data", countData.get("data"));
                nodesInfo.put("master", countData.get("master"));
                nodesInfo.put("ingest", countData.get("ingest"));
            }

            Map<String, Object> osData = (Map<String, Object>) nodesData.get("os");
            if (osData != null) {
                Map<String, Object> memData = (Map<String, Object>) osData.get("mem");
                if (memData != null) {
                    nodesInfo.put("mem_used_percent", memData.get("used_percent"));
                }
                nodesInfo.put("processors", osData.get("available_processors"));
            }

            Map<String, Object> jvmData = (Map<String, Object>) nodesData.get("jvm");
            if (jvmData != null) {
                Map<String, Object> jvmMemData = (Map<String, Object>) jvmData.get("mem");
                if (jvmMemData != null) {
                    // JVM 힙 사용량 계산 (Calculating JVM heap usage)
                    Object heapUsedBytesObj = jvmMemData.get("heap_used_in_bytes");
                    Object heapMaxBytesObj = jvmMemData.get("heap_max_in_bytes");

                    if (heapUsedBytesObj instanceof Number && heapMaxBytesObj instanceof Number) {
                        long heapUsedBytes = ((Number) heapUsedBytesObj).longValue();
                        long heapMaxBytes = ((Number) heapMaxBytesObj).longValue();
                        double heapUsedPercent = heapMaxBytes > 0 ? (double) heapUsedBytes / heapMaxBytes * 100 : 0.0;
                        nodesInfo.put("heap_used_bytes", heapUsedBytes);
                        nodesInfo.put("heap_max_bytes", heapMaxBytes);
                        nodesInfo.put("heap_used_percent", heapUsedPercent);
                    } else {
                        nodesInfo.put("heap_used_bytes", 0L);
                        nodesInfo.put("heap_max_bytes", 0L);
                        nodesInfo.put("heap_used_percent", 0.0);
                    }
                }
            }
        }
        result.put("nodes", nodesInfo);

        // 인덱스 정보 (Indices Info)
        Map<String, Object> indicesData = (Map<String, Object>) rootJsonMap.get("indices");
        Map<String, Object> indicesInfo = new HashMap<>();
        if (indicesData != null) {
            indicesInfo.put("count", indicesData.get("count"));

            Map<String, Object> shardsData = (Map<String, Object>) indicesData.get("shards");
            Map<String, Object> shardsInfo = new HashMap<>();
            if (shardsData != null) {
                shardsInfo.put("total", shardsData.get("total"));
                shardsInfo.put("primaries", shardsData.get("primaries"));
                // 'replication' 필드는 _cluster/stats API 응답에 직접적으로 없을 수 있습니다.
                // 이전 클라이언트는 이를 계산했을 수 있으나, 여기서는 응답에 있다면 사용하고 없다면 기본값을 설정합니다.
                shardsInfo.put("replication", shardsData.getOrDefault("replication", 0.0f)); // Assuming float or default
            }
            indicesInfo.put("shards", shardsInfo);

            Map<String, Object> docsData = (Map<String, Object>) indicesData.get("docs");
            Map<String, Object> docsInfo = new HashMap<>();
            if (docsData != null) {
                docsInfo.put("count", docsData.get("count"));
                docsInfo.put("deleted", docsData.get("deleted"));
            }
            indicesInfo.put("docs", docsInfo);
        }
        result.put("indices", indicesInfo);

        return result;
    }
}
