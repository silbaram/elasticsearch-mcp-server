import org.springframework.boot.gradle.tasks.bundling.BootJar

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false
jar.enabled = true

dependencies {
    // Elasticsearch Low-Level REST Client
    implementation("co.elastic.clients:elasticsearch-java:8.18.1")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    //annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}