package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.DocStats;
import co.elastic.clients.elasticsearch._types.StoreStats;
import co.elastic.clients.elasticsearch.cluster.ClusterStatsRequest;
import co.elastic.clients.elasticsearch.cluster.ClusterStatsResponse;
import co.elastic.clients.elasticsearch.cluster.stats.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ElasticsearchClusterStatisticsProvider {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchClusterStatisticsProvider.class);
    private final ElasticsearchClient elasticsearchMcpClient;

    public ElasticsearchClusterStatisticsProvider(ElasticsearchClient elasticsearchMcpClient) {
        this.elasticsearchMcpClient = elasticsearchMcpClient;
    }

    public Map<String, Object> getClusterStatistics() throws IOException {
        ClusterStatsRequest request = new ClusterStatsRequest.Builder().build();
        ClusterStatsResponse response = elasticsearchMcpClient.cluster().stats(request);
        Map<String, Object> result = new HashMap<>();

        // 클러스터 정보
        Map<String, Object> clusterInfo = new HashMap<>();
        clusterInfo.put("name", response.clusterName());
        clusterInfo.put("uuid", response.clusterUuid());
        clusterInfo.put("status", response.status().toString());
        clusterInfo.put("timestamp", response.timestamp());
        result.put("cluster", clusterInfo);

        // 노드 정보
        ClusterNodes nodes = response.nodes();
        ClusterNodeCount count = nodes.count();
        Map<String, Object> nodesInfo = new HashMap<>();
        nodesInfo.put("total", count.total());
        nodesInfo.put("data", count.data());
        nodesInfo.put("master", count.master());
        nodesInfo.put("ingest", count.ingest());
        result.put("nodes", nodesInfo);

        // OS · JVM 정보
        nodesInfo.put("mem_used_percent", nodes.os().mem().usedPercent());
        // JVM 메모리 raw 바이트 가져오기
        ClusterJvmMemory jvmMemory = nodes.jvm().mem();
        long heapUsedBytes = jvmMemory.heapUsedInBytes();
        long heapMaxBytes = jvmMemory.heapMaxInBytes();
        double heapUsedPercent = heapMaxBytes > 0 ? (double) heapUsedBytes / heapMaxBytes * 100 : 0.0;
        nodesInfo.put("heap_used_bytes", heapUsedBytes);
        nodesInfo.put("heap_max_bytes", heapMaxBytes);
        nodesInfo.put("heap_used_percent", heapUsedPercent);
        nodesInfo.put("processors", nodes.os().availableProcessors());

        // 4) indices() → ClusterIndices
        ClusterIndices indices = response.indices();
        Map<String, Object> indicesInfo = new HashMap<>();
        indicesInfo.put("count", indices.count());
        result.put("indices", indicesInfo);

        // 샤드
        ClusterIndicesShards shards = indices.shards();
        Map<String, Object> shardsInfo = new HashMap<>();
        shardsInfo.put("total", shards.total());
        shardsInfo.put("primaries", shards.primaries());
        shardsInfo.put("replication", shards.replication());
        indicesInfo.put("shards", shardsInfo);

        // 문서
        DocStats docs = indices.docs();
        Map<String, Object> docsInfo = new HashMap<>();
        docsInfo.put("count", docs.count());
        docsInfo.put("deleted", docs.deleted());
        indicesInfo.put("docs", docsInfo);

        // 스토어 크기
        StoreStats store = indices.store();
        log.info("store : {}", store);
        log.info("store_bytes : {}", store.sizeInBytes());
        indicesInfo.put("store_bytes", Long.valueOf(store.sizeInBytes()));

        return result;
    }
}
