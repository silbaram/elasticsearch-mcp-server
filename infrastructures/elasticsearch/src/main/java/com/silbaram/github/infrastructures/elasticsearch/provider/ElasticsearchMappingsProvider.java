package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.elasticsearch.indices.get_mapping.IndexMappingRecord;
import co.elastic.clients.json.jackson.JacksonJsonpGenerator;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.core.JsonFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;

@Component
public class ElasticsearchMappingsProvider {

    private final ElasticsearchClient elasticsearchMcpClient;

    public ElasticsearchMappingsProvider(ElasticsearchClient elasticsearchMcpClient) {
        this.elasticsearchMcpClient = elasticsearchMcpClient;
    }

    /**
     * elasticsearch /_cat/indices api
     */
    public String getMappings(String index) throws IOException {
        GetMappingResponse mappingResponse = elasticsearchMcpClient.indices()
            .getMapping(request -> request.index(index));
        IndexMappingRecord record = mappingResponse.result().get(index);

        StringWriter writer = new StringWriter();
        try (
            // 1) Jackson JsonFactory로 JsonGenerator 생성
            JacksonJsonpGenerator generator = new JacksonJsonpGenerator(new JsonFactory().createGenerator(writer))
        ) {
            // 2) searchRequest: JsonpSerializable 객체 (예: SearchRequest)
            //    JacksonJsonpMapper를 함께 넘겨줘야 JSON-P 직렬화가 가능
            record.serialize(generator, new JacksonJsonpMapper());
        }

        // 3) 최종 JSON 문자열
        return writer.toString();
    }
}
