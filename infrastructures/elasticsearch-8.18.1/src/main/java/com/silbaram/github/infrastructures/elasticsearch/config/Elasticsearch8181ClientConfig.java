package com.silbaram.github.infrastructures.elasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.silbaram.github.infrastructures.elasticsearch.properties.ElasticsearchProperties;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConditionalOnProperty(
//    prefix = "elasticsearch",
//    name = "version",
//    havingValue = "8.18.1"
//)
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class Elasticsearch8181ClientConfig {

    private final ElasticsearchProperties props;

    public Elasticsearch8181ClientConfig(ElasticsearchProperties props) {
        this.props = props;
    }

    @Bean
    public RestClient restClient() {
        HttpHost[] httpHosts = props.getHosts().stream()
            .map(HttpHost::create)
            .toArray(HttpHost[]::new);
        return RestClient.builder(httpHosts).build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        // JacksonJsonpMapper를 이용한 JSON 파싱
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchMcpClient(ElasticsearchTransport elasticsearchTransport) {
        // 최종 Java API 클라이언트
        return new ElasticsearchClient(elasticsearchTransport);
    }

}