# Elasticsearch MCP Server

## Overview

Elasticsearch Model Context Protocol(MCP) Server is a server application developed based on Spring AI MCP. It allows you to easily define various data processing workflows via the MCP and efficiently index and search the results in an Elasticsearch cluster. By using this server, you can leverage the following features.

### Key Features

- **Register and execute MCP tools**  
  Automatically register functions defined with the `@Tool` annotation in a Spring Boot environment as MCP tools, and call them via standard JSON-RPC communication with external clients (e.g., Claude, FastMCP CLI).
- **Elasticsearch cluster integration**  
  Use the official Elasticsearch Java client to conveniently call core Elasticsearch APIs such as cluster health check, index mapping retrieval, and document indexing/search, then expose the results as MCP tools.
- **Extensible architecture**  
  Designed with a modular package structure that allows flexible separation and management of client settings by Elasticsearch version, making it easy to add new tools or external integrations.

## Available Tools

- `get_cluster_health`: Check the health status of the Elasticsearch cluster.
- `get_cluster_statistics`: Returns comprehensive cluster statistics including cluster name, UUID, health status, node roles, OS and JVM resource usage, index counts, and shard metrics.
- `get_mappings`: Retrieve field mappings for a specific Elasticsearch index.
- `get_cat_indices`: Get a list of all indices in Elasticsearch.
- `get_cat_indices_by_name`: Get a list of indices matching the specified index name or wildcard pattern.
- `get_cat_aliases`: Get a list of all aliases Elasticsearch.
- `get_cat_aliases_by_name`: Get only the aliases matching the specified alias name or wildcard pattern.
- `get_document_search_by_index`: Searching for documents in Elasticsearch indices using AI-generated queryDsl.

## Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.4.5, Spring AI MCP
- **Search Engine**: Elasticsearch (version 7.16 or above)
- **Build Tool**: Gradle 8.12

## Prerequisites

To build and run this project, you will need the following software installed:

- JDK (version 17 or above)
- Elasticsearch (version 7.16 or above) running
- MCP client (e.g., Claude Desktop)

## Installation and Execution

Follow these steps to set up and run the project locally.

### 1. Clone the Source Code

```bash
git clone https://github.com/silbaram/elasticsearch-mcp-server.git
cd elasticsearch-mcp-server
```

### 2. Configure the `application.yml` of the `mcp-server` module

```yaml
elasticsearch:
  version: "8.6.1"
  search:
    hosts:
      - http://localhost:9200
```

- `version`: Specify the Elasticsearch cluster version to use
- `hosts`: Specify the URLs for connecting to the Elasticsearch cluster

### 3. Build

```bash
./gradlew build
```

- Location of the built JAR file: `mcp-server/build/libs/`

### 4. Configure the MCP Client

- Open your MCP client. Check the [MCP clients list](https://modelcontextprotocol.io/clients). Here, we configure Claude Desktop.
- Navigate to **Settings > Developer > MCP Servers**.
- Click **Edit Config** and add a new MCP server with the following configuration (using the built JAR file):

```json
{
  "mcpServers": {
    "elasticsearch-server": {
      "command": "java",
      "args": [
        "-jar",
        "/path/mcp-server.jar"
      ]
    }
  }
}
```