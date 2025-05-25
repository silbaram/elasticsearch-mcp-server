package com.silbaram.github.infrastructures.elasticsearch.provider;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Mockito 확장 기능 사용
class ElasticsearchMappingsProviderTest {

    @Mock
    private RestClient restClient; // RestClient Mock 객체

    @Mock
    private Response mockResponse; // Response Mock 객체
    
    // HttpEntity는 직접 Mocking하거나, 실제 인스턴스(NStringEntity)를 사용할 수 있습니다.
    // 여기서는 실제 NStringEntity를 사용하여 응답 본문을 설정합니다.

    @InjectMocks
    private ElasticsearchMappingsProvider mappingsProvider; // 테스트 대상 클래스

    @Test
    @DisplayName("getCatMappings_성공_매핑JSON문자열반환")
    void testGetCatMappings_Success_ReturnsMappingJsonString() throws IOException {
        // given: 테스트용 인덱스명 및 샘플 JSON 응답
        String indexName = "my_index";
        String sampleJsonResponse = "{ \"" + indexName + "\": { \"mappings\": { \"properties\": { \"field1\": { \"type\": \"text\" } } } } }";
        
        // HttpEntity를 NStringEntity로 생성하여 실제 응답 본문을 시뮬레이션합니다.
        HttpEntity httpEntity = new NStringEntity(sampleJsonResponse, ContentType.APPLICATION_JSON);
        
        when(mockResponse.getEntity()).thenReturn(httpEntity);
        when(restClient.performRequest(any(Request.class))).thenReturn(mockResponse);

        // when: 테스트 대상 메소드 호출
        String result = mappingsProvider.getCatMappings(indexName);

        // then: 결과 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(restClient).performRequest(requestCaptor.capture());
        Request actualRequest = requestCaptor.getValue();
        
        assertEquals("GET", actualRequest.getMethod(), "HTTP 메소드가 GET이어야 합니다.");
        assertEquals("/" + indexName + "/_mapping", actualRequest.getEndpoint(), "요청 엔드포인트가 정확해야 합니다.");
        
        assertEquals(sampleJsonResponse, result, "반환된 JSON 문자열이 예상과 일치해야 합니다.");
    }

    @Test
    @DisplayName("getCatMappings_IOException발생")
    void testGetCatMappings_ThrowsIOException() throws IOException {
        // given: RestClient.performRequest 호출 시 IOException 발생하도록 Mock 설정
        String indexName = "some_index";
        when(restClient.performRequest(any(Request.class))).thenThrow(new IOException("Simulated Network Error"));

        // when & then: getCatMappings 호출 시 IOException 발생하는지 검증
        IOException exception = assertThrows(IOException.class, () -> {
            mappingsProvider.getCatMappings(indexName);
        }, "IOException이 발생해야 합니다.");
        
        assertEquals("Simulated Network Error", exception.getMessage(), "예외 메시지가 예상과 동일해야 합니다.");
    }
}
