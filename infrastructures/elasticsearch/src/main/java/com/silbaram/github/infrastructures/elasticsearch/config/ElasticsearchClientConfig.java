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
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchClientConfig {

    private final ElasticsearchProperties props;

    public ElasticsearchClientConfig(ElasticsearchProperties props) {
        this.props = props;
    }

    @Bean
    public RestClient restClient() {
        //todo username, password가 있을때 코드 추그해야됨
//        // Set up credentials provider with username and password
//        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(props.getUsername(), props.getPassword()));
//
//        HttpHost[] httpHosts = props.getHosts().stream()
//            .map(HttpHost::create)
//            .toArray(HttpHost[]::new);

//        return RestClient.builder(httpHosts)
//            .setHttpClientConfigCallback(httpClientBuilder ->
//                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
//            .build();
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