package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.ElasticsearchCatClient;
import co.elastic.clients.elasticsearch.cat.IndicesRequest;
import co.elastic.clients.elasticsearch.cat.IndicesResponse;
import co.elastic.clients.elasticsearch.cat.indices.IndicesRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ElasticsearchIndicesProvider {

    private final ElasticsearchClient elasticsearchMcpClient;

    public ElasticsearchIndicesProvider(ElasticsearchClient elasticsearchMcpClient) {
        this.elasticsearchMcpClient = elasticsearchMcpClient;
    }

    /**
     * elasticsearch _cat/indices api
     */
    public List<Map<String, Object>> getCatIndices() throws IOException {
        // API 호출
        IndicesResponse indicesResponse = elasticsearchMcpClient.cat().indices();

        // 결과 변환
        List<Map<String, Object>> result = new ArrayList<>();
        for (IndicesRecord indicesRecord : indicesResponse.valueBody()) {
            Map<String, Object> index = new HashMap<>();
            index.put("index", indicesRecord.index());
            index.put("health", indicesRecord.health());
            index.put("status", indicesRecord.status());
            index.put("docsCount", indicesRecord.docsCount());
            index.put("docsDeleted", indicesRecord.docsDeleted());
            index.put("priStoreSize", indicesRecord.priStoreSize());
            index.put("storeSize", indicesRecord.storeSize());
            result.add(index);
        }

        return result;
    }

    /**
     * elasticsearch _cat/indices/{index} api
     */
    public List<Map<String, Object>> getCatIndicesByName(String indexName) throws IOException {
        // JSON 포맷 요청
        IndicesRequest request = new IndicesRequest.Builder()
                .index(indexName)
                .build();

        // API 호출
        IndicesResponse indicesResponse = elasticsearchMcpClient.cat().indices(request);

        // 결과 변환
        List<Map<String, Object>> result = new ArrayList<>();
        for (IndicesRecord indicesRecord : indicesResponse.valueBody()) {
            Map<String, Object> index = new HashMap<>();
            index.put("index", indicesRecord.index());
            index.put("health", indicesRecord.health());
            index.put("status", indicesRecord.status());
            index.put("docsCount", indicesRecord.docsCount());
            index.put("docsDeleted", indicesRecord.docsDeleted());
            index.put("priStoreSize", indicesRecord.priStoreSize());
            index.put("storeSize", indicesRecord.storeSize());
            result.add(index);
        }

        return result;
    }
}
