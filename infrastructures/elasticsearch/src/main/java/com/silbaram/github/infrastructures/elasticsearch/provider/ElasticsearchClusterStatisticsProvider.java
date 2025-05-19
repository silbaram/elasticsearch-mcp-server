package com.silbaram.github.infrastructures.elasticsearch.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.DocStats;
import co.elastic.clients.elasticsearch.cluster.ClusterStatsResponse;
import co.elastic.clients.elasticsearch.cluster.stats.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component
public class ElasticsearchClusterStatisticsProvider {

    private final ElasticsearchClient elasticsearchMcpClient;

    public ElasticsearchClusterStatisticsProvider(ElasticsearchClient elasticsearchMcpClient) {
        this.elasticsearchMcpClient = elasticsearchMcpClient;
    }

    /**
     * elasticsearch _cluster/stats api
     */
    public Map<String, Object> getClusterStatistics() throws IOException {
        ClusterStatsResponse response = elasticsearchMcpClient.cluster().stats();
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

        result.put("indices", indicesInfo);

        return result;
    }
}
