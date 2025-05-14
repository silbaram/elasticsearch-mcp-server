package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthRequest;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import com.silbaram.github.infrastructures.elasticsearch.properties.ElasticsearchProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Component
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchClientProvider {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchClientProvider.class);

    private final ElasticsearchClient elasticsearchMcpClient;

    public ElasticsearchClientProvider(ElasticsearchClient elasticsearchMcpClient) {
        this.elasticsearchMcpClient = elasticsearchMcpClient;
    }

    public Object getMappings(String index) throws IOException {
        GetMappingResponse mappingResponse = elasticsearchMcpClient.indices()
                .getMapping(g -> g.index(index));

        return Objects.requireNonNull(mappingResponse.get(index)).mappings();
    }

    public Map<String, String> getClusterHealth() throws IOException {

        // Create a HealthRequest
        HealthRequest healthRequest = new HealthRequest.Builder().build();
        // Fetch the cluster health
        HealthResponse response = elasticsearchMcpClient.cluster().health(healthRequest);

        return Collections.singletonMap("status", response.status().jsonValue());
    }
}
