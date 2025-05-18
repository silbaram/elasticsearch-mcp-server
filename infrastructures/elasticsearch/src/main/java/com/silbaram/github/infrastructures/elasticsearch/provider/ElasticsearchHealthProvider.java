package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthRequest;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
public class ElasticsearchHealthProvider {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchHealthProvider.class);

    final static HealthRequest healthRequest = new HealthRequest.Builder().build();
    private final ElasticsearchClient elasticsearchMcpClient;

    public ElasticsearchHealthProvider(ElasticsearchClient elasticsearchMcpClient) {
        this.elasticsearchMcpClient = elasticsearchMcpClient;
    }


    public Map<String, String> getClusterHealth() throws IOException {
        // Fetch the cluster health
        HealthResponse response = elasticsearchMcpClient.cluster().health(healthRequest);

        return Collections.singletonMap("status", response.status().jsonValue());
    }
}
