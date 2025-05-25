package com.silbaram.github.infrastructures.elasticsearch.provider;

import org.apache.http.HttpEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Mockito 확장 기능을 사용하여 Mockito 어노테이션 활성화
@ExtendWith(MockitoExtension.class)
class ElasticsearchHealthProviderTest {

    // 테스트 대상 클래스에 주입할 Mock 객체들
    @Mock
    private RestClient restClient; // Elasticsearch RestClient Mock

    @Mock
    private Response mockResponse; // RestClient가 반환할 Response Mock

    @Mock
    private HttpEntity mockHttpEntity; // Response가 포함할 HttpEntity Mock

    // Mock 객체들이 주입될 테스트 대상 클래스의 인스턴스
    @InjectMocks
    private ElasticsearchHealthProvider elasticsearchHealthProvider;

    @Test
    @DisplayName("클러스터 건강 정보 조회 성공 테스트 - 모든 필드 포함")
    void getClusterHealth_Success_AllFieldsPresent() throws IOException {
        // given: 테스트 데이터 준비
        String jsonResponseString = "{\n" +
                "  \"cluster_name\": \"elasticsearch_silbaram\",\n" +
                "  \"status\": \"green\",\n" +
                "  \"timed_out\": false,\n" +
                "  \"number_of_nodes\": 3,\n" +
                "  \"number_of_data_nodes\": 3,\n" +
                "  \"active_primary_shards\": 5,\n" +
                "  \"active_shards\": 10,\n" +
                "  \"relocating_shards\": 0,\n" +
                "  \"initializing_shards\": 0,\n" +
                "  \"unassigned_shards\": 0,\n" +
                "  \"delayed_unassigned_shards\": 0,\n" +
                "  \"number_of_pending_tasks\": 0,\n" +
                "  \"number_of_in_flight_fetch\": 0,\n" +
                "  \"task_max_waiting_in_queue_millis\": 1234,\n" +
                "  \"active_shards_percent_as_number\": 100.0\n" +
                "}";
        InputStream inputStream = new ByteArrayInputStream(jsonResponseString.getBytes(StandardCharsets.UTF_8));

        // Mock 설정: RestClient.performRequest 호출 시 mockResponse 반환
        when(restClient.performRequest(any(Request.class))).thenReturn(mockResponse);
        // Mock 설정: mockResponse.getEntity 호출 시 mockHttpEntity 반환
        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        // Mock 설정: mockHttpEntity.getContent 호출 시 준비된 JSON InputStream 반환
        when(mockHttpEntity.getContent()).thenReturn(inputStream);

        // when: 테스트 대상 메소드 호출
        Map<String, String> healthData = elasticsearchHealthProvider.getClusterHealth();

        // then: 결과 검증
        assertNotNull(healthData, "결과 맵은 null이 아니어야 합니다.");
        assertEquals("elasticsearch_silbaram", healthData.get("cluster"), "클러스터 이름 검증");
        assertEquals("green", healthData.get("status"), "상태 검증");
        assertEquals("3", healthData.get("node.total"), "총 노드 수 검증");
        assertEquals("3", healthData.get("node.data"), "데이터 노드 수 검증");
        assertEquals("10", healthData.get("shards"), "활성 샤드 수 검증");
        assertEquals("5", healthData.get("pri"), "활성 기본 샤드 수 검증");
        assertEquals("0", healthData.get("relo"), "재배치 중인 샤드 수 검증");
        assertEquals("0", healthData.get("init"), "초기화 중인 샤드 수 검증");
        assertEquals("0", healthData.get("unassign"), "할당되지 않은 샤드 수 검증");
        assertEquals("0", healthData.get("unassign.pri"), "할당되지 않은 기본 샤드 수 검증 (unassign과 동일 값)");
        assertEquals("0", healthData.get("pending_tasks"), "대기 중인 작업 수 검증");
        assertEquals("1234", healthData.get("max_task_wait_time"), "최대 작업 대기 시간 검증");
        assertEquals("100.0%", healthData.get("active_shards_percent"), "활성 샤드 백분율 검증");

        // epoch 및 timestamp는 "-"로 설정되어야 함 (현재 구현 기준)
        assertEquals("-", healthData.get("epoch"), "Epoch 시간 검증");
        assertEquals("-", healthData.get("timestamp"), "타임스탬프 검증");
    }

