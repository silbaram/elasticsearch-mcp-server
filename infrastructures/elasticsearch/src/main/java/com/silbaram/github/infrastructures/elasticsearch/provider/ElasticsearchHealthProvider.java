package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthRequest;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class ElasticsearchHealthProvider {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchHealthProvider.class);

    // HealthRequest는 특정 매개변수가 필요하지 않은 경우 static final 필드로 유지할 수 있습니다.
    final static HealthRequest healthRequest = new HealthRequest.Builder().build();
    private final ElasticsearchClient elasticsearchMcpClient;

    public ElasticsearchHealthProvider(ElasticsearchClient elasticsearchMcpClient) {
        this.elasticsearchMcpClient = elasticsearchMcpClient;
    }

    public Map<String, String> getClusterHealth() throws IOException {
        // 클러스터 상태 정보 가져오기
        HealthResponse response = elasticsearchMcpClient.cluster().health(healthRequest);
        Map<String, String> healthData = new HashMap<>();

        // --- /_cat/health 문서의 필드들 ---

        // epoch 및 timestamp는 _cat/health와 동일한 형식으로 HealthResponse에서 직접 사용할 수 없습니다.
        // _cat/health는 이를 제공하지만 HealthResponse는 구조화된 데이터에 중점을 둡니다.
        // 플레이스홀더나 기본값을 사용합니다.
        healthData.put("epoch", "-"); // 플레이스홀더, HealthResponse가 epoch 초를 직접 제공하지 않음.
        healthData.put("timestamp", "-"); // 플레이스홀더, HealthResponse가 HH:MM:SS 타임스탬프를 직접 제공하지 않음.

        healthData.put("cluster", Objects.toString(response.clusterName(), "-"));
        healthData.put("status", Objects.toString(response.status().jsonValue(), "-"));

        // 노드 정보
        // _cat/health의 node.total은 HealthResponse의 numberOfNodes에 해당합니다.
        healthData.put("node.total", Objects.toString(response.numberOfNodes(), "-"));
        // _cat/health의 node.data는 HealthResponse의 numberOfDataNodes에 해당합니다.
        healthData.put("node.data", Objects.toString(response.numberOfDataNodes(), "-"));

        // 샤드 정보
        // _cat/health의 shards (총 샤드 수)는 activeShards + initializingShards + unassignedShards + relocatingShards에 해당합니다.
        // 그러나 HealthResponse는 이러한 구성 요소를 별도로 제공합니다.
        // activeShards()는 활성 샤드를 고려할 때 "shards"에 가장 직접적으로 해당하는 것으로 보입니다.
        // 또는 이를 합산하거나 activeShards를 기본 표현으로 사용합니다.
        // HealthResponse로부터의 단순성과 직접적인 매핑을 위해 "shards"에는 activeShards 수를 사용합니다.
        // _cat/health 출력 예제에는 단일 샤드 클러스터에 대해 "shards": "1", "pri": "1"이 있습니다.
        // "shards"에는 activeShards를, "pri"에는 activePrimaryShards를 사용합니다.
        healthData.put("shards", Objects.toString(response.activeShards(), "-"));
        healthData.put("pri", Objects.toString(response.activePrimaryShards(), "-"));

        healthData.put("relo", Objects.toString(response.relocatingShards(), "-"));
        healthData.put("init", Objects.toString(response.initializingShards(), "-"));

        // _cat/health의 unassign 필드는 총 할당되지 않은 샤드 수입니다.
        healthData.put("unassign", Objects.toString(response.unassignedShards(), "-"));
        // unassign.pri는 직접 사용할 수 없습니다. HealthResponse.unassignedShards()는 총계입니다.
        // "unassign"에는 unassignedShards를 사용하고, "unassign.pri"에는 주석과 함께 플레이스홀더나 동일한 값을 넣습니다.
        // 현재로서는 최선의 노력으로 unassign.pri에 대해 총 할당되지 않은 샤드를 사용합니다.
        // 기본 샤드별 할당되지 않은 수가 필요한 경우 더 정확한 접근 방식에는 다른 API 호출이나 복잡한 계산이 필요합니다.
        healthData.put("unassign.pri", Objects.toString(response.unassignedShards(), "-")); // 이것은 기본 샤드뿐만 아니라 총 할당되지 않은 샤드 수입니다.

        healthData.put("pending_tasks", Objects.toString(response.numberOfPendingTasks(), "-"));

        // max_task_wait_time: HealthResponse에는 taskMaxWaitingInQueueMillis가 있습니다.
        // _cat/health API는 이를 시간 문자열(예: "1.2m", "-")로 표시합니다.
        // 밀리초 값을 문자열로 저장합니다.
        healthData.put("max_task_wait_time", response.taskMaxWaitingInQueueMillis() == -1 ? "-" : Objects.toString(response.taskMaxWaitingInQueueMillis(), "-"));
        
        healthData.put("active_shards_percent", Objects.toString(response.activeShardsPercentAsNumber(), "-") + "%");

        return healthData;
    }
}
