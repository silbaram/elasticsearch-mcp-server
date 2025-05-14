# Elasticsearch MCP Server

## 소개

`Elasticsearch MCP Server`Elasticsearch MCP Server는 Spring AI MCP 기반으로 개발된 서버 애플리케이션으로, 
모델 컨텍스트 프로토콜(Model Context Protocol, MCP)을 통해 다양한 데이터 처리 워크플로우를 손쉽게 정의하고,
그 결과를 Elasticsearch 클러스터에 효율적으로 색인·검색할 수 있도록 설계되었습니다.
이 서버를 이용하면 다음과 같은 기능을 활용할 수 있습니다:

### 주요 특징은 다음과 같습니다:
- MCP 도구(tool) 등록 및 실행
Spring Boot 환경에서 @Tool 애노테이션으로 정의된 기능을 MCP 서버에 자동 등록하여, 외부 클라이언트(예: Claude, FastMCP CLI)와의 표준 JSON-RPC 기반 통신을 통해 호출할 수 있습니다.
- Elasticsearch 클러스터 연동
elasticsearch 공식 Java 클라이언트를 사용하여, 클러스터 헬스체크, 인덱스 매핑 조회, 도큐먼트 색인·검색 등 Elasticsearch의 주요 API를 간편하게 호출하고 결과를 MCP 도구 형태로 제공할 수 있습니다.
- 확장 가능한 구조
모듈화된 패키지 구조를 통해 Elasticsearch 버전별 클라이언트 설정을 유연하게 분리·관리할 수 있으며, 새로운 도구(tool)나 외부 연동 기능을 손쉽게 추가할 수 있도록 설계되었습니다.

## 사용 기능 도구

- get_cluster_health : Elasticsearch cluster 상태 체크
- get_mappings : 특정 Elasticsearch 인덱스에 대한 필드 매핑 가져오기

## 기술 스택

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.4.5, Spring AI MCP
- **검색 엔진**: Elasticsearch [7.16 버전 이상]
- **빌드 도구**: Gradle 8.12

## 사전 요구 사항

이 프로젝트를 빌드하고 실행하기 위해 필요한 소프트웨어 목록입니다.
- JDK [버전, 예: 17   이상]
- Elasticsearch [버전 7.16 이상] 실행 중
- MCP 클라이언트(예: Claude Desktop)

## 설치 및 실행

프로젝트를 로컬 환경에서 설정하고 실행하는 방법입니다.

### 1. 소스 코드 복제
```bash
git clone https://github.com/silbaram/elasticsearch-mcp-server.git
cd elasticsearch-mcp-server
```

### 2. mcp-server 모듈의 application.yml 설정
  ```yaml
  elasticsearch:
    version: "8.6.1"
    search:
      hosts:
        - http://localhost:9200
  ```
- version : 사용하고자 하는 Elasticsearch cluster 버전 지정
- hosts : Elasticsearch cluster 접속 URL 지정

### 3. 빌드
  ```yaml
    ./gradlew build
  ```
- 빌드 JAR 파일 위치 : mcp-server/build/libs/

### 4. MCP 클라이언트 구성
- MCP 클라이언트를 엽니다. [MCP 클라이언트 목록을](https://modelcontextprotocol.io/clients) 확인하세요. 여기서는 Claude Desktop을 구성하고 있습니다.
- 설정 > 개발자 > MCP 서버 로 이동하세요.
- 클릭 Edit Config하고 다음 구성으로 새 MCP 서버를 추가합니다. (빌드된 jar 파일 사용)
  ```json
  {
    "mcpServers": {
      "elasticsearch-server": {
        "command": "java",
        "args": [
          "-jar",
          "mcp-server.jar"
        ]
      }
    }
  }
  ```
