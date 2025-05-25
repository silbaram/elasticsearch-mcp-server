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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 확장 기능 사용
class ElasticsearchAliasesProviderTest {

    @Mock
    private RestClient restClient; // RestClient Mock 객체

    @Mock
    private Response mockResponse; // Response Mock 객체

    @Mock
    private HttpEntity mockHttpEntity; // HttpEntity Mock 객체

    @InjectMocks
    private ElasticsearchAliasesProvider aliasesProvider; // 테스트 대상 클래스

    @Test
    @DisplayName("getCatAliases_성공_별칭목록반환_숨김항목필터링")
    void testGetCatAliases_Success_ReturnsAliases_FiltersHidden() throws IOException {
        // given
        String jsonResponse = "[{\"alias\":\"alias1\", \"index\":\"index1\", \"filter\":\"-\", \"routing.index\":\"ri1\", \"routing.search\":\"rs1\", \"is_write_index\":\"true\"}," +
                              "{\"alias\":\".hidden_alias\", \"index\":\"index2\"}]";
        InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
        when(mockHttpEntity.getContent()).thenReturn(inputStream);
        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        when(restClient.performRequest(any(Request.class))).thenReturn(mockResponse);

        // when
        List<Map<String, Object>> result = aliasesProvider.getCatAliases();

        // then
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(restClient).performRequest(requestCaptor.capture());
        assertEquals("GET", requestCaptor.getValue().getMethod());
        assertEquals("/_cat/aliases?format=json", requestCaptor.getValue().getEndpoint());

        assertNotNull(result);
        assertEquals(1, result.size(), "숨김 별칭은 필터링되어야 합니다.");
        Map<String, Object> aliasMap = result.get(0);
        assertEquals("alias1", aliasMap.get("alias"));
        assertEquals("index1", aliasMap.get("index"));
        assertEquals("-", aliasMap.get("filter"));
        assertEquals("ri1", aliasMap.get("routingIndex"), "routing.index 필드가 routingIndex로 매핑되어야 합니다.");
        assertEquals("rs1", aliasMap.get("routingSearch"), "routing.search 필드가 routingSearch로 매핑되어야 합니다.");
        assertEquals("true", aliasMap.get("isWriteIndex"), "is_write_index 필드가 isWriteIndex로 매핑되어야 합니다.");
    }

    @Test
    @DisplayName("getCatAliasesByName_성공_특정별칭반환")
    void testGetCatAliasesByName_Success_ReturnsSpecificAlias() throws IOException {
        // given
        String aliasName = "my_alias";
        String jsonResponse = "[{\"alias\":\"my_alias\", \"index\":\"my_index\", \"filter\":\"*\", \"routing.index\":\"my_ri\", \"routing.search\":\"my_rs\", \"is_write_index\":\"false\"}]";
        InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
        when(mockHttpEntity.getContent()).thenReturn(inputStream);
        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        when(restClient.performRequest(any(Request.class))).thenReturn(mockResponse);

        // when
        List<Map<String, Object>> result = aliasesProvider.getCatAliasesByName(aliasName);

        // then
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(restClient).performRequest(requestCaptor.capture());
        assertEquals("GET", requestCaptor.getValue().getMethod());
        assertEquals("/_cat/aliases/" + aliasName + "?format=json", requestCaptor.getValue().getEndpoint());

        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> aliasMap = result.get(0);
        assertEquals("my_alias", aliasMap.get("alias"));
        assertEquals("my_index", aliasMap.get("index"));
        assertEquals("*", aliasMap.get("filter"));
        assertEquals("my_ri", aliasMap.get("routingIndex"));
        assertEquals("my_rs", aliasMap.get("routingSearch"));
        assertEquals("false", aliasMap.get("isWriteIndex"));
    }

    @Test
    @DisplayName("getCatAliasesByName_성공_숨김별칭필터링")
    void testGetCatAliasesByName_Success_FiltersHiddenAlias() throws IOException {
        // given
        String hiddenAliasName = ".my_hidden_alias";
        String jsonResponse = "[{\"alias\":\".my_hidden_alias\", \"index\":\"my_index\"}]";
        InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
        when(mockHttpEntity.getContent()).thenReturn(inputStream);
        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        when(restClient.performRequest(any(Request.class))).thenReturn(mockResponse);

        // when
        List<Map<String, Object>> result = aliasesProvider.getCatAliasesByName(hiddenAliasName);

        // then
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(restClient).performRequest(requestCaptor.capture());
        assertEquals("GET", requestCaptor.getValue().getMethod());
        assertEquals("/_cat/aliases/" + hiddenAliasName + "?format=json", requestCaptor.getValue().getEndpoint());

        assertNotNull(result);
        assertTrue(result.isEmpty(), "숨김 별칭은 필터링되어 목록이 비어있어야 합니다.");
    }

    @Test
    @DisplayName("getCatAliases_IOException발생")
    void testGetCatAliases_ThrowsIOException() throws IOException {
        // given
        when(restClient.performRequest(any(Request.class))).thenThrow(new IOException("Simulated IO Error"));

        // when & then
        assertThrows(IOException.class, () -> {
            aliasesProvider.getCatAliases();
        }, "IOException이 발생해야 합니다.");
    }

    @Test
    @DisplayName("getCatAliasesByName_IOException발생")
    void testGetCatAliasesByName_ThrowsIOException() throws IOException {
        // given
        String aliasName = "some_alias";
        when(restClient.performRequest(any(Request.class))).thenThrow(new IOException("Simulated IO Error"));

        // when & then
        assertThrows(IOException.class, () -> {
            aliasesProvider.getCatAliasesByName(aliasName);
        }, "IOException이 발생해야 합니다.");
    }
}
