package com.silbaram.github.mcp.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.silbaram.github.mcp.server",
    "com.silbaram.github.infrastructures.elasticsearch"
})
public class ElasticSearchMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElasticSearchMcpServerApplication.class, args);
    }
}