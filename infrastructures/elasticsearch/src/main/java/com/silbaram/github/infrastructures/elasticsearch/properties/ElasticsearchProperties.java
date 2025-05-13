package com.silbaram.github.infrastructures.elasticsearch.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "elasticsearch.search")
public class ElasticsearchProperties {
    /** application.yml 의 elasticsearch.search.hosts 값을 바인딩 */
    private List<String> hosts;
    private String username;
    private String password;

    public List<String> getHosts() {
        return hosts;
    }
    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
