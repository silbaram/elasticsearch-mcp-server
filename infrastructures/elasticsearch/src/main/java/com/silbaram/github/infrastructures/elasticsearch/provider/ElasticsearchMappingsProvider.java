package com.silbaram.github.infrastructures.elasticsearch.provider;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticsearchMappingsProvider {

    private final RestClient restClient;

    public ElasticsearchMappingsProvider(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Elasticsearch: /{index}/_mapping API
     * 지정된 인덱스의 매핑 정보를 JSON 문자열로 조회합니다.
     * @param index 대상 인덱스명
     * @return 매핑 정보를 담은 JSON 문자열
     * @throws IOException API 호출 실패 시
     */
    public String getCatMappings(String index) throws IOException {
        Request request = new Request("GET", "/" + index + "/_mapping");
        Response response = restClient.performRequest(request);
        // HTTP 응답 본문을 문자열로 변환
        return EntityUtils.toString(response.getEntity());
    }
}
