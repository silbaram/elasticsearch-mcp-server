import org.springframework.boot.gradle.tasks.bundling.BootJar

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false
jar.enabled = true

dependencies {
    // Elasticsearch Low-Level REST Client
    implementation("co.elastic.clients:elasticsearch-java:8.6.1")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Testcontainers 핵심 라이브러리
    testImplementation("org.testcontainers:testcontainers:1.20.6")
    // Elasticsearch 전용 컨테이너 지원
    testImplementation("org.testcontainers:elasticsearch:1.20.6")
    // JUnit5 연동 (필요 시)
    testImplementation("org.testcontainers:junit-jupiter:1.20.6")
    //annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}