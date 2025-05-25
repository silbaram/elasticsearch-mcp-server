package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.ClusterClient; // 수정된 임포트
import co.elastic.clients.elasticsearch.cluster.HealthRequest;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
// import co.elastic.clients.elasticsearch.core.CatOperations; // _cat 작업에 직접 필요한 경우 사용
import co.elastic.clients.json.JsonData;
// import co.elastic.clients.transport.ElasticsearchTransport; // 필요시 사용
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ElasticsearchHealthProviderTest {

    private ElasticsearchClient mockClient;
    private ClusterClient mockClusterOps; // 타입 변경됨
    private HealthResponse mockHealthResponse;
    private ElasticsearchHealthProvider healthProvider;
    private co.elastic.clients.elasticsearch.cluster.ElasticsearchStatus mockStatus; // 이름 충돌을 피하기 위해 변경

    @BeforeEach
    void setUp() {
        // 핵심 ElasticsearchClient 모의 객체 생성
        mockClient = Mockito.mock(ElasticsearchClient.class);

        // ClusterClient 모의 객체 생성 (이전 ClusterOperations)
        mockClusterOps = Mockito.mock(ClusterClient.class); // 클래스명 변경됨

        // HealthResponse 모의 객체 생성
        mockHealthResponse = Mockito.mock(HealthResponse.class);
        
        // ElasticsearchStatus 모의 객체 생성 (HealthResponse.status()가 반환하는 타입)
        // 참고: 실제 클래스는 co.elastic.clients.elasticsearch.cluster.ElasticsearchStatus 입니다.
        mockStatus = Mockito.mock(co.elastic.clients.elasticsearch.cluster.ElasticsearchStatus.class);

        // 모의 객체에 대한 기본 동작 정의
        when(mockClient.cluster()).thenReturn(mockClusterOps); // mockClient.cluster()는 ClusterClient를 반환하므로 호환됨
        try {
            // mockClusterOps는 이제 ClusterClient 타입이며, health 메소드를 가지고 있음
            when(mockClusterOps.health(any(HealthRequest.class))).thenReturn(mockHealthResponse);
        } catch (IOException e) {
            // 모의 객체를 사용하는 테스트에서는 발생하지 않아야 하지만, 시그니처 때문에 try-catch 필요
            throw new RuntimeException(e);
        }
        when(mockHealthResponse.status()).thenReturn(mockStatus);

        // 테스트 대상 클래스 인스턴스화
        healthProvider = new ElasticsearchHealthProvider(mockClient);
    }

    @Test
    void getClusterHealth_shouldReturnComprehensiveHealthData() throws IOException {
        // 준비(Arrange): HealthResponse가 반환할 데이터 정의
        when(mockHealthResponse.clusterName()).thenReturn("test-cluster");
        when(mockStatus.jsonValue()).thenReturn("green"); // status().jsonValue()에 대한 모의 설정
        when(mockHealthResponse.numberOfNodes()).thenReturn(JsonData.of(3L));
        when(mockHealthResponse.numberOfDataNodes()).thenReturn(JsonData.of(2L));
        when(mockHealthResponse.activeShards()).thenReturn(JsonData.of(10L));
        when(mockHealthResponse.activePrimaryShards()).thenReturn(JsonData.of(5L));
        when(mockHealthResponse.relocatingShards()).thenReturn(JsonData.of(1L));
        when(mockHealthResponse.initializingShards()).thenReturn(JsonData.of(0L));
        when(mockHealthResponse.unassignedShards()).thenReturn(JsonData.of(0L)); // unassign 및 unassign.pri 모두에 해당
        when(mockHealthResponse.numberOfPendingTasks()).thenReturn(JsonData.of(2L));
        when(mockHealthResponse.taskMaxWaitingInQueueMillis()).thenReturn(120000L); // 밀리초 단위로 2분
        when(mockHealthResponse.activeShardsPercentAsNumber()).thenReturn(100.0);

        // 실행(Act): 테스트 대상 메소드 호출
        Map<String, String> healthData = healthProvider.getClusterHealth();

        // 단언(Assert): 모든 필드가 존재하고 올바른지 확인
        assertNotNull(healthData);
        assertEquals("-", healthData.get("epoch")); // 플레이스홀더
        assertEquals("-", healthData.get("timestamp")); // 플레이스홀더
        assertEquals("test-cluster", healthData.get("cluster"));
        assertEquals("green", healthData.get("status"));
        assertEquals("3", healthData.get("node.total"));
        assertEquals("2", healthData.get("node.data"));
        assertEquals("10", healthData.get("shards"));
        assertEquals("5", healthData.get("pri"));
        assertEquals("1", healthData.get("relo"));
        assertEquals("0", healthData.get("init"));
        assertEquals("0", healthData.get("unassign"));
        assertEquals("0", healthData.get("unassign.pri")); // 현재 unassignedShards()를 사용하는 구현 기반
        assertEquals("2", healthData.get("pending_tasks"));
        assertEquals("120000", healthData.get("max_task_wait_time"));
        assertEquals("100.0%", healthData.get("active_shards_percent"));
    }

    @Test
    void getClusterHealth_shouldHandleNullValuesAndDefaultPlaceholders() throws IOException {
        // 준비(Arrange): HealthResponse가 해당되는 경우 null 또는 기본값을 반환하도록 모의 설정
        when(mockHealthResponse.clusterName()).thenReturn(null); // nullable 필드의 예
        when(mockStatus.jsonValue()).thenReturn("yellow"); // 다른 상태
        when(mockHealthResponse.numberOfNodes()).thenReturn(null);
        when(mockHealthResponse.numberOfDataNodes()).thenReturn(null);
        when(mockHealthResponse.activeShards()).thenReturn(null);
        when(mockHealthResponse.activePrimaryShards()).thenReturn(null);
        when(mockHealthResponse.relocatingShards()).thenReturn(null);
        when(mockHealthResponse.initializingShards()).thenReturn(null);
        when(mockHealthResponse.unassignedShards()).thenReturn(null);
        when(mockHealthResponse.numberOfPendingTasks()).thenReturn(null);
        when(mockHealthResponse.taskMaxWaitingInQueueMillis()).thenReturn(-1L); // max_task_wait_time의 특수 경우
        when(mockHealthResponse.activeShardsPercentAsNumber()).thenReturn(null);

        // 실행(Act)
        Map<String, String> healthData = healthProvider.getClusterHealth();

        // 단언(Assert)
        assertNotNull(healthData);
        assertEquals("-", healthData.get("epoch"));
        assertEquals("-", healthData.get("timestamp"));
        assertEquals("-", healthData.get("cluster")); // Null 값은 "-"로 대체되어야 함
        assertEquals("yellow", healthData.get("status"));
        assertEquals("-", healthData.get("node.total"));
        assertEquals("-", healthData.get("node.data"));
        assertEquals("-", healthData.get("shards"));
        assertEquals("-", healthData.get("pri"));
        assertEquals("-", healthData.get("relo"));
        assertEquals("-", healthData.get("init"));
        assertEquals("-", healthData.get("unassign"));
        assertEquals("-", healthData.get("unassign.pri"));
        assertEquals("-", healthData.get("pending_tasks"));
        assertEquals("-", healthData.get("max_task_wait_time")); // 밀리초 -1L은 "-"가 되어야 함
        assertEquals("-"+"%", healthData.get("active_shards_percent")); // null 퍼센트는 "-%"가 됨
    }
}
