package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Map;

public class ElasticsearchClientProviderTest {

    private static final DockerImageName ELASTIC_IMAGE =
    DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.6.1");

    private static ElasticsearchContainer elasticsearchContainer;
    private static ElasticsearchClient client;
    private static ElasticsearchClientProvider provider;

    @BeforeAll
    static void setUpAll() throws IOException {
        // Start Elasticsearch container
        elasticsearchContainer = new ElasticsearchContainer(ELASTIC_IMAGE).withEnv("discovery.type", "single-node");
        elasticsearchContainer.start();

        // Build low-level RestClient
        RestClientBuilder builder = RestClient.builder(
            HttpHost.create(elasticsearchContainer.getHttpHostAddress())
        );
        RestClient lowLevelClient = builder.build();

        // Wrap in Elasticsearch Java API client
        client = new ElasticsearchClient(
            new RestClientTransport(lowLevelClient, new JacksonJsonpMapper())
        );

        provider = new ElasticsearchClientProvider(client);

        // Create index "test-index" with a simple mapping
        client.indices().create(c -> c.index("test-index")
            .mappings(m -> m
                .properties("field1", p -> p.text(t -> t))
                .properties("field2", p -> p.integer(i -> i))
            )
        );
    }

    @AfterAll
    static void tearDownAll() {
        if (elasticsearchContainer != null) {
            elasticsearchContainer.stop();
        }
    }

    @Test
    void getMappings_shouldReturnCorrectMapping() throws IOException {
        Object mappings = provider.getMappings("test-index");
        assertThat(mappings).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> properties =
            (Map<String, Object>) ((Map<?, ?>) mappings).get("properties");
        System.out.println(properties);
        assertThat(properties)
            .containsKey("field1")
            .containsKey("field2");
    }
}
