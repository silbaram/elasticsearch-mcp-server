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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Mockito 확장 기능 사용
class ElasticsearchIndicesProviderTest {

    @Mock
    private RestClient restClient; // RestClient Mock 객체

    @Mock
    private Response mockResponse; // Response Mock 객체

    @Mock
    private HttpEntity mockHttpEntity; // HttpEntity Mock 객체

    @InjectMocks
    private ElasticsearchIndicesProvider indicesProvider; // 테스트 대상 클래스

    private static final String CAT_INDICES_HEADERS = "health,status,index,docs.count,docs.deleted,pri.store.size,store.size";

    @Test
    @DisplayName("getCatIndices_성공_인덱스목록반환")
    void testGetCatIndices_Success_ReturnsIndexList() throws IOException {
        // given: 테스트용 샘플 JSON 응답
        String sampleJsonResponse = "[{\"health\":\"green\", \"status\":\"open\", \"index\":\"index1\", \"docs.count\":\"100\", \"docs.deleted\":\"10\", \"pri.store.size\":\"10mb\", \"store.size\":\"20mb\"}," +
                                    "{\"health\":\"yellow\", \"status\":\"open\", \"index\":\"index2\", \"docs.count\":\"50\", \"docs.deleted\":\"5\", \"pri.store.size\":\"5mb\", \"store.size\":\"10mb\"}]";
        InputStream inputStream = new ByteArrayInputStream(sampleJsonResponse.getBytes(StandardCharsets.UTF_8));
        when(mockHttpEntity.getContent()).thenReturn(inputStream);
        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        when(restClient.performRequest(any(Request.class))).thenReturn(mockResponse);

        // when: 테스트 대상 메소드 호출
        List<Map<String, Object>> result = indicesProvider.getCatIndices();

        // then: 결과 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(restClient).performRequest(requestCaptor.capture());
        assertEquals("GET", requestCaptor.getValue().getMethod());
        assertEquals("/_cat/indices?format=json&h=" + CAT_INDICES_HEADERS, requestCaptor.getValue().getEndpoint());

        assertNotNull(result, "결과 리스트는 null이 아니어야 합니다.");
        assertEquals(2, result.size(), "결과 리스트의 크기가 예상과 다릅니다.");

        Map<String, Object> index1 = result.get(0);
        assertEquals("green", index1.get("health"));
        assertEquals("open", index1.get("status"));
        assertEquals("index1", index1.get("index"));
        assertEquals("100", index1.get("docsCount")); // 필드명 매핑 확인
        assertEquals("10", index1.get("docsDeleted"));
        assertEquals("10mb", index1.get("priStoreSize"));
        assertEquals("20mb", index1.get("storeSize"));

        Map<String, Object> index2 = result.get(1);
        assertEquals("yellow", index2.get("health"));
        assertEquals("index2", index2.get("index"));
        assertEquals("50", index2.get("docsCount"));
    }

    @Test
    @DisplayName("getCatIndicesByName_성공_특정인덱스반환")
    void testGetCatIndicesByName_Success_ReturnsSpecificIndex() throws IOException {
        // given: 테스트용 샘플 JSON 응답 (단일 인덱스)
        String indexName = "my_index";
        String sampleJsonResponse = "[{\"health\":\"green\", \"status\":\"open\", \"index\":\"my_index\", \"docs.count\":\"123\", \"docs.deleted\":\"23\", \"pri.store.size\":\"12mb\", \"store.size\":\"24mb\"}]";
        InputStream inputStream = new ByteArrayInputStream(sampleJsonResponse.getBytes(StandardCharsets.UTF_8));
        when(mockHttpEntity.getContent()).thenReturn(inputStream);
        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        when(restClient.performRequest(any(Request.class))).thenReturn(mockResponse);

        // when: 테스트 대상 메소드 호출
        List<Map<String, Object>> result = indicesProvider.getCatIndicesByName(indexName);

        // then: 결과 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(restClient).performRequest(requestCaptor.capture());
        assertEquals("GET", requestCaptor.getValue().getMethod());
        assertEquals("/_cat/indices/" + indexName + "?format=json&h=" + CAT_INDICES_HEADERS, requestCaptor.getValue().getEndpoint());

        assertNotNull(result, "결과 리스트는 null이 아니어야 합니다.");
        assertEquals(1, result.size(), "결과 리스트에는 하나의 인덱스만 포함되어야 합니다.");

        Map<String, Object> index = result.get(0);
        assertEquals("green", index.get("health"));
        assertEquals("open", index.get("status"));
        assertEquals("my_index", index.get("index"));
        assertEquals("123", index.get("docsCount"));
        assertEquals("23", index.get("docsDeleted"));
        assertEquals("12mb", index.get("priStoreSize"));
        assertEquals("24mb", index.get("storeSize"));
    }

    @Test
    @DisplayName("getCatIndices_IOException발생")
    void testGetCatIndices_ThrowsIOException() throws IOException {
        // given: RestClient.performRequest 호출 시 IOException 발생하도록 Mock 설정
        when(restClient.performRequest(any(Request.class))).thenThrow(new IOException("Simulated Network Error"));

        // when & then: getCatIndices 호출 시 IOException 발생하는지 검증
        IOException exception = assertThrows(IOException.class, () -> {
            indicesProvider.getCatIndices();
        }, "IOException이 발생해야 합니다.");
        assertEquals("Simulated Network Error", exception.getMessage());
    }

    @Test
    @DisplayName("getCatIndicesByName_IOException발생")
    void testGetCatIndicesByName_ThrowsIOException() throws IOException {
        // given: RestClient.performRequest 호출 시 IOException 발생하도록 Mock 설정
        String indexName = "error_index";
        when(restClient.performRequest(any(Request.class))).thenThrow(new IOException("Simulated Network Error for specific index"));

        // when & then: getCatIndicesByName 호출 시 IOException 발생하는지 검증
        IOException exception = assertThrows(IOException.class, () -> {
            indicesProvider.getCatIndicesByName(indexName);
        }, "IOException이 발생해야 합니다.");
        assertEquals("Simulated Network Error for specific index", exception.getMessage());
    }
}
