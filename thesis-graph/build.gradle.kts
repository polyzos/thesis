plugins {
    kotlin("jvm")
}

dependencies {
    // Kotlin libs
    "implementation"(kotlin("stdlib-jdk8"))

    implementation(project(":thesis-commons"))

    // Neo4j
    implementation("org.neo4j:neo4j-ogm-core:3.0.2") {
        exclude(group="com.fasterxml.jackson.core")
    }

    implementation("org.neo4j:neo4j-ogm-bolt-driver:3.0.2") {
        exclude(group="com.fasterxml.jackson.core")
    }

    // Spark
    compile(group="org.apache.spark", name="spark-core_2.12", version= "2.4.0")
    compile(group="org.apache.spark", name="spark-sql_2.12", version= "2.4.0")
    compile(group="org.twitter4j", name= "twitter4j-core", version="4.0.7")
}