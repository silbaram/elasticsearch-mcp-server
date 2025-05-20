package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.AliasesRequest;
import co.elastic.clients.elasticsearch.cat.AliasesResponse;
import co.elastic.clients.elasticsearch.cat.aliases.AliasesRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ElasticsearchAliasesProvider {

    private final ElasticsearchClient elasticsearchMcpClient;

    public ElasticsearchAliasesProvider(ElasticsearchClient elasticsearchMcpClient) {
        this.elasticsearchMcpClient = elasticsearchMcpClient;
    }

    /**
     * /_cat/aliases
     */
    public List<Map<String, Object>> getCatAliases() throws IOException {
        // API 호출
        AliasesResponse aliasesResponse = elasticsearchMcpClient.cat().aliases();

        // 결과를 List<Map<String,Object>> 형태로 변환
        List<Map<String, Object>> result = new ArrayList<>();
        for (AliasesRecord aliasesRecord : aliasesResponse.valueBody()) {

            String alias = aliasesRecord.alias();
            // 점(.)으로 시작하면 시스템/숨김 별칭이므로 건너뛴다
            if (alias != null && alias.startsWith(".")) {
                continue;
            }

            Map<String, Object> aliasMap = new HashMap<>();
            aliasMap.put("alias", aliasesRecord.alias());
            aliasMap.put("index", aliasesRecord.index());
            aliasMap.put("filter", aliasesRecord.filter());
            aliasMap.put("routingIndex", aliasesRecord.routingIndex());
            aliasMap.put("routingSearch", aliasesRecord.routingSearch());
            aliasMap.put("isWriteIndex", aliasesRecord.isWriteIndex());
            result.add(aliasMap);
        }
        return result;
    }

    /**
     * /_cat/aliases/{name}
     */
    public List<Map<String, Object>> getCatAliasesByName(String aliasName) throws IOException {
        // Aliases 요청 생성
        AliasesRequest aliasesRequest = new AliasesRequest.Builder().name(aliasName).build();

        // API 호출
        AliasesResponse aliasesResponse = elasticsearchMcpClient.cat().aliases(aliasesRequest);

        // 결과를 List<Map<String,Object>> 형태로 변환
        List<Map<String, Object>> result = new ArrayList<>();
        for (AliasesRecord aliasesRecord : aliasesResponse.valueBody()) {

            String alias = aliasesRecord.alias();
            // 점(.)으로 시작하면 시스템/숨김 별칭이므로 건너뛴다
            if (alias != null && alias.startsWith(".")) {
                continue;
            }

            Map<String, Object> aliasMap = new HashMap<>();
            aliasMap.put("alias", aliasesRecord.alias());
            aliasMap.put("index", aliasesRecord.index());
            aliasMap.put("filter", aliasesRecord.filter());
            aliasMap.put("routingIndex", aliasesRecord.routingIndex());
            aliasMap.put("routingSearch", aliasesRecord.routingSearch());
            aliasMap.put("isWriteIndex", aliasesRecord.isWriteIndex());
            result.add(aliasMap);
        }
        return result;
    }
}
