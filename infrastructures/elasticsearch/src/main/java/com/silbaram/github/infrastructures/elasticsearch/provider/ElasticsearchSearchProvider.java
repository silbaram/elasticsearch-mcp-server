package com.silbaram.github.infrastructures.elasticsearch.provider;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticsearchSearchProvider {

    private final RestClient restClient;

    public ElasticsearchSearchProvider(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Elasticsearch: /{index}/_search API
     * 지정된 인덱스에 대해 JSON 쿼리 본문을 사용하여 검색을 수행하고 결과를 JSON 문자열로 반환합니다.
     *
     * @param index 대상 인덱스명
     * @param queryBody 검색에 사용될 JSON 쿼리 문자열
     * @return 검색 결과를 담은 JSON 문자열
     * @throws IOException API 호출 실패 시
     */
    public String searchByIndex(String index, String queryBody) throws IOException {
        Request request = new Request("POST", "/" + index + "/_search");
        // 요청 본문에 JSON 문자열 설정
        request.setJsonEntity(queryBody);

        Response response = restClient.performRequest(request);
        // HTTP 응답 본문을 문자열로 변환
        return EntityUtils.toString(response.getEntity());
    }
}
