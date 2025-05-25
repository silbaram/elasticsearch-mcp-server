package com.silbaram.github.infrastructures.elasticsearch.provider;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
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
class ElasticsearchSearchProviderTest {

    @Mock
    private RestClient restClient; // RestClient Mock 객체

    @Mock
    private Response mockResponse; // Response Mock 객체

    // HttpEntity는 이 테스트에서 실제 NStringEntity를 사용하여 설정되므로 @Mock으로 선언하지 않습니다.

    @InjectMocks
    private ElasticsearchSearchProvider searchProvider; // 테스트 대상 클래스

    @Test
    @DisplayName("searchByIndex_성공_검색결과JSON문자열반환")
    void testSearchByIndex_Success_ReturnsSearchJsonString() throws IOException {
        // given: 테스트용 샘플 데이터
        String sampleIndex = "test_index";
        String sampleQueryBody = "{\"query\":{\"match_all\":{}}}";
        String sampleJsonResponse = "{\"hits\":{\"total\":{\"value\":1,\"relation\":\"eq\"},\"hits\":[]}}"; // Simplified

        // HttpEntity를 NStringEntity로 생성하여 실제 응답 본문을 시뮬레이션
        HttpEntity responseEntity = new NStringEntity(sampleJsonResponse, ContentType.APPLICATION_JSON);
        when(mockResponse.getEntity()).thenReturn(responseEntity);
        when(restClient.performRequest(any(Request.class))).thenReturn(mockResponse);

        // when: 테스트 대상 메소드 호출
        String result = searchProvider.searchByIndex(sampleIndex, sampleQueryBody);

        // then: 결과 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(restClient).performRequest(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        assertEquals("POST", capturedRequest.getMethod(), "HTTP 메소드가 POST여야 합니다.");
        assertEquals("/" + sampleIndex + "/_search", capturedRequest.getEndpoint(), "요청 엔드포인트가 정확해야 합니다.");
        // 요청 본문 검증
        assertEquals(sampleQueryBody, EntityUtils.toString(capturedRequest.getEntity()), "요청 본문이 예상과 일치해야 합니다.");
        
        assertEquals(sampleJsonResponse, result, "반환된 JSON 문자열이 예상과 일치해야 합니다.");
    }

    @Test
    @DisplayName("searchByIndex_IOException발생")
    void testSearchByIndex_ThrowsIOException() throws IOException {
        // given: RestClient.performRequest 호출 시 IOException 발생하도록 Mock 설정
        String sampleIndex = "any_index";
        String sampleQueryBody = "{\"query\":{\"match_all\":{}}}";
        when(restClient.performRequest(any(Request.class))).thenThrow(new IOException("Simulated IO Error"));

        // when & then: searchByIndex 호출 시 IOException 발생하는지 검증
        IOException exception = assertThrows(IOException.class, () -> {
            searchProvider.searchByIndex(sampleIndex, sampleQueryBody);
        }, "IOException이 발생해야 합니다.");
        
        assertEquals("Simulated IO Error", exception.getMessage(), "예외 메시지가 예상과 동일해야 합니다.");
    }
}
