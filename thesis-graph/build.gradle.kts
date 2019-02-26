plugins {
    kotlin("jvm")
}

dependencies {
    // Kotlin libs
    "implementation"(kotlin("stdlib-jdk8"))

    testCompile("junit:junit:4.12")

    // Neo4j
    implementation("org.neo4j:neo4j-ogm-core:3.0.2")
    implementation("org.neo4j:neo4j-ogm-bolt-driver:3.0.2")

    // Json
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+")
}