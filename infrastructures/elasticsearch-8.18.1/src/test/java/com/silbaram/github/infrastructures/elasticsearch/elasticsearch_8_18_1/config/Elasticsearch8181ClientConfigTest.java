package com.silbaram.github.infrastructures.elasticsearch.elasticsearch_8_18_1.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.silbaram.github.infrastructures.elasticsearch.config.Elasticsearch8181ClientConfig;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(
    classes = Elasticsearch8181ClientConfig.class,
    properties = {
        "elasticsearch.version=8.18.1",
        "elasticsearch.search.hosts[0]=http://localhost:9200",
        "elasticsearch.search.hosts[1]=http://localhost:9201"
    }
)
public class Elasticsearch8181ClientConfigTest {

    @Autowired
    ApplicationContext context;

    @Test
    void configClassIsLoaded() {
        // 설정 클래스 자체는 빈으로 등록되지 않으므로,
        // 이를 통해 생성된 RestClient 혹은 ElasticsearchClient 빈이 있는지 확인
        assertThat(context.getBeanNamesForType(RestClient.class))
                .as("RestClient 빈이 등록되어야 한다")
                .isNotEmpty();

        assertThat(context.getBean(ElasticsearchClient.class))
                .as("ElasticsearchClient 빈이 등록되어야 한다")
                .isNotNull();
    }
}
