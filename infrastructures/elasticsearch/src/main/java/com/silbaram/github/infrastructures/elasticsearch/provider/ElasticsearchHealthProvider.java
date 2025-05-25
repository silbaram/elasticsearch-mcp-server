package com.silbaram.github.infrastructures.elasticsearch.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class ElasticsearchHealthProvider {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchHealthProvider.class);

    // RestClient를 사용하므로 HealthRequest는 더 이상 필요하지 않습니다.
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 응답 파싱을 위해 사용

    public ElasticsearchHealthProvider(RestClient restClient) {
        this.restClient = restClient;
    }

    public Map<String, String> getClusterHealth() throws IOException {
        Map<String, String> healthData = new HashMap<>();
        Request request = new Request("GET", "/_cluster/health");

        try {
            Response response = restClient.performRequest(request);
            // 응답 본문 읽기
            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }
            }
            logger.info("Elasticsearch Cluster Health Response: {}", responseBody.toString());

            // JSON 응답 파싱
            // objectMapper.readValue가 제네릭 Map을 반환하므로 타입 안정성을 위해 @SuppressWarnings 사용
            @SuppressWarnings("unchecked") 
            Map<String, Object> jsonResponse = objectMapper.readValue(responseBody.toString(), Map.class);

            // 파싱된 JSON으로부터 healthData 채우기
            // 참고: _cluster/health API는 epoch 및 timestamp를 _cat/health와 다른 방식으로 제공합니다.
            // 이전 구현의 플레이스홀더와 일관되게 "-"로 유지합니다.
            healthData.put("epoch", "-");
            healthData.put("timestamp", "-");

            healthData.put("cluster", Objects.toString(jsonResponse.get("cluster_name"), "-"));
            healthData.put("status", Objects.toString(jsonResponse.get("status"), "-"));
            healthData.put("node.total", Objects.toString(jsonResponse.get("number_of_nodes"), "-"));
            healthData.put("node.data", Objects.toString(jsonResponse.get("number_of_data_nodes"), "-"));
            healthData.put("shards", Objects.toString(jsonResponse.get("active_shards"), "-"));
            healthData.put("pri", Objects.toString(jsonResponse.get("active_primary_shards"), "-"));
            healthData.put("relo", Objects.toString(jsonResponse.get("relocating_shards"), "-"));
            healthData.put("init", Objects.toString(jsonResponse.get("initializing_shards"), "-"));
            healthData.put("unassign", Objects.toString(jsonResponse.get("unassigned_shards"), "-"));
            // "unassign.pri"는 원래 로직에 따라 "unassign"과 동일한 값을 사용합니다.
            healthData.put("unassign.pri", Objects.toString(jsonResponse.get("unassigned_shards"), "-"));
            healthData.put("pending_tasks", Objects.toString(jsonResponse.get("number_of_pending_tasks"), "-"));

            // task_max_waiting_in_queue_millis 필드 처리: ES 응답이 숫자이고 -1일 경우 "-"로 표시 (이전 로직과 일관성 유지)
            Object maxTaskWaitTimeMillisObj = jsonResponse.get("task_max_waiting_in_queue_millis");
            if (maxTaskWaitTimeMillisObj instanceof Number) {
                Number maxTaskWaitTimeMillisNum = (Number) maxTaskWaitTimeMillisObj;
                // 숫자일 경우 -1L (long 타입)과 비교하여 ES의 -1 값과 일치하는지 확인합니다.
                healthData.put("max_task_wait_time", maxTaskWaitTimeMillisNum.longValue() == -1L ? "-" : maxTaskWaitTimeMillisNum.toString());
            } else {
                healthData.put("max_task_wait_time", "-"); // null이거나 숫자가 아닌 경우 기본값 "-"
            }
            
            // active_shards_percent_as_number 필드 처리: 값 뒤에 "%" 추가
            Object activeShardsPercent = jsonResponse.get("active_shards_percent_as_number");
            healthData.put("active_shards_percent", activeShardsPercent == null ? "-" : Objects.toString(activeShardsPercent, "-") + "%");

        } catch (IOException e) {
            logger.error("Error fetching or parsing Elasticsearch cluster health: {}", e.getMessage());
            throw e; // 호출자가 처리하도록 예외를 다시 던집니다.
        }

        return healthData;
    }
}
