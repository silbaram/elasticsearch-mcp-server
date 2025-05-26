<div align="center">
  <h1>Elasticsearch MCP Server</h1>
  <p>Spring AI MCP 기반 Elasticsearch 데이터 처리 및 검색 서버</p>
</div>
<hr/>

# Elasticsearch MCP Server

## 소개

Elasticsearch Model Context Protocol(MCP) Server는 Spring AI MCP를 기반으로 개발된 서버 애플리케이션입니다. 
MCP를 통해 다양한 데이터 처리 워크플로우를 손쉽게 정의하고, 그 결과를 Elasticsearch 클러스터에 효율적으로 색인하고 검색할 수 있도록 설계되었습니다.

### 주요 특징
- **MCP 도구 자동 등록 및 실행**: Spring Boot 환경에서 `@Tool` 애노테이션으로 정의된 기능을 MCP 서버에 자동으로 등록합니다. 이를 통해 외부 클라이언트(예: Claude, FastMCP CLI)와 표준 JSON-RPC 기반 통신으로 해당 기능을 호출할 수 있습니다.
- **Elasticsearch 클러스터 연동**: Elasticsearch 공식 Java 클라이언트를 사용하여 클러스터 상태 확인, 인덱스 매핑 조회, 문서 색인 및 검색 등 Elasticsearch의 주요 API를 간편하게 호출하고, 그 결과를 MCP 도구 형태로 제공합니다.
- **확장 가능한 구조**: 모듈화된 패키지 구조를 통해 Elasticsearch 버전별 클라이언트 설정을 유연하게 분리하고 관리할 수 있습니다. 또한, 새로운 도구나 외부 연동 기능을 손쉽게 추가할 수 있도록 설계되었습니다.

## 사용 가능한 MCP 도구

- `get_cluster_health`: Elasticsearch 클러스터의 상태에 대한 기본 정보를 반환합니다.
- `get_cluster_statistics`: 클러스터 이름, UUID, 상태, 노드 역할, OS 및 JVM 리소스 사용량, 인덱스 수, 샤드 메트릭 등 포괄적인 클러스터 통계를 가져옵니다.
- `get_cat_mappings`: 특정 Elasticsearch 인덱스에 대한 필드 매핑 정보를 가져옵니다.
- `get_cat_indices`: Elasticsearch의 모든 인덱스 목록을 가져옵니다.
- `get_cat_indices_by_name`: 지정된 인덱스 이름 또는 와일드카드 패턴과 일치하는 인덱스 목록을 가져옵니다.
- `get_cat_aliases`: Elasticsearch의 모든 별칭 목록을 가져옵니다.
- `get_cat_aliases_by_name`: 지정된 별칭 이름 또는 와일드카드 패턴과 일치하는 별칭 목록을 가져옵니다.
- `get_document_search_by_index`: AI가 생성한 queryDSL을 사용하여 Elasticsearch 인덱스 내 문서를 검색합니다.
- `get_shard_allocation`: Elasticsearch 클러스터의 샤드 할당 정보를 반환합니다.
- `get_shard_allocation_for_node`: Elasticsearch 클러스터의 특정 노드에 대한 샤드 할당 정보를 반환합니다.

## 기술 스택

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.4.5, Spring AI MCP
- **검색 엔진**: Elasticsearch 7.16 버전 이상
- **빌드 도구**: Gradle 8.12

## 사전 요구 사항

이 프로젝트를 빌드하고 실행하기 위해 다음 소프트웨어가 필요합니다.
- JDK: 17 버전 이상
- 실행 중인 Elasticsearch: 7.16 버전 이상
- MCP 클라이언트 (예: Claude Desktop)

## 설치 및 실행

프로젝트를 로컬 환경에서 설정하고 실행하는 방법은 다음과 같습니다.

### 1. 소스 코드 복제
```bash
git clone https://github.com/silbaram/elasticsearch-mcp-server.git
cd elasticsearch-mcp-server
```

### 2. `mcp-server` 모듈의 `application.yml` 설정
`mcp-server` 모듈 내 `src/main/resources/application.yml` 파일을 열어 Elasticsearch 클러스터 정보를 설정합니다.
  ```yaml
  elasticsearch:
    version: "8.6.1" # 연결할 Elasticsearch 클러스터의 버전을 지정합니다.
    search:
      hosts:
        - http://localhost:9200 # Elasticsearch 클러스터의 접속 주소를 지정합니다.
  ```

### 3. 빌드
다음 명령어를 사용하여 프로젝트를 빌드합니다.
  ```bash
  ./gradlew build
  ```
- 빌드된 JAR 파일은 `mcp-server/build/libs/` 디렉터리에서 확인할 수 있습니다.

### 4. MCP 클라이언트 구성
- 사용 중인 MCP 클라이언트를 실행합니다. ([MCP 클라이언트 목록](https://modelcontextprotocol.io/clients)에서 다양한 클라이언트를 확인할 수 있습니다. 이 가이드에서는 Claude Desktop을 기준으로 설명합니다.)
- MCP 클라이언트의 설정 메뉴에서 '개발자' > 'MCP 서버' 항목으로 이동합니다.
- 'Edit Config' (또는 '설정 편집') 버튼을 클릭하고, 다음 내용을 참고하여 새 MCP 서버 구성을 추가합니다. (앞서 빌드한 JAR 파일의 실제 경로를 사용해야 합니다.)
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
- `-Dusername` (선택 사항): Elasticsearch 클러스터 접근에 필요한 사용자 아이디를 지정합니다. (예: `-Dusername=elastic`)
- `-Dpassword` (선택 사항): Elasticsearch 클러스터 접근에 필요한 비밀번호를 지정합니다. (예: `-Dpassword=yoursecurepassword`)
- `/path/to/your/mcp-server.jar`: 빌드된 `mcp-server.jar` 파일의 실제 전체 경로로 수정해야 합니다.