    @Test
    @DisplayName("클러스터 건강 정보 조회 성공 - 일부 필드 누락 및 특별 값 케이스")
    void getClusterHealth_Success_MissingFieldsAndSpecialValues() throws IOException {
        // given: 일부 필드가 누락되고, task_max_waiting_in_queue_millis가 -1인 JSON 응답
        String jsonResponseString = "{\n" +
                "  \"cluster_name\": \"elasticsearch_dev\",\n" +
                "  \"status\": \"yellow\",\n" +
                "  \"number_of_nodes\": 1,\n" +
                // "number_of_data_nodes" 필드 누락
                "  \"active_primary_shards\": 2,\n" +
                "  \"active_shards\": 4,\n" +
                "  \"relocating_shards\": 0,\n" +
                "  \"initializing_shards\": 1,\n" +
                "  \"unassigned_shards\": 1,\n" +
                "  \"task_max_waiting_in_queue_millis\": -1,\n" + // 특별 값: -1
                "  \"active_shards_percent_as_number\": 50.5\n" +
                // "pending_tasks" 필드 누락
                "}";
        InputStream inputStream = new ByteArrayInputStream(jsonResponseString.getBytes(StandardCharsets.UTF_8));

        when(restClient.performRequest(any(Request.class))).thenReturn(mockResponse);
        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        when(mockHttpEntity.getContent()).thenReturn(inputStream);

        // when
        Map<String, String> healthData = elasticsearchHealthProvider.getClusterHealth();

        // then
        assertNotNull(healthData);
        assertEquals("elasticsearch_dev", healthData.get("cluster"));
        assertEquals("yellow", healthData.get("status"));
        assertEquals("1", healthData.get("node.total"));
        assertEquals("-", healthData.get("node.data"), "누락된 node.data는 '-'여야 합니다.");
        assertEquals("4", healthData.get("shards"));
        assertEquals("2", healthData.get("pri"));
        assertEquals("0", healthData.get("relo"));
        assertEquals("1", healthData.get("init"));
        assertEquals("1", healthData.get("unassign"));
        assertEquals("1", healthData.get("unassign.pri"));
        assertEquals("-", healthData.get("pending_tasks"), "누락된 pending_tasks는 '-'여야 합니다.");
        assertEquals("-", healthData.get("max_task_wait_time"), "task_max_waiting_in_queue_millis가 -1이면 '-'여야 합니다.");
        assertEquals("50.5%", healthData.get("active_shards_percent"));
    }
    
    @Test
    @DisplayName("클러스터 건강 정보 조회 성공 - active_shards_percent_as_number가 null인 경우")
    void getClusterHealth_Success_NullActiveShardsPercent() throws IOException {
        // given: active_shards_percent_as_number가 null인 JSON 응답
        String jsonResponseString = "{\n" +
                "  \"cluster_name\": \"elasticsearch_test\",\n" +
                "  \"status\": \"green\",\n" +
                "  \"number_of_nodes\": 1,\n" +
                "  \"number_of_data_nodes\": 1,\n" +
                "  \"active_primary_shards\": 1,\n" +
                "  \"active_shards\": 1,\n" +
                "  \"relocating_shards\": 0,\n" +
                "  \"initializing_shards\": 0,\n" +
                "  \"unassigned_shards\": 0,\n" +
                "  \"task_max_waiting_in_queue_millis\": 0,\n" +
                "  \"active_shards_percent_as_number\": null\n" + // active_shards_percent_as_number가 null
                "}";
        InputStream inputStream = new ByteArrayInputStream(jsonResponseString.getBytes(StandardCharsets.UTF_8));

        when(restClient.performRequest(any(Request.class))).thenReturn(mockResponse);
        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        when(mockHttpEntity.getContent()).thenReturn(inputStream);

        // when
        Map<String, String> healthData = elasticsearchHealthProvider.getClusterHealth();

        // then
        assertNotNull(healthData);
        assertEquals("elasticsearch_test", healthData.get("cluster"));
        assertEquals("green", healthData.get("status"));
        assertEquals("-", healthData.get("active_shards_percent"), "active_shards_percent_as_number가 null이면 '-'여야 합니다.");
    }


    @Test
    @DisplayName("클러스터 건강 정보 조회 시 IOException 발생 테스트")
    void getClusterHealth_IOException() throws IOException {
        // given: RestClient.performRequest 호출 시 IOException 발생하도록 Mock 설정
        String errorMessage = "Simulated IO Error communicating with Elasticsearch";
        when(restClient.performRequest(any(Request.class))).thenThrow(new IOException(errorMessage));

        // when & then: getClusterHealth 호출 시 IOException 발생하는지 검증
        IOException exception = assertThrows(IOException.class, () -> {
            elasticsearchHealthProvider.getClusterHealth();
        }, "IOException이 발생해야 합니다.");

        // 예외 메시지 검증 (선택 사항이지만 좋은 습관)
        assertEquals(errorMessage, exception.getMessage(), "예외 메시지가 예상과 동일해야 합니다.");
    }
}
