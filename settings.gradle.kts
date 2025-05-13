buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // yml 파싱용
        classpath("org.yaml:snakeyaml:2.1")
    }
}

rootProject.name = "elasticsearch-mcp-server"

include("mcp-server")

include("infrastructures")

include("infrastructures:elasticsearch-8.18.1")
include("infrastructures:elasticsearch-8.6.1")