import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false
jar.enabled = true

// 1) 파싱기 준비
val yaml = Yaml()

// 2) 리소스 디렉터리의 application.yml 파일 위치 지정
val ymlFile = file("../../mcp-server/src/main/resources/application.yml")

// 3) 파일을 맵으로 읽어들임
@Suppress("UNCHECKED_CAST")
val root: Map<String, Any> = yaml.load(FileInputStream(ymlFile)) as Map<String, Any>

// 4) nested 맵에서 버전 꺼내기
val esConfig = root["elasticsearch"] as Map<*, *>
val elasticsearchVersion = esConfig["version"] as String

dependencies {
    // Elasticsearch Java API Client
    implementation("co.elastic.clients:elasticsearch-java:$elasticsearchVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Test dependencies
    testImplementation("org.junit.platform:junit-platform-commons:1.10.0") // Align JUnit Platform Commons version
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
}

// Configure the test task to use JUnit Platform
tasks.withType<Test> {
    useJUnitPlatform()
}