<div align="center">
  <h1>Elasticsearch MCP Server</h1>
  <p>Spring AI MCP-based Elasticsearch Data Processing and Search Server</p>
</div>
<hr/>

# Elasticsearch MCP Server

## Introduction

The Elasticsearch Model Context Protocol (MCP) Server is a server application developed based on Spring AI MCP.
It is designed to easily define various data processing workflows through MCP and efficiently index and search the results in an Elasticsearch cluster.

### Main Features
- **Automatic MCP Tool Registration and Execution**: Features defined with the `@Tool` annotation in a Spring Boot environment are automatically registered with the MCP server. This allows external clients (e.g., Claude, FastMCP CLI) to call these functions via standard JSON-RPC based communication.
- **Elasticsearch Cluster Integration**: Uses the official Elasticsearch Java client to easily call major Elasticsearch APIs such as cluster health checks, index mapping lookups, document indexing and searching, and provides the results in the form of MCP tools.
- **Scalable Architecture**: A modular package structure allows for flexible separation and management of client settings for different Elasticsearch versions. It is also designed to easily add new tools or external integration features.

## Available MCP Tools

- `get_cluster_health`: Returns basic information about the status of the Elasticsearch cluster.
- `get_cluster_statistics`: Retrieves comprehensive cluster statistics including cluster name, UUID, status, node roles, OS and JVM resource usage, index count, and shard metrics.
- `get_cat_mappings`: Retrieves field mapping information for a specific Elasticsearch index.
- `get_cat_indices`: Retrieves a list of all indices in Elasticsearch.
- `get_cat_indices_by_name`: Retrieves a list of indices that match the specified index name or wildcard pattern.
- `get_cat_aliases`: Retrieves a list of all aliases in Elasticsearch.
- `get_cat_aliases_by_name`: Retrieves a list of aliases that match the specified alias name or wildcard pattern.
- `get_document_search_by_index`: Searches for documents within an Elasticsearch index using AI-generated queryDSL.
- `get_shard_allocation`: Returns information about shard allocation in the Elasticsearch cluster.
- `get_shard_allocation_for_node`: Returns information about shard allocation for a specific node in the Elasticsearch cluster.

## Technology Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.4.5, Spring AI MCP
- **Search Engine**: Elasticsearch 7.16 or later
- **Build Tool**: Gradle 8.12

## Prerequisites

The following software is required to build and run this project:
- JDK: Version 17 or later
- Running Elasticsearch instance: Version 7.16 or later
- MCP Client (e.g., Claude Desktop)

## Installation and Execution

Here's how to set up and run the project in your local environment:

### 1. Clone the Source Code
```bash
git clone https://github.com/silbaram/elasticsearch-mcp-server.git
cd elasticsearch-mcp-server
```

### 2. Configure `application.yml` in the `mcp-server` module
Open the `application.yml` file located in `mcp-server/src/main/resources/application.yml` to set up your Elasticsearch cluster information.
  ```yaml
  elasticsearch:
    version: "8.6.1" # Specifies the version of the Elasticsearch cluster to connect to.
    search:
      hosts:
        - http://localhost:9200 # Specifies the access address of the Elasticsearch cluster.
  ```

### 3. Build
Use the following command to build the project:
  ```bash
  ./gradlew build
  ```
- The built JAR file can be found in the `mcp-server/build/libs/` directory.

### 4. Configure MCP Client
- Launch your MCP client. (You can find a list of MCP clients at [MCP Client List](https://modelcontextprotocol.io/clients). This guide uses Claude Desktop as an example.)
- In your MCP client's settings menu, navigate to 'Developer' > 'MCP Servers'.
- Click the 'Edit Config' button and add a new MCP server configuration with the following content. (You must use the actual path to the JAR file you built earlier.)
  ```json
  {
    "mcpServers": {
      "elasticsearch-server": {
        "command": "java",
        "args": [
          "-Dusername=YOUR_USERNAME", 
          "-Dpassword=YOUR_PASSWORD",
          "-jar",
          "/path/to/your/mcp-server.jar" 
        ]
      }
    }
  }
  ```
- `-Dusername` (Optional): Specifies the user ID required to access the Elasticsearch cluster. (e.g., `-Dusername=elastic`)
- `-Dpassword` (Optional): Specifies the password required to access the Elasticsearch cluster. (e.g., `-Dpassword=yoursecurepassword`)
- `/path/to/your/mcp-server.jar`: This must be replaced with the actual full path to your built `mcp-server.jar` file.