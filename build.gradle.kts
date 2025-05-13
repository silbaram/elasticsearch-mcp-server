import org.gradle.kotlin.dsl.dependencies
import org.springframework.boot.gradle.tasks.bundling.BootJar

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false
jar.enabled = true

plugins {
    // Spring Boot 플러그인: 의존성 관리, 실행·패키징 지원
    id("org.springframework.boot") version "3.4.5"
    // Maven BOM(import) 기능 제공
    id("io.spring.dependency-management") version "1.1.7"
    // Spring Java Format 플러그인
    id("io.spring.javaformat") version "0.0.43"
    // Java 플러그인
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

allprojects {
    group = "com.silbaram.github"
    version = "0.0.1-SNAPSHOT"
    description = "Elasticsearch MCP Server"

    repositories {
        mavenCentral()

        maven("https://repo.spring.io/milestone")
        maven("https://repo.spring.io/snapshot")
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

subprojects {
    apply(plugin = "java")

    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    dependencyManagement {
        imports {
            // Spring AI BOM
            mavenBom("org.springframework.ai:spring-ai-bom:1.0.0-SNAPSHOT")
        }
    }

    dependencies {
        implementation("org.springframework.ai:spring-ai-starter-mcp-server-webflux")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    // 테스트 실행 시 JUnit Platform 사용
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

project(":mcp-server") {
    dependencies {
        //모듈 의존성 (mcp-server <- infrastructures:elasticsearch)
        implementation(project(":infrastructures:elasticsearch"))
    }
}

project(":infrastructures") {
    val jar: Jar by tasks
    val bootJar: BootJar by tasks

    bootJar.enabled = false
    jar.enabled = false

    // Disable Java compilation and resource processing
    tasks.withType<JavaCompile> {
        enabled = false
    }
    tasks.withType<ProcessResources> {
        enabled = false
    }
    // Disable classes and jar tasks (already present)
    tasks.named("classes") {
        enabled = false
    }
}
