package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class ElasticsearchClientProvider {
    private final ElasticsearchClient elasticsearchMcpClient;

    public ElasticsearchClientProvider(ElasticsearchClient elasticsearchMcpClient) {
        this.elasticsearchMcpClient = elasticsearchMcpClient;
    }

    public Object getMappings(String index) throws IOException {
        GetMappingResponse mappingResponse = elasticsearchMcpClient.indices()
                .getMapping(g -> g.index(index));

        return Objects.requireNonNull(mappingResponse.get(index)).mappings();
    }
}
