package com.silbaram.github.infrastructures.elasticsearch.provider;

import org.apache.http.HttpEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Mockito 확장 기능 사용
class ElasticsearchClusterStatisticsProviderTest {

    @Mock
    private RestClient restClient; // RestClient Mock 객체

    @Mock
    private Response mockResponse; // Response Mock 객체

    @Mock
    private HttpEntity mockHttpEntity; // HttpEntity Mock 객체

    @InjectMocks
    private ElasticsearchClusterStatisticsProvider statisticsProvider; // 테스트 대상 클래스

    @Test
    @DisplayName("getClusterStatistics_성공_통계정보반환")
    @SuppressWarnings("unchecked") // 테스트 중 Map 캐스팅 경고 무시
    void testGetClusterStatistics_Success_ReturnsStatistics() throws IOException {
        // given: 테스트용 샘플 JSON 응답 문자열 준비
        String sampleJsonResponse = "{\n" +
                "  \"cluster_name\": \"test_cluster\",\n" +
                "  \"cluster_uuid\": \"test_uuid\",\n" +
                "  \"status\": \"green\",\n" +
                "  \"timestamp\": 1678886400000,\n" +
                "  \"nodes\": {\n" +
                "    \"count\": {\n" +
                "      \"total\": 3,\n" +
                "      \"data\": 2,\n" +
                "      \"master\": 1,\n" +
                "      \"ingest\": 2\n" +
                "    },\n" +
                "    \"os\": {\n" +
                "      \"mem\": {\n" +
                "        \"used_percent\": 60\n" +
                "      },\n" +
                "      \"available_processors\": 8\n" +
                "    },\n" +
                "    \"jvm\": {\n" +
                "      \"mem\": {\n" +
                "        \"heap_used_in_bytes\": 1073741824,\n" + // 1GB
                "        \"heap_max_in_bytes\": 2147483648\n" +  // 2GB
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"indices\": {\n" +
                "    \"count\": 5,\n" +
                "    \"shards\": {\n" +
                "      \"total\": 10,\n" +
                "      \"primaries\": 5,\n" +
                "      \"replication\": 1.0\n" + // JSON에서 숫자로 제공될 수 있음
                "    },\n" +
                "    \"docs\": {\n" +
                "      \"count\": 10000,\n" +
                "      \"deleted\": 500\n" +
                "    }\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(sampleJsonResponse.getBytes(StandardCharsets.UTF_8));
        when(mockHttpEntity.getContent()).thenReturn(inputStream);
        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        when(restClient.performRequest(any(Request.class))).thenReturn(mockResponse);

        // when: 테스트 대상 메소드 호출
        Map<String, Object> result = statisticsProvider.getClusterStatistics();

        // then: 결과 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(restClient).performRequest(requestCaptor.capture());
        assertEquals("GET", requestCaptor.getValue().getMethod());
        assertEquals("/_cluster/stats", requestCaptor.getValue().getEndpoint());

        assertNotNull(result, "결과 맵은 null이 아니어야 합니다.");

        // 클러스터 정보 검증
        Map<String, Object> clusterInfo = (Map<String, Object>) result.get("cluster");
        assertNotNull(clusterInfo);
        assertEquals("test_cluster", clusterInfo.get("name"));
        assertEquals("test_uuid", clusterInfo.get("uuid"));
        assertEquals("green", clusterInfo.get("status"));
        assertEquals(1678886400000L, clusterInfo.get("timestamp"));

        // 노드 정보 검증
        Map<String, Object> nodesInfo = (Map<String, Object>) result.get("nodes");
        assertNotNull(nodesInfo);
        assertEquals(3, nodesInfo.get("total"));
        assertEquals(2, nodesInfo.get("data"));
        assertEquals(1, nodesInfo.get("master"));
        assertEquals(2, nodesInfo.get("ingest"));
        assertEquals(60, nodesInfo.get("mem_used_percent"));
        assertEquals(8, nodesInfo.get("processors"));
        assertEquals(1073741824L, nodesInfo.get("heap_used_bytes"));
        assertEquals(2147483648L, nodesInfo.get("heap_max_bytes"));
        assertEquals(50.0, (Double) nodesInfo.get("heap_used_percent"), 0.001, "힙 사용률 계산 검증");

        // 인덱스 정보 검증
        Map<String, Object> indicesInfo = (Map<String, Object>) result.get("indices");
        assertNotNull(indicesInfo);
        assertEquals(5, indicesInfo.get("count"));

        Map<String, Object> shardsInfo = (Map<String, Object>) indicesInfo.get("shards");
        assertNotNull(shardsInfo);
        assertEquals(10, shardsInfo.get("total"));
        assertEquals(5, shardsInfo.get("primaries"));
        // API 응답에서 replication이 float (1.0f)으로 설정되어 있으므로 이에 맞게 검증
        assertEquals(1.0f, ((Number) shardsInfo.get("replication")).floatValue(), 0.001f, "샤드 복제본 수 검증");


        Map<String, Object> docsInfo = (Map<String, Object>) indicesInfo.get("docs");
        assertNotNull(docsInfo);
        assertEquals(10000, docsInfo.get("count"));
        assertEquals(500, docsInfo.get("deleted"));
    }

    @Test
    @DisplayName("getClusterStatistics_IOException발생")
    void testGetClusterStatistics_ThrowsIOException() throws IOException {
        // given: RestClient.performRequest 호출 시 IOException 발생하도록 Mock 설정
        when(restClient.performRequest(any(Request.class))).thenThrow(new IOException("Simulated Network Error"));

        // when & then: getClusterStatistics 호출 시 IOException 발생하는지 검증
        IOException exception = assertThrows(IOException.class, () -> {
            statisticsProvider.getClusterStatistics();
        }, "IOException이 발생해야 합니다.");

        assertEquals("Simulated Network Error", exception.getMessage(), "예외 메시지가 예상과 동일해야 합니다.");
    }
}
