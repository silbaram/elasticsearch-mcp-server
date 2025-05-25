package com.silbaram.github.infrastructures.elasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.silbaram.github.infrastructures.elasticsearch.properties.ElasticsearchProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchClientConfig {

    private final ElasticsearchProperties props;
    private static final String EMPTY_VALUE = "EMPTY";

    public ElasticsearchClientConfig(ElasticsearchProperties props) {
        this.props = props;
    }

    @Bean
    public RestClient restClient() {

        HttpHost[] httpHosts = props.getHosts().stream()
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);

            // 아이디, 패스워드가 있을 때만 인증 추가
            if (!Objects.equals(props.getUsername(), EMPTY_VALUE) && !Objects.equals(props.getPassword(), EMPTY_VALUE)) {
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(props.getUsername(), props.getPassword())
                );

                return RestClient.builder(httpHosts)
                        .setHttpClientConfigCallback(httpClientBuilder ->
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                        )
                        .build();
            } else {
                return RestClient.builder(httpHosts).build();
            }
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