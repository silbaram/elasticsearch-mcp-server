package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.jackson.JacksonJsonpGenerator;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.core.JsonFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Map;

@Component
public class ElasticsearchSearchProvider {

    private final ElasticsearchClient elasticsearchMcpClient;

    public ElasticsearchSearchProvider(ElasticsearchClient elasticsearchMcpClient) {
        this.elasticsearchMcpClient = elasticsearchMcpClient;
    }

    /**
     * elasticsearch: /{index}/_search API
     */
    public String searchByIndex(String index, String queryBody) throws IOException {

        // SearchRequest에 index와 raw JSON(body)을 그대로 주입
        SearchRequest searchRequest = SearchRequest.of(request -> request
            .index(index)
            .withJson(new StringReader(queryBody))
        );

        // 검색 실행, 결과를 Map 형태로 받음
        SearchResponse<Map<String, Object>> response = elasticsearchMcpClient.search(searchRequest, (Type) Map.class);

        StringWriter jsonResult = new StringWriter();
        try (
            // Jackson JsonFactory로 JsonGenerator 생성
            JacksonJsonpGenerator generator = new JacksonJsonpGenerator(new JsonFactory().createGenerator(jsonResult))
        ) {
            // searchRequest: JsonpSerializable 객체 (예: SearchRequest)
            // JacksonJsonpMapper를 함께 넘겨줘야 JSON-P 직렬화가 가능
            response.serialize(generator, new JacksonJsonpMapper());
        }

        return jsonResult.toString();
    }
}
